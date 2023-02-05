package com.example.camel.routes;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.model.dataformat.YAMLDataFormat;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.example.camel.model.CurrencyExchangeDto;
import com.example.camel.process.JsonToYamlProcessor;

@Component
public class FileRouter extends RouteBuilder {

	YAMLDataFormat yaml = new YAMLDataFormat();
	public static final String CONTENT_TYPE = "Content-Type";
	public static final String TEXT_CSV_VALUE = "text/csv";

	@Override
	public void configure() throws Exception {

		restConfiguration().host("localhost").port("9290").component("jetty").bindingMode(RestBindingMode.auto);

		rest("/file/{fileName}").post().routeId("rest-router").to("direct:content");

		from("direct:content").log("Data Details : Body : ${body} fileName  : ${header.name} ").choice()
				.when(header(CONTENT_TYPE).isEqualTo(MediaType.APPLICATION_XML_VALUE))
				.log("routing to direct:messageToXMLQueue").to("direct:messageToXMLQueue")
				.when(header(CONTENT_TYPE).isEqualTo(MediaType.APPLICATION_JSON_VALUE))
				.log("routing to direct:messageToJsonQueue").to("direct:messageToJsonQueue")
				.when(header(CONTENT_TYPE).isEqualTo(TEXT_CSV_VALUE)).log("routing to direct:messageToCSVQueue")
				.to("direct:messageToCSVQueue").otherwise().log("routing to direct:error")
				.to("direct:messageToDeadLetterQueue");

		from("direct:messageToJsonQueue").log("got this message from rabbit: ${body}").doTry()
				.unmarshal(new JacksonDataFormat(CurrencyExchangeDto.class)).process(new JsonToYamlProcessor())
				.log("Filename -: ${header.name}").log("Date -: ${date:now:yyyyMMddHHmmss}")
				.to("file:target/output/jsonToYaml?fileName=${header.name}_${date:now:yyyyMMddHHmmss}_jsonToYaml.yaml")
				.doCatch(Exception.class).log("json-route-error").process(exchange -> {
					String fileName = (String) exchange.getIn().getHeader("fileName");
					String errorFileName = fileName.substring(0, fileName.lastIndexOf(".")) + "-"
							+ System.currentTimeMillis() + "-error.txt";
					exchange.getIn().setHeader("errFileName", errorFileName);
				}).to("rabbitmq:amq.direct?queue=messageToDeadLetterQueue&routingKey=deadLetterRoutingKey").doFinally()
				.log("json-route-finally")
				.to("file:outputs?fileName=${header.fileName}-${header.currentTimeStamp}.yaml").end();

		from("direct:messageToCSVQueue").doTry().log("csv-router").unmarshal().csv()
				.bean("XmlCsvProcessor", "csvJavaTransformation").marshal(yaml)
				.to("file:target/output/csvToYaml?fileName=${header.name}_${date:now:yyyyMMddHHmmss}_cvsToYaml.yaml")
				.doCatch(Exception.class).log("csv-route-error").process(exchange -> {
					String fileName = (String) exchange.getIn().getHeader("fileName");
					String errorFileName = fileName.substring(0, fileName.lastIndexOf(".")) + "-"
							+ System.currentTimeMillis() + "-error.txt";
					exchange.getIn().setHeader("errFileName", errorFileName);
				}).to("rabbitmq:amq.direct?queue=messageToDeadLetterQueue&routingKey=deadLetterRoutingKey").doFinally()
				.log("csv-route-finally").to("file:outputs?fileName=${header.fileName}-${header.currentTimeStamp}.yaml")
				.end();

		from("direct:messageToXMLQueue")
				.routeId("xml-route").doTry().log("xml-route").unmarshal().jacksonXml()
				.bean("XmlCsvProcessor", "xmlJavaTransformation").marshal(yaml)
				.to("file:target/output/xmlToYaml?fileName=${header.name}_${date:now:yyyyMMddHHmmss}_xmlToYaml.yaml")
				.doCatch(Exception.class).log("xml-route-error").process(exchange -> {
					String fileName = (String) exchange.getIn().getHeader("fileName");
					String errorFileName = fileName.substring(0, fileName.lastIndexOf(".")) + "-"
							+ System.currentTimeMillis() + "-error.txt";
					exchange.getIn().setHeader("errFileName", errorFileName);
				}).to("rabbitmq:amq.direct?queue=messageToDeadLetterQueue&routingKey=deadLetterRoutingKey").doFinally()
				.log("xml-route-finally").to("file:outputs?fileName=${header.fileName}-${header.currentTimeStamp}.yaml")
				.end();

		from("rabbitmq:amq.direct?queue=direct:messageToDeadLetterQueue&autoDelete=false&routingKey=messageToCSVQueue&hostname=localhost&portNumber=5672&username=guest&password=guest")
				.setHeader(Exchange.FILE_NAME, simple("filename-${date:now:yyyyMMddHHmmss}-error.yaml"))
				.toD("file:outputs/error");

	}
}
