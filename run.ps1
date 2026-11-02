$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot
New-Item -ItemType Directory -Force -Path 'out' | Out-Null
$files = @(Get-ChildItem -Path 'src/main/java' -Filter '*.java' -Recurse | ForEach-Object { $_.FullName })
& javac -encoding UTF-8 -d out @files
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
& java -cp out chess.Main
