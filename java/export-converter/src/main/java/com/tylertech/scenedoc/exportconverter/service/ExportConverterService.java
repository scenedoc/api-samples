package com.tylertech.scenedoc.exportconverter.service;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.mongodb.client.MongoDatabase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class ExportConverterService {
    protected final Log LOG = LogFactory.getLog(this.getClass());

    @Autowired
    SceneDocClientService sceneDocClientService;

    @Autowired
    MongoClientService mongoClientService;


    /**
     *
     * Downloads export file from SceneDoc and loads the file into Mongo
     *
     * @throws Exception
     */
    @Async
    public void generateAndLoadExportFile(Long dateFrom) throws Exception {
        String timelineEntryId = this.generateExportFileFromSceneDoc(dateFrom);
        File exportFile = this.downloadExportFileWithRetries(timelineEntryId);
        this.extractCsvFilesFromExport(exportFile);
    }

    /**
     * Generate Export file from SceneDoc
     */
    public String generateExportFileFromSceneDoc(Long dateFrom) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        String url = sceneDocClientService.getUrl() + "/rest/timelines/generate-organization-csv?from=" + dateFrom;
        LOG.info("Requesting report generation file at: " + url);
        ResponseEntity<String> result = restTemplate.exchange(url, HttpMethod.POST, sceneDocClientService.getHeaders(), String.class);
        if (result!=null && result.getStatusCode().is2xxSuccessful()){
            JSONObject jsonObject = new JSONObject(result.getBody());
            LOG.info("Export file generating with ID: " + jsonObject.getString("id"));
            return jsonObject.getString("id");
        }
        throw new Exception("Did not successfully create Export File: " + result.getStatusCode());
    }

    /**
     * If Export file is available, download from SceneDoc
     */
    public File downloadExportFileWithRetries(String id) throws Exception{
        RestTemplate restTemplate = new RestTemplate();
        String url = sceneDocClientService.getUrl() + "/rest/timelines/" + id;
        LOG.info("Checking status of export file at: " + url);
        ResponseEntity<String> result = restTemplate.exchange(url, HttpMethod.GET, sceneDocClientService.getHeaders(), String.class);
        if (result!=null && result.getStatusCode().is2xxSuccessful()){
            JSONObject jsonObject = new JSONObject(result.getBody());
            String status = jsonObject.getString("mediaStatus");
            if (status.equals("UPLOADED")){
                return downloadExportFile(id);
            } else {
                LOG.info("Export file not ready yet, waiting then trying again");
                Thread.sleep(5000L);
                return downloadExportFileWithRetries(id);
            }
        }
        throw new Exception("Did not successfully fetch Timeline Entry: " + result.getStatusCode());
    }

    /**
     * Download export file from SceneDoc if available
     */
    public File downloadExportFile(String id) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        String url = sceneDocClientService.getUrl() + "/rest/timelines/stream/" + id;
        LOG.info("Downloading export file from: " + url);
        ResponseEntity<byte[]> result = restTemplate.exchange(url, HttpMethod.GET, sceneDocClientService.getHeaders(), byte[].class);

        File tempFile = File. createTempFile("export", ".zip");
        OutputStream os = new FileOutputStream(tempFile);

        // Starts writing the bytes in it
        os.write(result.getBody());
        if (!tempFile.exists() || tempFile.length()==0){
            throw new Exception("Downloaded file is empty or does not exist");
        }
        return tempFile;
    }

    /**
     * Export CSV files from downloaded binary
     */
    public Path extractCsvFilesFromExport(File exportFile) throws IOException{
        LOG.info("Extracting CSV files from export");
        Path tempDir = Files.createTempDirectory("export");
        FileInputStream fis;
        //buffer for read and write data to file
        byte[] buffer = new byte[1024];

        fis = new FileInputStream(exportFile);
        ZipInputStream zis = new ZipInputStream(fis);
        ZipEntry ze = zis.getNextEntry();
        while(ze != null){
            String fileName = ze.getName();
            File newFile = new File(tempDir.toString() + File.separator + fileName);
            LOG.info("Unzipping to "+newFile.getAbsolutePath());
            //create directories for sub directories in zip
            new File(newFile.getParent()).mkdirs();
            FileOutputStream fos = new FileOutputStream(newFile);
            int len;
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            fos.close();
            //close this ZipEntry
            zis.closeEntry();
            ze = zis.getNextEntry();

            LOG.info("Parsing CSV and loading into DB: " + fileName);
            parseCsvIntoJson(newFile, fileName);
        }
        //close last ZipEntry
        zis.closeEntry();
        zis.close();
        fis.close();

        return tempDir;
    }

    /**
     * Convert CSV file into JSON
     */
    public void parseCsvIntoJson(File csvFile, String fileName) throws IOException {
        List<Map<?, ?>> data = readObjectsFromCsv(csvFile);
        ObjectMapper mapper = new ObjectMapper();
        for (Map<?, ?> itm : data){
            loadPayloadIntoDb(fileName, mapper.writeValueAsString(itm));
        }
    }


    public static List<Map<?,?>> readObjectsFromCsv(File csvFile) throws IOException {
        CsvSchema bootstrap = CsvSchema.emptySchema().withHeader();
        CsvMapper csvMapper = new CsvMapper();
        MappingIterator<Map<?, ?>> mappingIterator = csvMapper.reader(Map.class).with(bootstrap).readValues(csvFile);
        return mappingIterator.readAll();
    }

    /**
     * Load the payload into the database
     */
    public void loadPayloadIntoDb(String collection, String payload){
        mongoClientService.getMongoDatabase().getCollection(collection).insertOne(Document.parse(payload));
    }
}
