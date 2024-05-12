package hedera.topics;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.TopicId;

import helpers.HederaTimestampConverter;
import helpers.JsonHelper;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Base64;


@JsonIgnoreProperties(ignoreUnknown = true)
public class Message {
    
    private String consensus_timestamp;		//: "1713883028.403168003",
    private String message;								//: "U0VMTEVSLCBCVVlFUiwgU0VSSUFMSUQsIFBSSUNF",
    private AccountId payer_account_id;		//: "0.0.4223028",
    private int sequence_number; 					//: 1,
    private TopicId topic_id;						//: "0.0.4256430"

  
    public LocalDateTime getConsensus_timestamp() {
    	long timestamp = Long.valueOf(consensus_timestamp.replace(".", ""));
			return HederaTimestampConverter.convertToDateTime(timestamp);
		}
 
		public void setConsensus_timestamp(String consensus_timestamp) {
			this.consensus_timestamp = consensus_timestamp;
		}

		public String getMessage() {
			return new String(Base64.getDecoder().decode(message));
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public AccountId getPayer_account_id() {
			return payer_account_id;
		}

		public void setPayer_account_id(AccountId payer_account_id) {
			this.payer_account_id = payer_account_id;
		}

		public int getSequence_number() {
			return sequence_number;
		}

		public void setSequence_number(int sequence_number) {
			this.sequence_number = sequence_number;
		}

		public TopicId getTopic_id() {
			return topic_id;
		}

		public void setTopic_id(TopicId topic_id) {
			this.topic_id = topic_id;
		}

		public void saveJson(String fileName) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(new File(fileName), this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Message fromJson(String fileName) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(new File(fileName), Message.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public String toString()
    {
    	return "id: "+sequence_number
    					+"\n message: "+getMessage()
    					+"\n timestamp: "+getConsensus_timestamp()
    					+"\n topicId: "+getTopic_id();
    }
    
    public static Message fromUrl(URL url) throws IOException
    {
      
    	JsonObject json =JsonHelper.readJsonFromUrl(""+url);
      
  		JsonArray messages = json.getAsJsonArray("messages");
  		
  		System.out.println( (JsonObject)messages.get(0));
  		
  		System.out.println( ((JsonObject)((JsonObject)json).get("links")).get("next"));

      ObjectMapper mapper = new ObjectMapper();
  		
      /*
      try {
          return mapper.readValue(url, Message.class);
      } catch (IOException e) {
          e.printStackTrace();
          return null;
      }
      */

  		
  		return null;
    }
    
		public static void main(String[] args) throws IOException {
			
    	Message n = Message.fromJson("message.json");
      System.out.println(n);
        
      URL url = new URL("https://testnet.mirrornode.hedera.com/api/v1/topics/0.0.4256430/messages/2");
      n = Message.fromUrl(url);
      System.out.println(n);
      
    }
}