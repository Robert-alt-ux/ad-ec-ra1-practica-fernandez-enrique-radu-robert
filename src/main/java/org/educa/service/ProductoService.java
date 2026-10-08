package org.educa.service;

import jakarta.xml.bind.JAXBException;
import org.educa.entity.ProductoEntity;

import java.io.IOException;
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



    public void exportSummary(String path, String fileXml) throws JAXBException, IOException {
        //TODO: Implementar

    }

    public void exportExcel(String path, String fileXml) throws JAXBException, IOException, ParseException {
        //TODO: Implementar
    }
}
