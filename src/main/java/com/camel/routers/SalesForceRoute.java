package com.camel.routers;

import com.camel.event.SalesforceSobjectEvent;
import com.camel.salesforce.dtos.AttachmentInfo;
import com.camel.salesforce.dtos.QueryRecordsAttachment;
import com.camel.salesforce.dtos.SalesforceObjectDTO;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.salesforce.SalesforceComponent;
import org.apache.camel.component.salesforce.api.SalesforceException;
import org.apache.camel.component.salesforce.api.dto.Version;
import org.apache.camel.component.salesforce.internal.SalesforceSession;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * @ClassName：SalesForceRoute
 * @Description：TODO
 * @Author：db.xie
 * @Date：2022/11/1 16:11
 * @Version：1.0.0
 */
@Configuration(proxyBeanMethods = false)
public class SalesForceRoute extends RouteBuilder implements ApplicationEventPublisherAware {
    @Value("${server.port}")
    String serverPort;

    @Value("${baeldung.api.path}")
    String contextPath;

    private RestTemplate restTemplate = new RestTemplate();

    ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private CamelContext camelContext;

    private SalesforceComponent salesforce;

    List<Version> list = null;

    String sql;

    @Override
    public void configure() throws Exception {

        restConfiguration().contextPath(contextPath)
                .port(serverPort)
                .enableCORS(true);
        rest("/api/").description("Teste REST Service")
                .id("api-route")
                .post("/bean")
                .produces(MediaType.APPLICATION_JSON)
                .consumes(MediaType.APPLICATION_JSON)
                .bindingMode(RestBindingMode.auto)
                .to("direct:getVersion");
        rest("/api/").description("attachment REST Service")
                .id("api-attachment")
                .get("/attachment")
                .produces(MediaType.APPLICATION_JSON)
                .bindingMode(RestBindingMode.auto)
                .to("direct:attachment");
        from("direct:attachment")
                .to("salesforce:query?sObjectQuery=SELECT Id, FileExtension,Title FROM ContentVersion&sObjectClass=com.camel.salesforce.dtos.QueryRecordsAttachment")
                .log("${body}")
                .process(exchange -> publishSalesforceSobjectEvent(exchange))
                .transform().simple("ok");

        from("direct:getVersion")
                .to("salesforce:getVersions")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        list = (List<Version>) exchange.getIn().getBody();
                        StringBuffer sb = new StringBuffer();
                        sb.append("insert into version(label, url, version) values ");
                        for (Version version : list) {
                            version.setLabel(version.getLabel().replace("\'", "\\'"));
                            version.setUrl(version.getUrl().replace("\'", "\\'"));
                            version.setVersion(version.getVersion().replace("\'", "\\'"));
                        }
                        sql = sb.toString().substring(0, sb.toString().length() - 1);
                        log.info("************************" + sql);
                        exchange.getIn().setBody(list);
                    }
                })
                .split(body())
                .setBody(simple("insert into version(label, url, version) values ('${body.label}', '${body.url}', '${body.version}');"))
                .to("spring-jdbc:salesforce");

    }

    private void publishSalesforceSobjectEvent(Exchange exchange) {
        salesforce = (SalesforceComponent) camelContext.getComponent("salesforce");
        SalesforceSession session = salesforce.getSession();
        if (session != null && session.getAccessToken() == null) {
            try {
                session.login(null);
            } catch (SalesforceException e) {
                throw RuntimeCamelException.wrapRuntimeCamelException(e);
            }
        }
        QueryRecordsAttachment recordsAttachment = (QueryRecordsAttachment) exchange.getIn().getBody();
        for (AttachmentInfo attachmentInfo : recordsAttachment.getList()) {
            SalesforceObjectDTO salesforceObjectDTO = new SalesforceObjectDTO();
            salesforceObjectDTO.setAccessToken(session.getAccessToken());
            salesforceObjectDTO.setBlobField(attachmentInfo.getId());
            salesforceObjectDTO.setFileName(attachmentInfo.getName());
            salesforceObjectDTO.setFileType(attachmentInfo.getFileType());
            SalesforceSobjectEvent event = new SalesforceSobjectEvent(salesforceObjectDTO);
            applicationEventPublisher.publishEvent(event);
        }
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
}
