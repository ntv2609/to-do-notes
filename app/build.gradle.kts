plugins {
alias(libs.plugins.android.application)
}

android {
namespace = "com.vinhnt.todonotes"
compileSdk {
version = release(37)
}

    defaultConfig {
        applicationId = "com.vinhnt.todonotes"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
                arguments["room.incremental"] = "true"
            }
        }
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    sourceSets {
        getByName("androidTest").assets.srcDir("$projectDir/schemas")
    }
}

dependencies {
implementation(libs.activity.ktx)
implementation(libs.appcompat)
implementation(libs.constraintlayout)
implementation(libs.material)
implementation(libs.recyclerview)
implementation(libs.lifecycle.viewmodel)
implementation(libs.lifecycle.livedata)
implementation(libs.room.runtime)
annotationProcessor(libs.room.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.arch.core.testing)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.arch.core.testing)
}
