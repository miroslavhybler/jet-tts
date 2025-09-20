plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-parcelize")
    id("maven-publish")
}

android {
    namespace = "com.jet.tts"
    compileSdk = 36

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    kotlin {
        jvmToolchain(jdkVersion = 11)
    }
    buildFeatures {
        compose = true
    }
    publishing {
        multipleVariants {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {

    compileOnly(platform(libs.androidx.compose.bom))
    compileOnly(libs.androidx.ui)
    compileOnly(libs.androidx.material3)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components.getByName("release"))
                groupId = "com.jet"
                artifactId = "tts"
                version = "1.0.0-RC4"
                pom {
                    description.set("Jitpack.io deploy")
                }
            }

        }
        repositories {
            mavenLocal()
        }
    }
}