package bloomfilter.polynomialhash;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import org.apache.commons.math4.legacy.stat.inference.ChiSquareTest;

public class HashDistribution {
	private static PolynomialHash h;
	private int setSize;
	private String[] set;
	private int[] hashes;
	public boolean isRandom=false;
	
	public String accessLineInFile(String fileName, int lineNumber) throws Exception {
		String line = "";
		try (Stream<String> lines = Files.lines(Paths.get(fileName))) {
	    line = lines.skip(lineNumber-1).findFirst().get();
	}
    return line;
	}
	
	public HashDistribution(PolynomialHash h_, int setSize_) throws Exception
	{
		h = h_;
		setSize = setSize_;
		set = generateRandomTestSet();
		hashes = computeHashes();
		isRandom = isRandom(hashes);
		saveSet();
	}

	
	public HashDistribution(PolynomialHash h_) throws Exception
	{
		h = h_;
		set = loadSet();
		setSize = set.length;
		hashes = computeHashes();
		isRandom = isRandom(hashes);
		//write();
	}
	
	private String[] loadSet() throws IOException {
		Path filePath = new File("./src/main/java/ressources/set.txt").toPath();
		Charset charset = Charset.defaultCharset();        
		List<String> stringList = Files.readAllLines(filePath, charset);
		
		return stringList.toArray(new String[]{});
	}

	private String[] generateRandomTestSet() throws Exception {
		String[] randomSet = new String[setSize];
		Random rand = new Random();
		for (int i=0; i<setSize; i++)
		{
	    int position = rand.nextInt(466551);
	    randomSet[i] = accessLineInFile("./src/main/java/ressources/words.txt", position);
	    //System.out.println(randomSet[i]);
		}
		return randomSet;
	} 
	
	
	public  void write() throws IOException{
	  BufferedWriter outputWriter = null;
	  String fileName = "./src/main/java/ressources/Test"+h.p+"_"+h.M+"_"+setSize+"_1.txt";
	  outputWriter = new BufferedWriter(new FileWriter(fileName));
	  for (int i = 0; i < setSize; i++) {
	    outputWriter.write(Integer.toString(hashes[i]));
	    outputWriter.newLine();
	  }
	  outputWriter.flush();  
	  outputWriter.close();  
	}
	
	public  void saveSet() throws IOException{
	  BufferedWriter outputWriter = null;
	  String fileName = "./src/main/java/ressources/set.txt";
	  outputWriter = new BufferedWriter(new FileWriter(fileName));
	  for (int i = 0; i < setSize; i++) {
	    outputWriter.write(set[i]);
	    outputWriter.newLine();
	  }
	  outputWriter.flush();  
	  outputWriter.close();  
	}
	
	
	private int[] computeHashes() {
		int[] hashes = new int[setSize];
		for (int i=0; i<setSize; i++)
		{
			hashes[i] = h.hash(set[i]);
			//System.out.println(hashes[i]);
		}
		return hashes;
	}


	
	public static boolean isRandom(int[] randomNums)
	{

		long[][] values =  getFrequencies(randomNums);
		
		ChiSquareTest t = new ChiSquareTest();
		double p = t.chiSquareTest(values);
		if (p>0.9)
			System.out.println(h.p+", "+p);
		
		return p > 0.9;
	}


	private static long[][] getFrequencies(int[] nums)
	{
		int r = h.M/10;
		
		long[][] bins = new long[11][2];

		for (int i=0; i<nums.length; i++)
		{
			bins[nums[i]/r][1]++;
		}

		for (int i=0; i<nums.length/r+1; i++)
			bins[nums[i]/r][0] = (long)nums.length/r;
		
		return bins;
	}

	public static void main(String[] args) throws Exception {
	
		PolynomialHash h = new PolynomialHash(51, 1024);
		HashDistribution hd = new HashDistribution(h);	
		
	}

}
