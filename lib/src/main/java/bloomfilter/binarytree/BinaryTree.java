package bloomfilter.binarytree;

import java.util.ArrayList;
import java.util.List;


import jakarta.xml.bind.JAXBException;

public class BinaryTree {

  private Node root = new Node();
  
  private List<Integer> sortedValues;
  
  public Node getRoot()
  {
  	return this.root;
  }

  private void setRoot(Node node) {
		this.root = node;
	}

  
  public List<Integer> getSortedValues()
  {
  	sortedValues = new ArrayList<Integer>();
  	
  	addSortedValues(this.root);
  	
  	return sortedValues;
  }
  
  public void addSortedValues(Node node){
    if (node != null) {
    	addSortedValues(node.getLeft());
    	sortedValues.add(node.getValue());
      addSortedValues(node.getRight());
  }
 }

  
  private Node addRecursive(Node current, int value) {
    if (current == null) {
    	  Node n = new Node();
    		n.setValue(value);
        return n;
    }

    if (value < current.getValue()) {
        current.setLeft(addRecursive(current.getLeft(), value));
    } else if (value > current.getValue()) {
        current.setRight(addRecursive(current.getRight(), value));
    } else {
        return current;
    }

    return current;
  }
  
  public void add(int value) {
    root = addRecursive(root, value);
  }
  
  public void saveJson(String fileName)
  {
  	root.saveJson(fileName);
  }
   
  public void saveXml(String fileName)
  {
  	root.saveXml(fileName);
  }

  
  private boolean containsNodeRecursive(Node current, int value) {
    if (current == null) {
        return false;
    } 
    if (value == current.getValue()) {
        return true;
    } 
    return value < current.getValue()
      ? containsNodeRecursive(current.getLeft(), value)
      : containsNodeRecursive(current.getRight(), value);
  }
  
  public boolean containsNode(int value) {
    return containsNodeRecursive(root, value);
  }
  
  public static BinaryTree fromXml(String fileName) throws JAXBException
  {
  	BinaryTree bt = new BinaryTree();
  	bt.root = Node.fromXml(fileName);
  	return bt;
  }

  public static BinaryTree fromJson(String fileName) throws JAXBException
  {
  	BinaryTree bt = new BinaryTree();
  	bt.root = Node.fromJson(fileName);
  	return bt;
  }

  
  private int findSmallestValue(Node root) {
    return root.getLeft() == null ? root.getValue() : findSmallestValue(root.getLeft());
  }
  
  public void delete(int value) {
    root = deleteRecursive(root, value);
  }
  
  private Node deleteRecursive(Node current, int value) {
    if (current == null) {
        return null;
    }

    if (value == current.getValue()) {
    	if (current.getLeft() == null && current.getRight() == null) {
        return null;
    	}
    	
    	if (current.getRight() == null) {
        return current.getLeft();
    }

    if (current.getLeft() == null) {
        return current.getRight();
    }
    
    int smallestValue = findSmallestValue(current.getRight());
    current.setValue(smallestValue);
    current.setRight(deleteRecursive(current.getRight(), smallestValue));
    return current;
    } 
    
    if (value < current.getValue()) {
        current.setLeft(deleteRecursive(current.getLeft(), value));
        return current;
    }
    current.setRight(deleteRecursive(current.getRight(), value));
    return current;
}
  
  public static void traverseInOrder(Node node) {
    if (node != null) {
        traverseInOrder(node.getLeft());
        System.out.print(" " + node.getValue());
        traverseInOrder(node.getRight());
    }
  }
    
  public Node getLeftMostNode()
  {
  	Node node = this.root;
  	
  	while (null != node.getLeft())
  		node = node.getLeft();
  	
  	return node;
  }
  
  public int getMeanValue()
  {
  	if (null == this.sortedValues)
  		this.sortedValues = getSortedValues();
  	return this.sortedValues.get(this.sortedValues.size()/2);
  }
  
  public static Node rebalanceTreeNodes(int[] nums, int start, int end) {
    if (start > end) {
        return null;
    }

    // Find the middle element of the array
    int mid = (end + start) / 2;

    // Create a new TreeNode with the middle element as the root
    Node root = new Node(nums[mid]);

    // Recursively build the left and right subtrees
    root.setLeft(rebalanceTreeNodes(nums, start, mid - 1));
    root.setRight(rebalanceTreeNodes(nums, mid + 1, end));

    return root;
  }  
  
  public static BinaryTree rebalanceTree(BinaryTree bt)
  {
  	List<Integer> list = bt.getSortedValues(); 
  	int[] nums = list.stream().mapToInt(i -> i).toArray();
  	Node root = rebalanceTreeNodes(nums, 0, nums.length-1);
  	BinaryTree ret = new BinaryTree();
  	
  	ret.setRoot(root);
  	
  	return ret;
  }
  

	public static void main(String[] args) throws JAXBException
  {
  	
  	BinaryTree bt = BinaryTree.fromXml("./indexOnSerialId.xml");
  	System.out.println(bt);
  	
  	BinaryTree balanced = BinaryTree.rebalanceTree(bt);
  	
  	BinaryTree.traverseInOrder(balanced.root);
  	
  	//balanced.saveXml("balanced.xml");
  	balanced.saveJson("balanced.json");
  	
  	
  }
  
}