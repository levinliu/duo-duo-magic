package org.duoduochild.magic.magiccrawler.db;

import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class MongoDBUtil {
    private static final Logger LOGGER = Logger.getLogger(MongoDBUtil.class);
    private static final CompositeConfiguration CONFIG = new CompositeConfiguration();

    private static MongoClient mongoClient = null;

    static {
        try {
            CONFIG.addConfiguration(new XMLConfiguration(
                    MongoDBUtil.class.getClassLoader().getResource("mongodb-dev.xml")
                            .getPath()));
            createMongoDBClient();
        } catch (ConfigurationException e) {
            LOGGER.error("Fail to load mongo CONFIG", e);
            throw new RuntimeException("Fail to load mongo CONFIG", e);
        }
    }

    public static void main(String[] args) throws ConfigurationException {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(CONFIG.getString("mongoDB.dataDB"));
        MongoIterable<String> collections = mongoDatabase.listCollectionNames();
        for (String col : collections) {
            LOGGER.info("collection=" + col);
        }
        MongoCollection<Document> collection = mongoDatabase.getCollection("searchItems");
        FindIterable<Document> result = collection.find();
        for (Document doc : result) {
            LOGGER.info("doc=" + doc);
        }
        LOGGER.info("Connect to database successfully");
    }

    private static void createMongoDBClient() {
        LOGGER.info("initialise mongo client name=" + CONFIG.getString("mongoDB.name"));
        ServerAddress serverAddress = new ServerAddress(CONFIG.getString("mongoDB.url"), CONFIG.getInt("mongoDB.port"));
        List<ServerAddress> addresses = new ArrayList<>();
        addresses.add(serverAddress);
        MongoCredential credential = MongoCredential.createScramSha1Credential(CONFIG.getString("mongoDB.login"), CONFIG.getString("mongoDB.adminDB"), CONFIG.getString("mongoDB.password").toCharArray());
        List<MongoCredential> credentials = new ArrayList<>();
        credentials.add(credential);
        mongoClient = new MongoClient(addresses, credentials, setupClientOptions(CONFIG));
        LOGGER.debug("Create mongodb client=" + mongoClient);
    }


    public static MongoDatabase getDB() {
        return mongoClient.getDatabase(CONFIG.getString("mongoDB.dataDB"));
    }

    public static MongoDatabase getDB(String dbName) {
        if (dbName != null && !"".equals(dbName)) {
            MongoDatabase database = mongoClient.getDatabase(dbName);
            return database;
        }
        return null;
    }

    private static MongoClientOptions setupClientOptions(CompositeConfiguration config) {
        MongoClientOptions.Builder options = new MongoClientOptions.Builder();
        options.connectionsPerHost(config.getInt("mongoDB.poolOptions.connectionsPerHost"));
        options.connectTimeout(config.getInt("mongoDB.poolOptions.connectTimeout"));
        options.maxWaitTime(config.getInt("mongoDB.poolOptions.maxWaitTime"));
        options.socketTimeout(config.getInt("mongoDB.poolOptions.socketTimeout"));
        options.threadsAllowedToBlockForConnectionMultiplier(config.getInt("mongoDB.poolOptions.threadsAllowedToBlockForConnectionMultiplier"));
        options.writeConcern(WriteConcern.SAFE);
        return options.build();
    }

    public static void close() {
        if (mongoClient != null) {
            mongoClient.close();
            mongoClient = null;
        }
    }
}  