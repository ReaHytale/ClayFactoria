plugins {
    `maven-publish`
    id("hytale-mod") version "0.+"
}

group = "com.clayfactoria"
version = "0.1.0"
val javaVersion = 25

repositories {
    mavenCentral()
    maven("https://maven.hytale-modding.info/releases") {
        name = "HytaleModdingReleases"
    }
}

val lombokVersion = "1.18.40"

dependencies {
    compileOnly(libs.jetbrains.annotations)
    compileOnly(libs.jspecify)

    // Lombok
    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")

    testCompileOnly("org.projectlombok:lombok:$lombokVersion")
    testAnnotationProcessor("org.projectlombok:lombok:$lombokVersion")
}

hytale {
    // uncomment if you want to add the Assets.zip file to your external libraries;
    // ⚠️ CAUTION, this file is very big and might make your IDE unresponsive for some time!

     addAssetsDependency = true

    // uncomment if you want to develop your mod against the pre-release version of the game.
    //
    // updateChannel = "pre-release"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
    }

    withSourcesJar()
}

tasks.named<ProcessResources>("processResources") {
    var replaceProperties = mapOf(
        "plugin_group" to findProperty("plugin_group"),
        "plugin_maven_group" to project.group,
        "plugin_name" to project.name,
        "plugin_version" to project.version,
        "server_version" to findProperty("server_version"),

        "plugin_description" to findProperty("plugin_description"),
        "plugin_website" to findProperty("plugin_website"),

        "plugin_main_entrypoint" to findProperty("plugin_main_entrypoint"),
        "plugin_author" to findProperty("plugin_author")
    )

    filesMatching("manifest.json") {
        expand(replaceProperties)
    }

    inputs.properties(replaceProperties)
}

tasks.withType<Jar> {
    manifest {
        attributes["Specification-Title"] = rootProject.name
        attributes["Specification-Version"] = version
        attributes["Implementation-Title"] = project.name
        attributes["Implementation-Version"] =
            providers.environmentVariable("COMMIT_SHA_SHORT")
                .map { "${version}-${it}" }
                .getOrElse(version.toString())
    }
}

publishing {
    repositories {
        // This is where you put repositories that you want to publish to.
        // Do NOT put repositories for your dependencies here.
    }

    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

// IDEA no longer automatically downloads sources/javadoc jars for dependencies, so we need to explicitly enable the behavior.
idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}

val syncAssets = tasks.register<Copy>("syncAssets") {
    group = "hytale"
    description = "Automatically syncs assets from Build back to Source after server stops."

    // Take from the temporary build folder (Where the game saved changes)
    from(layout.buildDirectory.dir("resources/main"))

    // Copy into your actual project source (Where your code lives)
    into("src/main/resources")

    // IMPORTANT: Protect the manifest template from being overwritten
    exclude("manifest.json")

    // If a file exists, overwrite it with the new version from the game
    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    doLast {
        println("✅ Assets successfully synced from Game to Source Code!")
    }
}
