// tools/demo-runner.main.kts

import java.io.File
import java.util.concurrent.TimeUnit

// --- Logging Functions (re-use from other scripts) ---
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
val PROJECT_ROOT = File(System.getProperty("user.dir"))

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

fun pause() {
    println("${YELLOW}按 Enter 繼續...${NC}")
    readLine()
}

fun main() {
    println(PURPLE)
    println("""
╔══════════════════════════════════════════════════════════╗
║                                                          ║
║    🚀 Zientis 自動編譯工具套件 - 功能示範                 ║
║                                                          ║
╚══════════════════════════════════════════════════════════╝
    """.trimIndent())
    println(NC)

    println("${CYAN}📋 示範內容：${NC}")
    println("1. 查看工具幫助")
    println("2. 檢查部署狀態")
    println("3. 編譯核心模組")
    println("4. 部署模組")
    println("5. 備份管理")
    println("6. 快速編譯示範")
    println("")
    pause()

    println("${BLUE}====== 1. 查看自動編譯工具幫助 ======${NC}")
    runCommand(listOf("kotlin", "-s", "./tools/build-runner.main.kts", "--help"), verbose = true)
    pause()

    println("${BLUE}====== 2. 查看部署狀態 ======${NC}")
    runCommand(listOf("kotlin", "-s", "./tools/deploy-runner.main.kts", "status"), verbose = true)
    pause()

    println("${BLUE}====== 3. 乾跑模式編譯示範 ======${NC}")
    println("${CYAN}執行命令: kotlin -s ./tools/build-runner.main.kts --dry-run core${NC}")
    runCommand(listOf("kotlin", "-s", "./tools/build-runner.main.kts", "--dry-run", "core"), verbose = true)
    pause()

    println("${BLUE}====== 4. 實際編譯核心模組（快速模式）======${NC}")
    println("${CYAN}執行命令: kotlin -s ./tools/build-runner.main.kts -q core${NC}")
    runCommand(listOf("kotlin", "-s", "./tools/build-runner.main.kts", "-q", "zientis-core"), verbose = true)
    pause()

    println("${BLUE}====== 5. 檢查編譯後的JAR檔案 ======${NC}")
    runCommand(listOf("cmd", "/c", "dir", "zientis-core\build\libs\ "), verbose = true) // Use dir for Windows
    pause()

    println("${BLUE}====== 6. 部署JAR到伺服器 ======${NC}")
    println("${CYAN}執行命令: kotlin -s ./tools/deploy-runner.main.kts deploy zientis-core${NC}")
    runCommand(listOf("kotlin", "-s", "./tools/deploy-runner.main.kts", "deploy", "zientis-core"), verbose = true)
    pause()

    println("${BLUE}====== 7. 檢查部署後狀態 ======${NC}")
    runCommand(listOf("kotlin", "-s", "./tools/deploy-runner.main.kts", "status", "zientis-core"), verbose = true)
    pause()

    println("${BLUE}====== 8. 列出備份檔案 ======${NC}")
    runCommand(listOf("kotlin", "-s", "./tools/deploy-runner.main.kts", "list-backups"), verbose = true)
    pause()

    println("${BLUE}====== 9. 檢查伺服器插件目錄 ======${NC}")
    runCommand(listOf("cmd", "/c", "dir", "minecraft-server\plugins\zientis-*"), verbose = true) // Use dir for Windows
    pause()

    println(GREEN)
    println("""
╔══════════════════════════════════════════════════════════╗
║                                                          ║
║  🎉 示範完成！工具已成功：                                 ║
║                                                          ║
║  ✅ 智慧編譯檢測                                         ║
║  ✅ 自動JAR部署                                          ║ 
║  ✅ 版本管理                                            ║
║  ✅ 備份機制                                            ║
║  ✅ 狀態監控                                            ║
║                                                          ║
║  🚀 開發效率大幅提升！                                    ║
║                                                          ║
╚══════════════════════════════════════════════════════════╝
    """.trimIndent())
    println(NC)

    println("${CYAN}💡 下一步建議：${NC}")
    println("• 使用 kotlin -s ./tools/build-runner.main.kts -w 啟動監控模式進行開發")
    println("• 配置 build.conf 以符合你的需求")
    println("• 設定通知系統以獲得編譯結果提醒")
    println("")

    println("${BLUE}📚 更多資訊請查看：${NC}")
    println("• tools/README.md - 完整使用指南")
    println("• tools/build.conf - 配置選項說明")
    println("")
}

main()
