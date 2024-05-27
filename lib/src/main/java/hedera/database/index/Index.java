package hedera.database.index;

import java.util.ArrayList;
import java.util.LinkedHashMap;



public class Index<T extends Comparable<? super T>> {

  private IndexNode root ;
  
  private LinkedHashMap<Integer, T> sortedValues;
  
  public Index(T value) {
  	root.setValue(value);
  }

	public Index(IndexNode root_) {
		this.setRoot(root_);
	}

	public IndexNode<T> getRoot()
  {
  	return this.root;
  }

  private void setRoot(IndexNode<T> node) {
		this.root = node;
	}

  
  public LinkedHashMap<Integer, T> getSortedValues()
  {
  	sortedValues = new LinkedHashMap<Integer, T>();
  	
  	addSortedValues(this.root);
  	
  	return sortedValues;
  }
  
  public void addSortedValues(IndexNode node){
    if (node != null) {
    	addSortedValues(node.getLeft());
    	sortedValues.put(node.getSequence_number(), (T)node.getValue());
      addSortedValues(node.getRight());
  }
 }

  
  private IndexNode<T> addRecursive(IndexNode<T> current, T value, int sequence_number_) {
    if (current == null) {
    		IndexNode<T> n = new IndexNode<T>(value, sequence_number_);
        return n;
    }

    if ( compare(value, current.getValue()) < 0 ) {
        current.setLeft(addRecursive(current.getLeft(), value, sequence_number_));
    } else if ( compare(value, current.getValue()) >0) {
        current.setRight(addRecursive(current.getRight(), value, sequence_number_));
    } else {
        return current;
    }

    return current;
  }
  
  public void add(T value, int sequence_number_) {
    root = addRecursive(root, value, sequence_number_);
  }
  
  public void saveJson(String fileName)
  {
  	root.saveJson(fileName);
  }
   
  
  private boolean containsNodeRecursive(IndexNode current, T value) {
    if (current == null) {
        return false;
    } 
    if (value == current.getValue()) {
        return true;
    } 
    return ( compare(value, current.getValue()) < 0 )
      ? containsNodeRecursive(current.getLeft(), value)
      : containsNodeRecursive(current.getRight(), value);
  }
  
  public boolean containsNode(T value, int sequence_number_) {
    return containsNodeRecursive(root, value);
  }
  

  public static Index fromJson(String fileName) 
  {
  	return new Index(IndexNode.fromJson(fileName));
  }

  
  private T findSmallestValue(IndexNode indexNode) {
    return indexNode.getLeft() == null ? (T) indexNode.getValue() : findSmallestValue(indexNode.getLeft());
  }
  
  public void delete(T value) {
    root = deleteRecursive(root, value);
  }
  
  private IndexNode deleteRecursive(IndexNode<T> current, T smallestValue2) {
    if (current == null) {
        return null;
    }

    if (smallestValue2 == current.getValue()) {
    	if (current.getLeft() == null && current.getRight() == null) {
        return null;
    	}
    	
    	if (current.getRight() == null) {
        return current.getLeft();
    }

    if (current.getLeft() == null) {
        return current.getRight();
    }
    
    T smallestValue = findSmallestValue(current.getRight());
    current.setValue(smallestValue);
    current.setRight(deleteRecursive(current.getRight(), smallestValue));
    return current;
    } 
    
    if ( compare(smallestValue2, current.getValue()) < 0) {
        current.setLeft(deleteRecursive(current.getLeft(), smallestValue2));
        return current;
    }
    current.setRight(deleteRecursive(current.getRight(), smallestValue2));
    return current;
}
  
  public static void traverseInOrder(IndexNode indexNode) {
    if (indexNode != null) {
        traverseInOrder(indexNode.getLeft());
        System.out.print(" " + indexNode.getValue());
        traverseInOrder(indexNode.getRight());
    }
  }
    
  public IndexNode getLeftMostNode()
  {
  	IndexNode node = this.root;
  	
  	while (null != node.getLeft())
  		node = node.getLeft();
  	
  	return node;
  }
  
  public T getMeanValue()
  {
  	if (null == this.sortedValues)
  		this.sortedValues = getSortedValues();
  	return this.sortedValues.get(this.sortedValues.size()/2);
  }
  
  public  IndexNode<Integer> rebalanceTreeNodes(LinkedHashMap<Integer,T> list, int start, int end) {
    if (start > end) {
        return null;
    }

    // Find the middle element of the array
    int mid = (end + start) / 2;

    // Create a new TreeNode with the middle element as the root
    T value_ = (new ArrayList<T>(list.values())).get(mid);
    Integer sequence_number_ = (Integer) list.keySet().toArray()[mid];
    IndexNode root = new IndexNode(value_, sequence_number_);

    // Recursively build the left and right subtrees
    root.setLeft(rebalanceTreeNodes(list, start, mid - 1));
    root.setRight(rebalanceTreeNodes(list, mid + 1, end));

    return root;
  }  
  
  public  Index rebalanceTree()
  {
  	LinkedHashMap<Integer,T> list = this.getSortedValues(); 
  	IndexNode root = rebalanceTreeNodes(list, 0, list.size()-1);
  	Index ret = new Index(root);
  	
  	return ret;
  }
  
  public static <T> int compare(T value, T object){
		return ((Comparable<? super T>) value).compareTo(object);
	}

	public static void main(String[] args) 
  {
  	
  	Index bt = Index.fromJson("./indexOnBuyer.json");
  	System.out.println(bt);
  	
  	Index balanced = bt.rebalanceTree();
  	
  	Index.traverseInOrder(balanced.getRoot());
  	
  	balanced.saveJson("balanced.json");
  	
  }
  
}