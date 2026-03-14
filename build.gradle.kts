import org.gradle.api.tasks.Delete

buildscript {

    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:8.9.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.10")
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}