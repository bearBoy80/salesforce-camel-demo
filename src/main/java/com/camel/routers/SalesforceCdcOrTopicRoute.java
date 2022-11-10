package com.camel.routers;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.context.annotation.Configuration;

/**
 * 基于topic、CDC两种方式获取数据变动监听
 * 对比见url ： https://developer.salesforce.com/docs/atlas.en-us.api_streaming.meta/api_streaming/event_comparison.htm
 * 注意点:-2 to replay all events, or -1 to replay only new events,replay=-2会回放所有历史消息
 *
 * @author jt.gui
 */
@Configuration(proxyBeanMethods = false)
public class SalesforceCdcOrTopicRoute extends RouteBuilder {

    @Override
    public void configure() {
        //-2 to replay all events, or -1 to replay only new events
        //基于cdc 方式 https://developer.salesforce.com/docs/atlas.en-us.change_data_capture.meta/change_data_capture/cdc_what.htm
        from("salesforce:data/ChangeEvents?replayId=-1").log("receive of change events for Contact records");

        //基于topic 方式
        from("salesforce:contact?rawPayload=True&notifyForFields=ALL&notifyForOperationUpdate=True&sObjectName=Contact&sObjectClass=Contact&updateTopic=true&sObjectQuery=SELECT Id, Name, Email, Phone FROM Contact&defaultReplayId=-2")
                .unmarshal().json()
                .choice()
                .when(header("CamelSalesforceEventType").isEqualTo("created"))
                .log("New Salesforce contact was created: [ID:${body[Id]}, Name:${body[Name]}, Email:${body[Email]}, Phone: ${body[Phone]}]")
                .when(header("CamelSalesforceEventType").isEqualTo("updated"))
                .log("A Salesforce contact was updated: [ID:${body[Id]}, Name:${body[Name]}, Email:${body[Email]}, Phone: ${body[Phone]}]")
                .when(header("CamelSalesforceEventType").isEqualTo("undeleted"))
                .log("A Salesforce contact was undeleted: [ID:${body[Id]}, Name:${body[Name]}, Email:${body[Email]}, Phone: ${body[Phone]}]")
                .when(header("CamelSalesforceEventType").isEqualTo("deleted"))
                .log("A Salesforce contact was deleted: [ID:${body[Id]}]");
    }
}