package com.tylertech.scenedoc.exportconverter.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.Arrays;

@Service
public class SceneDocClientService {
    protected final Log LOG = LogFactory.getLog(this.getClass());

    private String url;
    private String apiKey;
    private String apiSecretKey;
    private HttpEntity<String> headers;

    @PostConstruct
    public void initialize() throws Exception {
        apiKey = System.getenv("API_KEY");
        apiSecretKey = System.getenv("API_SECRET_KEY");
        url = System.getenv("URL");

        if (apiKey==null || apiSecretKey==null || url==null) throw new Exception("API_KEY, API_SECRET_KEY and URL are required");

        LOG.info("SceneDoc Server URL: " + url);
        LOG.info("User API Key: " + apiKey);

        HttpHeaders hdrs = new HttpHeaders();
        hdrs.setContentType(MediaType.APPLICATION_JSON);
        hdrs.setBasicAuth(apiKey, apiSecretKey);
        headers = new HttpEntity<>("parameters", hdrs);
    }

    public String getUrl() {
        return url;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getApiSecretKey() {
        return apiSecretKey;
    }

    public HttpEntity<String> getHeaders() {
        return headers;
    }
}
