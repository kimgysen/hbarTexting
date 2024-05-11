package hbarTopics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.TopicId;

import helpers.JsonHelper;
import io.github.cdimascio.dotenv.Dotenv;

public class HederaDBQuery {
	
	private static TopicId myDbTopicId = null;
	
	public HederaDBQuery()
	{
		myDbTopicId = TopicId.fromString(Dotenv.load().get("MY_DB_TOPIC_ID"));
	}
	
	
	public HederaDBQuery(String topicId)
	{
		myDbTopicId = TopicId.fromString(topicId);
	}
	
	public static void getDBReccords() throws IOException
	{
		List<AccountId> ret = new ArrayList<AccountId>();
		
		JsonObject json =JsonHelper.readJsonFromUrl("https://testnet.mirrornode.hedera.com/api/v1/topics/0.0.4256430/messages");
		
		JsonArray messages = json.getAsJsonArray("messages");
		
		System.out.println( (JsonObject)messages.get(0));
		
		/*
	    JsonArray jarr = json.getAsJsonArray("accounts");
		
	    for (int i=0; i<jarr.size(); i++)
	    {
	    	JsonObject jo = (JsonObject) jarr.get(i);
	    	String contractStr = (""+jo.get("account")).replace("\"", "");
	    	AccountId accountId = AccountId.fromString(contractStr);
	    	ret.add(accountId);
	    }
	    */
	}
	
	public static void main(String[] args) throws IOException
	{
		
		HederaDBQuery q = new HederaDBQuery();
		
		System.out.println(q.myDbTopicId);
		
		q.getDBReccords();
		
	}

}
