$baseUrl = "https://qa.scenedoc.com"

$accessKeyId = "SDIDKG2UJGHEXPOPNBLBRWAVY7CGV"
$token = ""

# Change as needed
$formCanonicalId = 18349
$reportFormat = "~PDF Mapping 2_MAPPING"
$dateFrom = (Get-Date (Get-Date).AddDays(-7) -UFormat %s) + "000" # will fetch anything in the last 7 days
$dateTo = (Get-Date -UFormat %s) + "000"

function Get-BasicAuthCreds {
    param([string]$Username,[string]$Password)
    $AuthString = "{0}:{1}" -f $Username,$Password
    $AuthBytes  = [System.Text.Encoding]::Ascii.GetBytes($AuthString)
    return [Convert]::ToBase64String($AuthBytes)
}

$params = @{
    "formCanonicalId"=$formCanonicalId;
    "reportFormat"=$reportFormat;
    "dateFrom"=$dateFrom
    "dateTo"=$dateTo;
}

$BasicCreds = Get-BasicAuthCreds -Username $accessKeyId -Password $token
$r = Invoke-WebRequest -Uri $baseUrl"/rest/reporting" -Method Post -Headers @{"Authorization"="Basic $BasicCreds"} -Body ($params|ConvertTo-Json) -ContentType "application/json"
$r.StatusCode
$content = ConvertFrom-Json $r.Content

if ($r.StatusCode -eq "200")
{
    $id = $content.id
    Write-Output "processing, please wait..."
    do {
        $rGetTimelineEntryByIdResponse = Invoke-WebRequest -Uri $baseUrl"/rest/timelines/"$id -Method Get -Headers @{"Authorization"="Basic $BasicCreds"}
        $contentUploaded = ConvertFrom-Json $rGetTimelineEntryByIdResponse.Content
    } while($contentUploaded.mediaStatus -ne "UPLOADED" -AND $contentUploaded.entryType -ne "NOTE")

    $rGetTimelineEntryByIdResponse = Invoke-WebRequest -Uri $baseUrl"/rest/timelines/"$id -Method Get -Headers @{"Authorization"="Basic $BasicCreds"}
    $contentUploaded = ConvertFrom-Json $rGetTimelineEntryByIdResponse.Content
    if($contentUploaded.entryType -eq "NOTE"){
        Write-Output "No submissions found"
    }
    else {
        $ScriptPath = Split-Path -parent $MyInvocation.MyCommand.Definition
        $output = $ScriptPath + "/" + $content.fileName
        $stream = $baseUrl + "/rest/timelines/stream/" + $id

        $start_time = Get-Date

        # Using System.Net.WebClient Speed is great as the HTTP response stream is buffered to disk throughout the download process.
        # There is also the option of System.Net.WebClient.DownloadFileAsync(). If you'd like your script to continue while the file downloads in parallel.
        $wc = New-Object System.Net.WebClient
        $wc.Headers["Authorization"] = "Basic $BasicCreds"
        Register-ObjectEvent -InputObject $wc -EventName DownloadFileCompleted -Action { Write-Host 'Download completed successfully' }
        $wc.DownloadFile($stream, $output)
        Write-Output "Time taken: $((Get-Date).Subtract($start_time).Seconds) second(s)"
    }
}
else {
    Write-Output "Error occurred"
}

