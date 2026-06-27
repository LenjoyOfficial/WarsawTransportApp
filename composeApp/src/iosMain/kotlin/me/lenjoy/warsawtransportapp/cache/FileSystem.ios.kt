package me.lenjoy.warsawtransportapp.cache

import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSDate
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import platform.Foundation.timeIntervalSince1970

actual val cacheDir: Path?
	get() {
		val paths = NSFileManager.defaultManager.URLsForDirectory(NSCachesDirectory, NSUserDomainMask)
		val cacheUrl = paths.firstOrNull() as? platform.Foundation.NSURL
		return cacheUrl?.path?.toPath()
	}

actual val platformFileSystem: FileSystem = FileSystem.SYSTEM

actual fun getCurrentEpochMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()
