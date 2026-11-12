import SwiftUI

struct ProfileView: View {
    @EnvironmentObject var store: Store

    var body: some View {
        let st = computeStats(store.data)
        let li = levelInfo(st.xp)
        let done = ACHIEVEMENTS.filter { store.data.unlocked[$0.id] != nil }.count
        return NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 14) {
                    Card {
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Lv \(li.level) · \(li.title)").font(.title2.bold()).foregroundColor(Theme.accent)
                            ProgressBarView(value: Double(li.xpInto) / Double(max(1, li.xpNeeded)))
                            Text("\(st.xp) XP összesen").font(.caption).foregroundColor(Theme.muted)
                            HStack {
                                mini("Edzés", "\(st.workouts)")
                                mini("Volumen", fmtVolume(st.volume))
                                mini("Rekord", "\(st.prs)")
                                mini("Széria", "\(st.bestStreak) hét")
                            }
                        }
                    }
                    Card {
                        VStack(alignment: .leading, spacing: 10) {
                            Text("Beállítások").font(.headline).foregroundColor(Theme.text)
                            Stepper("Heti cél: \(store.data.settings.weeklyGoal) edzés",
                                    value: Binding(get: { store.data.settings.weeklyGoal }, set: { store.setWeeklyGoal($0) }), in: 1...7)
                                .foregroundColor(Theme.text)
                            Stepper("Pihenő: \(store.data.settings.restSeconds) mp",
                                    value: Binding(get: { store.data.settings.restSeconds }, set: { store.setRest($0) }), in: 15...300, step: 15)
                                .foregroundColor(Theme.text)
                        }
                    }
                    Text("Achievementek \(done)/\(ACHIEVEMENTS.count)").font(.headline).foregroundColor(Theme.text)
                    ForEach(ACHIEVEMENTS) { a in
                        achievementRow(a, st)
                    }
                }.padding()
            }
            .background(Theme.bg.ignoresSafeArea())
            .navigationTitle("Profil")
        }
    }

    private func mini(_ l: String, _ v: String) -> some View {
        VStack {
            Text(v).font(.subheadline.bold()).foregroundColor(Theme.text)
            Text(l).font(.caption2).foregroundColor(Theme.muted)
        }.frame(maxWidth: .infinity)
    }

    private func tierColor(_ t: Int) -> Color {
        switch t {
        case 0: return Color(hex: 0xCD7F32)
        case 1: return Color(hex: 0xC0C6D0)
        case 2: return Theme.gold
        default: return Color(hex: 0x6EE7FF)
        }
    }

    private func achievementRow(_ a: Achievement, _ st: Stats) -> some View {
        let unlocked = store.data.unlocked[a.id] != nil
        let p = min(1.0, a.progress(st) / max(a.target, 0.0001))
        return Card {
            HStack(spacing: 12) {
                Text(a.emoji).font(.system(size: 30)).opacity(unlocked ? 1 : 0.35)
                VStack(alignment: .leading, spacing: 3) {
                    Text(a.title).font(.subheadline.bold()).foregroundColor(unlocked ? tierColor(a.tier) : Theme.text)
                    Text(a.desc).font(.caption).foregroundColor(Theme.muted)
                    if !unlocked { ProgressBarView(value: p) }
                }
                Spacer()
                Text(unlocked ? "✓" : "+\(a.xp)").font(.caption.bold()).foregroundColor(unlocked ? Theme.green : Theme.muted)
            }
        }
    }
}
