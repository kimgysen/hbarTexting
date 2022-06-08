package hbarTexting;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.FileUtils;

import com.google.common.io.Files;
import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.PublicKey;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.TransactionResponse;

import io.github.cdimascio.dotenv.Dotenv;

public class AccountsManager {
	
	private static final File accounts = new File("./src/main/java/accounts.txt");
	
	public static void createNewAccount(Client client, String rootName) throws TimeoutException, PrecheckStatusException, ReceiptStatusException, IOException
	{
        
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
        String accountVariable = rootName.toUpperCase()+"_ACCOUNT_ID=" +newAccountId;
        System.out.println(accountVariable);
        storeAccountVariable(accountVariable);
        
        accountVariable=rootName.toUpperCase()+"_PRIVATE_KEY=" +newAccountPrivateKey;
        System.out.println(accountVariable);
        storeAccountVariable(accountVariable);
        
        accountVariable=rootName.toUpperCase()+"_PUBLIC_KEY=" +newAccountPublicKey;
        storeAccountVariable(accountVariable+"\n\n");
        System.out.println(accountVariable);
           
	}

	// append a string to the end of the file
	private static void storeAccountVariable(String content) throws IOException {
		FileUtils.writeStringToFile(accounts, content+"\n" , StandardCharsets.UTF_8, true);
	}
	
	public static void main(String[] args) throws IOException, TimeoutException, PrecheckStatusException, ReceiptStatusException
	{
        AccountId myAccountId = AccountId.fromString(Dotenv.load().get("MY_ACCOUNT_ID"));
        PrivateKey myPrivateKey = PrivateKey.fromString(Dotenv.load().get("MY_PRIVATE_KEY"));  
         
        System.out.println(myAccountId);
        
        //Create your Hedera testnet client
        Client client = Client.forTestnet();
        client.setOperator(myAccountId, myPrivateKey);
        
        //createNewAccount(client, "CLIENT1");
        //createNewAccount(client, "SUPPLIER1");
        //createNewAccount(client, "SUPPLIER2");
        
        createNewAccount(client, "SUPPLIER3");
		
	}
	
}
