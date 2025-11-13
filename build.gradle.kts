plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("com.google.gms.google-services") version "4.4.3" apply false

}
buildscript {
    dependencies {
        classpath ("com.android.tools.build:gradle:8.1.1")
        classpath("com.google.gms:google-services:4.4.3")
    }
}