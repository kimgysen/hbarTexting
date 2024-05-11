package unitTest;



import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;

import bloomfilter.binarytree.BinaryTree;
import jakarta.xml.bind.JAXBException;


public class binarytreeTest {

	@Test
	public void givenABinaryTree_WhenAddingElements_ThenTreeContainsThoseElements() throws JAXBException {
	    BinaryTree bt = BinaryTree.load("./BinaryTree.xml");

	    assertTrue(bt.containsNode(6));
	    assertTrue(bt.containsNode(4));
	    assertFalse(bt.containsNode(1));
	}
	
	
	@Test
	public void givenABinaryTree_WhenDeletingElements_ThenTreeDoesNotContainThoseElements() throws JAXBException {
		BinaryTree bt = BinaryTree.load("./BinaryTree.xml");

	    assertTrue(bt.containsNode(9));
	    bt.delete(9);
	    assertFalse(bt.containsNode(9));
	}

}
