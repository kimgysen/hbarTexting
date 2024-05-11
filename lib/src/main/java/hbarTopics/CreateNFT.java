package hbarTopics;

import com.hedera.hashgraph.sdk.*;
import io.github.cdimascio.dotenv.Dotenv;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class CreateNFT {
	
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

    
	public static TokenId createNftToken(Client client, AccountId treasuryId, PrivateKey treasuryKey, PublicKey supplyKey, String name, String symbol) throws TimeoutException, PrecheckStatusException, ReceiptStatusException
	{
		//Create the NFT
		TokenCreateTransaction nftCreate = new TokenCreateTransaction()
		        .setTokenName(name)
		        .setTokenSymbol(symbol)
		        .setTokenType(TokenType.NON_FUNGIBLE_UNIQUE)
		        .setDecimals(0)
		        .setInitialSupply(0)
		        .setTreasuryAccountId(treasuryId)
		        .setSupplyType(TokenSupplyType.FINITE)
		        .setMaxSupply(250)
		        .setSupplyKey(supplyKey)
		        .setAdminKey(supplyKey)
		        .freezeWith(client);


		//Sign the transaction with the treasury key
		TokenCreateTransaction nftCreateTxSign = nftCreate.sign(treasuryKey);

		//Submit the transaction to a Hedera network
		TransactionResponse nftCreateSubmit = nftCreateTxSign.execute(client);

		//Get the transaction receipt
		TransactionReceipt nftCreateRx = nftCreateSubmit.getReceipt(client);

		//Get the token ID
		TokenId tokenId = nftCreateRx.tokenId;

		//Log the token ID
		System.out.println("Created NFT with token ID " +tokenId);
		
		return tokenId;
	}
	
	public static void mintToken(Client client, TokenId tokenId, PrivateKey supplyKey, String CID) throws TimeoutException, PrecheckStatusException, ReceiptStatusException
	{

		// Mint a new NFT
		TokenMintTransaction mintTx = new TokenMintTransaction()
		        .setTokenId(tokenId)
		        .addMetadata(CID.getBytes())
			.freezeWith(client);

		//Sign transaction with the supply key
		TokenMintTransaction mintTxSign = mintTx.sign(supplyKey);

		//Submit the transaction to a Hedera network
		TransactionResponse mintTxSubmit = mintTxSign.execute(client);

		//Get the transaction receipt
		TransactionReceipt mintRx = mintTxSubmit.getReceipt(client);

		//Log the serial number
		System.out.println("Created NFT " +tokenId + "with serial: " +mintRx.serials);
		   
	}
	
	public static void associateNftToAccount(Client client, TokenId tokenId, AccountId targetAccount, PrivateKey targetKey) throws TimeoutException, PrecheckStatusException, ReceiptStatusException
	{
		
        
		//Create the associate transaction and sign with Alice's key 
		TokenAssociateTransaction associateTargetTx = new TokenAssociateTransaction()
		        .setAccountId(targetAccount)
		        .setTokenIds(Collections.singletonList(tokenId))
		        .freezeWith(client)
		        .sign(targetKey);

		//Submit the transaction to a Hedera network
		TransactionResponse associateTargetTxSubmit = associateTargetTx.execute(client);

		//Get the transaction receipt
		TransactionReceipt associateTargetRx = associateTargetTxSubmit.getReceipt(client);

		//Confirm the transaction was successful
		System.out.println("NFT association with Target account: " +associateTargetRx.status);

	}
	
    public static void transferNFT(String[] args) throws TimeoutException, PrecheckStatusException, ReceiptStatusException
    {
    	// TODO  Heavy Cleanup required
    	
        //Grab your Hedera testnet account ID and private key
        AccountId myAccountId = AccountId.fromString(Dotenv.load().get("MY_ACCOUNT_ID"));
        PrivateKey myPrivateKey = PrivateKey.fromString(Dotenv.load().get("MY_PRIVATE_KEY")); 
        PublicKey myPublicKey = myPrivateKey.getPublicKey();
        AccountId kimAccountId = AccountId.fromString(Dotenv.load().get("KIM_ACCOUNT_ID"));
        
        //Create your Hedera testnet client
        Client client = Client.forTestnet();
        client.setOperator(myAccountId, myPrivateKey);
        
        System.out.println("Checking My Account balance:");
        AccountBalance myAccountBalance = checkBalance(client, myAccountId);
        
        TokenId tokenId = (TokenId) myAccountBalance.tokens.keySet().toArray()[0];
        
        System.out.println(tokenId);
    	
    	//Create NFT token
    	//TokenId tokenId = createNftToken(client, myAccountId, myPrivateKey, myPublicKey);
    	
		// IPFS content identifiers for which we will create a NFT
		//String CID = ("QmNkU6m38wvTgyx5LkkgfAiSij2SYKJM2v9JKx29p38CEv") ;
		
		//mintToken(client, tokenId, myPrivateKey, CID);
		
    	//NftId nftId = TokenId.fromString("0.0.34335146").nft(1);
        
        NftId nftId = tokenId.nft(1);
        
    	List<TokenNftInfo> nftInfos = new TokenNftInfoQuery()
    		     .setNftId(nftId)
    		     .execute(client);
    	
    	System.out.println(new String(nftInfos.get(0).metadata, StandardCharsets.UTF_8));
    	
    	// Check the balance before the transfer for the treasury account
    	AccountBalance balanceCheckTreasury = new AccountBalanceQuery().setAccountId(myAccountId).execute(client);
    	System.out.println("Treasury balance: " +balanceCheckTreasury.tokens + "NFTs of ID " +tokenId);

    	// Check the balance before the transfer for Alice's account
    	AccountBalance balanceCheckAlice = new AccountBalanceQuery().setAccountId(kimAccountId).execute(client);
    	System.out.println("Alice's balance: " +balanceCheckAlice.tokens + "NFTs of ID " +tokenId);

    	// Transfer the NFT from treasury to Alice
    	// Sign with the treasury key to authorize the transfer
    	TransferTransaction tokenTransferTx = new TransferTransaction()
    	        .addNftTransfer( new NftId(tokenId, 1), myAccountId, kimAccountId)
    	        .freezeWith(client)
    	        .sign(myPrivateKey);

    	TransactionResponse tokenTransferSubmit = tokenTransferTx.execute(client);
    	TransactionReceipt tokenTransferRx = tokenTransferSubmit.getReceipt(client);

    	System.out.println("NFT transfer from Treasury to Alice: " +tokenTransferRx.status);

    	// Check the balance of the treasury account after the transfer
    	AccountBalance balanceCheckTreasury2 = new AccountBalanceQuery().setAccountId(myAccountId).execute(client);
    	System.out.println("Treasury balance: " +balanceCheckTreasury2.tokens + "NFTs of ID " + tokenId);

    	// Check the balance of Alice's account after the transfer
    	AccountBalance balanceCheckAlice2 = new AccountBalanceQuery().setAccountId(kimAccountId).execute(client);
    	System.out.println("Alice's balance: " +balanceCheckAlice2.tokens +  "NFTs of ID " +tokenId);

    }
    
    public static void deleteToken(Client client, TokenId tokenId, PrivateKey adminKey) throws TimeoutException, PrecheckStatusException, ReceiptStatusException
    {
    	//Create the transaction
    	TokenDeleteTransaction transaction = new TokenDeleteTransaction()
    	     .setTokenId(tokenId);

    	//Freeze the unsigned transaction, sign with the admin private key of the account, submit the transaction to a Hedera network
    	TransactionResponse txResponse = transaction.freezeWith(client).sign(adminKey).execute(client);

    	//Request the receipt of the transaction
    	TransactionReceipt receipt = txResponse.getReceipt(client);

    	//Obtain the transaction consensus status
    	Status transactionStatus = receipt.status;

    	System.out.println("The transaction consensus status is " +transactionStatus);

    	//v2.0.1
    }
    
    public static NftId createAndMintNftfromCid(Client client, AccountId treasuryId, PrivateKey treasuryKey, PublicKey supplyKey, String name, String Symbol, String CID) throws TimeoutException, PrecheckStatusException, ReceiptStatusException
    {
    	TokenId tokenId = createNftToken(client, treasuryId, treasuryKey, supplyKey, name, Symbol );
    	
    	mintToken(client, tokenId, treasuryKey, CID);
    	
		return null;  	
    }
    
    public static void listAccountNfts(Client client, AccountId accountId) throws TimeoutException, PrecheckStatusException
    {
	      //Check the new account's balance
        AccountBalance accountBalance = new AccountBalanceQuery()
             .setAccountId(accountId)
             .execute(client);

        for (TokenId tokenId:accountBalance.tokens.keySet())
        {
        	TokenInfo tokenInfo = new TokenInfoQuery()
        		    .setTokenId(tokenId)
        		    .execute(client);
        	
        	System.out.println(tokenInfo);
        }
    }
    
    public static void createTrashAccount(Client client) throws TimeoutException, PrecheckStatusException, ReceiptStatusException
    {
    	// Generate a new key pair
        PrivateKey trashPrivateKey = PrivateKey.generate();
        PublicKey trashPublicKey = trashPrivateKey.getPublicKey();

        //Create new account and assign the public key
        TransactionResponse trashAccount = new AccountCreateTransaction()
                .setKey(trashPublicKey)
                .setInitialBalance( Hbar.fromTinybars(1000))
                .execute(client);

        // Get the new account ID
        AccountId trashAccountId = trashAccount.getReceipt(client).accountId;

        System.out.println("trash account ID is: " +trashAccountId);
        System.out.println("trash account Private K: " +trashPrivateKey);

        //Check the new account's balance
        AccountBalance accountBalance = new AccountBalanceQuery()
                .setAccountId(trashAccountId)
                .execute(client);
    };
    
    public static void main(String[] args) throws TimeoutException, PrecheckStatusException, ReceiptStatusException
    {
    	
    	
        //Grab your Hedera testnet account ID and private key
    	AccountId kimAccountId = AccountId.fromString(Dotenv.load().get("KIM_ACCOUNT_ID"));
    	AccountId inziAccountId = AccountId.fromString(Dotenv.load().get("INZI_ACCOUNT_ID"));
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
 *    START testing area
 *         
 ****************/
 

       
       /*Create INZI NFTs*/
       
   	//Create funky kid NFT token
    /*
    String funkyKidCID = ("QmX46rrWbm1i3iMGGJU5ZsUmx3oBeCLF7oMzvrr6DwNpBb") ;
   	TokenId inziTokenId = createNftToken(client, myAccountId, myPrivateKey, myPublicKey, "Funky Kid", "INZI");   		
    mintToken(client, inziTokenId, myPrivateKey, funkyKidCID);
	*/
        
     NftId funkyKidTokenId = TokenId.fromString("0.0.47832833").nft(1);
   	
     
     //Create funky tongue NFT token

     /*
     String funkyTongueCID = ("QmPRqJwK6FpvPj177yDBzuUtMNKNykpP4y344tf4p91hTu") ;
     TokenId inziTokenId = createNftToken(client, myAccountId, myPrivateKey, myPublicKey, "Funky Tongue", "INZI");   		
     mintToken(client, inziTokenId, myPrivateKey, funkyTongueCID);
     */
     
     NftId funkyTongueTokenId = TokenId.fromString("0.0.47832902").nft(1);

       
    //   NftId nftId = tokenId.nft(1);
        
    listAccountNfts(client, myAccountId);
    
  if (false)
  {
	  
	  TokenId tokenId = TokenId.fromString("0.0.34366200");
	  
	  //associateNftToAccount(client, tokenId, kimAccountId, myPublicKey );
	  
	  
      //Create a transaction to schedule
        TransferTransaction transaction = new TransferTransaction()
    	        .addNftTransfer( new NftId(tokenId, 1), myAccountId, kimAccountId);
        
      //Schedule a transaction
        TransactionResponse scheduleTransaction = new ScheduleCreateTransaction()
             .setScheduledTransaction(transaction)
             .execute(client);

        //Get the receipt of the transaction
        TransactionReceipt receipt = scheduleTransaction.getReceipt(client);
             
        //Get the schedule ID
        ScheduleId scheduleId = receipt.scheduleId;
        System.out.println("The schedule ID is " +scheduleId);

        //Get the scheduled transaction ID
        TransactionId scheduledTxId = receipt.scheduledTransactionId;
        System.out.println("The scheduled transaction ID is " +scheduledTxId);
        
      //Submit the first signatures
        TransactionResponse signature1 = new ScheduleSignTransaction()
             .setScheduleId(scheduleId)
             .freezeWith(client)
             .sign(myPrivateKey)
             .execute(client);
             
        //Verify the transaction was successful and submit a schedule info request
        TransactionReceipt receipt1 = signature1.getReceipt(client);
        System.out.println("The transaction status is " +receipt1.status);

        ScheduleInfo query1 = new ScheduleInfoQuery()
             .setScheduleId(scheduleId)
             .execute(client);

        //Confirm the signature was added to the schedule  
        System.out.println(query1);
        
        
      //Submit the second signature
        TransactionResponse signature2 = new ScheduleSignTransaction()
             .setScheduleId(scheduleId)
             .freezeWith(client)
             .sign(trashPrivateKey)
             .execute(client);
             
        //Verify the transaction was successful
        TransactionReceipt receipt2 = signature2.getReceipt(client);
        System.out.println("The transaction status" +receipt2.status);
        
        
      //Get the schedule info
        ScheduleInfo query2 = new ScheduleInfoQuery()
            .setScheduleId(scheduleId)
            .execute(client);
            
        System.out.println(query2);
        
      //Get the scheduled transaction record
        TransactionRecord scheduledTxRecord = TransactionId.fromString(scheduledTxId.toString()).getRecord(client);
        System.out.println("The scheduled transaction record is: " +scheduledTxRecord);

 }     
        
 /***************************
 *
 *   END testing area
 *         
 ****************/
        
        System.out.println("Checking Trash balance:");
        myAccountBalance = checkBalance(client, trashAccountId);

        
    }
}
