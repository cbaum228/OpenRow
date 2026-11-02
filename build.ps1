param([switch]$Release)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
Push-Location $PSScriptRoot
try {
    $jdk = if(Test-Path 'tools/jdk'){(Get-ChildItem -LiteralPath 'tools/jdk' -Directory | Select-Object -First 1).FullName}else{$env:JAVA_HOME}
    $compiler = if(Test-Path 'tools/build-tools/android-15'){Join-Path $PSScriptRoot 'tools/build-tools/android-15'}else{Join-Path $env:ANDROID_HOME 'build-tools/35.0.0'}
    $android = if(Test-Path 'tools/platform/android-35/android.jar'){Join-Path $PSScriptRoot 'tools/platform/android-35/android.jar'}else{Join-Path $env:ANDROID_HOME 'platforms/android-35/android.jar'}
    $build = Join-Path $PSScriptRoot 'build'
    New-Item -ItemType Directory -Force -Path "$build/classes", "$build/dex" | Out-Null
    function Run([string]$Exe, [string[]]$Arguments) {
        & $Exe @Arguments
        if ($LASTEXITCODE -ne 0) { throw "$Exe exited with $LASTEXITCODE" }
    }
    $sources = @(Get-ChildItem -LiteralPath 'app/src' -Recurse -Filter '*.java' | ForEach-Object FullName)
    Run "$jdk/bin/javac.exe" (@('-encoding','UTF-8','-source','8','-target','8','-classpath',$android,'-d',"$build/classes") + $sources)
    # Keep atomic jar replacements on the writable workspace filesystem.
    Run "$jdk/bin/jar.exe" @("-J-Djava.io.tmpdir=$build",'cf',"$build/classes.jar",'-C',"$build/classes",'.')
    Run "$jdk/bin/java.exe" @('-cp',"$compiler/lib/d8.jar",'com.android.tools.r8.D8','--min-api','28','--lib',$android,'--output',"$build/dex","$build/classes.jar")
    $manifest=Join-Path $PSScriptRoot 'app/AndroidManifest.xml'
    if($Release){
        [xml]$releaseManifest=Get-Content -Raw $manifest
        $ns='http://schemas.android.com/apk/res/android'
        $releaseManifest.manifest.application.SetAttribute('debuggable',$ns,'false') | Out-Null
        foreach($activity in @($releaseManifest.manifest.application.activity)){if($activity.GetAttribute('name',$ns) -eq '.GravityWellPreviewActivity'){$releaseManifest.manifest.application.RemoveChild($activity) | Out-Null}}
        New-Item -ItemType Directory -Force "$build/release" | Out-Null;$manifest=Join-Path $build 'release/AndroidManifest.xml';$releaseManifest.Save($manifest)
    }
    Run "$compiler/aapt.exe" @('package','-f','-M',$manifest,'-S','app/res','-I',$android,'-F',"$build/unaligned.apk")
    Run "$jdk/bin/jar.exe" @("-J-Djava.io.tmpdir=$build",'uf',"$build/unaligned.apk",'-C',"$build/dex",'classes.dex')
    Run "$compiler/zipalign.exe" @('-f','4',"$build/unaligned.apk","$build/aligned.apk")
    if ($Release){
        $configuration=Join-Path $PSScriptRoot '.local-signing/signing.json'
        if(-not (Test-Path -LiteralPath $configuration)){throw 'Run prepare-alpha-signing.ps1 once before a release build. Keep its private files backed up.'}
        $signing=Get-Content -Raw -LiteralPath $configuration | ConvertFrom-Json
        $env:OPENROW_SIGNING_PASSWORD=$signing.password
        try{Run "$jdk/bin/java.exe" @('-jar',"$compiler/lib/apksigner.jar",'sign','--ks',(Join-Path $PSScriptRoot $signing.keystore),'--ks-key-alias',$signing.alias,'--ks-pass','env:OPENROW_SIGNING_PASSWORD','--key-pass','env:OPENROW_SIGNING_PASSWORD','--out',"$build/OpenRow-Alpha.apk","$build/aligned.apk")}finally{Remove-Item Env:OPENROW_SIGNING_PASSWORD -ErrorAction SilentlyContinue}
        $apk="$build/OpenRow-Alpha.apk"
    }else{
    if (-not (Test-Path -LiteralPath "$build/debug.keystore")) {
        Run "$jdk/bin/keytool.exe" @('-genkeypair','-keystore',"$build/debug.keystore",'-storepass','android','-keypass','android','-alias','androiddebugkey','-keyalg','RSA','-keysize','2048','-validity','10000','-dname','CN=OpenRow Debug,O=OpenRow,C=US')
    }
    Run "$jdk/bin/java.exe" @('-jar',"$compiler/lib/apksigner.jar",'sign','--ks',"$build/debug.keystore",'--ks-pass','pass:android','--key-pass','pass:android','--out',"$build/OpenRow-Probe.apk","$build/aligned.apk")
    $apk="$build/OpenRow-Probe.apk"
    }
    Run "$jdk/bin/java.exe" @('-jar',"$compiler/lib/apksigner.jar",'verify','--verbose','--print-certs',$apk)
    Write-Output "Built: $apk"
} finally { Pop-Location }
