@echo off
cd /d "%~dp0"
kotlin -s build-runner.main.kts %*