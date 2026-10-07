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
    /***
     * (RETOCAR) Reads an XML File using JAXB.
     *
     * @param fileXml
     * @return List<ProductoEntity>
     * @throws JAXBException
     */
    public List<ProductoEntity> readFile(String fileXml) throws JAXBException {
        List<ProductoEntity> productoEntityList = new ArrayList<>();
        Productos productos = productoDAO.getProductos(fileXml);
        ArrayList<Producto> listaProductos = new ArrayList<>(productos.getProducto());
        for (Producto producto : listaProductos) {
            ProductoEntity productoEntity = createProductoEntity(producto);
            productoEntityList.add(productoEntity);
        }
        return productoEntityList;
    }

    private ProductoEntity createProductoEntity(Producto producto) {
        ProductoEntity productoEntity = new ProductoEntity();
        productoEntity.setProducto(producto);
        BigDecimal precioFinal = (producto.getPrecio().multiply(producto.getDescuento())).divide(BigDecimal.valueOf(100));
        BigDecimal coste = (producto.getCostes().getCostesAlmacenaje()).add(producto.getCostes().getCostesEnvio());
        productoEntity.setPrecioFinal(precioFinal);
        productoEntity.setCost(coste);
        productoEntity.setProfit(coste.subtract(precioFinal));
        return productoEntity;
    }

    public void exportSummary(String path, String fileXml) throws JAXBException, IOException {
        File xmlFile = new File(fileXml);
        ArrayList<Producto> listaProductos = new ArrayList<>(productoDAO.getProductos(fileXml).getProducto());
        SummaryEntity summaryEntity = new SummaryEntity();
        BigDecimal bigCosteTotal = BigDecimal.valueOf(0);
        BigDecimal bigPrecioFinal = BigDecimal.valueOf(0);
        double costeTotal = 0;
        double precioFinal = 0;
        for (Producto producto : listaProductos) {
            costeTotal += (bigCosteTotal.add(producto.getCostes().getCostesEnvio().add(producto.getCostes().getCostesAlmacenaje()))).doubleValue();
            precioFinal += (bigPrecioFinal.add(producto.getPrecio())).doubleValue();
        }
        StringBuilder dateAndNameBuilder = new StringBuilder();
        dateAndNameBuilder.append(dateMaker(fileXml));
        summaryEntity.setName(String.valueOf(dateAndNameBuilder));
        summaryEntity.setNumberOfProducts(listaProductos.size());
        summaryEntity.setTotalProfit(BigDecimal.valueOf(precioFinal - costeTotal));
        summaryEntity.setFileAbsolutePath(xmlFile.getAbsolutePath());
        summaryEntity.setFileName(String.valueOf(dateAndNameBuilder.insert(0, "result_")));
        summaryEntity.setFileSize(xmlFile.length());
        productoDAO.writeFile(summaryEntity, new File(String.valueOf(dateAndNameBuilder.insert(0, path).append(".txt"))));
    }

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

    public void exportExcel(String path, String fileXml) throws JAXBException, IOException, ParseException {
        //TODO: Implementar
    }
}
