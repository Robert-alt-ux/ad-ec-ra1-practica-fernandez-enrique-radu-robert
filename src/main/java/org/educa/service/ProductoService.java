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

    private BigDecimal calcularPrecioFinal(Producto producto){

        return producto.getPrecio()
                .subtract(producto.getPrecio()
                        .multiply(producto.getDescuento()
                                .divide(new BigDecimal(100))));
    }

    private BigDecimal calcularCoste(Producto producto){
        return producto.getCostes().getCostesEnvio().add(producto.getCostes().getCostesAlmacenaje());
    }

    private BigDecimal calcularBeneficio(Producto producto){

        return calcularPrecioFinal(producto).subtract(calcularCoste(producto));
    }

    public void exportSummary(String path, String fileXml) throws JAXBException, IOException {
        ArrayList<Producto> listaProductos = new ArrayList<>(productoDAO.getProductos(fileXml).getProducto());
        double beneficioTotal = 0;
        for (Producto producto : listaProductos) {
            beneficioTotal += calcularBeneficio(producto).doubleValue();
        }
        SummaryEntity summaryEntity = summmaryEntMaker(fileXml, beneficioTotal, listaProductos.size());
        // El primer año de este grado será el primero y el final que concatene Strings con '+' en java - Enrique
        productoDAO.writeFile(summaryEntity, new File(String.valueOf(new StringBuilder().append(path).append("result_junio2026.txt"))));
    }

    private SummaryEntity summmaryEntMaker(String fileXml, double beneficio, int numProductos) {
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


    }


}
