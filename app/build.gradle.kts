import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "name.lmj0011.jetpackreleasetracker"
    compileSdk = 35
    defaultConfig {
        applicationId = "name.lmj0011.jetpackreleasetracker"
        minSdk = 21
        targetSdk = 35
        versionCode = 48
        versionName = "1.4.2"

        vectorDrawables {
            useSupportLibrary = true
        }

        multiDexEnabled = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // ref: https://stackoverflow.com/a/48674264/2445763
        kapt {
            arguments {
                arg("room.schemaLocation", "$projectDir/schemas")
            }
        }
    }

    buildTypes {
        named("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            resValue("bool", "DEBUG_MODE", "false")
            setProguardFiles(listOf(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"))
        }

        named("debug") {
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = true
            versionNameSuffix = "+debug.${getGitSha().take(8)}"
            applicationIdSuffix = ".debug"
            resValue("bool", "DEBUG_MODE", "true")
            setProguardFiles(listOf(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"))
        }
    }

    buildTypes.forEach {
        it.resValue("string", "app_build", getGitSha().take(8))
        it.resValue("string", "git_commit_count", getCommitCount())
        it.resValue("string", "git_commit_sha", getGitSha())
        it.resValue("string", "app_buildtime", getBuildTime())
    }


    compileOptions {
        // Flag to enable support for the new language APIs
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    // Big Up! to cSn: https://stackoverflow.com/a/36626584/2445763
    configurations.implementation {
        isCanBeResolved = true
    }

    configurations.androidTestImplementation {
        isCanBeResolved = true
    }

//    project.gradle.addBuildListener(object : BuildListener {
//        override fun buildStarted(gradle: Gradle) {}
//
//        override fun settingsEvaluated(settings: Settings) {}
//
//        override fun projectsLoaded(gradle: Gradle) {}
//
//        override fun projectsEvaluated(gradle: Gradle) {}
//
//        override fun buildFinished(result: BuildResult) {
//            var str = "# auto-generated; this file should be checked into version control\n"
//            val resolvedImplementationConfig = configurations.implementation.get().resolvedConfiguration
//            val resolvedAndroidTestImplementationConfig = configurations.androidTestImplementation.get().resolvedConfiguration
//            val fileName = "deps.list.txt"
//            val depsFile = File(projectDir, fileName)
//
//            resolvedImplementationConfig.firstLevelModuleDependencies.forEach { dep ->
//                str += "${dep.moduleGroup}:${dep.moduleName}:${dep.moduleVersion}\n"
//            }
//
//            resolvedAndroidTestImplementationConfig.firstLevelModuleDependencies.forEach { dep ->
//                str += "${dep.moduleGroup}:${dep.moduleName}:${dep.moduleVersion}\n"
//            }
//
//           GlobalScope.launch(Dispatchers.IO) {
//               depsFile.writeText(str)
//               println("\n${fileName} created.\n")
//           }
//        }
//    })
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")

    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.vectordrawable:vectordrawable:1.2.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.3")
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.3")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    // Room dependencies
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // GSON
    implementation("com.google.code.gson:gson:2.11.0")

    // Lifecycle-aware components
    // ref: https://developer.android.com/jetpack/androidx/releases/lifecycle
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // androidx.preference
    implementation("androidx.preference:preference-ktx:1.2.1")

    // WorkManager
    val workVersion = "2.9.1"
    implementation("androidx.work:work-runtime-ktx:$workVersion")

    // https://developer.android.com/studio/write/java8-support?utm_source=android-studio-4-0&utm_medium=studio-assistant-stable#library-desugaring
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.2")

    // other 3rd party libs
    implementation("com.jakewharton.timber:timber:4.7.1")
    implementation("com.vdurmont:semver4j:3.1.0")
    implementation("com.github.leandroborgesferreira:loading-button-android:2.3.0")
    implementation("com.github.kittinunf.fuel:fuel:2.2.3")
}

// Git is needed in your system PATH for these commands to work.
// If it's not installed, you can return a random value as a workaround
// ref: https://github.com/tachiyomiorg/tachiyomi/blob/master/app/build.gradle.kts
fun getCommitCount(): String {
    return runCommand("git rev-list --count HEAD")
}

fun getGitSha(): String {
    return runCommand("git rev-parse HEAD")
}

fun getBuildTime(): String {
    val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'")
    df.timeZone = TimeZone.getTimeZone("UTC")
    return df.format(Date())
}

fun runCommand(command: String): String {
    val byteOut = ByteArrayOutputStream()
    project.exec {
        commandLine = command.split(" ")
        standardOutput = byteOut
    }
    return String(byteOut.toByteArray()).trim()
}


