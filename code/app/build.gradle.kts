plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.string_events"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.string_events"
        minSdk = 26
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
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")

    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")

    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    val protoLite = "3.25.3"
    implementation("com.google.protobuf:protobuf-javalite:$protoLite")
    androidTestImplementation("com.google.protobuf:protobuf-javalite:$protoLite")
    testImplementation("com.google.protobuf:protobuf-javalite:$protoLite")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")

    debugImplementation("androidx.fragment:fragment-testing:1.6.2")

    testImplementation("junit:junit:4.13.2")
}

configurations.configureEach {
    exclude(group = "com.google.protobuf", module = "protobuf-lite")
    resolutionStrategy {
        force("com.google.protobuf:protobuf-javalite:3.25.3")
        eachDependency {
            if (requested.group == "com.google.protobuf" && requested.name == "protobuf-lite") {
                useTarget("com.google.protobuf:protobuf-javalite:3.25.3")
                because("Avoid NoSuchMethodError by aligning to javalite everywhere")
            }
        }
    }
}
