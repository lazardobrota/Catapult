import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("androidx.navigation.safeargs.kotlin")
    kotlin("plugin.serialization") version "1.9.22"
    id("com.google.dagger.hilt.android")
    //Room
    id("androidx.room")
    id("com.google.devtools.ksp")
}

val keystorePropertiesFile = rootProject.file("keystore.properties")

// Initializes a new Properties() object called keystoreProperties.
val keystoreProperties = Properties()

// Loads the keystore.properties file into the keystoreProperties object.
keystoreProperties.load(FileInputStream(keystorePropertiesFile))

android {
    namespace = "com.example.catapult"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.catapult"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField("String", "CAT_API_KEY", "\"${keystoreProperties.getProperty("CAT_API_KEY")}\"")
    }

    room {
        schemaDirectory("$projectDir/schemas")
    }


    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        android.buildFeatures.buildConfig=true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

//noinspection UseTomlInstead
dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Jetpack Navigation
    val navVersion = "2.7.7"
    implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navVersion")
    implementation("androidx.navigation:navigation-compose:$navVersion")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")

    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")

    // KotlinX Serialiazation
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Coil
    implementation("io.coil-kt:coil-compose:2.6.0")

    //Hilt
    val hiltVersion = "2.51"
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    ksp("com.google.dagger:hilt-android-compiler:$hiltVersion")
    // Hilt Compose Navigation support (hiltViewModel factory)
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    //Room
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    //DataStore
    implementation("androidx.datastore:datastore:1.1.1")

    //Extend to see all available icons
    implementation("androidx.compose.material:material-icons-extended:1.6.8")

//    //Stolen from professor
//    /*
//     * Testing
//     */
//    testImplementation(libs.junit)
//
//    testImplementation(libs.androidx.junit)
//    androidTestImplementation(libs.androidx.junit)
//
//    androidTestImplementation(libs.androidx.espresso.core)
//    androidTestImplementation(platform(libs.androidx.compose.bom))
//
//    androidTestImplementation(libs.androidx.ui.test.junit4)
//    debugImplementation(libs.androidx.ui.tooling)
//    debugImplementation(libs.androidx.ui.test.manifest)
//
//
//    // kotest.io
//    // https://kotest.io/docs/framework/framework.html
//    testImplementation("io.kotest:kotest-assertions-core:5.9.0")
//
//    // mockk.io
//    // https://mockk.io/
//    val mockk = "1.13.10"
//    testImplementation("io.mockk:mockk:$mockk")
//    testImplementation("io.mockk:mockk-android:$mockk")
//    androidTestImplementation("io.mockk:mockk:$mockk")
//    androidTestImplementation("io.mockk:mockk-android:$mockk")
//
//    // Coroutines
//    // https://developer.android.com/kotlin/coroutines/test
//    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
//
//    // Android testing
//    testImplementation("androidx.test:core-ktx:1.5.0")
//    testImplementation("androidx.test.ext:junit-ktx:1.1.5")

}
