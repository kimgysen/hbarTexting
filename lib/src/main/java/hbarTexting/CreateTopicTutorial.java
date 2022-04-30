package hbarTexting;

import com.hedera.hashgraph.sdk.*;
import io.github.cdimascio.dotenv.Dotenv;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;




public class CreateTopicTutorial {

	
	public static TopicId createTopicId(Client client) throws InterruptedException, TimeoutException, PrecheckStatusException, ReceiptStatusException
	{
		//Create a new topic
		TransactionResponse txResponse = new TopicCreateTransaction()
		   .execute(client);

		//Get the receipt
		TransactionReceipt receipt = txResponse.getReceipt(client);
		        
		//Get the topic ID
		TopicId topicId = receipt.topicId;

		//Log the topic ID
		System.out.println("Your topic ID is: " +topicId);

		// Wait 5 seconds between consensus topic creation and subscription creation
		Thread.sleep(5000);
		
		return topicId;
	}
	
public static void main(String[] args) throws TimeoutException, PrecheckStatusException, ReceiptStatusException, InterruptedException
{
    //Grab your Hedera testnet account ID and private key
    AccountId myAccountId = AccountId.fromString(Dotenv.load().get("MY_ACCOUNT_ID"));
    PrivateKey myPrivateKey = PrivateKey.fromString(Dotenv.load().get("MY_PRIVATE_KEY"));  
    
    //Create your Hedera testnet client
    Client client = Client.forTestnet();
    client.setOperator(myAccountId, myPrivateKey);
	
    //TopicId topicId = createTopicId(client);
    
    TopicId topicId = TopicId.fromString("0.0.34319439");
	
	//Subscribe to the topic
	new TopicMessageQuery()
	    .setTopicId(topicId)
	    .subscribe(client, resp -> {
	            String messageAsString = new String(resp.contents, StandardCharsets.UTF_8);
	            System.out.println(resp.consensusTimestamp + " received topic message: " + messageAsString);
	    });
	
	//Submit a message to a topic
	TransactionResponse submitMessage = new TopicMessageSubmitTransaction()
	      .setTopicId(topicId)
	      .setMessage("By the way, here is a new message..")
	      .execute(client);

	//Get the receipt of the transaction
	 TransactionReceipt receipt2 = submitMessage.getReceipt(client);

	//Prevent the main thread from existing so the topic message can be returned and printed to the console
	Thread.sleep(30000);
}

}
