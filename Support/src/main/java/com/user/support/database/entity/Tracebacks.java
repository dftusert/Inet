package com.user.support.database.entity;

/**
 * Класс сущности tracebacks
 * @author dftusert
 * @version 1.0
 * @since 1.0
 */
public class Tracebacks {
    /**
     * id идентификатор traceback
     * message traceback для сообщения message
     * traceback traceback-сообщение
     */
    private int id;
    private Messages message;
    private String traceback;

    /**
     * Конструктор
     * @param id идентификатор traceback
     * @param message traceback для сообщения message
     * @param traceback traceback-сообщение
     */
    public Tracebacks(int id, Messages message, String traceback) {
        this.id = id;
        this.message = message;
        this.traceback = traceback;
    }

    /**
     * Конструктор
     * @param message traceback для сообщения message
     * @param traceback traceback-сообщение
     */
    public Tracebacks(Messages message, String traceback) {
        this.id = -1;
        this.message = message;
        this.traceback = traceback;
    }

    /**
     * Получение идентификатора traceback
     * @return идентификатор traceback
     */
    public int getId() { return id; }

    /**
     * Получение сообщения
     * @return сообщение
     */
    public Messages getMessage() { return message; }

    /**
     * Получение traceback-сообщения
     * @return traceback-сообщение
     */
    public String getTraceback() { return traceback; }

    /**
     * Реализация toString
     * @return строка
     */
    @Override
    public String toString() {
        return "id: "  + id + ", message: " + message.toString() + ", traceback: " + traceback;
    }
}
