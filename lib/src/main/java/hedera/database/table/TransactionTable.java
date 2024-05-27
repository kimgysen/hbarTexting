package hedera.database.table;

import java.io.IOException;
import java.util.List;

import hedera.database.index.Index;
import hedera.database.index.IndexNode;


public class TransactionTable extends RawTable {
	

	public int recordsNumber;
	
	public String[] seller;
	public String[] buyer;
	public int[] 		serialid;
	public double[] price;
	public int[] 		sequence_number;

	public TransactionTable() throws IOException {
		super();
		List<String> rawRecords = this.getRawRecords(); 
		List<Integer> sequenceNumbers = this.getSequenceNumbers();
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
			sequence_number[i] = sequenceNumbers.get(i);
		}
	}
	
	private static void createIndexOnBuyer() throws IOException {
		TransactionTable t = new TransactionTable();
		
		Index IndexOnBuyer = new Index(new IndexNode(t.buyer[1], t.sequence_number[1]));
		for (int i=2; i<t.buyer.length; i++)
			IndexOnBuyer.add(t.buyer[i], t.sequence_number[i]);
	
		IndexOnBuyer.saveJson("indexOnbuyer.json");
		Index.traverseInOrder(IndexOnBuyer.getRoot());		
	}
	
	public static void createIndexOnSeller() throws IOException
	{
		TransactionTable t = new TransactionTable();
		
		Index IndexOnSeller = new Index(new IndexNode(t.seller[1], t.sequence_number[1]));
		for (int i=2; i<t.seller.length; i++)
			IndexOnSeller.add(t.seller[i], t.sequence_number[i]);
	
		IndexOnSeller.saveJson("indexOnSeller.json");
		Index.traverseInOrder(IndexOnSeller.getRoot());	
	}

	public static void createIndexOnSerialid() throws IOException
	{
		TransactionTable t = new TransactionTable();
		
		Index IndexOnSerialid = new Index(new IndexNode(t.serialid[1], t.sequence_number[1]));
		for (int i=2; i<t.serialid.length; i++)
			IndexOnSerialid.add(t.serialid[i], t.sequence_number[i]);
	
		IndexOnSerialid.saveJson("indexOnSerialid.json");
		Index.traverseInOrder(IndexOnSerialid.getRoot());	
	}

	public static void createIndexOnPrice() throws IOException
	{
		TransactionTable t = new TransactionTable();
		
		Index IndexOnPrice = new Index(new IndexNode(t.price[1], t.sequence_number[1]));
		for (int i=2; i<t.price.length; i++)
			IndexOnPrice.add(t.price[i], t.sequence_number[i]);
	
		IndexOnPrice.saveJson("indexOnPrice.json");
		Index.traverseInOrder(IndexOnPrice.getRoot());	
	}

	
	public static void main(String[] args) throws IOException 
	{
		 //createIndexOnBuyer();

		 //createIndexOnSeller();

		 //createIndexOnSerialid();
		
		 //createIndexOnPrice();

	}


	
}
