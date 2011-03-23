// -*- tab-width: 4 -*-
package AceJet;

import java.util.*;
import java.io.*;

import Jet.Tipster.*;
import Jet.Lisp.*;
import Jet.Refres.Resolve;

/**
 *  convert a set of ACE APF files to XML files containing the named entities
 *  specified by the APF file.  Each name is marked with "ENAMEX TYPE=...".
 *  This version has been extended for ACE 04 to handle PRE tags, which may or
 *  may not be names, by using a supplementary dictionary.
 */

class NewAPFtoXML {

	static final String ACEdir =
	    "C:/Documents and Settings/Ralph Grishman/My Documents/ACE/";
	static final String outputDir =
		ACEdir + "NE/";
	static final String fileList =
		// ACEdir + "training all.txt";
		// ACEdir + "feb02 all.txt";
		// ACEdir + "sep02 all.txt";
		// ACEdir + "aug03 all.txt";
		// ACEdir + "files-to-process.txt";
		// ACEdir + "training04 nwire.txt";
		ACEdir + "training04 bnews.txt";
		// ACEdir + "rdreval04 nwire.txt";
	static Gazetteer gazetteer;

	static TreeSet unknownPre = new TreeSet();
	static HashMap preDict = new HashMap();

	public static void main (String [] args) throws IOException  {
		gazetteer = new Gazetteer();
		gazetteer.load("data/loc.dict");
		loadPreDict ("acedata/pre.dict");
		// processFileList (ACEdir + "training04 nwire.txt");
		// processFileList (ACEdir + "training04 bnews.txt");
		processFileList (fileList);
		System.out.println ("\nUnclassified items:  " + unknownPre.size());
		Iterator it = unknownPre.iterator();
		while (it.hasNext()) {
			String word = (String) it.next();
			System.out.println (word);
		}
	}

	private static void loadPreDict (String dictFile) {
		preDict = new HashMap();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(dictFile));
			String line;
			while ((line = reader.readLine()) != null) {
				String preType = line.substring(0,1);
				String word = line.substring(2);
				preDict.put(word, preType);
			}
		} catch (IOException e) {
			System.err.print("Unable to load dictionary due to exception: ");
			System.err.println(e);
		}
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
			String textFileName = ACEdir + currentDoc + ".sgm";
			ExternalDocument doc = new ExternalDocument("sgml", textFileName);
			doc.setAllTags(true);
			doc.open();
			boolean monocase = Ace.allLowerCase(doc);
			gazetteer.setMonocase(monocase);
			boolean newData = fileList.indexOf("03") > 0 || AceDocument.ace2004;
			String APFfileName = ACEdir + currentDoc + (newData ? ".apf.xml" : ".sgm.tmx.rdc.xml");
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
			ArrayList mentions = entity.mentions;
			for (int j=0; j<mentions.size(); j++) {
				AceEntityMention  mention = (AceEntityMention) mentions.get(j);
				String htext = Resolve.normalizeName(mention.headText);
				String[] mentionName = Gazetteer.splitAtWS(htext);
				String preClass = (String) preDict.get(htext.toLowerCase());
				if (mention.type.equals("PRE")) {
					if (gazetteer.isNationality(mentionName) || gazetteer.isLocation(mentionName) ||
					    "N".equals(preClass)) {
						Span aceSpan = mention.head;
						Span jetSpan = new Span (aceSpan.start(), aceSpan.end()+1);
						doc.annotate ("ENAMEX", jetSpan, new FeatureSet("TYPE", entity.type));
					} else if (preClass != null) {
						// do nothing
					} else {
						System.out.println ("Unclassified PRE: " + mention.text + " {" + mention.headText + ")");
						unknownPre.add(htext.toLowerCase());
					}
				}
			}
		}
	}

	/*
	static void addENAMEXtags (Document doc, AceDocument aceDoc) {
		ArrayList entities = aceDoc.entities;
		for (int i=0; i<entities.size(); i++) {
			AceEntity entity = (AceEntity) entities.get(i);
			ArrayList mentions = entity.mentions;
			for (int j=0; j<mentions.size(); j++) {
				AceEntityMention  mention = (AceEntityMention) mentions.get(j);
				if (!mention.type.equals("PRO")) {
					Span aceSpan = mention.head;
					Span jetSpan = new Span (aceSpan.start(), aceSpan.end()+1);
					doc.annotate ("ENAMEX", jetSpan, new FeatureSet("TYPE", entity.type,
					                                                "MTYPE", mention.type));
				}
			}
		}
	}	*/
}
