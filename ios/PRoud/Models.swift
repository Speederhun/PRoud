import Foundation

struct Exercise: Identifiable, Hashable {
    let id: String
    let name: String
    let primary: [String]
    let secondary: [String]
    let equipment: String
    let level: String
    let mechanic: String
    let instructions: [String]
    let images: [String]
    var huName: String { Hu.exerciseName(id) ?? name }
    var imageUrls: [URL] { images.compactMap { URL(string: Hu.imgBase + $0) } }
}

struct RawExercise: Decodable {
    let id: String
    let n: String
    let p: [String]?
    let s: [String]?
    let e: String?
    let l: String?
    let m: String?
    let i: [String]?
    let im: [String]?
}

struct TemplateItem: Codable, Hashable {
    var exerciseId: String
    var sets: Int
    var reps: Int
}

struct Template: Codable, Identifiable, Hashable {
    var id: String
    var name: String
    var emoji: String
    var group: String
    var items: [TemplateItem]
}

struct SetEntry: Codable, Identifiable, Hashable {
    var id: Int
    var weight: Double
    var reps: Int
    var done: Bool
    var warmup: Bool
}

struct ActiveExercise: Codable, Identifiable, Hashable {
    var exerciseId: String
    var sets: [SetEntry]
    var superset: Bool = false
    var id: String { exerciseId }

    init(exerciseId: String, sets: [SetEntry], superset: Bool = false) {
        self.exerciseId = exerciseId; self.sets = sets; self.superset = superset
    }
    init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        exerciseId = try c.decode(String.self, forKey: .exerciseId)
        sets = try c.decode([SetEntry].self, forKey: .sets)
        superset = try c.decodeIfPresent(Bool.self, forKey: .superset) ?? false
    }
}

struct ActiveWorkout: Codable {
    var startedAt: Date
    var name: String
    var exercises: [ActiveExercise]
}

struct FinishedSet: Codable, Hashable {
    var weight: Double
    var reps: Int
    var isPr: Bool
}

struct SessionExercise: Codable, Hashable {
    var exerciseId: String
    var sets: [FinishedSet]
}

struct Session: Codable, Identifiable, Hashable {
    var id: String
    var startedAt: Date
    var endedAt: Date
    var name: String
    var exercises: [SessionExercise]

    var volume: Double { exercises.reduce(0) { $0 + $1.sets.reduce(0) { $0 + $1.weight * Double($1.reps) } } }
    var setCount: Int { exercises.reduce(0) { $0 + $1.sets.count } }
    var prCount: Int { exercises.reduce(0) { $0 + $1.sets.filter { $0.isPr }.count } }
    var minutes: Int { Int(endedAt.timeIntervalSince(startedAt) / 60) }
}

struct Settings: Codable {
    var weeklyGoal: Int = 3
    var restSeconds: Int = 90
    var theme: String = "premium"

    init() {}
    init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        weeklyGoal = try c.decodeIfPresent(Int.self, forKey: .weeklyGoal) ?? 3
        restSeconds = try c.decodeIfPresent(Int.self, forKey: .restSeconds) ?? 90
        theme = try c.decodeIfPresent(String.self, forKey: .theme) ?? "premium"
    }
}

struct Plan: Codable, Hashable {
    var epochDay: Int
    var templateId: String
}

struct BodyEntry: Codable, Hashable {
    var epochDay: Int
    var type: String
    var value: Double
}

struct AppData: Codable {
    var templates: [Template] = []
    var sessions: [Session] = []
    var unlocked: [String: Double] = [:]
    var settings = Settings()
    var active: ActiveWorkout? = nil
    var seed: Int = 0
    var challenges: [String] = []
    var body: [BodyEntry] = []
    var plans: [Plan] = []

    init() {}
    init(from decoder: Decoder) throws {
        let c = try decoder.container(keyedBy: CodingKeys.self)
        templates = try c.decodeIfPresent([Template].self, forKey: .templates) ?? []
        sessions = try c.decodeIfPresent([Session].self, forKey: .sessions) ?? []
        unlocked = try c.decodeIfPresent([String: Double].self, forKey: .unlocked) ?? [:]
        settings = try c.decodeIfPresent(Settings.self, forKey: .settings) ?? Settings()
        active = try c.decodeIfPresent(ActiveWorkout.self, forKey: .active)
        seed = try c.decodeIfPresent(Int.self, forKey: .seed) ?? 0
        challenges = try c.decodeIfPresent([String].self, forKey: .challenges) ?? []
        body = try c.decodeIfPresent([BodyEntry].self, forKey: .body) ?? []
        plans = try c.decodeIfPresent([Plan].self, forKey: .plans) ?? []
    }
}

struct FinishResult {
    var session: Session
    var newAchievements: [Achievement]
    var xpGained: Int
    var levelBefore: Int
    var levelAfter: Int
    var newChallenges: [Challenge] = []
}

func score(_ weight: Double, _ reps: Int) -> Double {
    if weight > 0 { return reps <= 1 ? weight : weight * (1.0 + Double(reps) / 30.0) }
    return Double(reps)
}

func fmtKg(_ w: Double) -> String {
    if w == w.rounded() { return String(Int(w)) }
    return String(format: "%.1f", w)
}

func fmtVolume(_ v: Double) -> String {
    let n = Int(v.rounded())
    if n >= 10000 { return String(format: "%.1f t", Double(n) / 1000.0) }
    return "\(n) kg"
}

func fmtDuration(_ total: Int) -> String {
    let s = max(0, total)
    let h = s / 3600, m = (s % 3600) / 60, sec = s % 60
    if h > 0 { return String(format: "%d:%02d:%02d", h, m, sec) }
    return String(format: "%d:%02d", m, sec)
}

enum ExerciseRepo {
    static var all: [Exercise] = []
    private static var byId: [String: Exercise] = [:]

    static func load() {
        guard all.isEmpty,
              let url = Bundle.main.url(forResource: "exercises", withExtension: "json"),
              let data = try? Data(contentsOf: url),
              let raws = try? JSONDecoder().decode([RawExercise].self, from: data) else { return }
        let list = raws.map {
            Exercise(id: $0.id, name: $0.n, primary: $0.p ?? [], secondary: $0.s ?? [],
                     equipment: $0.e ?? "other", level: $0.l ?? "beginner", mechanic: $0.m ?? "",
                     instructions: $0.i ?? [], images: $0.im ?? [])
        }
        byId = Dictionary(list.map { ($0.id, $0) }, uniquingKeysWith: { a, _ in a })
        all = list.sorted { $0.huName.lowercased() < $1.huName.lowercased() }
    }

    static func get(_ id: String) -> Exercise? { byId[id] }
    static func nameOf(_ id: String) -> String { byId[id]?.huName ?? id }
}
