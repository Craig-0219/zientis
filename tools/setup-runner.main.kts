// tools/setup-runner.main.kts

import java.io.File
import java.util.concurrent.TimeUnit
import java.util.Properties

// --- Configuration (re-use from build-runner.main.kts or define locally) ---
val PROJECT_ROOT = File(System.getProperty("user.dir"))
val BUILD_CONFIG_PATH = PROJECT_ROOT.resolve("tools/build.conf")

val config = Properties()

fun loadConfig() {
    if (BUILD_CONFIG_PATH.exists()) {
        BUILD_CONFIG_PATH.inputStream().use { config.load(it) }
    }
    // No error if build.conf not found, setup script might create it or it's optional
}

// --- Logging Functions (re-use) ---
val RED = "\u001B[0;31m"
val GREEN = "\u001B[0;32m"
val YELLOW = "\u001B[1;33m"
val BLUE = "\u001B[0;34m"
val PURPLE = "\u001B[0;35m"
val CYAN = "\u001B[0;36m"
val NC = "\u001B[0m" // No Color

fun log(message: String) = println("${BLUE}[${java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"))}]${NC} $message")
fun success(message: String) = println("${GREEN}✅ $message${NC}")
fun warning(message: String) = println("${YELLOW}⚠️  $message${NC}")
fun error(message: String): Nothing {
    println("${RED}❌ $message${NC}")
    kotlin.system.exitProcess(1)
}
fun info(message: String) = println("${CYAN}ℹ️  $message${NC}")

// --- Helper Function to run shell commands ---
fun runCommand(command: List<String>, workingDir: File = PROJECT_ROOT, verbose: Boolean = false): Pair<Boolean, String> {
    val processBuilder = ProcessBuilder(command)
    processBuilder.directory(workingDir)
    processBuilder.redirectErrorStream(true) // Redirect stderr to stdout

    val process = processBuilder.start()
    val output = process.inputStream.bufferedReader().use { it.readText() }

    if (verbose) {
        println(output)
    }

    val exited = process.waitFor(10, TimeUnit.MINUTES)
    if (!exited) {
        process.destroyForcibly()
        return Pair(false, "Command timed out: ${command.joinToString(" ")}")
    }

    return Pair(process.exitValue() == 0, output)
}

fun checkCommand(cmd: String, packageName: String = ""): Boolean {
    val (success, output) = runCommand(if (System.getProperty("os.name").toLowerCase().contains("win")) listOf("where", cmd) else listOf("which", cmd))
    if (success) {
        success("$cmd 已安裝")
        return true
    }
    else {
        warning("$cmd 未安裝")
        if (packageName.isNotBlank()) {
            info("請手動安裝 $cmd (建議套件: $packageName)")
        }
        return false
    }
}

