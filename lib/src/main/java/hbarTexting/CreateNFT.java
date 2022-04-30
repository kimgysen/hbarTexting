package hbarTexting;

import com.hedera.hashgraph.sdk.*;
import io.github.cdimascio.dotenv.Dotenv;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

public class CreateNFT {
	
	public static AccountBalance checkBalance(Client client, AccountId newAccountId) throws TimeoutException, PrecheckStatusException
	{
	      //Check the new account's balance
        AccountBalance accountBalance = new AccountBalanceQuery()
             .setAccountId(newAccountId)
             .execute(client);

        System.out.println("The new treasury account balance is: " +accountBalance.hbars);
        System.out.println("The new tokens account balance is: " +accountBalance.tokens);
        
        return accountBalance;
		
	}

    
	public static TokenId createNftToken(Client client, AccountId treasuryId, PrivateKey treasuryKey, PublicKey supplyKey) throws TimeoutException, PrecheckStatusException, ReceiptStatusException
	{
		//Create the NFT
		TokenCreateTransaction nftCreate = new TokenCreateTransaction()
		        .setTokenName("diploma")
		        .setTokenSymbol("GRAD")
		        .setTokenType(TokenType.NON_FUNGIBLE_UNIQUE)
		        .setDecimals(0)
		        .setInitialSupply(0)
		        .setTreasuryAccountId(treasuryId)
		        .setSupplyType(TokenSupplyType.FINITE)
		        .setMaxSupply(250)
		        .setSupplyKey(supplyKey)
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
	
	public static void associateNftToAccount() throws TimeoutException, PrecheckStatusException, ReceiptStatusException
	{
		
        //Grab your Hedera testnet account ID and private key
        AccountId myAccountId = AccountId.fromString(Dotenv.load().get("MY_ACCOUNT_ID"));
        PrivateKey myPrivateKey = PrivateKey.fromString(Dotenv.load().get("MY_PRIVATE_KEY")); 
        
        //Create your Hedera testnet client
        Client client = Client.forTestnet();
        client.setOperator(myAccountId, myPrivateKey);
		
        //Cedric s NFT token id
        TokenId tokenId = TokenId.fromString("0.0.34335146");
        
		//Create the associate transaction and sign with Alice's key 
		TokenAssociateTransaction associateAliceTx = new TokenAssociateTransaction()
		        .setAccountId(myAccountId)
		        .setTokenIds(Collections.singletonList(tokenId))
			.freezeWith(client)
		        .sign(myPrivateKey);

		//Submit the transaction to a Hedera network
		TransactionResponse associateAliceTxSubmit = associateAliceTx.execute(client);

		//Get the transaction receipt
		TransactionReceipt associateAliceRx = associateAliceTxSubmit.getReceipt(client);

		//Confirm the transaction was successful
		System.out.println("NFT association with Kim's account: " +associateAliceRx.status);

	}
	
    public static void main(String[] args) throws TimeoutException, PrecheckStatusException, ReceiptStatusException
    {
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
}
