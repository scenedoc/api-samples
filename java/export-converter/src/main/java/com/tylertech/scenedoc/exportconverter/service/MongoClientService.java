package com.tylertech.scenedoc.exportconverter.service;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.rsocket.RSocketProperties;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;

@Service
public class MongoClientService {
    protected final Log LOG = LogFactory.getLog(this.getClass());

    private String host = null;
    private Integer port = null;
    private String databaseName = null;
    private MongoDatabase mongoDatabase;

    @PostConstruct
    public void initialize() throws Exception {
        host = System.getenv("MONGO_HOST");
        if (host==null) host = "localhost";
        port = System.getenv("MONGO_PORT")==null ? 27017 : Integer.parseInt(System.getenv("MONGO_PORT"));
        databaseName = System.getenv("MONGO_DATABASE");
        if (databaseName==null) databaseName="db";

        String connectionString = "mongodb://" + host + ":" + port;
        LOG.info("Connecting to: " + connectionString);
        MongoClientURI connection = new MongoClientURI(connectionString);
        MongoClient mongoClient = new MongoClient(connection);
        mongoDatabase = mongoClient.getDatabase(databaseName);
    }

    public MongoDatabase getMongoDatabase() {
        return mongoDatabase;
    }


}
