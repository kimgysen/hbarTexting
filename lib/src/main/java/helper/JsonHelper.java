package helper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;

import org.bson.Document;

import com.google.gson.Gson;
import com.google.gson.JsonObject;


public class JsonHelper {

	
	  public static JsonObject readJsonFromUrl(String url) throws IOException 
	  {
		Gson gson = new Gson();
	    InputStream is = new URL(url).openStream();
	    try {
	      JsonObject json = gson.fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), JsonObject.class);
	      return json;
	    } finally {
	      is.close();
	    }
	  }
	  
	  
	  public static Document convertFileToDocument(File file) throws IOException
	  {

		  byte[] filecontent =  Files.readAllBytes(file.toPath());
		  byte[] encoded = Base64.getEncoder().encode(filecontent);
		  
		  Document doc = new Document("filename", file.getName())
      				 		.append("filecontent", new String(encoded))
      				 ;
		  return doc;
	  }

	  public static File convertDocumentToFile(Document doc, String folder) throws IOException
	  {
		  
		String fileName = "./"+folder+"/"+doc.get("filename");

		File file = new File(fileName);  
		  
		String bytes = ""+doc.get("filecontent");
				
		byte[] decoded = Base64.getDecoder().decode(bytes.getBytes());
		
		try (FileOutputStream outputStream = new FileOutputStream(file)) {
		    outputStream.write(decoded);
		}
		
		return file;
	  }
	  
	  public static String doc2jsonString(Document doc) 
	  {
			return ""+doc.toJson();
	  }
	  
	  public static File doc2jsonFile(Document doc) throws FileNotFoundException, IOException
	  {
		  String fileName = "./tmp/"+(""+doc.get("filename")).replace(".", "_")+".json";
		  
		  File file = new File(fileName);  
		  
		  byte[] content = (""+doc.toJson()).getBytes();
		  
			try (FileOutputStream outputStream = new FileOutputStream(file)) {
			    outputStream.write(content);
			}
			
		 return file;
	  }
	  
	  public static void main(String[] args) throws IOException 
	  {

		File testFile = new File("test.pdf");
	
		Document doc = convertFileToDocument(testFile);
		
		//File file = convertDocumentToFile(doc, "download");
		
		doc2jsonFile(doc);
		
		
	  }
	}