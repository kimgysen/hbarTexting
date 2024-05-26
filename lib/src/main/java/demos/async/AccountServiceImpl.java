package demos.async;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.PublicKey;
import com.hedera.hashgraph.sdk.TransactionReceipt;

import io.github.cdimascio.dotenv.Dotenv;


@Slf4j
public class AccountServiceImpl {

	  private static AccountId myAccountId = AccountId.fromString(Dotenv.load().get("MY_ACCOUNT_ID"));
	  private static PrivateKey myPrivateKey = PrivateKey.fromString(Dotenv.load().get("MY_PRIVATE_KEY"));  

	  private static Client client = registerWithClient(myAccountId, myPrivateKey);  
	  
		static Client registerWithClient(AccountId myAccountId, PrivateKey myPrivateKey)
		{
			Client client = Client.forTestnet();
			client.setOperator(myAccountId, myPrivateKey);
			client.setDefaultMaxTransactionFee(new Hbar(100));
			client.setMaxQueryPayment(new Hbar(50));
	    
			return client;
		}

    public static void test() throws InterruptedException {
    	
    int sleepyTime = 500;
		PrivateKey privateKey = PrivateKey.generate();
		PublicKey publicKey = privateKey.getPublicKey();
		
		Function<TransactionReceipt, HederaAccountDto> lambda = (txnReceipt) -> new HederaAccountDto(txnReceipt, privateKey, publicKey);
		
		CompletableFuture<Void> txn = new AccountCreateTransaction()
				.setKey(publicKey)
				.executeAsync(client)
				.thenCompose(resp -> resp.getReceiptAsync(client))
				.thenApply(lambda)
				.exceptionally(ex -> {
					System.out.println("Error occurred: " + ex);
					return null;
				})
				.thenAccept(accountDto -> {
					System.out.println("accountId: " + accountDto.getAccountId());
				});
		
		
		while (!txn.isDone())
		{
			System.out.println("Bzzzzz.....");
			//Thread.sleep(sleepyTime);
		}
		
		System.out.println("async: "+txn);
		 
    }
    
    
    public static void main(String[] args) throws InterruptedException
    {
    	test();
    }
}
