package com.cubic.css.esb.cos.route;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class MessageListenerRouteBuilder extends RouteBuilder  {

	@Override
	public void configure() throws Exception{
		from("rabbitmq://localhost:5672/cubic?queue=createOrder_q&routingKey=createOrder&autoDelete=false")
		.process("messageListenerProcessor").to("mock:result");
	}
}
