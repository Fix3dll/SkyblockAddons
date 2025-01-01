import java.text.NumberFormat
import java.text.ParseException
import java.util.Locale
import org.apache.commons.lang3.SystemUtils

plugins {
    idea
    java
    id("gg.essential.loom") version ("1.6.21")
    id("dev.architectury.architectury-pack200") version ("0.1.3")
    id("io.freefair.lombok") version ("8.11")
    id("com.gradleup.shadow") version ("8.3.0")
    id("net.kyori.blossom") version ("1.3.1")
}

val group: String by project
val version: String by project
val minecraftVersion: String by project
val modId = project.name.lowercase(Locale.US)

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

blossom {
    if (project.hasProperty("runningOnCi")) {
        if (!project.hasProperty("buildNumber")) {
            throw InvalidUserDataException("No build number provided for CI build.")
        } else if (!project.hasProperty("runAttempt")) {
            throw InvalidUserDataException("No run attempt provided for CI build.")
        } else {
            val nf = NumberFormat.getIntegerInstance(Locale.US)
            val buildNumber = project.property("buildNumber")
            val runAttempt = project.property("runAttempt")
            val includeRunAttempt = nf.parse(runAttempt as String).toInt() > 1

            try {
                if (includeRunAttempt) {
                    project.setProperty("buildNumber", "${buildNumber}.${nf.parse(runAttempt).toInt() - 1}")
                }
                project.version = "${project.version}+" + project.property("buildNumber")
            } catch (e: ParseException) {
                throw InvalidUserDataException("Build number could not be parsed (${e.message})", e)
            }
        }
    }

    replaceTokenIn("src/main/java/codes/biscuit/skyblockaddons/SkyblockAddons.java")
    replaceToken("@VERSION@", version)
    replaceToken("@MOD_ACCEPTED@", minecraftVersion)
    replaceToken("@BUILD_NUMBER@", project.property("buildNumber"))
}

loom {
    runConfigs {
        getByName("client") {
            vmArg("-Xmx3G")
            property("mixin.debug", "true")
            property("devauth.enabled", "true")
            property("fml.coreMods.load", "codes.biscuit.skyblockaddons.tweaker.SkyblockAddonsLoadingPlugin")
            programArgs("--tweakClass", "org.spongepowered.asm.launch.MixinTweaker")
            if (SystemUtils.IS_OS_MAC_OSX) {
                // This argument causes a crash on macOS
                vmArgs.remove("-XstartOnFirstThread")
            }
        }
        remove(getByName("server"))
    }
    // Change this to one of the other log configs if desired
    log4jConfigs.from(file("log-config.xml"))
    forge {
        pack200Provider.set(dev.architectury.pack200.java.Pack200Adapter())
        accessTransformer("src/main/resources/META-INF/skyblockaddons_at.cfg")
        mixinConfig("mixins.${modId}.json")
    }
    mixin.defaultRefmapName = "mixins.${modId}.refmap.json"
}

sourceSets {
    main {
        // Forge needs resources to be in the same directory as the classes.
        output.setResourcesDir(java.classesDirectory)
    }
}

val bundle: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://repo.spongepowered.org/maven/")
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
    maven("https://repo.hypixel.net/repository/Hypixel/")
    maven("https://jitpack.io") {
        content {
            includeGroupByRegex("com\\.github\\..*")
        }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
    forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")
    runtimeOnly("me.djtheredstoner:DevAuth-forge-legacy:1.2.1")

    bundle("org.spongepowered:mixin:0.7.11-SNAPSHOT") {
        isTransitive = false
    }
    annotationProcessor("org.spongepowered:mixin:0.8.7-SNAPSHOT:processor")

    compileOnly("net.hypixel:mod-api-forge:1.0.1.1") {
        exclude(group = "me.djtheredstoner", module = "DevAuth-forge-legacy")
    }
    bundle("net.hypixel:mod-api-forge-tweaker:1.0.1.1")

    // Discord RPC for Java https://github.com/jagrosh/DiscordIPC
    bundle("io.github.CDAGaming:DiscordIPC:0.10.2") {
        exclude(module = "log4j")
        because("Different version conflicts with Minecraft's Log4J")
        exclude(module = "gson")
        because("Different version conflicts with Minecraft's GSON")
    }
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
}

tasks.withType(JavaCompile::class).configureEach {
    options.compilerArgs.add("-Xlint:unchecked")
    options.compilerArgs.add("-Xlint:deprecation")
    options.encoding = "UTF-8"
}

tasks.register("copyLicenses", Copy::class) {
    from(project.projectDir) {
        include(
            "LICENSE",
            "NOTICE",
            "dependencyLicenses/**"
        )
    }
    sourceSets.main.get().output.resourcesDir?.let { into(it) }
}

tasks.withType(Jar::class) {
    destinationDirectory.set(layout.buildDirectory.dir("badjars"))
    manifest.attributes.run {
        this["Manifest-Version"] = "2.0"
        this["Main-Class"] = "SkyblockAddonsInstallerFrame"
        this["Implementation-Title"] = project.name
        this["Implementation-Version"] = project.version
        this["Implementation-Vendor"] = "Fix3dll"
        this["Specification-Title"] = project.name
        this["Specification-Vendor"] = "Fix3dll"
        this["Specification-Version"] = project.version
        this["FMLCorePlugin"] = "${project.group}.${modId}.tweaker.${project.name}LoadingPlugin"
        this["ForceLoadAsMod"] = "true"
        this["FMLCorePluginContainsFMLMod"] = "true"
        this["ModSide"] = "CLIENT"
        this["FMLAT"] = "${modId}_at.cfg"
        this["TweakClass"] = "org.spongepowered.asm.launch.MixinTweaker"
        this["MixinConfigs"] = "mixins.${modId}.json"
    }
}

val remapJar by tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
    input = tasks.shadowJar.get().archiveFile
    archiveFileName = "${project.name}-${version}-for-MC-${minecraftVersion}.jar"
}

tasks.processResources {
    dependsOn("copyLicenses")
    inputs.property("version", version)
    inputs.property("mcversion", minecraftVersion)

    // replace stuff in mcmod.info, nothing else
    filesMatching("mcmod.info") {
        // replace version and mcversion
        expand("version" to version, "mcversion" to minecraftVersion)
    }
}

tasks.shadowJar {
    destinationDirectory = layout.buildDirectory.dir("badjars")
    configurations = listOf(bundle)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    exclude(
        "dummyThing",
        "module-info.class",
        "META-INF/versions/",
        "LICENSE.txt"
    )

    // Relocate Discord RPC into the main codebase
    relocate("com.jagrosh.discordipc", "${project.group}.${modId}.discordipc")
    relocate("net.hypixel.modapi.tweaker", "${project.group}.${modId}.modapi.tweaker")
}

tasks.withType(Test::class) {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.assemble.get().dependsOn(tasks.remapJar)