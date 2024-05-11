package bloomfilter.binarytree;

import jakarta.xml.bind.JAXBException;

public class BinaryTree {

  Node root;

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
  
  public static BinaryTree load(String fileName) throws JAXBException
  {
  	BinaryTree bt = new BinaryTree();
  	bt.root = Node.load(fileName);
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
  
  public static void main(String[] args) throws JAXBException
  {
  	
  	BinaryTree bt = BinaryTree.load("./BinaryTree.xml");
  	System.out.println(bt);
  	
  	traverseInOrder(bt.root);
  	
  	bt.saveJson("./BinaryTree.json");
  	bt.saveXml("./BT.xml");

  }
  
}