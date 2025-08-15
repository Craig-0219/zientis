plugins {
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow")
}

description = "Nations and diplomacy system for Zientis Server"

dependencies {
    // Core Module
    api(project(":zientis-core"))
    
    // Paper API
    compileOnly("io.papermc.paper:paper-api:${property("paperVersion")}")
    
    // Testing dependencies are inherited from parent
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
                name.set("Zientis Nations")
                description.set("Nations and diplomacy system for Zientis Server")
            }
        }
    }
}