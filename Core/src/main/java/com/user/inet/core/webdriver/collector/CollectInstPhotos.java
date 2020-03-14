package com.user.inet.core.webdriver.collector;

import com.user.inet.core.database.Database;
import com.user.inet.core.database.entity.Photos;
import com.user.inet.core.database.entity.Profile;
import com.user.inet.core.webdriver.driver.BaseDriver;
import com.user.support.Log;
import com.user.support.Utils;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Класс сбора фотографий профиля
 * @author dftusert
 * @version 1.0
 * @since 1.0
 */
public class CollectInstPhotos implements Collector {
    /**
     * driver одна из реализаций BaseDriver, позволяющих управлять веб-драйвером
     * db для работы с базой данных
     */
    private BaseDriver driver;
    private Database db;

    /**
     * Конструктор
     * @param driver одна из реализаций BaseDriver, позволяющих управлять веб-драйвером
     * @param db для работы с базой данных
     */
    public CollectInstPhotos(BaseDriver driver, Database db) {
        Log.log(Log.levels.DEBUG, "START CollectInstPhotos(driver: " + driver.toString() + ", db: " + db.toString() + ")");

        this.driver = driver;
        this.db = db;
    }

    /**
     * Сбор фотографий с профилей
     * @param profiles профили пользователей
     */
    public void collectImages(ArrayList<Profile> profiles)  {
        try {
            Log.log(Log.levels.DEBUG, "START Driver.collectImages(db: " + db.toString() + ')');
            for(Profile profile : profiles)
                collectImagesForProfile(profile);

        } catch(ClassCastException ex) {
            Log.log(Log.levels.ERROR, ex.getMessage());
        }
    }

    /**
     * Сбор фотографий конкретного пользователя
     * @param profile профиль пользователя
     */
    private void collectImagesForProfile(Profile profile) {
        Log.log(Log.levels.DEBUG, "START Driver.collectImagesForProfile(profile:" + profile.toString() + ')');
        try {
            String page = driver.getConf().get("site") + '/' + profile.getPerson();
            Log.log(Log.levels.INFO, "Получение страницы: " + page);
            driver.navigate(page);

            Log.log(Log.levels.INFO, "Получение фотографий пользователя " + profile.getPerson());

            ArrayList<WebElement> imageElements;
            try {
                imageElements = driver.getElementsByCSSSelector(driver.getConf().get("page-locator--imgs"));
            } catch (Exception ex) {
                Log.log(Log.levels.ERROR, ex.getMessage());
                return;
            }

            URL imageURL;
            byte[] writtenBlob;
            String fileExtension, bcpMark, cacheDirectory = driver.getConf().get("cache-directory");
            String saveToMain = db.getConf().get("insert-photos"), saveToTemp = db.getConf().get("insert-photos-temp");
            WebElement imgElement;
            ArrayList<Photos> diffPhotos;

            int imagesLoad, imagesLoaded = 0, diffPhotosCount = 0;
            int imagesLoadLimit = Integer.parseInt(driver.getConf().get("images-load-limit"));
            if(imagesLoadLimit < 0) imagesLoad = imageElements.size();
            else imagesLoad = Math.min(imagesLoadLimit, imageElements.size());
            for(int i = 0; i < imagesLoad; ++i) {
                imgElement = imageElements.get(i);
                imageURL = new URL(imgElement.getAttribute("src"));
                fileExtension = Utils.getImgExtensionFromURL(imageURL);
                bcpMark = Utils.getBcpMark();

                Log.log(Log.levels.INFO, "Сохранение фотографии в файл: " + cacheDirectory +
                        File.separator + profile.getPerson() + File.separator + profile.getPerson() + bcpMark + '.' + fileExtension);
                writtenBlob = Utils.saveImageToFileByURL(imageURL, cacheDirectory +
                        File.separator + profile.getPerson(), profile.getPerson() + bcpMark, fileExtension);

                if (Integer.parseInt(driver.getConf().get("clear-cache-files")) == 1) {
                    Log.log(Log.levels.INFO, "Удаление фотографии из кеша: " + cacheDirectory +
                            File.separator + profile.getPerson() + File.separator + profile.getPerson() + bcpMark + '.' + fileExtension);
                    Utils.deleteFile(cacheDirectory + File.separator + profile.getPerson(), profile.getPerson() + bcpMark, fileExtension);
                }

                if(!db.saveBlobImage(saveToTemp, profile, fileExtension, writtenBlob))
                    Log.log(Log.levels.ERROR, "Не получилось вставить фотографию для пользователя: " + profile.toString() + " ссылка на фотографию: " + imageURL.toString());
                else
                    Log.log(Log.levels.INFO, "Вставлена фотография пользователя: " + profile.toString() + " ссылка на фотографию: " + imageURL.toString());

                diffPhotos = db.getDiffPhotos(profile);
                diffPhotosCount = diffPhotos.size();
                for(Photos photos : diffPhotos) {
                    Log.log(Log.levels.INFO, "Добавление новой фотографии для пользователя " + profile.toString());
                    if(db.saveBlobImage(saveToMain, photos.getProfile(), photos.getPhotoExtension(), photos.getPhoto())) ++imagesLoaded;
                }
            }
            Log.log(Log.levels.INFO, "Профиль: " + profile.toString() +
                    ", загружено всего фотографий: " + imagesLoad + ", новых фотографий: " + diffPhotosCount  +
                    ", из них добавлено в таблицу: " + imagesLoaded);
            if(!db.deletePhotosTemp(profile))
                Log.log(Log.levels.ERROR, "Не получилось удалить данные пользователя " + profile.toString() + " из временной таблицы фотографий");
            else
                Log.log(Log.levels.INFO, "Удалены данные пользователя " + profile.toString() + " из временной таблицы фотографий");
        } catch(Exception ex) {
            Log.log(Log.levels.ERROR, ex.getMessage());
        }
    }

    /**
     * Сбор информации с профилей и занесение изменений в базу данных
     */
    @Override
    public void collect() {
        try {
            collectImages(db.getProfilesPhotosCollect());
        } catch (SQLException ex) {
            Log.log(Log.levels.ERROR, ex.getMessage());
        }
    }
}
