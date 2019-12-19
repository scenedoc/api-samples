package com.tylertech.scenedoc.exportconverter.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class ScheduledService {
    protected final Log LOG = LogFactory.getLog(this.getClass());

    @Autowired
    ExportConverterService exportConverterService;

    private Long fromDate = 0L;

    /**
     * Run once an hour
     */
    @Scheduled(fixedDelay = 3600000, initialDelay = 10000)
    public void executeExportService(){
        try {
            LOG.info("Running Export");
            exportConverterService.generateAndLoadExportFile(fromDate);
            fromDate = new Date().getTime();
            LOG.info("Next scheduled run will process entries from: " + new Date(fromDate).toString());
        } catch(Exception e){
            LOG.warn(e,e);
        }
    }
}
