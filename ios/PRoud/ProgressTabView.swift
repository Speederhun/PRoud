import SwiftUI
import Charts

struct SeriesPoint: Identifiable {
    let id = UUID()
    let date: Date
    let value: Double
}

struct ProgressTabView: View {
    @EnvironmentObject var store: Store
    @State private var exerciseId: String? = nil
    @State private var metric = 0
    @State private var weekOffset = 0
    @State private var bodyType = "Testsúly"
    @State private var bodyText = ""
    private let metrics = ["Max súly", "Becsült 1RM", "Volumen"]
    private let bodyTypes = ["Testsúly", "Mellkas", "Derék", "Bicepsz", "Comb"]

    private var doneExercises: [String] {
        var seen = Set<String>()
        var out: [String] = []
        for s in store.data.sessions.sorted(by: { $0.startedAt > $1.startedAt }) {
            for e in s.exercises where !seen.contains(e.exerciseId) { seen.insert(e.exerciseId); out.append(e.exerciseId) }
        }
        return out
    }

    private var series: [SeriesPoint] {
        guard let id = exerciseId ?? doneExercises.first else { return [] }
        var pts: [SeriesPoint] = []
        for s in store.data.sessions.sorted(by: { $0.startedAt < $1.startedAt }) {
            guard let e = s.exercises.first(where: { $0.exerciseId == id }), !e.sets.isEmpty else { continue }
            let v: Double
            switch metric {
            case 0: v = e.sets.map { $0.weight }.max() ?? 0
            case 1: v = e.sets.map { score($0.weight, $0.reps) }.max() ?? 0
            default: v = e.sets.reduce(0) { $0 + $1.weight * Double($1.reps) }
            }
            pts.append(SeriesPoint(date: s.startedAt, value: v))
        }
        return pts
    }

    private var groupSets: [(String, Int)] {
        let thisWeek = weekIndex(epochDay(Date())) - weekOffset
        var counts: [String: Int] = [:]
        for s in store.data.sessions where weekIndex(epochDay(s.startedAt)) == thisWeek {
            for e in s.exercises {
                guard let ex = ExerciseRepo.get(e.exerciseId) else { continue }
                let gs = Set(ex.primary.compactMap { Hu.groupOf($0) })
                for g in gs { counts[g, default: 0] += e.sets.count }
            }
        }
        return Hu.groups.map { ($0.name, counts[$0.name] ?? 0) }
    }

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 14) {
                    exerciseCard
                    muscleCard
                    bodyCard
                }.padding()
            }
            .background(Theme.bg.ignoresSafeArea())
            .navigationTitle("Haladás")
        }
    }

    private var exerciseCard: some View {
        Card {
            VStack(alignment: .leading, spacing: 10) {
                Text("📈 Gyakorlat fejlődése").font(.headline).foregroundColor(Theme.text)
                if doneExercises.isEmpty {
                    Text("Az első edzés után itt látod a súlyaid alakulását.").font(.footnote).foregroundColor(Theme.muted)
                } else {
                    Picker("Gyakorlat", selection: Binding(get: { exerciseId ?? doneExercises[0] }, set: { exerciseId = $0 })) {
                        ForEach(doneExercises, id: \.self) { Text(ExerciseRepo.nameOf($0)).tag($0) }
                    }.tint(Theme.accent)
                    Picker("Mutató", selection: $metric) {
                        ForEach(0..<3, id: \.self) { Text(metrics[$0]).tag($0) }
                    }.pickerStyle(.segmented)
                    if series.count < 2 {
                        Text("Legalább két edzés kell a grafikonhoz.").font(.footnote).foregroundColor(Theme.muted)
                    } else {
                        Chart(series) { p in
                            LineMark(x: .value("Nap", p.date), y: .value("Érték", p.value))
                                .foregroundStyle(Theme.accent)
                            PointMark(x: .value("Nap", p.date), y: .value("Érték", p.value))
                                .foregroundStyle(Theme.gold)
                        }
                        .frame(height: 180)
                        if let f = series.first?.value, let l = series.last?.value {
                            let diff = l - f
                            Text("\(fmtKg((f * 10).rounded() / 10)) → \(fmtKg((l * 10).rounded() / 10)) (\(diff >= 0 ? "+" : "")\(fmtKg((diff * 10).rounded() / 10)))")
                                .font(.caption).foregroundColor(diff >= 0 ? Theme.green : Theme.danger)
                        }
                    }
                }
            }
        }
    }

    private var muscleCard: some View {
        Card {
            VStack(alignment: .leading, spacing: 8) {
                Text("🧬 Heti izomcsoport-összesítő").font(.headline).foregroundColor(Theme.text)
                HStack {
                    Button { weekOffset = 0 } label: { ChipView(label: "Ez a hét", selected: weekOffset == 0) }
                    Button { weekOffset = 1 } label: { ChipView(label: "Múlt hét", selected: weekOffset == 1) }
                }
                ForEach(groupSets, id: \.0) { g, n in
                    HStack {
                        Text(g).font(.subheadline).foregroundColor(Theme.text).frame(width: 64, alignment: .leading)
                        ProgressBarView(value: Double(n) / 10.0)
                        Text(n == 0 ? "lemaradt" : "\(n) szett").font(.caption)
                            .foregroundColor(n == 0 ? Theme.danger : Theme.muted).frame(width: 66, alignment: .trailing)
                    }
                }
                Text("Cél: heti 10 szett izomcsoportonként.").font(.caption2).foregroundColor(Theme.muted)
            }
        }
    }

    private var bodyCard: some View {
        let unit = bodyType == "Testsúly" ? "kg" : "cm"
        let entries = store.data.body.filter { $0.type == bodyType }.sorted { $0.epochDay < $1.epochDay }
        return Card {
            VStack(alignment: .leading, spacing: 10) {
                Text("📏 Test adatok").font(.headline).foregroundColor(Theme.text)
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack {
                        ForEach(bodyTypes, id: \.self) { t in
                            Button { bodyType = t } label: { ChipView(label: t, selected: bodyType == t) }
                        }
                    }
                }
                HStack {
                    TextField(unit, text: $bodyText).keyboardType(.decimalPad)
                        .padding(10).background(Theme.surf2).clipShape(RoundedRectangle(cornerRadius: 10))
                        .foregroundColor(Theme.text).frame(width: 100)
                    Button("Mentés mára") {
                        if let v = Double(bodyText.replacingOccurrences(of: ",", with: ".")) {
                            store.addBody(bodyType, v)
                            bodyText = ""
                        }
                    }.buttonStyle(.borderedProminent).tint(Theme.accent)
                }
                if entries.count >= 2 {
                    Chart(entries, id: \.epochDay) { e in
                        LineMark(x: .value("Nap", Date(timeIntervalSince1970: Double(e.epochDay) * 86400)), y: .value("Érték", e.value))
                            .foregroundStyle(Theme.blue)
                    }.frame(height: 140)
                }
                ForEach(entries.reversed().prefix(8), id: \.self) { e in
                    HStack {
                        Text(Date(timeIntervalSince1970: Double(e.epochDay) * 86400).formatted(date: .abbreviated, time: .omitted))
                            .font(.caption).foregroundColor(Theme.muted)
                        Spacer()
                        Text("\(fmtKg(e.value)) \(unit)").font(.subheadline.weight(.semibold)).foregroundColor(Theme.text)
                        Button { store.removeBody(e) } label: { Text("✕").foregroundColor(Theme.muted) }
                    }
                }
            }
        }
    }
}
