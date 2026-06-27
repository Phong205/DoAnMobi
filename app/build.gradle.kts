plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.quanlydeadline"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.quanlydeadline"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility =
            JavaVersion.VERSION_11

        targetCompatibility =
            JavaVersion.VERSION_11
    }
}

dependencies {
    // Firebase BOM — quản lý version tự động cho tất cả các gói Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))

    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-storage")

    // Thư viện cần thiết cho Google Sign-In (Credential Manager)
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // AppCompat
    implementation(
        "androidx.appcompat:appcompat:1.7.0"
    )

    // Material Design
    implementation(
        "com.google.android.material:material:1.12.0"
    )

    // ConstraintLayout
    implementation(
        "androidx.constraintlayout:constraintlayout:2.2.1"
    )

    // RecyclerView
    implementation(
        "androidx.recyclerview:recyclerview:1.3.2"
    )

    // CardView
    implementation(
        "androidx.cardview:cardview:1.0.0"
    )
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.firebase.storage)
    implementation(libs.googleid)

    // Room Database
    val roomVersion = "2.6.1"

    implementation(
        "androidx.room:room-runtime:$roomVersion"
    )

    annotationProcessor(
        "androidx.room:room-compiler:$roomVersion"
    )

    // Lifecycle
    implementation(
        "androidx.lifecycle:lifecycle-livedata:2.8.4"
    )

    implementation(
        "androidx.lifecycle:lifecycle-viewmodel:2.8.4"
    )

    // Unit Test
    testImplementation(
        "junit:junit:4.13.2"
    )

    // Android Test
    androidTestImplementation(
        "androidx.test.ext:junit:1.2.1"
    )

    androidTestImplementation(
        "androidx.test.espresso:espresso-core:3.6.1"
    )

    implementation(
        "com.github.PhilJay:MPAndroidChart:v3.1.0"
    )

}