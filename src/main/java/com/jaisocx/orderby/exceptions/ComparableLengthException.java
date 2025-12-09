package com.jaisocx.orderby.exceptions;

public class ComparableLengthException extends Exception {

    protected final String MESSAGE = "Wrong calculated Comparable lenght!";

    @Override
    public String getMessage() {
        return MESSAGE;
    }
}
