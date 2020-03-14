package com.user.support.database.entity;

/**
 * Класс сущности levels
 * @author dftusert
 * @version 1.0
 * @since 1.0
 */
public class Levels {
    private int id;
    private String level_name;

    /**
     * Конструктор
     * @param id id уровня
     * @param level_name название уровня
     */
    public Levels(int id, String level_name) {
        this.id = id;
        this.level_name = level_name;
    }

    /**
     * Получение id уровня
     * @return id уровня
     */
    public int getId() { return id; }

    /**
     * Получение названия уровня
     * @return название уровня
     */
    public String getLevelName() { return level_name; }

    /**
     * Реализация toString
     * @return строка
     */
    @Override
    public String toString() {
        return "id: " + id + ", level_name: " + level_name;
    }
}
