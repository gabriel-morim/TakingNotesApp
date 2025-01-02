plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id ("com.google.gms.google-services")
}

android {
    namespace = "com.example.takingnotesapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.takingnotesapp"
        minSdk = 26
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
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.espresso.core)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation("com.colintheshots:twain:0.3.2")

    val nav_version = "2.8.2"
    implementation("androidx.navigation:navigation-compose:$nav_version")
    val compose_material_version = "1.7.3"
    implementation("androidx.compose.material:material:$compose_material_version")

    implementation ("com.google.firebase:firebase-firestore-ktx:24.10.1")
    implementation ("com.google.android.gms:play-services-auth:20.7.0")



    // Navigation Compose
    implementation ("androidx.navigation:navigation-compose:2.7.6")

    // Compose
    implementation ("androidx.compose.ui:ui:1.5.4")
    implementation ("androidx.compose.material3:material3:1.1.2")
    implementation ("androidx.activity:activity-compose:1.8.2")

    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-analytics")



    implementation ("com.google.firebase:firebase-auth-ktx")
    implementation ("com.google.firebase:firebase-firestore-ktx")

    // Compose Dependencies
    implementation ("androidx.compose.ui:ui:1.5.0")
    implementation ("androidx.compose.runtime:runtime:1.5.0")
    implementation ("androidx.compose.material:material:1.5.0")

    // Coil for image loading
    implementation ("io.coil-kt:coil:2.3.0")
    implementation ("io.coil-kt:coil-compose:2.3.0")


    val markwon_version = "4.6.2"
    implementation ("io.noties.markwon:core:$markwon_version")
    implementation ("io.noties.markwon:ext-strikethrough:$markwon_version")
    implementation ("io.noties.markwon:ext-tables:$markwon_version")
    implementation ("io.noties.markwon:html:$markwon_version")
    implementation ("io.noties.markwon:image-coil:$markwon_version")
    implementation ("io.noties.markwon:editor:$markwon_version")
}

