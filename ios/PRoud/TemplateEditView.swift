import SwiftUI

struct TemplateEditView: View {
    @EnvironmentObject var store: Store
    @Environment(\.dismiss) private var dismiss
    @State var template: Template
    @State private var showPicker = false

    var body: some View {
        NavigationStack {
            Form {
                Section("Alapadatok") {
                    TextField("Név", text: $template.name)
                    TextField("Emoji", text: $template.emoji)
                    Picker("Csoport", selection: $template.group) {
                        Text("Nincs").tag("")
                        ForEach(Hu.templateGroups, id: \.self) { Text($0).tag($0) }
                    }
                }
                Section("Gyakorlatok") {
                    ForEach(template.items.indices, id: \.self) { i in
                        VStack(alignment: .leading) {
                            Text(ExerciseRepo.nameOf(template.items[i].exerciseId)).font(.subheadline.bold())
                            Stepper("Szett: \(template.items[i].sets)", value: $template.items[i].sets, in: 1...10)
                            Stepper("Ismétlés: \(template.items[i].reps)", value: $template.items[i].reps, in: 1...50)
                        }
                    }
                    .onDelete { template.items.remove(atOffsets: $0) }
                    .onMove { template.items.move(fromOffsets: $0, toOffset: $1) }
                    Button("+ Gyakorlat hozzáadása") { showPicker = true }
                }
            }
            .scrollContentBackground(.hidden)
            .background(Theme.bg.ignoresSafeArea())
            .navigationTitle("Sablon")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) { Button("Mégse") { dismiss() } }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Mentés") {
                        if !template.name.trimmingCharacters(in: .whitespaces).isEmpty && !template.items.isEmpty {
                            store.saveTemplate(template)
                            dismiss()
                        }
                    }.bold()
                }
                ToolbarItem(placement: .bottomBar) { EditButton() }
            }
            .sheet(isPresented: $showPicker) {
                ExercisePickerView { e in
                    if !template.items.contains(where: { $0.exerciseId == e.id }) {
                        template.items.append(TemplateItem(exerciseId: e.id, sets: 3, reps: 10))
                    }
                }
            }
        }
    }
}
