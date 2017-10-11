package org.duoduochild.magic.magiccrawler.dataflow.sourceinput;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.duoduochild.magic.magiccrawler.db.MongoDBSupport;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

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

    public static void main(String[] args) throws InterruptedException {
        String rootUrl = "https://www.baidu.com";
        final String expectIndexTitle = "百度一下，你就知道";
        String keyword = "少儿培训";
        String searchInputId = "kw";
        final String expectTitleAfterSearch = "少儿培训";
        driver = new ChromeDriver();
        try {
            LOGGER.info("start searching");
            driver.get(rootUrl);
            Thread.sleep(5000);
            new WebDriverWait(driver, 5).until((Object arg0) -> {
                WebDriver d = (WebDriver) arg0;
                return d.getTitle().toLowerCase().startsWith(expectIndexTitle);
            });
            LOGGER.info(" Page title is: " + driver.getTitle());
            WebElement element = driver.findElement(By.id(searchInputId));
            element.sendKeys(keyword);
            Thread.sleep(5000);
            element.submit();
            new WebDriverWait(driver, 10).until((Object arg0) -> {
                WebDriver d = (WebDriver) arg0;
                boolean validWindow = d.getTitle().toLowerCase().startsWith(expectTitleAfterSearch);
                LOGGER.debug("validWindow=" + validWindow);
                return validWindow;
            });
            String page1Url = driver.getCurrentUrl();
            List<WebElement> elements = driver.findElements(By.xpath("//div[@id='page']/a"));
            savePageUrl(page1Url, elements);
            Thread.sleep(3000);
        } finally {
            driver.quit();
        }
    }

    private static void savePageUrl(String page1Url, List<WebElement> elements) {
        LOGGER.info("Find page item=" + elements.size());
        List<Document> documents = new LinkedList<>();
        Document page1 = new Document();
        page1.append("searchEngine", "baidu");
        page1.append("page", 1);
        page1.append("link", page1Url);
        documents.add(page1);
        for (WebElement e : elements) {
            String href = e.getAttribute("href");
            String pageTitle = e.getText();
            LOGGER.info("link=" + href);
            LOGGER.info("page=" + pageTitle);
            if (!pageTitle.contains("下一页")) {
                Document doc = new Document();
                doc.append("searchEngine", "baidu");
                doc.append("page", pageTitle);
                doc.append("link", href);
                documents.add(doc);
            }
        }
        MongoDBSupport.saveDocs("searchResultPages", documents);
    }


}
