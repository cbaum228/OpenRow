$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    $jdk = if(Test-Path 'tools/jdk'){(Get-ChildItem -LiteralPath 'tools/jdk' -Directory | Select-Object -First 1).FullName}else{$env:JAVA_HOME}
    New-Item -ItemType Directory -Force -Path 'build/tests' | Out-Null
    & "$jdk/bin/javac.exe" -encoding UTF-8 -d build/tests app/src/org/openrow/probe/FamilyProfiles.java tests/FamilyProfilesTest.java
    if ($LASTEXITCODE -ne 0) { throw 'Profile test compilation failed' }
    & "$jdk/bin/java.exe" -cp build/tests org.openrow.probe.FamilyProfilesTest
    if ($LASTEXITCODE -ne 0) { throw 'Profile tests failed' }
    & "$jdk/bin/javac.exe" -encoding UTF-8 -d build/tests app/src/org/openrow/probe/WorkoutSession.java app/src/org/openrow/probe/WorkoutStore.java app/src/org/openrow/probe/RowingTelemetryFrame.java app/src/org/openrow/probe/S4Channels.java app/src/org/openrow/probe/RowingStats.java app/src/org/openrow/probe/RowingMetrics.java app/src/org/openrow/probe/CastlebreakerGame.java app/src/org/openrow/probe/CampaignSession.java app/src/org/openrow/probe/GravityWellBalance.java app/src/org/openrow/probe/GravityWellCampaign.java app/src/org/openrow/probe/GravityWellGame.java app/src/org/openrow/probe/GravityWellScene.java app/src/org/openrow/probe/AthleteHistory.java tests/S4ChannelsTest.java tests/RowingMetricsTest.java tests/CastlebreakerGameTest.java tests/CampaignSessionTest.java tests/GravityWellCampaignTest.java tests/GravityWellGameTest.java tests/GravityWellCatalogTest.java tests/AthleteHistoryTest.java
    if ($LASTEXITCODE -ne 0) { throw 'Test compilation failed' }
    & "$jdk/bin/java.exe" -cp build/tests org.openrow.probe.S4ChannelsTest
    if ($LASTEXITCODE -ne 0) { throw 'S4 channel tests failed' }
    $replay = @('tests/fixtures/s4-five-strokes.log')
    & "$jdk/bin/java.exe" -cp build/tests org.openrow.probe.RowingMetricsTest @replay
    if ($LASTEXITCODE -ne 0) { throw 'Metric tests failed' }
    & "$jdk/bin/java.exe" -cp build/tests org.openrow.probe.CastlebreakerGameTest
    if ($LASTEXITCODE -ne 0) { throw 'Castlebreaker tests failed' }
    & "$jdk/bin/java.exe" -cp build/tests org.openrow.probe.CampaignSessionTest
    if ($LASTEXITCODE -ne 0) { throw 'Campaign tests failed' }
    & "$jdk/bin/java.exe" -cp build/tests org.openrow.probe.GravityWellCampaignTest
    if ($LASTEXITCODE -ne 0) { throw 'Gravity Well campaign tests failed' }
    & "$jdk/bin/java.exe" -cp build/tests org.openrow.probe.GravityWellGameTest
    if ($LASTEXITCODE -ne 0) { throw 'Gravity Well tests failed' }
    & "$jdk/bin/java.exe" -cp build/tests org.openrow.probe.GravityWellCatalogTest
    if ($LASTEXITCODE -ne 0) { throw 'Gravity Well catalog tests failed' }
    & "$jdk/bin/java.exe" -cp build/tests org.openrow.probe.AthleteHistoryTest
    if ($LASTEXITCODE -ne 0) { throw 'Athlete history tests failed' }
    & "$jdk/bin/javac.exe" -encoding UTF-8 -cp build/tests -d build/tests app/src/org/openrow/probe/TrainingCore.java app/src/org/openrow/probe/GameWorkoutAdapter.java app/src/org/openrow/probe/TrainingVisuals.java tests/TrainingCampaignIntegrationTest.java tests/TrainingCoreTest.java tests/TrainingGameIntegrationTest.java tests/TrainingVisualsTest.java app/src/org/openrow/probe/GameplayEffort.java tests/GameplayEffortTest.java
    if ($LASTEXITCODE -ne 0) { throw 'Training test compilation failed' }
    & "$jdk/bin/java.exe" -cp build/tests org.openrow.probe.TrainingCoreTest
    if ($LASTEXITCODE -ne 0) { throw 'Training scenario tests failed' }
    & "$jdk/bin/java.exe" -cp build/tests org.openrow.probe.TrainingCampaignIntegrationTest
    if ($LASTEXITCODE -ne 0) { throw 'Trainer/campaign integration tests failed' }
    & "$jdk/bin/java.exe" -cp build/tests org.openrow.probe.TrainingGameIntegrationTest
    if ($LASTEXITCODE -ne 0) { throw 'Training/game integration tests failed' }
    & "$jdk/bin/java.exe" -cp build/tests org.openrow.probe.TrainingVisualsTest
    if ($LASTEXITCODE -ne 0) { throw 'Training visual integration tests failed' }
    & "$jdk/bin/java.exe" -cp build/tests org.openrow.probe.GameplayEffortTest
    if ($LASTEXITCODE -ne 0) { throw 'Personal gameplay contribution tests failed' }
    & "$jdk/bin/javac.exe" -encoding UTF-8 -cp build/tests -d build/tests app/src/org/openrow/probe/RowingEffortFrame.java tests/WorkoutRecordingTest.java
    if ($LASTEXITCODE -ne 0) { throw 'Workout recorder test compilation failed' }
    & "$jdk/bin/java.exe" -cp build/tests org.openrow.probe.WorkoutRecordingTest
    if ($LASTEXITCODE -ne 0) { throw 'Workout recorder tests failed' }
    & "$jdk/bin/javac.exe" -encoding UTF-8 -cp build/tests -d build/tests app/src/org/openrow/probe/GraphicsPerformance.java tests/GraphicsMotionTest.java
    if ($LASTEXITCODE -ne 0) { throw 'Graphics motion compilation failed' }
    & "$jdk/bin/java.exe" -cp build/tests org.openrow.probe.GraphicsMotionTest
    if ($LASTEXITCODE -ne 0) { throw 'Graphics motion tests failed' }
} finally { Pop-Location }



