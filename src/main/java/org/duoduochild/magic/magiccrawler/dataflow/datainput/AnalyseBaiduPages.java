package org.duoduochild.magic.magiccrawler.dataflow.datainput;

import com.mongodb.client.DistinctIterable;
import com.mongodb.client.MongoCollection;
import com.sun.javadoc.Doc;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.duoduochild.magic.magiccrawler.dao.MongoDBSupport;
import org.duoduochild.magic.magiccrawler.dao.MongoDBUtil;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class AnalyseBaiduPages {
    private static final Logger LOGGER = Logger.getLogger(AnalyseBaiduPages.class);

    static {
        System.setProperty("webdriver.chrome.driver",
                "/Users/mac/Documents/workspace/robot/browserdriver/chrome/mac64/chromedriver");
        System.setProperty("sun.net.client.defaultConnectTimeout", "95000");
        System.setProperty("sun.net.client.defaultReadTimeout", "95000");
    }

    public static WebDriver driver;

    public static void main(String[] args) throws InterruptedException {
        driver = new ChromeDriver();
        try {
            MongoCollection pagesCollection = MongoDBUtil.getDB().getCollection("searchResultPages");
            Document doc = new Document("searchEngine", "baidu");
            doc.append("$or", Arrays.asList(new Document("isAnalyzed", false), new Document("isAnalyzed", new Document("$exists", false))));
            DistinctIterable<String> pageUrls = pagesCollection.distinct("link", doc, String.class);
            int urlNo = 0;
            MongoCollection urlsCollection = MongoDBUtil.getDB().getCollection("processedResultUrls");
            for (String url : pageUrls) {
                urlNo++;
                LOGGER.info("open page by url=" + url + ",no=" + urlNo);
                driver.get(url);
                Thread.sleep(5000);
                new WebDriverWait(driver, 5).until((Object arg0) -> {
                    WebDriver d = (WebDriver) arg0;
                    boolean valid = d.getTitle().toLowerCase().startsWith("少儿培训_百度搜索");
                    if (!valid) LOGGER.debug("Invalid page title=" + d.getTitle());
                    return valid;
                });
                List<WebElement> itemBlocks = driver.findElements(By.xpath("//div[@id='content_left']/div"));
                List<Document> pageItems = new LinkedList<>();
                for (WebElement block : itemBlocks) {
                    try {
                        readBaiduPageItem(block).ifPresent(outDoc -> {
                            LOGGER.debug("collect a doc=" + outDoc);
                            pageItems.add(outDoc);
                        });
                    } catch (Exception e) {
                        LOGGER.error("Fail to read page[" + urlNo + "] " + url, e);
                        throw e;
                    }
                }
                saveResult(pagesCollection, urlsCollection, url, pageItems);
                captureScreen(urlNo, url);
            }
            Thread.sleep(3000);
        } finally {
            driver.quit();
        }
    }

    private static void captureScreen(int urlNo, String url) {
        try {
            File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            File file = new File("./screenshot/page" + urlNo + "_" + System.currentTimeMillis() + ".png");
            FileUtils.copyFile(scrFile, file);
            LOGGER.debug("Capture screen of url=" + url + " to path=" + file.getCanonicalPath());
        } catch (Exception e) {
            LOGGER.warn("Fail to capture screen of url=" + url + ",errorMsg=" + e.getMessage());
        }
    }

    private static void saveResult(MongoCollection pagesCollection, MongoCollection urlsCollection, String url, List<Document> pageItems) {
        urlsCollection.insertMany(pageItems);
        Document condition = new Document("link", url);
        condition.append("searchEngine", "baidu");
        Document update = new Document();
        update.append("$set", new Document("isAnalyzed", true));
        pagesCollection.updateOne(condition, update);
    }

    private static Optional<Document> readBaiduPageItem(WebElement block) {
        String cssClass = block.getAttribute("class");
        LOGGER.info("myCssClass=" + cssClass);
        LOGGER.debug("pageText=" + block.getText());
        String type = null;
        if (cssClass.equals("result c-container") || cssClass.contains("c-container")) {
            type = "content";
        } else if (cssClass.contains("b_UBCc") || cssClass.contains("txhBDs")) {
            type = "ad";
        } else {
            return Optional.empty();
        }
        Document document = new Document("type", type);
        WebElement title = block.findElement(By.tagName("h3"));
        LOGGER.info("h3Class=" + title.getAttribute("class"));
        document.append("title", title.getText());
        WebElement abstractText = block.findElement(By.xpath("//div[@class='c-abstract']"));
        document.append("abstract", abstractText.getText());
        WebElement image = block.findElement(By.xpath("//a[@class='c-img6']/img"));
        String imageSrc = image.getAttribute("src");
        String height = image.getAttribute("height");
        Document imageDoc = new Document();
        imageDoc.append("imageSrc", imageSrc);
        imageDoc.append("imageHeight", height);
        document.append("image", imageDoc);
        LOGGER.info("image, url=" + imageSrc + ",height=" + height);
        if (block.getText().contains("广告")) {
            LOGGER.debug("ad page");
            List<WebElement> hyperLinks = block.findElements(By.xpath("//div/a"));
            for (WebElement link : hyperLinks) {
                String linkCssClass = link.getAttribute("class");
                if ("D_qEJR".equals(linkCssClass) || "c-showurl".equals(linkCssClass)
                        || "cyqRMk".equals(linkCssClass)) {
                    String baiduRefUrl = link.getAttribute("href");
                    String webOriginUrl = link.getText();
                    document.append("refUrl", baiduRefUrl);
                    document.append("webOriginUrl", webOriginUrl);
                    LOGGER.info("baiduRefUrl=" + baiduRefUrl + ",webOriginUrl=" + webOriginUrl + ", text=" + block.getText().replaceAll("\"", ""));
                } else {
                    LOGGER.info("invalid link cssClass=" + link.getAttribute("class"));
                    LOGGER.info("invalid text=" + link.getText());
                }
            }
        } else {
            WebElement hyperLink = block.findElement(By.xpath("//div[@class='f13']/a"));
            String baiduRefUrl = hyperLink.getAttribute("href");
            String webOriginUrl = hyperLink.getText();
            document.append("refUrl", baiduRefUrl);
            document.append("webOriginUrl", webOriginUrl);
            LOGGER.info("baiduRefUrl=" + baiduRefUrl + ",webOriginUrl=" + webOriginUrl + ", text=" + block.getText().replaceAll("\"", ""));
        }
        return Optional.of(document);
    }

}
