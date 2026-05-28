import SwiftUI
import shared

@main
struct iOSApp: App {
    init() {
        InitKoinKt.doInitKoin(baseUrl: "http://192.168.1.5:3333/api/v1/")
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
