import org.gradle.api.tasks.JavaExec

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

val devClientUsername = providers.gradleProperty("devClientUsername").orElse("AdamMatthiesen")
val devClientUuid = providers.gradleProperty("devClientUuid").orElse("2a1cde34-0cee-4b23-bc8f-9145b1b8cc51")

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

    testImplementation(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)
}

tasks {
    named<JavaExec>("runClient") {
        args("--username", devClientUsername.get(), "--uuid", devClientUuid.get())
    }

    processResources {
        filesMatching("fabric.mod.json") {
            expand(project.properties)
        }
    }

    shadowJar {
        configurations = listOf(shadowBundle)
    }
}
