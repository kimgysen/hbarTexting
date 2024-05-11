package demos;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MongoShell {
	
	private static final String mongosh = "C:\\Users\\Lorand\\OneDrive\\Desktop\\mongosh-1.5.0-win32-x64\\bin\\mongosh";

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
	

	public static void runCmd() throws IOException, InterruptedException
	{
		
		String[] cmd = {mongosh, 
						"mongodb://kitakazenoaobara:Girolles05*@kitakazenoaobara.provendb.io/kitakazenoaobara?ssl=true",
						"--quiet",
						"--ssl",
						"getProof.js"
						};
		Process process;
		process = Runtime.getRuntime()
		     .exec(cmd);

		StreamGobbler streamGobbler = 
		  new StreamGobbler(process.getInputStream(), System.out::println);
		Executors.newSingleThreadExecutor().submit(streamGobbler);

		int ret = process.waitFor();
		
		
		Runtime.getRuntime().halt(0);
			
	}
	
	
    public static void main(String[] args) throws IOException, InterruptedException 
    {
    	runCmd();
    }
}