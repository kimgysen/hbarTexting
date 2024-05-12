package hedera.topics;

import com.hedera.hashgraph.sdk.*;
import io.github.cdimascio.dotenv.Dotenv;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;




public class TopicsManager {

  private static AccountId myAccountId = AccountId.fromString(Dotenv.load().get("MY_ACCOUNT_ID"));
  private static PrivateKey myPrivateKey = PrivateKey.fromString(Dotenv.load().get("MY_PRIVATE_KEY"));  
   
	
	public static TopicId createTopicId(Client client, String memo) throws InterruptedException, TimeoutException, PrecheckStatusException, ReceiptStatusException
	{
		//Create a new topic
		TransactionResponse txResponse = new TopicCreateTransaction()
		   .setTopicMemo(memo)
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
	
	public static TransactionReceipt submitMessage(Client client, TopicId topicId, String message, String memo) throws InterruptedException, TimeoutException, PrecheckStatusException, ReceiptStatusException
	{
	
		
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
		      .setMessage(message)
		      .setTransactionMemo(memo)
		      .execute(client);

		//Get the receipt of the transaction
		 TransactionReceipt receipt2 = submitMessage.getReceipt(client);

		//Prevent the main thread from existing so the topic message can be returned and printed to the console
		Thread.sleep(30000);
		
		return receipt2;
	}
	
	static Client registerWithClient(AccountId myAccountId, PrivateKey myPrivateKey)
	{
    Client client = Client.forTestnet();
    client.setOperator(myAccountId, myPrivateKey);
    client.setDefaultMaxTransactionFee(new Hbar(100));
    client.setMaxQueryPayment(new Hbar(50));
    
		return client;
	}
	
public static void main(String[] args) throws TimeoutException, PrecheckStatusException, ReceiptStatusException, InterruptedException
{
 
	TopicId topicId = TopicId.fromString(Dotenv.load().get("MY_TOPIC_ID"));

    Client client = registerWithClient(myAccountId, myPrivateKey);    
    String message = "{11111110011011110001001111111, 10000010001111110111101000001, 10111010111100011000101011101, 10111010011010100011101011101, 10111010001110111111001011101, 10000010010101101011001000001, 11111110101010101010101111111, 00000000111011100111000000000, 11101111101111101100111000100, 10101000101011011000011001010, 11011010110000110010010111010, 00111001000011011110011001110, 00111110000111011110001000001, 10100000110011101010111000011, 00000011011011101100001011001, 11101101101011010111110100110, 00101010111111001110111000111, 01000100100010000100101101001, 10101110110010101010001111011, 01000001010101111100010001100, 10001010101011001000111110011, 00000000111001001110100011010, 11111110110001001011101010111, 10000010111011101110100011011, 10111010111111011100111110001, 10111010010010001111100011001, 10111010100011000001000110101, 10000010100111110111100100010, 11111110110111001101111111011}"; 
    String memo = "QR code smaller!!";
    submitMessage(client, topicId, message, memo);
            
//    PrivateKey fileKey = PrivateKey.generate();
}

}
