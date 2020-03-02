package com.user.core.info;

import com.user.core.dbdesc.Profile;
import com.user.support.Utils;

/**
 * Класс, описывающий базовую собираемую информацию
 * @author dftusert
 * @version 1.0
 * @since 1.0
 */
public class BaseInfo {
    /**
     * profile профиль пользователя
     * publicationsCount количество публикаций
     * subscribersCount количество подписчиков
     * subscriptionsCount количество подписок
     * avatar аватар пользователя (blob)
     */
    private final Profile profile;
    private final int publicationsCount;
    private final int subscribersCount;
    private final int subscriptionsCount;
    private final String avatarType;
    private final byte[] avatar;

    /**
     * Конструктор
     * @param profile профиль пользователя
     * @param publicationsCount количество публикаций
     * @param subscribersCount количество подписчиков
     * @param subscriptionsCount количество подписок
     * @param avatarType расширение изображения аватара
     * @param avatar аватар пользователя
     */
    public BaseInfo(Profile profile, int publicationsCount, int subscribersCount, int subscriptionsCount, String avatarType, byte[] avatar) {
        this.profile = profile;
        this.publicationsCount = publicationsCount;
        this.subscribersCount = subscribersCount;
        this.subscriptionsCount = subscriptionsCount;
        this.avatarType = avatarType;
        this.avatar = avatar;
    }

    /**
     * Проверка на равенство экземпляров BaseInfo
     * @param obj экземпляр класса для сравнения
     * @return true - экземпляры равны, false - экземпляры не равны
     */
    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof BaseInfo)) return false;
        BaseInfo baseInfoObj = (BaseInfo) obj;

        boolean basePartIsEquals = this.publicationsCount == baseInfoObj.publicationsCount &&
                this.subscribersCount == baseInfoObj.subscribersCount &&
                this.subscriptionsCount == baseInfoObj.subscriptionsCount;


        return (basePartIsEquals && this.avatar == null && baseInfoObj.getAvatar() == null) ||
                (this.avatar != null && baseInfoObj.getAvatar()!= null && basePartIsEquals && Utils.isByteArraysEquals(this.avatar, baseInfoObj.getAvatar()));
    }

    /**
     * Проверка на равенство базовых полей BaseInfo
     * @param baseInfo экземпляр класса BaseInfo для сравнения
     * @return true - экземпляры равны, false - экземпляры не равны
     */
    public boolean infoEquals(BaseInfo baseInfo) {
        return this.publicationsCount == baseInfo.publicationsCount &&
                this.subscribersCount == baseInfo.subscribersCount &&
                this.subscriptionsCount == baseInfo.subscriptionsCount;
    }

    /**
     * Проверка на равенство аватаров BaseInfo
     * @param baseInfo экземпляр класса BaseInfo для сравнения
     * @return true - экземпляры равны, false - экземпляры не равны
     */
    public boolean avatarEquals(BaseInfo baseInfo) {
        return (this.avatar == null && baseInfo.getAvatar() == null) ||
                (this.avatar != null && baseInfo.getAvatar()!= null &&
                        Utils.isByteArraysEquals(this.avatar, baseInfo.getAvatar()));
    }

    /**
     * получение профиля пользователя
     * @return профиль пользователя
     */
    public Profile getProfile() {return profile; }

    /**
     * получение количества публикаций
     * @return количество публикаций пользователя
     */
    public int getPublicationsCount () { return publicationsCount; }

    /**
     * получение количества подписчиков
     * @return количество подписчиков
     */
    public int getSubscribersCount () { return subscribersCount; }

    /**
     * получение количества подписок
     * @return количество подписок
     */
    public int getSubscriptionsCount() { return subscriptionsCount; }

    /**
     * получение расширения изображения аватара
     * @return расширение изображения аватара
     */
    public String getAvatarType() { return avatarType; }

    /**
     * получение картинки аватара в виде символов
     * @return картинка аватара в виде символов
     */
    public byte[] getAvatar() { return avatar; }
}
