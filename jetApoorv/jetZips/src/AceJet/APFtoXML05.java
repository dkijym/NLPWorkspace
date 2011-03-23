// -*- tab-width: 4 -*-
package AceJet;

import java.util.*;
import java.io.*;

import Jet.Tipster.*;
import Jet.Lisp.*;
import Jet.Refres.Resolve;

/**
 *  convert a set of ACE 2005 APF files to XML files containing the named
 *  entities, values, and time expressions specified by the APF file.
 *  Each name is marked with "ENAMEX TYPE=...";  each value by "VALUE TYPE=
 *  ...", and each time expression by "TIMEX".
 */

class APFtoXML05 {

	static final String home =
	  "C:/Documents and Settings/Ralph Grishman/My Documents/";
	static final String ACEdir =
	  home + "ACE 05/V4/";
	static final String outputDir =
		ACEdir + "NE/";
	static final String fileList =
		ACEdir + "allSgm.txt";

	public static void main (String [] args) throws IOException  {
		processFileList (fileList);
	}

	private static void processFileList (String fileList) throws IOException {
		// open list of files
		BufferedReader reader = new BufferedReader (new FileReader(fileList));
		int docCount = 0;
		String currentDoc;
		while ((currentDoc = reader.readLine()) != null) {
			// process file 'currentDoc'
			docCount++;
			System.out.println ("\nProcessing document " + docCount + ": " + currentDoc);
			String textFileName = ACEdir + currentDoc;
			ExternalDocument doc = new ExternalDocument("sgml", textFileName);
			doc.setAllTags(true);
			doc.open();
			doc.saveAs(outputDir, currentDoc);
			String APFfileName = textFileName.replaceFirst(".sgm", ".apf.xml");
			// String APFfileName = ACEdir + currentDoc + ".entities.apf.xml";
			AceDocument aceDoc = new AceDocument(textFileName, APFfileName);
			addENAMEXtags (doc, aceDoc);
			doc.saveAs(outputDir, currentDoc + ".ne.txt");
		}
	}

	/**
	 *  write 'fileText' out as file 'XMLfileName' with ENAMEX tags for the
	 *  names in the document
	 */

	static void addENAMEXtags (Document doc, AceDocument aceDoc) {
		ArrayList entities = aceDoc.entities;
		for (int i=0; i<entities.size(); i++) {
			AceEntity entity = (AceEntity) entities.get(i);
			ArrayList names = entity.names;
			for (int j=0; j<names.size(); j++) {
				AceEntityName name = (AceEntityName) names.get(j);
				Span aceSpan = name.extent;
				Span jetSpan = new Span (aceSpan.start(), aceSpan.end()+1);
				doc.annotate ("ENAMEX", jetSpan, new FeatureSet("TYPE", entity.type));
			}
		}
		ArrayList values = aceDoc.values;
		for (int i=0; i<values.size(); i++) {
			AceValue value = (AceValue) values.get(i);
			ArrayList mentions = value.mentions;
			for (int j=0; j<mentions.size(); j++) {
				AceValueMention mention = (AceValueMention) mentions.get(j);
				Span aceSpan = mention.extent;
				Span jetSpan = new Span (aceSpan.start(), aceSpan.end()+1);
				doc.annotate ("VALUE", jetSpan, new FeatureSet("TYPE", value.type));
			}
		}
		ArrayList timexes = aceDoc.timeExpressions;
		for (int i=0; i<timexes.size(); i++) {
			AceTimex timex = (AceTimex) timexes.get(i);
			ArrayList mentions = timex.mentions;
			for (int j=0; j<mentions.size(); j++) {
				AceTimexMention mention = (AceTimexMention) mentions.get(j);
				Span aceSpan = mention.extent;
				Span jetSpan = new Span (aceSpan.start(), aceSpan.end()+1);
				doc.annotate ("TIMEX", jetSpan, new FeatureSet());
			}
		}
	}

}
