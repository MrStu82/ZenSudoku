# Zen Sudoku

A calm, offline sudoku for Android: washi paper, sumi ink and quiet ambient music. No accounts, no network, no ads.

<img src="docs/screenshot.png" alt="Zen Sudoku game screen" width="360">

## Features

- Four levels: Pebble 石 (Easy), Bamboo 竹 (Medium), Stone 岩 (Hard), Mountain 山 (Expert).
- 8-character seeds: share a seed or recall one to play the same board. Seeds match the web version.
- Daily Seed 日, the same puzzle for everyone each day, with a quiet streak.
- Cell-first or digit-first input, up to four notes per cell, and notes that tidy themselves.
- Gentle guidance you can turn off: greyed impossible digits, possibility dots, a clarity border on cells with one answer, and Bloom to fill them.
- Hint lantern, undo, and a mistake limit of 3, 5 or none.
- Pause veil, resume after leaving, and an ensō with a hanko time stamp when you finish.
- Records: best and average per level, games played, and the longest daily streak.
- Themes Washi, Yoru, Sabi and Koke, with optional sakura or momiji accents.
- Calm mode, soft haptics, larger digits and a left-handed pad.

## Build

```
gradle :app:assembleRelease
```

Release signing reads `SIGNING_STORE_FILE`, `SIGNING_STORE_PASSWORD`, `SIGNING_KEY_ALIAS` and `SIGNING_KEY_PASSWORD` from the environment. No keystore is kept in this repository.

## Credits

- Music (`audio/`): all five tracks are Stuart's (_llama_) own, made with Suno. They're bundled as stereo Opus at 96 kbps, re-encoded from his MP3 masters (source SHA-256 below). To swap a track, replace its file; the playlist is the `Tracks` list in `Audio.kt`.
  - Wind 風 (`wind.webm`): "Wind", 28 Dec 2025. Source `2114d1c516877c90cfe9710d2bf4d74a94602031947eacf6df58197423e2d3d3`
  - Water 水 (`water.webm`): "Water", 29 Dec 2025. Source `b71cd2f77d4463b61cac8376051a36bef08d7486411e1fb8e0921424c6b4f738`
  - Spirit 魂 (`spirit.webm`): "Night Spirits", 28 Dec 2025. Source `bb686d43495cb35ff80b95695e9c06261107b1189f46f0c72889796fc2add010`
  - Earth 地 (`earth.webm`): "Bamboo Forest". Source `2d7668aaa37576b94b21bd6ee04fbdfe3f2ef2b7365c3a5d9a59d4d3aeaa1e6b`
  - Ether 空 (`ether.webm`): "The Slow Mountain Walk", 28 Dec 2025. Source `286c27f5453ec43590719618179063a674249880cecfcd7aaa1ba49f99eed15d`
- Fonts: Shippori Mincho, Yuji Syuku and JetBrains Mono, all under the SIL Open Font License (see `licenses/`).
