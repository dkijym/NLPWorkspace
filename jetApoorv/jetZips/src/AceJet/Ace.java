// -*- tab-width: 4 -*-
package AceJet;

//Author:       Ralph Grishman
//Date:         July 10, 2003

import java.util.*;
import java.io.*;
import Jet.*;
import Jet.Control;
import Jet.Refres.Resolve;
import Jet.Lex.Tokenizer;
import Jet.Pat.Pat;
import Jet.Lisp.*;
import Jet.Tipster.*;
import Jet.Parser.SynFun;
import Jet.Parser.ParseTreeNode;
import Jet.Time.TimeMain;
import Jet.Time.TimeAnnotator;

/**
 *  procedures for generating ACE output for a Jet document.
 */

public class Ace {

	public static boolean useParser = false;
	static final boolean useParseCollection = false;
	public static boolean perfectMentions = false;
	public static boolean perfectEntities = false;
	static final boolean asr = false;

	public static boolean preferRelations = false;
	public static boolean preferEntities = !preferRelations;

	public static boolean entityTrace = false;

	public static boolean monocase = false;

	static PrintWriter apf;
	public static ExternalDocument doc;
	public static Gazetteer gazetteer;
	static int aceEntityNo;
	static HashMap aceTypeDict;
	static final String suffix = ".sgm.apf";
	// for formal evaluation
	// static final String suffix = ".apf.xml";
	/**
	 *  the path (directory + file name) of the document currently being processed
	 */
	static String currentDocPath;
	/**
	 *  the file name of the document currently being processed
	 */
	static String currentDoc;
	/**
	 *  the DOCID field of the document currently being processed.  The file name
	 *  is used if no DOCID field is present.
	 */
	static String docId;
	static String sourceType = "text";
	// relational model
	public static RelationPatternSet eve = null;
	static EventTagger eventTagger = null;

	static final String valueDictFile = "acedata/values.dict";

	/**
	 *  generate ACE annotation files (in APF) format for a list of documents.
	 *  Takes four to seven command line parameters:                             <BR>
	 *  propertyFile:  Jet properties file                                     <BR>
	 *  filelist:  a list of the files to be processed                         <BR>
	 *  docDirectory:  the path of the directory containing the input files    <BR>
	 *  outDirectory:  the path of the directory in which APF files are to
	 *                 be written                                              <BR>
	 *  timexDir:      the path of the directory containing TIMEX info
	 *                 [optional]                                              <BR>
	 *  glarfDir:      the path of the directory containing GLARF tuples
	 *                 [optional]                                              <BR>
	 *  glarfSuffix:   the file suffix for GLARF files                         <BR>
	 *  For each <I>file</I> in <I>filelist</I>, the document is read from
	 *  <I>docDirectory</file</I>.sgm and the APF file is written to
	 *  <I>outDirectory</file</I>.sgm.apf
	 */

