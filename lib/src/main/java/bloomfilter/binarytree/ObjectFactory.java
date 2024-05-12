package bloomfilter.binarytree;

import javax.xml.namespace.QName;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class ObjectFactory {
    
    @XmlElementDecl(name = "node")
    public JAXBElement<Node> createNode(Node node) {
        return new JAXBElement<Node>(new QName("node"), Node.class, node);
    }

}