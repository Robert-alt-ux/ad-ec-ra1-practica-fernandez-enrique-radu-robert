package org.educa.dao;

import jakarta.xml.bind.JAXBException;
import org.educa.entity.SummaryEntity;
import org.xml.sax.SAXException;

import java.io.File;
import java.util.List;

public interface SummaryDAO {
    public List<SummaryEntity> getSummary(File filexml) throws JAXBException, SAXException;
    public void writeFile(File path);
}
