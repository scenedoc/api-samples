package com.tylertech.scenedoc.exportconverter.controller;

import com.tylertech.scenedoc.exportconverter.service.ExportConverterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@RestController
public class MainController {

    @Autowired
    ExportConverterService exportConverterService;

    @RequestMapping("/")
    public String main() throws Exception{
        String timelineEntryId = exportConverterService.generateExportFileFromSceneDoc();
        File exportFile = exportConverterService.downloadExportFileWithRetries(timelineEntryId);
        exportConverterService.extractCsvFilesFromExport(exportFile);
        return "OK;";
    }
}
