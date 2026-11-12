import SwiftUI

struct NumField: View {
    let value: Double
    let decimals: Bool
    let onChange: (Double) -> Void
    @State private var text = ""
    @FocusState private var focused: Bool

    var body: some View {
        TextField("0", text: $text)
            .keyboardType(decimals ? .decimalPad : .numberPad)
            .multilineTextAlignment(.center)
            .focused($focused)
            .frame(width: 64)
            .padding(.vertical, 8)
            .background(Theme.surf2)
            .clipShape(RoundedRectangle(cornerRadius: 10))
            .foregroundColor(Theme.text)
            .onAppear { text = fmtKg(value) }
            .onChange(of: text) { t in
                if let v = Double(t.replacingOccurrences(of: ",", with: ".")) { onChange(v) }
                else if t.isEmpty { onChange(0) }
            }
            .onChange(of: value) { v in
                if !focused { text = fmtKg(v) }
            }
    }
}

struct WorkoutView: View {
    @EnvironmentObject var store: Store
    @State private var showPicker = false
    @State private var swapId: String? = nil
    @State private var result: FinishResult? = nil
    @State private var confirmDiscard = false
    @State private var restLeft = 0
    @State private var now = Date()
    private let ticker = Timer.publish(every: 1, on: .main, in: .common).autoconnect()

    var body: some View {
        NavigationStack {
            Group {
                if let a = store.data.active {
                    content(a)
                } else {
                    Color.clear
                }
            }
            .background(Theme.bg.ignoresSafeArea())
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .principal) {
                    if let a = store.data.active {
                        VStack(spacing: 0) {
                            Text(a.name).font(.headline).foregroundColor(Theme.text)
                            Text(fmtDuration(Int(now.timeIntervalSince(a.startedAt)))).font(.caption).foregroundColor(Theme.muted)
                        }
                    }
                }
                ToolbarItem(placement: .cancellationAction) {
                    Button("Elvet") { confirmDiscard = true }.foregroundColor(Theme.danger)
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Kész") { result = store.finishActive() ?? nil; if result == nil { store.discardActive() } }
                        .bold().foregroundColor(Theme.accent)
                }
            }
            .onReceive(ticker) { _ in
                now = Date()
                if restLeft > 0 { restLeft -= 1 }
            }
            .alert("Edzés elvetése?", isPresented: $confirmDiscard) {
                Button("Elvet", role: .destructive) { store.discardActive() }
                Button("Mégse", role: .cancel) {}
            }
            .sheet(isPresented: $showPicker) {
                ExercisePickerView { e in store.addExercise(e.id) }
            }
            .sheet(item: Binding(get: { swapId.map { IdBox(id: $0) } }, set: { swapId = $0?.id })) { box in
                ExercisePickerView { e in store.replaceExercise(box.id, e.id) }
            }
            .fullScreenCover(item: Binding(get: { result.map { ResultBox(r: $0) } }, set: { if $0 == nil { result = nil } })) { box in
                SummaryView(result: box.r) { result = nil }
            }
        }
    }

    @ViewBuilder
    private func content(_ a: ActiveWorkout) -> some View {
        ScrollView {
            VStack(spacing: 12) {
                if restLeft > 0 {
                    Card {
                        HStack {
                            Text("⏱ Pihenő").foregroundColor(Theme.text)
                            Spacer()
                            Text(fmtDuration(restLeft)).font(.title3.bold()).foregroundColor(Theme.accent)
                            Button("Kihagy") { restLeft = 0 }.foregroundColor(Theme.muted)
                        }
                    }
                }
                ForEach(a.exercises) { ex in
                    exerciseCard(ex)
                }
                Button { showPicker = true } label: {
                    Text("+ Gyakorlat hozzáadása").font(.headline).frame(maxWidth: .infinity).padding()
                        .background(Theme.surf2).foregroundColor(Theme.text).clipShape(RoundedRectangle(cornerRadius: 16))
                }
            }
            .padding()
        }
    }

    private func exerciseCard(_ ex: ActiveExercise) -> some View {
        let e = ExerciseRepo.get(ex.exerciseId)
        return Card {
            VStack(alignment: .leading, spacing: 10) {
                HStack(spacing: 10) {
                    ExerciseThumb(exercise: e, size: 44)
                    NavigationLink(destination: Group { if let e = e { ExerciseDetailView(exercise: e) } }) {
                        Text(ExerciseRepo.nameOf(ex.exerciseId)).font(.headline).foregroundColor(Theme.text)
                    }
                    Spacer()
                    Menu {
                        Button("Bemelegítő szettek") { store.addWarmups(ex.exerciseId) }
                        Button("Csere másik gyakorlatra") { swapId = ex.exerciseId }
                        Button("Törlés", role: .destructive) { store.removeExercise(ex.exerciseId) }
                    } label: {
                        Image(systemName: "ellipsis.circle").font(.title3).foregroundColor(Theme.muted)
                    }
                }
                HStack {
                    Text("SZETT").frame(width: 44, alignment: .leading)
                    Spacer()
                    Text("KG").frame(width: 64)
                    Text("ISM").frame(width: 64)
                    Text("").frame(width: 40)
                }.font(.caption2).foregroundColor(Theme.muted)
                ForEach(Array(ex.sets.enumerated()), id: \.element.id) { idx, s in
                    HStack {
                        Text(s.warmup ? "M" : "\(ex.sets[..<idx].filter { !$0.warmup }.count + 1)")
                            .font(.subheadline.bold())
                            .foregroundColor(s.warmup ? Theme.gold : Theme.text)
                            .frame(width: 44, alignment: .leading)
                        Spacer()
                        NumField(value: s.weight, decimals: true) { v in
                            store.updateSet(ex.exerciseId, s.id) { $0.weight = v }
                        }
                        NumField(value: Double(s.reps), decimals: false) { v in
                            store.updateSet(ex.exerciseId, s.id) { $0.reps = Int(v) }
                        }
                        Button {
                            let wasDone = s.done
                            store.updateSet(ex.exerciseId, s.id) { $0.done.toggle() }
                            if !wasDone { restLeft = store.data.settings.restSeconds }
                        } label: {
                            Image(systemName: s.done ? "checkmark.circle.fill" : "circle")
                                .font(.title2).foregroundColor(s.done ? Theme.green : Theme.muted)
                        }.frame(width: 40)
                    }
                    .contextMenu {
                        Button("Szett törlése", role: .destructive) { store.removeSet(ex.exerciseId, s.id) }
                    }
                }
                Button { store.addSet(ex.exerciseId) } label: {
                    Text("+ Szett").font(.subheadline.weight(.semibold)).foregroundColor(Theme.accent)
                }
            }
        }
    }
}