	public static void main (String[] args) throws IOException {
		// get arguments
		System.out.println("Starting ACE Jet...");
		if (args.length < 4 || args.length > 7) {
			System.out.println ("Ace must take 4 to 6 arguments:");
			System.out.println ("    properties filelist documentDir outputDir [timexDir]");
			System.exit(1);
		}
		String propertyFile = args[0];
		String fileList = args[1];
		String docDir = args[2];
		String outputDir = args[3];
		String timexDir = null;
		if (args.length >= 5)	timexDir = args[4];
		String glarfDir = null;
		if (args.length >= 6) glarfDir = args[5];
		if (args.length == 7) EventTagger.triplesSuffix = args[6];

		// initialize Jet
		JetTest.initializeFromConfig (propertyFile);
		// set ACE mode for reference resolution
		Resolve.ACE = true;
		// Resolve.useMaxEnt = true;
		// load type dictionary
		EDTtype.readTypeDict();
		EDTtype.readGenericDict ();
		FindAceValues.readTypeDict(valueDictFile);
		// load time annotation patterns
		TimeMain.timeAnnotator = new TimeAnnotator("data/time_rules.yaml"); // <<<
		// load relational model
		String relationPatternFileName = JetTest.getConfigFile("Ace.RelationPatterns.fileName");
		if (relationPatternFileName != null) {
			eve = new RelationPatternSet();
			eve.load(relationPatternFileName, 0);
		} else {
			System.out.println ("Ace:  no relation pattern file name specified in config file");
			System.out.println ("      Will not tag relations.");
		}
		// load event model
		String eventModelsDir = JetTest.getConfigFile("Ace.EventModels.directory");
		if (eventModelsDir != null) {
			eventTagger = new EventTagger();
			EventTagger.useParser = useParser;
			EventTagger.usePA = (glarfDir != null);
			EventTagger.triplesDir = glarfDir;
			String eventPatternFile = eventModelsDir + "eventPatterns.log";
			eventTagger.load(eventPatternFile);
			eventTagger.loadAllModels (eventModelsDir);
		} else {
			System.out.println ("Ace:  no event model file name specified in config file");
			System.out.println ("      Will not tag events.");
		}
		// turn off traces
		Pat.trace = false;
		Resolve.trace = false;
		// open list of files
		BufferedReader reader = new BufferedReader (new FileReader(fileList));
		int docCount = 0;
		while ((currentDocPath = reader.readLine()) != null) {
			// process file 'currentDoc'
			docCount++;
			if (currentDocPath.endsWith(".sgm"))
				currentDocPath = currentDocPath.substring(0, currentDocPath.length() - 4);
			System.out.println ("\nProcessing document " + docCount + ": " + currentDocPath);
			// read document
			if (perfectMentions) {
				doc = new ExternalDocument("sgml", docDir + "perfect parses/" + currentDocPath + ".sgm");
				String textFile = docDir + currentDocPath + ".sgm";
				// String keyFile = docDir + currentDocPath + ".apf.xml";
				// String keyFile = docDir + currentDocPath + ".mentions.apf.xml"; //<< for corefeval
				String keyFile = docDir + currentDocPath + ".entities.apf.xml"; //<< for rdreval
				AceDocument keyDoc = new AceDocument(textFile, keyFile);
				PerfectAce.buildEntityMentionMap (doc, keyDoc);
			} else {
				doc = new ExternalDocument("sgml", docDir + currentDocPath + ".sgm");
			}
			doc.setAllTags(true);
			// doc.setEmptyTags(new String[] {"W", "TURN"});
			doc.open();
			doc.stretchAll();
			// process document
			monocase = allLowerCase(doc);
			System.out.println (">>> Monocase is " + monocase);
			gazetteer.setMonocase(monocase);
			Jet.HMM.BigramHMMemitter.useBigrams = monocase;
			Jet.HMM.HMMstate.otherPreference = monocase ? 1.0 : 0.0;
			Control.processDocument (doc, null, docCount == -1, docCount);
			tagReciprocalRelations(doc);
			// new View (doc, docCount);
			// open apf file
			currentDoc = currentDocPath;
			if (currentDocPath.indexOf('/') >= 0)
				currentDoc = currentDocPath.substring(currentDocPath.lastIndexOf('/')+1);
			docId = getDocId(doc);
			if (docId == null)
				docId = currentDoc;
			sourceType = "text";
			Vector doctypes = doc.annotationsOfType("DOCTYPE");
			if (doctypes != null && doctypes.size() > 0) {
				Annotation doctype = (Annotation) doctypes.get(0);
				String source = (String) doctype.get("SOURCE");
				if (source != null)
					sourceType = source;
			}
			String apfFileName = outputDir + currentDocPath + suffix;
			apf = new PrintWriter(new BufferedWriter(new FileWriter(apfFileName)));
			AceDocument aceDoc;
			if (timexDir != null && !timexDir.equals("-")) {
				String textFileName = docDir + currentDocPath + ".sgm";
				// patch because LCC produces a flat time directory
				// String timexFileName = timexDir + currentDocPath + ".apf.xml";
				String timexFileName = timexDir + currentDoc + ".apf.xml";
				aceDoc = new AceDocument (textFileName, timexFileName);
			} else {
				aceDoc =
				  new AceDocument(currentDoc + ".sgm", sourceType, docId, doc.text());
			}
			// build entities
			buildAceEntities (doc, aceDoc);
			// build TIMEX2 expressioms
			buildTimex (doc, aceDoc, docId);
			// build values
			FindAceValues.buildAceValues (doc, docId, aceDoc);
			// build relations
			if (eve != null)
				LearnRelations.findRelations(docId, doc, aceDoc);
			// build events
			if (eventTagger != null)
				eventTagger.tag(doc, aceDoc, currentDocPath, docId);
			// remove Crimes, Sentences, and Job-Titles not referenced by events
			FindAceValues.pruneAceValues (aceDoc);
			// write APF file
			aceDoc.write(apf);
			// break;
		}
	}

