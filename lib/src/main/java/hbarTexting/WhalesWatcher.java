package hbarTexting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Hbar;

public class WhalesWatcher 
{
	
	public static List<AccountId> getWhalesAccountList() throws IOException
	{
		List<AccountId> ret = new ArrayList<AccountId>();
		
		JsonObject json =JsonReader.readJsonFromUrl("https://mainnet-public.mirrornode.hedera.com/api/v1/accounts?account.balance=gt:50000000000000000");
	    JsonArray jarr = json.getAsJsonArray("accounts");
		
	    for (int i=0; i<jarr.size(); i++)
	    {
	    	JsonObject jo = (JsonObject) jarr.get(i);
	    	String contractStr = (""+jo.get("account")).replace("\"", "");
	    	AccountId accountId = AccountId.fromString(contractStr);
	    	ret.add(accountId);
	    }
	    
		return ret;
	}
	
	public static Hbar getAccountBalance(AccountId accountId) throws IOException
	{
		Hbar balance = Hbar.from(0);
		JsonObject json =JsonReader.readJsonFromUrl("https://mainnet-public.mirrornode.hedera.com/api/v1/balances?account.id="+accountId);
	    JsonArray jarr = json.getAsJsonArray("balances");
	    JsonObject jo = (JsonObject) jarr.get(0);
	    balance = Hbar.fromTinybars(Long.valueOf(""+jo.get("balance")));
	    
		return balance;
	}
	
	public static void main(String args[]) throws IOException
	{
		List<AccountId> whales = getWhalesAccountList();
		for (int i=0; i<whales.size(); i++)
		{	
			AccountId accountId = whales.get(i); 
			Hbar balance = getAccountBalance(accountId);
			
			System.out.println("The whale:"+accountId+"\t has an account balance of "+balance);
		}
	}
	

}
