plugins {
    java
    application
}

group = "io.github.landgrafhomyak.chatwars"
version = "1.0"

repositories {
    mavenCentral()
}


dependencies {
    implementation("org.xerial:sqlite-jdbc:3.40.0.0")
    implementation("org.json:json:20220924")
}

//tasks.withType<KotlinCompile> {
//    kotlinOptions.jvmTarget = "17"
//}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

java {
    this.sourceCompatibility = JavaVersion.VERSION_11
    this.targetCompatibility = JavaVersion.VERSION_11
}

application {
    mainClass.set("io.github.landgrafhomyak.chatwars.ny2023_map.Main")
}

tasks.register<Jar>("buildFatJar") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    group = "application"
    // archiveVersion.set("v${project.version}")
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }
    from(configurations.runtimeClasspath.get().filter { f ->
        f.exists()
    }.map { d -> if (d.isDirectory) d else zipTree(d) })
    with(tasks.jar.get() as CopySpec)
    @Suppress("DEPRECATION")
    destinationDir = projectDir.resolve("out")
}
