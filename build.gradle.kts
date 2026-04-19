plugins {
    id("idea")
    id("java")
    id("com.azuredoom.hytale-tools") version "1.0.23"
}


tasks.withType<Javadoc>().configureEach {
    (options as StandardJavadocDocletOptions).addStringOption(
        "Xdoclint:-missing",
        "-quiet"
    )
}

group = project.property("group").toString()

java {
    toolchain.languageVersion.set(
        JavaLanguageVersion.of(
            property("java_version").toString().toInt()
        )
    )
}

val lombokVersion = "1.18.40"

dependencies {
    compileOnly("org.jetbrains:annotations:26.1.0")
    compileOnly("org.jspecify:jspecify:1.0.0")
    implementation("curse.maven:hyui-1431415:7820303")

    // Lombok
    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")
    testCompileOnly("org.projectlombok:lombok:$lombokVersion")
    testAnnotationProcessor("org.projectlombok:lombok:$lombokVersion")
}

hytaleTools {
    javaVersion = property("java_version").toString().toInt()
    hytaleVersion = property("hytale_version").toString()
    manifestGroup = property("manifest_group").toString()
    modId = property("mod_id").toString()
    modDescription = property("mod_description").toString()
    modUrl = property("mod_url").toString()
    mainClass = property("main_class").toString()
    modCredits = property("mod_author").toString()
    manifestDependencies = property("manifest_dependencies").toString()
    manifestOptionalDependencies = property("manifest_opt_dependencies").toString()
    curseforgeId = property("curseforgeID").toString()
    disabledByDefault = property("disabled_by_default").toString().toBoolean()
    includesPack = property("includes_pack").toString().toBoolean()
    patchline = property("patchline").toString()
}

repositories {
    mavenCentral()
    maven { setUrl("https://cursemaven.com") }
}

val serverRunDir = file("$projectDir/run")
if (!serverRunDir.exists()) {
    serverRunDir.mkdirs()
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}
