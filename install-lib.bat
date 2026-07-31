@echo off
setlocal
cd /d "%~dp0"

set "ARGS="
if /I "%~1"=="--force" set "ARGS=-Force"
if /I "%~1"=="-f" set "ARGS=-Force"
if /I "%~1"=="-Force" set "ARGS=-Force"

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0install-lib.ps1" %ARGS%
exit /b %ERRORLEVEL%
