package com.user.inet.core.webdriver.collector;

import com.user.inet.core.database.Database;
import com.user.inet.core.database.entity.Profile;
import com.user.inet.core.database.infocontainer.BaseInfo;
import com.user.inet.core.webdriver.driver.BaseDriver;
import com.user.support.Log;
import com.user.support.Utils;
import org.openqa.selenium.WebElement;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Класс сбора основной информации профиля
 * @author dftusert
 * @version 1.0
 * @since 1.0
 */
public class CollectInstInfo implements Collector {
    /**
     * driver одна из реализаций BaseDriver, позволяющих управлять веб-драйвером
     * db для работы с базой данных
     */
    private BaseDriver driver;
    private Database db;

    /**
     * Конструктор
     * @param driver driver одна из реализаций BaseDriver, позволяющих управлять веб-драйвером
     * @param db для работы с базой данных
     */
    public CollectInstInfo(BaseDriver driver, Database db) {
        Log.log(Log.levels.DEBUG, "START CollectInstInfo(driver: " + driver.toString() + ", db: " + db.toString() + ")");

        this.driver = driver;
        this.db = db;
    }

    /**
     * Сбор информации с профилей
     * @param profiles список профилей
     */
    private void collectBaseInfo(ArrayList<Profile> profiles) {
        Log.log(Log.levels.DEBUG, "START collectBaseInfo(profile: "+ profiles.toString() +")");

        BaseInfo baseInfo, dbBaseInfo;
            for (Profile profile : profiles) {
                baseInfo = collectBaseInfoForProfile(profile);
                try {
                    if(baseInfo == null) throw new Error("Не получилось получить основную информацию (info=null) по профилю " + profile.toString());
                    Log.log(Log.levels.INFO, "id в базе: " + baseInfo.getProfile().getId());
                    Log.log(Log.levels.INFO, "id в базе: " + baseInfo.getProfile().getPerson());
                    Log.log(Log.levels.INFO, "id в базе: " + baseInfo.getPublicationsCount());
                    Log.log(Log.levels.INFO, "id в базе: " + baseInfo.getSubscribersCount());
                    Log.log(Log.levels.INFO, "id в базе: " + baseInfo.getSubscriptionsCount());

                    dbBaseInfo = db.getLatestProfileInfo(baseInfo.getProfile());
                    Log.log(Log.levels.INFO, "Информация не поменялась: " + baseInfo.infoEquals(dbBaseInfo));
                    Log.log(Log.levels.INFO, "Аватар не поменялся: " + baseInfo.avatarEquals(dbBaseInfo));

                    if (db.noInfoForProfile(baseInfo.getProfile()) || !baseInfo.infoEquals(dbBaseInfo))
                        Log.log(Log.levels.INFO, "Информация добавлена: " + db.insertIntoInfo(baseInfo));
                    if (db.noAvatarForProfile(baseInfo.getProfile()) || !baseInfo.avatarEquals(dbBaseInfo))
                        Log.log(Log.levels.INFO, "Аватар добавлен: " + db.insertIntoAvatar(baseInfo));
                } catch (Exception ex) {
                    Log.log(Log.levels.ERROR, ex.getMessage());
                }
            }
    }

    /**
     * Сбор информации с конкретного профиля
     * @param profile профиль
     * @return основная информация о профилях
     */
    private BaseInfo collectBaseInfoForProfile(Profile profile) {
        Log.log(Log.levels.DEBUG, "START CollectInstInfo.collectBaseInfoForProfile(profile: "+ profile.toString() + ")");
        String baseURL = driver.getConf().get("site");

        String fileExtension, bcpMark, cacheDirectory = driver.getConf().get("cache-directory");
        byte[] avatar;
        WebElement element;
        int publications, subscribers, subscriptions, clearCacheDirectory = Integer.parseInt(driver.getConf().get("clear-cache-files"));
        URL imageURL;
        Log.log(Log.levels.INFO, "Получение данных профиля " + profile.getPerson());

        driver.navigate(baseURL + '/' + profile.getPerson());

        try {
            element = driver.getElementByCSSSelector(driver.getConf().get("page-locator--publications"));
            publications = Integer.parseInt(element.getText().replaceAll(" ", ""));

            element = driver.getElementByCSSSelector(driver.getConf().get("page-locator--subscribers"));
            subscribers = Integer.parseInt(element.getText().replaceAll(" ", ""));

            element = driver.getElementByCSSSelector(driver.getConf().get("page-locator--subscriptions"));
            subscriptions = Integer.parseInt(element.getText().replaceAll(" ", ""));

            element = driver.getElementByCSSSelector(driver.getConf().get("page-locator--avatar"));
        } catch(Exception ex) {
            Log.log(Log.levels.ERROR, ex.getMessage());
            return null;
        }

        try {
            imageURL = new URL(element.getAttribute("src"));
        } catch (MalformedURLException ex) {
            Log.log(Log.levels.ERROR, ex.getMessage());
            return null;
        }

        fileExtension = Utils.getImgExtensionFromURL(imageURL);
        bcpMark = Utils.getBcpMark();

        Log.log(Log.levels.INFO, "Сохранение аватара в кеш по пути " + cacheDirectory +
                '/' + profile.getPerson() + '/' + profile.getPerson() + bcpMark + '.' + fileExtension);

        try {
            avatar = Utils.saveImageToFileByURL(imageURL, cacheDirectory +
                    '/' + profile.getPerson(), profile.getPerson() +
                    bcpMark, fileExtension);
        } catch (IOException ex) {
            Log.log(Log.levels.ERROR, ex.getMessage());
            return null;
        }

        if(clearCacheDirectory == 1) {
            Utils.deleteFile(cacheDirectory +
                    '/' + profile.getPerson(), profile.getPerson() +
                    bcpMark, fileExtension);
            Log.log(Log.levels.INFO, "Файл аватара удален из кеша " + cacheDirectory +
                    '/' + profile.getPerson() + '/' + profile.getPerson() + bcpMark + '.' + fileExtension);
        }

        Log.log(Log.levels.INFO, "Получены данные профиля " + profile.getPerson());

        return new BaseInfo(profile, publications, subscribers, subscriptions, fileExtension, avatar);
    }

    /**
     * Сбор информации с профилей и занесение изменений в базу данных
     */
    @Override
    public void collect() {
        Log.log(Log.levels.DEBUG, "START CollectInstInfo.collect()");

        try {
            collectBaseInfo(db.getProfilesInfoCollect());
        } catch (SQLException ex) {
            Log.log(Log.levels.ERROR, ex.getMessage());
        }
    }
}
