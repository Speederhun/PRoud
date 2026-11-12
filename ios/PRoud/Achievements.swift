import Foundation

// Automatikusan generálva az Android-forrásból (gen_swift.py).
let SQUAT_A = "Barbell_Full_Squat"
let SQUAT_B = "Barbell_Squat"
let BENCH = "Barbell_Bench_Press_-_Medium_Grip"
let DEAD = "Barbell_Deadlift"
let OHP = "Barbell_Shoulder_Press"

func bestOf(_ s: Stats, _ ids: [String]) -> Double { ids.map { s.best[$0] ?? 0 }.max() ?? 0 }

let ACHIEVEMENTS: [Achievement] = {
    var a: [Achievement] = []
    a.append(Achievement(id: "w1", category: "Edzések", emoji: "🔰", title: "Első vas", desc: "Teljesítsd az első edzésed", target: 1.0, tier: 0, progress: { s in Double(s.workouts) }))
    a.append(Achievement(id: "w5", category: "Edzések", emoji: "🔥", title: "Bemelegítés", desc: "5 edzés a naplóban", target: 5.0, tier: 0, progress: { s in Double(s.workouts) }))
    a.append(Achievement(id: "w10", category: "Edzések", emoji: "💪", title: "Rendszeres", desc: "10 edzés a naplóban", target: 10.0, tier: 1, progress: { s in Double(s.workouts) }))
    a.append(Achievement(id: "w25", category: "Edzések", emoji: "🏅", title: "Törzsvendég", desc: "25 edzés a naplóban", target: 25.0, tier: 1, progress: { s in Double(s.workouts) }))
    a.append(Achievement(id: "w50", category: "Edzések", emoji: "🥈", title: "Vasbarát", desc: "50 edzés a naplóban", target: 50.0, tier: 2, progress: { s in Double(s.workouts) }))
    a.append(Achievement(id: "w100", category: "Edzések", emoji: "🏆", title: "Százas klub", desc: "100 edzés a naplóban", target: 100.0, tier: 3, progress: { s in Double(s.workouts) }))
    a.append(Achievement(id: "w250", category: "Edzések", emoji: "👑", title: "Teremlakó", desc: "250 edzés a naplóban", target: 250.0, tier: 3, progress: { s in Double(s.workouts) }))
    a.append(Achievement(id: "v1k", category: "Mozgatott súly", emoji: "⚖️", title: "Egy tonna", desc: "Mozgass meg összesen 1 000 kg-ot", target: 1000.0, tier: 0, progress: { s in s.volume }))
    a.append(Achievement(id: "v10k", category: "Mozgatott súly", emoji: "🚚", title: "Teherautó", desc: "Összesen 10 000 kg", target: 10000.0, tier: 1, progress: { s in s.volume }))
    a.append(Achievement(id: "v50k", category: "Mozgatott súly", emoji: "📦", title: "Konténer", desc: "Összesen 50 000 kg", target: 50000.0, tier: 2, progress: { s in s.volume }))
    a.append(Achievement(id: "v100k", category: "Mozgatott súly", emoji: "🚢", title: "Teherhajó", desc: "Összesen 100 000 kg", target: 100000.0, tier: 2, progress: { s in s.volume }))
    a.append(Achievement(id: "v500k", category: "Mozgatott súly", emoji: "🚀", title: "Rakéta", desc: "Összesen 500 000 kg", target: 500000.0, tier: 3, progress: { s in s.volume }))
    a.append(Achievement(id: "v1m", category: "Mozgatott súly", emoji: "⛰️", title: "Hegymozgató", desc: "Összesen 1 000 000 kg", target: 1000000.0, tier: 3, progress: { s in s.volume }))
    a.append(Achievement(id: "s50", category: "Szettek", emoji: "🎯", title: "Ötven szett", desc: "50 teljesített szett", target: 50.0, tier: 0, progress: { s in Double(s.sets) }))
    a.append(Achievement(id: "s250", category: "Szettek", emoji: "🎲", title: "Szettgyűjtő", desc: "250 teljesített szett", target: 250.0, tier: 1, progress: { s in Double(s.sets) }))
    a.append(Achievement(id: "s1000", category: "Szettek", emoji: "🤖", title: "Szettgép", desc: "1000 teljesített szett", target: 1000.0, tier: 3, progress: { s in Double(s.sets) }))
    a.append(Achievement(id: "pr1", category: "Rekordok", emoji: "🌟", title: "Első rekord", desc: "Állíts fel egy új egyéni rekordot", target: 1.0, tier: 0, progress: { s in Double(s.prs) }))
    a.append(Achievement(id: "pr10", category: "Rekordok", emoji: "⭐", title: "Rekordvadász", desc: "10 egyéni rekord", target: 10.0, tier: 1, progress: { s in Double(s.prs) }))
    a.append(Achievement(id: "pr25", category: "Rekordok", emoji: "💫", title: "Rekordgyár", desc: "25 egyéni rekord", target: 25.0, tier: 2, progress: { s in Double(s.prs) }))
    a.append(Achievement(id: "pr100", category: "Rekordok", emoji: "🌠", title: "Csillagpor", desc: "100 egyéni rekord", target: 100.0, tier: 3, progress: { s in Double(s.prs) }))
    a.append(Achievement(id: "st2", category: "Széria", emoji: "📆", title: "Lendületben", desc: "2 hét egymás után a heti cél teljesítve", target: 2.0, tier: 0, progress: { s in Double(s.bestStreak) }))
    a.append(Achievement(id: "st4", category: "Széria", emoji: "🗓️", title: "Egy hónap vas", desc: "4 hét egymás után a heti cél teljesítve", target: 4.0, tier: 1, progress: { s in Double(s.bestStreak) }))
    a.append(Achievement(id: "st8", category: "Széria", emoji: "🔥", title: "Megállíthatatlan", desc: "8 hét egymás után", target: 8.0, tier: 2, progress: { s in Double(s.bestStreak) }))
    a.append(Achievement(id: "st12", category: "Széria", emoji: "☄️", title: "Negyedév", desc: "12 hét egymás után", target: 12.0, tier: 3, progress: { s in Double(s.bestStreak) }))
    a.append(Achievement(id: "st26", category: "Széria", emoji: "🌋", title: "Fél év acél", desc: "26 hét egymás után", target: 26.0, tier: 3, progress: { s in Double(s.bestStreak) }))
    a.append(Achievement(id: "bn40", category: "Erő", emoji: "🛏️", title: "Fekvenyomó", desc: "Fekvenyomás 40 kg-mal", target: 40.0, tier: 0, progress: { s in bestOf(s, [BENCH]) }))
    a.append(Achievement(id: "bn60", category: "Erő", emoji: "💥", title: "Hatvanas mell", desc: "Fekvenyomás 60 kg-mal", target: 60.0, tier: 1, progress: { s in bestOf(s, [BENCH]) }))
    a.append(Achievement(id: "bn100", category: "Erő", emoji: "🦍", title: "Százas fekvő", desc: "Fekvenyomás 100 kg-mal", target: 100.0, tier: 3, progress: { s in bestOf(s, [BENCH]) }))
    a.append(Achievement(id: "sq60", category: "Erő", emoji: "🦵", title: "Guggoló", desc: "Guggolás 60 kg-mal", target: 60.0, tier: 0, progress: { s in bestOf(s, [SQUAT_A, SQUAT_B]) }))
    a.append(Achievement(id: "sq100", category: "Erő", emoji: "🏋️", title: "Százas guggolás", desc: "Guggolás 100 kg-mal", target: 100.0, tier: 2, progress: { s in bestOf(s, [SQUAT_A, SQUAT_B]) }))
    a.append(Achievement(id: "sq140", category: "Erő", emoji: "🐘", title: "Elefántlábak", desc: "Guggolás 140 kg-mal", target: 140.0, tier: 3, progress: { s in bestOf(s, [SQUAT_A, SQUAT_B]) }))
    a.append(Achievement(id: "dl80", category: "Erő", emoji: "🧲", title: "Felhúzó", desc: "Felhúzás 80 kg-mal", target: 80.0, tier: 0, progress: { s in bestOf(s, [DEAD]) }))
    a.append(Achievement(id: "dl140", category: "Erő", emoji: "⚙️", title: "Erőgép", desc: "Felhúzás 140 kg-mal", target: 140.0, tier: 2, progress: { s in bestOf(s, [DEAD]) }))
    a.append(Achievement(id: "dl200", category: "Erő", emoji: "🦖", title: "Kétszázas húzás", desc: "Felhúzás 200 kg-mal", target: 200.0, tier: 3, progress: { s in bestOf(s, [DEAD]) }))
    a.append(Achievement(id: "ohp40", category: "Erő", emoji: "🙌", title: "Vállból nyomó", desc: "Vállból nyomás 40 kg-mal", target: 40.0, tier: 1, progress: { s in bestOf(s, [OHP]) }))
    a.append(Achievement(id: "big300", category: "Erő", emoji: "🔱", title: "Big Three 300", desc: "A guggolás + fekvenyomás + felhúzás legjobbjai együtt: 300 kg", target: 300.0, tier: 1, progress: { s in s.bigThree }))
    a.append(Achievement(id: "big400", category: "Erő", emoji: "⚔️", title: "Big Three 400", desc: "A három nagy gyakorlat összesen: 400 kg", target: 400.0, tier: 2, progress: { s in s.bigThree }))
    a.append(Achievement(id: "big500", category: "Erő", emoji: "🐉", title: "Big Three 500", desc: "A három nagy gyakorlat összesen: 500 kg", target: 500.0, tier: 3, progress: { s in s.bigThree }))
    a.append(Achievement(id: "x5", category: "Változatosság", emoji: "🎨", title: "Kísérletező", desc: "5 különböző gyakorlat kipróbálva", target: 5.0, tier: 0, progress: { s in Double(s.distinct) }))
    a.append(Achievement(id: "x15", category: "Változatosság", emoji: "🧰", title: "Eszköztár", desc: "15 különböző gyakorlat", target: 15.0, tier: 1, progress: { s in Double(s.distinct) }))
    a.append(Achievement(id: "x40", category: "Változatosság", emoji: "📚", title: "Gyakorlat-enciklopédia", desc: "40 különböző gyakorlat", target: 40.0, tier: 2, progress: { s in Double(s.distinct) }))
    a.append(Achievement(id: "t1", category: "Különleges", emoji: "🐜", title: "Tonna egy menetben", desc: "Egyetlen edzésen mozgass meg 1 000 kg-ot", target: 1000.0, tier: 1, progress: { s in s.bestSessionVolume }))
    a.append(Achievement(id: "t5", category: "Különleges", emoji: "🐋", title: "Öt tonna", desc: "Egyetlen edzésen mozgass meg 5 000 kg-ot", target: 5000.0, tier: 2, progress: { s in s.bestSessionVolume }))
    a.append(Achievement(id: "long", category: "Különleges", emoji: "⏱️", title: "Maraton", desc: "Egy edzés, ami legalább 90 percig tart", target: 90.0, tier: 1, progress: { s in Double(s.longestMin) }))
    a.append(Achievement(id: "early", category: "Különleges", emoji: "🌅", title: "Korán kelő", desc: "Kezdj edzést reggel 7 óra előtt", target: 1.0, tier: 1, progress: { s in Double(s.early) }))
    a.append(Achievement(id: "night", category: "Különleges", emoji: "🦉", title: "Éjjeli bagoly", desc: "Kezdj edzést este 9 óra után", target: 1.0, tier: 1, progress: { s in Double(s.night) }))
    a.append(Achievement(id: "day25", category: "Különleges", emoji: "💣", title: "Szetthalmozó", desc: "25 szett egyetlen napon", target: 25.0, tier: 2, progress: { s in Double(s.maxSetsInDay) }))
    a.append(Achievement(id: "fb", category: "Különleges", emoji: "🧬", title: "Teljes test hete", desc: "Egy héten edzd meg a mellet, hátat, lábat, vállat, kart és core-t", target: 1.0, tier: 2, progress: { s in Double(s.fullBodyWeeks) }))
    return a
}()
