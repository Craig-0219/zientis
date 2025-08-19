plugins {
    `java-library`
    `maven-publish`
}

description = "Core API and data models for Zientis Server"

dependencies {
    // Paper API
    compileOnly("io.papermc.paper:paper-api:${property("paperVersion")}")
    testImplementation("io.papermc.paper:paper-api:${property("paperVersion")}")
    
    // Database
    api("org.mariadb.jdbc:mariadb-java-client:${property("mariadbVersion")}")
    api("redis.clients:jedis:${property("redisVersion")}")
    api("com.zaxxer:HikariCP:${property("hikariVersion")}")
    
    // Jackson for JSON processing (Discord API)
    api("com.fasterxml.jackson.core:jackson-databind:2.16.1")
    api("com.fasterxml.jackson.core:jackson-annotations:2.16.1")
    
    // Testing dependencies are inherited from parent
}

tasks {
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

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            
            pom {
                name.set("Zientis Core")
                description.set("Core API and data models for Zientis Server")
            }
        }
    }
}