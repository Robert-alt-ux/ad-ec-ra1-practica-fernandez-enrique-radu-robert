package org.educa.dao;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.educa.entity.SummaryEntity;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.validation.SchemaFactory;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SummaryDAOImpl implements SummaryDAO {
    private static final String NEW_LINE = "\n";
    @Override
    public SummaryEntity getSummary(File filexml) throws JAXBException {
        try {
            JAXBContext contextoSummary = JAXBContext.newInstance(SummaryEntity.class);
            Unmarshaller unmarshaller = contextoSummary.createUnmarshaller();
            unmarshaller.setSchema(
                    SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(new File("src/main/resources/xsd/inventario_junio2026.xsd")));
            unmarshaller.setEventHandler(new GestorEventos());
            // Hay que probar esto
            return (SummaryEntity) unmarshaller.unmarshal(filexml);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeFile(SummaryEntity summaryEntity, File path) throws IOException {
        try (BufferedWriter escritor = new BufferedWriter(new FileWriter(path)))
        {
            StringBuilder lineaBuilder = new StringBuilder();
            StringBuilder fechaBuilder = new StringBuilder();
            if (path.createNewFile())
            {
                String nombreFichero = path.getName();
                char[] fecha = nombreFichero.toCharArray();
                int separador = nombreFichero.indexOf("_");
                int formato = nombreFichero.indexOf(".");
                for (int i = separador + 1; i < formato; i++)
                {
                    fechaBuilder.append(fecha[i]);
                }
                escritor.write(String.valueOf(lineaBuilder
                        .append("Fecha:").append(fechaBuilder).append(NEW_LINE)
                        .append("Numero de productos: ").append(summaryEntity.getNumberOfProducts()).append(NEW_LINE)
                                .append("BeneficioTotal: ").append(summaryEntity.getTotalProfit()).append(NEW_LINE)
                        .append("Ruta del fichero: ").append(path.getAbsolutePath()).append(NEW_LINE)
                        .append("Nombre del fichero: ").append(nombreFichero).append(NEW_LINE)
                        .append("Tamaño del fichero: ").append(path.length()).append(" bytes")
                ));
                escritor.newLine();
            }

        }
    }
}
