// Standalone JVM tool (not part of the library build). Compiles the Android-free engine files
// straight from the library sources, so the GIFs are drawn by the real DustEngine.
plugins { kotlin("jvm") version "2.3.10"; application }
repositories { mavenCentral() }
kotlin { jvmToolchain(17) }
sourceSets.main {
    kotlin.srcDir("../../dustaway/src/main/kotlin")
    kotlin.include("Render.kt", "**/DustEngine.kt", "**/DustConfig.kt", "**/DustParticles.kt")
}
application { mainClass.set("RenderKt") }
