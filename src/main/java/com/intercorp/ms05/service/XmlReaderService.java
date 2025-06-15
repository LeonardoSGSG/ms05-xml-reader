package com.intercorp.ms05.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intercorp.ms05.model.Root;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.Objects;

@Service
public class XmlReaderService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public XmlReaderService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(initialDelay = 0, fixedRate = 180000)
    public void readXmlFiles() {
        File folder = new File("xml-testing");
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".xml"));

        if (files == null || files.length == 0) {
            System.out.println("No se encontraron archivos XML.");
            return;
        }

        for (File file : Objects.requireNonNull(files)) {
            try {
                JAXBContext context = JAXBContext.newInstance(Root.class);
                Unmarshaller unmarshaller = context.createUnmarshaller();
                Root root = (Root) unmarshaller.unmarshal(file);

                String firstname2 = root.getPerson().getFirstname2();
                if (firstname2 == null || firstname2.isBlank()) {
                    System.out.println("Archivo omitido (sin firstname2): " + file.getName());
                    continue;
                }

                Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
                NodeList dynamicNodes = doc.getElementsByTagName(firstname2);

                if (dynamicNodes.getLength() > 0) {
                    Element element = (Element) dynamicNodes.item(0);
                    String ageValue = element.getElementsByTagName("age").item(0).getTextContent();
                    root.setAge(Integer.parseInt(ageValue));
                }

                String json = objectMapper.writeValueAsString(root);
                System.out.println("JSON generado de " + file.getName() + ":");
                System.out.println(json);

                kafkaTemplate.send("kafka-topic-01", json);
                System.out.println("Enviado a Kafka: kafka-topic-01");

            } catch (Exception e) {
                System.out.println("Error al procesar el archivo: " + file.getName() + ": " + e.getMessage());
            }
        }
    }
}
