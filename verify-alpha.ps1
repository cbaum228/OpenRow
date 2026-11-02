param([string]$Apk='distribution/OpenRow-0.22.0-alpha.1.apk')
$ErrorActionPreference='Stop'
Push-Location $PSScriptRoot
try{
    $policy=Get-Content -Raw release-policy.json | ConvertFrom-Json
    $jdk=if(Test-Path 'tools/jdk'){(Get-ChildItem tools/jdk -Directory | Select-Object -First 1).FullName}else{$env:JAVA_HOME}
    $compiler=if(Test-Path 'tools/build-tools/android-15'){'tools/build-tools/android-15'}else{Join-Path $env:ANDROID_HOME 'build-tools/35.0.0'}
    $signature=& "$jdk/bin/java.exe" -jar "$compiler/lib/apksigner.jar" verify --min-sdk-version 28 --verbose --print-certs $Apk
    if($LASTEXITCODE -ne 0 -or ($signature -join "`n") -notmatch [regex]::Escape($policy.certificateSha256)){throw 'APK signature or pinned certificate failed'}
    $badging=& "$compiler/aapt.exe" dump badging $Apk
    if($LASTEXITCODE -ne 0){throw 'APK metadata unreadable'}
    $identity="package: name='$($policy.package)' versionCode='$($policy.versionCode)' versionName='$($policy.version)'"
    if(-not $badging[0].StartsWith($identity) -or $badging -notcontains "sdkVersion:'28'"){throw 'Unexpected package, version or minimum Android version'}
    $manifest=& "$compiler/aapt.exe" dump xmltree $Apk AndroidManifest.xml
    if($LASTEXITCODE -ne 0){throw 'APK manifest unreadable'}
    $text=$manifest -join "`n"
    if($text -notmatch 'android:debuggable[^\r\n]*0x0\s' -or $text -match 'GravityWellPreviewActivity|uses-permission|E: instrumentation'){throw 'Release contains debugging, preview entry point or unexpected permissions'}
    $hash=(Get-FileHash -LiteralPath $Apk -Algorithm SHA256).Hash.ToLowerInvariant()
    $entry="$hash  $([IO.Path]::GetFileName($Apk))"
    if(Test-Path distribution/SHA256SUMS.txt){if((Get-Content distribution/SHA256SUMS.txt) -notcontains $entry){throw 'Published APK checksum mismatch'}}
    Write-Output "PASS: signed non-debug alpha, pinned certificate, version/minSdk, no preview or permissions; SHA-256 $hash"
}finally{Pop-Location}
