package com.cubic.css.esb.cos;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.Processor;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cxf.CxfEndpoint;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cubic.css.esb.cos.processor.CreateOrderProcessor;
import com.cubic.css.esb.cos.processor.GetOrderProcessor;
import com.cubic.css.esb.cos.processor.SoapFaultProcessor;
import com.cubic.css.esb.cos.route.CustomerOrderRouteBuilder;
import com.cubic.css.ws.cos.customerorder.v1.Address;
import com.cubic.css.ws.cos.customerorder.v1.CustomerOrder;
import com.cubic.css.ws.cos.customerorder.v1.CustomerOrder_Type;
import com.cubic.css.ws.cos.customerorder.v1.GetOrder;
import com.cubic.css.ws.cos.customerorder.v1.GetOrderResponse;
import com.cubic.css.ws.identifiers.v1.CommonDetails;

public class CustomerOrderRouteBuilderTest extends CamelTestSupport {
	
	private static final Logger LOG = LoggerFactory.getLogger(CustomerOrderRouteBuilderTest.class);
	
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
		return new CustomerOrderRouteBuilder();
	}
	
	
	
	/**
	 * Add registry for Camel Route to use. 
	 */
	@Override
	protected JndiRegistry createRegistry() throws Exception {
		JndiRegistry registry = super.createRegistry();
		
		final CxfEndpoint serviceEndpoint = new CxfEndpoint("cxf:bean:servicePublisher", context());
		serviceEndpoint.setServiceClass(com.cubic.css.ws.cos.customerorder.v1.CustomerOrder.class);
		serviceEndpoint.setAddress("/webservices");
		registry.bind("servicePublisher", serviceEndpoint);
		registry.bind("getOrderProcessor", new GetOrderProcessor());
		registry.bind("soapFaultProcessor", new SoapFaultProcessor()); 
		registry.bind("createOrderProcessor", new CreateOrderProcessor()); 
		return registry;
	}

	
	@Override
	public boolean isUseAdviceWith(){
		return true;
	}
	
	@Test
	public void GetOrder() throws Exception {
		 
		GetOrderResponse getOrderResponse = getOrderResponseMock();
		
		/* Mock the endpoint via AdvicewithRoute and using interceptSendToEndpoint */
		context.getRouteDefinition("route1").adviceWith(context, new AdviceWithRouteBuilder() {
		    @Override
		    public void configure() throws Exception {
		    	interceptSendToEndpoint("direct:GetOrder")
	                // skip sending to the real http when the detour ends
                	.process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                        	LOG.info("intercept route >>>");
                        	GetOrder getOrder = exchange.getIn().getBody(GetOrder.class);
                        	LOG.info("getOrder >>"+getOrder);
                        	exchange.getOut().setBody(getOrderResponse);
                        }
                    }).skipSendToOriginalEndpoint().to("mock:out");
		    }
		});
		
		//Start the camel context
		context.start();
		
		//Set expected result for output 
		MockEndpoint out = getMockEndpoint("mock:out");
		out.setExpectedMessageCount(1);
		out.message(0).body().isEqualTo(getOrderResponse);
		
		//Send message to route via template
		context.createProducerTemplate().sendBodyAndHeader("direct:start",getOrderRequestMock(),"operationName", "GetOrder");
		assertNotNull(context.hasEndpoint("direct:GetOrder"));
		assertNotNull(context.hasEndpoint("direct:start"));
		
		// compare the expected response with actual one.
		out.assertIsSatisfied();
		
		//stop the camel context 
		context.stop();
	}

	protected static CustomerOrder createCXFClient(String url) {
	        // we use CXF to create a client for us as its easier than JAXWS and works
	        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
	        factory.setServiceClass(CustomerOrder.class);
	        factory.setAddress(url);
	        return (CustomerOrder) factory.create();
	    }
	 
	private GetOrder getOrderRequestMock(){
		GetOrder getOrder = new GetOrder();
		getOrder.setCommonDetails(getCommonMock());
		getOrder.setOrderId("1234");
		
		return getOrder;
	}
	private GetOrderResponse getOrderResponseMock() {
		GetOrderResponse getOrderResponse = new GetOrderResponse();
		getOrderResponse.setCommonDetails(getCommonMock());
		getOrderResponse.setCustomerOrder(getCustomerOrdeResponserMock());
		return getOrderResponse;
	}

	private CommonDetails getCommonMock() {
		CommonDetails commonDetails = new CommonDetails();
		commonDetails.setUserIdentifier("mockuser");
		commonDetails.setAdditionalElements("additional-element");
		commonDetails.setCubicCorrelationIdentifier("correlation-id");
		commonDetails.setSourceIdentifier("source");

		GregorianCalendar gregorianCalendar = new GregorianCalendar();
		gregorianCalendar.setTime(new Date(System.currentTimeMillis()));
		commonDetails.setSourceEventTimeStamp(getXMLCalendarCurrentTime());

		return commonDetails;
	}

	private CustomerOrder_Type getCustomerOrdeResponserMock() {
		CustomerOrder_Type customerOrder = new CustomerOrder_Type();
		customerOrder.setChannelCode("channelCode");
		customerOrder.setCompletedDtm(null);
		customerOrder.setCreatedDtm(getXMLCalendarCurrentTime());
		customerOrder.setCurrentStatus("status");
		customerOrder.setCustomerId("customerId");
		customerOrder.setCustomerName("customerName");
		Address address = new Address();
		address.setAddress1("address1");
		address.setAddress2("address2");
		address.setCountry("AUS");
		address.setPostcode("2150");
		address.setSuburb("sydney");
		customerOrder.setDeliveryAddress(address);
		customerOrder.setEntitlementId(null);
		customerOrder.setOrderId("orderId");
		customerOrder.setOrderItemGroups(null);
		customerOrder.setOrderStatusHistory(null);
		customerOrder.setPaymentAuthority(null);
		customerOrder.setTotalAmountInCents(4000);
		return customerOrder;
	}

	private XMLGregorianCalendar getXMLCalendarCurrentTime() {
		GregorianCalendar gregorianCalendar = new GregorianCalendar();
		XMLGregorianCalendar xmlGrogerianCalendar = null;
		gregorianCalendar.setTime(new Date(System.currentTimeMillis()));
		try {
			xmlGrogerianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
		} catch (DatatypeConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return xmlGrogerianCalendar;
	}
}
