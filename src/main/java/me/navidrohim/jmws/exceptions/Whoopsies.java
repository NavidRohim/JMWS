package me.navidrohim.jmws.exceptions;

public class Whoopsies extends RuntimeException {
    public Whoopsies(String message) {
        super(message);
    }
}
