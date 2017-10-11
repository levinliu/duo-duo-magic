package org.duoduochild.magic.magiccrawler.dataflow.datainput.processor;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.duoduochild.magic.magiccrawler.db.MongoDBSupport;
import org.duoduochild.magic.magiccrawler.dataflow.datainput.model.Type;
import org.duoduochild.magic.magiccrawler.util.PrintException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.Optional;

/**
 * Created by levinliu on 2017/10/11
 * GitHub: https://github.com/levinliu
 * (Change file header on Settings -> Editor -> File and Code Templates)
 */
public class PublicResultEntryProcessor implements ResultEntryProcessor {
    private final Logger log = Logger.getLogger(PublicResultEntryProcessor.class);

    @Override
    public Optional<Document> process(WebElement resultEntry) {
        Document document = new Document("type", Type.PUBLIC.toString());
        WebElement title = resultEntry.findElement(By.tagName("h3"));
        try {
            document.append("title", title.getText());
            WebElement abstractText = resultEntry.findElement(By.cssSelector("div[class='c-abstract']"));
            document.append("abstract", abstractText.getText());
            WebElement image = resultEntry.findElement(By.xpath("//a[@class='c-img6']/img"));
            String imageSrc = image.getAttribute("src");
            String height = image.getAttribute("height");
            Document imageDoc = new Document();
            imageDoc.append("imageSrc", imageSrc);
            imageDoc.append("imageHeight", height);
            document.append("image", imageDoc);
        } catch (Exception e) {
            log.warn("suppress error on entry=" + title.getText(), e);
            recordProcessingError(title, e);
        }
        return Optional.of(document);
    }

    private void recordProcessingError(WebElement title, Exception e) {
        Document error = new Document("type", "dataInput");
        error.append("msg", title.getText());
        error.append("error", PrintException.print(e));
        MongoDBSupport.saveDoc("dataProcessingError", error);
    }
}
