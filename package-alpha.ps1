param([switch]$SkipBuild)
$ErrorActionPreference='Stop'
Push-Location $PSScriptRoot
try{
    if(-not $SkipBuild){& ./build.ps1 -Release;if($LASTEXITCODE -ne 0){throw 'Release build failed'}}
    $version=(Get-Content -Raw release-policy.json | ConvertFrom-Json).version
    $name="OpenRow-$version.apk";$kit=Join-Path $PSScriptRoot 'build/alpha-kit'
    New-Item -ItemType Directory -Force distribution,$kit | Out-Null
    Copy-Item -LiteralPath build/OpenRow-Alpha.apk -Destination "distribution/$name"
    $apkHash=(Get-FileHash "distribution/$name" -Algorithm SHA256).Hash.ToLowerInvariant()
    "$apkHash  $name" | Set-Content -Encoding ascii distribution/SHA256SUMS.txt
    & ./verify-alpha.ps1 -Apk "distribution/$name"
    Copy-Item -LiteralPath "distribution/$name" -Destination $kit
    Copy-Item -LiteralPath docs/ALPHA-INSTALL.md -Destination "$kit/START-HERE.md"
    foreach($doc in @('ALPHA-TEST-CHECKLIST.md','PRIVACY.md','PVV-ALPHA-1.md')){Copy-Item -LiteralPath "docs/$doc" -Destination $kit}
    Copy-Item -LiteralPath Install-Windows.ps1 -Destination $kit
    Get-ChildItem -LiteralPath $kit -File | Where-Object Name -ne SHA256SUMS.txt | Sort-Object Name | ForEach-Object {"$((Get-FileHash $_.FullName -Algorithm SHA256).Hash.ToLowerInvariant())  $($_.Name)"} | Set-Content -Encoding ascii "$kit/SHA256SUMS.txt"
    Compress-Archive -Path "$kit/*" -DestinationPath "build/OpenRow-$version-install-kit.zip" -Force
    Write-Output "Kit: build/OpenRow-$version-install-kit.zip"
}finally{Pop-Location}
