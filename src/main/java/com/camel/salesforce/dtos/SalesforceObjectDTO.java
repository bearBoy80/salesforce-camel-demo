package com.camel.salesforce.dtos;

import lombok.Data;

import java.io.Serializable;


/**
 * @author jt.gui
 */
@Data
public class SalesforceObjectDTO implements Serializable {

    private String blobField;

    private String accessToken;

    private String fileType;

    private String fileName;


}
