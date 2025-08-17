plugins {
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
}

group = "com.zientis"
version = "0.1.0-ALPHA"
description = "Revolutionary Minecraft Skyblock MMO Server"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

allprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    group = rootProject.group
    version = rootProject.version

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

}

subprojects {
    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://oss.sonatype.org/content/groups/public/")
        maven("https://repo.codemc.org/repository/maven-public/")
        maven("https://jitpack.io")
    }

    // Set version properties for subprojects
    ext.set("paperVersion", rootProject.extra["paperVersion"])
    ext.set("bentoboxVersion", rootProject.extra["bentoboxVersion"])
    ext.set("slimefunVersion", rootProject.extra["slimefunVersion"])
    ext.set("vaultVersion", rootProject.extra["vaultVersion"])
    ext.set("mariadbVersion", rootProject.extra["mariadbVersion"])
    ext.set("redisVersion", rootProject.extra["redisVersion"])
    ext.set("hikariVersion", rootProject.extra["hikariVersion"])

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    dependencies {
        // Testing
        testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
        testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.1")
        testImplementation("org.junit.platform:junit-platform-launcher:1.10.1")
        testImplementation("org.mockito:mockito-core:5.8.0")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.1")
    }
}

// Version catalog for dependency management
val paperVersion by extra("1.20.6-R0.1-SNAPSHOT")
val bentoboxVersion by extra("1.20.6")
val slimefunVersion by extra("RC-32")
val vaultVersion by extra("1.7")
val mariadbVersion by extra("3.3.2")
val redisVersion by extra("5.0.2")
val hikariVersion by extra("5.1.0")

// Publishing configuration
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            
            pom {
                name.set("Zientis Server")
                description.set("Revolutionary Minecraft Skyblock MMO Server")
                url.set("https://github.com/craig900219/zientis")
                
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                
                developers {
                    developer {
                        id.set("craig900219")
                        name.set("Craig")
                    }
                }
                
                scm {
                    connection.set("scm:git:git://github.com/craig900219/zientis.git")
                    developerConnection.set("scm:git:ssh://github.com:craig900219/zientis.git")
                    url.set("https://github.com/craig900219/zientis/tree/main")
                }
            }
        }
    }
}