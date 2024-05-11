package demos;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.TimeoutException;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.FileAppendTransaction;
import com.hedera.hashgraph.sdk.FileContentsQuery;
import com.hedera.hashgraph.sdk.FileCreateTransaction;
import com.hedera.hashgraph.sdk.FileId;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.Status;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.TransactionResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import io.github.cdimascio.dotenv.Dotenv;


public class HederaFile {
	
  private static AccountId myAccountId = AccountId.fromString(Dotenv.load().get("MY_ACCOUNT_ID"));
  private static PrivateKey myPrivateKey = PrivateKey.fromString(Dotenv.load().get("MY_PRIVATE_KEY"));  
  private static Client client = registerWithClient(myAccountId, myPrivateKey); 
  private static final int MaxChunkSize = 5*1024;
  
	
	static Client registerWithClient(AccountId myAccountId, PrivateKey myPrivateKey)
	{
    Client client = Client.forTestnet();
    client.setOperator(myAccountId, myPrivateKey);
    client.setDefaultMaxTransactionFee(new Hbar(100));
    client.setMaxQueryPayment(new Hbar(50));
    
		return client;
	}

	
	
	static FileId uploadFileToHedera(PrivateKey fileKey, String fileName, Client client) throws TimeoutException, PrecheckStatusException, ReceiptStatusException, FileNotFoundException, IOException
	{
		
		Gson gson = new Gson();
		FileContentsB64 file = new FileContentsB64(fileName);
		String json = gson.toJson(file);
		System.out.println(json.length());	
		String[] chunks = json.split("(?<=\\G.{"+MaxChunkSize+"})");

		System.out.println(chunks[0]);

		
		/*Create the new file*/
		FileCreateTransaction transaction = new FileCreateTransaction()
		    .setKeys(fileKey) 
		    .setContents(chunks[0]);
		FileCreateTransaction modifyMaxTransactionFee = transaction.setMaxTransactionFee(new Hbar(2)); 
		TransactionResponse txResponse = modifyMaxTransactionFee.freezeWith(client).sign(fileKey).execute(client);
		TransactionReceipt receipt = txResponse.getReceipt(client);
		FileId newFileId = receipt.fileId;
		System.out.println("The status for file Creation Transaction is: "+receipt.status);
		System.out.println("The new file ID is: " + newFileId);
		
		/*Append the rest of the chunks*/
		for (int i=1; i<chunks.length; i++)
		{
			FileAppendTransaction appendTx = new FileAppendTransaction()
					.setFileId(newFileId)
					.setContents(chunks[i]);
			FileAppendTransaction modifyAppendMaxTransactionFee = appendTx.setMaxTransactionFee(new Hbar(2)); 
			TransactionResponse appendTxResponse = modifyAppendMaxTransactionFee.freezeWith(client).sign(fileKey).execute(client);
			TransactionReceipt appendReceit = appendTxResponse.getReceipt(client);
			Status appendTxStatus = appendReceit.status;
			System.out.println("The status for appendTx no "+i+" is :"+appendTxStatus);
		}
		
		
		return newFileId;		
	}
	
	public static Status appendToHederaFile(PrivateKey fileKey, FileId fileID, Client client, String chunk) throws PrecheckStatusException, TimeoutException, ReceiptStatusException
	{
	//Create the transaction
		FileAppendTransaction transaction = new FileAppendTransaction()
		    .setFileId(fileID)
		    .setContents(chunk);

		//Change the default max transaction fee to 2 hbars
		FileAppendTransaction modifyMaxTransactionFee = transaction.setMaxTransactionFee(new Hbar(2)); 

		//Prepare transaction for signing, sign with the key on the file, sign with the client operator key and submit to a Hedera network
		TransactionResponse txResponse = modifyMaxTransactionFee.freezeWith(client).sign(fileKey).execute(client);

		//Request the receipt
		TransactionReceipt receipt = txResponse.getReceipt(client);

		//Get the transaction consensus status
		Status transactionStatus = receipt.status;

		System.out.println("The transaction consensus status is " +transactionStatus);

		
		return transactionStatus;
	}
	
	public static String downLoadFileFromHedera(FileId fileId, Client client) throws TimeoutException, PrecheckStatusException, IOException
	{
		FileContentsQuery query = new FileContentsQuery()
		    .setFileId(fileId);

		ByteString contents = query.execute(client);

		String json = contents.toStringUtf8();

		System.out.println(json);
		 
		Gson gson = new Gson();
		FileContentsB64 download = gson.fromJson(json,FileContentsB64.class);

		System.out.println(download.fileName);
		
		download.write();
		
		return json;
	}
	
	
	private static class FileContents
	{
		private String fileName = "";
		private byte[] fileContents = null;
		
		public FileContents(String fileName_) throws FileNotFoundException, IOException
		{
			fileName = fileName_;
			File myFile = new File(fileName);
	    fileContents = new byte[(int) myFile.length()];
	    try (FileInputStream inputStream = new FileInputStream(myFile)) {
	        inputStream.read(fileContents);
	    }
		}
		
		public void write() throws IOException
		{
			File outputFile = new File("downLoaded_"+fileName);
			try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
			    outputStream.write(fileContents);
			}
		}
	}
	 
	private static class FileContentsB64
	{
		private String fileName = "";
		private String contentString = "";
		
		public FileContentsB64(String fileName_) throws FileNotFoundException, IOException
		{
			fileName = fileName_;
			File myFile = new File(fileName);
	    byte[] bytes = new byte[(int) myFile.length()];
	    try (FileInputStream inputStream = new FileInputStream(myFile)) {
	        inputStream.read(bytes);
	    }
	    contentString = Base64.getEncoder().encodeToString(bytes);
		}
		
		public void write() throws IOException
		{
			File outputFile = new File("downLoaded_"+fileName);
			byte[] bytes = Base64.getDecoder().decode(contentString);
			try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
			    outputStream.write(bytes);
			}
		}
	}
	
	
	
	
	public static void main(String[] args) throws FileNotFoundException, IOException, TimeoutException, PrecheckStatusException, ReceiptStatusException
	{
		
		PrivateKey fileKey = PrivateKey.fromString(Dotenv.load().get("FILE_KEY")); 	
		String fileName = "flat_13.jpg";
		//FileId fileId = FileId.fromString(Dotenv.load().get("SAMPLE_FILE_ID"));
		

	
		//FileId fileId = uploadFileToHedera(fileKey, fileName, client);
		//System.out.println(fileId);
		
		Gson gson = new Gson();
		FileContentsB64 file = new FileContentsB64(fileName);
		String json = gson.toJson(file);
		System.out.println(json.length());	
		String[] chunks = json.split("(?<=\\G.{"+MaxChunkSize+"})");
		
		FileId fileId = FileId.fromString("0.0.4301047");	
		appendToHederaFile(fileKey, fileId, client, chunks[1]);
		
		//downLoadFileFromHedera(fileId, client);
		

	}
	
}
