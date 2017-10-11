package org.duoduochild.magic.magiccrawler.dao;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.util.Asserts;
import org.bson.Document;

import java.util.List;

/**
 * Created by levinliu on 2017/10/8
 * GitHub: https://github.com/levinliu
 * (Change file header on Settings -> Editor -> File and Code Templates)
 */
public class MongoDBSupport {
    public static void saveDoc(String collection, Document document) {
        Asserts.notBlank(collection, "collection");
        Asserts.notNull(document, "document shouldn't be null");
        MongoDBUtil.getDB().getCollection(collection).insertOne(document);
    }

    public static void saveDocs(String collection, List<Document> documents) {
        Asserts.notBlank(collection, "collection");
        Asserts.notNull(documents, "documents shouldn't be null");
        MongoDBUtil.getDB().getCollection(collection).insertMany(documents);
    }
}
