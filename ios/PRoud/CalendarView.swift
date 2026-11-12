import SwiftUI

struct CalendarView: View {
    @EnvironmentObject var store: Store
    @State private var month = Date()
    @State private var selected: Date? = nil
    private var cal: Calendar {
        var c = Calendar(identifier: .gregorian)
        c.firstWeekday = 2
        return c
    }

    private func sessions(on d: Date) -> [Session] {
        store.data.sessions.filter { cal.isDate($0.startedAt, inSameDayAs: d) }
    }

    private var days: [Date?] {
        guard let start = cal.date(from: cal.dateComponents([.year, .month], from: month)),
              let range = cal.range(of: .day, in: .month, for: start) else { return [] }
        let weekday = cal.component(.weekday, from: start)
        let lead = (weekday - cal.firstWeekday + 7) % 7
        var out: [Date?] = Array(repeating: nil, count: lead)
        for d in range { out.append(cal.date(byAdding: .day, value: d - 1, to: start)) }
        return out
    }

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 12) {
                    HStack {
                        Button { month = cal.date(byAdding: .month, value: -1, to: month) ?? month } label: { Image(systemName: "chevron.left") }
                        Spacer()
                        Text(month.formatted(.dateTime.year().month(.wide))).font(.headline).foregroundColor(Theme.text)
                        Spacer()
                        Button { month = cal.date(byAdding: .month, value: 1, to: month) ?? month } label: { Image(systemName: "chevron.right") }
                    }.foregroundColor(Theme.accent)
                    let cols = Array(repeating: GridItem(.flexible(), spacing: 6), count: 7)
                    LazyVGrid(columns: cols, spacing: 6) {
                        ForEach(["H", "K", "Sz", "Cs", "P", "Sz", "V"], id: \.self) { _ in EmptyView() }
                        ForEach(Array(days.enumerated()), id: \.offset) { _, d in
                            if let d = d {
                                let n = sessions(on: d).count
                                let isSel = selected.map { cal.isDate($0, inSameDayAs: d) } ?? false
                                Button { selected = d } label: {
                                    Text("\(cal.component(.day, from: d))")
                                        .font(.subheadline.weight(n > 0 ? .bold : .regular))
                                        .frame(maxWidth: .infinity).frame(height: 40)
                                        .background(n > 0 ? Theme.accent : (isSel ? Theme.surf2 : Theme.surf))
                                        .foregroundColor(n > 0 ? .black : Theme.text)
                                        .clipShape(RoundedRectangle(cornerRadius: 10))
                                        .overlay(RoundedRectangle(cornerRadius: 10).stroke(cal.isDateInToday(d) ? Theme.gold : .clear, lineWidth: 2))
                                }
                            } else {
                                Color.clear.frame(height: 40)
                            }
                        }
                    }
                    if let sel = selected {
                        let list = sessions(on: sel)
                        if list.isEmpty {
                            Text("Nincs edzés ezen a napon").font(.footnote).foregroundColor(Theme.muted).padding(.top, 8)
                        }
                        ForEach(list) { s in
                            Card {
                                VStack(alignment: .leading, spacing: 4) {
                                    Text(s.name).font(.headline).foregroundColor(Theme.text)
                                    Text("\(fmtVolume(s.volume)) · \(s.setCount) szett · \(s.minutes) perc")
                                        .font(.caption).foregroundColor(Theme.muted)
                                    ForEach(s.exercises, id: \.exerciseId) { e in
                                        Text("• \(ExerciseRepo.nameOf(e.exerciseId)) – \(e.sets.map { "\(fmtKg($0.weight))×\($0.reps)" }.joined(separator: ", "))")
                                            .font(.caption).foregroundColor(Theme.text)
                                    }
                                }
                            }
                        }
                    }
                }.padding()
            }
            .background(Theme.bg.ignoresSafeArea())
            .navigationTitle("Naptár")
        }
    }
}
