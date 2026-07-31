# Install all jars under .\lib into local Maven repo.
# GAV is read from each jar's META-INF/maven/**/pom.properties.
# Default: skip if already present. Force: .\install-lib.ps1 -Force

param(
  [switch]$Force
)

$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.IO.Compression.FileSystem

Set-Location -Path $PSScriptRoot

$libDir = Join-Path $PSScriptRoot "lib"
if (-not (Test-Path $libDir)) {
  Write-Error "lib directory not found: $libDir"
}

if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
  Write-Error "mvn not found in PATH"
}

if ($env:MAVEN_REPO) {
  $localRepo = $env:MAVEN_REPO
} else {
  $localRepo = (& mvn -q help:evaluate "-Dexpression=settings.localRepository" -DforceStdout 2>$null)
  if (-not $localRepo) {
    $localRepo = Join-Path $env:USERPROFILE ".m2\repository"
  }
}

Write-Host "Local Maven repo: $localRepo"
if ($Force) {
  Write-Host "Mode: FORCE overwrite"
} else {
  Write-Host "Mode: skip if already installed"
}
Write-Host ""

function Read-GavFromJar([string]$jarPath) {
  $zip = [System.IO.Compression.ZipFile]::OpenRead($jarPath)
  try {
    $propEntry = $zip.Entries |
      Where-Object { $_.FullName -match '^META-INF/maven/.+/pom\.properties$' } |
      Select-Object -First 1
    if (-not $propEntry) {
      return $null
    }
    $reader = New-Object System.IO.StreamReader($propEntry.Open())
    try {
      $text = $reader.ReadToEnd()
    } finally {
      $reader.Close()
    }

    # maven plugins must be installed as packaging=maven-plugin, not jar
    $packaging = "jar"
    $pomEntry = $zip.Entries |
      Where-Object { $_.FullName -match '^META-INF/maven/.+/pom\.xml$' } |
      Select-Object -First 1
    if ($pomEntry) {
      $pomReader = New-Object System.IO.StreamReader($pomEntry.Open())
      try {
        $pomText = $pomReader.ReadToEnd()
      } finally {
        $pomReader.Close()
      }
      if ($pomText -match '<packaging>\s*([^<]+)\s*</packaging>') {
        $packaging = $Matches[1].Trim()
      }
    }
  } finally {
    $zip.Dispose()
  }

  $gav = @{
    groupId = $null
    artifactId = $null
    version = $null
    packaging = $packaging
  }
  foreach ($line in ($text -split "`r?`n")) {
    if ($line -match '^\s*#' -or $line.Trim() -eq "") { continue }
    if ($line -match '^(groupId|artifactId|version)=(.*)$') {
      $gav[$Matches[1]] = $Matches[2].Trim()
    }
  }
  if (-not $gav.groupId -or -not $gav.artifactId -or -not $gav.version) {
    return $null
  }
  return $gav
}

$count = 0
$installed = 0
$skipped = 0
$failed = 0

Get-ChildItem -Path $libDir -Filter *.jar | ForEach-Object {
  $count++
  $src = $_.FullName
  $gav = Read-GavFromJar $src
  if (-not $gav) {
    Write-Host "[MISS] no/invalid pom.properties in $($_.Name)"
    $script:failed++
    return
  }

  $gidPath = $gav.groupId.Replace('.', '\')
  $dest = Join-Path $localRepo "$gidPath\$($gav.artifactId)\$($gav.version)\$($gav.artifactId)-$($gav.version).jar"

  if ((Test-Path $dest) -and -not $Force) {
    Write-Host "[SKIP] $($gav.groupId):$($gav.artifactId):$($gav.version)"
    $script:skipped++
    return
  }

  Write-Host "[INSTALL] $($gav.groupId):$($gav.artifactId):$($gav.version) ($($gav.packaging))  <- $($_.Name)"
  & mvn -q install:install-file `
    "-Dfile=$src" `
    "-DgroupId=$($gav.groupId)" `
    "-DartifactId=$($gav.artifactId)" `
    "-Dversion=$($gav.version)" `
    "-Dpackaging=$($gav.packaging)"
  if ($LASTEXITCODE -ne 0) {
    Write-Host "[FAIL] $($gav.groupId):$($gav.artifactId):$($gav.version)"
    $script:failed++
    return
  }
  $script:installed++
}

Write-Host ""
Write-Host "Done. jars=$count installed=$installed skipped=$skipped failed=$failed"
if ($failed -gt 0) { exit 1 }
