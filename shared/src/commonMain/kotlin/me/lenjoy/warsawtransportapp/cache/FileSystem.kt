package me.lenjoy.warsawtransportapp.cache

import okio.FileSystem
import okio.Path

/**
 * The platform-specific cache directory path.
 * - Android: context.cacheDir
 * - iOS: NSCachesDirectory
 */
expect val cacheDir: Path?

/**
 * The default [FileSystem] for the current platform (typically SYSTEM).
 */
expect val platformFileSystem: FileSystem

/**
 * Returns the current time in milliseconds since the epoch for the current platform.
 */
expect fun getCurrentEpochMillis(): Long
