package org.duoduochild.magic.magiccrawler.dataflow.datainput.processor;

import org.bson.Document;
import org.openqa.selenium.WebElement;

import java.util.Optional;

/**
 * Created by levinliu on 2017/10/11
 * GitHub: https://github.com/levinliu
 * (Change file header on Settings -> Editor -> File and Code Templates)
 */
public interface ResultEntryProcessor {
    Optional<Document> process(WebElement resultEntry);
}
