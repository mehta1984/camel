package com.cubic.css.esb.cos.processor;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("messageListenerProcessor")
public class MessageListenerProcessor implements Processor{

	private static final Logger LOG = LoggerFactory.getLogger(MessageListenerProcessor.class);
	
	@Override
	public void process(Exchange exchange) throws Exception {
		byte[] body =  (byte[]) exchange.getIn().getMandatoryBody();
		
		ByteArrayInputStream in = new ByteArrayInputStream(body);
	    ObjectInputStream is = new ObjectInputStream(in);
	    Object object = is.readObject();
	    
	    if(object instanceof ArrayList){
	    	List list = (List) object;
	    	LOG.info("Listner input list>>>"+list);
	    }else{
	    	String message = new String(body);
	    	LOG.info("Listner input >>"+message);
	    }
		
		Map<String, Object> headers = exchange.getIn().getHeaders();
		for (Map.Entry<String, Object> header : headers.entrySet()){
			LOG.info("MessageListenerProcessor headers: " + header.getKey() + "  " + header.getValue() );
		}
		
		exchange.getOut().setBody("success");
		
	}
}
