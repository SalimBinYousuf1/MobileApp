buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.5.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.21")
        classpath("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:2.0.21-1.0.27")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.52")
        classpath("com.google.protobuf:protobuf-gradle-plugin:0.9.4")
    }
}
