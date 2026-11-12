package app.proud

/** Magyar nevek, szabályos technika tippek és szűrők. */
object Hu {
    const val IMG_BASE = "https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/exercises/"

    private val muscles = mapOf(
        "abdominals" to "Hasizom",
        "abductors" to "Csípőtávolító",
        "adductors" to "Combközelítő",
        "biceps" to "Bicepsz",
        "calves" to "Vádli",
        "chest" to "Mell",
        "forearms" to "Alkar",
        "glutes" to "Farizom",
        "hamstrings" to "Hátsó comb",
        "lats" to "Széles hátizom",
        "lower back" to "Ágyék",
        "middle back" to "Közép hát",
        "neck" to "Nyak",
        "quadriceps" to "Combfeszítő",
        "shoulders" to "Váll",
        "traps" to "Csuklyás izom",
        "triceps" to "Tricepsz"
    )

    fun muscle(key: String): String = muscles[key] ?: key

    private val equipmentNames = mapOf(
        "barbell" to "Rúd",
        "dumbbell" to "Kézisúlyzó",
        "cable" to "Csiga",
        "machine" to "Gép",
        "kettlebells" to "Kettlebell",
        "body only" to "Saját testsúly",
        "e-z curl bar" to "Hajlított rúd",
        "bands" to "Gumiszalag",
        "medicine ball" to "Medicinlabda",
        "exercise ball" to "Fitnesz labda",
        "foam roll" to "Henger",
        "other" to "Egyéb"
    )

    fun equipment(key: String): String = equipmentNames[key] ?: key

    private val levelNames = mapOf(
        "beginner" to "Kezdő",
        "intermediate" to "Haladó",
        "expert" to "Profi"
    )

    fun level(key: String): String = levelNames[key] ?: key

    /** Izomcsoportok a szűréshez és az achievementekhez. */
    val groups: LinkedHashMap<String, List<String>> = linkedMapOf(
        "Mell" to listOf("chest"),
        "Hát" to listOf("lats", "middle back", "lower back", "traps"),
        "Láb" to listOf("quadriceps", "hamstrings", "glutes", "calves", "adductors", "abductors"),
        "Váll" to listOf("shoulders"),
        "Kar" to listOf("biceps", "triceps", "forearms"),
        "Core" to listOf("abdominals")
    )

    fun groupOf(muscle: String): String? =
        groups.entries.firstOrNull { muscle in it.value }?.key

    /** Edzéscsoportok az edzésterv-választóhoz. */
    val templateGroups = listOf("Mell", "Hát", "Láb", "Váll", "Kar", "Core", "Teljes test")

    fun groupEmoji(g: String): String = when (g) {
        "Mell" -> "💪"
        "Hát" -> "🦍"
        "Láb" -> "🦵"
        "Váll" -> "🙌"
        "Kar" -> "💥"
        "Core" -> "🎯"
        "Teljes test" -> "🔥"
        else -> "🏋️"
    }

    val mainEquipment = listOf("barbell", "dumbbell", "cable", "machine", "body only", "kettlebells")

    class Info(val name: String, val tips: List<String>)

