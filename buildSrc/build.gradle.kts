plugins {
    id("java-gradle-plugin")
    kotlin("jvm") version "1.5.0"
}
repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1-native-mt")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("com.electronwill.night-config:toml:3.6.3")
}

sourceSets {
    main.get().java.srcDirs("src/main/kotlin")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = freeCompilerArgs + "-XXLanguage:+InlineClasses"
    }
}

gradlePlugin {
    plugins {
        create("mcgenPlugin") {
            id = "szewek.mcgen.mcgen-plugin"
            implementationClass = "szewek.mcgen.MCGenPlugin"
        }
    }
}
