import java.io.File;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class StateStore {
	public static void writeState(Calc3DState state, String path) throws Exception {
		writeState(state, new File(path));
	}
	
	public static void writeState(Calc3DState state, File file) throws Exception {
		//TODO write state to XML file
	}
	
	public static Calc3DState readState(String path) throws Exception {
		return readState(new File(path));
	}
	
	public static Calc3DState readState(File file) throws Exception {
		if (!file.exists()) throw new Exception("File to read not found!!");
		Calc3DState readState = new Calc3DState();
		Document c3sdoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
		c3sdoc.getDocumentElement().normalize();
		
		Node eqRoot = c3sdoc.getElementsByTagName("equations").item(0);
		NodeList eqList = eqRoot.getChildNodes();
		int qReal = 0;
		for (int q = 0; q < eqList.getLength(); q++) {
			Node eqNode = eqList.item(q);
			if (eqNode.getNodeType() == Node.ELEMENT_NODE) {
				boolean used = false;
				String var = "x";
				String txt = "";
				
				NamedNodeMap attrs = eqNode.getAttributes();
				
				Node usedNode = attrs.getNamedItem("used");
				if (usedNode != null) {
					String usedTxt = usedNode.getNodeValue();
					if (usedTxt.equalsIgnoreCase("true") || usedTxt.equalsIgnoreCase("used")) used = true;
				}
				
				Node varNode = attrs.getNamedItem("var");
				if (varNode != null) {
					String varTxt = varNode.getNodeValue();
					if (varTxt.equalsIgnoreCase("y")) var = "y";
				}
				
				NodeList eqChildList = eqNode.getChildNodes();
				for (int w = 0; w < eqChildList.getLength(); w++) {
					Node eqChild = eqChildList.item(w);
					if (eqChild.getNodeType() == Node.TEXT_NODE) {
						txt = eqChild.getNodeValue();
					}
				}
				if (txt.isEmpty()) txt = null;
				
				readState.setValue(Calc3DState.EQ_KEYS[qReal][0], txt);
				readState.setValue(Calc3DState.EQ_KEYS[qReal][1], used + "");
				readState.setValue(Calc3DState.EQ_KEYS[qReal][2], var);
				
				qReal++;
			}
		}
		
		boolean rotUsed = true;
		String rotAxisVar = "x";
		String rotAxisVal = "";
		String rotShape = "disc";
		String rotShapeNum = "10";
		
		Node rotRoot = c3sdoc.getElementsByTagName("rotation").item(0);
		
		Node usedRotNode = rotRoot.getAttributes().getNamedItem("used");
		if (usedRotNode != null) {
			String usedTxt = usedRotNode.getNodeValue();
			if (usedTxt.equalsIgnoreCase("true") || usedTxt.equalsIgnoreCase("used")) rotUsed = true;
		}
		
		NodeList rotNodes = rotRoot.getChildNodes();
		for (int q = 0; q < rotNodes.getLength(); q++) {
			Node rotNode = rotNodes.item(q);
			if (rotNode.getNodeType() == Node.ELEMENT_NODE) {
				if (rotNode.getNodeName().equals("axis")) {
					Node rotAxisVarNode = rotNode.getAttributes().getNamedItem("var");
					if (rotAxisVarNode != null) {
						String rotAxisVarTxt = rotAxisVarNode.getNodeValue();
						if (rotAxisVarTxt.equalsIgnoreCase("y")) rotAxisVar = "y";
					}
					
					NodeList rotChildList = rotNode.getChildNodes();
					for (int w = 0; w < rotChildList.getLength(); w++) {
						Node rotChild = rotChildList.item(w);
						if (rotChild.getNodeType() == Node.TEXT_NODE) {
							rotAxisVal = rotChild.getNodeValue();
						}
					}
					if (rotAxisVal.isEmpty()) rotAxisVal = null;
				}
				else if (rotNode.getNodeName().equals("shapes")) {
					Node rotShapeTypeNode = rotNode.getAttributes().getNamedItem("var");
					if (rotShapeTypeNode != null) {
						String rotAxisVarTxt = rotShapeTypeNode.getNodeValue();
						if (rotAxisVarTxt.equalsIgnoreCase("shell")) rotAxisVar = "shell";
					}
					
					NodeList rotChildList = rotNode.getChildNodes();
					for (int w = 0; w < rotChildList.getLength(); w++) {
						Node rotChild = rotChildList.item(w);
						if (rotChild.getNodeType() == Node.TEXT_NODE) {
							rotShapeNum = rotChild.getNodeValue();
						}
					}
					if (rotShapeNum.isEmpty()) rotShapeNum = null;
				}
			}
		}
		
		readState.setValue(Calc3DState.ValidKey.AXISVAR, rotAxisVar);
		readState.setValue(Calc3DState.ValidKey.AXISTXT, rotAxisVal);
		readState.setValue(Calc3DState.ValidKey.ROTSHPTYPE, rotShape);
		readState.setValue(Calc3DState.ValidKey.ROTSHPNUM, rotShapeNum);
		
		boolean xsectUsed = false;
		String xsectShp = "0";
		String xsectShpNum = "10";
		String xsectHeight = "";
		String xsectPerp = "x";
		
		Node xsectRoot = c3sdoc.getElementsByTagName("crosssect").item(0);
		
		Node usedSectNode = rotRoot.getAttributes().getNamedItem("used");
		if (usedSectNode != null) {
			String usedTxt = usedSectNode.getNodeValue();
			if (usedTxt.equalsIgnoreCase("true") || usedTxt.equalsIgnoreCase("used")) xsectUsed = true;
		}
		
		NodeList sectNodes = xsectRoot.getChildNodes();
		for (int q = 0; q < sectNodes.getLength(); q++) {
			Node sectNode = sectNodes.item(q);
			if (sectNode.getNodeType() == Node.ELEMENT_NODE && sectNode.getNodeName().equals("shapes")) {
				NamedNodeMap shapeAttrs = sectNode.getAttributes();
				
				Node perpNode = shapeAttrs.getNamedItem("perpto");
				if (perpNode != null) {
					String perpTxt = perpNode.getNodeValue();
					if (perpTxt.equalsIgnoreCase("y")) xsectPerp = perpTxt;
				}
				
				Node heightNode = shapeAttrs.getNamedItem("height");
				if (heightNode != null) {
					xsectHeight = heightNode.getNodeValue();
				}
				
				Node typeNode = shapeAttrs.getNamedItem("type");
				if (typeNode != null) {
					xsectShp = typeNode.getNodeValue();
				}
				
				NodeList sectChildList = sectNode.getChildNodes();
				for (int w = 0; w < sectChildList.getLength(); w++) {
					Node sectChild = sectChildList.item(w);
					if (sectChild.getNodeType() == Node.TEXT_NODE) {
						xsectShpNum = sectChild.getNodeValue();
					}
				}
				if (xsectShpNum.isEmpty()) xsectShpNum = null;
			}
		}
		
		readState.setValue(Calc3DState.ValidKey.XSECTSHP, xsectShp);
		readState.setValue(Calc3DState.ValidKey.XSECTHGHT, xsectHeight);
		readState.setValue(Calc3DState.ValidKey.XSECTPERP, xsectPerp);
		readState.setValue(Calc3DState.ValidKey.XSECTSHPNUM, xsectShpNum);
		
		readState.setValue(Calc3DState.ValidKey.XSECTCHK, xsectUsed + "");
		readState.setValue(Calc3DState.ValidKey.ROTCHK, rotUsed + "");
		
		return readState;
	}
}

