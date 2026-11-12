import SwiftUI

extension Color {
    init(hex: UInt32) {
        self.init(.sRGB,
                  red: Double((hex >> 16) & 0xFF) / 255,
                  green: Double((hex >> 8) & 0xFF) / 255,
                  blue: Double(hex & 0xFF) / 255,
                  opacity: 1)
    }
}

enum Theme {
    static let bg = Color(hex: 0x0E0F13)
    static let surf = Color(hex: 0x181A21)
    static let surf2 = Color(hex: 0x22252E)
    static let accent = Color(hex: 0xFF7A1A)
    static let gold = Color(hex: 0xFFC53D)
    static let green = Color(hex: 0x3DDC84)
    static let muted = Color(hex: 0x8A8F9C)
    static let text = Color(hex: 0xF2F3F5)
    static let danger = Color(hex: 0xFF5A5F)
}

struct Card<Content: View>: View {
    let content: Content
    init(@ViewBuilder _ content: () -> Content) { self.content = content() }
    var body: some View {
        content
            .padding(14)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(Theme.surf)
            .clipShape(RoundedRectangle(cornerRadius: 18))
    }
}

struct ChipView: View {
    let label: String
    let selected: Bool
    var body: some View {
        Text(label)
            .font(.subheadline.weight(.semibold))
            .padding(.horizontal, 14).padding(.vertical, 8)
            .background(selected ? Theme.accent : Theme.surf2)
            .foregroundColor(selected ? .black : Theme.text)
            .clipShape(Capsule())
    }
}

struct ExerciseThumb: View {
    let exercise: Exercise?
    var size: CGFloat = 52
    var body: some View {
        Group {
            if let url = exercise?.imageUrls.first {
                AsyncImage(url: url) { phase in
                    if let img = phase.image {
                        img.resizable().scaledToFill()
                    } else {
                        Theme.surf2
                    }
                }
            } else {
                Theme.surf2
            }
        }
        .frame(width: size, height: size)
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}

struct ExerciseDemo: View {
    let urls: [URL]
    @State private var frame = 0
    private let timer = Timer.publish(every: 0.9, on: .main, in: .common).autoconnect()
    var body: some View {
        Group {
            if urls.isEmpty {
                Theme.surf2
            } else {
                AsyncImage(url: urls[frame % urls.count]) { phase in
                    if let img = phase.image {
                        img.resizable().scaledToFit()
                    } else {
                        ProgressView()
                    }
                }
            }
        }
        .frame(maxWidth: .infinity)
        .frame(height: 220)
        .background(Theme.surf2)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .onReceive(timer) { _ in frame += 1 }
    }
}

struct ProgressBarView: View {
    let value: Double
    var body: some View {
        GeometryReader { g in
            ZStack(alignment: .leading) {
                Capsule().fill(Theme.surf2)
                Capsule().fill(Theme.accent).frame(width: g.size.width * CGFloat(min(1, max(0, value))))
            }
        }
        .frame(height: 8)
    }
}
