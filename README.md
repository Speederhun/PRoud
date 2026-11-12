# PRoud 🏋️

Személyes súlyzós edzésnapló Androidra, játékos achievementekkel.

## Funkciók

- **Választható edzésterv-sablonok** (Push / Pull / Láb / Teljes test) + saját tervek szerkesztése, szabad edzés
- **Edzés közbeni naplózás**: súly × ismétlés szettenként, a legutóbbi eredmény előtöltve, pihenő-időzítő (rezgéssel), élő 🏆 PR-jelzés, kijelző ébren tartása
- **Naptár**: elvégzett (narancs) és tervezett edzések, napi részletek, havi összesítő
- **Achievementek** (45 db, 4 szint) konfettis megszerzési animációval, XP és szintrendszer (Újonc → Legenda), heti széria
- **Gyakorlat-bemutató**: 675 gyakorlat animált (két képkockás) bemutatóval, izomcsoport- és eszköz-szűrővel, magyar „Szabályos technika" tippekkel a fő gyakorlatokhoz
- Minden adat a telefonon marad (`proud.json`), regisztráció nincs

## APK letöltése

Minden `main`-re pusholt változtatás után a GitHub Actions lefordítja az appot:

- **Legegyszerűbb:** *Releases → PRoud (legfrissebb)* → `PRoud.apk` (telefonról is letölthető)
- vagy *Actions → Build APK → Artifacts*

Telepítéskor engedélyezni kell az ismeretlen forrásból való telepítést.

## Fejlesztés

Kotlin + Jetpack Compose, minSdk 26. Nyisd meg Android Studióban, vagy:

```
./gradlew assembleDebug
```

Az APK: `app/build/outputs/apk/debug/app-debug.apk`

## Adatforrás

A gyakorlatok adatai és képei a [free-exercise-db](https://github.com/yuhonas/free-exercise-db) projektből származnak (Unlicense / közkincs). A képek az első megtekintéskor töltődnek le, utána gyorsítótárból jönnek. A gyakorlatok leírása angol nyelvű, a felület magyar.
