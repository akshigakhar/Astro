import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Environment takes precedence; local.properties is ignored by Git.
val localSettings =
    Properties().apply {
        val file = rootProject.file("local.properties")
        if (file.exists()) file.inputStream().use { load(it) }
    }

fun setting(environment: String, property: String): String? =
    providers.environmentVariable(environment).orNull?.takeIf { it.isNotBlank() }
        ?: localSettings.getProperty(property)?.takeIf { it.isNotBlank() }

val signingValues =
    linkedMapOf(
        "ASTRO_KEYSTORE_PATH" to setting("ASTRO_KEYSTORE_PATH", "astro.keystorePath"),
        "ASTRO_KEYSTORE_PASSWORD" to setting("ASTRO_KEYSTORE_PASSWORD", "astro.keystorePassword"),
        "ASTRO_KEY_ALIAS" to setting("ASTRO_KEY_ALIAS", "astro.keyAlias"),
        "ASTRO_KEY_PASSWORD" to setting("ASTRO_KEY_PASSWORD", "astro.keyPassword"),
    )
val hasReleaseSigning = signingValues.values.all { it != null }
val appVersionName = setting("ASTRO_VERSION_NAME", "astro.versionName") ?: "1.0.0"
val appVersionCode =
    (setting("ASTRO_VERSION_CODE", "astro.versionCode") ?: "1").toIntOrNull()?.takeIf {
        it in 1..2100000000
    } ?: error("ASTRO_VERSION_CODE / astro.versionCode must be a positive Android version code.")
val validateReleaseSigning =
    tasks.register("validateReleaseSigning") {
        doLast {
            val missing = signingValues.filterValues { it == null }.keys
            check(missing.isEmpty()) {
                "Release signing is required. Set environment variables or local.properties entries described in README. Missing: ${missing.joinToString()}"
            }
            check(rootProject.file(signingValues.getValue("ASTRO_KEYSTORE_PATH")!!).isFile) {
                "Release keystore file was not found. Check ASTRO_KEYSTORE_PATH / astro.keystorePath."
            }
        }
    }

// Never silently fall back to debug signing or an unsigned release APK.
tasks.configureEach { if (name == "preReleaseBuild") dependsOn(validateReleaseSigning) }

android {
    namespace = "com.astro.kundali"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.astro.kundali"
        minSdk = 26
        targetSdk = 34
        versionCode = appVersionCode
        versionName = appVersionName
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    signingConfigs {
        if (hasReleaseSigning)
            create("release") {
                storeFile = rootProject.file(signingValues.getValue("ASTRO_KEYSTORE_PATH")!!)
                storePassword = signingValues.getValue("ASTRO_KEYSTORE_PASSWORD")
                keyAlias = signingValues.getValue("ASTRO_KEY_ALIAS")
                keyPassword = signingValues.getValue("ASTRO_KEY_PASSWORD")
            }
    }
    buildTypes {
        getByName("release") {
            isDebuggable = false
            isMinifyEnabled = false
            if (hasReleaseSigning) signingConfig = signingConfigs.getByName("release")
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    testImplementation("junit:junit:4.13.2")
}
