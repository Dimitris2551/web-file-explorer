
import java.io.File;
import java.io.IOException;
import java.security.KeyStore.Entry.Attribute;
import java.util.logging.Level;
import java.util.logging.Logger;
//import javax.swing.text.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Parser {
    int listenPort;
    int statisticsPort;
    String accessLog;
    String errorLog;
    String rootDir;
    
    public void parse() {
        
        try {
            File inputFile = new File("server.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            System.out.println("Root element :"+doc.getDocumentElement().getNodeName());
            Element root = doc.getDocumentElement();
            NodeList childsListen= root.getElementsByTagName("listen");
            Node nextNode = childsListen.item(0);
            Element listen = (Element)nextNode;
            
            String str = listen.getAttribute("port");
            listenPort = Integer.parseInt(str);
            System.out.println("We are listening in port: "+listenPort);
            
            childsListen= root.getElementsByTagName("statistics");
            nextNode = childsListen.item(0);
            listen = (Element)nextNode;
            
            str = listen.getAttribute("port");
            statisticsPort = Integer.parseInt(str);
            System.out.println("We are sending the statistics in port: "+statisticsPort);
            
            childsListen= root.getElementsByTagName("log");
            nextNode = childsListen.item(0);
            listen = (Element)nextNode;
            childsListen = listen.getElementsByTagName("access");
            nextNode = childsListen.item(0);
            listen = (Element)nextNode;
            accessLog = listen.getAttribute("filepath");
            System.out.println("access filepath: "+accessLog);
            
            childsListen= root.getElementsByTagName("log");
            nextNode = childsListen.item(0);
            listen = (Element)nextNode;
            childsListen = listen.getElementsByTagName("error");
            nextNode = childsListen.item(0);
            listen = (Element)nextNode;
            errorLog = listen.getAttribute("filepath");
            System.out.println("error filepath: "+errorLog);
            
            childsListen= root.getElementsByTagName("documentroot");
            nextNode = childsListen.item(0);
            listen = (Element)nextNode;
            rootDir = listen.getAttribute("filepath");
            System.out.println("documentroot : "+rootDir);
        } catch (ParserConfigurationException | SAXException ex) {
            Logger.getLogger(Parser.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IOException e) {
            System.out.println("Settings could not be retrived so the server did not start");
        }
    }
    
}
