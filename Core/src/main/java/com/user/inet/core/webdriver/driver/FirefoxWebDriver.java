package com.user.inet.core.webdriver.driver;

import com.user.inet.conf.Conf;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

/**
 * Класс конкретного веб-драйвера
 * @author dftusert
 * @version 1.0
 * @since 1.0
 */
public class FirefoxWebDriver extends BaseDriver {
    /**
     * Конструктор
     * @param conf конфигурация веб-драйвера
     */
    public FirefoxWebDriver(Conf conf) {
        super(conf);
        Proxy proxy = getProxy();
        FirefoxOptions firefoxOptions = new FirefoxOptions();
        firefoxOptions.setAcceptInsecureCerts(true);

        if(Integer.parseInt(conf.get("headless")) == 1)
            firefoxOptions.setHeadless(true);

        if(proxy != null) {
            if (proxy.getHttpProxy() != null) {
                firefoxOptions.addPreference("network.proxy.type", 1);
                firefoxOptions.addPreference("network.proxy.http", proxy.getHttpProxy().split(":")[0]);
                firefoxOptions.addPreference("network.proxy.http_port", Integer.parseInt(proxy.getHttpProxy().split(":")[1]));
            } else if (proxy.getSocksProxy() != null) {
                firefoxOptions.addPreference("network.proxy.type", 1);
                firefoxOptions.addPreference("network.proxy.socks", proxy.getSocksProxy().split(":")[0]);
                firefoxOptions.addPreference("network.proxy.socks_port", Integer.parseInt(proxy.getSocksProxy().split(":")[1]));
                firefoxOptions.addPreference("network.proxy.socks_version", 5);
            }
        }

        setDriver(new FirefoxDriver(firefoxOptions));
    }
}
