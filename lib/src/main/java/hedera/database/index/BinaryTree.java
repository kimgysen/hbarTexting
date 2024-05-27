package hedera.database.index;

import java.util.ArrayList;
import java.util.List;



public class BinaryTree<T extends Comparable<? super T>> {

  private Node root = new Node();
  
  private List<T> sortedValues;
  
  public BinaryTree(T value) {
  	root.setValue(value);
  }

	public BinaryTree(Node root_) {
		this.setRoot(root_);
	}

	public Node<T> getRoot()
  {
  	return this.root;
  }

  private void setRoot(Node<T> node) {
		this.root = node;
	}

  
  public List<T> getSortedValues()
  {
  	sortedValues = new ArrayList<T>();
  	
  	addSortedValues(this.root);
  	
  	return sortedValues;
  }
  
  public void addSortedValues(Node node){
    if (node != null) {
    	addSortedValues(node.getLeft());
    	sortedValues.add((T) node.getValue());
      addSortedValues(node.getRight());
  }
 }

  
  private Node<T> addRecursive(Node<T> current, T value) {
    if (current == null) {
    	  Node<T> n = new Node<T>();
    		n.setValue(value);
        return n;
    }

    if ( compare(value, current.getValue()) < 0 ) {
        current.setLeft(addRecursive(current.getLeft(), value));
    } else if ( compare(value, current.getValue()) >0) {
        current.setRight(addRecursive(current.getRight(), value));
    } else {
        return current;
    }

    return current;
  }
  
  public void add(T value) {
    root = addRecursive(root, value);
  }
  
  public void saveJson(String fileName)
  {
  	root.saveJson(fileName);
  }
   
  
  private boolean containsNodeRecursive(Node current, T value) {
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
  
  public boolean containsNode(T value) {
    return containsNodeRecursive(root, value);
  }
  

  public static BinaryTree fromJson(String fileName) 
  {
  	return new BinaryTree(Node.fromJson(fileName));
  }

  
  private T findSmallestValue(Node root) {
    return root.getLeft() == null ? (T) root.getValue() : findSmallestValue(root.getLeft());
  }
  
  public void delete(T value) {
    root = deleteRecursive(root, value);
  }
  
  private Node<T> deleteRecursive(Node<T> current, T smallestValue2) {
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
  
  public static void traverseInOrder(Node node) {
    if (node != null) {
        traverseInOrder(node.getLeft());
        System.out.print(" " + node.getValue());
        traverseInOrder(node.getRight());
    }
  }
    
  public Node<T> getLeftMostNode()
  {
  	Node<T> node = this.root;
  	
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
  
  public static Node<Integer> rebalanceTreeNodes(int[] nums, int start, int end) {
    if (start > end) {
        return null;
    }

    // Find the middle element of the array
    int mid = (end + start) / 2;

    // Create a new TreeNode with the middle element as the root
    Node<Integer> root = new Node<Integer>(nums[mid]);

    // Recursively build the left and right subtrees
    root.setLeft(rebalanceTreeNodes(nums, start, mid - 1));
    root.setRight(rebalanceTreeNodes(nums, mid + 1, end));

    return root;
  }  
  
  public static BinaryTree rebalanceTree(BinaryTree<Integer> bt)
  {
  	List<Integer> list = bt.getSortedValues(); 
  	int[] nums = list.stream().mapToInt(i -> i).toArray();
  	Node root = rebalanceTreeNodes(nums, 0, nums.length-1);
  	BinaryTree ret = new BinaryTree(root);
  	
  	return ret;
  }
  
  public static <T> int compare(T value, T object){
		return ((Comparable<? super T>) value).compareTo(object);
	}

	public static void main(String[] args) 
  {
  	
  	BinaryTree bt = BinaryTree.fromJson("./indexOnSerialId.json");
  	System.out.println(bt);
  	
  	BinaryTree balanced = BinaryTree.rebalanceTree(bt);
  	
  	BinaryTree.traverseInOrder(balanced.root);
  	
  	balanced.saveJson("balanced.json");
  	
  }
  
}