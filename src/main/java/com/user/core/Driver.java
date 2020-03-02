package com.user.core;

import com.user.core.info.BaseInfo;
import com.user.core.dbdesc.Profile;
import com.user.exceptions.PRException;
import com.user.support.Config;
import com.user.support.Utils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.net.URL;
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
    HashMap<String, String> driverConfiguration;

    /**
     * Конструктор
     * @param config конфигурация и локаторы для веб-драйвера
     */
    Driver(HashMap<String, String> config) {
        driverConfiguration = config;
    }

    /**
     * Получение базовой информации о профилях
     * @param profiles список профилей
     * @return информация о профилях
     * @throws PRException ошибка чтения конфигурации
     * @throws IOException ошибка при формировании URL или работе с файлами
     */
    public ArrayList<BaseInfo> collectBaseInfo(ArrayList<Profile> profiles) throws PRException, IOException {
        String baseURL = Config.getValueByKey(driverConfiguration, "site");

        WebDriver driver;
        String fileExtension, bcpMark;
        byte[] avatar;
        WebElement element;
        int publications, subscribers, subscriptions;
        ArrayList<BaseInfo> baseInfo = new ArrayList<>();
        URL imageURL;

        for (Profile profile : profiles) {
            // @MARK:LOG
            Utils.log(Utils.logLevels.INFO, "Получение данных профиля " + profile.getPerson());

            driver = initializeWebDriver();
            driver.get(baseURL + '/' + profile.getPerson());

            element = getElementByCSSSelector(driver, Config.getValueByKey(driverConfiguration, "page-locator--publications"));
            publications = Integer.parseInt(element.getText().replaceAll(" ", ""));

            element = getElementByCSSSelector(driver, Config.getValueByKey(driverConfiguration, "page-locator--subscribers"));
            subscribers = Integer.parseInt(element.getText().replaceAll(" ", ""));

            element = getElementByCSSSelector(driver, Config.getValueByKey(driverConfiguration, "page-locator--subscriptions"));
            subscriptions = Integer.parseInt(element.getText().replaceAll(" ", ""));

            element = getElementByCSSSelector(driver, Config.getValueByKey(driverConfiguration, "page-locator--avatar"));

            imageURL = new URL(element.getAttribute("src"));
            fileExtension = Utils.getImgExtensionFromURL(imageURL);
            bcpMark = Utils.getBcpMark();

            // @MARK:LOG
            Utils.log(Utils.logLevels.INFO, "Сохранение аватара в кеш по пути " + Config.getValueByKey(driverConfiguration, "cache--directory") +
                    '/' + profile.getPerson() + '/' + profile.getPerson() + bcpMark + '.' + fileExtension);
            avatar = Utils.saveImageToFileByURL(imageURL, Config.getValueByKey(driverConfiguration, "cache--directory") +
                                                                      '/' + profile.getPerson(), profile.getPerson() +
                                                                      bcpMark, fileExtension);

            if(Integer.parseInt(Config.getValueByKey(driverConfiguration, "cache--clear-cache-files")) == 1) {
                Utils.deleteFile(Config.getValueByKey(driverConfiguration, "cache--directory") +
                        '/' + profile.getPerson(), profile.getPerson() +
                        bcpMark, fileExtension);
                // @MARK:LOG
                Utils.log(Utils.logLevels.INFO, "Файл аватара удален из кеша " + Config.getValueByKey(driverConfiguration, "cache--directory") +
                        '/' + profile.getPerson() + '/' + profile.getPerson() + bcpMark + '.' + fileExtension);
            }

            deinitializeWebDriver(driver);

            // @MARK:LOG
            Utils.log(Utils.logLevels.INFO, "Получены данные профиля " + profile.getPerson());

            baseInfo.add(new BaseInfo(profile, publications, subscribers, subscriptions, fileExtension, avatar));
        }
        return baseInfo;
    }

    /**
     * Получение элемента по CSS селектору
     * @param driver веб-драйвер
     * @param CSSSelector css селектор
     * @return элемент, соответствующий селектору
     * @throws PRException ошибка чтения конфигурации
     */
    private WebElement getElementByCSSSelector(WebDriver driver, String CSSSelector, Integer... timeoutVA) throws PRException {
        int timeout = timeoutVA.length > 0 ? timeoutVA[0] : Integer.parseInt(Config.getValueByKey(driverConfiguration, "driver--search-timeout"));
        @Deprecated
        WebDriverWait wait = new WebDriverWait(driver, timeout);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(CSSSelector)));
        return driver.findElement(By.cssSelector(CSSSelector));
    }

    /**
     * Инициализация веб-драйвера
     * @return инициализированный веб-драйвер
     * @throws PRException ошибка чтения конфигурации
     */
    private WebDriver initializeWebDriver() throws PRException, ClassCastException {
        //@MARK:LOG
        Utils.log(Utils.logLevels.INFO, "Создается веб-драйвер");

        Proxy proxy = getProxy();
        if(proxy != null) Utils.log(Utils.logLevels.INFO, "Будет использована полученная конфигурация прокси");

        FirefoxOptions firefoxOptions;
        ChromeOptions chromeOptions;
        switch(Config.getValueByKey(driverConfiguration, "driver")) {
            case "firefox":
                firefoxOptions = new FirefoxOptions();
                firefoxOptions.setAcceptInsecureCerts(true);

                if(Integer.parseInt(Config.getValueByKey(driverConfiguration, "driver--headless")) == 1)
                    firefoxOptions.setHeadless(true);

                if(proxy != null) {
                    if(proxy.getHttpProxy() != null)  {
                        firefoxOptions.addPreference("network.proxy.type", 1);
                        firefoxOptions.addPreference("network.proxy.http", proxy.getHttpProxy().split(":")[0]);
                        firefoxOptions.addPreference("network.proxy.http_port", Integer.parseInt(proxy.getHttpProxy().split(":")[1]));
                    }
                    else if (proxy.getSocksProxy() != null) {
                        firefoxOptions.addPreference("network.proxy.type", 1);
                        firefoxOptions.addPreference("network.proxy.socks", proxy.getSocksProxy().split(":")[0]);
                        firefoxOptions.addPreference("network.proxy.socks_port", Integer.parseInt(proxy.getSocksProxy().split(":")[1]));
                        firefoxOptions.addPreference("network.proxy.socks_version", 5);
                    }
                    else throw new PRException("Неизвестный тип прокси");
                }
                return new FirefoxDriver(firefoxOptions);

            case "chrome":
                chromeOptions = new ChromeOptions();
                chromeOptions.setAcceptInsecureCerts(true);

                if(Integer.parseInt(Config.getValueByKey(driverConfiguration, "driver--headless")) == 1)
                    chromeOptions.addArguments("--headless");

                if(proxy != null) {
                    if(proxy.getHttpProxy() != null)  chromeOptions.addArguments("--proxy-server=http://" + proxy.getHttpProxy());
                    else if (proxy.getSocksProxy() != null) chromeOptions.addArguments("--proxy-server=socks5://" + proxy.getSocksProxy());
                    else throw new PRException("Неизвестный тип прокси");
                }
                return new ChromeDriver(chromeOptions);
            /*
             * TODO:
             * Не получилось реализовать
             * при открытии профиля
             * веб-сервер отсылает ответ 301 (Moved Permanently) и редиректит на страницу входа/регистрации
             * может кто-нибудь знает как решить данную проблему ?
             */
            case "tor":
                //@MARK:LOG
                Utils.log(Utils.logLevels.WARNING, "Создается веб-драйвер, использующий tor, внимание! это крайне нежелателено");
                firefoxOptions = new FirefoxOptions();

                firefoxOptions.addPreference("network.proxy.type", Integer.parseInt(Config.getValueByKey(driverConfiguration, "tor--proxy-type")));
                firefoxOptions.addPreference("network.proxy.socks", Config.getValueByKey(driverConfiguration, "tor--socks"));
                firefoxOptions.addPreference("network.proxy.socks_port", Integer.parseInt(Config.getValueByKey(driverConfiguration, "tor--socks_port")));

                if(Integer.parseInt(Config.getValueByKey(driverConfiguration, "driver--headless")) == 1)
                    firefoxOptions.setHeadless(true);
                if(proxy != null) firefoxOptions.setProxy(proxy);

                return new FirefoxDriver(firefoxOptions);
            default: throw new PRException("Неверно задано значение driver в конфигурационном файле");
        }
    }

    /**
     * Получение настроек прокси разными способами в зависимости от значения driver--proxy-needed
     * @return null - прокси не нужен, proxy - настройки берутся из файла или с сайта
     * @throws PRException ошибки чтения конфигурации, ошибки веб-драйвера
     */
    private Proxy getProxy() throws PRException {
        Proxy proxy;
        // @MARK:LOG
        Utils.log(Utils.logLevels.INFO, "Проверка необходимости использования прокси...");
        switch (Integer.parseInt(Config.getValueByKey(driverConfiguration, "driver--proxy-needed"))) {
            case 0:
                // @MARK:LOG
                Utils.log(Utils.logLevels.INFO, "Прокси использован не будет");
                return null;
            case 1:
                proxy = new Proxy();
                // @MARK:LOG
                Utils.log(Utils.logLevels.INFO, "Настройки для прокси будут взяты из .properties файла");
                String proxyHost = Config.getValueByKey(driverConfiguration, "driver--proxy-host");
                String proxyPort = Config.getValueByKey(driverConfiguration, "driver--proxy-port");

                if (Config.getValueByKey(driverConfiguration, "driver--proxy-type")
                        .equals(Config.getValueByKey(driverConfiguration, "proxy--type-http"))) {
                    // @MARK:LOG
                    Utils.log(Utils.logLevels.INFO, "HTTP-прокси " + proxyHost + ':' + proxyPort);
                    proxy.setHttpProxy(proxyHost + ':' + proxyPort);
                    proxy.setSslProxy(proxyHost + ':' + proxyPort);
                    proxy.setProxyType(Proxy.ProxyType.MANUAL);
                    return proxy;
                } else if (Config.getValueByKey(driverConfiguration, "driver--proxy-type")
                        .equals(Config.getValueByKey(driverConfiguration, "proxy--type-socks5"))) {
                    // @MARK:LOG
                    Utils.log(Utils.logLevels.INFO, "SOCKS5-прокси " + proxyHost + ':' + proxyPort);
                    proxy.setSocksProxy(proxyHost + ':' + proxyPort);
                    proxy.setProxyType(Proxy.ProxyType.MANUAL);
                    proxy.setSocksVersion(5);
                    return proxy;
                } else throw new PRException("Не удалось загрузить конфигурацию прокси из настроек");
            case 2:
                proxy = new Proxy();
                int attemptNumber = 1, maxAttemptsNumber = Integer.parseInt(Config.getValueByKey(driverConfiguration, "proxy-list-site--max-attempts"));

                while (attemptNumber <= maxAttemptsNumber) {
                    // @MARK:LOG
                    Utils.log(Utils.logLevels.INFO, "Настройки для прокси будут получены с сайта списков проси " +
                            Config.getValueByKey(driverConfiguration, "proxy-list-site") + ", попытка " + attemptNumber + " из " + maxAttemptsNumber);
                    // @MARK:LOG
                    Utils.log(Utils.logLevels.INFO, "Запускается веб-драйвер для получения информации для прокси");
                    WebDriver driver = fastInitializeWebDriver();
                    driver.get(Config.getValueByKey(driverConfiguration, "proxy-list-site"));
                    WebElement element;

                    int siteLoadTimeout = Integer.parseInt(Config.getValueByKey(driverConfiguration, "driver--search-timeout-proxy"));
                    String host, port, type;
                    element = getElementByCSSSelector(driver, Config.getValueByKey(driverConfiguration, "proxy-list-site--locator-ip"), siteLoadTimeout);
                    host = element.getText();
                    element = getElementByCSSSelector(driver, Config.getValueByKey(driverConfiguration, "proxy-list-site--locator-port"), siteLoadTimeout);
                    port = element.getText();
                    element = getElementByCSSSelector(driver, Config.getValueByKey(driverConfiguration, "proxy-list-site--locator-type"), siteLoadTimeout);
                    type = element.getText();

                    // @MARK:LOG
                    Utils.log(Utils.logLevels.INFO, "Останавливается веб-драйвер для получения информации для прокси");
                    deinitializeWebDriver(driver);

                    // @MARK:LOG
                    Utils.log(Utils.logLevels.INFO, "Получены данные прокси: " + type + ' ' + host + ':' + port);

                    if (type.contains(Config.getValueByKey(driverConfiguration, "proxy--type-http")) &&
                        !type.equals("HTTPS")) {
                        // @MARK:LOG
                        Utils.log(Utils.logLevels.INFO, "HTTP-прокси " + host + ':' + port);
                        proxy.setHttpProxy(host + ':' + port);
                        proxy.setSslProxy(host + ':' + port);
                        proxy.setProxyType(Proxy.ProxyType.MANUAL);
                        return proxy;
                    } else if (type.contains(Config.getValueByKey(driverConfiguration, "proxy--type-socks5"))) {
                        // @MARK:LOG
                        Utils.log(Utils.logLevels.INFO, type + "-прокси " + host + ':' + port);
                        proxy.setSocksProxy(host + ':' + port);
                        proxy.setProxyType(Proxy.ProxyType.MANUAL);
                        proxy.setSocksVersion(5);
                        return proxy;
                    } else {
                        ++attemptNumber;
                        try { Thread.sleep(Integer.parseInt(Config.getValueByKey(driverConfiguration, "proxy-list-site--refresh-wait-time"))); }
                        catch (Exception ex) {
                            // @MARK:LOG
                            Utils.log(Utils.logLevels.INFO, "Прервано ожидание следующей попытки получения прокси");
                        }
                    }
                }
                throw new PRException("Превышено количество попыток обращения к сайту со списком прокси");
            default: throw new PRException("Неверно задан способ получения прокси");
        }
    }

    /**
     * Быстрая инициализация веб-драйвера, нужна в основном для парсинга сайтов списков прокси
     * @return инициализированный веб-драйвер
     * @throws PRException ошибка чтения конфигурации
     */
    private WebDriver fastInitializeWebDriver() throws PRException, ClassCastException {
        FirefoxOptions firefoxOptions;
        ChromeOptions chromeOptions;
        switch(Config.getValueByKey(driverConfiguration, "driver")) {
            case "firefox":
                firefoxOptions = new FirefoxOptions();
                firefoxOptions.setHeadless(true);
                return new FirefoxDriver(firefoxOptions);
            case "chrome":
                chromeOptions = new ChromeOptions();
                chromeOptions.addArguments("--headless");
                return new ChromeDriver();
            // см. initializeDriver case "tor"
            case "tor":
                //@MARK:LOG
                Utils.log(Utils.logLevels.WARNING, "Создается веб-драйвер, использующий tor, внимание! это крайне нежелателено");
                firefoxOptions = new FirefoxOptions();
                firefoxOptions.addPreference("network.proxy.type", Integer.parseInt(Config.getValueByKey(driverConfiguration, "tor--proxy-type")));
                firefoxOptions.addPreference("network.proxy.socks", Config.getValueByKey(driverConfiguration, "tor--socks"));
                firefoxOptions.addPreference("network.proxy.socks_port", Integer.parseInt(Config.getValueByKey(driverConfiguration, "tor--socks_port")));

                firefoxOptions.setHeadless(true);
                return new FirefoxDriver(firefoxOptions);
            default: throw new PRException("Неверно задано значение driver в конфигурационном файле");
        }
    }

    /**
     * Закрытие веб-драйвера
     * @param driver веб-драйвер
     */
    private void deinitializeWebDriver(WebDriver driver) {
        Utils.log(Utils.logLevels.INFO, "Останавливается веб-драйвер");
        driver.quit();
    }
}
