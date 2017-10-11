package org.duoduochild.magic.magiccrawler.query;

import com.mongodb.client.FindIterable;
import org.bson.Document;
import org.duoduochild.magic.magiccrawler.db.MongoDBUtil;

/**
 * Created by levinliu on 2017/10/8
 * GitHub: https://github.com/levinliu
 * (Change file header on Settings -> Editor -> File and Code Templates)
 */
public class DataQuery {
    public static void main(String[] args) {
        FindIterable<Document> resultPages = MongoDBUtil.getDB().getCollection("searchResultPages").find();
        for (Document page : resultPages) {
            System.out.println(page);
        }
        resultPages = MongoDBUtil.getDB().getCollection("processedResultUrls").find();
        for (Document page : resultPages) {
            System.out.println(page);
        }
    }
}
