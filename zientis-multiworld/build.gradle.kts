plugins {
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow")
}

description = "Multi-world management system for Zientis Server"

dependencies {
    // Core Module
    api(project(":zientis-core"))
    
    // Paper API
    compileOnly("io.papermc.paper:paper-api:${property("paperVersion")}")
    testImplementation("io.papermc.paper:paper-api:${property("paperVersion")}")
    
    // BentoBox - 暫時註解掉以進行測試
    // compileOnly("world.bentobox:bentobox:${property("bentoboxVersion")}")
    
    // Database dependencies (will be shaded)
    implementation("com.zaxxer:HikariCP:${property("hikariVersion")}")
    implementation("org.mariadb.jdbc:mariadb-java-client:${property("mariadbVersion")}")
    
    // Testing dependencies are inherited from parent
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        archiveFileName.set("${project.name}-${project.version}.jar")
        
        // Relocate dependencies to avoid conflicts
        relocate("com.zaxxer.hikari", "com.zientis.multiworld.libs.hikari")
        relocate("org.mariadb", "com.zientis.multiworld.libs.mariadb")
        
        // Exclude unnecessary files
        exclude("META-INF/DEPENDENCIES")
        exclude("META-INF/LICENSE*")
        exclude("META-INF/NOTICE*")
        exclude("META-INF/*.SF")
        exclude("META-INF/*.DSA")
        exclude("META-INF/*.RSA")
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
        filesMatching("plugin.yml") {
            expand(project.properties)
        }
    }
}

// Configure the jar as the main artifact
artifacts {
    archives(tasks.jar)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            
            pom {
                name.set("Zientis MultiWorld")
                description.set("Multi-world management system for Zientis Server")
            }
        }
    }
}