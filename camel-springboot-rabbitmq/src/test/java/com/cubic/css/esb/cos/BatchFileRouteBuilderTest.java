package com.cubic.css.esb.cos;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;

import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.hibernate.validator.internal.util.privilegedactions.GetClassLoader;
import org.junit.Test;
import org.springframework.stereotype.Component;

import com.cubic.css.esb.cos.route.BatchFileRouteBuilder;

@Component
public class BatchFileRouteBuilderTest extends CamelTestSupport {

	@Override
	public boolean isCreateCamelContextPerClass() {
		return true;
	}

	/**
	 * Specify the Route you want to test
	 */
	@Override
	protected RouteBuilder createRouteBuilder() throws Exception {
		PropertiesComponent pc = context.getComponent("properties", PropertiesComponent.class);
		pc.setLocation("classpath:application.properties");
		return new BatchFileRouteBuilder();
	}
	
	
	
	/**
	 * Add registry for Camel Route to use. 
	 */
	@Override
	protected JndiRegistry createRegistry() throws Exception {
		JndiRegistry registry = super.createRegistry();
		return registry;
	}

	
	 @Test
	 public void testWithRealFile() throws Exception {
		 
		 AdviceWithRouteBuilder adviceWithRouteBuilder = new AdviceWithRouteBuilder() {
		        @Override
		        public void configure() throws Exception {
		            // mock the for testing
		            interceptSendToEndpoint("bean:batchFileRouteBuilder?method=processCSV")
		                .skipSendToOriginalEndpoint()
		                .to("mock:catchRabbitMQMessages");
		        }
		    };
		context.getRouteDefinition("batchCsvRoute").adviceWith(context, adviceWithRouteBuilder); 
		 
	    MockEndpoint mockRabbitMQ = getMockEndpoint("mock:catchRabbitMQMessages");
	    URL url = getClass().getClassLoader().getResource("data/sample.csv");
	    FileInputStream fis = new FileInputStream(url.getFile());
	    context.start();
	    mockRabbitMQ.expectedMessageCount(160);
	    context.createProducerTemplate().sendBodyAndHeader("direct:batch:start",fis,"", "");
	    mockRabbitMQ.assertIsSatisfied();
	    context.stop();
	 }
}