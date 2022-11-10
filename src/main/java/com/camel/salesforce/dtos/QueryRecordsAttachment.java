package com.camel.salesforce.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.camel.component.salesforce.api.dto.AbstractQueryRecordsBase;

import java.util.List;


/**
 * @author jt.gui
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class QueryRecordsAttachment extends AbstractQueryRecordsBase {
    public List<AttachmentInfo> records;

    public List<AttachmentInfo> getList() {
        return records;
    }

    public void setList(List<AttachmentInfo> list) {
        this.records = list;
    }
}

