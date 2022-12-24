import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode


plugins {
    kotlin("multiplatform") version "1.7.0"
    java
    application
}

group = "io.github.landgrafhomyak.chatwars"
version = "1.0"

repositories {
    mavenCentral()
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


kotlin {
    explicitApi = ExplicitApiMode.Strict
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }

        tasks.register<Jar>("buildFatJar") {
            duplicatesStrategy = DuplicatesStrategy.EXCLUDE
            group = "application"
            // archiveVersion.set("v${project.version}")
            manifest {
                attributes["Main-Class"] = application.mainClass.get()
            }
            from(compilations["main"].output.classesDirs)
            from(compilations["main"].runtimeDependencyFiles.map { d -> if (d.isDirectory) d else zipTree(d) })
            with(tasks.jar.get() as CopySpec)
            @Suppress("DEPRECATION")
            destinationDir = projectDir.resolve("out")
        }

        withJava()
    }

    js(IR) {
        browser {
        }
    }

    @Suppress("UNUSED_VARIABLE")
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.xerial:sqlite-jdbc:3.40.0.0")
                implementation("org.json:json:20220924")
            }
        }
        val commonTest by getting
        val jvmMain by getting {
            dependsOn(commonMain)
        }
        val jsMain by getting {
            dependsOn(commonMain)
        }
    }
}

