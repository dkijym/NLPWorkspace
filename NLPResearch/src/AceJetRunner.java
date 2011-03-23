import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Vector;

import javax.xml.bind.JAXBException;

import org.apache.derby.impl.io.vfmem.PathUtil;

import com.google.common.collect.Sets;
import com.google.common.io.Files;

import nlp.Mail.*;
import nlp.Mail.Thread;
import AceJet.Ace;
import AceJet.AceDocument;
import AceJet.AceEntity;
import AceJet.AceEntityMention;
import AceJet.AceEntityName;
import AceJet.EDTtype;
import AceJet.FindAceValues;
import AceJet.LearnRelations;
import Jet.Control;
import Jet.JetTest;
import Jet.Pat.Pat;
import Jet.Refres.Resolve;
import Jet.Time.TimeAnnotator;
import Jet.Time.TimeMain;
import Jet.Tipster.Annotation;

import Jet.Tipster.*;
public class AceJetRunner {

	/** The PRO p_ file. */
	private static String PROP_FILE = "C:\\Users\\reuben\\NLPResearch\\jetApoorv\\props\\MEace06.properties";


	/** The Constant IMPORT_DIRECTORYPATH. */
	public final static String IMPORT_DIRECTORYPATH = "C:\\Users\\reuben\\NLPResearch\\ThreadedData\\raw-updated-2009";
	static final String valueDictFile = "C:\\Users\\reuben\\NLPResearch\\jetApoorv\\acedata\\values.dict";
	public AceJetRunner()
	{
		try {
			initAceJet();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * Initializes ace jet from some config file.
	 * @throws IOException
	 */
	private void initAceJet() throws IOException {

		String propertyFile = PROP_FILE;

		// initialize Jet
		JetTest.initializeFromConfig(propertyFile);
		// set ACE mode for reference resolution
		Resolve.ACE = true;
		// Resolve.useMaxEnt = true;
		// load type dictionary
		EDTtype.readTypeDict();
		EDTtype.readGenericDict();
		 FindAceValues.readTypeDict(valueDictFile);
		// load time annotation patterns

	//	TimeMain.timeAnnotator = new TimeAnnotator("C:\\Users\\reuben\\NLPResearch\\jetApoorv\\data\\time_rules.yaml");
		// // <<<
		// load relational model
		String relationPatternFileName = JetTest
				.getConfigFile("Ace.RelationPatterns.fileName");
		/*
		 * if (relationPatternFileName != null) { eve = new
		 * RelationPatternSet(); eve.load(relationPatternFileName, 0); } else {
		 * System.out.println
		 * ("Ace:  no relation pattern file name specified in config file");
		 * System.out.println ("      Will not tag relations."); } // load event
		 * model String eventModelsDir =
		 * JetTest.getConfigFile("Ace.EventModels.directory"); if
		 * (eventModelsDir != null) { eventTagger = new EventTagger();
		 * EventTagger.useParser = useParser; EventTagger.usePA = (glarfDir !=
		 * null); EventTagger.triplesDir = glarfDir; String eventPatternFile =
		 * eventModelsDir + "eventPatterns.log";
		 * eventTagger.load(eventPatternFile); eventTagger.loadAllModels
		 * (eventModelsDir); } else { System.out.println
		 * ("Ace:  no event model file name specified in config file");
		 * System.out.println ("      Will not tag events."); }
		 */
		// turn off traces
		Pat.trace = false;
		Resolve.trace = false;
	}

	/**
	 * This runs acejet with the specfied configuration on the thread.
	 *
	 * @param th the thread to run AceJet on
	 * @param docNum the doc num
	 * @throws IOException if the write doesnt work
	 */
	public void runAceJetThread(Thread th, int docNum) throws IOException {
		System.out.println("Running ace jet on doc num "+ docNum);
		/* Get the parts of the thread together for NER */
		String threadNER = "";
		for (Message m : th.getMessage()) {
			threadNER += m.getSubject() + m.getContent();
		}
		// Jet.HMM.BigramHMMemitter.useBigrams = monocase; ??
		// Jet.HMM.HMMstate.otherPreference = monocase ? 1.0 : 0.0; ??
		Jet.Tipster.Document doc = new Jet.Tipster.Document("<TEXT>"
				+ threadNER + "</TEXT>");

		try {
			Control.processDocument(doc, null, false, docNum);

		} catch (IOException e) {
			System.out.println(e.getMessage());

			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Ace.tagReciprocalRelations(doc);

		Vector v = doc.annotationsOfType("ENAMEX");
		if (v != null) {
			for (Annotation a : (Vector<Annotation>) v) {
				System.out.println(doc.normalizedText(a.span()) + " is "
						+ a.toSGMLString());
			}
		} else {
			System.out.println("NULL");
		}
		String apfName = th.getID();
		System.out.println("Processing "+ apfName);
		PrintWriter apf = new PrintWriter(new BufferedWriter(new FileWriter("NER\\"+apfName+".apf")));
		PrintWriter apf2 = new PrintWriter(new BufferedWriter(new FileWriter("Out\\"+apfName)));
		apf2.write("<TEXT>"
				+ threadNER + "</TEXT>");
		apf2.close();
		AceDocument d = convertJetToAce(doc,apfName);
		d.write(apf);

		//AceDocument
		//d.write(System.out);
		apf.close();
	}

	public AceDocument convertJetToAce(Document doc, String apfFileName)
	{
		System.out.println("Converting document");
		AceDocument aceDoc = null;

		aceDoc = new AceDocument(apfFileName + ".sgm", "news", apfFileName, doc.text());

		// build entities
		Ace.buildAceEntities (doc, aceDoc);
		// build TIMEX2 expressioms
		//Ace.buildTimex (doc, aceDoc, apfFileName);
		// build values
		FindAceValues.buildAceValues (doc, apfFileName, aceDoc);
		// build relations
		//if (eve != null)
		//	LearnRelations.findRelations(apfFileName, doc, aceDoc);
		// build events
	//	if (eventTagger != null)
	//		eventTagger.tag(doc, aceDoc, currentDocPath, docId);
		// remove Crimes, Sentences, and Job-Titles not referenced by events
		FindAceValues.pruneAceValues (aceDoc);
		// write APF file
		return aceDoc;
	}
	/*
	 * System.out.println(doc.toString()); for(Object
	 * s:doc.printOutAnnotationTypes()) { System.out.println(s); Vector v =
	 * doc.annotationsOfType((String)s); for (int i=0; i<v.size(); i++) {
	 * Annotation a = (Annotation) v.get(i);
	 *
	 * System.out.println ((String)s +" "+ a.get("cat") + " over " +
	 * doc.text(a)); System.out.println( a.toSGMLString()); } } }
	 */

	/*
	 * Vector v = doc.annotationsOfType("ENAMEX"); for (int i=0; i<v.size();
	 * i++) { Annotation a = (Annotation) v.get(i); System.out.println
	 * ("Constit " + a.get("cat") + " over " + doc.text(a)); } }
	 */
	/*
	 * Vector v = doc.annotationsOfType("constit"); for (int i=0; i<v.size();
	 * i++) { Annotation a = (Annotation) v.get(i); System.out.println
	 * ("Constit " + a.get("cat") + " over " + doc.text(a)); } }
	 */
	// new View (doc, docCount);
	// open apf file

	// sourceType = "text";
	/*
	 * Vector doctypes = doc.annotationsOfType("DOCTYPE"); if (doctypes != null
	 * && doctypes.size() > 0) { Annotation doctype = (Annotation)
	 * doctypes.get(0); String source = (String) doctype.get("SOURCE"); if
	 * (source != null) sourceType = source; } String apfFileName = outputDir +
	 * currentDocPath + suffix; apf = new PrintWriter(new BufferedWriter(new
	 * FileWriter(apfFileName))); AceDocument aceDoc; if (timexDir != null &&
	 * !timexDir.equals("-")) { String textFileName = docDir + currentDocPath +
	 * ".sgm"; // patch because LCC produces a flat time directory // String
	 * timexFileName = timexDir + currentDocPath + ".apf.xml"; String
	 * timexFileName = timexDir + currentDoc + ".apf.xml"; aceDoc = new
	 * AceDocument (textFileName, timexFileName); } else {
	 */
	// aceDoc =
	// new AceDocument(currentDoc + ".sgm", sourceType, docId, doc.text());
	// }
	// build entities
	// buildAceEntities (doc, aceDoc);
	// build TIMEX2 expressioms
	// buildTimex (doc, aceDoc, docId);
	// build values
	// FindAceValues.buildAceValues (doc, docId, aceDoc);
	// build relations
	// if (eve != null)
	// LearnRelations.findRelations(docId, doc, aceDoc);
	// build events
	// if (eventTagger != null)
	// eventTagger.tag(doc, aceDoc, currentDocPath, docId);
	// remove Crimes, Sentences, and Job-Titles not referenced by events
	// FindAceValues.pruneAceValues (aceDoc);
	// write APF file
	// aceDoc.write(apf);
	// break;

	/**
	 * Adds the edge in emt.
	 */
	public void addEdgeInEMT() {

	}

	/**
	 * This runs through a certian number of threads and runs ace jet on them.
	 *
	 * @throws JAXBException the jAXB exception
	 * @throws IOException
	 */
	public void runAceJet() throws JAXBException, IOException {
		System.out.println("Starting ace jet.");
		for (Thread t : new ThreadCollection(IMPORT_DIRECTORYPATH, 35000)) {
			if(t != null)
				runAceJetThread(t, 0);
		}
	}

	public static void main(String args[])
	{

		try {
			getAllUniqueNERNames();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/*
		try {
		//	getAllUniqueNERNames();
			new AceJetRunner().runAceJet();
		} catch (JAXBException e) {
			System.out.println(e.getMessage());
			// TODO Auto-generated catch block
			//e.printStackTrace();
		} catch (IOException e) {
			System.out.println(e.getMessage());
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
	}

}
