package com.user.inet.core.database.entity;

/**
 * Класс, описывающий таблицу Photos или Photos_temp
 * @author dftusert
 * @version 1.0
 * @since 1.0
 */
public class Photos {
    /**
     * id идентификатор записи
     * profile профиль пользователя
     * photo фотография пользователя
     * photoExtension расширение фотографии
     */
    private final int id;
    private final Profile profile;
    private final byte[] photo;
    private final String photoExtension;

    /**
     * Конструктор
     * @param id идентификатор записи
     * @param profile профиль пользователя
     * @param photoExtension расширение фотографии
     * @param photo фотография пользователя
     */
    public Photos(int id, Profile profile, String photoExtension, byte[] photo) {
        this.id = id;
        this.profile = profile;
        this.photo = photo;
        this.photoExtension = photoExtension;
    }

    /**
     * Получение профиля идентификатора записи
     * @return идентификатор записи
     */
    public int getId() { return id; }

    /**
     * Получение профиля пользователя
     * @return профиль пользователя
     */
    public Profile getProfile() { return profile; }

    /**
     * Получение фотографи пользователя
     * @return фотография пользователя
     */
    public byte[] getPhoto () { return photo; }

    /**
     * Получение расширения фотографии пользователя
     * @return расширение фотографии пользователя
     */
    public String getPhotoExtension () { return photoExtension; }

    /**
     * Реализачия toString
     * @return строка
     */
    @Override
    public String toString() {
        return "id: " + id + ", " + profile.toString();
    }
}