fun main() {
    loadConfig() // Load config for PROJECT_VERSION etc.

    println(PURPLE)
    println("""
╔══════════════════════════════════════════════════════════╗
║                                                          ║
║        🛠️ Zientis 自動編譯工具 - 快速設定                 ║
║                                                          ║
╚══════════════════════════════════════════════════════════╝
    """.trimIndent())
    println(NC)

    log("檢查系統依賴...")

    val requiredDeps = listOf(
        Pair("java", "openjdk-21-jdk"),
        Pair("gradle", "gradle"),
        Pair("git", "git"),
        Pair("kotlin", "kotlin"), // Add kotlin as a required dependency
        Pair("rsync", "rsync"), // rsync is Unix-specific, will warn on Windows
        Pair("sha256sum", "coreutils") // sha256sum is Unix-specific, will warn on Windows
    )

    val missingDeps = mutableListOf<String>()

    for ((cmd, pkg) in requiredDeps) {
        if (!checkCommand(cmd, pkg)) {
            missingDeps.add(cmd)
        }
    }

    if (missingDeps.isNotEmpty()) {
        error("缺少必要依賴，請安裝: ${missingDeps.joinToString(", ")}")
    }

    // Check Java version
    val (javaVersionSuccess, javaVersionOutput) = runCommand(listOf("java", "-version"))
    if (javaVersionSuccess) {
        val javaVersion = javaVersionOutput.lines().firstOrNull { it.contains("version") }
            ?.let { Regex("version \"([0-9]+)\\.").find(it)?.groups?.get(1)?.value?.toIntOrNull() }
        if (javaVersion != null && javaVersion < 21) {
            warning("建議使用 Java 21 或更高版本，當前版本: $javaVersion")
        } else if (javaVersion == null) {
            warning("無法解析 Java 版本。")
        }
    } else {
        warning("無法執行 Java 版本檢查。")
    }

    // Create necessary directories
    log("創建必要目錄...")

    val dirs = listOf(
        PROJECT_ROOT.resolve("tools/logs"),
        PROJECT_ROOT.resolve("tools/backups"),
        PROJECT_ROOT.resolve("minecraft-server/plugins")
    )

    for (dir in dirs) {
        if (!dir.exists()) {
            dir.mkdirs()
            success("創建目錄: ${dir.absolutePath}")
        } else {
            info("目錄已存在: ${dir.absolutePath}")
        }
    }

    // Setting tool permissions (removed for Windows compatibility)
    // log("設定工具執行權限...")
    // TOOLS = listOf(...) 
    // for (tool in TOOLS) { ... }

    // Check config file
    log("檢查配置檔案...")
    if (BUILD_CONFIG_PATH.exists()) {
        success("配置檔案已存在: build.conf")
    } else {
        warning("配置檔案不存在，將在首次執行時自動創建")
    }

    // Test Gradle
    log("測試 Gradle 配置...")
    val (gradleTestSuccess, _) = runCommand(listOf(if (System.getProperty("os.name").toLowerCase().contains("win")) "gradlew.bat" else "gradlew", "tasks"), workingDir = PROJECT_ROOT)
    if (gradleTestSuccess) {
        success("Gradle 配置正常")
    } else {
        warning("Gradle 配置可能有問題，請檢查")
    }

    // Check existing JAR files
    log("檢查現有 JAR 檔案...")
    var jarCount = 0
    PROJECT_ROOT.listFiles { file -> file.isDirectory && file.name.startsWith("zientis-") }?.forEach { moduleDir ->
        val libsDir = moduleDir.resolve("build/libs")
        if (libsDir.exists() && libsDir.isDirectory) {
            val jarFiles = libsDir.listFiles { file -> file.isFile && file.extension == "jar" }?.size ?: 0
            if (jarFiles > 0) {
                jarCount += jarFiles
                success("找到 $jarFiles 個 JAR 檔案在 ${moduleDir.name}")
            }
        }
    }

    if (jarCount == 0) {
        info("未找到現有 JAR 檔案，建議先執行編譯")
    } else {
        success("總共找到 $jarCount 個 JAR 檔案")
    }

    // Create shortcut script (run.bat)
    log("創建快捷方式...")
    val runBatPath = PROJECT_ROOT.resolve("run.bat")
    if (!runBatPath.exists()) {
        runBatPath.writeText("@call tools\\build.bat %*\n")
        success("創建快捷方式: run.bat")
    } else {
        info("快捷方式已存在: run.bat")
    }

    // Generate usage suggestions
    println("")
    println("${CYAN}🎯 設定完成！建議的下一步操作：${NC}")
    println("")

    println("${YELLOW}1. 編譯所有模組：${NC}")
    println("   run.bat -a")
    println("")

    println("${YELLOW}2. 啟動監控模式（開發推薦）：${NC}")
    println("   run.bat -w")
    println("")

    println("${YELLOW}3. 快速編譯核心模組：${NC}")
    println("   run.bat -q core")
    println("")

    println("${YELLOW}4. 部署模組到伺服器：${NC}")
    println("   run.bat deploy-all")
    println("")

    println("${YELLOW}5. 查看工具功能示範：${NC}")
    println("   run.bat demo") // Assuming a unified launcher will handle "demo" command
    println("")

    println("${YELLOW}6. 查看詳細說明：${NC}")
    println("   cat tools/README.md")
    println("")

    // Ask to run demo
    print("${CYAN}是否要執行功能示範？(y/N): ${NC}")
    val response = readLine()
    if (response?.toLowerCase() == "y") {
        println("")
        log("啟動功能示範...")
        runCommand(listOf("kotlin", "-s", "./tools/demo-runner.main.kts"), verbose = true)
    }

    println("")
    success("設定完成！開始享受高效的開發體驗吧！")
    println("")
}

main()
