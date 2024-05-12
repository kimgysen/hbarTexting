package hedera.database;

import com.hedera.hashgraph.sdk.*;
import io.github.cdimascio.dotenv.Dotenv;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;




public class Database {

  private static AccountId myAccountId = AccountId.fromString(Dotenv.load().get("MY_ACCOUNT_ID"));
  private static PrivateKey myPrivateKey = PrivateKey.fromString(Dotenv.load().get("MY_PRIVATE_KEY"));  
  private static Client client = registerWithClient(myAccountId, myPrivateKey);   
	
	public static TopicId createDatabaseTopicId(String dbName) throws InterruptedException, TimeoutException, PrecheckStatusException, ReceiptStatusException
	{
		TransactionResponse txResponse = new TopicCreateTransaction()
		   .setTopicMemo(dbName)
		   .execute(client);

		TransactionReceipt receipt = txResponse.getReceipt(client);
		        
		TopicId dbTopicId = receipt.topicId;

		System.out.println("Your Database topic ID is: " +dbTopicId+". Don't forget to save it to .env .");
		
		Thread.sleep(5000);
		
		return dbTopicId;
	}
	
	public static TransactionReceipt insertRecord(TopicId dbTopicId, String record, String memo) throws InterruptedException, TimeoutException, PrecheckStatusException, ReceiptStatusException
	{
	 
		
		new TopicMessageQuery()
		    .setTopicId(dbTopicId)
		    .subscribe(client, resp -> {
		            String messageAsString = new String(resp.contents, StandardCharsets.UTF_8);
		            System.out.println(resp.consensusTimestamp + " received topic message: " + messageAsString);
		    });
		

		TransactionResponse submitMessage = new TopicMessageSubmitTransaction()
		      .setTopicId(dbTopicId)
		      .setMessage(record)
		      .setTransactionMemo(memo)
		      .execute(client);

		 TransactionReceipt receipt = submitMessage.getReceipt(client);

		Thread.sleep(30000);
		
		return receipt;
	}
	
	static Client registerWithClient(AccountId myAccountId, PrivateKey myPrivateKey)
	{
    Client client = Client.forTestnet();
    client.setOperator(myAccountId, myPrivateKey);
    client.setDefaultMaxTransactionFee(new Hbar(100));
    client.setMaxQueryPayment(new Hbar(50));
    
		return client;
	}
	
	public static String accessLineInFile(String fileName, int lineNumber) throws Exception {
		String line = "";
		try (Stream<String> lines = Files.lines(Paths.get(fileName))) {
	    line = lines.skip(lineNumber-1).findFirst().get();
	}
    return line;
	}
	
	public static String getRandomName() throws Exception
	{
		Random rand = new Random();
	  int position = rand.nextInt(4945);
		return accessLineInFile("./src/main/java/ressources/first-names.txt", position);
	}
	

	
public static void main(String[] args) throws Exception
{
	
		
		TopicId dbTopicId = TopicId.fromString(Dotenv.load().get("MY_DB_TOPIC_ID"));
	
		Random rand = new Random();
		
		for (int i=0; i<5; i++)
		{
			long start = System.currentTimeMillis();
			try {
					String buyer = getRandomName();
					String seller = getRandomName();
					int id  = rand.nextInt(10000);
					double price  = rand.nextDouble(1000);
		
					String record = buyer+", "+seller+", "+id+", "+price;
					String memo = "new record";
					System.out.println(record);
    
					TransactionReceipt rct = insertRecord(dbTopicId, record, memo);
					System.out.println(rct);
					
			}catch(Exception e) {
					System.out.println(""+e);
			}
			
			long finish = System.currentTimeMillis();
			long timeElapsed = finish - start;
			
			System.out.println(""+timeElapsed/1000+" s elapsed...");
			
		}
    
}

}
