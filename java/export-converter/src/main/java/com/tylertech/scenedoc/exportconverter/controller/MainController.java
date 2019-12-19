package com.tylertech.scenedoc.exportconverter.controller;

import com.tylertech.scenedoc.exportconverter.service.ExportConverterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@RestController
public class MainController {

    @Autowired
    ExportConverterService exportConverterService;

    @RequestMapping("/")
    public String main(@RequestParam(name="dateFrom", required = false, defaultValue = "0") String dateFrom) throws Exception{
        exportConverterService.generateAndLoadExportFile(Long.parseLong(dateFrom));
        return "OK;";
    }
}
