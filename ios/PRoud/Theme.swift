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
    static var gamer: Bool { UserDefaults.standard.string(forKey: "proud.theme") == "gamer" }
    static var bg: Color { gamer ? Color(hex: 0x0A0618) : Color(hex: 0x07080B) }
    static var surf: Color { gamer ? Color(hex: 0x181033) : Color(hex: 0x12141A) }
    static var surf2: Color { gamer ? Color(hex: 0x2B1F5E) : Color(hex: 0x1D2028) }
    static var accent: Color { gamer ? Color(hex: 0x8B5CF6) : Color(hex: 0xFF7A3D) }
    static var onAccent: Color { gamer ? .white : Color(hex: 0x0B0B0D) }
    static var gold: Color { gamer ? Color(hex: 0xFFD34D) : Color(hex: 0xFFC857) }
    static var green: Color { gamer ? Color(hex: 0x39FFB0) : Color(hex: 0x3DDC97) }
    static var blue: Color { gamer ? Color(hex: 0x38BDF8) : Color(hex: 0x4DA3FF) }
    static var muted: Color { gamer ? Color(hex: 0xA79FCB) : Color(hex: 0x9AA0AC) }
    static var text: Color { gamer ? Color(hex: 0xF4F1FF) : Color(hex: 0xF2F3F5) }
    static var danger: Color { gamer ? Color(hex: 0xFF5C8A) : Color(hex: 0xFF5C5C) }
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
            .overlay(RoundedRectangle(cornerRadius: 18)
                .stroke(Theme.gamer ? Theme.accent.opacity(0.45) : Color.white.opacity(0.08), lineWidth: Theme.gamer ? 1.5 : 1))
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
            .foregroundColor(selected ? Theme.onAccent : Theme.text)
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
                Capsule().fill(Theme.gamer ? AnyShapeStyle(LinearGradient(colors: [Theme.accent, Theme.blue], startPoint: .leading, endPoint: .trailing)) : AnyShapeStyle(Theme.accent)).frame(width: g.size.width * CGFloat(min(1, max(0, value))))
            }
        }
        .frame(height: 8)
    }
}
