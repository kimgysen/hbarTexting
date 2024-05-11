package demos;



import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hedera.hashgraph.sdk.*;
import io.github.cdimascio.dotenv.Dotenv;

public class BankContract 
{
	
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

	    InputStream jsonStream = BankContract.class.getClassLoader().getResourceAsStream(jsonFilename);
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

	    
	    System.out.println("bytecode size is:"+bytecode.length);
	    
	    
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
	
	public static ContractId createSmartContract(Client client, FileId bytecodeFileId) throws TimeoutException, PrecheckStatusException, ReceiptStatusException
	{
		
		 // Instantiate the contract instance
	    ContractCreateTransaction contractTx = new ContractCreateTransaction()
	         //Set the file ID of the Hedera file storing the bytecode
	         .setBytecodeFileId(bytecodeFileId)
	         //Set the gas to instantiate the contract
	         .setGas(100_000)
	         ;

	   //Submit the transaction to the Hedera test network
	   TransactionResponse contractResponse = contractTx.execute(client);

	   //Get the receipt of the file create transaction
	   TransactionReceipt contractReceipt = contractResponse.getReceipt(client);

	   //Get the smart contract ID
		return  contractReceipt.contractId;
	}
	
	public static String getContractMessage(Client client, ContractId contractId) throws TimeoutException, PrecheckStatusException
	{
		 // Calls a function of the smart contract
	    ContractCallQuery contractQuery = new ContractCallQuery()
	         //Set the gas for the query
	         .setGas(100000) 
	         //Set the contract ID to return the request for
	         .setContractId(contractId)
	         //Set the function of the contract to call 
	         .setFunction("get_message" )
	         //Set the query payment for the node returning the request
	         //This value must cover the cost of the request otherwise will fail 
	         .setQueryPayment(new Hbar(2)); 

	    //Submit to a Hedera network
	    ContractFunctionResult getMessage = contractQuery.execute(client);

		
		return getMessage.getString(0);
	}
	
	public static void setContractMessage(Client client, ContractId contractId, String message) throws TimeoutException, PrecheckStatusException, ReceiptStatusException
	{
		//Create the transaction to update the contract message
		 ContractExecuteTransaction contractExecTx = new ContractExecuteTransaction()
		        //Set the ID of the contract
		        .setContractId(contractId)
		        //Set the gas for the call
		        .setGas(100_000)
		        //Set the function of the contract to call
		        .setFunction("set_message", new ContractFunctionParameters().addString(message));

		//Submit the transaction to a Hedera network and store the response
		TransactionResponse submitExecTx = contractExecTx.execute(client);

		//Get the receipt of the transaction
		TransactionReceipt receipt2 = submitExecTx.getReceipt(client);

		//Confirm the transaction was executed successfully 
		System.out.println("The transaction status is " +receipt2.status);
		
	}
	
	public static void main(String[] args) throws TimeoutException, PrecheckStatusException, ReceiptStatusException
	{
    //Grab your Hedera testnet account ID and private key
    AccountId kimAccountId = AccountId.fromString(Dotenv.load().get("KIM_ACCOUNT_ID"));
    AccountId myAccountId = AccountId.fromString(Dotenv.load().get("MY_ACCOUNT_ID"));
    PrivateKey myPrivateKey = PrivateKey.fromString(Dotenv.load().get("MY_PRIVATE_KEY")); 
    PublicKey myPublicKey = myPrivateKey.getPublicKey();
    
    // grab trash account credentials
    AccountId trashAccountId = AccountId.fromString(Dotenv.load().get("TRASH_ACCOUNT_ID"));
    PrivateKey trashPrivateKey = PrivateKey.fromString(Dotenv.load().get("TRASH_PRIVATE_KEY")); 
    
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
    
  //Import the compiled contract from the HelloHedera.json file
    byte[] bytecode = retrieveBytecodeFromJson("BankContract.json");
   

    //Get the file ID from the receipt
    //FileId bytecodeFileId = loadBytecodeToHederaFile(client, bytecode);
    //System.out.println("The smart contract bytecode file ID is " +bytecodeFileId);

    
    //FileId bytecodeFileId = FileId.fromString("0.0.45909545");
    
    
    //ContractId contractId = createSmartContract(client, bytecodeFileId);
    /*The smart contract ID is 0.0.34376603*/
   //Log the smart contract ID
    ContractId contractId = ContractId.fromString("0.0.45909546");
    System.out.println("The smart contract ID is " + contractId);

 
    /*
    String message = getContractMessage(client, contractId);

    System.out.println("The contract message: " + message);
    
    setContractMessage(client, contractId, "Hello from Hedera again!!");
    
    System.out.println("The new contract message: " + getContractMessage(client, contractId));
    */
    
    /***************************
    *
    *   END testing area
    *         
    ****************/
    
	}
	
}
