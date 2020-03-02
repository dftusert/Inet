package com.user.exceptions;

/**
 * Реализация своего типа ошибки
 * @author dftusert
 * @version 1.0
 * @since 1.0
 */
public class PRException extends Exception {
    /**
     * Конструктор
     * @param message сообщение об ошибке
     */
    public PRException(String message) { super("P " + message); }
}
