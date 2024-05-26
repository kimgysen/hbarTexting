package hedera.database.table;

import java.io.IOException;
import java.util.List;

import bloomfilter.binarytree.BinaryTree;

public class TransactionTable extends RawTable {
	

	public int recordsNumber;
	
	public String[] seller;
	public String[] buyer;
	public int[] 		serialid;
	public double[] price;
	public int[] sequence_number;

	public TransactionTable() throws IOException {
		super();
		List<String> rawRecords = this.getRawRecords(); 
		recordsNumber = rawRecords.size();
		seller 		= 	new String[recordsNumber];
		buyer 		= 	new String[recordsNumber];
		serialid 	= 	new int[recordsNumber];
		price 		= 	new double[recordsNumber];
		sequence_number = new int[recordsNumber];
		
		for (int i=1; i<recordsNumber; i++)
		{
			String[] fields = rawRecords.get(i).split(",");
			
			seller[i] = fields[0];
			buyer[i]  = fields[1];
			serialid[i] = Integer.valueOf(fields[2].strip());
			price[i]    = Double.valueOf(fields[3].strip());
			sequence_number[i] = i;
		}
	}

	public static void main(String[] args) throws IOException 
	{
		TransactionTable t = new TransactionTable();
		BinaryTree IndexOnSerialid = new BinaryTree();
	
		for (int i=0; i<t.serialid.length; i++)
			IndexOnSerialid.add(t.serialid[i]);
		IndexOnSerialid.delete(0);
		
		
		IndexOnSerialid.saveJson("indexOnSerialId.json");
		IndexOnSerialid.saveXml("indexOnSerialId.xml");
		
		IndexOnSerialid.traverseInOrder(IndexOnSerialid.getRoot());

		
	}
	
}
