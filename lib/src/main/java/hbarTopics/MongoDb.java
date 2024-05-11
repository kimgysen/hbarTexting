package hbarTopics;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bson.Document;
import org.reactivestreams.Publisher;

import io.github.cdimascio.dotenv.Dotenv;

import com.mongodb.BasicDBObject;
import com.mongodb.ConnectionString;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoDatabase;

import helpers.OldSubscriberHelpers.ObservableSubscriber;
import helpers.OldSubscriberHelpers.OperationSubscriber;
import helpers.OldSubscriberHelpers.PrintDocumentSubscriber;

import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.result.InsertOneResult;



public class MongoDb 
{

	
	
    public static void main(String[] args) throws Throwable 
    {
    	
    	String mongoAdminKey = Dotenv.load().get("MONGO_ADMIN_KEY");
    	
    	ConnectionString connectionString = new ConnectionString("mongodb+srv://kitakazenodb:"+mongoAdminKey+"@cluster0.mqynd.mongodb.net/myFirstDatabase");
    	System.out.println(connectionString);
    	MongoClientSettings settings = MongoClientSettings.builder()
    	        .applyConnectionString(connectionString)
    	        .serverApi(ServerApi.builder()
    	            .version(ServerApiVersion.V1)
    	            .build())
    	        .build();
    	
    	System.out.println(""+connectionString);
    	MongoClient mongoClient = MongoClients.create(settings);
  
    		MongoDatabase database = mongoClient.getDatabase("kitakazenoaobara");
    		MongoCollection collection = database.getCollection("HBARquotes");
    		
            // drop all the data in it
    		/*
            ObservableSubscriber<Void> successSubscriber = new OperationSubscriber<>();
            collection.drop().subscribe(successSubscriber);
            successSubscriber.await();
    		*/
    		
    		Document document = new Document("timestamp", "2022-06-23 00:14:17.609")
    	               				 .append("HBAR_USD", 0.07109)
    	               				 .append("dd", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS ZZZ").parse("2017-01-25 09:28:04.041 UTC"))
    	               				 ;
    		
    		//System.out.println(""+document);
    		
/*

            ObservableSubscriber<InsertOneResult> insertOneSubscriber = new OperationSubscriber<>();
            collection.insertOne(document).subscribe(insertOneSubscriber);
            insertOneSubscriber.await();
    		
            // get it (since it's the only one in there since we dropped the rest earlier on)
            ObservableSubscriber<Document> documentSubscriber = new PrintDocumentSubscriber();
            collection.find().first().subscribe(documentSubscriber);
            documentSubscriber.await();

            String dateStr = "2017-01-25 09:28:04.041 UTC";
            Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS ZZZ").parse(dateStr);
            
            ObservableSubscriber<Document> findOneSubscriber = new PrintDocumentSubscriber();
            BasicDBObject filter = new BasicDBObject("dd", date);
            collection.find(filter).subscribe(findOneSubscriber);
            findOneSubscriber.await();
 */
    		
    		collection = database.getCollection("timestampTest");
    		
    		 document = new Document("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS ZZZ").parse("2017-01-25 09:28:04.041 UTC"))
      				 .append("HBAR_USD", 0.07109)
      				 ;
    		 
             ObservableSubscriber<InsertOneResult> insertOneSubscriber = new OperationSubscriber<>();
             collection.insertOne(document).subscribe(insertOneSubscriber);
             insertOneSubscriber.await();
             
             String dateStr = "2017-01-25 09:28:04.041 UTC";
             Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS ZZZ").parse(dateStr);
             
             ObservableSubscriber<Document> findOneSubscriber = new PrintDocumentSubscriber();
             BasicDBObject filter = new BasicDBObject("timestamp", date);
             collection.find(filter).subscribe(findOneSubscriber);
             findOneSubscriber.await();
             
    }

}
