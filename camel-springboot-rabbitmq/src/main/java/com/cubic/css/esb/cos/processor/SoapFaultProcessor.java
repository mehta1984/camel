package com.cubic.css.esb.cos.processor;

import org.apache.camel.CamelExecutionException;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

/**
 * 
 * @author 200642
 *
 */

@Component("soapFaultProcessor")
public class SoapFaultProcessor implements Processor {

	@Override
	public void process(Exchange exchange) throws Exception {
		CamelExecutionException camelExecutionException = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, org.apache.camel.CamelExecutionException.class);
		exchange.getOut().setFault(true);
		exchange.getOut().setBody(camelExecutionException.getCause());
		System.out.println("sfs");
	}

}
