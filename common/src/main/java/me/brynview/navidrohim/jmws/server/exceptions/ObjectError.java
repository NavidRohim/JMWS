package me.brynview.navidrohim.jmws.server.exceptions;

public class ObjectError extends RuntimeException {
    public ObjectError(String errorMessage) {
        super(errorMessage);
    }
}
