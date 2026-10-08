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

    private final ProductoDAO productoDAO = new ProductoDAOImpl();


    /***
     * (RETOCAR) Reads an XML File using JAXB.
     *
     * @param fileXml
     * @return List<ProductoEntity>
     * @throws JAXBException
     */
    public List<ProductoEntity> readFile(String fileXml) throws JAXBException {

        Productos productos = productoDAO.readFile(fileXml);

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
    }

    private BigDecimal calcularPrecioFinal(Producto producto){
        BigDecimal precioFinal = producto.getPrecio()
                .subtract(producto.getPrecio()
                        .multiply(producto.getDescuento()
                                .divide(new BigDecimal(100))));

        return precioFinal;
    }

    private BigDecimal calcularCoste(Producto producto){
        BigDecimal coste = producto.getCostes().getCostesEnvio().add(producto.getCostes().getCostesAlmacenaje());
        return coste;
    }

    private BigDecimal calcularBeneficio(Producto producto){
        BigDecimal beneficio = calcularPrecioFinal(producto).subtract(calcularCoste(producto));

        return beneficio;
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
