package org.duoduochild.magic.magiccrawler.dataflow.datainput;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.MongoCollection;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.assertj.core.api.Assertions;
import org.bson.Document;
import org.duoduochild.magic.magiccrawler.dao.MongoDBUtil;
import org.duoduochild.magic.magiccrawler.dataflow.datainput.model.Type;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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
            doc.append("$or", Arrays.asList(new Document("isAnalyzed", true), new Document("isAnalyzed", new Document("$exists", false))));
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
                List<WebElement> resultBlocks = driver.findElements(By.xpath("//div[@id='content_left']/div"));
                List<Document> pageItems = Lists.newArrayList();
                Map<String, AtomicInteger> patternCounter = Maps.newHashMap();
                for (WebElement block : resultBlocks) {
                    try {
                        if (block.getText().contains("广告")) {
                            LOGGER.debug("ad-1111");
                        }
                        readBaiduPageItem(block, patternCounter).ifPresent(outDoc -> {
                            LOGGER.debug("collect a doc=" + outDoc);
                            pageItems.add(outDoc);
                        });
                    } catch (Exception e) {
                        LOGGER.error("Fail to read page[" + urlNo + "] " + url, e);
                        throw e;
                    }
                }
                LOGGER.debug("stats=" + patternCounter);
                Assertions.assertThat(pageItems.size()).isGreaterThan(0).isLessThan(resultBlocks.size());
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


    private static Optional<Document> readBaiduPageItem(WebElement block, Map<String, AtomicInteger> patternCounter) {
        String cssClass = block.getAttribute("class");
        increasePatternCounter(patternCounter, cssClass);
        LOGGER.info("cssClass=" + cssClass);
        LOGGER.info("blockText=" + block.getText().replaceAll("\r\n", "").replaceAll("\n", ""));
        return ResultEntryProcessorFactory.newProcessor(resolveTypeFromCssClass(block, cssClass)).process(block);
    }

    private static Type resolveTypeFromCssClass(WebElement block, String cssClass) {
        Type type = Type.UNKOWN;
        if (cssClass.equals("result-op c-container xpath-log")) {
            LOGGER.debug("Baidu image search");
            type = Type.IMAGE;
        } else if (cssClass.equals("result c-container") || cssClass.contains("c-container")) {
            type = Type.PUBLIC;
            Assertions.assertThat(block.getText()).contains("百度快照");
        } else if (cssClass.contains("b_UBCc") || cssClass.contains("txhBDs") || cssClass.contains("KRTrBI") || cssClass.contains("WxONMU") || cssClass.contains("GoZxDR")) {
            type = Type.AD;
            Assertions.assertThat(block.getText()).contains("广告");
        } else {
            LOGGER.debug("Unknown content type");
        }
        return type;
    }

    private static void increasePatternCounter(Map<String, AtomicInteger> patternCounter, String cssClass) {
        AtomicInteger counter = patternCounter.get(cssClass);
        if (counter != null) {
            counter.incrementAndGet();
        } else {
            counter = new AtomicInteger();
        }
        patternCounter.put(cssClass, counter);
    }

}
