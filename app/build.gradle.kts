plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.bibliotecaplus"
    compileSdk = 35

    val resolvedBaseUrl: String = when {
        project.hasProperty("baseUrl") -> project.property("baseUrl") as String
        System.getenv("BASE_URL") != null -> System.getenv("BASE_URL")!!
        else -> "http://10.0.2.2:3333/api/v1/"
    }

    defaultConfig {
        applicationId = "com.bibliotecaplus"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "BASE_URL", "\"$resolvedBaseUrl\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            val releaseUrl = if (project.hasProperty("baseUrl"))
                project.property("baseUrl") as String
            else
                System.getenv("BASE_URL") ?: "https://api.biblioteca.com/v1/"
            buildConfigField("String", "BASE_URL", "\"$releaseUrl\"")
        }
        debug {
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
