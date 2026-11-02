package me.lenjoy.warsawtransportapp.cache

import android.annotation.SuppressLint
import android.content.Context
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

@SuppressLint("StaticFieldLeak")
private var appContext: Context? = null

fun initCache(context: Context) {
	appContext = context.applicationContext
}

actual val cacheDir: Path?
	get() = appContext?.cacheDir?.absolutePath?.toPath()

actual val platformFileSystem: FileSystem = FileSystem.SYSTEM

actual fun getCurrentEpochMillis(): Long = System.currentTimeMillis()
