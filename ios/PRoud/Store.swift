import Foundation
import SwiftUI

final class Store: ObservableObject {
    @Published var data = AppData()
    private let fileURL: URL = {
        let dir = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0]
        return dir.appendingPathComponent("proud.json")
    }()

    init() {
        if let d = try? Data(contentsOf: fileURL),
           let decoded = try? JSONDecoder().decode(AppData.self, from: d) {
            data = decoded
        }
        UserDefaults.standard.set(data.settings.theme, forKey: "proud.theme")
        migrate()
    }

    private func migrate() {
        if data.seed < Defaults.seedVersion {
            let defaultIds = Set(Defaults.templates().map { $0.id })
            let custom = data.templates.filter { !defaultIds.contains($0.id) && !$0.id.hasPrefix("d_") }
            data.templates = Defaults.templates() + custom
            data.seed = Defaults.seedVersion
            save()
        }
    }

    func save() {
        if let d = try? JSONEncoder().encode(data) { try? d.write(to: fileURL, options: .atomic) }
    }

    // MARK: - Aktív edzés

    func lastWeight(_ exerciseId: String) -> Double {
        for s in data.sessions.sorted(by: { $0.startedAt > $1.startedAt }) {
            if let e = s.exercises.first(where: { $0.exerciseId == exerciseId }),
               let w = e.sets.map({ $0.weight }).max() { return w }
        }
        return 0
    }

    func start(_ t: Template) {
        let now = Int(Date().timeIntervalSince1970 * 1000)
        var n = 0
        let exs = t.items.map { item -> ActiveExercise in
            let w = lastWeight(item.exerciseId)
            let sets = (0..<item.sets).map { _ -> SetEntry in
                n += 1
                return SetEntry(id: now + n, weight: w, reps: item.reps, done: false, warmup: false)
            }
            return ActiveExercise(exerciseId: item.exerciseId, sets: sets)
        }
        data.active = ActiveWorkout(startedAt: Date(), name: t.name, exercises: exs)
        save()
    }

    func startEmpty() {
        data.active = ActiveWorkout(startedAt: Date(), name: "Szabad edzés", exercises: [])
        save()
    }

    func mutateActive(_ f: (inout ActiveWorkout) -> Void) {
        guard var a = data.active else { return }
        f(&a)
        data.active = a
        save()
    }

    func addExercise(_ id: String) {
        mutateActive { a in
            guard !a.exercises.contains(where: { $0.exerciseId == id }) else { return }
            let w = lastWeight(id)
            let sid = Int(Date().timeIntervalSince1970 * 1000)
            a.exercises.append(ActiveExercise(exerciseId: id, sets: [SetEntry(id: sid, weight: w, reps: 10, done: false, warmup: false)]))
        }
    }

    func removeExercise(_ id: String) {
        mutateActive { $0.exercises.removeAll { $0.exerciseId == id } }
    }

    func addSet(_ exId: String) {
        mutateActive { a in
            guard let i = a.exercises.firstIndex(where: { $0.exerciseId == exId }) else { return }
            let last = a.exercises[i].sets.last(where: { !$0.warmup })
            let sid = Int(Date().timeIntervalSince1970 * 1000)
            a.exercises[i].sets.append(SetEntry(id: sid, weight: last?.weight ?? 0, reps: last?.reps ?? 10, done: false, warmup: false))
        }
    }

    func removeSet(_ exId: String, _ setId: Int) {
        mutateActive { a in
            guard let i = a.exercises.firstIndex(where: { $0.exerciseId == exId }) else { return }
            a.exercises[i].sets.removeAll { $0.id == setId }
        }
    }

    func updateSet(_ exId: String, _ setId: Int, _ f: (inout SetEntry) -> Void) {
        mutateActive { a in
            guard let i = a.exercises.firstIndex(where: { $0.exerciseId == exId }),
                  let j = a.exercises[i].sets.firstIndex(where: { $0.id == setId }) else { return }
            f(&a.exercises[i].sets[j])
        }
    }

    func replaceExercise(_ oldId: String, _ newId: String) {
        mutateActive { a in
            guard let i = a.exercises.firstIndex(where: { $0.exerciseId == oldId }),
                  !a.exercises.contains(where: { $0.exerciseId == newId }) else { return }
            let w = lastWeight(newId)
            a.exercises[i].exerciseId = newId
            for j in a.exercises[i].sets.indices where !a.exercises[i].sets[j].done {
                a.exercises[i].sets[j].weight = w
            }
        }
    }

    func addWarmups(_ exId: String) {
        mutateActive { a in
            guard let i = a.exercises.firstIndex(where: { $0.exerciseId == exId }),
                  let work = a.exercises[i].sets.first(where: { !$0.warmup }), work.weight > 0,
                  !a.exercises[i].sets.contains(where: { $0.warmup }) else { return }
            let base = Int(Date().timeIntervalSince1970 * 1000)
            let plan: [(Double, Int)] = [(0.4, 5), (0.6, 3), (0.8, 2)]
            var ws: [SetEntry] = []
            for (k, p) in plan.enumerated() {
                let w = max(2.5, (work.weight * p.0 / 2.5).rounded() * 2.5)
                ws.append(SetEntry(id: base + k, weight: w, reps: p.1, done: false, warmup: true))
            }
            a.exercises[i].sets.insert(contentsOf: ws, at: 0)
        }
    }

    func discardActive() {
        data.active = nil
        save()
    }

    func finishActive() -> FinishResult? {
        guard let a = data.active else { return nil }
        let before = computeStats(data)
        var bestScore: [String: Double] = [:]
        for s in data.sessions {
            for e in s.exercises {
                for x in e.sets {
                    bestScore[e.exerciseId] = max(bestScore[e.exerciseId] ?? 0, score(x.weight, x.reps))
                }
            }
        }
        var exs: [SessionExercise] = []
        for e in a.exercises {
            var sets: [FinishedSet] = []
            for x in e.sets where x.done && !x.warmup {
                let sc = score(x.weight, x.reps)
                let prev = bestScore[e.exerciseId] ?? 0
                let isPr = sc > prev && prev > 0
                bestScore[e.exerciseId] = max(prev, sc)
                sets.append(FinishedSet(weight: x.weight, reps: x.reps, isPr: isPr))
            }
            if !sets.isEmpty { exs.append(SessionExercise(exerciseId: e.exerciseId, sets: sets)) }
        }
        guard !exs.isEmpty else { return nil }
        let session = Session(id: UUID().uuidString, startedAt: a.startedAt, endedAt: Date(), name: a.name, exercises: exs)
        data.sessions.append(session)
        var newCh: [Challenge] = []
        let wk = weekIndex(epochDay(session.startedAt))
        for c in challengesFor(wk, data) where c.done && !data.challenges.contains(c.key) {
            data.challenges.append(c.key)
            newCh.append(c)
        }
        let after = computeStats(data)
        var newAch: [Achievement] = []
        for ach in ACHIEVEMENTS where data.unlocked[ach.id] == nil && ach.progress(after) >= ach.target {
            data.unlocked[ach.id] = Date().timeIntervalSince1970
            newAch.append(ach)
        }
        data.active = nil
        save()
        let final = computeStats(data)
        return FinishResult(session: session, newAchievements: newAch, xpGained: final.xp - before.xp,
                            levelBefore: levelInfo(before.xp).level, levelAfter: levelInfo(final.xp).level, newChallenges: newCh)
    }

    func setWeeklyGoal(_ g: Int) { data.settings.weeklyGoal = g; save() }
    func setRest(_ s: Int) { data.settings.restSeconds = s; save() }

    func setTheme(_ t: String) {
        UserDefaults.standard.set(t, forKey: "proud.theme")
        data.settings.theme = t
        save()
    }

    func setWeightAll(_ exId: String, _ weight: Double) {
        mutateActive { a in
            guard let i = a.exercises.firstIndex(where: { $0.exerciseId == exId }) else { return }
            for j in a.exercises[i].sets.indices where !a.exercises[i].sets[j].done && !a.exercises[i].sets[j].warmup {
                a.exercises[i].sets[j].weight = weight
            }
        }
    }

    func toggleSuperset(_ exId: String) {
        mutateActive { a in
            guard let i = a.exercises.firstIndex(where: { $0.exerciseId == exId }) else { return }
            a.exercises[i].superset.toggle()
        }
    }

    /// Az utolsó edzés munkaszettjei az adott gyakorlatból.
    func lastSets(_ exId: String) -> [FinishedSet] {
        for s in data.sessions.sorted(by: { $0.startedAt > $1.startedAt }) {
            if let e = s.exercises.first(where: { $0.exerciseId == exId }) { return e.sets }
        }
        return []
    }

    func saveTemplate(_ t: Template) {
        if let i = data.templates.firstIndex(where: { $0.id == t.id }) { data.templates[i] = t }
        else { data.templates.append(t) }
        save()
    }

    func deleteTemplate(_ id: String) {
        data.templates.removeAll { $0.id == id }
        data.plans.removeAll { $0.templateId == id }
        save()
    }

    func addPlan(_ day: Int, _ templateId: String) {
        data.plans.append(Plan(epochDay: day, templateId: templateId))
        save()
    }

    func removePlan(_ day: Int, _ templateId: String) {
        if let i = data.plans.firstIndex(where: { $0.epochDay == day && $0.templateId == templateId }) {
            data.plans.remove(at: i)
            save()
        }
    }

    func addBody(_ type: String, _ value: Double) {
        let day = epochDay(Date())
        data.body.removeAll { $0.epochDay == day && $0.type == type }
        data.body.append(BodyEntry(epochDay: day, type: type, value: value))
        save()
    }

    func removeBody(_ e: BodyEntry) {
        data.body.removeAll { $0 == e }
        save()
    }
}
