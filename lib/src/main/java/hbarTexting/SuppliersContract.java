package hbarTexting;



import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hedera.hashgraph.sdk.*;
import io.github.cdimascio.dotenv.Dotenv;
import hbarTexting.AccountsManager;



public class SuppliersContract 
{
	
	private String rootName = null;
	private AccountId  clientAccountId = null;
	private PrivateKey clientPrivateKey = null;
	private PublicKey  clientPublicKey = null;
	private Client client = null;
	
	public SuppliersContract(String rootName_)
	{
		rootName = rootName_;
		clientAccountId = AccountId.fromString(Dotenv.load().get(rootName+"_ACCOUNT_ID"));
		clientPrivateKey = PrivateKey.fromString(Dotenv.load().get(rootName+"_PRIVATE_KEY"));
		clientPublicKey = PublicKey.fromString(Dotenv.load().get(rootName+"_PUBLIC_KEY"));
		
		System.out.println(clientAccountId);
		
		
	    client = Client.forTestnet();
	    client.setOperator(clientAccountId, clientPrivateKey);
		
	}
	
	public AccountBalance checkBalance(AccountId newAccountId) throws TimeoutException, PrecheckStatusException
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

	    InputStream jsonStream = SuppliersContract.class.getClassLoader().getResourceAsStream(jsonFilename);
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
	
	public static ContractId deploySmartContract(Client client, FileId bytecodeFileId) throws TimeoutException, PrecheckStatusException, ReceiptStatusException
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
	
	
	public void addSupplier(ContractId contractId, String solidityAddress, String email) 
			throws TimeoutException, PrecheckStatusException
	{
		 // Calls a function of the smart contract
	    ContractCallQuery contractQuery = new ContractCallQuery()
	         //Set the gas for the query
	         .setGas(1000_000) 
	         //Set the contract ID to return the request for
	         .setContractId(contractId)
	         //Set the function of the contract to call 
	         .setFunction("addSupplier", new ContractFunctionParameters().addAddress(solidityAddress).addString(email))
	         //Set the query payment for the node returning the request
	         //This value must cover the cost of the request otherwise will fail 
	         .setQueryPayment(new Hbar(2)); 

	    //Submit to a Hedera network
	    ContractFunctionResult getMessage = contractQuery.execute(client);
		
	}

	
	public static void main(String[] args) throws TimeoutException, PrecheckStatusException, ReceiptStatusException, IOException
	{

	SuppliersContract sp = new SuppliersContract("CLIENT1");
    
    System.out.println("Checking client account balance:");
    AccountBalance myAccountBalance = sp.checkBalance(sp.clientAccountId);
    
    /***************************
    *
    *   Start testing area
    *         
    ****************/
    
  //Deploy Client/Supplier smartContract
/*
   byte[] bytecode = retrieveBytecodeFromJson("Suppliers.json");
   
   FileId bytecodeFileId = loadBytecodeToHederaFile(sp.client, bytecode);
   System.out.println("The smart contract bytecode file ID is " +bytecodeFileId);

    
   ContractId contractId = deploySmartContract(sp.client, bytecodeFileId);
     
   String accountVariable = sp.rootName.toUpperCase()+"_CONTRACT_ID=" +contractId;
   System.out.println(accountVariable);
   AccountsManager.storeAccountVariable(accountVariable);
 */
     
    
   //Log the smart contract ID
    ContractId clientContractId = ContractId.fromString(Dotenv.load().get("CLIENT1_CONTRACT_ID"));
    System.out.println("The smart contract ID is " + clientContractId);
    
    String supplier1SolidityAddress = AccountId.fromString(Dotenv.load().get("SUPPLIER1_ACCOUNT_ID")).toSolidityAddress();
    String email = "supplier1@mail.com";

    sp.addSupplier(clientContractId, supplier1SolidityAddress, email);

    
    /***************************
    *
    *   END testing area
    *         
    ****************/
    
	}
	
}
