plugins {
    `java-library`
    `maven-publish`
}

description = "Core API and data models for Zientis Server"

dependencies {
    // Paper API
    compileOnly("io.papermc.paper:paper-api:${property("paperVersion")}")
    testImplementation("io.papermc.paper:paper-api:${property("paperVersion")}")
    
    // Database - use implementation to include in JAR
    implementation("org.mariadb.jdbc:mariadb-java-client:${property("mariadbVersion")}")
    implementation("redis.clients:jedis:${property("redisVersion")}")
    implementation("com.zaxxer:HikariCP:${property("hikariVersion")}")
    
    // Jackson for JSON processing (Discord API) - use implementation
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.1")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.16.1")
    implementation("com.fasterxml.jackson.core:jackson-core:2.16.1")
    
    // Testing dependencies are inherited from parent
}

tasks {
    jar {
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
                name.set("Zientis Core")
                description.set("Core API and data models for Zientis Server")
            }
        }
    }
}