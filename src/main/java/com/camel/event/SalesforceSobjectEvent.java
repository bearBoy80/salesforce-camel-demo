package com.camel.event;

import com.camel.salesforce.dtos.SalesforceObjectDTO;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 * @author jt.gui
 */
@Getter
@Setter
public class SalesforceSobjectEvent extends ApplicationEvent {

    private SalesforceObjectDTO dto;

    public SalesforceSobjectEvent(SalesforceObjectDTO dto) {
        super(dto);
        this.dto = dto;
    }
}
