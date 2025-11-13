plugins {
  application
}

java {
  toolchain { languageVersion.set(org.gradle.jvm.toolchain.JavaLanguageVersion.of(17)) }
}

repositories { mavenCentral() }

dependencies {
  implementation("io.javalin:javalin-bundle:6.7.0")
  implementation("org.xerial:sqlite-jdbc:3.50.3.0")
}


tasks.register<JavaExec>("runGamification") {
  group = "application"
  mainClass.set("demo.Gamification")
  classpath = sourceSets["main"].runtimeClasspath
}
