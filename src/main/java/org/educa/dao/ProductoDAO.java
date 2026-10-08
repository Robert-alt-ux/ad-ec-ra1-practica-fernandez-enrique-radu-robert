package org.educa.dao;

public interface ProductoDAO {
    public Productos readFile(String fileXml) throws JAXBException;
}
