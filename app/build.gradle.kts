plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.my_gachon_map"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.my_gachon_map"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")

    implementation("com.google.android.gms:play-services-maps:20.0.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
}
