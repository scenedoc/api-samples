# Export Converter
Export Converter is an application which runs as a service that will export data from an authenticated user's SceneDoc Organization and input it into Metabase (BI).  This project was created with the intention to demonstrate SceneDoc's capabilities for exporting data into a general format (CSV) and ingest it into a data warehouse, analytics platform or other 3rd party system.

The BI stack that was chosen for this demonstration consists of:

- Metabase (https://www.metabase.com/)
- MongoDB (https://www.mongodb.com/)

The Export Converter utility is specifically designed to export data from SceneDoc and ingest it into this particular BI stack.

## How it works

When the Export Converter service is started the following scheduled task will be run once an hour:

1) Call SceneDoc Server's Export API endpoint which creates a Timeline Attachment that contains the organization's content translated into multiple CSV files
2) Check if the Timeline Attachment is ready for download
3) When the Timeline Attachment is ready, download and extract the files
4) Traverse each CSV file extracted
5) Translate and load each file into MongoDB so it can be analyzed using Metabase

## Docker Setup

The application can be run standalone as a Java application (JAR) or as a Docker container.  Steps to build a Docker image are included in the projects `Makefile`.  A Docker Compose definition file has been provided which will provision the BI stack mentioned above and the Export Converter. 


