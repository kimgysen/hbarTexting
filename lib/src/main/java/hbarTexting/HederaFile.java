package hbarTexting;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.FileContentsQuery;
import com.hedera.hashgraph.sdk.FileCreateTransaction;
import com.hedera.hashgraph.sdk.FileId;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.TransactionResponse;

import io.github.cdimascio.dotenv.Dotenv;


public class HederaFile {
	
  private static AccountId myAccountId = AccountId.fromString(Dotenv.load().get("MY_ACCOUNT_ID"));
  private static PrivateKey myPrivateKey = PrivateKey.fromString(Dotenv.load().get("MY_PRIVATE_KEY"));  
  
	
	static Client registerWithClient(AccountId myAccountId, PrivateKey myPrivateKey)
	{
    Client client = Client.forTestnet();
    client.setOperator(myAccountId, myPrivateKey);
    client.setDefaultMaxTransactionFee(new Hbar(100));
    client.setMaxQueryPayment(new Hbar(50));
    
		return client;
	}
	
	static byte[] file2byteArray(String filename) throws FileNotFoundException, IOException
	{
    File myFile = new File(filename);
    byte[] byteArray = new byte[(int) myFile.length()];
    try (FileInputStream inputStream = new FileInputStream(myFile)) {
        inputStream.read(byteArray);
    }
    
    return byteArray;
	}
	
	static FileId loadNewFileToHedera(PrivateKey fileKey, byte[] fileContents, Client client) throws TimeoutException, PrecheckStatusException, ReceiptStatusException
	{
		FileCreateTransaction transaction = new FileCreateTransaction()
		    .setKeys(fileKey) 
		    .setContents(fileContents);
		        
		FileCreateTransaction modifyMaxTransactionFee = transaction.setMaxTransactionFee(new Hbar(2)); 

		TransactionResponse txResponse = modifyMaxTransactionFee.freezeWith(client).sign(fileKey).execute(client);

		TransactionReceipt receipt = txResponse.getReceipt(client);

		FileId newFileId = receipt.fileId;

		System.out.println("The new file ID is: " + newFileId);
		
		return newFileId;		
	}
	
	public static String readFile(FileId fileId, Client client) throws TimeoutException, PrecheckStatusException
	{
		FileContentsQuery query = new FileContentsQuery()
		    .setFileId(fileId);

		ByteString contents = query.execute(client);

		String contentsToUtf8 = contents.toStringUtf8();

		System.out.println(contentsToUtf8);
		 
		return contentsToUtf8;
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException, TimeoutException, PrecheckStatusException, ReceiptStatusException
	{

		FileId fileId = FileId.fromString(Dotenv.load().get("FILE_ID"));  

		Client client = registerWithClient(myAccountId, myPrivateKey);
 		 
		readFile(fileId, client);
		 
	}
	
}
