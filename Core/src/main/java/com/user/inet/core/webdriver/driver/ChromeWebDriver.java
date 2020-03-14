package com.user.inet.core.webdriver.driver;

import com.user.inet.conf.Conf;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 * Класс конкретного веб-драйвера
 * @author dftusert
 * @version 1.0
 * @since 1.0
 */
public class ChromeWebDriver extends BaseDriver {
    /**
     * Конструктор
     * @param conf конфигурация веб-драйвера
     */
    public ChromeWebDriver(Conf conf) {
        super(conf);
        Proxy proxy = getProxy();
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.setAcceptInsecureCerts(true);

        if(Integer.parseInt(conf.get("headless")) == 1)
            chromeOptions.addArguments("--headless");

        if(proxy != null) {
            if(proxy.getHttpProxy() != null)  chromeOptions.addArguments("--proxy-server=http://" + proxy.getHttpProxy());
            else if (proxy.getSocksProxy() != null) chromeOptions.addArguments("--proxy-server=socks5://" + proxy.getSocksProxy());
        }

        setDriver(new ChromeDriver(chromeOptions));
    }
}
