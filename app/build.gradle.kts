plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.bitelens"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.bitelens"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"

    }
    buildFeatures {
        mlModelBinding = true
        viewBinding = true
    }
}

dependencies {

    implementation ("androidx.core:core-ktx:1.7.0")
    implementation ("androidx.appcompat:appcompat:1.5.1")
    implementation ("com.google.android.material:material:1.7.0")
    implementation ("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("org.tensorflow:tensorflow-lite-support:0.1.0")
    implementation ("org.tensorflow:tensorflow-lite-metadata:0.1.0")
    implementation("org.tensorflow:tensorflow-lite-gpu:2.3.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation("com.google.firebase:firebase-auth:22.3.1")
    implementation("com.google.firebase:firebase-database-ktx:20.3.1")
    implementation("com.google.firebase:firebase-database:20.3.1")
    implementation("androidx.test.ext:junit-ktx:1.1.5")
    implementation("androidx.test:runner:1.5.2")
    testImplementation ("junit:junit:4.13.2")
    androidTestImplementation ("androidx.test.ext:junit:1.1.4")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.5.0")
    implementation ("androidx.constraintlayout:constraintlayout:2.0.4")

    implementation("com.squareup.okhttp3:okhttp:4.9.0")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation ("com.google.android.material:material:1.11.0")

    implementation(platform("com.google.firebase:firebase-bom:32.8.1"))
    implementation("com.google.firebase:firebase-analytics")
    implementation ("com.google.firebase:firebase-auth:22.3.1") // Firebase Auth
    implementation ("com.google.firebase:firebase-firestore:24.11.1") // Cloud Firestore
    implementation ("com.google.firebase:firebase-storage:20.3.0") // Firebase Storage
    implementation ("com.google.android.gms:play-services-auth:21.1.0")

    implementation ("com.squareup.picasso:picasso:2.71828")


    implementation ("de.hdodenhof:circleimageview:3.1.0") //circle profile pic


    // JUnit for unit testing
    testImplementation ("junit:junit:4.13.2")
//    // Mockito for mocking objects
    testImplementation ("org.mockito:mockito-core:3.3.3")

    // Espresso dependencies
    androidTestImplementation ("androidx.test.ext:junit:1.1.5")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation ("androidx.test.espresso:espresso-intents:3.5.1")
    androidTestImplementation ("androidx.test.espresso:espresso-contrib:3.5.1")

    androidTestImplementation ("androidx.test:runner:1.5.2")
    androidTestImplementation ("androidx.test:rules:1.5.0")
    // Hamcrest (optional as it's included with Espresso, but ensures you have it)
    androidTestImplementation ("org.hamcrest:hamcrest:2.2")

    testImplementation ("org.robolectric:robolectric:4.7.3")

//    // Firebase Test Lab
//
//
//    // Make sure to include this if not already added
//    androidTestImplementation ("androidx.test:runner:1.5.2")
//    androidTestImplementation ("androidx.test:rules:1.5.0")


//    // For networking
//    implementation ("com.squareup.okhttp3:okhttp:4.10.0")
//    implementation ("com.squareup.moshi:moshi-kotlin:1.12.0")



//
//    // AndroidX Test Core
//    androidTestImplementation ("androidx.test:core:1.5.0")
//    // Needed for Intent and Bundle
//    androidTestImplementation ("androidx.test:runner:1.5.2")
//    // Mockito integration for Android
//    androidTestImplementation ("org.mockito:mockito-android:3.3.3")

//

}

