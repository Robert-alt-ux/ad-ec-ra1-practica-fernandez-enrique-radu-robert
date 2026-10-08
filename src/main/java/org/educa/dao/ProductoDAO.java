package org.educa.dao;

import generated.Productos;
import jakarta.xml.bind.JAXBException;
import org.educa.entity.ProductoEntity;
import org.educa.entity.SummaryEntity;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface ProductoDAO {
    Productos getProductos(String fileXml) throws JAXBException;

    void writeFile(SummaryEntity summaryEntity, File file) throws IOException;

    void writeExcel(List<ProductoEntity> productos, File file) throws IOException;
}
