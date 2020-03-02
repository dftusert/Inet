package com.user.core.dbdesc;

/**
 * Класс, описывающий таблицу Profile
 * @author dftusert
 * @version 1.0
 * @since 1.0
 */
public class Profile {
    /**
     * id идентификатор профиля в базе данных
     * person ник профиля
     */
    private final int id;
    private final String person;

    /**
     * Конструктор
     * @param id идентификатор профиля в базе данных
     * @param person ник профиля
     */
    public Profile(int id, String person) {
        this.id = id;
        this.person = person;
    }

    /**
     * Получение идентификатора профиля в базе данных
     * @return идентификатор профиля в базе данных
     */
    public int getId() { return id; }

    /**
     * Получение ника профиля
     * @return ник профиля
     */
    public String getPerson () { return person; }
}
