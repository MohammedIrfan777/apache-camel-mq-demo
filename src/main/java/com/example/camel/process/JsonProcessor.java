package com.example.camel.process;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

@Component
public class JsonProcessor implements Processor {
    ObjectMapper objectMapper =new ObjectMapper();

    @Override
    public void process(Exchange exchange) throws Exception {
        System.out.println("In JsonProcessor body :" +exchange.getIn().getBody());
        exchange.getIn().setBody(objectMapper.writeValueAsString(exchange.getIn().getBody()));
    }

}
