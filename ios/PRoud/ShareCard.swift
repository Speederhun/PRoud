import SwiftUI

struct ShareCardView: View {
    let rows: [RecordRow]
    let level: Int
    let title: String
    let xp: Int

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("PRoud").font(.system(size: 34, weight: .heavy)).foregroundColor(Theme.accent)
                Spacer()
                Text("Lv \(level) · \(title)").font(.headline).foregroundColor(Theme.gold)
            }
            Text("🏆 Rekordfal").font(.title3.bold()).foregroundColor(Theme.text)
            ForEach(rows.prefix(8)) { r in
                HStack {
                    Text(ExerciseRepo.nameOf(r.exerciseId)).font(.subheadline).foregroundColor(Theme.text).lineLimit(1)
                    Spacer()
                    Text("\(fmtKg(r.weight)) kg × \(r.reps)").font(.subheadline.bold()).foregroundColor(Theme.accent)
                }
            }
            Text("\(xp) XP").font(.caption).foregroundColor(Theme.muted)
        }
        .padding(24)
        .frame(width: 360)
        .background(LinearGradient(colors: [Theme.surf, Theme.bg], startPoint: .top, endPoint: .bottom))
    }
}
