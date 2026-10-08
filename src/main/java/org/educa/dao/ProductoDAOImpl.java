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

    /**
     * Unmarshalls a XML file given by parameter. Uses an instance of GestorEventos class as an EventHandler.
     * @param filexml, the file we want to unmarshall.
     * @return An instance of a generated Productos given by JAXB API's call.
     * @throws JAXBException if something went wrong going trough the XML file.
     */
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

    /**
     * Creates the file specified by parameter, then it gets written with its SummaryEntity instance's data.
     * @param summaryEntity class the method gets it's info from.
     * @param file The file we want to write on.
     * @throws IOException if the file wasn't created nor found.
     */
    @Override
    public void writeFile(SummaryEntity summaryEntity, File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(summaryEntity.toPrint());
        }
    }

    /**
     * Creates an XLSX file with the data of the given list of ProductoEntity. It builds a workbook with a single sheet
     * and delegates the header and the product rows writing to this class's private methods writeHeaderRow() and
     * writeProductRows(). Once the content is written, it auto-sizes every column to fit its data and dumps the
     * workbook into the given file.
     * @param productos List of ProductoEntity whose data is going to be written, one row per element.
     * @param file The File where the XLSX workbook is going to be created and written.
     * @throws IOException if the file can't be created or written, or if the workbook fails to be dumped into it.
     */
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

            for (int i = 0; i < EXCEL_HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);


        }

    }

    /**
     * Private method that's called from this class's writeExcel() instance. It serves the function of
     * create the header row (the first row of the sheet) from the EXCEL_HEADERS constant, applying to each cell
     * the style made by createHeaderStyle().
     * @param workbook The XSSFWorkbook that's being written, needed to create the header style.
     * @param sheet The Sheet where the header row is created.
     * @param boldFont The Font that's going to be applied to the header cells.
     */
    private void writeHeaderRow(XSSFWorkbook workbook, Sheet sheet, Font boldFont) {
        Row headerRow = sheet.createRow(0);

        CellStyle headerStyle = createHeaderStyle(workbook, boldFont);

        for (int i = 0; i < EXCEL_HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(EXCEL_HEADERS[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    /**
     * Private method that's called from this class's writeHeaderRow() instance. It serves the function of
     * create the CellStyle of the header cells: bold font, centered content (horizontally and vertically)
     * and thin borders on all four sides.
     * @param workbook The XSSFWorkbook used to create the CellStyle.
     * @param boldFont The Font that's set to the style.
     * @return A CellStyle object which is the header style.
     */
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

    /**
     * Private method that's called from this class's writeExcel() instance. It serves the function of
     * write one row per ProductoEntity of the list, starting right below the header row. Every row contains,
     * in this order: codigo, numero de serie, precio, descuento (as a percentage), precio final, costes de envio,
     * costes de almacenaje and profit. Rows with an odd index get an alternate background color, and the numeric
     * cells get a money or percent format depending on the column.
     * @param workbook The XSSFWorkbook that's being written, needed to create the cell styles.
     * @param sheet The Sheet where the rows are created.
     * @param productos List of ProductoEntity whose data is written into the sheet.
     */
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

     /**
     * Private method that's called from this class's writeProductRows() instance. It serves the function of
     * create the base CellStyle of a product row: centered content (horizontally and vertically) and thin borders
     * on all four sides. If alternate is true, it also applies a solid light green background.
     * @param workbook The XSSFWorkbook used to create the CellStyle.
     * @param alternate true if the row must have the alternate background color, false otherwise.
     * @return A CellStyle object which is the base style of the row.
     */
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

    /**
     * Private method that's called from this class's writeProductRows() instance. It serves the function of
     * create a cell in the given column of the row, set a numeric value to it and apply the given style.
     * @param row The Row where the cell is created.
     * @param column The index of the column where the cell is created.
     * @param value The double value that's set to the cell.
     * @param style The CellStyle that's applied to the cell.
     * @return The Cell that has been created.
     */
    private Cell createNumberCell(Row row, int column, double value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
        return cell;
    }

    /**
     * Private method that's called from this class's writeProductRows() instance. It serves the function of
     * create a new CellStyle by cloning the given base style and setting to it a data format (money, percent...).
     * The base style is not modified.
     * @param workbook The XSSFWorkbook used to create the CellStyle and the DataFormat.
     * @param baseStyle The CellStyle that's cloned to keep its alignment, borders and background.
     * @param format The String pattern of the data format that's applied to the new style.
     * @return A CellStyle object which is the base style plus the given data format.
     */
    private CellStyle createNumberStyle(XSSFWorkbook workbook, CellStyle baseStyle, String format) {
        CellStyle style = workbook.createCellStyle();

        style.cloneStyleFrom(baseStyle);
        style.setDataFormat(workbook.createDataFormat().getFormat(format));

        return style;
    }


}
