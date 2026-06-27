package me.lenjoy.warsawtransportapp.cache

import okio.FileSystem
import okio.Path

expect val cacheDir: Path?
expect val platformFileSystem: FileSystem
expect fun getCurrentEpochMillis(): Long
