package hbarTopics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Hbar;

import helpers.JsonHelper;

public class WhalesWatcher 
{
	
	private static final String mainnet = "https://mainnet-public.mirrornode.hedera.com";
	
	private static final String testnet = "https://testnet.mirrornode.hedera.com";
	
	private static final String hederanet = mainnet;
	
	public static List<AccountId> getWhalesAccountList() throws IOException
	{
		List<AccountId> ret = new ArrayList<AccountId>();
		
		JsonObject json =JsonHelper.readJsonFromUrl(hederanet+"/api/v1/accounts?account.balance=gt:1000000000000000");
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
		
		JsonObject json =JsonHelper.readJsonFromUrl(hederanet+"/api/v1/accounts?account.balance=gt:"+limit);
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
	    	String next = hederanet+(""+links).replace("\"", "");
	    	System.out.println(next);
			json =JsonHelper.readJsonFromUrl(next);
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
		JsonObject json =JsonHelper.readJsonFromUrl(hederanet+"/api/v1/balances?account.id="+accountId);
	    JsonArray jarr = json.getAsJsonArray("balances");
	    JsonObject jo = (JsonObject) jarr.get(0);
	    balance = Hbar.fromTinybars(Long.valueOf(""+jo.get("balance")));
	    
		return balance;
	}
	
	
	public static List<String> getAllAccountNfts(AccountId accountId) throws IOException
	{
		List<String> metadata = null;
		
		Hbar balance = Hbar.from(0);
		JsonObject json =JsonHelper.readJsonFromUrl(hederanet+"/api/v1/accounts/"+accountId+"/nfts");
	    JsonArray jarr = json.getAsJsonArray("nfts");
	    
	    
	    if (jarr.size()>0)
	    {
	        metadata = new ArrayList<String>();
	    	for (int i=0; i< jarr.size(); i++)
	    	{
	    		JsonObject jo = (JsonObject) jarr.get(i);
	    		String encodedString = (""+jo.get("metadata")).replace("\"", "");	    
	    		byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
	    	
	    		System.out.println(new String(decodedBytes));
	    		
	    		metadata.add(new String(decodedBytes));
	    	}
	    }
	    
		return metadata;
	}
	
	public static String getAccountNfts(AccountId accountId) throws IOException
	{
		String metadata = null;
		
		Hbar balance = Hbar.from(0);
		JsonObject json =JsonHelper.readJsonFromUrl(hederanet+"/api/v1/accounts/"+accountId+"/nfts");
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
	
		String testnetLimit = "1500000000000";
		String mainnetLimit = "10000000000000";
		String limit = (hederanet.equals(mainnet)?testnetLimit:mainnetLimit);
		
		List<AccountId> whales = browzeWhalesByNfts(limit);
		
		for (int i=0; i<whales.size(); i++)
		{	
			AccountId accountId = whales.get(i); 
			Hbar balance = getAccountBalance(accountId);
			String nft = getAccountNfts(accountId);
			
			System.out.println("The whale:"+accountId+"\t has an account balance of "+balance+" and NFT: "+nft);
		}
	
		
		AccountId accountId = AccountId.fromString("0.0.29575514");
		
		List<String> nfts = getAllAccountNfts(accountId);
		
		
	}
	

}
