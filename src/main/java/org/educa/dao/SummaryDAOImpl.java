package org.educa.dao;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.educa.entity.SummaryEntity;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SummaryDAOImpl implements SummaryDAO {

    @Override
    public ArrayList<SummaryEntity> getSummary(File filexml) throws JAXBException {
        try {
            List<SummaryEntity> listaEntity = new ArrayList<>();
            JAXBContext contextoSummary = JAXBContext.newInstance(SummaryEntity.class);
            Unmarshaller unmarshaller = contextoSummary.createUnmarshaller();
            unmarshaller.setSchema(
                    SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(new File("src/main/resources/xsd/inventario_junio2026.xsd")));
            unmarshaller.setEventHandler(new GestorEventos());
            return listaEntity.add((SummaryEntity) unmarshaller.unmarshal(f));
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeFile(File path) {

    }
}