class Calc3DState {
	private HashMap<ValidKey, String> settings = new HashMap<ValidKey, String>();
	
	public Calc3DState() { }
	
	public void setValue(ValidKey key, String value) {
		settings.put(key, value);
	}
	
	public String getValue(ValidKey key) {
		if (settings.containsKey(key)) return settings.get(key);
		else return null;
	}
	
	public static enum ValidKey {
		EQ1TXT, EQ1CHK, EQ1VAR,
		EQ2TXT, EQ2CHK, EQ2VAR,
		EQ3TXT, EQ3CHK, EQ3VAR,
		EQ4TXT, EQ4CHK, EQ4VAR,
		ROTCHK, XSECTCHK,
		AXISTXT, AXISVAR,
		ROTSHPNUM, ROTSHPTYPE,
		XSECTSHP, XSECTSHPNUM, XSECTHGHT, XSECTPERP
	}
	
	public static final ValidKey[][] EQ_KEYS = new ValidKey[][] {
		{ValidKey.EQ1TXT, ValidKey.EQ1CHK, ValidKey.EQ1VAR},
		{ValidKey.EQ2TXT, ValidKey.EQ2CHK, ValidKey.EQ2VAR},
		{ValidKey.EQ3TXT, ValidKey.EQ3CHK, ValidKey.EQ3VAR},
		{ValidKey.EQ4TXT, ValidKey.EQ4CHK, ValidKey.EQ4VAR},
	}; 
}