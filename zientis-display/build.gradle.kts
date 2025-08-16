plugins {
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow")
}

description = "Display and UI system for Zientis Server"

dependencies {
    // Core Module
    api(project(":zientis-core"))
    api(project(":zientis-multiworld"))
    
    // Paper API
    compileOnly("io.papermc.paper:paper-api:${property("paperVersion")}")
    
    // 全息圖庫 (HolographicDisplays API)
    compileOnly("me.filoghost.holographicdisplays:holographicdisplays-api:3.0.0")
    
    // NBT API for advanced block handling
    compileOnly("de.tr7zw:item-nbt-api:2.12.2")
    
    // Testing dependencies
    testImplementation("com.github.seeseemelk:MockBukkit-v1.20:3.9.0")
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        archiveFileName.set("${project.name}-${project.version}.jar")
    }
    
    build {
        dependsOn(shadowJar)
    }
    
    jar {
        enabled = false
    }
    
    processResources {
        filteringCharset = "UTF-8"
        filesMatching("**/*.yml") {
            expand(project.properties)
        }
    }
}

artifacts {
    archives(tasks.shadowJar)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            
            pom {
                name.set("Zientis Display")
                description.set("Display and UI system for Zientis Server")
            }
        }
    }
}