package org.educa.eventHandler;

import jakarta.xml.bind.ValidationEvent;
import jakarta.xml.bind.ValidationEventHandler;

public class GestorEventos implements ValidationEventHandler {
    public boolean handleEvent(ValidationEvent event) {
        System.err.println("Evento de validacion (" + event.getSeverity() + ")"
                + "[linea: " + event.getLocator().getLineNumber() + "]"
                + "[columna: " + event.getLocator().getColumnNumber() + "]"
                + event.getMessage());
        return (event.getSeverity() != ValidationEvent.FATAL_ERROR);
    }
}
