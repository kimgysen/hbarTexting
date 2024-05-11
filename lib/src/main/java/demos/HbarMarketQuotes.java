package demos;

import java.io.IOException;
import java.sql.Timestamp;

import org.bson.BsonTimestamp;
import org.bson.conversions.Bson;

import com.google.gson.JsonObject;

import helpers.JsonHelper;

public class HbarMarketQuotes 
{
	
	public double HBAR_USD = 0;
	public double HBAR_EUR = 0;
	public double HBAR_JPY = 0;
	public double HBAR_GBP = 0;
	
	public Timestamp ts = null;
	
	public HbarMarketQuotes() throws IOException
	{
        ts = new Timestamp(System.currentTimeMillis());    
        
		JsonObject jo = JsonHelper.readJsonFromUrl("https://min-api.cryptocompare.com/data/price?fsym=HBAR&tsyms=USD,EUR,JPY,GBP");
		
		HBAR_USD = Double.valueOf(""+jo.get("USD"));
		HBAR_EUR = Double.valueOf(""+jo.get("EUR"));
		HBAR_JPY = Double.valueOf(""+jo.get("JPY"));
		HBAR_GBP = Double.valueOf(""+jo.get("GBP"));

	}
	
	public String toString()
	{
		return		 ""+ts+"\n"
					 +"HBAR/USD: "+HBAR_USD+"\n"
					 +"HBAR/EUR: "+HBAR_EUR+"\n"
					 +"HBAR/JPY: "+HBAR_JPY+"\n"
					 +"HBAR/GBP: "+HBAR_GBP+"\n"
					 ;
	}
	
	public static void main(String args[]) throws IOException
	{
		
		BsonTimestamp bts = new BsonTimestamp((int) (System.currentTimeMillis()/1000), 0);
		
		System.out.println(""+bts);
		
		HbarMarketQuotes q = new HbarMarketQuotes();
		System.out.println(""+q);
		
	}

}
