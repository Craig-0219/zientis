// tools/deploy-runner.main.kts

import java.io.File
import java.util.Properties
import java.util.concurrent.TimeUnit
import java.security.MessageDigest
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions
import java.util.EnumSet

// --- Configuration (re-use from build-runner.main.kts or define locally) ---
val PROJECT_ROOT = File(System.getProperty("user.dir"))
val BUILD_CONFIG_PATH = PROJECT_ROOT.resolve("tools/build.conf")

val config = Properties()

fun loadConfig() {
    if (BUILD_CONFIG_PATH.exists()) {
        BUILD_CONFIG_PATH.inputStream().use { config.load(it) }
        log("Configuration loaded from: $BUILD_CONFIG_PATH")
    } else {
        error("Configuration file not found: $BUILD_CONFIG_PATH. Please ensure build.conf exists.")
    }
}

// --- Logging Functions (re-use) ---
fun log(message: String) = println("\u001B[0;34m[${java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"))}]\u001B[0m $message")
fun success(message: String) = println("\u001B[0;32m✅ $message\u001B[0m")
fun warning(message: String) = println("\u001B[1;33m⚠️  $message\u001B[0m")
fun error(message: String): Nothing {
    println("\u001B[0;31m❌ $message\u001B[0m")
    kotlin.system.exitProcess(1)
}
fun info(message: String) = println("\u001B[0;36mℹ️  $message\u001B[0m")

// --- Helper Functions (re-use runCommand, getGradlewCommand is not needed here) ---
fun runCommand(command: List<String>, workingDir: File = PROJECT_ROOT, verbose: Boolean = false): Boolean {
    val processBuilder = ProcessBuilder(command)
    processBuilder.directory(workingDir)
    processBuilder.redirectErrorStream(true) // Redirect stderr to stdout

    val process = processBuilder.start()
    val output = process.inputStream.bufferedReader().use { it.readText() }

    if (verbose) {
        println(output)
    } else {
        // Only print output if command failed or specific keywords are present
        if (process.exitValue() != 0 || output.contains("error", ignoreCase = true) || output.contains("fail", ignoreCase = true)) {
            println(output)
        }
    }

    val exited = process.waitFor(10, TimeUnit.MINUTES)
    if (!exited) {
        process.destroyForcibly()
        error("Command timed out: ${command.joinToString(" ")}")
    }

    return process.exitValue() == 0
}

// --- Core Deployment Functions ---

fun getJarInfo(module: String): File? {
    val version = config.getProperty("PROJECT_VERSION", "0.2.0-BETA")
    val jarPath = PROJECT_ROOT.resolve("$module/build/libs/$module-$version.jar")
    return if (jarPath.exists()) jarPath else null
}

fun getServerPluginPath(module: String): File {
    val version = config.getProperty("PROJECT_VERSION", "0.2.0-BETA")
    val serverPluginsDir = PROJECT_ROOT.resolve(config.getProperty("PLUGINS_RELATIVE_PATH", "minecraft-server/plugins"))
    return serverPluginsDir.resolve("$module-$version.jar")
}

fun calculateHash(file: File): String {
    if (!file.exists()) return "no_file"
    val digest = MessageDigest.getInstance("SHA-256")
    file.inputStream().use {
        val buffer = ByteArray(1024)
        var bytesRead: Int
        while (fis.read(buffer).also { bytesRead = it } != -1) {
            digest.update(buffer, 0, bytesRead)
        }
    }
    return digest.digest().joinToString("") { "%02x".format(it) }
}

fun backupExistingJar(targetJar: File, module: String): File? {
    if (!targetJar.exists()) return null

    val backupDir = PROJECT_ROOT.resolve(config.getProperty("BACKUP_DIR", "tools/backups"))
    backupDir.mkdirs()

    val timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
    val backupName = "${module}_${timestamp}.jar"
    val backupPath = backupDir.resolve(backupName)

    targetJar.copyTo(backupPath, overwrite = true)
    success("Backed up existing JAR: ${backupName}")

    cleanupOldBackups(backupDir, module)
    return backupPath
}

fun cleanupOldBackups(backupDir: File, module: String) {
    val retention = config.getProperty("BACKUP_RETENTION_COUNT", "5").toInt()

    backupDir.listFiles { file -> file.isFile && file.name.startsWith("${module}_") && file.name.endsWith(".jar") }
        ?.sortedByDescending { it.lastModified() }
        ?.drop(retention)
        ?.forEach { oldBackup ->
            oldBackup.delete()
            info("Deleted old backup: ${oldBackup.name}")
        }
}

fun deployJar(module: String, force: Boolean) {
    val sourceJar = getJarInfo(module) ?: run {
        error("JAR file not found for module: $module")
    }

    val targetJar = getServerPluginPath(module)
    targetJar.parentFile.mkdirs()

    if (!force) {
        val sourceHash = calculateHash(sourceJar)
        val targetHash = calculateHash(targetJar)

        if (sourceHash == targetHash) {
            info("Module $module JAR unchanged, skipping deployment.")
            return
        }
    }

    // Backup existing JAR (if enabled)
    if (config.getProperty("BACKUP_BEFORE_DEPLOY", "true").toBoolean()) {
        backupExistingJar(targetJar, module)
    }

    // Pre-deploy Hook
    val preDeployHook = config.getProperty("PRE_DEPLOY_HOOK", "")
    if (preDeployHook.isNotBlank() && File(preDeployHook).canExecute()) {
        log("Executing pre-deploy hook: $preDeployHook")
        runCommand(listOf(preDeployHook, module, sourceJar.absolutePath, targetJar.absolutePath), verbose = true)
    }

    // Copy JAR file
    sourceJar.copyTo(targetJar, overwrite = true)

    // Set file permissions (Note: PosixFilePermissions only works on Unix-like systems)
    if (!System.getProperty("os.name").toLowerCase().contains("win")) {
        try {
            val perms = EnumSet.of(
                PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE, PosixFilePermission.GROUP_READ, PosixFilePermission.OTHERS_READ
            )
            Files.setPosixFilePermissions(targetJar.toPath(), perms)
        } catch (e: UnsupportedOperationException) {
            warning("Cannot set POSIX file permissions on this OS.")
        } catch (e: Exception) {
            warning("Failed to set file permissions: ${e.message}")
        }
    }

    // Record deploy info
    val deployInfoFile = targetJar.parentFile.resolve(".$module.deploy-info")
    deployInfoFile.writeText("""
        DEPLOY_TIME=${java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}
        MODULE=$module
        VERSION=${config.getProperty("PROJECT_VERSION", "0.2.0-BETA")}
        SOURCE_PATH=${sourceJar.absolutePath}
        TARGET_PATH=${targetJar.absolutePath}
        SOURCE_HASH=${calculateHash(sourceJar)}
        DEPLOYED_BY=${System.getProperty("user.name")}
    """.trimIndent())

    success("Module $module JAR deployed successfully.")

    // Post-deploy Hook
    val postDeployHook = config.getProperty("POST_DEPLOY_HOOK", "")
    if (postDeployHook.isNotBlank() && File(postDeployHook).canExecute()) {
        log("Executing post-deploy hook: $postDeployHook")
        runCommand(listOf(postDeployHook, module, sourceJar.absolutePath, targetJar.absolutePath), verbose = true)
    }

    // Hot reload support
    val hotReloadModules = config.getProperty("HOT_RELOAD_MODULES", "").split(" ").filter { it.isNotBlank() }
    if (hotReloadModules.contains(module)) {
        attemptHotReload(module)
    }
}

fun attemptHotReload(module: String) {
    val delay = config.getProperty("HOT_RELOAD_DELAY", "3").toLong()
    info("Attempting hot reload for module: $module")
    Thread.sleep(delay * 1000) // Wait for server to detect changes

    // This part is highly OS-specific and depends on server setup.
    // For cross-platform compatibility, this will be a placeholder.
    // A more robust solution would involve a dedicated server management API or plugin.
    warning("Hot reload functionality is OS-specific and not fully implemented in a cross-platform way.")
    warning("Please restart your server manually if hot reload fails.")
    // sendServerCommand("zientis reload $module") // Placeholder for actual server command sending
}

// Placeholder for sending commands to server (highly OS-specific)
fun sendServerCommand(command: String): Boolean {
    val serverDir = PROJECT_ROOT.resolve(config.getProperty("MINECRAFT_SERVER_RELATIVE_PATH", "minecraft-server"))
    val serverInput = serverDir.resolve("server.input")

    if (serverInput.exists() && System.getProperty("os.name").toLowerCase().contains("linux")) { // Only attempt on Linux with pipe
        try {
            serverInput.appendText("$command\n")
            success("Sent command to server: $command")
            return true
        } catch (e: Exception) {
            warning("Failed to send command to server input pipe: ${e.message}")
        }
    }
    else {
        info("Server command sending is not supported on this OS or server input pipe not found.")
    }
    return false
}

fun rollbackJar(module: String, backupName: String = "latest") {
    val backupDir = PROJECT_ROOT.resolve(config.getProperty("BACKUP_DIR", "tools/backups"))
    val targetJar = getServerPluginPath(module)

    val backupFile: File = if (backupName == "latest") {
        backupDir.listFiles { file -> file.isFile && file.name.startsWith("${module}_") && file.name.endsWith(".jar") }
            ?.sortedByDescending { it.lastModified() }
            ?.firstOrNull() ?: error("No backups found for module: $module")
    } else {
        backupDir.resolve(backupName)
    }

    if (!backupFile.exists()) {
        error("Backup file not found: $backupFile")
    }

    // Backup current version before rolling back
    backupExistingJar(targetJar, "${module}_rollback")

    backupFile.copyTo(targetJar, overwrite = true)
    success("Module $module rolled back to: ${backupFile.name}")
}

fun listBackups(module: String = "all") {
    val backupDir = PROJECT_ROOT.resolve(config.getProperty("BACKUP_DIR", "tools/backups"))

    if (!backupDir.exists()) {
        warning("Backup directory does not exist: $backupDir")
        return
    }

    println("\u001B[0;36m📦 Available Backups:\u001B[0m")
    println("")

    val pattern = if (module == "all") "*.jar" else "${module}_*.jar"

    backupDir.listFiles { file -> file.isFile && file.name.matches(Regex(pattern.replace("*", ".*"))) }
        ?.sortedByDescending { it.lastModified() }
        ?.forEach {
            val basenameFile = it.name
            val size = "%.2f MB".format(it.length().toDouble() / (1024 * 1024))
            val date = java.time.Instant.ofEpochMilli(it.lastModified())
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDateTime()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

            println("  📄 \u001B[0;32m$basenameFile\u001B[0m ($size, $date)")
        }
    println("")
}

fun cleanupDeploy(target: String = "all") {
    val pluginsDir = PROJECT_ROOT.resolve(config.getProperty("PLUGINS_RELATIVE_PATH", "minecraft-server/plugins"))

    if (target == "all") {
        pluginsDir.listFiles { file -> file.isFile && (file.name.endsWith(".deploy-info") || file.name.endsWith(".deployed")) }
            ?.forEach { it.delete() }
        success("Cleaned up all deploy info files.")
    } else {
        pluginsDir.resolve(".$target.deploy-info").delete()
        pluginsDir.resolve(".$target.deployed").delete()
        success("Cleaned up deploy info files for module: $target")
    }
}

fun showDeployStatus(module: String = "all") {
    val pluginsDir = PROJECT_ROOT.resolve(config.getProperty("PLUGINS_RELATIVE_PATH", "minecraft-server/plugins"))
    val modules = config.getProperty("MODULES", "").split(" ").filter { it.isNotBlank() }

    println("\u001B[0;36m🚀 Deployment Status:\u001B[0m")
    println("")

    for (mod in modules) {
        if (module != "all" && mod != module) {
            continue
        }

        val jarPath = getServerPluginPath(mod)
        val deployInfoFile = pluginsDir.resolve(".$mod.deploy-info")

        println("\u001B[1;33mModule: $mod\u001B[0m")

        if (jarPath.exists()) {
            val size = "%.2f MB".format(jarPath.length().toDouble() / (1024 * 1024))
            val date = java.time.Instant.ofEpochMilli(jarPath.lastModified())
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDateTime()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            println("  ✅ JAR: Deployed ($size, $date)")

            if (deployInfoFile.exists()) {
                deployInfoFile.readLines().forEach {
                    val parts = it.split("=", limit = 2)
                    if (parts.size == 2) {
                        val key = parts[0]
                        val value = parts[1]
                        when (key) {
                            "DEPLOY_TIME" -> println("  📅 Deploy Time: $value")
                            "VERSION" -> println("  🏷️  Version: $value")
                            "DEPLOYED_BY" -> println("  👤 Deployed By: $value")
                        }
                    }
                }
            }
        } else {
            println("  ❌ JAR: Not Deployed")
        }
        println("")
    }
}

fun showHelp() {
    println("""
🚀 Zientis JAR Deployment Helper

Usage: kotlin -s ./deploy-runner.main.kts [command] [options]

Commands:
  deploy <module>              Deploy the JAR for the specified module
  deploy-all                   Deploy JARs for all modules
  rollback <module> [backup]   Rollback module to a specified backup
  list-backups [module]        List available backup files
  status [module]              Show deployment status
  cleanup [module]             Clean up deploy info files
  hot-reload <module>          Manually trigger hot reload (OS-specific)

Options:
  -f, --force                  Force deployment (ignore hash check)
  -h, --help                   Show this help message

Examples:
  kotlin -s ./deploy-runner.main.kts deploy core              # Deploy core module
  kotlin -s ./deploy-runner.main.kts deploy-all               # Deploy all modules
  kotlin -s ./deploy-runner.main.kts rollback core            # Rollback core module to latest backup
  kotlin -s ./deploy-runner.main.kts list-backups             # List all backups
  kotlin -s ./deploy-runner.main.kts status                   # Show deployment status for all modules
  kotlin -s ./deploy-runner.main.kts cleanup                  # Clean up all deploy info
    """.trimIndent())
}

// --- Main Function ---
fun main(args: Array<String>) {
    loadConfig()

    var command: String? = null
    var force = false
    var module: String? = null
    var backup: String? = null

    var i = 0
    while (i < args.size) {
        when (args[i]) {
            "-f", "--force" -> force = true
            "-h", "--help" -> {
                showHelp()
                kotlin.system.exitProcess(0)
            }
            "deploy", "deploy-all", "rollback", "list-backups", "status", "cleanup", "hot-reload" -> {
                command = args[i]
            }
            else -> {
                if (command == "deploy" || command == "rollback" || command == "list-backups" || command == "status" || command == "cleanup" || command == "hot-reload") {
                    if (module == null) {
                        module = args[i]
                    } else if (command == "rollback" && backup == null) {
                        backup = args[i]
                    } else {
                        error("Too many arguments for command: $command")
                    }
                } else {
                    error("Unknown command or argument: ${args[i]}")
                }
            }
        }
        i++
    }

    when (command) {
        "deploy" -> {
            if (module == null) error("Please specify a module to deploy.")
            deployJar(module!!, force)
        }
        "deploy-all" -> {
            val modules = config.getProperty("MODULES", "").split(" ").filter { it.isNotBlank() }
            for (mod in modules) {
                deployJar(mod, force)
            }
        }
        "rollback" -> {
            if (module == null) error("Please specify a module to rollback.")
            rollbackJar(module!!, backup ?: "latest")
        }
        "list-backups" -> listBackups(module ?: "all")
        "status" -> showDeployStatus(module ?: "all")
        "cleanup" -> cleanupDeploy(module ?: "all")
        "hot-reload" -> {
            if (module == null) error("Please specify a module to hot-reload.")
            attemptHotReload(module!!)
        }
        else -> showHelp()
    }
}

main(args)
