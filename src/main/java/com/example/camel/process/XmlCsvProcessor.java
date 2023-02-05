package com.example.camel.process;

import java.util.HashMap;
import java.util.List;

import org.apache.camel.Exchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.codeud.springcamelintegration.util.FileExchangeProcessorUtil;

@Component
public class XmlCsvProcessor {
	
    @Autowired
    FileExchangeProcessorUtil fileExchangeProcessorUtil;

    public void csvJavaTransformation(Exchange exchange) {
        List<List<String>> csvRecordList = (List<List<String>>) exchange.getIn().getBody();
        fileExchangeProcessorUtil.processFileExchange(csvRecordList, exchange);
    }

    public void xmlJavaTransformation(Exchange exchange) {
        HashMap<String, Object> xmlRecordMap = (HashMap<String, Object>) exchange.getIn().getBody();
        fileExchangeProcessorUtil.processFileExchange(xmlRecordMap, exchange);
    }


}
