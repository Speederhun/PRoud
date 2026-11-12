import Foundation

// Automatikusan generálva az Android-forrásból (gen_swift.py).
enum Hu {
    static let imgBase = "https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/exercises/"
    static let muscles: [String: String] = [
        "abdominals": "Hasizom",
        "abductors": "Csípőtávolító",
        "adductors": "Combközelítő",
        "biceps": "Bicepsz",
        "calves": "Vádli",
        "chest": "Mell",
        "forearms": "Alkar",
        "glutes": "Farizom",
        "hamstrings": "Hátsó comb",
        "lats": "Széles hátizom",
        "lower back": "Ágyék",
        "middle back": "Közép hát",
        "neck": "Nyak",
        "quadriceps": "Combfeszítő",
        "shoulders": "Váll",
        "traps": "Csuklyás izom",
        "triceps": "Tricepsz",
    ]
    static let equipmentNames: [String: String] = [
        "barbell": "Rúd",
        "dumbbell": "Kézisúlyzó",
        "cable": "Csiga",
        "machine": "Gép",
        "kettlebells": "Kettlebell",
        "body only": "Saját testsúly",
        "e-z curl bar": "Hajlított rúd",
        "bands": "Gumiszalag",
        "medicine ball": "Medicinlabda",
        "exercise ball": "Fitnesz labda",
        "foam roll": "Henger",
        "other": "Egyéb",
    ]
    static let levelNames: [String: String] = [
        "beginner": "Kezdő",
        "intermediate": "Haladó",
        "expert": "Profi",
    ]
    static func muscle(_ k: String) -> String { muscles[k] ?? k }
    static func equipment(_ k: String) -> String { equipmentNames[k] ?? k }
    static func level(_ k: String) -> String { levelNames[k] ?? k }
    static let groups: [(name: String, muscles: [String])] = [
        ("Mell", ["chest"]),
        ("Hát", ["lats", "middle back", "lower back", "traps"]),
        ("Láb", ["quadriceps", "hamstrings", "glutes", "calves", "adductors", "abductors"]),
        ("Váll", ["shoulders"]),
        ("Kar", ["biceps", "triceps", "forearms"]),
        ("Core", ["abdominals"]),
    ]
    static func groupOf(_ muscle: String) -> String? { groups.first(where: { $0.muscles.contains(muscle) })?.name }
    static let mainEquipment: [String] = ["barbell", "dumbbell", "cable", "machine", "body only", "kettlebells"]
    static let templateGroups: [String] = ["Push", "Pull", "Láb", "Mell", "Hát", "Váll", "Kar", "Core", "Teljes test", "Testsúly", "Női"]
    static func groupEmoji(_ g: String) -> String {
        switch g {
        case "Push": return "🚀"
        case "Pull": return "🧲"
        case "Láb": return "🦵"
        case "Mell": return "💪"
        case "Hát": return "🦍"
        case "Váll": return "🙌"
        case "Kar": return "💥"
        case "Core": return "🎯"
        case "Teljes test": return "🔥"
        case "Testsúly": return "🤸"
        case "Női": return "🌸"
        default: return "🏋️"
        }
    }
    static let info: [String: (name: String, tips: [String])] = [
        "Barbell_Full_Squat": ("Guggolás rúddal", [
            "Lábak vállszélesen vagy kicsit szélesebben, lábujjak enyhén kifelé.",
            "Emelés előtt vegyél mély levegőt a hasadba, és feszítsd meg a törzsed – ezt tartsd végig a mozgás alatt.",
            "A térd a lábujjak irányába nyíljon, ne essen befelé.",
            "A hát végig egyenes, a mellkas felfelé néz; a csípő és a térd egyszerre hajlik.",
            "Menj le legalább addig, amíg a comb vízszintes lesz, a sarok végig a talajon marad.",
            "Felfelé a csípő és a mellkas együtt emelkedjen – ne a csípő induljon előre.",
        ]),
        "Barbell_Squat": ("Guggolás (rúd a vállon)", [
            "Rúd a hátsó vállon, a lapockákat húzd össze, hogy a rúd stabilan üljön.",
            "Vegyél levegőt, feszítsd meg a törzsed, és csak utána kezdj lemenni.",
            "Térd a lábujjak irányába, a sarok a talajon.",
            "Mély, kontrollált lemenet, a hát egyenes marad.",
            "Felfelé a talajt told el magadtól, a mellkas ne bukjon előre.",
        ]),
        "Barbell_Bench_Press_-_Medium_Grip": ("Fekvenyomás rúddal", [
            "Lapockákat húzd hátra és le, és tartsd így végig; a farpofa végig a padon.",
            "Lábak stabilan a talajon, a hát enyhén ívelt.",
            "Fogás kicsivel vállszélesség fölött; a legalsó pontnál az alkarod függőleges.",
            "A rúd a mellkas alsó részére érkezzen, a könyök kb. 45–75°-ban a törzstől (ne oldalra, 90°-ban).",
            "Kontrolláltan érintsd a mellkast, ne pattintsd; csukló egyenes, a rúd az alkar felett.",
        ]),
        "Dumbbell_Bench_Press": ("Fekvenyomás kézisúlyzóval", [
            "Lapockák hátra és le, mellkas kidüllesztve.",
            "Könyök kb. 45–60°-ban a törzstől, ne oldalra.",
            "A súlyzók a mellkas magasságáig ereszkedjenek, kontrolláltan.",
            "Nyomás közben a súlyzók a mellkas felett találkozzanak, a váll ne emelkedjen fel.",
        ]),
        "Incline_Dumbbell_Press": ("Ferde padon nyomás kézisúlyzóval", [
            "A pad dőlése kb. 30°; ennél meredekebb a vállat terheli.",
            "Lapockák hátra és le, a fenék a padon.",
            "Könyök kb. 45–60°-ban a törzstől.",
            "Lassú lemenet, a súlyzók a mellkas felső részénél érjenek le.",
        ]),
        "Barbell_Deadlift": ("Felhúzás", [
            "A rúd a lábfej közepe felett legyen, a lapockák kb. a rúd felett.",
            "Fogd meg a rudat, húzd le-hátra a lapockákat, és feszítsd ki a lazaságot a rúdból, mielőtt emelsz.",
            "A hát végig egyenes (semleges), a mellkas büszkén előre.",
            "A rúd végig a testhez közel mozogjon, szinte súrolja a combot.",
            "Felül a csípő és a térd egyszerre nyúljon ki, ne dőlj hátra.",
            "Lefelé kontrolláltan: előbb a csípő hátra, aztán a térd hajlik.",
        ]),
        "Romanian_Deadlift": ("Román felhúzás", [
            "A térd enyhén hajlított és állandó, a mozgást a csípő hátratolása adja.",
            "A rúd végig a lábszárhoz közel csússzon.",
            "A hát egyenes, addig menj le, amíg erős húzást érzel a hátsó combban.",
            "Felül a farizmot szorítsd össze, ne dőlj hátra.",
        ]),
        "Barbell_Shoulder_Press": ("Vállból nyomás rúddal", [
            "Állj stabilan, farizom és has feszítve, a derék ne ívelje túl.",
            "Fogás vállszélesség fölött, az alkar függőleges.",
            "A fejed húzd kicsit hátra, hogy a rúd az arcod előtt menjen fel, majd bújj át alatta.",
            "Fent teljesen kinyújtott kar, a rúd a fej felett, ne dőlj hátra.",
        ]),
        "Dumbbell_Shoulder_Press": ("Vállból nyomás kézisúlyzóval", [
            "A súlyzók a fülmagasságban induljanak, az alkar függőleges.",
            "Nyomj egyenesen felfelé, ne dőlj hátra.",
            "A törzs feszes, a derék nem ívelődik be.",
            "Lassan engedd vissza a kiinduló helyzetbe.",
        ]),
        "Bent_Over_Barbell_Row": ("Előrehajolt evezés rúddal", [
            "A törzs kb. 45°-ban előre, a hát végig egyenes, a térd enyhén hajlított.",
            "A rudat a köldök / alsó mellkas felé húzd, a könyök a test mellett marad.",
            "Fent szorítsd össze a lapockákat egy pillanatra.",
            "Ne lendíts a törzzsel – ha ez kell, vegyél vissza a súlyból.",
        ]),
        "One-Arm_Dumbbell_Row": ("Egykezes evezés", [
            "Támaszkodj a padra, a hát vízszintes és egyenes.",
            "A súlyzót a csípőd felé húzd, a könyök a törzs mellett.",
            "Lent engedd ki a lapockát teljesen, fent szorítsd össze.",
            "A törzs ne forduljon be.",
        ]),
        "Pullups": ("Húzódzkodás", [
            "Teljes lógásból indulj, majd először a lapockákat húzd le.",
            "Húzd a mellkasod a rúd felé, az áll a rúd fölé érjen.",
            "A könyököt lefelé és kicsit hátra vidd.",
            "Lassan engedd le, ne lendíts a lábbal.",
        ]),
        "Wide-Grip_Lat_Pulldown": ("Lehúzás széles fogással", [
            "Lapockákat húzd le, mielőtt a karod húzni kezd.",
            "A rudat a kulcscsont / felső mellkas felé húzd, ne a tarkóhoz.",
            "Ne dőlj túl hátra, a törzs maradjon majdnem függőleges.",
            "Felül engedd fel teljesen, de kontrolláltan.",
        ]),
        "Barbell_Curl": ("Bicepsz hajlítás rúddal", [
            "A könyök a törzs mellett marad, ne menjen előre.",
            "Ne lendíts a törzzsel és a csípővel.",
            "Teljes mozgástartomány: lent kinyújtva, fent összehúzva.",
            "A csukló egyenes, lassú leengedés.",
        ]),
        "Dips_-_Triceps_Version": ("Tolódzkodás (tricepsz)", [
            "Törzs függőleges, a könyök a test mellett.",
            "Addig menj le, amíg a felkar vízszintes lesz.",
            "Felül zárd ki a könyököt, de ne feszítsd túl.",
            "A váll ne emelkedjen a füledig.",
        ]),
        "Leg_Press": ("Lábtoló gép", [
            "A fenék és a hát végig a támlán marad, a derék ne emelkedjen el.",
            "Lábak vállszélesen, a térd a lábujj irányába.",
            "Ne zárd ki teljesen a térdet felül.",
            "Annyira engedd le, amennyire a derék még a támlán marad.",
        ]),
        "Barbell_Hip_Thrust": ("Csípőemelés rúddal", [
            "A lapockák alja a padon, a talp a talajon, a térd kb. 90°.",
            "Az állat húzd kicsit a mellkasra, a tekintet előre.",
            "Fent a farizmot szorítsd össze, a törzs és a comb egy vonal legyen.",
            "Ne ívelj be a derékkal – a csípő dolgozik, nem az ágyék.",
        ]),
        "Standing_Calf_Raises": ("Vádliemelés állva", [
            "Teljes mozgástartomány: lent nyújtsd ki, fent állj ki a lábujjhegyre.",
            "Fent tarts meg egy pillanatra.",
            "Lassú, kontrollált mozgás, ne pattogj.",
        ]),
        "Face_Pull": ("Face pull", [
            "A kötelet az arc felé húzd, a könyök magasan, a kéz a fül mellé érjen.",
            "A végén fordítsd kifelé a kezeket, szorítsd össze a lapockákat.",
            "Könnyű súllyal, szép technikával dolgozz.",
        ]),
        "Side_Lateral_Raise": ("Oldalemelés", [
            "Enyhén hajlított könyök, a súlyzó csak vállmagasságig jön fel.",
            "Ne lendíts a törzzsel, a váll ne emelkedjen a füledig.",
            "Lassan engedd vissza.",
        ]),
        "Triceps_Pushdown": ("Tricepsz lenyomás csigán", [
            "A könyök a törzs mellett rögzítve marad.",
            "Teljesen nyújtsd ki lent, fent csak derékszögig engedd fel.",
            "Ne dőlj rá a súlyra, a váll nyugodt.",
        ]),
        "Barbell_Lunge": ("Kitörés rúddal", [
            "Hosszú lépés előre, a törzs függőleges.",
            "Az első térd a lábfej felett marad, nem esik befelé.",
            "Hátul a térd majdnem a talajt éri.",
            "A sarokkal told vissza magad az induló helyzetbe.",
        ]),
        "Dumbbell_Lunges": ("Kitörés kézisúlyzóval", [
            "Hosszú lépés előre, a törzs függőleges.",
            "Az első térd a lábfej felett marad.",
            "Hátul a térd majdnem a talajt éri.",
            "Told vissza magad az első lábbal.",
        ]),
    ]
    static func exerciseName(_ id: String) -> String? { info[id]?.name }
    static func tips(_ id: String) -> [String] { info[id]?.tips ?? [] }
}
