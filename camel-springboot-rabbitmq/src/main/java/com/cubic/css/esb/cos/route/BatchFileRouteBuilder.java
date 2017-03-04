package com.cubic.css.esb.cos.route;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.springframework.stereotype.Component;

@Component
public class BatchFileRouteBuilder extends RouteBuilder  {

	private DefaultCamelContext	 camel;
    private ProducerTemplate template;
    
	@Override
	public void configure() throws Exception{
		from("{{batch.csv.endpoint}}").routeId("batchCsvRoute")
		 .unmarshal().csv()
		 .log(LoggingLevel.INFO,"com.cubic.css.esb.cos.BatchFileRouteBuilder","${body}")
		 .split(body())
		 .to("bean:batchFileRouteBuilder?method=processCSV");
		
	}
	
	public void processCSV(Exchange exchange){
		
		log.info("Inside processCSV receivee body"+exchange);
		this.camel = (DefaultCamelContext) exchange.getContext();
	    template = camel.createProducerTemplate();
		List<String> list =  (ArrayList<String>)exchange.getIn().getBody();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
	    ObjectOutputStream os;
	    byte[] bytes = null;
		try {
			os = new ObjectOutputStream(out);
			os.writeObject(list);
			bytes = out.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		template.asyncSendBody("rabbitmq://localhost:5672/cubic?routingKey=updateOrder&declare=false&autoDelete=false",bytes);
	}
}