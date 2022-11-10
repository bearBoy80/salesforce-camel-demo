package com.camel.event;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URI;

@Configuration(proxyBeanMethods = false)
@Slf4j
public class SalesforceSobjectListener {
    @Autowired
    private CamelContext camelContext;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${baeldung.api.downFileUrl}")
    private String url;

    @EventListener
    @Async
    public void processBlockedListEvent(SalesforceSobjectEvent event) {
        //参考文档 https://developer.salesforce.com/docs/atlas.en-us.234.0.api_rest.meta/api_rest/resources_sobject_blob_retrieve.htm
        String uriTemplate = url;
        URI uri = UriComponentsBuilder.fromUriString(uriTemplate).build(event.getDto().getBlobField());
        RequestEntity<Void> requestEntity = RequestEntity.get(uri)
                .header("Authorization", "Bearer " + event.getDto().getAccessToken())
                .build();
        String fileName = event.getDto().getFileName() + "." + event.getDto().getFileType();
        log.info("start down file name :" + fileName);
        ResponseEntity<byte[]> response = restTemplate.exchange(requestEntity, byte[].class);
        File file = new File(fileName);
        try {
            FileUtils.writeByteArrayToFile(file, response.getBody());
        } catch (IOException e) {
            //todo
            e.printStackTrace();
        }
        log.info("end down file name :" + fileName);
    }
}
