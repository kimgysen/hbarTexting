package hedera.topics;

import javax.xml.namespace.QName;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class ObjectFactory {
    
    @XmlElementDecl(name = "message")
    public JAXBElement<Message> createMessage(Message message) {
        return new JAXBElement<Message>(new QName("message"), Message.class, message);
    }

}