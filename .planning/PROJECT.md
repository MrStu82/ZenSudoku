# ZenSudoku (Android)

A calm, offline Sudoku for unwinding, with a Japanese aesthetic driving every visual, sound and interaction choice.
Kotlin + Jetpack Compose, DataStore for settings/scores/resume. No network, no accounts, no Firebase.

- Look: approved prototype 78e3f35 + difficulty menu 2d11acf (washi, seigaiha, brush ensō, Shippori Mincho / Yuji Syuku, ai/shu/matcha/sumi).
- Scope: all 17 features in research DESIGN.md (approved by Stuart, 27 Oct 2026).
- Music: all five tracks are Stuart's (spirit/earth/ether his own Suno tracks; wind/water his original repo uploads), bundled locally.
- Generator is a bit-exact port of the web version; `SudokuTest` pins seed parity.
- Delivery: signed, minified release APK 1.0.0 built on Trantor with Gradle (never GitHub Actions).
