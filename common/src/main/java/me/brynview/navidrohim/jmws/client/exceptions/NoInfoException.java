package me.brynview.navidrohim.jmws.client.exceptions;

public class NoInfoException extends IllegalStateException {
    public NoInfoException() {
        super("getInfo() is null. Was the object created using createRemotely() before calling this?");
    }
}
