package com.user.support.database.entity;

/**
 * Класс сущности messages
 * @author dftusert
 * @version 1.0
 * @since 1.0
 */
public class Messages {
    /**
     * id идентификатор сообщения
     * level уровень сообщения
     * message сообщение
     * timestamp временная метка сообщения
     */
    private int id;
    private Levels level;
    private String message;
    private String timestamp;

    /**
     * Конструктор
     * @param id идентификатор сообщения
     * @param level уровень сообщения
     * @param message сообщение
     * @param timestamp временная метка
     */
    public Messages(int id, Levels level, String message, String timestamp) {
        this.id = id;
        this.level = level;
        this.message = message;
        this.timestamp = timestamp;
    }

    /**
     * Конструктор
     * @param level уровень сообщения
     * @param message сообщение
     * @param timestamp временная метка
     */
    public Messages(Levels level, String message, String timestamp) {
        this.id = -1;
        this.level = level;
        this.message = message;
        this.timestamp = timestamp;
    }

    /**
     * Получение идентификатора сообщения
     * @return идентификатор сообщения
     */
    public int getId() { return id; }

    /**
     * Получение уровня сообщения
     * @return уровень сообщения
     */
    public Levels getLevel() { return level; }

    /**
     * Получение сообщения
     * @return сообщение
     */
    public String getMessage() { return message; }

    /**
     * Получение временной меьки сообщения
     * @return временная метка сообщения
     */
    public String getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return "id: " + id + ", level: " + level.toString() + ", message: " + message + ", timestamp: " + timestamp;
    }
}
