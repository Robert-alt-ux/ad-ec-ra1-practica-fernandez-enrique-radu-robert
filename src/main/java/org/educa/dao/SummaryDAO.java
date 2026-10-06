package org.educa.dao;

import jakarta.xml.bind.JAXBException;
import org.educa.entity.SummaryEntity;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface SummaryDAO {
    public SummaryEntity getSummary(File filexml) throws JAXBException, SAXException;
    public void writeFile(SummaryEntity summaryEntity, File path) throws IOException;
}
