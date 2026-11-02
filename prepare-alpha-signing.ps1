$ErrorActionPreference='Stop'
Push-Location $PSScriptRoot
try{
    if(Test-Path '.local-signing/signing.json'){throw 'Signing identity already exists; keep it for updates.'}
    $jdk=if(Test-Path 'tools/jdk'){(Get-ChildItem tools/jdk -Directory | Select-Object -First 1).FullName}else{$env:JAVA_HOME}
    New-Item -ItemType Directory -Force '.local-signing' | Out-Null
    $bytes=New-Object byte[] 32;$random=[Security.Cryptography.RandomNumberGenerator]::Create();$random.GetBytes($bytes);$random.Dispose()
    $password=[Convert]::ToBase64String($bytes);$env:OPENROW_SIGNING_PASSWORD=$password
    try{
        if(Test-Path 'build/debug.keystore'){
            # Preserve the original tablet's certificate so installation remains an in-place update.
            & "$jdk/bin/keytool.exe" -importkeystore -srckeystore build/debug.keystore -srcstorepass android -srcalias androiddebugkey -destkeystore .local-signing/openrow-alpha.p12 -deststoretype PKCS12 -destalias openrowalpha -deststorepass:env OPENROW_SIGNING_PASSWORD -noprompt
        }else{
            & "$jdk/bin/keytool.exe" -genkeypair -keystore .local-signing/openrow-alpha.p12 -storetype PKCS12 -storepass:env OPENROW_SIGNING_PASSWORD -alias openrowalpha -keyalg RSA -keysize 3072 -validity 10000 -dname 'CN=OpenRow Alpha,O=OpenRow,C=US'
        }
        if($LASTEXITCODE -ne 0){throw 'Signing identity preparation failed'}
        @{keystore='.local-signing/openrow-alpha.p12';alias='openrowalpha';password=$password} | ConvertTo-Json | Set-Content -Encoding utf8 .local-signing/signing.json
    }finally{Remove-Item Env:OPENROW_SIGNING_PASSWORD -ErrorAction SilentlyContinue}
    Write-Output 'Private signing identity prepared in .local-signing. Back up this folder securely; never publish it.'
}finally{Pop-Location}
