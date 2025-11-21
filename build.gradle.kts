plugins {
    id("java")
    id("application")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.javalin:javalin:5.6.3")
    implementation("org.xerial:sqlite-jdbc:3.42.0.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.0")
    implementation("org.slf4j:slf4j-simple:2.0.7")
    }

application {
    mainClass.set("Gamification")
}

sourceSets {
    main {
        resources {
            srcDirs("src/main/resources")
            exclude("**/.keep")
        }
    }
}

tasks.jar {
    manifest {
        attributes("Main-Class" to "Gamification")
    }
    from(sourceSets.main.get().output)
}

tasks.register<JavaExec>("runGamification") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("Gamification")
}

tasks.shadowJar {
    archiveBaseName.set("gamification")
    archiveClassifier.set("")
    archiveVersion.set("")
    manifest {
        attributes("Main-Class" to "Gamification")
    }
}

tasks.named("build") {
    dependsOn("shadowJar")
}