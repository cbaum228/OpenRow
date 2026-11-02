param([string]$AdbPath='adb',[string]$Serial)
$ErrorActionPreference='Stop'
$apk=Join-Path $PSScriptRoot 'OpenRow-0.22.0-alpha.1.apk'
$hashes=Get-Content -LiteralPath (Join-Path $PSScriptRoot 'SHA256SUMS.txt')
$entry=@($hashes | Where-Object {$_ -match '^[a-fA-F0-9]{64}  OpenRow-0\.22\.0-alpha\.1\.apk$'})
if($entry.Count -ne 1 -or (Get-FileHash -LiteralPath $apk -Algorithm SHA256).Hash -ne ($entry[0] -split '  ',2)[0]){throw 'APK checksum failed. Download the complete kit again.'}
& $AdbPath start-server
if($LASTEXITCODE -ne 0){throw 'ADB unavailable. Download Google Platform Tools and pass -AdbPath.'}
$devices=@(& $AdbPath devices | Where-Object {$_ -match '^\S+\s+device$'} | ForEach-Object {($_ -split '\s+')[0]})
if($Serial){if($devices -notcontains $Serial){throw 'Requested tablet is not connected/authorized.'}}elseif($devices.Count -eq 1){$Serial=$devices[0]}else{throw 'Connect and authorize exactly one tablet, or pass -Serial. Check adb devices.'}
$sdk=& $AdbPath -s $Serial shell getprop ro.build.version.sdk
if($LASTEXITCODE -ne 0 -or [int]$sdk -lt 28){throw 'This alpha requires Android 9 / API 28 or newer.'}
& $AdbPath -s $Serial install -r $apk
if($LASTEXITCODE -ne 0){throw 'Installation failed. Keep existing data; do not uninstall. Send the error to the owner.'}
& $AdbPath -s $Serial shell am start -n org.openrow.probe/.MainActivity
if($LASTEXITCODE -ne 0){throw 'Installed, but launch failed. Open OpenRow from the app drawer.'}
Write-Output 'Installed. Complete profile setup, connect the S4 and follow START-HERE.md.'
