package hedera.topics;

import java.io.IOException;
import java.net.URL;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hedera.hashgraph.sdk.TopicId;

import helpers.JsonHelper;

public class Chunk {

	public static final String hederanet = "https://testnet.mirrornode.hedera.com";
	private static URL url;
	private TopicId topicId;
	private Message[] messages;
	private URL link;
	
	public Chunk(TopicId topicId_) throws IOException
	{
		topicId = topicId_;
	  url = new URL("https://testnet.mirrornode.hedera.com/api/v1/topics/"+topicId+"/messages");
	  JsonObject json =JsonHelper.readJsonFromUrl(""+url);
	  messages = this.getMessagesFromJson(json);
	  String urlStr = (hederanet+""+((JsonObject)((JsonObject)json).get("links")).get("next")).replace("\"", "");
	  setLink(new URL(urlStr));
	  
	  //System.out.println(getLink());
	  
	}
	
	public Chunk(URL url_) throws IOException
	{
		topicId = TopicId.fromString((""+url_).substring(52, 63));
		url = url_;
	  JsonObject json =JsonHelper.readJsonFromUrl(""+url);
	  messages = this.getMessagesFromJson(json);
	  JsonElement next = ((JsonObject)((JsonObject)json).get("links")).get("next");
	  if ("null".compareTo(""+next) != 0)
	  {
	  	setLink(new URL(hederanet+(""+next).replace("\"", "")));
	  }	  
	}
	 
	 
	public Message[] getMessagesFromJson(JsonObject json) throws IOException
	{   
		JsonArray jarr = json.getAsJsonArray("messages");		
    ObjectMapper mapper = new ObjectMapper();
    Message[] messages = new Message[jarr.size()];
    
    try {
      for (int i=0; i<jarr.size(); i++)
      	messages[i] = mapper.readValue(""+jarr.get(i), Message.class);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
    
		return messages;
	}
	
	public Chunk next() throws IOException
	{
		
		if (this.getLink() != null)
		{
			return new Chunk(getLink());
		}
		else
		{
			return null;
		}
		
	}
	
	
	public Chunk(TopicId topicId_, String link_)
	{
		topicId = topicId_;
		
	}

	public static void main(String[] args) throws IOException
	{
		TopicId topicId = TopicId.fromString("0.0.4256430");
		Chunk chunk = new Chunk(topicId);
				
		URL link = new URL(hederanet+"/api/v1/topics/0.0.4256430/messages?timestamp=gt:1714924395.005769586");
		
		chunk = new Chunk(link);
		
		while (chunk.getLink() != null)
		{
			chunk = chunk.next();
		}
		
	}

	public Message[] getMessages() {
		return messages;
	}

	public void setMessages(Message[] messages) {
		this.messages = messages;
	}

	public URL getLink() {
		return link;
	}

	public void setLink(URL link) {
		this.link = link;
	}

}
