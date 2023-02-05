package com.example.camel.process;

import com.example.camel.model.CurrencyExchangeDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JsonToYamlProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        CurrencyExchangeDto currencyExchangeDto = exchange.getIn().getBody(CurrencyExchangeDto.class);
        System.out.println("CSV-Processor complete");
        String yamlString = convertToYaml(currencyExchangeDto);
        exchange.getIn().setBody(yamlString);
    }

    public static String convertToYaml(Object obj) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));
        String yaml = mapper.writeValueAsString(obj);
        System.out.println("yaml : " +yaml);
        return yaml;
    }
}
