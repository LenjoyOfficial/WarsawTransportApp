import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

group = "me.lenjoy.warsawtransportapp"
version = "1.0"

plugins {
	alias(libs.plugins.kotlinMultiplatform)
	alias(libs.plugins.androidApplication)
	alias(libs.plugins.composeMultiplatform)
	alias(libs.plugins.composeCompiler)
	alias(libs.plugins.kotlinSerialization)
	alias(libs.plugins.buildkonfig)
}

kotlin {
	// Suppress warnings about expect/actual classes being in Beta
	compilerOptions {
		freeCompilerArgs.add("-Xexpect-actual-classes")
	}

	androidTarget {
		compilerOptions {
			jvmTarget.set(JvmTarget.JVM_17)
		}
	}

	listOf(
		iosArm64(),
		iosSimulatorArm64()
	).forEach { iosTarget ->
		iosTarget.binaries.framework {
			baseName = "ComposeApp"
			isStatic = true
		}
	}

	sourceSets {
		androidMain.dependencies {
			implementation(libs.compose.uiToolingPreview)
			implementation(libs.androidx.activity.compose)
			implementation(libs.ktor.client.okhttp)
			implementation(libs.play.services.location)
			implementation(libs.kotlinx.coroutines.play.services)
		}
		commonMain.dependencies {
			implementation(libs.compose.runtime)
			implementation(libs.compose.foundation)
			implementation(libs.compose.material3)
			implementation(compose.materialIconsExtended)
			implementation(libs.compose.ui)
			implementation(libs.compose.components.resources)
			implementation(libs.compose.uiToolingPreview)
			implementation(libs.androidx.lifecycle.viewmodelCompose)
			implementation(libs.androidx.lifecycle.runtimeCompose)
			implementation(libs.jetbrains.navigation3.ui)
			implementation(libs.jetbrains.lifecycle.viewmodelNavigation3)
			implementation(libs.ktor.client.core)
			implementation(libs.ktor.client.contentNegotiation)
			implementation(libs.ktor.serialization.kotlinx.json)
			implementation(libs.ktor.client.logging)
			implementation(libs.kotlinx.datetime)
			implementation(libs.okio)
			implementation(libs.maps.compose)
			implementation(libs.maps.utils)
			implementation(libs.moko.permissions)
			implementation(libs.moko.permissions.compose)
			implementation(libs.moko.permissions.location)
		}
		iosMain.dependencies {
			implementation(libs.ktor.client.darwin)
		}
		commonTest.dependencies {
			implementation(libs.kotlin.test)
			implementation(libs.kotlinx.coroutines.test)
			implementation("com.squareup.okio:okio-fakefilesystem:3.10.2")
		}
	}
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
	localProperties.load(FileInputStream(localPropertiesFile))
}

fun getLocalProperty(key: String): String {
	return (localProperties[key] as String?) ?: (project.findProperty(key) as String?) ?: ""
}

android {
	namespace = "me.lenjoy.warsawtransportapp"
	compileSdk = libs.versions.android.compileSdk.get().toInt()

	defaultConfig {
		applicationId = project.group.toString()
		minSdk = libs.versions.android.minSdk.get().toInt()
		targetSdk = libs.versions.android.targetSdk.get().toInt()
		versionCode = 1
		versionName = project.version.toString()

		manifestPlaceholders["MAPS_API_KEY"] = getLocalProperty("maps.api.key")
	}
	buildFeatures {
		buildConfig = true
	}
	packaging {
		resources {
			excludes += "/META-INF/{AL2.0,LGPL2.1}"
		}
	}
	buildTypes {
		getByName("release") {
			isMinifyEnabled = false
			signingConfig = signingConfigs.getByName("debug")
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}
}

buildkonfig {
	packageName = "me.lenjoy.warsawtransportapp.config"
	defaultConfigs {
		val ztmApiKey = getLocalProperty("ztm.api.key")

		buildConfigField(FieldSpec.Type.STRING, "ZTM_API_KEY", ztmApiKey)
		buildConfigField(FieldSpec.Type.STRING, "MAPS_API_KEY", getLocalProperty("maps.api.key"))
		buildConfigField(FieldSpec.Type.STRING, "APP_NAME", rootProject.name)
		buildConfigField(FieldSpec.Type.STRING, "APP_ID", project.group.toString())
		buildConfigField(FieldSpec.Type.STRING, "VERSION", project.version.toString())
	}
}

dependencies {
	debugImplementation(libs.compose.uiTooling)
}
