
package demos.async;


import java.util.concurrent.CompletableFuture;
import com.hedera.hashgraph.sdk.PublicKey;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.TransactionResponse;

public interface HederaAccountService {


	CompletableFuture<TransactionResponse> createAccountAsync(PublicKey publicKey);


	CompletableFuture<TransactionReceipt> getReceiptAsync(TransactionResponse response);
}
