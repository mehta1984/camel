package com.cubic.css.esb.cos.processor;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.xml.ws.Holder;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.cubic.css.ws.cos.customerorder.v1.CreateOrder;
import com.cubic.css.ws.cos.customerorder.v1.CreateOrderResponse;
import com.cubic.css.ws.cos.customerorder.v1.CustomerOrder_Type;
import com.cubic.css.ws.cos.customerorder.v1.OrderItemGroups;
import com.cubic.css.ws.cos.customerorder.v1.PaymentAuthority;

@Component("createOrderProcessor")
public class CreateOrderProcessor implements Processor {

	private DefaultCamelContext	 camel;
    private ProducerTemplate template;
    
    private static final Logger LOG = LoggerFactory.getLogger(CreateOrderProcessor.class);
     
	@Override
	public void process(Exchange exchange) throws Exception {
		
		CreateOrder createOrder = exchange.getIn().getBody(CreateOrder.class);	
		this.camel = (DefaultCamelContext) exchange.getContext();
	    template = camel.createProducerTemplate();
	    

		Map<String, Object> headers = exchange.getIn().getHeaders();
		CreateOrderResponse createOrderResponse = new CreateOrderResponse();
		CustomerOrder_Type customerOrder_Type = new CustomerOrder_Type();
		customerOrder_Type.setCustomerId("122212");
		customerOrder_Type.setOrderId("122");
		createOrderResponse.setCustomerOrder(customerOrder_Type);

		exchange.getOut().setBody(createOrderResponse);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
	    ObjectOutputStream os = new ObjectOutputStream(out);
	    os.writeObject(createOrder);
	    
	    headers = new HashMap<String, Object>();
		headers.put("routingKey", "createOrder");
	    CompletableFuture<Object> futureTask =   template.asyncSendBody("rabbitmq://localhost:5672/cubic?routingKey=createOrder&declare=false&autoDelete=false",
	    		out.toByteArray());
	    		
	    LOG.info("futureTask" + futureTask.isDone());
	}
}