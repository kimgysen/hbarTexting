package bloomfilter.binarytree;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.persistence.jaxb.JAXBContextProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
 
@XmlRootElement
public class Node {
    private int value;
    private Node left;
    private Node right;

    public Node() {
    }

    public Node(int data) {
        this.value = data;
        this.left = null;
        this.right = null;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int data) {
        this.value = data;
    }

    @XmlElement
    public Node getLeft() {
        return left;
    }

    public void setLeft(Node left) {
        this.left = left;
    }

    @XmlElement
    public Node getRight() {
        return right;
    }

    public void setRight(Node right) {
        this.right = right;
    }

    
    public void saveJson(String fileName)
    {
      try {  
      	 	Map<String, Object> properties = new HashMap<String, Object>(2);
      	 	properties.put(JAXBContextProperties.MEDIA_TYPE, "application/json");
      	 	properties.put(JAXBContextProperties.JSON_INCLUDE_ROOT, false);
      	 	jakarta.xml.bind.JAXBContext context = org.eclipse.persistence.jaxb.JAXBContextFactory.createContext(new Class[] {Node.class, ObjectFactory.class}, properties);
          Marshaller marshaller = context.createMarshaller();
          marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

          java.io.StringWriter sw = new java.io.StringWriter();
          marshaller.marshal(this, sw);
          String jsonString = sw.toString();    
          System.out.println("Binary tree marshalled to XML:");
          System.out.println(jsonString);

          marshaller.marshal(this, new File(fileName));


      } catch (JAXBException e) {
          e.printStackTrace();
      }    	
    }
    
    public void saveXml(String fileName)
    {
      try {  
          JAXBContext context = org.eclipse.persistence.jaxb.JAXBContextFactory.createContext(new Class[] {Node.class}, null);
          Marshaller marshaller = context.createMarshaller();
          marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

          java.io.StringWriter sw = new java.io.StringWriter();
          marshaller.marshal(this, sw);
          String jsonString = sw.toString();    
          System.out.println("Binary tree marshalled to XML:");
          System.out.println(jsonString);

          marshaller.marshal(this, new File(fileName));


      } catch (JAXBException e) {
          e.printStackTrace();
      }    	
    }
    
    public static Node fromXml(String fileName) throws JAXBException {
      File file = new File(fileName);
      JAXBContext context = org.eclipse.persistence.jaxb.JAXBContextFactory.createContext(new Class[] {Node.class}, null);
      Unmarshaller jaxbUnmarshaller = context.createUnmarshaller();
      return (Node) jaxbUnmarshaller.unmarshal(file);
    }

    public static Node fromJson(String fileName) throws JAXBException {
      ObjectMapper mapper = new ObjectMapper();
      try {
          return mapper.readValue(new File(fileName), Node.class);
      } catch (IOException e) {
          e.printStackTrace();
          return null;
      }
    }

     
    public static void main(String[] args) throws JAXBException {

    	Node n = fromXml("BinaryTree.xml");
    	System.out.println(n);
    	
    			 n = fromJson("BinaryTree.json");
    	System.out.println(n);
    	
    }
}