	/**
	 *  returns the document ID of Document <CODE>doc</CODE>, if found,
	 *  else returns <CODE>null</CODE>.  It looks for <br>
	 *  the text of a <B>DOCID</B> annotation (as provided with ACE documents); <br>
	 *  the text of a <B>DOCNO</B> annotation (as provided with TDT4 and TDT5 documents); <br>
	 *  the <B>DOCID</B> feature of a <B>document</B> annotation
	 *       (for GALE ASR transcripts); or <br>
	 *  the <B>id</B> feature of a <B>doc</B> annotation (for newer LDC documents).
	 */

	public static String getDocId (Document doc) {
		Vector docIdAnns = doc.annotationsOfType ("DOCID");
		if (docIdAnns != null && docIdAnns.size() > 0) {
			Annotation docIdAnn = (Annotation) docIdAnns.get(0);
			return doc.text(docIdAnn).trim();
		}
		docIdAnns = doc.annotationsOfType ("DOCNO");
		if (docIdAnns != null && docIdAnns.size() > 0) {
			Annotation docIdAnn = (Annotation) docIdAnns.get(0);
			return doc.text(docIdAnn).trim();
		}
		Vector docAnns = doc.annotationsOfType ("document");
		if (docAnns != null && docAnns.size() > 0) {
			Annotation docAnn = (Annotation) docAnns.get(0);
			Object docId = docAnn.get("DOCID");
			if (docId != null && docId instanceof String) {
				return (String) docId;
			}
		}
		docAnns = doc.annotationsOfType ("DOC");
		if (docAnns != null && docAnns.size() > 0) {
			Annotation docAnn = (Annotation) docAnns.get(0);
			Object docId = docAnn.get("id");
			if (docId != null && docId instanceof String) {
				return (String) docId;
			}
		}
		return null;
	}

	public static void setPatternSet (String fileName) throws IOException {
		eve = new RelationPatternSet();
		eve.load(fileName, 0);
	}

	public static boolean allLowerCase (Document doc) {
		Vector textSegments = doc.annotationsOfType ("TEXT");
		if (textSegments == null || textSegments.size() == 0)
			return false;
		Annotation text = (Annotation) textSegments.get(0);
		Span span = text.span();
		return allLowerCase (doc, span);
	}

	public static boolean allLowerCase (Document doc, Span span) {
		boolean allLower = true;
		boolean allUpper = true;
		for (int i=span.start(); i<span.end(); i++) {
			if (Character.isUpperCase(doc.charAt(i)))
				allLower = false;
			if (Character.isLowerCase(doc.charAt(i)))
				allUpper = false;
			}
		return allLower || allUpper;
	}

	public static boolean titleCase (Document doc, Span span) {
		boolean allTitle = true;
		for (int i=span.start(); i<span.end()-1; i++) {
			if (Character.isWhitespace(doc.charAt(i)) &&
			    Character.isLowerCase(doc.charAt(i+1)))
				allTitle = false;
			}
		return allTitle;
	}

	/**
	 *  create ACE entities from entity annotations produced by refres.
	 */

	public static void buildAceEntities (Document doc, AceDocument aceDoc) {
		aceEntityNo = 0;
		LearnRelations.resetMentions(); // for relations
		String docText = doc.text();
		Vector entities = doc.annotationsOfType("entity");
		if (entities != null) {
			for (int ientity=0; ientity<entities.size(); ientity++) {
				AceEntity aceEntity =
					buildEntity(((Annotation) entities.get(ientity)), ientity, doc, docText);
				if (aceEntity != null)
					aceDoc.addEntity(aceEntity);
			}
		}
		System.err.println ("buildAceEntities: generated " + aceDoc.entities.size() + " entities");
	}

	/**
	 *  create an AceEntity from <CODE>entity</CODE>.  If the
	 *  entity is not a valid EDT type, nothing is written.
	 */

