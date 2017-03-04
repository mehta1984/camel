package com.cubic.css.esb.cos;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.cubic.css.esb.cos.processor.MessageListenerProcessor;
import com.cubic.css.esb.cos.route.MessageListenerRouteBuilder;

/**
 * 
 * @author 200642
 *
 */
public class MessageListenerRouteBuilderTest extends CamelTestSupport {

	@Override
	protected JndiRegistry createRegistry() throws Exception {
		JndiRegistry registry = super.createRegistry();
		registry.bind("messageListenerProcessor", new MessageListenerProcessor());
		return registry;
	}

	@Override
	public String isMockEndpoints() {
		// override this method and return the pattern for which endpoints to
		// mock.
		// use * to indicate all
		return "*";
	}

	@Override
	public boolean isCreateCamelContextPerClass() {
	        return true;
	}

	@Override
	protected RouteBuilder createRouteBuilder() throws Exception {
		return new MessageListenerRouteBuilder();
	}

	@Test
	public void testMockAllEndpoints() throws Exception {
		getMockEndpoint("mock:result").expectedBodiesReceived("success");
		ByteArrayOutputStream out = new ByteArrayOutputStream();
	    ObjectOutputStream os = new ObjectOutputStream(out);
	    os.writeObject("Hello World");
		template.sendBody("rabbitmq://localhost:5672/cubic?queue=createOrder_q&routingKey=createOrder&autoDelete=false", out.toByteArray());
		assertMockEndpointsSatisfied();
		assertNotNull(context.hasEndpoint("rabbitmq://localhost:5672/cubic?queue=createOrder_q&routingKey=createOrder&autoDelete=false"));
		assertNotNull(context.hasEndpoint("mock:result"));
	}

}
