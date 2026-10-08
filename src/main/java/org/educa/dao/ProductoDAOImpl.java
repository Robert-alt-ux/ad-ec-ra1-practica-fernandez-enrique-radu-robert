package org.educa.dao;

import generated.Productos;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.educa.entity.ProductoEntity;
import org.educa.entity.SummaryEntity;
import org.educa.eventHandler.GestorEventos;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.validation.SchemaFactory;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ProductoDAOImpl implements ProductoDAO {

    private static final String SHEET_NAME = "Inventario";
    private static final String[] EXCEL_HEADERS = {"Codigo", "Número de Serie", "Precio", "Descuento",
            "Precio Final", "Costes Envío", "Costes Almacenaje", "Beneficio"};
    private static final String FORMAT_MONEY = "#,##0.00 \"€\"";
    private static final String FORMAT_PERCENT = "0.00%";

    @Override
    public Productos getProductos(String filexml) throws JAXBException {
        try {
            JAXBContext contextoEntity = JAXBContext.newInstance(Productos.class);
            Unmarshaller unmarshaller = contextoEntity.createUnmarshaller();
            unmarshaller.setSchema(
                    SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(new File("src/main/resources/xsd/inventario_junio2026.xsd")));
            unmarshaller.setEventHandler(new GestorEventos());
            return (Productos) unmarshaller.unmarshal(new File(filexml));
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeFile(SummaryEntity summaryEntity, File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(summaryEntity.toPrint());
        }
    }

    @Override
    public void writeExcel(List<ProductoEntity> productos, File file) throws IOException {



    }

    

}
