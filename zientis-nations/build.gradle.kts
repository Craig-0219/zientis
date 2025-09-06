plugins {
    `java-library`
    `maven-publish`
    id("com.gradleup.shadow")
}

description = "Nations and diplomacy system for Zientis Server"

dependencies {
    // Core Module
    api(project(":zientis-core"))
    api(project(":zientis-economy"))
    api(project(":zientis-multiworld"))
    
    // Paper API
    compileOnly("io.papermc.paper:paper-api:${property("paperVersion")}")
    
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