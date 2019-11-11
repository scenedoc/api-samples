# Generate Report
PowerShell script that will generate form reports using SceneDoc's API. Please keep in mind this is just an example of what you can build,
please feel free to edit the code to meet your intentions.

## Getting Started
This script will get you up and running for generating Form Submission/Citation reports. 
You will need to provide your own credentials where you can find by going to your ```My Account/API keys``` section and generate a new API token

Replace `$accessKeyId` & `$token` with your own.

## How it works

### Part 1
The script calls SceneDoc's API `POST` endpoint ```/rest/reporting``` passing basic parameters to generate any available form submissions in the last 7 days from current Day and Time. Notice that `formCanonicalId` and `reportFormat` are required fields.

For more information on what parameters are available please visit the documentation section under your SceneDoc environment.

This script can be tailored to more interesting actions like adding a field filter or by providing the final filename to use for the packaged export

### Part 2
The script checks for a `200` status code after the first call, if successful it moves onto the second call being `GET /rest/timelines/{id}` inside a loop,
it basically calls the API over and over until the payload's mediaStatus has a value of `UPLOADED` meaning it found submissions and it finished stitching it all together.

If no submissions were found it simply displays a message to the screen

### Part 3
Assuming submissions were found it moves to the final call `/rest/timelines/stream/{id}` where it downloads the compressed file onto your machine.


## Downloading Content
If submissions for the specified form template where found a `.zip` file containing the respective files will be downloaded to your machine in the current directory you chose to run the script.
If you wish to change it, you can do so by changing the `$output` variable containing the path to where to save the compressed file.

To access the files simply unzip the compressed folder.

