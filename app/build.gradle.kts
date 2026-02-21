import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.ksp)
  alias(libs.plugins.google.services)
  alias(libs.plugins.navigation.safeargs)
  id("kotlin-parcelize")
}

android {
  namespace = "com.example.petrescue"
  compileSdk = 36

  defaultConfig {
    applicationId = "com.example.petrescue"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    val properties = Properties()
    val localPropertiesFile = project.rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
      properties.load(localPropertiesFile.inputStream())
    }

    val apiKey = properties.getProperty("LOCATION_IQ_KEY") ?: ""
    buildConfigField("String", "LOCATION_IQ_KEY", "\"$apiKey\"")
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  kotlinOptions {
    jvmTarget = "11"
  }
  buildFeatures {
    compose = true
    viewBinding = true
    buildConfig = true
  }
}

dependencies {
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.activity.compose)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.navigation.fragment)
  implementation(libs.androidx.navigation.ui)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.fragment)
  implementation(libs.androidx.room.runtime)
  implementation(libs.androidx.room.ktx)
  
  // Fixed Firebase dependency names to match libs.versions.toml
  implementation(libs.firebase.firestore)
  implementation(libs.firebase.storage)
  
  ksp(libs.androidx.room.compiler)
  implementation(platform(libs.firebase.bom))
  implementation(libs.firebase.auth)
  implementation(libs.java.jwt)
  implementation(libs.picasso)
  implementation(libs.retrofit.v300)
  implementation(libs.converter.gson.v290)
  implementation(libs.kotlinx.coroutines.android.v173)
  implementation(libs.android.sdk)
  implementation(libs.play.services.location)
  implementation(libs.secrets.gradle.plugin)

  implementation(libs.cloudinary.android)
  implementation(libs.cloudinary.android.download)
  implementation(libs.cloudinary.android.preprocess)

  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  debugImplementation(libs.androidx.compose.ui.tooling)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
}
