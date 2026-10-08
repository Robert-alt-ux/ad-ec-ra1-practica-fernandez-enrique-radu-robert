package org.educa.service;

import generated.Producto;
import generated.Productos;
import jakarta.xml.bind.JAXBException;
import org.educa.dao.ProductoDAO;
import org.educa.dao.ProductoDAOImpl;
import org.educa.entity.ProductoEntity;
import org.educa.entity.SummaryEntity;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class ProductoService {
    private final ProductoDAO productoDAO = new ProductoDAOImpl();

    /**
     * Calls ProductoDao.getProductos() to get an instance of Productos class and returns
     * a List of ProductoEntity. Each ProductoEntity it's processed with the help of this's class private methods.
     * @param fileXml The XML file that's meant to be unmarshalled.
     * @return List of ProductoEntity.
     * @throws JAXBException if the data persistence layer called method fails to read from the XML file.
     */
    public List<ProductoEntity> readFile(String fileXml) throws JAXBException {

        Productos productos = productoDAO.getProductos(fileXml);

        List<Producto> listaProductos = productos.getProducto();
        List<ProductoEntity> resultado = new ArrayList<>();

        for (Producto p : listaProductos) {

            ProductoEntity productoEntity = new ProductoEntity();

            productoEntity.setProducto(p);
            productoEntity.setPrecioFinal(calcularPrecioFinal(p));
            productoEntity.setCost(calcularCoste(p));
            productoEntity.setProfit(calcularBeneficio(p));


            resultado.add(productoEntity);
        }
        return resultado;
    }

    /**
     * Private method that's called from this ProductoService.readFile() instance. It serves
     * the function of calculate the final price of a ProductoEntity class instance from precio and descuento
     * attributes of the Producto class.
     * @param producto that this method processes.
     * @return A BigDecimal object which is the final price.
     */
    private BigDecimal calcularPrecioFinal(Producto producto){

        return producto.getPrecio()
                .subtract(producto.getPrecio()
                        .multiply(producto.getDescuento()
                                .divide(new BigDecimal(100))));
    }

    /**
     * Private method that's called from this ProductoService.readFile() instance. It serves
     * the function of calculate the cost of a ProductoEntity class instance from multiple
     * attributes of the Producto class.
     * @param producto that this method processes.
     * @return A BigDecimal object which is the cost.
     */
    private BigDecimal calcularCoste(Producto producto){
        return producto.getCostes().getCostesEnvio().add(producto.getCostes().getCostesAlmacenaje());
    }

    /**
     * Private method that's called from this ProductoService.readFile() instance. It serves
     * the function of calculate the benefit of a ProductoEntity class by calling the calcularCoste() method
     * of this class instance.
     * @param producto that this method processes.
     * @return A BigDecimal object which is the benefit.
     */
    private BigDecimal calcularBeneficio(Producto producto){

        return calcularPrecioFinal(producto).subtract(calcularCoste(producto));
    }

    /**
     * Calls a data persistence layer method (ProductoDao.getProductos()) that it's goal it's to unmarshall an XML file and then returns an instance
     * of Productos class. This method then generates a SummaryEntity with the data given from the previous method called,
     * then it gives it to ProductoDao.writeFile(), in order to create and manipulate the made-to-be text file.
     * @param path of the text file we want the data to be written.
     * @param fileXml, the path of the XML file to unmarshall.
     * @throws JAXBException if the DAO method fails to unmarshall.
     * @throws IOException if the DAO method fails to write or create the text file.
     */
    public void exportSummary(String path, String fileXml) throws JAXBException, IOException {
        ArrayList<Producto> listaProductos = new ArrayList<>(productoDAO.getProductos(fileXml).getProducto());
        double beneficioTotal = 0;
        for (Producto producto : listaProductos) {
            beneficioTotal += calcularBeneficio(producto).doubleValue();
        }
        SummaryEntity summaryEntity = summaryEntMaker(fileXml, beneficioTotal, listaProductos.size());
        // El primer año de este grado será el primero y el final que concatene Strings con '+' en java - Enrique
        productoDAO.writeFile(summaryEntity, new File(String.valueOf(new StringBuilder().append(path).append("result_junio2026.txt"))));
    }

    /**
     * Helps ProductoService.exportSummary() to create a SummaryEntity instance. Merely created to make .exportSummary() more comprehensible.
     * @param fileXml, passed as a String, later used as a path of a File class to analyze certain SummaryEntity attributes.
     * @param beneficio, as an attribute of a SummaryEntity class object.
     * @param numProductos, as the number of Producto in a Productos class.
     * @return SummaryEntity to .exportSummary().
     */
    private SummaryEntity summaryEntMaker(String fileXml, double beneficio, int numProductos) {
        File xmlFile = new File(fileXml);
        StringBuilder dateAndNameBuilder = new StringBuilder();
        SummaryEntity summaryEntity = new SummaryEntity();
        dateAndNameBuilder.append(dateMaker(fileXml));
        summaryEntity.setName(String.valueOf(dateAndNameBuilder));
        summaryEntity.setNumberOfProducts(numProductos);
        summaryEntity.setTotalProfit(BigDecimal.valueOf(beneficio));
        summaryEntity.setFileAbsolutePath(xmlFile.getAbsolutePath());
        summaryEntity.setFileName(String.valueOf(dateAndNameBuilder.insert(0, "result_")));
        summaryEntity.setFileSize(xmlFile.length());
        return summaryEntity;
    }

    /**
     * It helps summaryEntMaker() to set the name of a SummaryEntity class.
     * @param fileXml, as a String the method manipulates.
     * @return String,date that will be the SummaryEntity.name value of its instance.
     */
    private String dateMaker(String fileXml) {
        StringBuilder fechaBuilder = new StringBuilder();
        char[] fecha = fileXml.toCharArray();
        int underlineIndex = fileXml.lastIndexOf("_");
        int formatDotIndex = fileXml.lastIndexOf(".");
        for (int i = underlineIndex + 1; i < formatDotIndex; i++) {
            fechaBuilder.append(fecha[i]);
        }
        return String.valueOf(fechaBuilder);
    }

    /**
     * Calls ProductoService.readFile() to unmarshall the XML file given by parameter, then creates a File class
     * to give it to productoDAO.writeExcel() in order to create XLSX file in the data persistence layer.
     * @param path of the XLSX file we want to be created and written in.
     * @param fileXml of the XML file we want to be unmarshalled.
     * @throws JAXBException if something went wrong with unmarshalling process.
     * @throws IOException if the file cant be written on or be created.
     * @throws ParseException if something went wrong in the manipulation of the XLSX file.
     */
    public void exportExcel(String path, String fileXml) throws JAXBException, IOException, ParseException {
        List<ProductoEntity> productos = readFile(fileXml);
        File excelFile = new File(path, "export_" + dateMaker(fileXml) + ".xlsx");
        productoDAO.writeExcel(productos, excelFile);
    }
}
