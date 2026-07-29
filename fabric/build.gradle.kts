plugins {
    id("com.gradleup.shadow")
    id("dev.architectury.loom")
    id("architectury-plugin")
    id("matthiesen.shadow-platform-conventions")
}

architectury {
    platformSetupLoomIde()
    fabric()
}

val shadowBundle: Configuration by configurations.creating

dependencies {
    minecraft(libs.minecraft)
    mappings(loom.officialMojangMappings())

    modImplementation(libs.bundles.fabricModImplementation)
    modImplementation(libs.bundles.fabricModImplementationNoTransitive) { isTransitive = false }
    modRuntimeOnly(libs.bundles.fabricModRuntimeOnly)

    implementation(project(":common", configuration = "namedElements"))
    "developmentFabric"(project(":common", configuration = "namedElements"))
    shadowBundle(project(":common", configuration = "transformProductionFabric"))

    runtimeOnly("org.xerial:sqlite-jdbc:3.47.2.0")
    shadowBundle("org.xerial:sqlite-jdbc:3.47.2.0")

    runtimeOnly("com.mysql:mysql-connector-j:8.4.0")
    shadowBundle("com.mysql:mysql-connector-j:8.4.0")

    testImplementation(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)
}

tasks {
    processResources {
        filesMatching("fabric.mod.json") {
            expand(project.properties)
        }
    }

    shadowJar {
        configurations = listOf(shadowBundle)
        relocate("com.mysql", "dev.matthiesen.cobble_poke_bank.shadow.com.mysql")
        relocate("org.sqlite", "dev.matthiesen.cobble_poke_bank.shadow.org.sqlite")
    }
}
