package com.gutied.project.reports;


import com.gutied.project.datasets.OpinionRange;
import com.gutied.project.mongodb.MongoDB;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.gutied.project.mongodb.HotelReviewDbMapper.tripAdvisorReviewCollection;
import static com.gutied.project.mongodb.HotelReviewDbMapper.tripAdvisorReviewCollectionKeys.quote;
import static com.gutied.project.mongodb.HotelReviewDbMapper.tripAdvisorReviewCollectionKeys.rank;

public class Opinions {

    private static final Logger LOG = LoggerFactory.getLogger(Opinions.class);

    private long[] counters;

    public Opinions() {
        counters = new long[OpinionRange.values().length];
        Arrays.stream(OpinionRange.values()).forEach(x -> counters[x.ordinal()] = 0);
    }

    private void writeOpinionsToFile() {
        DB mongoDb = MongoDB.getProjectDB();
        DBCollection hotelReviewCollection = mongoDb.getCollection(tripAdvisorReviewCollection);
        DBObject projection = new BasicDBObject(quote.toString(), 1);
        projection.put(rank.toString(), 1);

        List<DBObject> allQuotes = hotelReviewCollection.find(new BasicDBObject(), projection).toArray();

        allQuotes.stream().forEach(x -> {
            LOG.info("{} - {}", x.get(rank.toString()), x.get(quote.toString()));
            counters[OpinionRange.getRange((Double) x.get(rank.toString())).ordinal()]++;
        });
        LOG.info("Number of quotes: ", allQuotes.size());
        Arrays.stream(OpinionRange.values()).forEach(x -> LOG.info("{} {} quotes", counters[x.ordinal()], x.name()));
    }

    public static void main(String[] args) throws IOException {
        Opinions quoteDataSet = new Opinions();
        quoteDataSet.writeOpinionsToFile();
    }

}
