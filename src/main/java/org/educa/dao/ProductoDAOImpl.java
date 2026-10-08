package org.educa.dao;

import generated.Productos;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.educa.entity.ProductoEntity;
import org.educa.entity.SummaryEntity;
import org.educa.eventHandler.GestorEventos;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.validation.SchemaFactory;
import java.io.*;
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

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             FileOutputStream out = new FileOutputStream(file)) {

            Sheet sheet = workbook.createSheet(SHEET_NAME);

            workbook.write(out);
        }

    }

    private void writeHeaderRow(XSSFWorkbook workbook, Sheet sheet, Font boldFont) {
        Row headerRow = sheet.createRow(0);

        for (int i = 0; i < EXCEL_HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(EXCEL_HEADERS[i]);
            cell.setCellStyle(createHeaderStyle(workbook, boldFont));
        }
    }

    private CellStyle createHeaderStyle(XSSFWorkbook workbook, Font boldFont) {
        CellStyle style = workbook.createCellStyle();

        style.setFont(boldFont);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        return style;
    }



}