	private static AceEntity buildEntity (Annotation entity, int ientity,
			Document doc, String docText) {
		Vector mentions = (Vector) entity.get("mentions");
		Annotation firstMention = (Annotation) mentions.get(0);
		String aceTypeSubtype;
		aceTypeSubtype = EDTtype.getTypeSubtype (doc, entity, firstMention);
		String aceType = EDTtype.bareType(aceTypeSubtype);
		if (entityTrace)
			System.out.println ("Type of " + Resolve.normalizeName(doc.text(firstMention)) + " is " + aceTypeSubtype);
		if (aceType.equals("OTHER")) return null;
		String aceSubtype = EDTtype.subtype(aceTypeSubtype);
		// don't tag items generic for Ace 2004 or later
		boolean generic = !AceDocument.ace2004 && isGeneric(doc, firstMention);

		if (generic) {
			System.out.println ("Identified generic mention " +
			                    Resolve.normalizeName(doc.text(firstMention)));
		}

		aceEntityNo++;
		if (entityTrace)
			System.out.println("Generating ace entity " + aceEntityNo +
			                   " (internal entity " + ientity + ") = " +
			                   Resolve.normalizeName(doc.text(firstMention)) +
			                   " [" + aceType + "]");
		String entityID = docId + "-" + aceEntityNo;
		AceEntity aceEntity = new AceEntity (entityID, aceType, aceSubtype, generic);
		for (int imention=0; imention<mentions.size(); imention++) {
			Annotation mention = (Annotation) mentions.get(imention);
			Annotation head = Resolve.getHeadC(mention);
			String mentionID = entityID + "-" + imention;
			AceEntityMention aceMention = buildMention (mention, head, mentionID, aceType, doc, docText);
			aceEntity.addMention(aceMention);
			LearnRelations.addMention (aceMention);
			boolean isNameMention = aceMention.type == "NAME";
			if (isNameMention) {
				aceEntity.addName(new AceEntityName(head.span(), docText));
			}
		}
		return aceEntity;
	}

	static final String[] locativePrepositions = {"in", "at", "to", "near"};

	/**
	 *  write the information for <CODE>mention</CODE> with head <CODE>head</CODE>
	 *  in APF format.
	 */

	private static AceEntityMention buildMention
			(Annotation mention, Annotation head, String mentionID, String entityType,
				Document doc, String docText) {
		Span mentionSpan = mention.span();
		Span headSpan = head.span();
		String mentionType = mentionType(head, mention);
		AceEntityMention m =
			new AceEntityMention (mentionID, mentionType, mentionSpan, headSpan, docText);
		if (entityType.equals("GPE")) {
			if (perfectMentions)
				m.role = PerfectAce.getMentionRole(head);
			else {
				String prep = governingPreposition(doc, mention);
				if ((prep != null && in(prep, locativePrepositions)) ||
				     // for location in dateline
				     Resolve.sentenceSet.sentenceNumber(mention.start()) == 0) {
					m.role = "LOC";
				} else {
					m.role = "GPE";
				}
			}
		}
		return m;
	}

	/**
	 *  determine the mention type of a mention (NOMINAL, PRONOUN, or NAME)
	 *  from its <CODE>head</CODE>.
	 */

	private static String mentionType (Annotation head, Annotation mention) {
		if (perfectMentions)
			return PerfectAce.getMentionType (head);
		String cat = (String) head.get("cat");
		String mcat = (String) mention.get("cat");
		// if (mention.get("preName-1") != null || mention.get("nameMod-1") != null ||
		//     cat == "adj")
		// 	return "PRE";
		// else
		if (cat == "n" || cat == "title" || cat == "tv" || cat == "v")
			return "NOMINAL";
		else if (cat == "pro" || cat == "det" /*for possessives - his, its */ ||
			       cat == "adj" || cat == "ven" || cat == "q" ||
		         cat == "np" /* for headless np's */ || cat == "wp" || cat == "wp$")
			return "PRONOUN";
		else // cat == "name"
			if (mention.get("nameWithModifier") != null)
				return "NOMINAL";
			else
				return "NAME";
	}

	static final String[] genericFriendlyDeterminers =
		{"no", "neither", "any", "many", "every", "each"};
	static final String[] clearGenericPronouns =
		{"everyone", "anyone", "everybody", "anybody",
		"something", "who", "whoever", "whomever",
		"wherever", "whatever", "where"};

	private static boolean isGeneric (Document doc, Annotation mention) {
		Annotation ngHead = getNgHead (mention);
		Annotation headC = Resolve.getHeadC (mention);
		if (headC.get("cat") == "n") {
			// is always generic
			String det = SynFun.getDet(mention);
			if (det != null && in(det, genericFriendlyDeterminers))
				return true;
			// OR is generic head
			if (!EDTtype.hasGenericHead(doc, mention)) return false;
			// if (pa.get("number") != "plural") return false;
			if (ngHead.get("poss") != null || det == "poss") return false;
			if (ngHead.get("quant") != null || det == "q") return false;
			//    AND is in generic environment
			Annotation vg = governingVerbGroup(mention);
			if (vg != null) {
				FeatureSet vpa = (FeatureSet) vg.get("pa");
				if (vpa != null && vpa.get("tense") != "past"
				                && vpa.get("aspect") == null) {
				    System.out.println ("Governing verb group = " + doc.text(vg));
				    System.out.println ("Verb group pa = " + vpa);
					return true;
				}
			}
			return false;
		} else if (headC.get("cat") == "pro" || headC.get("cat") == "np"
		                                     || headC.get("cat") == "det") {
			String pronoun = SynFun.getHead(doc, mention);
			return in(pronoun,clearGenericPronouns) ||
			       in(pronoun,genericFriendlyDeterminers);  // << added Oct. 10
		} else /* head is a name */ return false;
	}

