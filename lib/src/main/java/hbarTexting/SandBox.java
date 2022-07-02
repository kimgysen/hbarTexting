package hbarTexting;



import java.util.Date;

import org.bson.BsonTimestamp;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;



public class SandBox 
{
	
	public static void main(String[] args)
	{
		
		BsonTimestamp ts = new BsonTimestamp((int) (System.currentTimeMillis()/1000), 0);
		
		Date date = new Date(ts.getTime()*1000L);
		
		System.out.println(ts.getTime()+", "+date);
				
		DateTime dt = new DateTime(DateTimeZone.UTC);
		
		System.out.println(dt+ " " + dt.withZone(DateTimeZone.UTC).getMillis()/1000);
		
		Instant instant = Instant.parse("2017-01-25T09:28:04.041Z"); //Pass your date.

	}
}
