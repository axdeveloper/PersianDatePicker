plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.vanniktech.maven.publish") version "0.35.0"
    id("signing")
}

android {
    namespace = "com.xdev.arch.persiancalendar"
    compileSdk = 36

    defaultConfig {
        minSdk = 21
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

    buildFeatures {
        buildConfig = true
    }

    buildToolsVersion = "36.0.0"
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.10")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.recyclerview:recyclerview:1.3.0")
    implementation("com.google.android.material:material:1.10.0")
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()
    coordinates("io.github.axdeveloper.persiancalendar", "datepicker", "0.5.0")

    pom {
        name.set("PersianCalendar")
        description.set("Shamsi/Jalali date picker with Material Design (rewritten from Google's material-components for Android in Kotlin)")
        inceptionYear.set("2020")
        url.set("https://github.com/axdeveloper/PersianDatePicker")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("axdeveloper")
                name.set("Rahman Mohammadi")
                url.set("https://github.com/axdeveloper/")
                email.set("xdev.arch@gmail.com")
            }
        }
        scm {
            url.set("https://github.com/axdeveloper/PersianDatePicker")
            connection.set("scm:git:git://github.com/axdeveloper/PersianDatePicker.git")
            developerConnection.set("scm:git:ssh://git@github.com/axdeveloper/PersianDatePicker.git")
        }
    }
}