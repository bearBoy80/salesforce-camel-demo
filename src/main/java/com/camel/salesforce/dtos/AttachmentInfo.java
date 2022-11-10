package com.camel.salesforce.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Map;

/**
 * @author jt.gui
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AttachmentInfo {
    private Map<String, Object> attributes;

    @JsonAlias({"Title"})
    private String name;

    @JsonAlias({"FileExtension"})
    private String fileType;

    @JsonAlias(value = {"Description"})
    private String description;
    @JsonAlias(value = {"Id"})
    private String id;

}
