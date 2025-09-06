// tools/build-runner.main.kts

import java.io.File
import java.util.Properties
import java.util.concurrent.TimeUnit

// --- Configuration ---
val PROJECT_ROOT = File(System.getProperty("user.dir")) // Assuming script runs from project root
val BUILD_CONFIG_PATH = PROJECT_ROOT.resolve("tools/build.conf")

val config = Properties()

fun loadConfig() {
    if (BUILD_CONFIG_PATH.exists()) {
        BUILD_CONFIG_PATH.inputStream().use { config.load(it) }
        log("Configuration loaded from: $BUILD_CONFIG_PATH")
    } else {
        warning("Configuration file not found, creating default.")
        createDefaultConfig()
        BUILD_CONFIG_PATH.inputStream().use { config.load(it) } // Load after creation
    }
}

fun createDefaultConfig() {
    val defaultConfigContent = """
        # Zientis Auto Build Tool Configuration

        # Module list (space-separated)
        MODULES="zientis-core zientis-economy zientis-multiworld zientis-social zientis-nations zientis-display zientis-discord-api"

        # Core modules (base modules that other modules depend on)
        CORE_MODULES="zientis-core"

        # Whether to automatically copy to server
        AUTO_COPY_TO_SERVER=true

        # Whether to automatically restart server after copying
        AUTO_RESTART_SERVER=false

        # Server restart delay (seconds)
        SERVER_RESTART_DELAY=5

        # Whether to clean before build
        CLEAN_BEFORE_BUILD=false

        # Whether to build only changed modules
        BUILD_CHANGED_ONLY=true

        # File change check interval (seconds)
        WATCH_INTERVAL=2

        # Excluded test modules
        SKIP_TESTS_MODULES="zientis-display"
    """.trimIndent()
    BUILD_CONFIG_PATH.writeText(defaultConfigContent)
    success("Default configuration file created: $BUILD_CONFIG_PATH")
}

