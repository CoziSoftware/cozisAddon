plugins {
    id("fabric-loom") version "1.10.1"
}

base {
    archivesName.set(project.property("archives_base_name") as String)
}

version = project.property("mod_version") as String
group = project.property("maven_group") as String

repositories {
    exclusiveContent {
        forRepository {
            maven {
                name = "Modrinth"
                url = uri("https://api.modrinth.com/maven")
            }
        }
        filter {
            includeGroup("maven.modrinth")
        }
    }
    maven {
        name = "Meteor Dev Releases"
        url = uri("https://maven.meteordev.org/releases")
    }
    maven {
        name = "Meteor Dev Snapshots"
        url = uri("https://maven.meteordev.org/snapshots")
    }
    maven {
        name = "JitPack"
        url = uri("https://jitpack.io")
    }
}

dependencies {
    // Fabric
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${project.property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")

    // Meteor
    modImplementation("meteordevelopment:meteor-client:${project.property("meteor_version")}")
    // XaeroPlus
    modImplementation("maven.modrinth:xaeroplus:${project.property("xaeroplus_version")}")
    // XaeroWorldMap
    modImplementation("maven.modrinth:xaeros-world-map:${project.property("xaeros_worldmap_version")}")
    // XaeroMinimap
    modImplementation("maven.modrinth:xaeros-minimap:${project.property("xaeros_minimap_version")}")
    // lenni
    modImplementation("net.lenni0451:LambdaEvents:2.4.2")
    modImplementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
    // Baritone - compile only (users should install as a mod jar)
    modCompileOnly("meteordevelopment:baritone:${properties["baritone_version"] as String}")
}

tasks.named<ProcessResources>("processResources") {
    val props = mapOf(
        "version" to project.version,
        "mc_version" to project.property("minecraft_version"),
        "xp_version" to project.property("xaeroplus_version"),
        "xwm_version" to project.property("xaeros_worldmap_version"),
        "xmm_version" to project.property("xaeros_minimap_version")
    )
    inputs.properties(props)
    filesMatching("fabric.mod.json") {
        expand(props)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

