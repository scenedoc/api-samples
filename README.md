# SceneDoc API Samples
This repository contains samples that use SceneDoc's API.

## Getting Started
You will require an account on one of SceneDoc's production or training servers in order to use the API.  If you have an account, you will need to generate an Access Key ID and Token in order to authenticate with the API.  Complete the following steps to generate a new API token:

1. Login to the web console of the SceneDoc environment you have access to
2. Click on your name in the top left corner of the window.  This will display a floating menu
3. Click `My Account`
4. Under the `My Account` submenu there are a list of menu options, click `API Keys`
5. You will now be in the API Key Management console.  To generate a new key, click the `Generate New API Token` button
6. Copy both the Access Key ID and Token to a safe location.  You will not have access to the Token once the prompt is closed

## Authentication

SceneDoc's API uses `Basic` auth for authentication where the Access Key ID is the username and the Token is the password.  Some tools/libraries will allow you to provide username and password when authenticating.  If you are required to generate the Basic Authorization header manually, <a href="https://gist.github.com/brandonmwest/a2632d0a65088a20c00a" target="_blank">here</a> is a link to a resource which will help you generate these headers.

