// -*- tab-width: 4 -*-
package AceJet;

import java.util.*;
import java.io.*;

import org.w3c.dom.*;
import org.xml.sax.*;
import javax.xml.parsers.*;

/**
 *  convert a set of ACE APF files to XML files containing the named entities
 *  specified by the APF file.  Each name is marked with "ENAMEX TYPE=...".
 */

class APFtoXML {

	static String encoding = "ISO-8859-1";  // default:  ISO-LATIN-1
	static HashMap startTag;
	static HashSet endTag;
	static DocumentBuilder builder;
	static final String ACEdir =
	    "/home/xmbrst/workspace/jet/acedocdata/";
	static final String outputDir =
		ACEdir + "finaltestoutput01_13_05/system_rrm3/";
	static final String fileList =
		// ACEdir + "training all.txt";
		// ACEdir + "feb02 all.txt";
		// ACEdir + "sep02 all.txt";
		ACEdir + "finaltestoutput01_13_05/system_rrm3/system_rrm3_files.txt";
		// ACEdir + "files-to-process.txt";

	public static void main (String [] args) throws Exception  {
		// initialize APF reader
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		builder = factory.newDocumentBuilder();

		// open list of files
		BufferedReader reader = new BufferedReader (new FileReader(fileList));
		int docCount = 0;
		String currentDoc;
		while ((currentDoc = reader.readLine()) != null) {
			// process file 'currentDoc'
			docCount++;
			System.out.println ("\nProcessing document " + docCount + ": " + currentDoc);
			String textFileName = currentDoc + ".sgm";
			boolean newData = fileList.indexOf("03") > 0;
			String APFfileName = currentDoc + (newData ? ".sgm.apf.xml" : ".sgm.tmx.rdc.xml");
			String XMLfileName = currentDoc + ".ne.txt";
			convertDocument (textFileName, APFfileName, XMLfileName);
		}
	}

	private static void convertDocument (String textFileName, String APFfileName, String XMLfileName)
	    throws SAXException, IOException {
		Document apfDoc = builder.parse(APFfileName);
		findNames (apfDoc);
		StringBuffer fileText = readDocument(textFileName);
		computeOffsets (fileText);
		writeXML (fileText, XMLfileName);
	}

	/**
	 *  read the names in the APF file.  For each name, create an entry in
	 *  the startTag map and endTag set.
	 */

	static void findNames (Document apfDoc) {
		startTag = new HashMap();
		endTag = new HashSet();
		NodeList entities = apfDoc.getElementsByTagName("entity");
		for (int i=0; i<entities.getLength(); i++) {
			Element entity = (Element) entities.item(i);
			// System.out.println ("Found entity " + entityID);
			NodeList entityTypeList = entity.getElementsByTagName("entity_type");
			Element entityType = (Element) entityTypeList.item(0);
			String type = getElementText (entity, "entity_type");
			NodeList names = entity.getElementsByTagName("name");
			for (int j=0; j<names.getLength(); j++) {
				Element name = (Element) names.item(j);
				String start = getElementText (name, "start");
				String end = getElementText (name, "end");
				// System.out.println ("Name from " + start + " to " + end);
				startTag.put(new Integer(start), type);
				endTag.add(new Integer(end));
			}
		}
	}

	/*  assumes elementType is a leaf element type */

	private static String getElementText (Element e, String elementType) {
		NodeList typeList = e.getElementsByTagName(elementType);
		Element typeElement = (Element) typeList.item(0);
		String text = (String) typeElement.getFirstChild().getNodeValue();
		return text;
	}

	/**
	 *  read file 'fileName' and return its contents as a StringBuffer
	 */

	static StringBuffer readDocument (String fileName) throws IOException {
		File file = new File(fileName);
		String line;
		BufferedReader reader = new BufferedReader (
			// (new FileReader(file));
			new InputStreamReader (new FileInputStream(file), encoding));
		StringBuffer fileText = new StringBuffer();
		while((line = reader.readLine()) != null)
			fileText.append(line + "\n");
		return fileText;
	}

	// map from ACE offset to Jet offset
	static int[] ACEoffsetMap = null;

	/**
	 *  compute ACEoffsetMap, a map from ACE offsets (which exclude XML tags
	 *  to Jet offsets (which include all characters in the file)
	 */

	static void computeOffsets (StringBuffer fileText) {
		boolean inTag = false;
		int xmlCount = 0;
		int length = fileText.length();
		ACEoffsetMap = new int[length];
		for (int i=0; i<length; i++) {
			if(fileText.charAt(i) == '<') inTag = true;
			if (inTag) xmlCount++;
			ACEoffsetMap[i] = i - xmlCount;
			if(fileText.charAt(i) == '>') inTag = false;
		}
	}

	// map from APF type names to 'standard' names

	static HashMap standardType = new HashMap();
	static {standardType.put("GSP", "GPE");
	        standardType.put("PER", "PERSON");
	        standardType.put("ORG", "ORGANIZATION");
	        standardType.put("LOC", "LOCATION");
	        standardType.put("FAC", "FACILITY");
	     }

	/**
	 *  write 'fileText' out as file 'XMLfileName' with ENAMEX tags for the
	 *  names in the document
	 */

	static void writeXML (StringBuffer fileText, String XMLfileName)
	    throws IOException {
		OutputStreamWriter xml =
		    new OutputStreamWriter (new FileOutputStream (XMLfileName), encoding);
		for (int i=0; i<fileText.length(); i++) {
			int ACEoffset = ACEoffsetMap[i];
			Integer posn = new Integer(ACEoffset);
			if (startTag.containsKey(posn)) {
				String type = (String) startTag.get(posn);
				if (standardType.containsKey(type))
					type = (String) standardType.get(type);
				xml.write ("<ENAMEX TYPE=" + type + ">");
			}
			xml.write (fileText.charAt(i));
			if (endTag.contains(posn)) {
				xml.write ("</ENAMEX>");
			}
		}
		if (endTag.contains(new Integer (fileText.length())))
			xml.write ("</ENAMEX>");
		xml.close();
	}
}
