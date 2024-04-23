package mongowrapper;

import static com.mongodb.client.model.Filters.eq;
import static helpers.PublisherHelpers.toPublisher;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.reactivestreams.Publisher;

import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.TopicId;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import com.mongodb.reactivestreams.client.MongoDatabase;
import com.mongodb.reactivestreams.client.gridfs.GridFSBucket;
import com.mongodb.reactivestreams.client.gridfs.GridFSBuckets;

import hbarTexting.HederaTopic;
import helpers.SubscriberHelpers.ConsumerSubscriber;
import helpers.SubscriberHelpers.ObservableSubscriber;
import helpers.SubscriberHelpers.OperationSubscriber;
import io.github.cdimascio.dotenv.Dotenv;

public class GridFS 
{

    private MongoClient 	mongoClient;
    private GridFSBucket 	gridFSBucket;
    private TopicId 		proofTopicId; 
    private Client			hederaClient;
    private String			streamMessage;
	
    public static String hex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte aByte : bytes) {
            result.append(String.format("%02x", aByte));
        }
        return result.toString();
    }

    
	public GridFS()
	{
       
        String mongoAdminKey = Dotenv.load().get("MONGO_ADMIN_KEY");
        	
        ConnectionString connectionString = new ConnectionString("mongodb+srv://kitakazenodb:"+mongoAdminKey+"@cluster0.mqynd.mongodb.net/myFirstDatabase");
        MongoClientSettings settings = MongoClientSettings.builder()
        	        .applyConnectionString(connectionString)
        	        .serverApi(ServerApi.builder()
        	            .version(ServerApiVersion.V1)
        	            .build())
        	        .build();
        		
        mongoClient = MongoClients.create(settings);

        MongoDatabase database = mongoClient.getDatabase("mydb");        
        gridFSBucket = GridFSBuckets.create(database);

        proofTopicId = TopicId.fromString(Dotenv.load().get("PROOF_TOPIC_ID"));
        
        //Grab your Hedera testnet account ID and private key
        AccountId myAccountId = AccountId.fromString(Dotenv.load().get("MY_ACCOUNT_ID"));
        PrivateKey myPrivateKey = PrivateKey.fromString(Dotenv.load().get("MY_PRIVATE_KEY"));  
        
        //Create your Hedera testnet client
        hederaClient = Client.forTestnet();
        hederaClient.setOperator(myAccountId, myPrivateKey);
	}
	
	public void uploadFile(Path path) throws IOException, NoSuchAlgorithmException
	{

		byte[] bytes = Files.readAllBytes(path);		
		ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        Publisher<ByteBuffer> publisherToUploadFrom = toPublisher(byteBuffer);
		
        // Add SHA-256 hash to metadata
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(bytes);
        byte[] digest = md.digest();
        String myHash = new String (hex(digest));
        
        System.out.println(myHash);
        GridFSUploadOptions options = new GridFSUploadOptions()
                .chunkSizeBytes(1024)
                .metadata(new Document("hash", myHash));

        ObservableSubscriber<ObjectId> uploadSubscriber = new OperationSubscriber<>();
        gridFSBucket.uploadFromPublisher(""+path.getFileName(), publisherToUploadFrom, options).subscribe(uploadSubscriber);
        ObjectId fileId = uploadSubscriber.get().get(0);
        
        System.out.println(fileId);
		
	}
	
	public void close()
	{
		mongoClient.close();	
	}
	
	private String concatStrings(String str1, String str2)
	{
		return str1+str2;
	}
	
	
	private ByteBuffer concatByteBuffers(ByteBuffer bf1, ByteBuffer bf2)
	{
		int size = bf1.limit() + bf2.limit();
		return ByteBuffer.allocate(size).put(bf1).put(bf2);
	}
	
	public void downloadFile(String fileId)
	{
		System.out.println("Attempts to download: "+fileId);
		
        ObservableSubscriber<ByteBuffer> downloadSubscriber = new OperationSubscriber<>();
        gridFSBucket.downloadToPublisher(fileId).subscribe(downloadSubscriber);
        
        Path path = Paths.get("./download/test.pdf");
        Optional<ByteBuffer> s = downloadSubscriber.get().stream().reduce(this::concatByteBuffers);
      
        System.out.println(new String(s.get().array()));
	}
	
	public void generateProof(String hash) throws InterruptedException, TimeoutException, PrecheckStatusException, ReceiptStatusException
	{
		System.out.println("Generate document proof:"+hash);
		
		 TransactionReceipt trx = HederaTopic.submitMessage(hederaClient, proofTopicId, hash, hash);
		 
		 System.out.println(trx);
		
	}
	
	private void writeStreamMessage(String message)
	{
		streamMessage = message;
	}
	
	public String findUploadedDocumentHash(String filename)
	{
		ConsumerSubscriber<GridFSFile> filesSubscriber = new ConsumerSubscriber<>(gridFSFile -> writeStreamMessage(gridFSFile.getMetadata().getString("hash")));
        gridFSBucket.find(Filters.eq("filename", filename)).subscribe(filesSubscriber);
        filesSubscriber.await();
        
        //System.out.println("returned hash: "+streamMessage);
        
        return new String(""+streamMessage);
	}
	
	public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InterruptedException, TimeoutException, PrecheckStatusException, ReceiptStatusException
	{
	    
	    Path path = Paths.get("./test1.pdf");
	    
	    GridFS g = new GridFS();
	    
	    //g.uploadFile(path);
	    
	    //g.downloadFile("test.txt");
	    
	    String hash = g.findUploadedDocumentHash("test.txt");
	    
	    g.generateProof(hash);
	    
	    g.close();	
	}
	
}
