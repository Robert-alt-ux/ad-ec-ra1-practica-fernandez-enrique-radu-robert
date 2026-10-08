package org.educa.dao;

import generated.Producto;
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

            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            Font normalFont = workbook.createFont();

            writeHeaderRow(workbook, sheet, boldFont);
            writeProductRows(workbook, sheet, productos);


            workbook.write(out);


        }

    }

    private void writeHeaderRow(XSSFWorkbook workbook, Sheet sheet, Font boldFont) {
        Row headerRow = sheet.createRow(0);

        CellStyle headerStyle = createHeaderStyle(workbook, boldFont);

        for (int i = 0; i < EXCEL_HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(EXCEL_HEADERS[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private CellStyle createHeaderStyle(XSSFWorkbook workbook, Font boldFont) {
        CellStyle style = workbook.createCellStyle();

        style.setFont(boldFont);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        return style;
    }

    private void writeProductRows(XSSFWorkbook workbook, Sheet sheet,
                                  List<ProductoEntity> productos) {

        for (int i = 0; i < productos.size(); i++) {

            ProductoEntity entity = productos.get(i);
            Producto producto = entity.getProducto();

            Row row = sheet.createRow(i + 1);

            CellStyle rowStyle = createProductStyle(workbook, i % 2 != 0);

            CellStyle moneyStyle = createNumberStyle(workbook, rowStyle, FORMAT_MONEY);
            CellStyle percentStyle = createNumberStyle(workbook, rowStyle, FORMAT_PERCENT);

            row.createCell(0).setCellValue(producto.getCodigo());
            row.createCell(1).setCellValue(producto.getNumeroSerie());

            createNumberCell(row, 2,
                    producto.getPrecio().doubleValue(),
                    moneyStyle);

            createNumberCell(row, 3,
                    producto.getDescuento().movePointLeft(2).doubleValue(),
                    percentStyle);

            createNumberCell(row, 4,
                    entity.getPrecioFinal().doubleValue(),
                    moneyStyle);

            createNumberCell(row, 5,
                    producto.getCostes().getCostesEnvio().doubleValue(),
                    moneyStyle);

            createNumberCell(row, 6,
                    producto.getCostes().getCostesAlmacenaje().doubleValue(),
                    moneyStyle);

            createNumberCell(row, 7,
                    entity.getProfit().doubleValue(),
                    moneyStyle);
        }
    }

    private CellStyle createProductStyle(XSSFWorkbook workbook, boolean alternate) {
        CellStyle style = workbook.createCellStyle();

        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        if (alternate) {
            style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }

        return style;
    }

    private Cell createNumberCell(Row row, int column, double value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
        return cell;
    }

    private CellStyle createNumberStyle(XSSFWorkbook workbook, CellStyle baseStyle, String format) {
        CellStyle style = workbook.createCellStyle();

        style.cloneStyleFrom(baseStyle);
        style.setDataFormat(workbook.createDataFormat().getFormat(format));

        return style;
    }


}
