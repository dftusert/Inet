package com.user.core;

import com.user.core.db.Photos;
import com.user.core.db.Profile;
import com.user.support.Config;
import com.user.support.Log;
import com.user.support.Utils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.net.URL;
import java.security.InvalidParameterException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Класс работы с веб-драйвером
 * @author dftusert
 * @version 1.0
 * @since 1.0
 */
public class Driver {
    /**
     * driverConfiguration конфигурация и локаторы для веб-драйвера
     */
    private HashMap<String, String> driverConfiguration;

    /**
     * Конструктор
     * @param config конфигурация и локаторы для веб-драйвера
     */
    Driver(HashMap<String, String> config) {
        Log.log(Log.levels.DEBUG, "Driver(config:" + config.toString() + ')');
        driverConfiguration = config;
    }

    /**
     * Сбор фотографий с профилей
     * @param db соединение и настройки базы данных
     */
    public void collectImages(Database db)  {
        try {
            Log.log(Log.levels.DEBUG, "START Driver.collectImages(db:" + db.toString() + ')');

            Log.log(Log.levels.INFO, "Получение списка профилей");
            ArrayList<Profile> profiles = db.getProfiles();
            Log.log(Log.levels.DEBUG, "Профили получены: " + profiles.toString());
            for(Profile profile : profiles)
                collectImagesForProfile(db, profile);

        } catch(InvalidParameterException | ClassCastException | SQLException ex) {
            Log.log(Log.levels.CRITICAL, ex.getMessage());
        } finally {
            Log.log(Log.levels.DEBUG, "STOP Driver.collectImages");
        }
    }

    /**
     * Сбор фотографий конкретного пользователя
     * @param db соединение и настройки базы данных
     * @param profile профиль пользователя
     */
    private void collectImagesForProfile(Database db, Profile profile) {
        WebDriver driver = null;
        try {
            Log.log(Log.levels.DEBUG, "START Driver.collectImagesForProfile(db:" + db.toString() + ", profile:" + profile.toString() + ')');

            Log.log(Log.levels.INFO, "Инициализация веб-драйвера для профиля: " + profile.getPerson());
            driver = initializeWebDriver();
            Log.log(Log.levels.DEBUG, "Инициализирован веб-драйвер " + driver.toString() + " для профиля " + profile.getPerson());

            String page = Config.getValueByKey(driverConfiguration, "site") + '/' + profile.getPerson();
            Log.log(Log.levels.INFO, "Получение страницы: " + page);
            driver.get(page);

            Log.log(Log.levels.INFO, "Получение фотографий пользователя " + profile.getPerson());
            ArrayList<WebElement> imageElements = getElementsByCSSSelector(driver, Config.getValueByKey(driverConfiguration, "site--img-locator"));

            URL imageURL;
            byte[] writtenBlob;
            String fileExtension, bcpMark, cacheDirectory = Config.getValueByKey(driverConfiguration, "cache-directory");
            String saveToMain = Config.getValueByKey(db.getDatabaseConfiguration(), "insert-photos"), saveToTemp = Config.getValueByKey(db.getDatabaseConfiguration(), "insert-photos-temp");
            WebElement imgElement;
            ArrayList<Photos> diffPhotos;

            int imagesLoad, imagesLoaded = 0, diffPhotosCount = 0;
            int imagesLoadLimit = Integer.parseInt(Config.getValueByKey(driverConfiguration, "images-load-limit"));
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

                if (Integer.parseInt(Config.getValueByKey(driverConfiguration, "clear-cache-files")) == 1) {
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
            Log.log(Log.levels.CRITICAL, ex.getMessage());
        } finally {
            if(driver != null) deinitializeWebDriver(driver);
            Log.log(Log.levels.DEBUG, "STOP Driver.collectImagesForProfile");
        }
    }

    /**
     * Получение элементов по CSS селектору
     * @param driver веб-драйвер
     * @param CSSSelector css селектор
     * @return элемент, соответствующий селектору
     * @throws InvalidParameterException ошибка чтения конфигурации
     */
    private ArrayList<WebElement> getElementsByCSSSelector(WebDriver driver, String CSSSelector, Integer... timeoutVA) throws InvalidParameterException {
        Log.log(Log.levels.DEBUG, "START Driver.getElementsByCSSSelector(driver:" + driver.toString() + ", CSSSelector: " + CSSSelector + ", timeoutVA:<--->)");

        int timeout = timeoutVA.length > 0 ? timeoutVA[0] : Integer.parseInt(Config.getValueByKey(driverConfiguration, "esearch-timeout"));
        Log.log(Log.levels.DEBUG, "Установлен таймаут поиска элемента на странице, таймаут: " + timeout);

        Log.log(Log.levels.DEBUG, "Начат поиск элементов по селектору: " + CSSSelector + " драйвером: " + driver.toString());
        @Deprecated
        WebDriverWait wait = new WebDriverWait(driver, timeout);
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(CSSSelector)));
        Log.log(Log.levels.DEBUG, "Закончен поиск элементов по селектору: " + CSSSelector + " драйвером: " + driver.toString());

        Log.log(Log.levels.DEBUG, "RSTOP Driver.getElementByCSSSelector");
        return (ArrayList<WebElement>) driver.findElements(By.cssSelector(CSSSelector));
    }

    /**
     * Инициализация веб-драйвера
     * @return инициализированный веб-драйвер
     * @throws InvalidParameterException ошибка чтения конфигурации
     */
    private WebDriver initializeWebDriver() throws InvalidParameterException, ClassCastException {
        Log.log(Log.levels.DEBUG, "START Driver.initializeWebDriver()");

        switch(Config.getValueByKey(driverConfiguration, "driver")) {
            case "firefox":
                Log.log(Log.levels.INFO, "Будет использован geckodriver (firefox)");
                FirefoxOptions firefoxOptions;
                firefoxOptions = new FirefoxOptions();
                firefoxOptions.setAcceptInsecureCerts(true);

                if(Integer.parseInt(Config.getValueByKey(driverConfiguration, "headless")) == 1) {
                    Log.log(Log.levels.INFO, "Будет использован headless-режим");
                    firefoxOptions.setHeadless(true);
                }

                Log.log(Log.levels.DEBUG, "RSTOP Driver.initializeWebDriver");
                return new FirefoxDriver(firefoxOptions);

            case "chrome":
                Log.log(Log.levels.INFO, "Будет использован chromedriver (chrome)");
                ChromeOptions chromeOptions;
                chromeOptions = new ChromeOptions();
                chromeOptions.setAcceptInsecureCerts(true);

                if(Integer.parseInt(Config.getValueByKey(driverConfiguration, "headless")) == 1) {
                    Log.log(Log.levels.INFO, "Будет использован headless-режим");
                    chromeOptions.addArguments("--headless");
                }

                Log.log(Log.levels.DEBUG, "RSTOP Driver.initializeWebDriver");
                return new ChromeDriver(chromeOptions);

            default: throw new InvalidParameterException("Неверно задано значение driver в конфигурационном файле");
        }
    }

    /**
     * Закрытие веб-драйвера
     * @param driver веб-драйвер
     */
    private void deinitializeWebDriver(WebDriver driver) {
        Log.log(Log.levels.DEBUG, "START Driver.deinitializeWebDriver(driver: " + driver.toString() + ')');
        Log.log(Log.levels.INFO, "Закрытие веб-драйвера " + driver.toString());
        driver.quit();
        Log.log(Log.levels.DEBUG, "STOP Driver.deinitializeWebDriver");
    }
}
