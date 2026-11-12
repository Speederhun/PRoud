import Foundation

struct Stats {
    var workouts = 0
    var volume = 0.0
    var sets = 0
    var prs = 0
    var streak = 0
    var bestStreak = 0
    var distinct = 0
    var bestSessionVolume = 0.0
    var longestMin = 0
    var early = 0
    var night = 0
    var best: [String: Double] = [:]
    var fullBodyWeeks = 0
    var bigThree = 0.0
    var maxSetsInDay = 0
    var xp = 0
    var workoutsThisWeek = 0
}

struct Achievement: Identifiable {
    let id: String
    let category: String
    let emoji: String
    let title: String
    let desc: String
    let target: Double
    let tier: Int
    let progress: (Stats) -> Double
    var xp: Int {
        switch tier {
        case 0: return 50
        case 1: return 100
        case 2: return 200
        default: return 400
        }
    }
}

let ACH_BY_ID: [String: Achievement] = Dictionary(ACHIEVEMENTS.map { ($0.id, $0) }, uniquingKeysWith: { a, _ in a })

func sessionXp(_ s: Session) -> Int {
    50 + s.setCount * 5 + Int(s.volume / 100.0) + s.prCount * 25
}

func epochDay(_ d: Date) -> Int {
    var cal = Calendar(identifier: .gregorian)
    cal.timeZone = TimeZone.current
    let comps = cal.dateComponents([.year, .month, .day], from: d)
    var utc = Calendar(identifier: .gregorian)
    utc.timeZone = TimeZone(identifier: "UTC")!
    let u = utc.date(from: comps)!
    return Int(floor(u.timeIntervalSince1970 / 86400.0))
}

func weekIndex(_ epochDay: Int) -> Int {
    Int(floor(Double(epochDay + 3) / 7.0))
}

func computeStats(_ data: AppData) -> Stats {
    var st = Stats()
    let goal = data.settings.weeklyGoal
    var distinct = Set<String>()
    var setsPerDay: [Int: Int] = [:]
    var weekCounts: [Int: Int] = [:]
    var weekGroups: [Int: Set<String>] = [:]
    let cal = Calendar.current

    for s in data.sessions {
        st.volume += s.volume
        st.sets += s.setCount
        st.prs += s.prCount
        if s.volume > st.bestSessionVolume { st.bestSessionVolume = s.volume }
        if s.minutes > st.longestMin { st.longestMin = s.minutes }
        let hour = cal.component(.hour, from: s.startedAt)
        if hour < 7 { st.early += 1 }
        if hour >= 21 { st.night += 1 }
        let day = epochDay(s.startedAt)
        setsPerDay[day, default: 0] += s.setCount
        let week = weekIndex(day)
        weekCounts[week, default: 0] += 1
        for e in s.exercises {
            distinct.insert(e.exerciseId)
            for x in e.sets where x.weight > (st.best[e.exerciseId] ?? 0) {
                st.best[e.exerciseId] = x.weight
            }
            if let ex = ExerciseRepo.get(e.exerciseId) {
                for m in ex.primary {
                    if let g = Hu.groupOf(m) { weekGroups[week, default: []].insert(g) }
                }
            }
        }
        st.xp += sessionXp(s)
    }
    for id in data.unlocked.keys { st.xp += ACH_BY_ID[id]?.xp ?? 0 }
    for k in data.challenges { st.xp += challengeXp(k) }

    let okWeeks = Set(weekCounts.filter { $0.value >= goal }.keys)
    let thisWeek = weekIndex(epochDay(Date()))
    var cur = okWeeks.contains(thisWeek) ? thisWeek : thisWeek - 1
    var streak = 0
    while okWeeks.contains(cur) { streak += 1; cur -= 1 }
    var bestStreak = 0, run = 0
    var prev: Int? = nil
    for w in okWeeks.sorted() {
        if let p = prev, w == p + 1 { run += 1 } else { run = 1 }
        bestStreak = max(bestStreak, run)
        prev = w
    }
    let squat = max(st.best[SQUAT_A] ?? 0, st.best[SQUAT_B] ?? 0)
    st.workouts = data.sessions.count
    st.streak = streak
    st.bestStreak = max(bestStreak, streak)
    st.distinct = distinct.count
    st.fullBodyWeeks = weekGroups.values.filter { $0.count >= 6 }.count
    st.bigThree = squat + (st.best[BENCH] ?? 0) + (st.best[DEAD] ?? 0)
    st.maxSetsInDay = setsPerDay.values.max() ?? 0
    st.workoutsThisWeek = weekCounts[thisWeek] ?? 0
    return st
}

struct LevelInfo {
    let level: Int
    let title: String
    let xpInto: Int
    let xpNeeded: Int
}

func levelTitle(_ level: Int) -> String {
    switch level {
    case 25...: return "Legenda"
    case 18...: return "Bajnok"
    case 14...: return "Erőgép"
    case 10...: return "Vasmarok"
    case 7...: return "Súlyemelő"
    case 5...: return "Lelkes"
    case 3...: return "Vasbarát"
    case 2...: return "Kezdő"
    default: return "Újonc"
    }
}

func levelInfo(_ xp: Int) -> LevelInfo {
    let level = 1 + Int(floor((Double(xp) / 150.0).squareRoot()))
    let base = 150 * (level - 1) * (level - 1)
    let next = 150 * level * level
    return LevelInfo(level: level, title: levelTitle(level), xpInto: xp - base, xpNeeded: next - base)
}
