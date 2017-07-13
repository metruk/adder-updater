import java.io.File;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlWorker {

	void createXml(Map<String,String> map,String fileName,String root,String objectName,String objectAttr,String objectField1,String objectField2){
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document doc = docBuilder.newDocument();

			Element rootElement = doc.createElement(root);
			doc.appendChild(rootElement);

			for (Map.Entry<String, String> entry : map.entrySet()){

				System.out.println(entry.getKey() + "/" + entry.getValue());
				// staff elements
				Element staff = doc.createElement(objectName);
				rootElement.appendChild(staff);

				objectAttr=entry.getKey();
				// set attribute to staff element
				Attr attr = doc.createAttribute("id");
				attr.setValue(objectAttr);
				staff.setAttributeNode(attr);

				String key=entry.getKey();
				String value=entry.getValue();

				xmlFieldAndValueAppender(doc, staff, objectField1, value);
				xmlFieldAndValueAppender(doc, staff, objectField2, key);

			}


			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(fileName));

			// Output to console for testing
			// StreamResult result = new StreamResult(System.out);

			transformer.transform(source, result);

			System.out.println("File saved!");

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		}
	}

	String readXml(String header,String fileName,String tagName,String findElementInTagName1,String findElementInTagName2){
		String id = null;
		try {
			File fXmlFile = new File(fileName);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			//optional, but recommended
			//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();

			//System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
			NodeList nList = doc.getElementsByTagName(tagName);



			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				//System.out.println("\nCurrent Element :" + nNode.getNodeName());
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					/*System.out.println("Staff id : " + eElement.getAttribute("id"));
					System.out.println("First Name : " + eElement.getElementsByTagName("postTitle").item(0).getTextContent());
					System.out.println("Last Name : " + eElement.getElementsByTagName("id").item(0).getTextContent());
					*/
					String xmlTitle=eElement.getElementsByTagName(findElementInTagName1).item(0).getTextContent();

					if(header.contains(xmlTitle)&&xmlTitle.length()>0){
						id=eElement.getElementsByTagName(findElementInTagName2).item(0).getTextContent();
					}

				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return id;
	}

	void xmlFieldAndValueAppender(Document doc,Element parent,String elmentName,String elementValue){
		Element lastname = doc.createElement(elmentName);
		lastname.appendChild(doc.createTextNode(elementValue));
		parent.appendChild(lastname);
	}

}