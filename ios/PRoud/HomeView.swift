import SwiftUI

struct HomeView: View {
    @EnvironmentObject var store: Store
    @State private var group = "Mind"
    @State private var detail: Template? = nil
    @State private var editing: Template? = nil

    private var groups: [String] { ["Mind"] + Hu.templateGroups }

    private var templates: [Template] {
        store.data.templates.filter { group == "Mind" || $0.group == group }
    }

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 14) {
                    levelCard
                    todayPlan
                    challengesCard
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack {
                            ForEach(groups, id: \.self) { g in
                                Button { group = g } label: {
                                    ChipView(label: (g == "Mind" ? "" : Hu.groupEmoji(g) + " ") + g, selected: group == g)
                                }
                            }
                        }
                    }
                    ForEach(templates) { t in
                        Button { detail = t } label: { templateCard(t) }
                            .contextMenu {
                                Button("Szerkesztés") { editing = t }
                                Button("Törlés", role: .destructive) { store.deleteTemplate(t.id) }
                            }
                    }
                    Button {
                        editing = Template(id: "c_" + UUID().uuidString, name: "", emoji: "🏋️", group: group == "Mind" ? "" : group, items: [])
                    } label: {
                        Text("+ Saját sablon").font(.headline).frame(maxWidth: .infinity).padding()
                            .background(Theme.surf2).foregroundColor(Theme.text).clipShape(RoundedRectangle(cornerRadius: 16))
                    }
                    Button { store.startEmpty() } label: {
                        Text("+ Szabad edzés").font(.headline).frame(maxWidth: .infinity).padding()
                            .background(Theme.surf2).foregroundColor(Theme.text).clipShape(RoundedRectangle(cornerRadius: 16))
                    }
                    recent
                }
                .padding()
            }
            .background(Theme.bg.ignoresSafeArea())
            .navigationTitle("PRoud")
            .sheet(item: $detail) { t in TemplatePreview(template: t) }
            .sheet(item: $editing) { t in TemplateEditView(template: t) }
        }
    }

    private var levelCard: some View {
        let st = computeStats(store.data)
        let li = levelInfo(st.xp)
        return Card {
            VStack(alignment: .leading, spacing: 8) {
                HStack {
                    Text("Lv \(li.level)").font(.title.bold()).foregroundColor(Theme.accent)
                    Text(li.title).font(.headline).foregroundColor(Theme.text)
                    Spacer()
                    Text("🔥 \(st.streak) hét").font(.subheadline.weight(.semibold)).foregroundColor(Theme.gold)
                }
                ProgressBarView(value: Double(li.xpInto) / Double(max(1, li.xpNeeded)))
                Text("\(li.xpInto) / \(li.xpNeeded) XP · e heti edzés: \(st.workoutsThisWeek)/\(store.data.settings.weeklyGoal)")
                    .font(.caption).foregroundColor(Theme.muted)
            }
        }
    }

    @ViewBuilder private var todayPlan: some View {
        let today = epochDay(Date())
        let planned = store.data.plans.filter { $0.epochDay == today }.compactMap { p in store.data.templates.first(where: { $0.id == p.templateId }) }
        if !planned.isEmpty {
            Card {
                VStack(alignment: .leading, spacing: 8) {
                    Text("📌 Mára tervezve").font(.headline).foregroundColor(Theme.gold)
                    ForEach(planned) { t in
                        Button { detail = t } label: {
                            Text("\(t.emoji) \(t.name)").foregroundColor(Theme.text)
                        }
                    }
                }
            }
        }
    }

    private var challengesCard: some View {
        let week = weekIndex(epochDay(Date()))
        let list = challengesFor(week, store.data)
        return Card {
            VStack(alignment: .leading, spacing: 10) {
                Text("🎯 Heti kihívások").font(.headline).foregroundColor(Theme.text)
                ForEach(list) { c in
                    VStack(alignment: .leading, spacing: 4) {
                        HStack {
                            Text("\(c.emoji) \(c.title)").font(.subheadline).foregroundColor(c.done ? Theme.green : Theme.text)
                            Spacer()
                            Text(c.done ? "✓" : "+\(c.xp) XP").font(.caption.bold()).foregroundColor(c.done ? Theme.green : Theme.gold)
                        }
                        ProgressBarView(value: c.current / max(c.target, 1))
                    }
                }
            }
        }
    }

    private func templateCard(_ t: Template) -> some View {
        Card {
            HStack(spacing: 12) {
                Text(t.emoji).font(.system(size: 32))
                VStack(alignment: .leading, spacing: 3) {
                    Text(t.name).font(.headline).foregroundColor(Theme.text)
                    let sets = t.items.reduce(0) { $0 + $1.sets }
                    Text("\(t.items.count) gyakorlat · \(sets) szett · ~\(sets * 3 + t.items.count) perc")
                        .font(.caption).foregroundColor(Theme.muted)
                }
                Spacer()
                Image(systemName: "chevron.right").foregroundColor(Theme.muted)
            }
        }
    }

    private var recent: some View {
        VStack(alignment: .leading, spacing: 8) {
            if !store.data.sessions.isEmpty {
                Text("Legutóbbi edzések").font(.headline).foregroundColor(Theme.text).padding(.top, 6)
            }
            ForEach(store.data.sessions.sorted { $0.startedAt > $1.startedAt }.prefix(5)) { s in
                Card {
                    HStack {
                        VStack(alignment: .leading) {
                            Text(s.name).font(.subheadline.weight(.semibold)).foregroundColor(Theme.text)
                            Text(s.startedAt.formatted(date: .abbreviated, time: .shortened))
                                .font(.caption).foregroundColor(Theme.muted)
                        }
                        Spacer()
                        VStack(alignment: .trailing) {
                            Text(fmtVolume(s.volume)).font(.subheadline.weight(.bold)).foregroundColor(Theme.accent)
                            Text("\(s.setCount) szett · \(s.minutes) perc").font(.caption).foregroundColor(Theme.muted)
                        }
                    }
                }
            }
        }
    }
}

struct TemplatePreview: View {
    let template: Template
    @EnvironmentObject var store: Store
    @Environment(\.dismiss) private var dismiss
    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 10) {
                    ForEach(template.items, id: \.exerciseId) { it in
                        Card {
                            HStack(spacing: 12) {
                                ExerciseThumb(exercise: ExerciseRepo.get(it.exerciseId))
                                VStack(alignment: .leading) {
                                    Text(ExerciseRepo.nameOf(it.exerciseId)).font(.subheadline.weight(.semibold)).foregroundColor(Theme.text)
                                    Text("\(it.sets) × \(it.reps)").font(.caption).foregroundColor(Theme.muted)
                                }
                                Spacer()
                            }
                        }
                    }
                    Button {
                        store.start(template)
                        dismiss()
                    } label: {
                        Text("Edzés indítása").font(.headline).frame(maxWidth: .infinity).padding()
                            .background(Theme.accent).foregroundColor(Theme.onAccent).clipShape(RoundedRectangle(cornerRadius: 16))
                    }.padding(.top, 8)
                }.padding()
            }
            .background(Theme.bg.ignoresSafeArea())
            .navigationTitle(template.name)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar { ToolbarItem(placement: .cancellationAction) { Button("Bezár") { dismiss() } } }
        }
    }
}
