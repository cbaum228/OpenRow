$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    $jdk = (Get-ChildItem -LiteralPath 'tools/jdk' -Directory | Select-Object -First 1).FullName
    $android = Join-Path $PSScriptRoot 'tools/platform/android-35/android.jar'
    $compiler = Join-Path $PSScriptRoot 'tools/build-tools/android-15'
    New-Item -ItemType Directory -Force build/telemetry-tests/classes,build/telemetry-tests/dex | Out-Null
    function Run([string]$Exe,[string[]]$Arguments){ & $Exe @Arguments; if($LASTEXITCODE -ne 0){throw "$Exe failed"} }
    Run "$jdk/bin/javac.exe" @('-encoding','UTF-8','-source','8','-target','8','-cp',"$android;build/classes",'-d','build/telemetry-tests/classes','tests/TelemetryInstrumentation.java','tests/GraphicsEvidenceInstrumentation.java','tests/WorkoutHandoffInstrumentation.java')
    Run "$jdk/bin/jar.exe" @("-J-Djava.io.tmpdir=$PSScriptRoot/build",'cf','build/telemetry-tests/classes.jar','-C','build/telemetry-tests/classes','.')
    Run "$jdk/bin/java.exe" @('-cp',"$compiler/lib/d8.jar",'com.android.tools.r8.D8','--min-api','28','--lib',$android,'--classpath','build/classes.jar','--output','build/telemetry-tests/dex','build/telemetry-tests/classes.jar')
    Copy-Item -LiteralPath tests/AndroidTelemetryManifest.xml -Destination build/telemetry-tests/AndroidManifest.xml
    Run "$compiler/aapt.exe" @('package','-f','-M','build/telemetry-tests/AndroidManifest.xml','-I',$android,'-F','build/telemetry-tests/unaligned.apk')
    Run "$jdk/bin/jar.exe" @("-J-Djava.io.tmpdir=$PSScriptRoot/build",'uf','build/telemetry-tests/unaligned.apk','-C','build/telemetry-tests/dex','classes.dex')
    Run "$compiler/zipalign.exe" @('-f','4','build/telemetry-tests/unaligned.apk','build/telemetry-tests/aligned.apk')
    Run "$jdk/bin/java.exe" @('-jar',"$compiler/lib/apksigner.jar",'sign','--ks','build/debug.keystore','--ks-pass','pass:android','--key-pass','pass:android','--out','build/telemetry-tests/tests.apk','build/telemetry-tests/aligned.apk')
} finally { Pop-Location }
