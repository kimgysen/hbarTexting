package hbarTexting;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.ContractId;
import com.hedera.hashgraph.sdk.FileId;

import io.github.cdimascio.dotenv.Dotenv;

public class JsonReader {

	  public static final FileId byteCodeFileId = FileId.fromString("0.0.45925852"); 
	
	  public static JsonObject readJsonFromUrl(String url) throws IOException 
	  {
		Gson gson = new Gson();
	    InputStream is = new URL(url).openStream();
	    try {
	      JsonObject json = gson.fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), JsonObject.class);
	      return json;
	    } finally {
	      is.close();
	    }
	  }
	  
	  public static boolean  isContractDeployed(AccountId accountId) throws IOException
	  {
		  	boolean ret = false;
		  
			System.out.println("retrieve account smart contract list:" + accountId);
			JsonObject json = readJsonFromUrl("https://testnet.mirrornode.hedera.com/api/v1/transactions/?account.id="+accountId+"&transactionType=contractcreateinstance");
		    JsonArray jarr = json.getAsJsonArray("transactions");

		    
		    for (int i=0; i<jarr.size(); i++)
		    {
		    	JsonObject jo = (JsonObject) jarr.get(i);
		    	String contractStr = (""+jo.get("entity_id")).replace("\"", "");
			    if (!contractStr.equals("null"))
			    	{
				    	// get file_id from contract_id
			    		ContractId contractId = ContractId.fromString(contractStr);	    		
					    json = readJsonFromUrl("https://testnet.mirrornode.hedera.com/api/v1/contracts/"+contractId);    
					    FileId fileId = FileId.fromString((""+json.get("file_id")).replace("\"", ""));
					    ret |=(fileId.equals(byteCodeFileId));
			    	}
		    }
		      
		  return ret;
	  }

	  public static void main(String[] args) throws IOException 
	  {
		AccountId myAccountId = AccountId.fromString(Dotenv.load().get("MY_ACCOUNT_ID"));
		AccountId client1AccountId = AccountId.fromString(Dotenv.load().get("CLIENT1_ACCOUNT_ID"));
		AccountId client2AccountId = AccountId.fromString(Dotenv.load().get("CLIENT1_ACCOUNT_ID"));
		AccountId kimAccountId = AccountId.fromString(Dotenv.load().get("KIM_ACCOUNT_ID"));
		
		
		System.out.println(""+isContractDeployed(myAccountId));
		System.out.println(""+isContractDeployed(client1AccountId));
		System.out.println(""+isContractDeployed(client2AccountId));
		System.out.println(""+isContractDeployed(kimAccountId));
		
		
	  }
	}