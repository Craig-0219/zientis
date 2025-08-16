plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.zientis"
version = "1.0.0-SNAPSHOT"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
}

dependencies {
    // Bukkit API
    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")
    
    // Zientis Core Dependencies
    implementation(project(":zientis-core"))
    implementation(project(":zientis-economy"))
    implementation(project(":zientis-multiworld"))
    implementation(project(":zientis-display"))
    implementation(project(":zientis-nations"))
    
    // Discord API Dependencies
    implementation("net.dv8tion:JDA:5.0.0-beta.15")
    implementation("club.minnced:discord-webhooks:0.8.4")
    
    // HTTP Client
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    
    // JSON Processing
    implementation("com.fasterxml.jackson.core:jackson-core:2.15.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.2")
    
    // Security
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    implementation("io.jsonwebtoken:jjwt-impl:0.11.5")
    implementation("io.jsonwebtoken:jjwt-jackson:0.11.5")
    
    // Caching
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
    
    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.3")
    testImplementation("org.mockito:mockito-core:5.4.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.4.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    archiveClassifier.set("")
    archiveFileName.set("ZientisDiscordAPI-${version}.jar")
    
    relocate("net.dv8tion.jda", "com.zientis.discord.lib.jda")
    relocate("club.minnced", "com.zientis.discord.lib.webhooks")
    relocate("okhttp3", "com.zientis.discord.lib.okhttp")
    relocate("com.fasterxml.jackson", "com.zientis.discord.lib.jackson")
    relocate("io.jsonwebtoken", "com.zientis.discord.lib.jwt")
    relocate("com.github.benmanes.caffeine", "com.zientis.discord.lib.caffeine")
    
    // 排除不需要的依賴
    exclude("META-INF/maven/**")
    exclude("META-INF/versions/**")
    exclude("**/module-info.class")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}