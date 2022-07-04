package hbarTexting;



import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hedera.hashgraph.sdk.*;
import io.github.cdimascio.dotenv.Dotenv;
import hbarTexting.AccountsManager;
import helpers.JsonHelper;



public class SuppliersContract 
{
	
	private String rootName = null;
	private AccountId  clientAccountId = null;
	private PrivateKey clientPrivateKey = null;
	private PublicKey  clientPublicKey = null;
	private ContractId clientContractId = null;
	private Client client = null;

	public static final FileId byteCodeFileId = FileId.fromString("0.0.45925852");

	
	public String toString()
	{
		return  "AccountID  = " + clientAccountId + "\n" +
				"ContractID = " + clientContractId + "\n"
		;
	}
	
	public SuppliersContract(String rootName_) throws IOException, TimeoutException, PrecheckStatusException, ReceiptStatusException
	{
		rootName = rootName_;
		clientAccountId = AccountId.fromString(Dotenv.load().get(rootName+"_ACCOUNT_ID"));
		clientPrivateKey = PrivateKey.fromString(Dotenv.load().get(rootName+"_PRIVATE_KEY"));
		clientPublicKey = PublicKey.fromString(Dotenv.load().get(rootName+"_PUBLIC_KEY"));
		
		System.out.println(clientAccountId);
		
	    client = Client.forTestnet();
	    client.setOperator(clientAccountId, clientPrivateKey);
		
		clientContractId = getContractId();
		
		if (null==clientContractId) clientContractId = deploySmartContract();
		
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
	
	public ContractId deploySmartContract() throws TimeoutException, PrecheckStatusException, ReceiptStatusException
	{
		
		System.out.println("Deploying Smart Contract...");
		
		 // Instantiate the contract instance
	    ContractCreateTransaction contractTx = new ContractCreateTransaction()
	         //Set the file ID of the Hedera file storing the bytecode
	         .setBytecodeFileId(byteCodeFileId)
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
			throws TimeoutException, PrecheckStatusException, ReceiptStatusException
	{
		 // Calls a function of the smart contract
		ContractExecuteTransaction transaction = new ContractExecuteTransaction()
	         //Set the gas for the query
	         .setGas(1000_000) 
	         //Set the contract ID to return the request for
	         .setContractId(contractId)
	         //Set the function of the contract to call 
	         .setFunction("addSupplier", new ContractFunctionParameters().addAddress(solidityAddress).addString(email))
	         //Set the query payment for the node returning the request
	         //This value must cover the cost of the request otherwise will fail 
	        ; 

	    //Submit to a Hedera network
	    TransactionResponse response = transaction.execute(client);
	    
	    TransactionReceipt receipt = response.getReceipt(client);
	    
	    System.out.println("The transaction consensus status is " +receipt.status);
		
	}
	
	public boolean validateSupplier(ContractId contractId, String email) throws TimeoutException, PrecheckStatusException
	{
		 // Calls a function of the smart contract
	    ContractCallQuery contractQuery = new ContractCallQuery()
	         //Set the gas for the query
	         .setGas(1000_000) 
	         //Set the contract ID to return the request for
	         .setContractId(contractId)
	         //Set the function of the contract to call 
	         .setFunction("validateSupplier", new ContractFunctionParameters().addString(email))
	         //Set the query payment for the node returning the request
	         //This value must cover the cost of the request otherwise will fail 
	         .setQueryPayment(new Hbar(1)); 

	    //Submit to a Hedera network
	    ContractFunctionResult getMessage = contractQuery.execute(client);

		
		return getMessage.getBool(0);
	}

	
	  public  boolean  isContractDeployed() throws IOException
	  {
		  	boolean ret = false;
		  
			System.out.println("retrieve account smart contract list:" + clientAccountId);
			JsonObject json = JsonHelper.readJsonFromUrl("https://testnet.mirrornode.hedera.com/api/v1/transactions/?account.id="+clientAccountId+"&transactionType=contractcreateinstance");
		    JsonArray jarr = json.getAsJsonArray("transactions");

		    
		    for (int i=0; i<jarr.size(); i++)
		    {
		    	JsonObject jo = (JsonObject) jarr.get(i);
		    	String contractStr = (""+jo.get("entity_id")).replace("\"", "");
			    if (!contractStr.equals("null"))
			    	{
				    	// get file_id from contract_id
			    		ContractId contractId = ContractId.fromString(contractStr);	    		
					    json = JsonHelper.readJsonFromUrl("https://testnet.mirrornode.hedera.com/api/v1/contracts/"+contractId);    
					    FileId fileId = FileId.fromString((""+json.get("file_id")).replace("\"", ""));
					    ret |=(fileId.equals(byteCodeFileId));
			    	}
		    }
		      
		  return ret;
	  }
	  
	  public  ContractId  getContractId() throws IOException
	  {
		  ContractId ret = null;
		  
			System.out.println("retrieve account smart contract list:" + clientAccountId);
			JsonObject json = JsonHelper.readJsonFromUrl("https://testnet.mirrornode.hedera.com/api/v1/transactions/?account.id="+clientAccountId+"&transactionType=contractcreateinstance");
		    JsonArray jarr = json.getAsJsonArray("transactions");

		    
		    for (int i=0; i<jarr.size(); i++)
		    {
		    	JsonObject jo = (JsonObject) jarr.get(i);
		    	String contractStr = (""+jo.get("entity_id")).replace("\"", "");
			    if (!contractStr.equals("null"))
			    	{
				    	// get file_id from contract_id
			    		ContractId contractId = ContractId.fromString(contractStr);	    		
					    json = JsonHelper.readJsonFromUrl("https://testnet.mirrornode.hedera.com/api/v1/contracts/"+contractId);    
					    FileId fileId = FileId.fromString((""+json.get("file_id")).replace("\"", ""));
					    if (fileId.equals(byteCodeFileId)) ret = contractId;
			    	}
		    }
		      
		  return ret;
	  }
	
	public static void main(String[] args) throws TimeoutException, PrecheckStatusException, ReceiptStatusException, IOException
	{

	SuppliersContract sp = new SuppliersContract("CLIENT3");
	
	System.out.println(""+sp);
    
    System.out.println("Checking client account balance:");
    AccountBalance accountBalance = sp.checkBalance(sp.clientAccountId);
    
    System.out.println("Checking contract deployment: "+sp.getContractId());
    
    /***************************
    *
    *   Start testing area
    *         
    ****************/
    
  //Deploy Client/Supplier smartContract


    /*
   ContractId contractId = deploySmartContract(sp.client, byteCodeFileId);

        
   String accountVariable = sp.rootName.toUpperCase()+"_CONTRACT_ID=" +contractId;
   System.out.println(accountVariable);
   AccountsManager.storeAccountVariable(accountVariable);
 
 
    ContractId clientContractId = ContractId.fromString(Dotenv.load().get("CLIENT1_CONTRACT_ID"));
    System.out.println("The smart contract ID is " + clientContractId);
    
    String supplier1SolidityAddress = AccountId.fromString(Dotenv.load().get("SUPPLIER3_ACCOUNT_ID")).toSolidityAddress();
    String email = "supplier3@mail.com"; 
    
    boolean success = sp.validateSupplier(clientContractId, email);

    System.out.println(""+success);
    
    success = sp.validateSupplier(clientContractId, "nobody@mail.com");
    
    System.out.println(""+success);
 */
    
    
    /***************************
    *
    *   END testing area
    *         
    ****************/
    
	}
	
}
