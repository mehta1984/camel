package com.cubic.css.esb.cos.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.cxf.message.MessageContentsList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.cubic.css.ws.cos.customerorder.v1.GetOrder;
import com.cubic.css.ws.cos.customerorder.v1.GetOrderResponse;

@Component("getOrderProcessor")
public class GetOrderProcessor implements Processor {

	private static final Logger LOG = LoggerFactory.getLogger(GetOrderProcessor.class);
	private DefaultCamelContext camel;
	private ProducerTemplate template;

	@Override
	public void process(Exchange exchange) throws Exception {
		this.camel = (DefaultCamelContext) exchange.getContext();
		template = camel.createProducerTemplate();
		GetOrder getOrder = exchange.getIn().getBody(GetOrder.class);
		String orderId = getOrder.getOrderId();
		template.sendBody("log:com.cubic.css.esb.cos.GetOrderProcessor?level=INFO", orderId);

		MessageContentsList msgContentList;
		msgContentList = (MessageContentsList) template.requestBody("cxf://http://127.0.0.1:8080/cos/CustomerOrder?"
				+ "wsdlURL=http://127.0.0.1:8080/cos/CustomerOrder?WSDL" + "&defaultOperationName=GetOrder"
				+ "&serviceClass=com.cubic.css.ws.cos.customerorder.v1.CustomerOrder", getOrder);
		GetOrderResponse getOrderResponse = (GetOrderResponse) msgContentList.get(0);
		exchange.getOut().setBody(getOrderResponse);

	}
}
