package hbarTexting;

import java.io.IOException;

import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MongoDb 
{

	
	
    public static void main(String[] args) throws IOException 
    {
    	
    	String mongoApiKey = "\""+Dotenv.load().get("MONGO_API_KEY")+"\"";
    	
    	
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, "{\n    \"collection\":\"HBARquotes\",\n    \"database\":\"HBAR\",\n    \"dataSource\":\"Cluster0\",\n    \"projection\": {\"_id\": 1}\n\n}");
        Request request = new Request.Builder()
          .url("https://data.mongodb-api.com/app/data-gguma/endpoint/data/v1/action/findOne")
          .method("POST", body)
          .addHeader("Content-Type", "application/json")
          .addHeader("Access-Control-Request-Headers", "*")
          .addHeader("api-key", mongoApiKey)
          .build();
        Response response = client.newCall(request).execute();
        
        System.out.println(response);
        
        body = RequestBody.create(mediaType, "{\n    \"collection\":\"HBARquotes\",\n    \"database\":\"HBAR\",\n    \"dataSource\":\"Cluster0\",\n  \"timestamp\":\"2022-06-22 17:57:35.118\",\"HBAR_USD\":{\"$numberDouble\":\"0.07298\"}  \n\n}");
        request = new Request.Builder()
                .url("https://data.mongodb-api.com/app/data-gguma/endpoint/data/v1/action/insertOne")
                .method("POST", body)
                .addHeader("Content-Type", "application/ejson")
                .addHeader("Access-Control-Request-Headers", "*")
                .addHeader("api-key", mongoApiKey)
                .build();
        
        response = client.newCall(request).execute();
        
        System.out.println(response);
    
        
        body = RequestBody.create(mediaType, "{\n    \"collection\":\"HBARquotes\",\n    \"database\":\"HBAR\",\n    \"dataSource\":\"Cluster0\",\n  \"filter\": { \"timestamp\": \"2022-06-22 19:03:42.826\" } \n\n}");
        request = new Request.Builder()
                .url("https://data.mongodb-api.com/app/data-gguma/endpoint/data/v1/action/findOne")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Access-Control-Request-Headers", "*")
                .addHeader("api-key", mongoApiKey)
                .build();
        
        response = client.newCall(request).execute();
        
        System.out.println(response);
        
    }

}
