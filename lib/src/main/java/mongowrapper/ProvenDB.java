package mongowrapper;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import org.bson.Document;

import helper.JsonHelper;
import io.github.cdimascio.dotenv.Dotenv;


public class ProvenDB {
	
	private static final String mongosh = "C:\\Users\\Lorand\\OneDrive\\Desktop\\mongosh-1.5.0-win32-x64\\bin\\mongosh";
	
	private static String conn = null;
	

	private ProvenDB()
	{
		conn = "mongodb://kitakazenoaobara:"+Dotenv.load().get("PROVENDB_ADMIN_KEY")+"@kitakazenoaobara.provendb.io/kitakazenoaobara?ssl=true";
	}

	private static class StreamGobbler implements Runnable {
	    private InputStream inputStream;
	    private Consumer<String> consumer;

	    public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
	        this.inputStream = inputStream;
	        this.consumer = consumer;
	    }

	    @Override
	    public void run() {
	        new BufferedReader(new InputStreamReader(inputStream)).lines()
	          .forEach(consumer);
	    }
	}
	
	
	private void runCmd(String mongoCmd) throws IOException, InterruptedException
	{
		String[] cmd = {mongosh, 
				conn,
				"--quiet",
				"--eval",
				mongoCmd
				};
		
		
		Process process;
		process = Runtime.getRuntime()
						.exec(cmd);

		StreamGobbler streamGobbler = 
		new StreamGobbler(process.getInputStream(), System.out::println);
		Executors.newSingleThreadExecutor().submit(streamGobbler);

		int ret = process.waitFor();

		Runtime.getRuntime().halt(0);
		
	};
	
	private void runJavaScript(String JsScriptName) throws IOException, InterruptedException
	{
		String[] cmd = {mongosh, 
				conn,
				"--quiet",
				"--file",
				JsScriptName
				};
		
		
		Process process;
		process = Runtime.getRuntime()
						.exec(cmd);

		StreamGobbler streamGobbler = 
		new StreamGobbler(process.getInputStream(), System.out::println);
		Executors.newSingleThreadExecutor().submit(streamGobbler);

		int ret = process.waitFor();

		Runtime.getRuntime().halt(0);
		
	};
	
	private String doc2insertJsFile(String collection, Document doc) throws FileNotFoundException, IOException
	{
		  String fileName = "./tmp/insert_"+(""+doc.get("filename")).replace(".", "_")+".js";
		  
		  File file = new File(fileName);  
		  
		  
		  byte[] content = ("db."+collection+".insertOne(\n"+doc.toJson()+"\n);").getBytes();
		  
			try (FileOutputStream outputStream = new FileOutputStream(file)) {
			    outputStream.write(content);
			}
			
		 return fileName;
	}

	private void uploadDocument(String collection, Document doc) throws IOException, InterruptedException
	{
		String cmd = "'db."+collection+".insertOne("+doc.toJson().replace("\"", "")+");'";
		System.out.println(cmd);
		runCmd(cmd);
		
	}
	
	private void uploadFile(String Collection, File file) throws IOException, InterruptedException
	{
		Document doc = JsonHelper.convertFileToDocument(file);
		
		String fileName = doc2insertJsFile("pdf", doc);
		
		runJavaScript(fileName);
	}
	
    public static void main(String[] args) throws IOException, InterruptedException 
    {
    	ProvenDB p = new ProvenDB();
    	
    	File file = new File("test.zip");

    	//p.uploadFile("pdf", file);
    	
    	p.runCmd("'db.pdf.find({'filename':'test.pdf'});'");
    			
    }
}