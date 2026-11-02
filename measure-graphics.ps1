param([ValidateRange(5,120)][int]$Seconds=20,[string]$Name='sample')
$ErrorActionPreference='Stop'
Push-Location $PSScriptRoot
try {
    if($Name -notmatch '^[a-zA-Z0-9_-]+$'){throw 'Use a simple sample name'}
    $adb=(Resolve-Path tools/platform-tools/adb.exe).Path
    $output=Join-Path $PSScriptRoot 'build/graphics-021'
    New-Item -ItemType Directory -Force $output | Out-Null
    $layer='org.openrow.probe/org.openrow.probe.GravityWellPreviewActivity#0'
    $presented=[System.Collections.Generic.SortedSet[long]]::new()
    $raw=[System.Collections.Generic.List[string]]::new()
    $cutoff=0L
    for($poll=0;$poll -le $Seconds;$poll++){
        $lines=& $adb shell dumpsys SurfaceFlinger --latency $layer
        if($LASTEXITCODE -ne 0){throw 'SurfaceFlinger read failed'}
        foreach($line in $lines){$raw.Add($line);$parts=$line.Trim() -split '\s+';if($parts.Count -ne 3){continue}
            $timestamp=0L;if([long]::TryParse($parts[1],[ref]$timestamp) -and $timestamp -gt 0 -and $timestamp -lt 9000000000000000000L){
                if($poll -eq 0){$cutoff=[Math]::Max($cutoff,$timestamp)}elseif($timestamp -gt $cutoff){$presented.Add($timestamp) | Out-Null}
            }
        }
        if($poll -lt $Seconds){Start-Sleep -Milliseconds 1000}
    }
    $raw | Set-Content -Encoding ascii (Join-Path $output "$Name-surface.tsv")
    $times=@($presented);if($times.Count -lt 10){throw 'No sufficient presentation timestamps; keep the visual lab foregrounded'}
    $intervals=for($i=1;$i -lt $times.Count;$i++){($times[$i]-$times[$i-1])/1000000.0}
    $ordered=@($intervals | Sort-Object)
    $result=[ordered]@{name=$Name;source='SurfaceFlinger actualPresentTime';frames=$times.Count;seconds=($times[-1]-$times[0])/1000000000.0;fps=1000000000.0*($times.Count-1)/($times[-1]-$times[0]);median_ms=$ordered[[int][Math]::Ceiling($ordered.Count*.5)-1];p95_ms=$ordered[[int][Math]::Ceiling($ordered.Count*.95)-1];p99_ms=$ordered[[int][Math]::Ceiling($ordered.Count*.99)-1]}
    $result | ConvertTo-Json | Set-Content -Encoding utf8 (Join-Path $output "$Name-summary.json")
    & $adb logcat -d -s GravityFrames:I | Set-Content -Encoding utf8 (Join-Path $output "$Name-draw-log.txt")
    $result | ConvertTo-Json
} finally {Pop-Location}