	private static Annotation getNgHead (Annotation ng) {
		Annotation hd = ng;
		while (true) {
			ng = (Annotation) hd.get("headC");
			if (ng == null) return hd;
			if (ng.get("cat") != "np"  || ng.get("possPrefix") == "true") return hd;
			hd = ng;
		}
	}

	/**
	 *  tag the document <CODE>doc</CODE> for time expressions, and create
	 *  (and add to <CODE>aceDoc</CODE> an AceTimex object for each time
	 *  expression.
	 */

	private static void buildTimex (Document doc, AceDocument aceDoc, String docId) {
		TimeMain.processDocument (doc);
		Vector v = doc.annotationsOfType("TIMEX2");
		if (v != null) {
			System.out.println ( v.size() + " time expressions found.");
			for (int i=0; i<v.size(); i++) {
				Annotation ann = (Annotation) v.get(i);
				String docText = doc.text();
				String timeId = docId + "-T" + i;
				String val = (String) ann.get("VAL");
				if (val == null)
					System.err.println ("TIMEX " + timeId + " has no VAL.");
				AceTimexMention mention =
					new AceTimexMention (timeId + "-1", ann.span(), docText);
				AceTimex timex = new AceTimex (timeId, val);
				timex.addMention(mention);
				aceDoc.addTimeExpression(timex);
			}
		}
	}

	private static boolean in (Object o, Object[] array) {
		for (int i=0; i<array.length; i++)
			// if (array[i] == o) return true;
			if (array[i] != null && array[i].equals(o)) return true;
		return false;
	}

	/**
	 *  assigns reciprocal relations subject-1 and object-1
	 */

	public static void tagReciprocalRelations (Document doc) {
		Vector constits = doc.annotationsOfType("constit");
		if (constits != null) {
			for (int j = 0; j < constits.size();  j++) {
				Annotation ann = (Annotation) constits.elementAt(j);
				if (ann.get("subject") != null) {
					Annotation subject = (Annotation) ann.get("subject");
					if (subject.get("subject-1") == null) {
						subject.put("subject-1", ann);
					}
				}
				if (ann.get("object") != null) {
					Annotation object = (Annotation) ann.get("object");
					if (object.get("object-1") == null) {
						object.put("object-1", ann);
					}
				}
			}
		}
	}

	static Annotation governingVerbGroup (Annotation ann) {
		Annotation governingConstituent;
		if (ann.get("subject-1") != null) {
			governingConstituent = (Annotation) ann.get("subject-1");
		} else if (ann.get("object-1") != null) {
			governingConstituent = (Annotation) ann.get("object-1");
		} else return null;
		return Resolve.getHeadC(governingConstituent);
	}

	static String governingPreposition (Document doc, Annotation ann) {
		Annotation pp = (Annotation) ann.get("p-obj-1");
		if (pp == null)
			return null;
		Annotation[] ppChildren = (Annotation[]) pp.get("children");
		if (ppChildren.length != 2)
			return null;
		Annotation in = ppChildren[0];
		String prep = doc.text(in).trim();
		return prep;
	}

	static int getACEoffset (int posn) {
		return posn;
	}

	static int getJetOffset (int posn) {
		return posn;
	}

	// for David's code

	 /**
     * @param doc2
     */
    public static void addParentLinks(ExternalDocument doc2) {
        Vector allAnnotations = doc2.annotationsOfType("constit");
        if (allAnnotations == null) return;
        Iterator anns = allAnnotations.iterator();
        while (anns.hasNext()) {
            Annotation constit = (Annotation) anns.next();
            Annotation[] children = ParseTreeNode.children(constit);
            if (children == null) continue;
            for (int c=0; c < children.length; ++c)
                if (children[c] != null) children[c].put("parent", constit);
        }
    }
}
