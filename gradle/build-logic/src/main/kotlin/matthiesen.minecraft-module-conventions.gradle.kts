import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.task.RemapJarTask
import net.fabricmc.loom.task.RemapSourcesJarTask
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.named

plugins {
    id("matthiesen.project-conventions")
}

pluginManager.withPlugin("dev.architectury.loom") {
    configure<LoomGradleExtensionAPI> {
        enableTransitiveAccessWideners.set(true)
        silentMojangMappingsLicense()

        val clientConfig = runConfigs.getByName("client")
        clientConfig.programArg("--username=AdamMatthiesen")
        clientConfig.programArg("--uuid=2a1cde34-0cee-4b23-bc8f-9145b1b8cc51")
    }

    tasks.named<RemapJarTask>("remapJar") {
        archiveBaseName.set("${rootProject.property("archives_base_name")}-${project.name}")
        archiveVersion.set(project.version.toString())
    }

    tasks.named<RemapSourcesJarTask>("remapSourcesJar") {
        archiveBaseName.set("${rootProject.property("archives_base_name")}-${project.name}")
        archiveVersion.set(project.version.toString())
        archiveClassifier.set("sources")
    }
}

