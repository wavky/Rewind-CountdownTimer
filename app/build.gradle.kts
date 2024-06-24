import java.text.SimpleDateFormat
import java.util.Date

plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("kotlin-parcelize")
  id("org.jetbrains.kotlin.kapt")
  id("com.google.gms.google-services")
  id("com.google.firebase.crashlytics")
  id("androidx.navigation.safeargs.kotlin")
}

val debug = "Debug"
val beta = "Beta"
val staging = "Stg"
val baseName = "CDTimer"

android {
  namespace = "com.wavky.cdtimer"
  compileSdk = 34

  defaultConfig {
    applicationId = "com.wavky.cdtimer"
    minSdk = 30 // Android 11
    targetSdk = 34
    versionCode = 4
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildFeatures {
    buildConfig = true
    viewBinding = true
  }

  signingConfigs {
    create("release") {
      storeFile = file("release.keystore")
      storePassword = "%Va2XuXybHa7\$q63"
      keyAlias = "release"
      keyPassword = "anNX#2n^Z3PxUCZS"
    }
  }

  buildTypes {
    debug {
      val date = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
      versionNameSuffix = "_${date}"
      applicationIdSuffix = ".debug"
      isMinifyEnabled = false
      manifestPlaceholders["crashlyticsCollectionEnabled"] = false
      manifestPlaceholders["analyticsCollectionEnabled"] = false
    }
    release {
      isMinifyEnabled = false
      signingConfig = signingConfigs.getByName("release")
      manifestPlaceholders["crashlyticsCollectionEnabled"] = true
      manifestPlaceholders["analyticsCollectionEnabled"] = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
    create("beta").initWith(buildTypes.getByName("release"))
    getByName("beta") {
      versionNameSuffix = "_${beta}"
      applicationIdSuffix = ".beta"
      isDebuggable = true
    }
    create("staging").initWith(buildTypes.getByName("release"))
    getByName("staging") {
      versionNameSuffix = "_${staging}"
      applicationIdSuffix = ".stg"
      isDebuggable = true
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

  kotlinOptions {
    jvmTarget = "1.8"
  }

//  tasks.whenTaskAdded {
//    if (name in setOf("assembleDebug", "assembleBeta", "assembleStaging", "assembleRelease")) {
//      finalizedBy(tasks.named("renameOutputApk"))
//    }
//  }
}

tasks.register("renameOutputApk") {
  fun apkVersionName(): String {
    return android.defaultConfig.versionName?.replace(Regex("\\."), "_") ?: ""
  }

  doLast {
    buildOutputs.toList().forEach {
      when (it.name) {
        "debug" -> it.outputFile.renameTo(
          File(
            it.outputFile.parent,
            "${baseName}_${debug}_${apkVersionName()}.apk"
          )
        )

        "beta" -> it.outputFile.renameTo(
          File(
            it.outputFile.parent,
            "${baseName}_${beta}_${apkVersionName()}.apk"
          )
        )

        "staging" -> it.outputFile.renameTo(
          File(
            it.outputFile.parent,
            "${baseName}_${staging}_${apkVersionName()}.apk"
          )
        )

        "release" -> it.outputFile.renameTo(
          File(
            it.outputFile.parent,
            "${baseName}_${apkVersionName()}.apk"
          )
        )
      }
    }
  }
}.configure {
  dependsOn(tasks.matching {
    it.name in listOf(
      "assembleDebug", "assembleBeta", "assembleStaging", "assembleRelease"
    )
  })
}

dependencies {
  // coroutine
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
  implementation("io.github.reactivecircus.flowbinding:flowbinding-android:1.2.0")

  // androidx
  implementation("androidx.core:core-ktx:1.13.1")
  implementation("androidx.appcompat:appcompat:1.7.0")
  implementation("androidx.fragment:fragment-ktx:1.8.0")
  implementation("androidx.constraintlayout:constraintlayout:2.1.4")
  implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
  val navVersion = "2.7.7"
  // Kotlin
  implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
  implementation("androidx.navigation:navigation-ui-ktx:$navVersion")

  // ------ google系 ------
  implementation("com.google.android.material:material:1.12.0")

  // firebase
  //  https://firebase.google.com/docs/android/learn-more#bom
  implementation(platform("com.google.firebase:firebase-bom:33.1.0"))
  // Add the dependencies for any other desired Firebase products
  // https://firebase.google.com/docs/android/setup#available-libraries
  implementation("com.google.firebase:firebase-analytics-ktx")
  implementation("com.google.firebase:firebase-crashlytics-ktx")

  // ------ 3rd-party libraries ------
  // glide
  implementation("com.github.bumptech.glide:glide:4.12.0")
  implementation("jp.wasabeef:glide-transformations:4.3.0")
  // koin
  implementation("io.insert-koin:koin-android:3.2.2")
  implementation("io.insert-koin:koin-android-compat:3.2.2")
  //  ------ debug ------
  debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")

  // test
  testImplementation("junit:junit:4.13.2")
  androidTestImplementation("androidx.test.ext:junit:1.1.5")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
  // Testing Navigation
  androidTestImplementation("androidx.navigation:navigation-testing:$navVersion")
}
