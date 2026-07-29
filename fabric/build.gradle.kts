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

    runtimeOnly(libs.sqlite.jdbc)
    shadowBundle(libs.sqlite.jdbc)

    runtimeOnly(libs.mysql.connector.j)
    shadowBundle(libs.mysql.connector.j)

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
        relocate("com.google.protobuf", "dev.matthiesen.cobble_poke_bank.shadow.com.google.protobuf")
        relocate("org.sqlite", "dev.matthiesen.cobble_poke_bank.shadow.org.sqlite")
    }
}
