package com.user.inet.core.webdriver.driver;

import com.user.inet.conf.Conf;
import com.user.support.Log;
import org.openqa.selenium.By;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;

/**
 * Класс работы с драйвером
 * @author dftusert
 * @version 1.0
 * @since 1.0
 */
public class BaseDriver implements AutoCloseable {
    /**
     * driver веб-драйвер, инициализироваться должен в конструкторах производных от BaseDriver классах
     * conf конфигурация веб-драйвера
     */
    private WebDriver driver;
    private Conf conf;

    /**
     * Конструктор, protected т.к. должен быть доступен только производным классам, без них driver = null и смысла создавать через
     * этот конструктор нет
     * @param conf конфигурация веб-драйвера
     */
    protected BaseDriver(Conf conf) {
        Log.log(Log.levels.DEBUG, "START BaseDriver(conf: " + conf.toString() + ")");
        this.conf = conf;
    }

    /**
     * Переход на страницу
     * @param to URL-адрес страницы в виде строки
     */
    public void navigate(String to) {
        Log.log(Log.levels.DEBUG, "START BaseDriver.navigate(to: "+ to + ")");
        driver.get(to);
    }

    /**
     * Получение настроек прокси разными способами в зависимости от значения driver--proxy-needed
     * @return null - прокси не нужен, proxy - настройки берутся из файла или с сайта
     * @throws Error ошибки чтения конфигурации, ошибки веб-драйвера
     */
    public Proxy getProxy() throws Error {
        Log.log(Log.levels.DEBUG, "START BaseDriver.getProxy()");
        Proxy proxy;
        Log.log(Log.levels.INFO, "Проверка необходимости использования прокси...");

        switch (Integer.parseInt(conf.get("proxy-needed"))) {
            case 0:
                Log.log(Log.levels.INFO, "Прокси использован не будет");
                return null;
            case 1:
                proxy = new Proxy();
                Log.log(Log.levels.INFO, "Настройки для прокси будут взяты из .properties файла");
                String proxyHost = conf.get("proxy--host");
                String proxyPort = conf.get("proxy--port");
                String proxyType = conf.get("proxy--type");
                String proxyTypeHttp = conf.get("proxy--types-http");
                String proxyTypeSocks5 = conf.get("proxy--types-socks5");

                if (proxyType.equals(proxyTypeHttp)) {
                    Log.log(Log.levels.INFO, "HTTP-прокси " + proxyHost + ':' + proxyPort);
                    proxy.setHttpProxy(proxyHost + ':' + proxyPort);
                    proxy.setSslProxy(proxyHost + ':' + proxyPort);
                    proxy.setProxyType(Proxy.ProxyType.MANUAL);
                    return proxy;
                } else if (proxyType.equals(proxyTypeSocks5)) {
                    Log.log(Log.levels.INFO, "SOCKS5-прокси " + proxyHost + ':' + proxyPort);
                    proxy.setSocksProxy(proxyHost + ':' + proxyPort);
                    proxy.setProxyType(Proxy.ProxyType.MANUAL);
                    proxy.setSocksVersion(5);
                    return proxy;
                } else throw new Error("Не удалось загрузить конфигурацию прокси из настроек");
            default: throw new Error("Неверно задан способ получения прокси");
        }
    }

    /**
     * Получение элемента по CSS селектору
     * @param CSSSelector css селектор
     * @param timeoutVA таймаут поиска элементов
     * @return элемент, соответствующий селектору
     * @throws Error ошибка чтения конфигурации
     */
    public WebElement getElementByCSSSelector(String CSSSelector, Integer... timeoutVA) throws Error {
        Log.log(Log.levels.DEBUG, "START BaseDriver.getElementByCSSSelector(CSSSelector: " + CSSSelector + ")");
        int timeout = timeoutVA.length > 0 ? timeoutVA[0] : Integer.parseInt(conf.get("esearch-timeout"));
        @Deprecated
        WebDriverWait wait = new WebDriverWait(driver, timeout);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(CSSSelector)));
        return driver.findElement(By.cssSelector(CSSSelector));
    }

    /**
     * Получение элементов по CSS селектору
     * @param CSSSelector css селектор
     * @param timeoutVA таймаут поиска элементов
     * @return элемент, соответствующий селектору
     * @throws Error ошибка чтения конфигурации
     */
    public ArrayList<WebElement> getElementsByCSSSelector(String CSSSelector, Integer... timeoutVA) throws Error {
        Log.log(Log.levels.DEBUG, "START BaseDriver.getElementsByCSSSelector(CSSSelector: " + CSSSelector + ")");

        int timeout = timeoutVA.length > 0 ? timeoutVA[0] : Integer.parseInt(conf.get("esearch-timeout"));

        Log.log(Log.levels.DEBUG, "Начат поиск элементов по селектору: " + CSSSelector + " драйвером: " + driver.toString());
        @Deprecated
        WebDriverWait wait = new WebDriverWait(driver, timeout);
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(CSSSelector)));

        return (ArrayList<WebElement>) driver.findElements(By.cssSelector(CSSSelector));
    }

    /**
     * Получение конфигурации веб-драйвера
     * @return конфигурация веб-драйвера
     */
    public Conf getConf() { return conf; }

    /**
     * Установка веб-драйвера, используется производными классами для установки настроенного по-своему веб-драйвера
     * @param driver веб-драйвер
     */
    protected void setDriver(WebDriver driver) { this.driver = driver; }

    /**
     * Реализация close от AutoCloseable
     */
    @Override
    public void close() {
        Log.log(Log.levels.DEBUG, "START BaseDriver.close(driver: " + (driver == null ? null : driver.toString()) + ")");
        if(driver != null) driver.quit();
    }
}