struct IdBox: Identifiable { let id: String }
struct ResultBox: Identifiable {
    let r: FinishResult
    var id: String { r.session.id }
}

struct SummaryView: View {
    let result: FinishResult
    let onClose: () -> Void
    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                Text("🏆").font(.system(size: 64)).padding(.top, 30)
                Text("Edzés kész!").font(.largeTitle.bold()).foregroundColor(Theme.text)
                HStack(spacing: 10) {
                    stat("Volumen", fmtVolume(result.session.volume))
                    stat("Szett", "\(result.session.setCount)")
                    stat("Idő", "\(result.session.minutes) p")
                }
                Card {
                    VStack(alignment: .leading, spacing: 4) {
                        Text("+\(result.xpGained) XP").font(.title2.bold()).foregroundColor(Theme.accent)
                        if result.session.prCount > 0 {
                            Text("🎉 \(result.session.prCount) új egyéni rekord").foregroundColor(Theme.gold)
                        }
                        if result.levelAfter > result.levelBefore {
                            Text("⬆️ Szintlépés: Lv \(result.levelAfter) – \(levelTitle(result.levelAfter))").foregroundColor(Theme.green)
                        }
                    }
                }
                ForEach(result.newAchievements) { a in
                    Card {
                        HStack {
                            Text(a.emoji).font(.largeTitle)
                            VStack(alignment: .leading) {
                                Text("Új achievement!").font(.caption).foregroundColor(Theme.gold)
                                Text(a.title).font(.headline).foregroundColor(Theme.text)
                                Text(a.desc).font(.caption).foregroundColor(Theme.muted)
                            }
                            Spacer()
                            Text("+\(a.xp)").bold().foregroundColor(Theme.accent)
                        }
                    }
                }
                Button(action: onClose) {
                    Text("Szuper").font(.headline).frame(maxWidth: .infinity).padding()
                        .background(Theme.accent).foregroundColor(.black).clipShape(RoundedRectangle(cornerRadius: 16))
                }
            }
            .padding()
        }
        .background(Theme.bg.ignoresSafeArea())
    }

    private func stat(_ label: String, _ value: String) -> some View {
        VStack {
            Text(value).font(.headline).foregroundColor(Theme.text)
            Text(label).font(.caption).foregroundColor(Theme.muted)
        }
        .frame(maxWidth: .infinity).padding(.vertical, 12)
        .background(Theme.surf).clipShape(RoundedRectangle(cornerRadius: 14))
    }
}
