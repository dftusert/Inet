package com.user.support;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.security.InvalidParameterException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Класс для дополнительных функций
 * @author dftusert
 * @version 1.0
 * @since 1.0
 */
public class Utils {
    /**
     * Получение расширения изображения
     * @param imageURL ссылка на изображения
     * @return расширение
     */
    public static String getImgExtensionFromURL(URL imageURL) {
        Log.log(Log.levels.DEBUG, "START Utils.getImgExtensionFromURL(imageURL:" + imageURL.toString() + ')');
        String fileExtension = imageURL.toString().substring(0, imageURL.toString().indexOf("?"));
        Log.log(Log.levels.DEBUG, "RSTOP Utils.getImgExtensionFromURL");
        return fileExtension.substring(fileExtension.lastIndexOf(".") + 1);
    }

    /**
     * Сохранение изображения, находящегося поссылке, в файл
     * @param imageURL ссылка на изображение
     * @param imagePath путь к изображению
     * @param imageName название изображения
     * @param imageExtension расширение изображения
     * @return записанные данные
     * @throws IOException проблемы с записью
     * @throws InvalidParameterException при проблемах с правами на создание директорий и удаление файлов из этих директорий
     */
    public static byte[] saveImageToFileByURL(URL imageURL, String imagePath, String imageName, String imageExtension) throws IOException, InvalidParameterException {
        Log.log(Log.levels.DEBUG, "START Utils.saveImageToFileByURL(imageURL:" + imageURL.toString() + ", imagePath:" + imagePath +
                ", imageName:" + imageName + ", imageExtension:" + imageExtension + ')');

        Log.log(Log.levels.INFO, "Получение фотографии: " + imageURL.toString());
        BufferedImage saveImage = ImageIO.read(imageURL);

        Log.log(Log.levels.INFO, "Подготовка директории для записи фотографии");
        prepareDirectoryToWriteFile(imagePath, imageName, imageExtension);

        Log.log(Log.levels.INFO, "Запись фотографии в файл");
        ImageIO.write(saveImage, imageExtension, new File(imagePath + File.separator + imageName + '.' + imageExtension));
        Log.log(Log.levels.DEBUG, "RSTOP Utils.saveImageToFileByURL");
        return getFileData(imagePath + File.separator + imageName + '.' + imageExtension);
    }

    /**
     * Считывание файла
     * @param file путь и имя файла
     * @return данные файла
     * @throws IOException проблемы чтения файла
     */
    public static byte[] getFileData(String file) throws IOException {
        try(FileInputStream fis = new FileInputStream(file)) {
            Log.log(Log.levels.DEBUG, "START Utils.getFileData(file:" + file + ')');
            Log.log(Log.levels.DEBUG, "RSTOP Utils.getFileData(file:" + file + ')');
            return fis.readAllBytes();
        }
    }

    /**
     * Подготовка к созданию и записи в файл
     * @param path директория для файла
     * @param name имя файла
     * @param extension расширение файла
     * @throws InvalidParameterException при проблемах с правами на создание директорий и удаление файлов из этих директорий
     */
    private static void prepareDirectoryToWriteFile(String path, String name, String extension) throws InvalidParameterException {
        Log.log(Log.levels.DEBUG, "START Utils.getFileData(path:" + path + ", name:" + name + ", extension:" + extension + ')');
        File directory = new File(path);
        if (!directory.exists())
            if(!directory.mkdirs()) throw new InvalidParameterException("Невозможно создать директорию " + directory.getAbsolutePath());
        File file = new File(path + File.separator + name + '.' + extension);
        if (file.exists())
            if(!file.delete()) throw new InvalidParameterException("Невозможно удалить файл " + file.getAbsolutePath());
        Log.log(Log.levels.DEBUG, "STOP Utils.getFileData");
    }

    /**
     * Специальная информация для добавления к имени файла для хранения снимков
     * @return .yyyy_MM_dd_HH_mm_ss_SSSSSSS.bcp
     */
    public static String getBcpMark() {
        Log.log(Log.levels.DEBUG, "START Utils.getBcpMark()");
        DateFormat time = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSSSSSS");
        Calendar calendar = Calendar.getInstance();
        Log.log(Log.levels.DEBUG, "RSTOP Utils.getBcpMark()");
        return '.' + time.format(calendar.getTime()) + ".bcp";
    }

    /**
     * Удаление файла
     * @param path путь к файлу
     * @param name имя файла
     * @param extension расширение файла
     * @throws InvalidParameterException проблемы с удалением файла (в большинстве случаев из-за прав)
     */
    public static void deleteFile(String path, String name, String extension) throws InvalidParameterException {
        Log.log(Log.levels.DEBUG, "START Utils.deleteFile(path:" + path + ", name:" + name + ", extension:" + extension);
        Log.log(Log.levels.INFO, "Удаление файла " + path + File.separator + name + '.' + extension);
        File directory = new File(path);
        if (!directory.exists())
            throw new InvalidParameterException("Невозможно найти директорию " + directory.getAbsolutePath());
        File file = new File(path + File.separator + name + '.' + extension);
        if (file.exists())
            if(!file.delete()) throw new InvalidParameterException("Невозможно удалить файл " + file.getAbsolutePath());
        Log.log(Log.levels.DEBUG, "STOP Utils.deleteFile");
    }

    /**
     * Получение временной метки для хранения информации о времени добавления данных в базу данных
     * @return временная метки
     */
    public static String getDbTimeMark() {
        Log.log(Log.levels.DEBUG, "START Utils.getDbTimeMark()");
        DateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSSSSSS");
        Calendar calendar = Calendar.getInstance();
        Log.log(Log.levels.DEBUG, "RSTOP Utils.getDbTimeMark()");
        return time.format(calendar.getTime());
    }
}