    private val info: Map<String, Info> = mapOf(
        "Barbell_Full_Squat" to Info(
            "Guggolás rúddal",
            listOf(
                "Lábak vállszélesen vagy kicsit szélesebben, lábujjak enyhén kifelé.",
                "Emelés előtt vegyél mély levegőt a hasadba, és feszítsd meg a törzsed – ezt tartsd végig a mozgás alatt.",
                "A térd a lábujjak irányába nyíljon, ne essen befelé.",
                "A hát végig egyenes, a mellkas felfelé néz; a csípő és a térd egyszerre hajlik.",
                "Menj le legalább addig, amíg a comb vízszintes lesz, a sarok végig a talajon marad.",
                "Felfelé a csípő és a mellkas együtt emelkedjen – ne a csípő induljon előre."
            )
        ),
        "Barbell_Squat" to Info(
            "Guggolás (rúd a vállon)",
            listOf(
                "Rúd a hátsó vállon, a lapockákat húzd össze, hogy a rúd stabilan üljön.",
                "Vegyél levegőt, feszítsd meg a törzsed, és csak utána kezdj lemenni.",
                "Térd a lábujjak irányába, a sarok a talajon.",
                "Mély, kontrollált lemenet, a hát egyenes marad.",
                "Felfelé a talajt told el magadtól, a mellkas ne bukjon előre."
            )
        ),
        "Barbell_Bench_Press_-_Medium_Grip" to Info(
            "Fekvenyomás rúddal",
            listOf(
                "Lapockákat húzd hátra és le, és tartsd így végig; a farpofa végig a padon.",
                "Lábak stabilan a talajon, a hát enyhén ívelt.",
                "Fogás kicsivel vállszélesség fölött; a legalsó pontnál az alkarod függőleges.",
                "A rúd a mellkas alsó részére érkezzen, a könyök kb. 45–75°-ban a törzstől (ne oldalra, 90°-ban).",
                "Kontrolláltan érintsd a mellkast, ne pattintsd; csukló egyenes, a rúd az alkar felett."
            )
        ),
        "Dumbbell_Bench_Press" to Info(
            "Fekvenyomás kézisúlyzóval",
            listOf(
                "Lapockák hátra és le, mellkas kidüllesztve.",
                "Könyök kb. 45–60°-ban a törzstől, ne oldalra.",
                "A súlyzók a mellkas magasságáig ereszkedjenek, kontrolláltan.",
                "Nyomás közben a súlyzók a mellkas felett találkozzanak, a váll ne emelkedjen fel."
            )
        ),
        "Incline_Dumbbell_Press" to Info(
            "Ferde padon nyomás kézisúlyzóval",
            listOf(
                "A pad dőlése kb. 30°; ennél meredekebb a vállat terheli.",
                "Lapockák hátra és le, a fenék a padon.",
                "Könyök kb. 45–60°-ban a törzstől.",
                "Lassú lemenet, a súlyzók a mellkas felső részénél érjenek le."
            )
        ),
        "Barbell_Deadlift" to Info(
            "Felhúzás",
            listOf(
                "A rúd a lábfej közepe felett legyen, a lapockák kb. a rúd felett.",
                "Fogd meg a rudat, húzd le-hátra a lapockákat, és feszítsd ki a lazaságot a rúdból, mielőtt emelsz.",
                "A hát végig egyenes (semleges), a mellkas büszkén előre.",
                "A rúd végig a testhez közel mozogjon, szinte súrolja a combot.",
                "Felül a csípő és a térd egyszerre nyúljon ki, ne dőlj hátra.",
                "Lefelé kontrolláltan: előbb a csípő hátra, aztán a térd hajlik."
            )
        ),
        "Romanian_Deadlift" to Info(
            "Román felhúzás",
            listOf(
                "A térd enyhén hajlított és állandó, a mozgást a csípő hátratolása adja.",
                "A rúd végig a lábszárhoz közel csússzon.",
                "A hát egyenes, addig menj le, amíg erős húzást érzel a hátsó combban.",
                "Felül a farizmot szorítsd össze, ne dőlj hátra."
            )
        ),
        "Barbell_Shoulder_Press" to Info(
            "Vállból nyomás rúddal",
            listOf(
                "Állj stabilan, farizom és has feszítve, a derék ne ívelje túl.",
                "Fogás vállszélesség fölött, az alkar függőleges.",
                "A fejed húzd kicsit hátra, hogy a rúd az arcod előtt menjen fel, majd bújj át alatta.",
                "Fent teljesen kinyújtott kar, a rúd a fej felett, ne dőlj hátra."
            )
        ),
        "Dumbbell_Shoulder_Press" to Info(
            "Vállból nyomás kézisúlyzóval",
            listOf(
                "A súlyzók a fülmagasságban induljanak, az alkar függőleges.",
                "Nyomj egyenesen felfelé, ne dőlj hátra.",
                "A törzs feszes, a derék nem ívelődik be.",
                "Lassan engedd vissza a kiinduló helyzetbe."
            )
        ),
        "Bent_Over_Barbell_Row" to Info(
            "Előrehajolt evezés rúddal",
            listOf(
                "A törzs kb. 45°-ban előre, a hát végig egyenes, a térd enyhén hajlított.",
                "A rudat a köldök / alsó mellkas felé húzd, a könyök a test mellett marad.",
                "Fent szorítsd össze a lapockákat egy pillanatra.",
                "Ne lendíts a törzzsel – ha ez kell, vegyél vissza a súlyból."
            )
        ),
        "One-Arm_Dumbbell_Row" to Info(
            "Egykezes evezés",
            listOf(
                "Támaszkodj a padra, a hát vízszintes és egyenes.",
                "A súlyzót a csípőd felé húzd, a könyök a törzs mellett.",
                "Lent engedd ki a lapockát teljesen, fent szorítsd össze.",
                "A törzs ne forduljon be."
            )
        ),
        "Pullups" to Info(
            "Húzódzkodás",
            listOf(
                "Teljes lógásból indulj, majd először a lapockákat húzd le.",
                "Húzd a mellkasod a rúd felé, az áll a rúd fölé érjen.",
                "A könyököt lefelé és kicsit hátra vidd.",
                "Lassan engedd le, ne lendíts a lábbal."
            )
        ),
        "Wide-Grip_Lat_Pulldown" to Info(
            "Lehúzás széles fogással",
            listOf(
                "Lapockákat húzd le, mielőtt a karod húzni kezd.",
                "A rudat a kulcscsont / felső mellkas felé húzd, ne a tarkóhoz.",
                "Ne dőlj túl hátra, a törzs maradjon majdnem függőleges.",
                "Felül engedd fel teljesen, de kontrolláltan."
            )
        ),
        "Barbell_Curl" to Info(
            "Bicepsz hajlítás rúddal",
            listOf(
                "A könyök a törzs mellett marad, ne menjen előre.",
                "Ne lendíts a törzzsel és a csípővel.",
                "Teljes mozgástartomány: lent kinyújtva, fent összehúzva.",
                "A csukló egyenes, lassú leengedés."
            )
        ),
        "Dips_-_Triceps_Version" to Info(
            "Tolódzkodás (tricepsz)",
            listOf(
                "Törzs függőleges, a könyök a test mellett.",
                "Addig menj le, amíg a felkar vízszintes lesz.",
                "Felül zárd ki a könyököt, de ne feszítsd túl.",
                "A váll ne emelkedjen a füledig."
            )
        ),
        "Leg_Press" to Info(
            "Lábtoló gép",
            listOf(
                "A fenék és a hát végig a támlán marad, a derék ne emelkedjen el.",
                "Lábak vállszélesen, a térd a lábujj irányába.",
                "Ne zárd ki teljesen a térdet felül.",
                "Annyira engedd le, amennyire a derék még a támlán marad."
            )
        ),
        "Barbell_Hip_Thrust" to Info(
            "Csípőemelés rúddal",
            listOf(
                "A lapockák alja a padon, a talp a talajon, a térd kb. 90°.",
                "Az állat húzd kicsit a mellkasra, a tekintet előre.",
                "Fent a farizmot szorítsd össze, a törzs és a comb egy vonal legyen.",
                "Ne ívelj be a derékkal – a csípő dolgozik, nem az ágyék."
            )
        ),
        "Standing_Calf_Raises" to Info(
            "Vádliemelés állva",
            listOf(
                "Teljes mozgástartomány: lent nyújtsd ki, fent állj ki a lábujjhegyre.",
                "Fent tarts meg egy pillanatra.",
                "Lassú, kontrollált mozgás, ne pattogj."
            )
        ),
        "Face_Pull" to Info(
            "Face pull",
            listOf(
                "A kötelet az arc felé húzd, a könyök magasan, a kéz a fül mellé érjen.",
                "A végén fordítsd kifelé a kezeket, szorítsd össze a lapockákat.",
                "Könnyű súllyal, szép technikával dolgozz."
            )
        ),
        "Side_Lateral_Raise" to Info(
            "Oldalemelés",
            listOf(
                "Enyhén hajlított könyök, a súlyzó csak vállmagasságig jön fel.",
                "Ne lendíts a törzzsel, a váll ne emelkedjen a füledig.",
                "Lassan engedd vissza."
            )
        ),
        "Triceps_Pushdown" to Info(
            "Tricepsz lenyomás csigán",
            listOf(
                "A könyök a törzs mellett rögzítve marad.",
                "Teljesen nyújtsd ki lent, fent csak derékszögig engedd fel.",
                "Ne dőlj rá a súlyra, a váll nyugodt."
            )
        ),
        "Barbell_Lunge" to Info(
            "Kitörés rúddal",
            listOf(
                "Hosszú lépés előre, a törzs függőleges.",
                "Az első térd a lábfej felett marad, nem esik befelé.",
                "Hátul a térd majdnem a talajt éri.",
                "A sarokkal told vissza magad az induló helyzetbe."
            )
        ),
        "Dumbbell_Lunges" to Info(
            "Kitörés kézisúlyzóval",
            listOf(
                "Hosszú lépés előre, a törzs függőleges.",
                "Az első térd a lábfej felett marad.",
                "Hátul a térd majdnem a talajt éri.",
                "Told vissza magad az első lábbal."
            )
        )
    )

    fun exerciseName(id: String): String? = info[id]?.name
    fun tips(id: String): List<String> = info[id]?.tips ?: emptyList()
}
