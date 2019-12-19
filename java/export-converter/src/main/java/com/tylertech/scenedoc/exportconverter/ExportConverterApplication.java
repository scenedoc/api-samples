package com.tylertech.scenedoc.exportconverter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {MongoAutoConfiguration.class})
@EnableAsync
@EnableScheduling
public class ExportConverterApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExportConverterApplication.class, args);
    }

}
