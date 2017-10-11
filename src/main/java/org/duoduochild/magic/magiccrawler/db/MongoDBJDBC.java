package org.duoduochild.magic.magiccrawler.db;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.apache.log4j.Logger;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import org.bson.Document;

public class MongoDBJDBC {
    private static final Logger LOGGER = Logger.getLogger(MongoDBJDBC.class);

    public static void main(String[] args) {
        ServerAddress serverAddress = new ServerAddress("localhost", 27017);
        List<ServerAddress> addrs = new ArrayList<>();
        addrs.add(serverAddress);
        MongoCredential credential = MongoCredential.createScramSha1Credential("duoduo_user", "admin", "mongo654321db".toCharArray());
        List<MongoCredential> credentials = new ArrayList<>();
        credentials.add(credential);
        MongoClient mongoClient = new MongoClient(addrs, credentials);
        MongoDatabase mongoDatabase = mongoClient.getDatabase("duoduodb");
        MongoIterable<String> collections = mongoDatabase.listCollectionNames();
        for (String col : collections) {
            LOGGER.info("collection=" + col);
        }
        MongoCollection<Document> collection = mongoDatabase.getCollection("testItem");
        FindIterable<Document> result = collection.find();
        for (Document doc : result) {
            LOGGER.info("doc=" + doc);
        }
        collection.insertOne(new Document("test", true));
        LOGGER.info("Connect to database successfully");
    }
}  