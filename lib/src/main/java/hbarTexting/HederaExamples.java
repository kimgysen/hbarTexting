package hbarTexting;


import com.hedera.hashgraph.sdk.*;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.concurrent.TimeoutException;


public class HederaExamples {
	
	public static AccountId createNewAccount(Client client) throws TimeoutException, PrecheckStatusException, ReceiptStatusException
	{
        
      //Create your Hedera testnet client
      //Client client = Client.forTestnet();
      //client.setOperator(myAccountId, myPrivateKey)
      //-----------------------<enter code below>--------------------------------------

      // Generate a new key pair
      PrivateKey newAccountPrivateKey = PrivateKey.generate();
      PublicKey newAccountPublicKey = newAccountPrivateKey.getPublicKey();
        
      //Create new account and assign the public key
        TransactionResponse newAccount = new AccountCreateTransaction()
             .setKey(newAccountPublicKey)
             .setInitialBalance( Hbar.fromTinybars(1000))
             .execute(client);
        
     // Get the new account ID
        AccountId newAccountId = newAccount.getReceipt(client).accountId;

        //Log the account ID
        System.out.println("The new account ID is: " +newAccountId);
        
        return newAccountId;
        
	}

	public static AccountBalance checkBalance(Client client, AccountId newAccountId) throws TimeoutException, PrecheckStatusException
	{
	      //Check the new account's balance
        AccountBalance accountBalance = new AccountBalanceQuery()
             .setAccountId(newAccountId)
             .execute(client);

        System.out.println("Current hbar   account balance is: " +accountBalance.hbars);
        System.out.println("Current tokens account balance is: " +accountBalance.tokens);
        
        return accountBalance;
	}
	
	public static void transferHbar(Client client, AccountId myAccountId, AccountId newAccountId, int amount) throws TimeoutException, PrecheckStatusException, ReceiptStatusException
	{
		TransactionResponse sendHbar = new TransferTransaction()
			     .addHbarTransfer(myAccountId, Hbar.fromTinybars(-amount)) //Sending account
			     .addHbarTransfer(newAccountId, Hbar.fromTinybars(amount)) //Receiving account
			     .execute(client);
		
		System.out.println("The transfer transaction was: " +sendHbar.getReceipt(client).status);
		
		//Request the cost of the query
		Hbar queryCost = new AccountBalanceQuery()
		     .setAccountId(newAccountId)
		     .getCost(client);

		System.out.println("The cost of this query is: " +queryCost);
	}
	
    public static void main(String[] args) throws TimeoutException, PrecheckStatusException, ReceiptStatusException {

        //Grab your Hedera testnet account ID and private key
        AccountId myAccountId = AccountId.fromString(Dotenv.load().get("MY_ACCOUNT_ID"));
        PrivateKey myPrivateKey = PrivateKey.fromString(Dotenv.load().get("MY_PRIVATE_KEY"));  
                
        //Create your Hedera testnet client
        Client client = Client.forTestnet();
        client.setOperator(myAccountId, myPrivateKey);
    	
    	//AccountId newAccountID  = createNewAccount(client);
        
        /*
        AccountId newAccountID = AccountId.fromString("0.0.34319243");
        

    	*/
        
        AccountId kimAccountId = AccountId.fromString(Dotenv.load().get("KIM_ACCOUNT_ID"));
        System.out.println("Kim Account ID: "+kimAccountId);
        
        System.out.println("Checking My Account balance:");
    	checkBalance(client, myAccountId);

        System.out.println("Checking Transfer Account balance:");
    	checkBalance(client, kimAccountId);
    	
    	transferHbar(client, myAccountId, kimAccountId, 10000000);
    	
    	checkBalance(client, kimAccountId);
        
        AccountBalance kimAccountBalance = checkBalance(client, kimAccountId);
        
        for (TokenId tokenId:kimAccountBalance.tokens.keySet())
        {
        	TokenInfo tokenInfo = new TokenInfoQuery()
        		    .setTokenId(tokenId)
        		    .execute(client);
        	
        	System.out.println(tokenInfo);
        }
        	     
    }
}