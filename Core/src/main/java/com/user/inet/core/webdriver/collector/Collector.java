package com.user.inet.core.webdriver.collector;

/**
 * Интерфейс, используемый сборщиками информации
 * @author dftusert
 * @version 1.0
 * @since 1.0
 */
public interface Collector {
    /**
     * Сбор информации, реализуемый конкретным сборщиком информации (CollectInstInfo, CollectInstPhotos)
     */
    void collect();
}
