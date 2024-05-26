package demos.async;

import lombok.Builder;
import lombok.Data;

import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.PublicKey;
import com.hedera.hashgraph.sdk.TransactionReceipt;

@Builder
@Data
public class HederaAccountDto {
	AccountId accountId;
	PrivateKey privateKey;
	PublicKey publicKey;
	public  HederaAccountDto(TransactionReceipt txnReceipt, PrivateKey privateKey_, PublicKey publicKey_) {
		accountId = txnReceipt.accountId;
		privateKey = privateKey_;
		publicKey_ = publicKey_;
		return ;
	}
	
	public AccountId getAccountId() {
		return accountId;
	}

}
