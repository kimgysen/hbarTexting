package hbarTopics;


import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hedera.hashgraph.sdk.*;
import io.github.cdimascio.dotenv.Dotenv;

public class GuessNumberSmartContract {
	
	public static AccountBalance checkBalance(Client client, AccountId newAccountId) throws TimeoutException, PrecheckStatusException
	{
	      //Check the new account's balance
        AccountBalance accountBalance = new AccountBalanceQuery()
             .setAccountId(newAccountId)
             .execute(client);

        System.out.println("Treasury account balance is: " +accountBalance.hbars);
        System.out.println("Tokens account balance is: " +accountBalance.tokens);
        
        return accountBalance;
		
	}
	
	public static byte[] retrieveBytecodeFromJson(String jsonFilename)
	{
	    Gson gson = new Gson();
	    JsonObject jsonObject;

	    InputStream jsonStream = CreateSmartContract.class.getClassLoader().getResourceAsStream(jsonFilename);
	    jsonObject = gson.fromJson(new InputStreamReader(jsonStream, StandardCharsets.UTF_8), JsonObject.class);

	    //Store the "object" field from the HelloHedera.json file as hex-encoded bytecode
	    String object = jsonObject.getAsJsonObject("data").getAsJsonObject("bytecode").get("object").getAsString();
		
		return object.getBytes(StandardCharsets.UTF_8);
	}
	
	
	public static FileId loadBytecodeToHederaFile(Client client, byte[] bytecode) throws TimeoutException, PrecheckStatusException, ReceiptStatusException
	{
		
		  //Create a file on Hedera and store the hex-encoded bytecode
	    FileCreateTransaction fileCreateTx = new FileCreateTransaction()
	            //Set the bytecode of the contract
	            .setContents(bytecode);

	    //Submit the file to the Hedera test network signing with the transaction fee payer key specified with the client
	    TransactionResponse submitTx = fileCreateTx.execute(client);

	    //Get the receipt of the file create transaction
	    TransactionReceipt fileReceipt = submitTx.getReceipt(client);

	    //Get the file ID from the receipt
	    FileId bytecodeFileId = fileReceipt.fileId;

	    //Log the file ID
	    System.out.println("The bytecode file ID is " +bytecodeFileId);
		
		return bytecodeFileId;
	}
	
	
	public static ContractId deployContract(Client client, FileId bytecodeFileId, ContractFunctionParameters constructorParameters) throws TimeoutException, PrecheckStatusException, ReceiptStatusException
	{
		
		 // Instantiate the contract instance
	    ContractCreateTransaction contractTx = new ContractCreateTransaction()
	         //Set the file ID of the Hedera file storing the bytecode
	         .setBytecodeFileId(bytecodeFileId)
	         //Set the gas to instantiate the contract
	         .setGas(1000_000)
	         //Provide the constructor parameters for the contract
	         .setConstructorParameters(constructorParameters);

	   //Submit the transaction to the Hedera test network
	   TransactionResponse contractResponse = contractTx.execute(client);

	   //Get the receipt of the file create transaction
	   TransactionReceipt contractReceipt = contractResponse.getReceipt(client);

	   System.out.println("Smart contract id: "+contractReceipt.contractId);
	   //Get the smart contract ID
	   return  contractReceipt.contractId;
	}
	
	public static String tryNumberGuess(Client client, ContractId contractId, int guess) throws TimeoutException, PrecheckStatusException
	{
		 // Calls a function of the smart contract
	    ContractCallQuery contractQuery = new ContractCallQuery()
	         //Set the gas for the query
	         .setGas(1000_000) 
	         //Set the contract ID to return the request for
	         .setContractId(contractId)
	         //Set the function of the contract to call 
	         .setFunction("guess", new ContractFunctionParameters().addUint256(new BigInteger(""+guess)))
	         //Set the query payment for the node returning the request
	         //This value must cover the cost of the request otherwise will fail 
	         .setQueryPayment(new Hbar(4)); 

	    //Submit to a Hedera network
	    ContractFunctionResult getMessage = contractQuery.execute(client);

		
		return getMessage.getString(0);
	}
	
	
	public static void main(String[] args) throws Exception
	{
		//Grab your Hedera testnet account ID and private key
		AccountId kimAccountId = AccountId.fromString(Dotenv.load().get("KIM_ACCOUNT_ID"));
		AccountId myAccountId = AccountId.fromString(Dotenv.load().get("MY_ACCOUNT_ID"));
		PrivateKey myPrivateKey = PrivateKey.fromString(Dotenv.load().get("MY_PRIVATE_KEY")); 
		PublicKey myPublicKey = myPrivateKey.getPublicKey();
    
    
		//Create your Hedera testnet client
		Client client = Client.forTestnet();
		client.setOperator(myAccountId, myPrivateKey);
    
		System.out.println("Checking My Account balance:");
		AccountBalance myAccountBalance = checkBalance(client, myAccountId);
    
		/***************************
		 *
		 *   Start testing area
		 *         
		 ****************/
    
		//1 Deploy Contract
		/*
		byte[] bytecode = retrieveBytecodeFromJson("GuessNumber.json");
		System.out.println(bytecode);  
   
		FileId bytecodeFileId = loadBytecodeToHederaFile(client, bytecode);
		System.out.println("The smart contract bytecode file ID is " +bytecodeFileId);
    
		int secretNumber = 5;
		ContractId contractId = deployContract(client, bytecodeFileId, new ContractFunctionParameters().addUint32(secretNumber));
		System.out.println("The contract ID is:"+contractId);
		 */
		
		/***************************
		 *
		 *  Play Number Guessing Game
		 *         
		 ****************/

		//Log the smart contract ID
		ContractId contractId = ContractId.fromString("0.0.34910840");
		
		System.out.println("The contract ID is:"+contractId);
		
		String message = tryNumberGuess(client, contractId, 2); 
		System.out.println("query result: " + message);
		
		message = tryNumberGuess(client, contractId, 5); 
		System.out.println("query result: " + message);

		message = tryNumberGuess(client, contractId, 6); 
		System.out.println("query result: " + message);


		/***************************
		 *
		 *   END testing area
		 *         
		 ****************/
    
	}

}
