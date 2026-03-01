plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.kazvoeten.omadketonics.data.repository"
    compileSdk = 36

    defaultConfig {
        minSdk = 27
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}


kotlin {
    jvmToolchain(11)
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    api(project(":data:local"))
    api(project(":data:remote"))

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.room.ktx)
    api(libs.retrofit.core)
    api(libs.retrofit.moshi)
    api(libs.okhttp.core)
    api(libs.okhttp.logging)
    api(libs.moshi.kotlin)
    implementation(libs.dagger.hilt.android)
    implementation(libs.androidx.health.connect.client)
    ksp(libs.dagger.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.androidx.health.connect.testing)
}



