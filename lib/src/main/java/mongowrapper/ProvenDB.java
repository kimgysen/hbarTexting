package mongowrapper;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import org.bson.Document;

import helpers.JsonHelper;
import io.github.cdimascio.dotenv.Dotenv;


public class ProvenDB {
	
	private static final String mongosh = "C:\\Users\\Lorand\\OneDrive\\Desktop\\mongosh-1.5.0-win32-x64\\bin\\mongosh";
	
	private static final String mongoexport = "C:\\Users\\Lorand\\OneDrive\\Desktop\\mongodb-database-tools-windows-x86_64-100.5.3\\bin\\mongoexport";
	
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
	
	
	private void runMongoCmd(String mongoCmd) throws IOException, InterruptedException
	{
		String[] cmd = {mongoCmd, 
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
	
	private void runExportCmd(String mongoCmd) throws IOException, InterruptedException
	{
		String[] cmd = {
				mongoexport, 
				conn,
				"--db=kitakazenoaobara",
				"--collection=pdf",
				"--out=./download/tmp.json",
				mongoCmd
				};
		
		System.out.println(String.join(" ", cmd));
		
		Process process;
		process = Runtime.getRuntime()
						.exec(cmd);

		StreamGobbler streamGobbler = 
		new StreamGobbler(process.getErrorStream(), System.out::println);
		Executors.newSingleThreadExecutor().submit(streamGobbler);

		int ret = process.waitFor();
		
	};
	
	private void runJavaScript(String JsScriptName) throws IOException, InterruptedException
	{
		String[] cmd = {mongosh, 
				conn,
				"--quiet",
				"--file",
				JsScriptName
				};
		
		System.out.println(String.join(" ", cmd));
		
		
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
		runMongoCmd(cmd);
		
	}
	
	private void uploadFile(String Collection, File file) throws IOException, InterruptedException
	{
		Document doc = JsonHelper.convertFileToDocument(file);
		
		String fileName = doc2insertJsFile("pdf", doc);
		
		runJavaScript(fileName);
	}
	
	private void downloadFile(String collection, String fileName) throws IOException, InterruptedException
	{
		runExportCmd("--query=\"{\\\"filename\\\":\\\""+fileName+"\\\"}");
		
		Path filePath = Path.of("./download/tmp.json");

		String content = Files.readString(filePath);
		
		Document doc = Document.parse(content);
		
		File file = JsonHelper.convertDocumentToFile(doc, "download");
		
		new File("./download/tmp.json").delete();
		
		Runtime.getRuntime().halt(0);
	}
	
    public static void main(String[] args) throws IOException, InterruptedException 
    {
    	ProvenDB p = new ProvenDB();
    	
    	File file = new File("test1.pdf");

    	p.uploadFile("pdf", file);
    	
    	//p.runMongoCmd("'db.pdf.find({'filename':'test.pdf'});'");
    	//p.downloadFile("pdf", "test.pdf");
    			
    }
}