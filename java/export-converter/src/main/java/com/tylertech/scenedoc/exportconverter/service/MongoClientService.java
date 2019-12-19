package com.tylertech.scenedoc.exportconverter.service;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import org.springframework.boot.autoconfigure.rsocket.RSocketProperties;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;

@Service
public class MongoClientService {
    private String host = null;
    private Integer port = null;
    private String databaseName = null;
    private MongoDatabase mongoDatabase;

    @PostConstruct
    public void initialize() throws Exception {

        host = System.getenv("MONGO_HOST");
        port = System.getenv("MONGO_PORT")==null ? null : Integer.parseInt(System.getenv("MONGO_PORT"));
        databaseName = System.getenv("MONGO_DATABASE");

        MongoClientURI connectionString = new MongoClientURI("mongodb://" + (host==null ? "localhost" : host) + ":" + (port==null ? "27017" : port.toString()));
        MongoClient mongoClient = new MongoClient(connectionString);
        mongoDatabase = mongoClient.getDatabase(databaseName==null ? "db" : databaseName);
    }

    public MongoDatabase getMongoDatabase() {
        return mongoDatabase;
    }


}
