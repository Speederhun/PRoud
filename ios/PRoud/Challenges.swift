import Foundation

struct Challenge: Identifiable {
    let key: String
    let emoji: String
    let title: String
    let current: Double
    let target: Double
    let xp: Int
    var id: String { key }
    var done: Bool { current >= target }
}

private struct WeekStats {
    var workouts = 0
    var volume = 0.0
    var sets = 0
    var prs = 0
    var groups = 0
    var newExercises = 0
    var early = 0
}

private func weekStats(_ week: Int, _ data: AppData) -> WeekStats {
    var w = WeekStats()
    var earlier = Set<String>()
    for s in data.sessions where weekIndex(epochDay(s.startedAt)) < week {
        for e in s.exercises { earlier.insert(e.exerciseId) }
    }
    var newEx = Set<String>()
    var groups = Set<String>()
    for s in data.sessions where weekIndex(epochDay(s.startedAt)) == week {
        w.workouts += 1
        w.volume += s.volume
        w.sets += s.setCount
        w.prs += s.prCount
        if Calendar.current.component(.hour, from: s.startedAt) < 8 { w.early += 1 }
        for e in s.exercises {
            if !earlier.contains(e.exerciseId) { newEx.insert(e.exerciseId) }
            if let ex = ExerciseRepo.get(e.exerciseId) {
                for m in ex.primary { if let g = Hu.groupOf(m) { groups.insert(g) } }
            }
        }
    }
    w.groups = groups.count
    w.newExercises = newEx.count
    return w
}

func challengesFor(_ week: Int, _ data: AppData) -> [Challenge] {
    let w = weekStats(week, data)
    let goal = data.settings.weeklyGoal
    func mk(_ id: Int, _ emoji: String, _ title: String, _ cur: Double, _ target: Double, _ xp: Int) -> Challenge {
        Challenge(key: "\(week)|\(id)|\(xp)", emoji: emoji, title: title, current: cur, target: target, xp: xp)
    }
    let pool = [
        mk(0, "📅", "Edzz \(goal)-szor ezen a héten", Double(w.workouts), Double(goal), 75),
        mk(1, "⚖️", "Mozgass meg 5 000 kg-ot a héten", w.volume, 5000, 75),
        mk(2, "🏆", "Állíts fel 2 új rekordot", Double(w.prs), 2, 100),
        mk(3, "🎯", "Teljesíts 40 szettet a héten", Double(w.sets), 40, 75),
        mk(4, "🧬", "Edzd meg mind a 6 izomcsoportot", Double(w.groups), 6, 125),
        mk(5, "🎨", "Próbálj ki 3 új gyakorlatot", Double(w.newExercises), 3, 100),
        mk(6, "🌅", "Edzz egyszer reggel 8 óra előtt", Double(w.early), 1, 75),
    ]
    return (0..<3).map { k in
        let idx = (((week * 3 + k) % 7) + 7) % 7
        return pool[idx]
    }
}

func challengeXp(_ key: String) -> Int {
    Int(key.split(separator: "|").last ?? "") ?? 0
}

struct RecordRow: Identifiable {
    let exerciseId: String
    let weight: Double
    let reps: Int
    let oneRm: Double
    var id: String { exerciseId }
}

func recordRows(_ data: AppData, limit: Int = 10) -> [RecordRow] {
    var best: [String: RecordRow] = [:]
    for s in data.sessions {
        for e in s.exercises {
            for st in e.sets where st.weight > 0 {
                let sc = score(st.weight, st.reps)
                if best[e.exerciseId] == nil || sc > best[e.exerciseId]!.oneRm {
                    best[e.exerciseId] = RecordRow(exerciseId: e.exerciseId, weight: st.weight, reps: st.reps, oneRm: sc)
                }
            }
        }
    }
    return Array(best.values.sorted { $0.oneRm > $1.oneRm }.prefix(limit))
}
