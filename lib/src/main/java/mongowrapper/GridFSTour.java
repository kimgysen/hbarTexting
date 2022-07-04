package mongowrapper;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
/*
 * Copyright 2008-present MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
import com.mongodb.client.gridfs.model.GridFSDownloadOptions;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoDatabase;
import com.mongodb.reactivestreams.client.gridfs.GridFSBucket;
import com.mongodb.reactivestreams.client.gridfs.GridFSBuckets;

import helpers.SubscriberHelpers.*;
import io.github.cdimascio.dotenv.Dotenv;

import org.bouncycastle.util.encoders.Base64;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.reactivestreams.Publisher;



import static com.mongodb.client.model.Filters.eq;
import static helpers.PublisherHelpers.toPublisher;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * The GridFS code example see: https://mongodb.github.io/mongo-java-driver/3.1/driver/reference/gridfs
 */
public final class GridFSTour {
	
	
    public static String hex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte aByte : bytes) {
            result.append(String.format("%02x", aByte));
            // upper case
            // result.append(String.format("%02X", aByte));
        }
        return result.toString();
    }

    /**
     * Run this main method to see the output of this quick example.
     *
     * @param args takes an optional single argument for the connection string
     * @throws FileNotFoundException if the sample file cannot be found
     * @throws IOException if there was an exception closing an input stream
     * @throws NoSuchAlgorithmException 
     */
    public static void main(final String[] args) throws FileNotFoundException, IOException, NoSuchAlgorithmException {
        MongoClient mongoClient;

        if (args.length == 0) {
        	String mongoAdminKey = Dotenv.load().get("MONGO_ADMIN_KEY");
        	
        	ConnectionString connectionString = new ConnectionString("mongodb+srv://kitakazenodb:"+mongoAdminKey+"@cluster0.mqynd.mongodb.net/myFirstDatabase");
        	MongoClientSettings settings = MongoClientSettings.builder()
        	        .applyConnectionString(connectionString)
        	        .serverApi(ServerApi.builder()
        	            .version(ServerApiVersion.V1)
        	            .build())
        	        .build();
        	
        	//System.out.println(""+connectionString);
        	 mongoClient = MongoClients.create(settings);
        } else {
            mongoClient = MongoClients.create(args[0]);
        }

        // get handle to "mydb" database
        MongoDatabase database = mongoClient.getDatabase("mydb");
        
        /*
        ObservableSubscriber<Void> dropSubscriber = new OperationSubscriber<>();
        database.drop().subscribe(dropSubscriber);
        dropSubscriber.await();
        */
        
        GridFSBucket gridFSBucket = GridFSBuckets.create(database);

        /*
         * UploadFromPublisher Example
         */
        // Get the input publisher
        Publisher<ByteBuffer> publisherToUploadFrom = toPublisher(ByteBuffer.wrap("MongoDB Tutorial..".getBytes(StandardCharsets.UTF_8)));

        		
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update("MongoDB Tutorial..".getBytes());
        byte[] digest = md.digest();
        String myHash = new String (hex(digest));
        
        System.out.println(myHash);
        
        // Create some custom options
        GridFSUploadOptions options = new GridFSUploadOptions()
                .chunkSizeBytes(1024)
                .metadata(new Document("type", "presentation"));

        ObservableSubscriber<ObjectId> uploadSubscriber = new OperationSubscriber<>();
        gridFSBucket.uploadFromPublisher("mongodb-tutorial", publisherToUploadFrom, options).subscribe(uploadSubscriber);
        ObjectId fileId = uploadSubscriber.get().get(0);

        /*
         * Find documents
         */
        System.out.println("File names:");
        ConsumerSubscriber<GridFSFile> filesSubscriber = new ConsumerSubscriber<>(gridFSFile ->
                System.out.println(" - " + gridFSFile.getFilename()));
        gridFSBucket.find().subscribe(filesSubscriber);
        filesSubscriber.await();

        /*
         * Find documents with a filter
         */
        filesSubscriber = new ConsumerSubscriber<>(gridFSFile -> System.out.println("Found: " + gridFSFile.getFilename()));
        gridFSBucket.find(eq("metadata.contentType", "image/png")).subscribe(filesSubscriber);
        filesSubscriber.await();

        /*
         * DownloadToPublisher
         */
        ObservableSubscriber<ByteBuffer> downloadSubscriber = new OperationSubscriber<>();
        gridFSBucket.downloadToPublisher(fileId).subscribe(downloadSubscriber);
        Integer size = downloadSubscriber.get().stream().map(Buffer::limit).reduce(0, Integer::sum);
        System.out.println("downloaded file sized: " + size);

        /*
         * DownloadToStreamByName
         */
        GridFSDownloadOptions downloadOptions = new GridFSDownloadOptions().revision(0);
        downloadSubscriber = new OperationSubscriber<>();
        gridFSBucket.downloadToPublisher("mongodb-tutorial", downloadOptions).subscribe(downloadSubscriber);
        size = downloadSubscriber.get().stream().map(Buffer::limit).reduce(0, Integer::sum);
        System.out.println("downloaded file sized: " + size);

        /*
         * Rename
         */
        OperationSubscriber<Void> successSubscriber = new OperationSubscriber<>();
        gridFSBucket.rename(fileId, "mongodbTutorial").subscribe(successSubscriber);
        successSubscriber.await();
        System.out.println("Renamed file");

        /*
         * Delete
         */
        /*
        successSubscriber = new OperationSubscriber<>();
        gridFSBucket.delete(fileId).subscribe(successSubscriber);
        successSubscriber.await();
        System.out.println("Deleted file");
         */
        
        // Final cleanup
        /*
        successSubscriber = new OperationSubscriber<>();
        database.drop().subscribe(successSubscriber);
        successSubscriber.await();
        System.out.println("Finished");
        */
    }

    private GridFSTour() {
    }
}