# Media Sync
Media Sync is a simple Golang project that will pull all entries from an authenticated user's Timeline and download all relevant binary data from those entries.  Relevant entries include Timeline entries that are Photos, Video, Audio or Attachments.

## How it works

The user is required to enter their SceneDoc Access Key ID and Token when the program is run.  After doing so the application will do the following:

1) Fetch all Timeline entries from the user's Timeline
2) Loop through the list of entries and download available binary data

## Future Changes

- Provide a date range filter in the Timeline list request
- Allow user to provide credentials via environment variables
- Compile the project into executables that a user can download for Windows, Linux or MacOS

