plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    `maven-publish`
}

group = "com.raiyanshahid"
version = "0.1.0"

android {
    namespace = "com.raiyanshahid.dustaway"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

kotlin {
    // Every public declaration must be deliberate and explicit.
    explicitApi()
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.animation.core)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.compose.material3)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

publishing {
    repositories {
        // Static Maven repo served by GitHub Pages from docs/maven (see README).
        maven {
            name = "docs"
            url = uri(rootProject.file("docs/maven"))
        }
    }
    publications {
        register<MavenPublication>("release") {
            groupId = "com.raiyanshahid"
            artifactId = "dustaway"
            version = project.version.toString()
            afterEvaluate { from(components["release"]) }

            pom {
                name.set("DustAway")
                description.set("Dust-and-ash disintegration effect for Jetpack Compose: Modifier.disintegrate(state)")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
            }
        }
    }
}
