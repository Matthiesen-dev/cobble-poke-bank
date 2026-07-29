plugins {
    id("com.gradleup.shadow")
    id("dev.architectury.loom")
    id("architectury-plugin")
    id("matthiesen.shadow-platform-conventions")
}

architectury {
    platformSetupLoomIde()
    neoForge()
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/groups/public/")
    maven("https://thedarkcolour.github.io/KotlinForForge/")
    maven("https://maven.neoforged.net/releases/")
}

val shadowBundle: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

dependencies {
    minecraft(libs.minecraft.net)
    mappings(loom.officialMojangMappings())
    neoForge(libs.neoforge)
    modImplementation(libs.bundles.neoforgeModImplementation) { isTransitive = false }
    modRuntimeOnly(libs.bundles.neoforgeModRuntimeOnly)

    forgeRuntimeLibrary(libs.kotlinforforge) {
        exclude("net.neoforged.fancymodloader", "loader")
    }

    implementation(project(":common", configuration = "namedElements"))
    "developmentNeoForge"(project(":common", configuration = "namedElements")) {
        isTransitive = false
    }
    shadowBundle(project(":common", configuration = "transformProductionNeoForge"))

    runtimeOnly(libs.sqlite.jdbc)
    shadowBundle(libs.sqlite.jdbc)

    runtimeOnly(libs.mysql.connector.j)
    shadowBundle(libs.mysql.connector.j)

    testImplementation(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)
}

tasks {
    processResources {
        filesMatching("META-INF/neoforge.mods.toml") {
            expand(project.properties)
        }
    }

    shadowJar {
        exclude("fabric.mod.json")
        configurations = listOf(shadowBundle)
        relocate("com.mysql", "dev.matthiesen.cobble_poke_bank.shadow.com.mysql")
        relocate("com.google.protobuf", "dev.matthiesen.cobble_poke_bank.shadow.com.google.protobuf")
        relocate("org.sqlite", "dev.matthiesen.cobble_poke_bank.shadow.org.sqlite")
    }
}
