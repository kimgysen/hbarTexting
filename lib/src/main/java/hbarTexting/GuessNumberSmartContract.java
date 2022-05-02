package hbarTexting;


import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
	
	
	public static ContractId createGuessNumberContract(Client client, FileId bytecodeFileId, int secretNumber, String lower, String equal, String higher) throws TimeoutException, PrecheckStatusException, ReceiptStatusException
	{
		
		 // Instantiate the contract instance
	    ContractCreateTransaction contractTx = new ContractCreateTransaction()
	         //Set the file ID of the Hedera file storing the bytecode
	         .setBytecodeFileId(bytecodeFileId)
	         //Set the gas to instantiate the contract
	         .setGas(1000_000)
	         //Provide the constructor parameters for the contract
	         .setConstructorParameters(new ContractFunctionParameters()
	        		 							.addUint32(secretNumber)
	        		 							.addString(lower)
	        		 							.addString(equal)
	        		 							.addString(higher));

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
	         .setFunction("guess", new ContractFunctionParameters().addUint32(guess))
	         //Set the query payment for the node returning the request
	         //This value must cover the cost of the request otherwise will fail 
	         .setQueryPayment(new Hbar(4)); 

	    //Submit to a Hedera network
	    ContractFunctionResult getMessage = contractQuery.execute(client);

		
		return getMessage.getString(0);
	}
	
	
	public static String sayHello(Client client, ContractId contractId) throws TimeoutException, PrecheckStatusException
	{
		 // Calls a function of the smart contract
	    ContractCallQuery contractQuery = new ContractCallQuery()
	         //Set the gas for the query
	         .setGas(100000) 
	         //Set the contract ID to return the request for
	         .setContractId(contractId)
	         //Set the function of the contract to call 
	         .setFunction("hello", new ContractFunctionParameters())
	         //Set the query payment for the node returning the request
	         //This value must cover the cost of the request otherwise will fail 
	         .setQueryPayment(new Hbar(2)); 

	    //Submit to a Hedera network
	    ContractFunctionResult getMessage = contractQuery.execute(client);

		
		return getMessage.getString(0);
	}
	
	
	public static String getLower(Client client, ContractId contractId) throws TimeoutException, PrecheckStatusException
	{
		 // Calls a function of the smart contract
	    ContractCallQuery contractQuery = new ContractCallQuery()
	         //Set the gas for the query
	         .setGas(100000) 
	         //Set the contract ID to return the request for
	         .setContractId(contractId)
	         //Set the function of the contract to call 
	         .setFunction("lower", new ContractFunctionParameters())
	         //Set the query payment for the node returning the request
	         //This value must cover the cost of the request otherwise will fail 
	         .setQueryPayment(new Hbar(2)); 

	    //Submit to a Hedera network
	    ContractFunctionResult getMessage = contractQuery.execute(client);

		
		return getMessage.getString(0);
	}
	
	public static int cheat(Client client, ContractId contractId) throws TimeoutException, PrecheckStatusException
	{
		 // Calls a function of the smart contract
	    ContractCallQuery contractQuery = new ContractCallQuery()
	         //Set the gas for the query
	         .setGas(100000) 
	         //Set the contract ID to return the request for
	         .setContractId(contractId)
	         //Set the function of the contract to call 
	         .setFunction("secret", new ContractFunctionParameters())
	         //Set the query payment for the node returning the request
	         //This value must cover the cost of the request otherwise will fail 
	         .setQueryPayment(new Hbar(2)); 

	    //Submit to a Hedera network
	    ContractFunctionResult getMessage = contractQuery.execute(client);
	
		return getMessage.getInt32(0);
	}
	
	
	public static void main(String[] args) throws TimeoutException, PrecheckStatusException, ReceiptStatusException
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
    
		//Import the compiled contract from the *.json file
		byte[] bytecode = retrieveBytecodeFromJson("GuessNumber.json");
		System.out.println(bytecode);  
   

		//Get the file ID from the receipt
		FileId bytecodeFileId = loadBytecodeToHederaFile(client, bytecode);
		//Log the file ID
		System.out.println("The smart contract bytecode file ID is " +bytecodeFileId);

		/*The bytecode file ID is 0.0.34382461*/
		//FileId bytecodeFileId = FileId.fromString("0.0.34382461");
    

		ContractId contractId = createGuessNumberContract(client, bytecodeFileId, 5, "Trop petit", "Bravo!! C'est gagné", "Trop grand");
		//Log the smart contract ID
		//ContractId contractId = ContractId.fromString("0.0.34382608");
		
		/***************************
		 *
		 *  Play Number Guessing Game
		 *         
		 ****************/
		
		String message = sayHello(client, contractId);
		System.out.println("query result: " + message);
		
		 message = getLower(client, contractId);
		System.out.println("query result: " + message);
		
		int secret = cheat(client, contractId);
		System.out.println("query result: " + secret);
		
		  message = tryNumberGuess(client, contractId, 2); // this  crashes, I dont know why???
		System.out.println("query result: " + message);

    
		/***************************
		 *
		 *   END testing area
		 *         
		 ****************/
    
	}

}
