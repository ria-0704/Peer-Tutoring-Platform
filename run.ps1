# ============================================================
# Peer Tutoring Platform - Quick Run Script (Windows PowerShell)
# ============================================================
# 
# USAGE: Right-click this file > Run with PowerShell
#        OR in a terminal:  .\run.ps1
#
# PREREQUISITES:
#   1. JDK 21+ installed (java in PATH)
#   2. Maven installed  (mvn in PATH, OR set MAVEN_HOME below)
#   3. MySQL running with peer_tutoring database created
#   4. db.properties configured with your MySQL password
#
# If Maven is not in your PATH, set the path below:
# ============================================================

$MAVEN_PATH = "C:\maven\apache-maven-3.9.9\bin"

# Add Maven to PATH for this session
if (Test-Path $MAVEN_PATH) {
    $env:PATH = "$MAVEN_PATH;" + $env:PATH
    Write-Host "Using Maven from: $MAVEN_PATH" -ForegroundColor Cyan
} else {
    Write-Host "Maven not found at $MAVEN_PATH. Using system Maven." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=========================================" -ForegroundColor Magenta
Write-Host "  Peer Tutoring and Study Session Platform" -ForegroundColor Magenta
Write-Host "=========================================" -ForegroundColor Magenta
Write-Host ""

# Check Java
$javaVersion = java -version 2>&1 | Select-Object -First 1
Write-Host "Java: $javaVersion" -ForegroundColor Green

# Compile if needed
Write-Host ""
Write-Host "Compiling..." -ForegroundColor Cyan
mvn compile -q

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "Compilation failed! Please check the error messages above." -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host "Compilation successful!" -ForegroundColor Green
Write-Host ""
Write-Host "Starting application..." -ForegroundColor Cyan
Write-Host ""

# Run the app
mvn javafx:run

Read-Host "Press Enter to exit"