// --- Logging Functions ---
fun log(message: String) = println("\u001B[0;34m[${java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"))}]\u001B[0m $message")
fun success(message: String) = println("\u001B[0;32m✅ $message\u001B[0m")
fun warning(message: String) = println("\u001B[1;33m⚠️  $message\u001B[0m")
fun error(message: String): Nothing {
    println("\u001B[0;31m❌ $message\u001B[0m")
    kotlin.system.exitProcess(1)
}
fun info(message: String) = println("\u001B[0;36mℹ️  $message\u001B[0m")

// --- Helper Functions ---
fun runCommand(command: List<String>, workingDir: File = PROJECT_ROOT, verbose: Boolean = false): Boolean {
    val processBuilder = ProcessBuilder(command)
    processBuilder.directory(workingDir)
    processBuilder.redirectErrorStream(true) // Redirect stderr to stdout

    val process = processBuilder.start()
    val output = process.inputStream.bufferedReader().use { it.readText() }

    if (verbose) {
        println(output)
    } else {
        output.lines().forEach { line ->
            if (line.contains("BUILD SUCCESSFUL") || line.contains("BUILD FAILED")) {
                println(line) // Print only relevant lines if not verbose
            }
        }
    }

    val exited = process.waitFor(10, TimeUnit.MINUTES) // Wait for process to finish
    if (!exited) {
        process.destroyForcibly()
        error("Command timed out: ${command.joinToString(" ")}")
    }

    return process.exitValue() == 0
}

fun getGradlewCommand(): String {
    return if (System.getProperty("os.name").toLowerCase().contains("win")) {
        "gradlew.bat"
    } else {
        "gradlew"
    }
}

// --- Core Logic Functions ---

fun checkModuleExists(module: String) {
    if (!PROJECT_ROOT.resolve(module).isDirectory) {
        error("Module does not exist: $module")
    }
}

fun detectChanges(module: String): Boolean {
    val moduleDir = PROJECT_ROOT.resolve(module)
    val version = config.getProperty("VERSION", "0.2.0-BETA") // Get version from config
    val jarFile = moduleDir.resolve("build/libs/$module-$version.jar")

    if (!jarFile.exists()) {
        return true // JAR doesn't exist, needs compilation
    }

    // Check if any source file is newer than the JAR
    val sourceDir = moduleDir.resolve("src/main/java") // Assuming Java source
    if (!sourceDir.exists()) return false // No source to check

    val lastModifiedJar = jarFile.lastModified()

    sourceDir.walkTopDown().forEach { file ->
        if (file.isFile && file.extension == "java" && file.lastModified() > lastModifiedJar) {
            return true // Found a newer source file
        }
    }
    return false // No newer source files
}

fun buildModule(module: String, quick: Boolean, clean: Boolean, verbose: Boolean): Boolean {
    log("Starting build for module: $module")

    val gradlewCmd = getGradlewCommand()
    val args = mutableListOf(gradlewCmd)

    if (clean) {
        args.add("clean")
    }
    args.add(":$module:build")

    if (quick) {
        args.add("-x")
        args.add("test")
    }

    val startTime = System.currentTimeMillis()
    val success = runCommand(args, verbose = verbose)
    val endTime = System.currentTimeMillis()
    val duration = (endTime - startTime) / 1000.0

    if (success) {
        success("Module $module compiled successfully (${"%.2f".format(duration)}s)")
    } else {
        error("Module $module compilation failed.")
    }
    return success
}

fun copyToServer(module: String) {
    val version = config.getProperty("VERSION", "0.2.0-BETA")
    val jarFile = PROJECT_ROOT.resolve("$module/build/libs/$module-$version.jar")
    val pluginsDir = PROJECT_ROOT.resolve(config.getProperty("PLUGINS_RELATIVE_PATH", "minecraft-server/plugins"))

    if (!jarFile.exists()) {
        warning("JAR file not found: $jarFile")
        return
    }

    pluginsDir.mkdirs() // Ensure plugins directory exists
    jarFile.copyTo(pluginsDir.resolve(jarFile.name), overwrite = true)
    success("Copied $module JAR to server.")
}

fun restartServer() {
    val serverScriptPath = PROJECT_ROOT.resolve(config.getProperty("MINECRAFT_SERVER_RELATIVE_PATH", "minecraft-server")).resolve("restart-server.sh")

    if (serverScriptPath.exists()) {
        log("Restarting server...")
        // Note: This assumes restart-server.sh is executable and works on Windows (e.g., via WSL or Git Bash)
        // For a truly cross-platform solution, this would need to be a platform-specific command or a Kotlin implementation.
        runCommand(listOf(serverScriptPath.absolutePath), workingDir = serverScriptPath.parentFile, verbose = true)
    } else {
        warning("Server restart script not found: $serverScriptPath")
        info("Please restart the server manually to load new JAR files.")
    }
}

fun watchMode(modulesToWatch: List<String>, quick: Boolean, clean: Boolean, verbose: Boolean) {
    log("Entering watch mode - Press Ctrl+C to exit")
    val watchInterval = config.getProperty("WATCH_INTERVAL", "2").toLong() * 1000 // Convert to milliseconds

    while (true) {
        val changedModules = mutableListOf<String>()
        for (module in modulesToWatch) {
            if (detectChanges(module)) {
                changedModules.add(module)
            }
        }

        if (changedModules.isNotEmpty()) {
            log("Detected changes in modules: ${changedModules.joinToString(", ")}")
            buildModules(changedModules, quick, clean, verbose, skipCopy = false, restart = false) // Build and copy in watch mode
        }

        Thread.sleep(watchInterval)
    }
}

fun buildModules(
    modules: List<String>,
    quick: Boolean,
    clean: Boolean,
    verbose: Boolean,
    skipCopy: Boolean,
    restart: Boolean
) {
    val autoCopyToServer = config.getProperty("AUTO_COPY_TO_SERVER", "true").toBoolean()
    val autoRestartServer = config.getProperty("AUTO_RESTART_SERVER", "false").toBoolean()
    val serverRestartDelay = config.getProperty("SERVER_RESTART_DELAY", "5").toLong()

    val failedModules = mutableListOf<String>()

    log("Preparing to build ${modules.size} modules: ${modules.joinToString(", ")}")

    for (module in modules) {
        checkModuleExists(module)

        val buildChangedOnly = config.getProperty("BUILD_CHANGED_ONLY", "true").toBoolean()
        val forceBuild = config.getProperty("FORCE", "false").toBoolean() // Assuming FORCE can be set via args or config

        if (buildChangedOnly && !forceBuild) {
            if (!detectChanges(module)) {
                info("Skipping module $module (no changes detected)")
                continue
            }
        }

        if (!buildModule(module, quick, clean, verbose)) {
            failedModules.add(module)
        } else {
            if (autoCopyToServer && !skipCopy) {
                copyToServer(module)
            }
        }
    }

    if (failedModules.isEmpty()) {
        success("All modules compiled successfully!")
        if ((autoRestartServer || restart) && !skipCopy) { // Only restart if copy happened
            Thread.sleep(serverRestartDelay * 1000)
            restartServer()
        }
    } else {
        error("The following modules failed to compile: ${failedModules.joinToString(", ")}")
    }
}

// --- Main Function ---
fun main(args: Array<String>) {
    loadConfig() // Load config first

    var targetModules = mutableListOf<String>()
    var cleanBeforeBuild = config.getProperty("CLEAN_BEFORE_BUILD", "false").toBoolean()
    var watchModeEnabled = false
    var skipCopy = false
    var restartAfterBuild = false
    var runTests = false // Not fully implemented yet, but keep for args parsing
    var forceBuild = false
    var quickMode = false
    var verboseMode = false
    var dryRun = false

    val allModules = config.getProperty("MODULES", "").split(" ").filter { it.isNotBlank() }

    var i = 0
    while (i < args.size) {
        when (args[i]) {
            "-h", "--help" -> {
                // showHelp() // Need to implement showHelp
                println("Help message will be displayed here.")
                kotlin.system.exitProcess(0)
            }
            "-a", "--all" -> {
                targetModules.addAll(allModules)
            }
            "-c", "--clean" -> cleanBeforeBuild = true
            "-w", "--watch" -> watchModeEnabled = true
            "-s", "--skip-copy" -> skipCopy = true
            "-r", "--restart" -> restartAfterBuild = true
            "-t", "--test" -> runTests = true
            "-f", "--force" -> forceBuild = true
            "-q", "--quick" -> quickMode = true
            "-v", "--verbose" -> verboseMode = true
            "--dry-run" -> dryRun = true
            "core" -> targetModules.add("zientis-core")
            "economy" -> targetModules.add("zientis-economy")
            "multiworld" -> targetModules.add("zientis-multiworld")
            "social" -> targetModules.add("zientis-social")
            "nations" -> targetModules.add("zientis-nations")
            "display" -> targetModules.add("zientis-display")
            "discord-api" -> targetModules.add("zientis-discord-api")
            else -> {
                if (args[i].startsWith("-")) {
                    error("Unknown option: ${args[i]}")
                } else {
                    targetModules.add(args[i]) // Assume it's a module name
                }
            }
        }
        i++
    }

    // If no specific modules are targeted, use all modules
    if (targetModules.isEmpty()) {
        targetModules.addAll(allModules)
    }

    // Display banner (can be a function)
    println("\u001B[0;35m")
    println("""
 _______ _______ _______ _______ _______ _______ _______
|    ___|_     _|    ___|    |  |_     _|_     _|    ___|
|    ___| |   | |    ___|       | |   | | |   | |    ___|
|_______| |___| |_______|__|____| |___| |_|___|_|_______|
                                                        
    🚀 Zientis Auto Build Tool v1.0                        
    """.trimIndent())
    println("\u001B[0m")

    if (watchModeEnabled) {
        watchMode(targetModules, quickMode, cleanBeforeBuild, verboseMode)
    } else {
        buildModules(targetModules, quickMode, cleanBeforeBuild, verboseMode, skipCopy, restartAfterBuild)
    }
}

// Call main function with command line arguments
main(args)