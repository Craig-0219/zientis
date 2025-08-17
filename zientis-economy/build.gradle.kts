plugins {
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow")
}

description = "Economy system for Zientis Server"

dependencies {
    // Core Module
    api(project(":zientis-core"))
    
    // Paper API
    compileOnly("io.papermc.paper:paper-api:${property("paperVersion")}")
    testImplementation("io.papermc.paper:paper-api:${property("paperVersion")}")
    
    // Vault API
    compileOnly("net.milkbowl.vault:VaultAPI:${property("vaultVersion")}")
    testImplementation("net.milkbowl.vault:VaultAPI:${property("vaultVersion")}")
    
    // Jackson for JSON processing
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.1")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.16.1")
    
    // Testing dependencies are inherited from parent
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        archiveFileName.set("${project.name}-${project.version}.jar")
    }
    
    // Temporarily disable shadowJar for development
    jar {
        archiveClassifier.set("")
        archiveFileName.set("${project.name}-${project.version}.jar")
    }
    
    processResources {
        filteringCharset = "UTF-8"
        filesMatching("**/*.yml") {
            expand(project.properties)
        }
    }
}

artifacts {
    archives(tasks.jar)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            
            pom {
                name.set("Zientis Economy")
                description.set("Economy system for Zientis Server")
            }
        }
    }
}