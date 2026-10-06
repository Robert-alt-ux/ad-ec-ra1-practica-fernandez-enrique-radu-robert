package org.educa.service;

import jakarta.xml.bind.JAXBException;
import org.educa.dao.ProductoDAO;
import org.educa.dao.SummaryDAO;
import org.educa.dao.SummaryDAOImpl;
import org.educa.entity.ProductoEntity;
import org.educa.entity.SummaryEntity;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class ProductoService {
    private final ProductoDAO productoDAO = new ProductoDAOImpl();
    private final SummaryDAO summaryDAO = new SummaryDAOImpl();

    /***
     * (RETOCAR) Reads an XML File using JAXB.
     *
     * @param fileXml
     * @return List<ProductoEntity>
     * @throws JAXBException
     */
    public List<ProductoEntity> readFile(String fileXml) throws JAXBException {
            return productoDAO.getProductos(fileXml);
    }

    public void exportSummary(String path, String fileXml) throws JAXBException, IOException {
        //TODO: Implementar
    }

    public void exportExcel(String path, String fileXml) throws JAXBException, IOException, ParseException {
        //TODO: Implementar
    }
}
