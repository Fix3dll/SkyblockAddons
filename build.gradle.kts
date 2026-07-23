import java.util.*
import java.text.NumberFormat
import java.text.ParseException

plugins {
    java
    id("net.fabricmc.fabric-loom") version ("1.15-SNAPSHOT")
    id("com.gradleup.shadow") version ("9.4.1")
    id("io.freefair.lombok") version ("9.2.0")
}

ext {
    if (project.hasProperty("runningOnCi")) {
        val nf = NumberFormat.getIntegerInstance(Locale.US)
        val buildNumber = project.property("buildNumber")
        val runAttempt = project.property("runAttempt")
        val includeRunAttempt = nf.parse(runAttempt as String).toInt() > 1

        try {
            if (includeRunAttempt) {
                project.setProperty("buildNumber", "${buildNumber}.${nf.parse(runAttempt).toInt() - 1}")
            }
            set("formattedVersion", "${project.version}+" + project.property("buildNumber"))
        } catch (e: ParseException) {
            set("formattedVersion", project.version)
            throw InvalidUserDataException("Build number could not be parsed (${e.message})", e)
        }
    } else {
        set("formattedVersion", project.version)
    }
}

base {
    archivesName.set(project.name)
}

loom {
    log4jConfigs.from(file(("log-config.xml")))
    accessWidenerPath.set(project.file("src/main/resources/skyblockaddons.classtweaker"))
    runConfigs {
        getByName("client") {
            vmArg("-Xmx4G")
            property("mixin.debug", "true")
            property("devauth.enabled", "false")
            property("sba.data.online", "false")
        }
        remove(getByName("server"))
    }
    mixin.useLegacyMixinAp = false
}

fabricApi {
    configureTests {
        createSourceSet = true
        modId = "skyblockaddons-test"
        enableGameTests = false
        eula = true
    }
}

repositories {
    // Add repositories to retrieve artifacts from in here.
    // You should only use this when depending on other mods because
    // Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
    // See https://docs.gradle.org/current/userguide/declaring_repositories.html
    // for more information about repositories.
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1") {
        content {
            includeGroup("me.djtheredstoner")
        }
    }
    maven("https://repo.hypixel.net/repository/Hypixel/") {
        content {
            includeGroup("net.hypixel")
        }
    }
    //maven("https://repo.nea.moe/releases")
    maven("https://jitpack.io") {
        content {
            includeGroupByRegex("com\\.github\\..*")
        }
    }
    maven("https://api.modrinth.com/maven") {
        content {
            includeGroup("maven.modrinth")
        }
    }
    maven("https://maven.terraformersmc.com/repository/maven-terraformers/") {
        content {
            includeGroup("com.terraformersmc")
        }
    }
    maven("https://maven.shedaniel.me") {
        content {
            includeGroupByRegex("me\\.shedaniel.*")
            includeGroup("dev.architectury")
        }
    }
}

val bundle : Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {
    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:${properties["minecraft_version"]}")
    implementation("net.fabricmc:fabric-loader:${properties["loader_version"]}")

    // Fabric API. This is technically optional, but you probably want it anyway.
    implementation("net.fabricmc.fabric-api:fabric-api:${properties["fabric_version"]}")
    implementation("com.terraformersmc:modmenu:${properties["modmenu_version"]}")

    // REI compat
    compileOnly("me.shedaniel:RoughlyEnoughItems-api-fabric:${properties["rei_version"]}") {
        exclude("net.fabricmc.fabric-api")
    }

    runtimeOnly("me.djtheredstoner:DevAuth-fabric:1.2.2")
    implementation("net.hypixel:mod-api:1.0.2")
    implementation("maven.modrinth:hypixel-mod-api:1.0.2+build.1+mc26.1")
    //bundle("moe.nea:libautoupdate:1.3.1")
    bundle("com.github.nea89o:libautoupdate:841d9f7e78") {
        exclude(module = "gson")
    }
    // Discord RPC for Java https://github.com/jagrosh/DiscordIPC
    bundle("io.github.cdagaming:DiscordIPC:0.11.3") {
        exclude(module = "log4j")
        because("Different version conflicts with Minecraft's Log4J")
        exclude(module = "gson")
        because("Different version conflicts with Minecraft's GSON")
    }

    // Test
    testImplementation(platform("org.junit:junit-bom:6.0.3"))
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("net.fabricmc:fabric-loader-junit:${properties["loader_version"]}")
}

tasks.withType(JavaCompile::class).configureEach {
    options.compilerArgs.add("-Xlint:unchecked")
    options.compilerArgs.add("-Xlint:deprecation")
    options.encoding = "UTF-8"
}

tasks.processResources {
    dependsOn("copyLicenses")

    inputs.property("version", ext.get("formattedVersion"))

    filesMatching("fabric.mod.json") {
        expand(mapOf(
            "version" to ext.get("formattedVersion"),
            "sbaJarName" to "${project.name}-${ext.get("formattedVersion")}-for-MC-${properties["minecraft_version"]}.jar",
            "sbaBuildNumber" to project.property("buildNumber"),
            "loader_version" to project.property("loader_version"),
            "minecraft_version" to project.property("minecraft_version"),
            "fabric_version" to project.property("fabric_version")
        ))
    }
}

tasks.register("copyLicenses", Copy::class) {
    from(project.projectDir) {
        include(
            "LICENSE",
            "dependencyLicenses/**"
        )
    }
    from(".github/docs/NOTICES.md").into(project.projectDir)
    sourceSets.main.get().output.resourcesDir?.let { into(it) }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(25)
}

tasks.jar {
    destinationDirectory.set(layout.buildDirectory.dir("intermediates"))
    from("LICENSE") {
        rename { "${it}_${base.archivesName.get()}"}
    }
    manifest.attributes.run {
        this["Main-Class"] = "SkyblockAddonsInstallerFrame"
    }
}

tasks.shadowJar {
    exclude("META-INF/versions/9/**")
    exclude("META-INF/versions/11/**")
    exclude("META-INF/versions/14/**")
    exclude("META-INF/versions/15/**")
    exclude("META-INF/versions/16/**")
    exclude("META-INF/versions/20/**")
    archiveFileName.set("${project.name}-${ext.get("formattedVersion")}-for-MC-${properties["minecraft_version"]}.jar")
    configurations = listOf(bundle)

    val basePackage = "${project.group}.${project.name.lowercase(Locale.US)}"
    relocate("com.jagrosh.discordipc", "${basePackage}.discordipc")
    relocate("moe.nea.libautoupdate", "${basePackage}.libautoupdate")

    from("LICENSE") {
        rename { "${it}_${base.archivesName.get()}"}
    }
    manifest.attributes.run {
        this["Main-Class"] = "SkyblockAddonsInstallerFrame"
    }
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.assemble.get().dependsOn(tasks.jar)