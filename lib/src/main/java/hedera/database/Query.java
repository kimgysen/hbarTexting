package hedera.database;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.hedera.hashgraph.sdk.TopicId;

import hedera.topics.Chunk;
import hedera.topics.Message;
import io.github.cdimascio.dotenv.Dotenv;

public class Query {
	
	private TopicId myDbTopicId = null;
	
	private List<Message> records = null; 
	
	public Query() throws IOException
	{
		myDbTopicId = TopicId.fromString(Dotenv.load().get("MY_DB_TOPIC_ID"));
		records = getAllReccords();
	}
	
	public Query(String topicId)
	{
		myDbTopicId = TopicId.fromString(topicId);
	}
	
	public List<Message> getAllReccords() throws IOException
	{
			List<Message> records = new ArrayList();
			
			Chunk chunk = new Chunk(myDbTopicId);			
			for (Message m:chunk.getMessages())
				records.add(m);
			
			while (chunk.getLink() != null)
			{
				chunk = chunk.next();
				for (Message m:chunk.getMessages())
					records.add(m);
			}
			
			return records;
	} 
	
	public static void main(String[] args) throws IOException
	{
		
		Query q = new Query();
		
		System.out.println(q.myDbTopicId);
		
		q.getAllReccords();
		
	}

}
