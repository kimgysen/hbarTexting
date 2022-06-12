package hbarTexting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Hbar;

public class WhalesWatcher 
{
	
	public static List<AccountId> getWhalesAccountList() throws IOException
	{
		List<AccountId> ret = new ArrayList<AccountId>();
		
		JsonObject json =JsonReader.readJsonFromUrl("https://mainnet-public.mirrornode.hedera.com/api/v1/accounts?account.balance=gt:1000000000000000");
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
	
	public static List<AccountId> browzeWhalesByNfts(String limit) throws IOException
	{
		List<AccountId> ret = new ArrayList<AccountId>();
		
		JsonObject json =JsonReader.readJsonFromUrl("https://mainnet-public.mirrornode.hedera.com/api/v1/accounts?account.balance=gt:"+limit+"&account.balance=lt:1000000000000000");
	    JsonArray jarr = json.getAsJsonArray("accounts");
	    JsonElement links = json.getAsJsonObject("links").get("next");
		
	    //System.out.println("null".equals(""+links));
	   
	    
	    for (int i=0; i<jarr.size(); i++)
	    {
	    	JsonObject jo = (JsonObject) jarr.get(i);
	    	String accountStr = (""+jo.get("account")).replace("\"", "");
	    	AccountId accountId = AccountId.fromString(accountStr);
	    	String nft = getAccountNfts(accountId);
	    	if (null!=nft) ret.add(accountId);
	    }
	   
	    
	    while (!"null".equals(""+links))
	    {
	    	String next = "https://mainnet-public.mirrornode.hedera.com"+(""+links).replace("\"", "");
	    	System.out.println(next);
			json =JsonReader.readJsonFromUrl(next);
		    jarr = json.getAsJsonArray("accounts");
		    links = json.getAsJsonObject("links").get("next");
		    
		    for (int i=0; i<jarr.size(); i++)
		    {
		    	JsonObject jo = (JsonObject) jarr.get(i);
		    	String accountStr = (""+jo.get("account")).replace("\"", "");
		    	AccountId accountId = AccountId.fromString(accountStr);
		    	
		    	String nft = getAccountNfts(accountId);
		    	if (null!=nft) ret.add(accountId);
		    }
		    
		    System.out.println(links);
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
	
	public static String getAccountNfts(AccountId accountId) throws IOException
	{
		String metadata = null;
		
		Hbar balance = Hbar.from(0);
		JsonObject json =JsonReader.readJsonFromUrl("https://mainnet-public.mirrornode.hedera.com/api/v1/accounts/"+accountId+"/nfts");
	    JsonArray jarr = json.getAsJsonArray("nfts");
	    
	    
	    if (jarr.size()>0)
	    {
	    	JsonObject jo = (JsonObject) jarr.get(0);
	
	    	String encodedString = (""+jo.get("metadata")).replace("\"", "");	    
	    	byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
	    	
	    	metadata = new String(decodedBytes);
	    }
	    
		return metadata;
	}
	
	public static void main(String args[]) throws IOException
	{
		/*
		String limit = "10000000000000";	
		List<AccountId> whales = browzeWhalesByNfts(limit);
		for (int i=0; i<whales.size(); i++)
		{	
			AccountId accountId = whales.get(i); 
			Hbar balance = getAccountBalance(accountId);
			String nft = getAccountNfts(accountId);
			
			System.out.println("The whale:"+accountId+"\t has an account balance of "+balance+" and NFT: "+nft);
		}
		*/
		
		JsonObject jo = JsonReader.readJsonFromUrl("ipfs://bafyreib2bdydvokah7snghhaighltakmwsjahasfe3rsqufaqa22hv7muq/metadata.json");
		System.out.println(jo);
		
		
		
	}
	

}
