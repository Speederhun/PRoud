import SwiftUI

struct PlateView: View {
    @State var weightText: String
    @State private var bar = 20.0
    @Environment(\.dismiss) private var dismiss

    private let plates = [25.0, 20.0, 15.0, 10.0, 5.0, 2.5, 1.25]

    private var target: Double { Double(weightText.replacingOccurrences(of: ",", with: ".")) ?? 0 }

    private func plateColor(_ p: Double) -> Color {
        switch p {
        case 25: return .red
        case 20: return .blue
        case 15: return .yellow
        case 10: return .green
        case 5: return .white
        default: return .gray
        }
    }

    var body: some View {
        let result = compute()
        return NavigationStack {
            VStack(alignment: .leading, spacing: 14) {
                Text("Cél súly (kg)").font(.caption).foregroundColor(Theme.muted)
                TextField("kg", text: $weightText).keyboardType(.decimalPad)
                    .padding(10).background(Theme.surf2).clipShape(RoundedRectangle(cornerRadius: 10))
                    .foregroundColor(Theme.text).frame(width: 140)
                Text("Rúd súlya").font(.caption).foregroundColor(Theme.muted)
                HStack {
                    ForEach([20.0, 15.0, 10.0], id: \.self) { b in
                        Button { bar = b } label: { ChipView(label: "\(fmtKg(b)) kg", selected: bar == b) }
                    }
                }
                if target <= bar {
                    Text("Ennyi a rúd súlya, nem kell tárcsa.").foregroundColor(Theme.muted)
                } else {
                    Text("Oldalanként:").font(.caption).foregroundColor(Theme.muted)
                    HStack(spacing: 4) {
                        ForEach(Array(result.used.enumerated()), id: \.offset) { _, u in
                            ForEach(0..<u.1, id: \.self) { _ in
                                Text(fmtKg(u.0)).font(.caption2.bold()).foregroundColor(.black)
                                    .frame(width: 30, height: 20 + CGFloat(u.0) * 2.4)
                                    .background(plateColor(u.0)).clipShape(RoundedRectangle(cornerRadius: 4))
                            }
                        }
                    }
                    ForEach(Array(result.used.enumerated()), id: \.offset) { _, u in
                        Text("\(u.1) × \(fmtKg(u.0)) kg").foregroundColor(Theme.text)
                    }
                    if result.leftover > 0 {
                        Text("Nem kirakható pontosan: \(fmtKg((result.leftover * 100).rounded() / 100)) kg hiányzik.")
                            .font(.footnote).foregroundColor(Theme.danger)
                    }
                }
                Spacer()
            }
            .padding()
            .background(Theme.bg.ignoresSafeArea())
            .navigationTitle("Tárcsakalkulátor")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar { ToolbarItem(placement: .confirmationAction) { Button("Kész") { dismiss() } } }
        }
        .presentationDetents([.medium, .large])
    }

    private func compute() -> (used: [(Double, Int)], leftover: Double) {
        var rest = (target - bar) / 2.0
        var used: [(Double, Int)] = []
        if rest > 0 {
            for p in plates {
                let n = Int(floor(rest / p + 1e-9))
                if n > 0 { used.append((p, n)); rest -= Double(n) * p }
            }
        }
        return (used, rest > 0.01 ? rest * 2 : 0)
    }
}
