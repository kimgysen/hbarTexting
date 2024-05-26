package demos.async;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.PublicKey;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.TransactionResponse;



@AllArgsConstructor
@Slf4j
public class HederaAccountServiceImpl  implements HederaAccountService {

	private final Client client = null;

	@Override
	public CompletableFuture<TransactionResponse> createAccountAsync(PublicKey publicKey) {
		return new AccountCreateTransaction()
				.setKey(publicKey)
				.executeAsync(client);

	}

	@Override
	public CompletableFuture<TransactionReceipt> getReceiptAsync(TransactionResponse response) {
		return response.getReceiptAsync(client);
	}

}
