package com.cubic.css.esb.cos.route;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cxf.CxfComponent;
import org.apache.camel.component.cxf.CxfEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.cubic.css.ws.cos.customerorder.v1.UpdateOrder;
import com.cubic.css.ws.cos.customerorder.v1.UpdateOrderResponse;
import com.cubic.css.ws.errors.v1.BusinessFault_Exception;

@Component
public class CustomerOrderRouteBuilder  extends RouteBuilder  {
	
	private DefaultCamelContext	 camel;
    private ProducerTemplate template;

    
    @Bean
    public CxfEndpoint servicePublisher(final CamelContext camelContext) {
      final CxfComponent cxfComponent = new CxfComponent(camelContext);

      final CxfEndpoint serviceEndpoint = new CxfEndpoint("", cxfComponent);
      serviceEndpoint.setServiceClass(com.cubic.css.ws.cos.customerorder.v1.CustomerOrder.class);
      serviceEndpoint.setAddress("/webservices");
      return serviceEndpoint;
    }
    
	@Override
	public void configure() throws Exception{
		from("{{cxf.bean.cxfEndpoint}}").routeId("customerOrderRoute")
		.recipientList(simple("direct:${header.operationName}"));
		
		from("direct:GetOrder")
		.doTry()
		.log(LoggingLevel.INFO,"com.cubic.css.esb.cos.route.CustomerOrderRouteBuilder","operation: getTheOrder")
		.process("getOrderProcessor")
		.doCatch(Exception.class)
		.process("soapFaultProcessor").to("mock:GetOrderResult");
		
		
		from("direct:CreateOrder")
		.doTry()
		.log(LoggingLevel.INFO,"com.cubic.css.esb.cos.route.CustomerOrderRouteBuilder","operation: createOrder")
		.process("createOrderProcessor")
		.doCatch(Exception.class)
		.process("soapFaultProcessor");
		
		from("direct:UpdateOrder")
		.doTry()
		.log(LoggingLevel.INFO,"com.cubic.css.esb.cos.route.CustomerOrderRouteBuilder","operation: UpdateOrder")
		.to("bean:customerOrderRouteBuilder?method=updateOrder")
		.doCatch(Exception.class)
		.process("soapFaultProcessor");
		
	}
	
	
	public void updateOrder(Exchange exchange){
		log.info("Inside updateOrder receivee body"+exchange);
		this.camel = (DefaultCamelContext) exchange.getContext();
	    template = camel.createProducerTemplate();
	    
	    UpdateOrder updateOrder =  exchange.getIn().getBody(UpdateOrder.class);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
	    ObjectOutputStream os;
	    byte[] bytes = null;
		try {
			os = new ObjectOutputStream(out);
			os.writeObject(updateOrder);
			bytes = out.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		template.sendBodyAndHeader("rabbitmq://localhost:5672/cubic?routingKey=updateOrder&declare=false&autoDelete=false&queue=updateOrder_q",
				bytes, "rabbitmq.DELIVERY_MODE", "2");
		
		UpdateOrderResponse updateOrderResponse = new UpdateOrderResponse();
		updateOrderResponse.setOrderId(updateOrder.getOrderId());
		updateOrderResponse.setCommonDetails(updateOrder.getCommonDetails());
		exchange.getOut().setBody(updateOrderResponse);
	}
}
