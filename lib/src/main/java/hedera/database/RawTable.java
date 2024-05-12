package hedera.database;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.hedera.hashgraph.sdk.TopicId;

import hedera.topics.Chunk;
import hedera.topics.Message;
import io.github.cdimascio.dotenv.Dotenv;

public class RawTable {
	
	private TopicId myDbTopicId = null;
	private List<Message> messages = null;
	private List<String> rawRecords = null;
	
	public RawTable() throws IOException
	{
		myDbTopicId = TopicId.fromString(Dotenv.load().get("MY_DB_TOPIC_ID"));
		messages = getAllMessages();
		buildRawrecords();
	}
	
	public RawTable(String topicId) throws IOException
	{
		myDbTopicId = TopicId.fromString(topicId);
		messages = getAllMessages();
		buildRawrecords();
	}
	
	public void buildRawrecords()
	{
		rawRecords = new ArrayList<String>();
		for (Message m:messages)
			rawRecords.add(m.getMessage());
	}
	
	protected List<String> getRawRecords() {
		
		return this.rawRecords;
	}
	
	public List<Message> getAllMessages() throws IOException
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
		
		RawTable q = new RawTable();
		
	
		System.out.println(q.messages.get(0));
		
	}

}
