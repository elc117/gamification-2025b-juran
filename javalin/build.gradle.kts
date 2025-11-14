plugins {
    id("java")
    id("application")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.javalin:javalin:5.6.3")
    implementation("org.xerial:sqlite-jdbc:3.42.0.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.0")
    implementation("com.fasterxml.jackson.core:jackson-core:2.15.0")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.15.0")
    }

application {
    mainClass.set("demo.Gamification")
}

tasks.jar {
    manifest {
        attributes("Main-Class" to "demo.Gamification")
    }
}

tasks.register<JavaExec>("runGamification") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("demo.Gamification")
}