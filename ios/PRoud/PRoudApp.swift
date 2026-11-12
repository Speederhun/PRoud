import SwiftUI

@main
struct PRoudApp: App {
    @StateObject private var store = Store()

    init() {
        ExerciseRepo.load()
        let cache = URLCache(memoryCapacity: 50_000_000, diskCapacity: 500_000_000)
        URLCache.shared = cache
    }

    var body: some Scene {
        WindowGroup {
            RootView()
                .environmentObject(store)
                .preferredColorScheme(.dark)
                .tint(Theme.accent)
        }
    }
}

struct RootView: View {
    @EnvironmentObject var store: Store
    var body: some View {
        TabView {
            HomeView().tabItem { Label("Edzés", systemImage: "dumbbell.fill") }
            CalendarView().tabItem { Label("Naptár", systemImage: "calendar") }
            ProgressTabView().tabItem { Label("Haladás", systemImage: "chart.line.uptrend.xyaxis") }
            ProfileView().tabItem { Label("Profil", systemImage: "trophy.fill") }
            LibraryView().tabItem { Label("Gyakorlat", systemImage: "figure.strengthtraining.traditional") }
        }
        .id(store.data.settings.theme)
        .tint(Theme.accent)
        .fullScreenCover(isPresented: Binding(get: { store.data.active != nil }, set: { _ in })) {
            WorkoutView().environmentObject(store)
        }
    }
}
