package hedera.database.index;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;

public class IndexNode<T> {
    private T value;
    private int sequence_number;
    private IndexNode left;
    private IndexNode right;
    
    public IndexNode(){
    }
    
		public IndexNode(T v, int sequence_number_) {
			this.value = v;
			this.sequence_number = sequence_number_;
		}


    public int getSequence_number() {
			return sequence_number;
		}


		public void setSequence_number(int sequence_number) {
			this.sequence_number = sequence_number;
		}
		
		public T getValue() {
			return value;
		}

		public void setValue(T value) {
			this.value = value;
		}

		public IndexNode getLeft() {
			return left;
		}

		public void setLeft(IndexNode left) {
			this.left = left;
		}

		public IndexNode getRight() {
			return right;
		}

		public void setRight(IndexNode right) {
			this.right = right;
		}

    public void saveJson(String fileName) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(new File(fileName), this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


		public static IndexNode fromJson(String fileName) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(new File(fileName), IndexNode.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
    		IndexNode n = IndexNode.fromJson("./indexOnBuyer.json");
        System.out.println(n);
    }
}
