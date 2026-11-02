param(
    [ValidateSet('all','balanced-standard','balanced-easy','balanced-high','focused-standard','unupgraded-standard','balanced-guided','balanced-seed2','balanced-seed3')]
    [string]$Scenario = 'all'
)
$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    $auditJdk = if(Test-Path 'tools/jdk'){(Get-ChildItem -LiteralPath 'tools/jdk' -Directory | Select-Object -First 1).FullName}else{$env:JAVA_HOME}
    New-Item -ItemType Directory -Force -Path 'build/progression-audit','build/tests' | Out-Null
    $sources = @(
        'app/src/org/openrow/probe/GravityWellBalance.java',
        'app/src/org/openrow/probe/GravityWellCampaign.java',
        'app/src/org/openrow/probe/GravityWellGame.java',
        'app/src/org/openrow/probe/GravityWellScene.java',
        'app/src/org/openrow/probe/TrainingCore.java',
        'app/src/org/openrow/probe/TrainingVisuals.java',
        'app/src/org/openrow/probe/GameWorkoutAdapter.java',
        'tests/GravityWellProgressionSimulation.java'
    )
    & "$auditJdk/bin/javac.exe" -encoding UTF-8 -d build/tests @sources
    if ($LASTEXITCODE -ne 0) { throw 'Progression simulation compilation failed' }
    $cases = @(
        @('balanced-standard','0.5','2.5','balanced'),
        @('balanced-easy','0.2','1.5','balanced'),
        @('balanced-high','1.0','4.0','balanced'),
        @('focused-standard','0.5','2.5','focused'),
        @('unupgraded-standard','0.5','2.5','none'),
        @('balanced-guided','0.5','2.5','balanced'),
        @('balanced-seed2','0.5','2.5','balanced'),
        @('balanced-seed3','0.5','2.5','balanced')
    )
    foreach ($case in $cases) {
        if ($Scenario -ne 'all' -and $case[0] -ne $Scenario) { continue }
        & "$auditJdk/bin/java.exe" -Xmx512m -cp build/tests org.openrow.probe.GravityWellProgressionSimulation @case build/progression-audit
        if ($LASTEXITCODE -ne 0) { throw "Progression simulation failed: $($case[0])" }
    }
    Write-Output 'Simulation artifacts: build/progression-audit'
} finally { Pop-Location }

