$baseUrl = ""

$accessKeyId = ""
$token = ""

#### API parameters ####
# Change as needed
# ~ - search form mapping by name
# ^ - search report format by name
$formCanonicalId = 12345
$reportFormat = "~PDF Mapping 2_MAPPING,^Sunridge"

$dateFrom = [int][double]::Parse((Get-Date -Date ((get-date).AddDays(-7).ToUniversalTime()) -UFormat %s)) # will fetch anything in the last 7 days
$dateFromUnix = $dateFrom.ToString() + "000"
$dateTo = [int](Get-Date -UFormat %s -Millisecond 0)
$dateToUnix = $dateTo.ToString() + "000"

# modify at own risk
$Timeout = 30 # seconds
function Get-BasicAuthCreds {
    param([string]$Username,[string]$Password)
    if([string]::IsNullOrEmpty($Username) -or [string]::IsNullOrEmpty($Password)){
        return null
    }
    else { 
        $AuthString = "{0}:{1}" -f $Username,$Password
        $AuthBytes  = [System.Text.Encoding]::Ascii.GetBytes($AuthString)
        return [Convert]::ToBase64String($AuthBytes)
    }
}

$params = @{
    "formCanonicalId"=$formCanonicalId;
    "reportFormat"=$reportFormat;
    "dateFrom"=$dateFromUnix;
    "dateTo"=$dateToUnix;
}

$BasicCreds = Get-BasicAuthCreds -Username $accessKeyId -Password $token
if(![string]::IsNullOrEmpty($BasicCreds)){
    try {
        $r = Invoke-WebRequest -Uri $baseUrl"/rest/reporting" -Method Post -Headers @{"Authorization"="Basic $BasicCreds"} -Body ($params|ConvertTo-Json) -ContentType "application/json" -TimeoutSec $Timeout
        $r.StatusCode

        if ($r.StatusCode -eq "200")
        {
            $content = ConvertFrom-Json $r.Content
            $id = $content.id
            Write-Output "processing, please wait..."
            $contentUploaded = $null
            do {
                $rGetTimelineEntryByIdResponse = Invoke-WebRequest -Uri $baseUrl"/rest/timelines/"$id -Method Get -Headers @{"Authorization"="Basic $BasicCreds"} -TimeoutSec $Timeout
                $contentUploaded = ConvertFrom-Json $rGetTimelineEntryByIdResponse.Content
            } while($contentUploaded.mediaStatus -ne "UPLOADED" -AND $contentUploaded.entryType -ne "NOTE")

            if($contentUploaded.entryType -eq "NOTE"){
                Write-Output "No submissions found"
            } elseif($contentUploaded.mediaStatus -eq "ERROR") {
                Write-Output "ERROR"
            } else {
                $ScriptPath = Split-Path -parent $MyInvocation.MyCommand.Definition
                $output = $ScriptPath + "/" + $content.fileName
                $stream = $baseUrl + "/rest/timelines/stream/" + $id

                $start_time = Get-Date

                # Using System.Net.WebClient Speed is great as the HTTP response stream is buffered to disk throughout the download process.
                # There is also the option of System.Net.WebClient.DownloadFileAsync(). If you'd like your script to continue while the file downloads in parallel.
                $wc = New-Object System.Net.WebClient
                $wc.Headers["Authorization"] = "Basic $BasicCreds"
                try {
                    $wc.DownloadFile($stream, $output)
                    Write-Output "------------------------------------"
                    Write-Output "|  Download completed successfully |"
                    Write-Output "------------------------------------"
                } catch [System.Net.WebException]  {  
                    Write-Warning "Download of $stream failed"  
                } finally {
                    # once files are done downloading
                    $wc.Dispose()
                }
                Write-Output "Time taken: $((Get-Date).Subtract($start_time).Seconds) second(s)"
            }
        }
    } catch {
        Write-Output "Error ocurred"
        $err = $_.Exception #or try '$_' only
        $StatusCode = $_.Exception.Response.StatusCode.value__
        # $err | Get-Member
        $err.Message
        $StatusCode
        Write-Output "==================================================================="
    }
}
else {
    Write-Output "Please provide Credentials"
}