package org.duoduochild.magic.magiccrawler;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class SearchBaidu {
    private static final Logger LOGGER = Logger.getLogger(SearchBaidu.class);

    static {
        System.setProperty("webdriver.chrome.driver",
                "/Users/mac/Documents/workspace/robot/browserdriver/chrome/mac64/chromedriver");
        System.setProperty("sun.net.client.defaultConnectTimeout", "95000");
        System.setProperty("sun.net.client.defaultReadTimeout", "95000");
    }

    public static WebDriver driver;

    public static void main(String[] args) {
        String rootUrl = "https://www.baidu.com";
        final String expectIndexTitle = "百度一下，你就知道";
        String keyword = "少儿培训";
        String searchInputId = "kw";
        final String expectTitleAfterSearch = "少儿培训";
        try {
            LOGGER.info("start testing");
            LOGGER.info(System.getProperty("webdriver.chrome.driver"));
            // specialDrive();
            driver = new ChromeDriver();
            // 让浏览器访问 Baidu
            driver.get(rootUrl);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            (new WebDriverWait(driver, 5)).until(new ExpectedCondition() {
                public Object apply(Object arg0) {
                    WebDriver d = (WebDriver) arg0;
                    return d.getTitle().toLowerCase().startsWith(expectIndexTitle);
                }
            });
            LOGGER.info(" Page title is: " + driver.getTitle());
            WebElement element = driver.findElement(By.id(searchInputId));
            element.sendKeys(keyword);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            element.submit();
            (new WebDriverWait(driver, 10)).until(new ExpectedCondition() {
                public Object apply(Object arg0) {
                    WebDriver d = (WebDriver) arg0;
                    boolean validWindow = d.getTitle().toLowerCase().startsWith(expectTitleAfterSearch);
                    LOGGER.debug("validWindow=" + validWindow);
                    return validWindow;
                }
            });
            List<WebElement> elementses = driver.findElements(By.xpath("//div[@id='page']/a"));
            LOGGER.info("Find page item=" + elementses.size());
            List<String> sourcePages = new LinkedList<String>();
            sourcePages.add(driver.getCurrentUrl());
            for (WebElement e : elementses) {
                String href= e.getAttribute("href");
                LOGGER.info("link=" + href);
                LOGGER.info("element=" + e.getText());
                sourcePages.add(href);
            }
            for(String page:sourcePages){
                processItemsOnPage(page);
            }
//            clickNextPage();
        } finally {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            driver.quit();
            System.exit(0);
        }
    }

    private static void processItemsOnPage(String page) {
        driver.get(page);
        handlePage();
    }

    private static void handlePage() {
        List<WebElement> titleList = driver
                .findElements(By
                        .xpath("//div[@id='content_left']/div[@class='result c-container ']/h3/a"));
        List<WebElement> abstractList = driver
                .findElements(By
                        .xpath("//div[@id='content_left']/div[@class='result c-container ']/div"));

        LOGGER.info("find result size=" + titleList.size());
        List<Result> resultList = new LinkedList<Result>();
        for (int i = 0; i < titleList.size(); i++) {
            WebElement title = titleList.get(i);
            WebElement substract = abstractList.get(i);
            String titleTxt = title.getText();
            LOGGER.info("get item title[" + (i + 1) + "]=" + titleTxt);
            String link = title.getAttribute("href");
            LOGGER.info("get item link=" + link);
            String abstractTxt = substract.getText();
            LOGGER.info("get abstract=" + abstractTxt);
            resultList.add(new Result(titleTxt, link, abstractTxt));
        }
        String resultHtml = toHtml(resultList);
        LOGGER.info(resultHtml);
//        List<WebElement> paginationButtons = driver.findElements(By
//                .xpath("//div[@id='page']/a"));
//        LOGGER.info("find paginationButtons size="
//                + paginationButtons.size());
//        for (int i = 0; i < paginationButtons.size(); i++) {
//            WebElement button = paginationButtons.get(i);
//            System.out
//                    .println("get title[" + (i + 1) + "]=" + button.getText());
//            String link = button.getAttribute("href");
//            LOGGER.info("get link=" + link);
//        }
    }

    private static String toHtml(List<Result> resultList) {
        StringBuilder resultHtml = new StringBuilder();
        for (Result result : resultList) {
            resultHtml.append(result.toString());
            resultHtml.append("<br/>");
        }
        return resultHtml.toString();
    }

}
