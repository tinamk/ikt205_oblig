plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // Add Firebase plugin
    id("com.google.gms.google-services")


}

android {
    namespace = "com.example.applikasjons_avokadoene"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.applikasjons_avokadoene"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)


    // Firebase dependencies
    implementation(platform("com.google.firebase:firebase-bom:33.10.0"))
    implementation("com.google.firebase:firebase-analytics")

    // MPAndroidChart for grade distribution visualization
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation(libs.firebase.firestore.ktx)

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}