import SwiftUI

struct ExerciseDetailView: View {
    let exercise: Exercise
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 14) {
                ExerciseDemo(urls: exercise.imageUrls)
                Text(exercise.huName).font(.title2.bold()).foregroundColor(Theme.text)
                HStack(spacing: 8) {
                    ForEach(exercise.primary, id: \.self) { m in
                        Text(Hu.muscle(m)).font(.caption.weight(.bold))
                            .padding(.horizontal, 10).padding(.vertical, 5)
                            .background(Theme.accent).foregroundColor(.black).clipShape(Capsule())
                    }
                    Text(Hu.equipment(exercise.equipment)).font(.caption)
                        .padding(.horizontal, 10).padding(.vertical, 5)
                        .background(Theme.surf2).foregroundColor(Theme.text).clipShape(Capsule())
                }
                let tips = Hu.tips(exercise.id)
                if !tips.isEmpty {
                    Card {
                        VStack(alignment: .leading, spacing: 6) {
                            Text("Helyes kivitelezés").font(.headline).foregroundColor(Theme.gold)
                            ForEach(tips, id: \.self) { t in
                                Text("• " + t).font(.subheadline).foregroundColor(Theme.text)
                            }
                        }
                    }
                }
                if !exercise.instructions.isEmpty {
                    Card {
                        VStack(alignment: .leading, spacing: 6) {
                            Text("Leírás (angolul)").font(.headline).foregroundColor(Theme.text)
                            ForEach(Array(exercise.instructions.enumerated()), id: \.offset) { i, t in
                                Text("\(i + 1). " + t).font(.footnote).foregroundColor(Theme.muted)
                            }
                        }
                    }
                }
            }
            .padding()
        }
        .background(Theme.bg.ignoresSafeArea())
        .navigationBarTitleDisplayMode(.inline)
    }
}

struct ExerciseRow: View {
    let exercise: Exercise
    var body: some View {
        HStack(spacing: 12) {
            ExerciseThumb(exercise: exercise)
            VStack(alignment: .leading, spacing: 2) {
                Text(exercise.huName).font(.subheadline.weight(.semibold)).foregroundColor(Theme.text)
                Text(exercise.primary.map { Hu.muscle($0) }.joined(separator: ", ") + " · " + Hu.equipment(exercise.equipment))
                    .font(.caption).foregroundColor(Theme.muted)
            }
            Spacer()
        }
    }
}

struct ExerciseBrowser: View {
    var onPick: ((Exercise) -> Void)? = nil
    @State private var query = ""
    @State private var group: String? = nil
    @State private var equip: String? = nil

    private var filtered: [Exercise] {
        let q = query.lowercased()
        return ExerciseRepo.all.filter { e in
            if let g = group, !e.primary.contains(where: { Hu.groupOf($0) == g }) { return false }
            if let eq = equip, e.equipment != eq { return false }
            if q.isEmpty { return true }
            return e.huName.lowercased().contains(q) || e.name.lowercased().contains(q)
        }
    }

    var body: some View {
        VStack(spacing: 8) {
            TextField("Keresés…", text: $query)
                .padding(10).background(Theme.surf2).clipShape(RoundedRectangle(cornerRadius: 12))
                .foregroundColor(Theme.text).padding(.horizontal)
            ScrollView(.horizontal, showsIndicators: false) {
                HStack {
                    Button { group = nil } label: { ChipView(label: "Mind", selected: group == nil) }
                    ForEach(Hu.groups, id: \.name) { g in
                        Button { group = (group == g.name ? nil : g.name) } label: { ChipView(label: g.name, selected: group == g.name) }
                    }
                }.padding(.horizontal)
            }
            ScrollView(.horizontal, showsIndicators: false) {
                HStack {
                    Button { equip = nil } label: { ChipView(label: "Minden eszköz", selected: equip == nil) }
                    ForEach(Hu.mainEquipment, id: \.self) { e in
                        Button { equip = (equip == e ? nil : e) } label: { ChipView(label: Hu.equipment(e), selected: equip == e) }
                    }
                }.padding(.horizontal)
            }
            List(filtered) { ex in
                if let pick = onPick {
                    Button { pick(ex) } label: { ExerciseRow(exercise: ex) }
                        .listRowBackground(Theme.bg)
                } else {
                    NavigationLink(destination: ExerciseDetailView(exercise: ex)) { ExerciseRow(exercise: ex) }
                        .listRowBackground(Theme.bg)
                }
            }
            .listStyle(.plain)
        }
        .background(Theme.bg.ignoresSafeArea())
    }
}

struct LibraryView: View {
    var body: some View {
        NavigationStack {
            ExerciseBrowser()
                .navigationTitle("Gyakorlatok")
        }
    }
}

struct ExercisePickerView: View {
    let onPick: (Exercise) -> Void
    @Environment(\.dismiss) private var dismiss
    var body: some View {
        NavigationStack {
            ExerciseBrowser(onPick: { e in onPick(e); dismiss() })
                .navigationTitle("Gyakorlat választás")
                .navigationBarTitleDisplayMode(.inline)
                .toolbar { ToolbarItem(placement: .cancellationAction) { Button("Mégse") { dismiss() } } }
        }
    }
}
