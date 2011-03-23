// -*- tab-width: 4 -*-
package Jet.Parser;

import Jet.*;
import Jet.Tipster.*;
import Jet.Lisp.*;
import Jet.Lex.Tokenizer;
import Jet.HMM.HMMTagger;
import Jet.HMM.Retagger;
import Jet.Pat.Pat;
import Jet.Chunk.Chunker;
import Jet.Zoner.SpecialZoner;

import danbikel.parser.Parser;
import danbikel.parser.Settings;
import danbikel.parser.english.HeadFinder;
import danbikel.lisp.*;

import java.util.*;
import java.io.*;
import java.rmi.*;
import java.lang.reflect.*;

/**
 *  an interface to Dan Bikel's implementation of Collins' parser
 */

public class StatParser {

	static int nextToken = 0;
	static ArrayList words;
	static ArrayList spans;
	static ArrayList wordDefns;
	static ArrayList pennPOS;
	static Parser parser;
	static boolean initialized = false;
	static final boolean findHeads = true;
	static HeadFinder headFinder = null;

	static final String home =
	    "C:/Documents and Settings/Ralph Grishman/My Documents/";
	static final String ACEdir =
	    home + "ACE 05/V4/";

	public static void main (String[] args) throws IOException {

		// String collection = ACEdir + "training npaper sgm.txt";
		// String collection = ACEdir + "sep02 nwire sgm.txt";
		// -------- 2004 training
		// String collection = ACEdir + "training04 nwire sgm.txt";
		// String collection = ACEdir + "training04 bnews 20 sgm.txt";
		// String collection = ACEdir + "training04 bnews sgm.txt";
		// String collection = ACEdir + "training04 chinese sgm.txt";
		// String collection = ACEdir + "training04 arabic sgm.txt";
		// -------- 2004 evaluation
		// String collection = ACEdir + "eval04 nwire sgm.txt";
		// String collection = ACEdir + "eval04 bnews sgm.txt";
		// String parseCollection = ACEdir + "parses/training04 nwire parses.txt";
		// String parseCollection = ACEdir + "parses/training04 bnews 20 parses.txt";
		// String parseCollection = ACEdir + "parses/training04 bnews parses.txt";
		// String parseCollection = ACEdir + "parses/training04 chinese parses.txt";
		// String parseCollection = ACEdir + "parses/training04 arabic parses.txt";
		// String parseCollection = ACEdir + "parses/eval04 nwire parses.txt";
		// String parseCollection = ACEdir + "parses/eval04 bnews parses.txt";
		// -------- 2005 dev test
		String collection = ACEdir + "nw-head.txt";
		String parseCollection = ACEdir + "parses/nw-head.txt";
		String parseDir = ACEdir + "parses/";

		if (args.length == 3) {
			collection = args[0];
			parseCollection = args[1];
			parseDir = args[2];
		}
		boolean useRelativePathsInList = false;
		parseCollection (collection, parseCollection, parseDir, useRelativePathsInList);
		/*
		JetTest.initializeFromConfig("ace parser.properties");
		// Document doc = new Document("<TEXT>Asbestos kills people. </TEXT>");
		// Document doc = new Document("<TEXT>According to vice president Al Gore, asbestos has killed lots of people. </TEXT>");
		Document doc = new Document("<TEXT>According to \"Al Gore\" ( the former vice president ) asbestos kills people. </TEXT>");
		Control.processDocument (doc, null, false, 1);
		doc.setSGMLindent(4);
		StringBuffer s = doc.writeSGML("constit");
		System.out.println ("\n" + s);
		/*
		Span span = new Span(0,doc.length());
		initialize("../dbparser/settings/collins.properties",
		           "../dbparser/00.obj");
		Tokenizer.tokenize(doc, span);
		HMMTagger tagger = new HMMTagger();
    tagger.load ("C:/Documents and Settings/Ralph Grishman/My Documents/Jet/Data/pos_hmm.txt");
		tagger.tagPenn(doc, span);
		ParseTreeNode parseTree = parse(doc, span);
		new ParseTreeView("", parseTree);
		// parseTree.printTree();
		*/
	}

	/**
	 *  initialize the parser (load grammar and data files).  File names
	 *  are obtainted from properties 'StatParser.properties.fileName' and
	 *  'StatParser.grammar.fileName', and are relative to 'dataPath'.
	 */


    public static void initialize (String dataPath, Properties config) {
    String properties = config.getProperty("StatParser.properties.fileName");
    String grammar = config.getProperty("StatParser.grammar.fileName");
    if (properties == null && grammar == null)
    	return;
    if (properties == null || grammar == null) {
    	System.err.println ("Error in properties file:  for StatParser, both");
    	System.err.println ("properties.Filename and grammar.fileName must be specified");
    	return;
    }
    initialize (dataPath + File.separatorChar + properties,
                dataPath + File.separatorChar + grammar);
  }

  /**
   *  initialize the parser.  Load the properties from 'propertiesFile' and
   *  the grammar from file 'grammarFile'.
   */

	public static void initialize (String propertiesFile, String grammarFile) {
		try {
			Settings.load(propertiesFile);
			parser = new Parser(grammarFile);
			initialized = true;
		} catch (Exception e) {
			System.out.println (e);
			System.out.println ("Unable to initialize parser.");
		}
	}

	/**
	 *  return true if the parser has already been initialized.
	 */

	public static boolean isInitialized () {
		return initialized;
	}

	/**
	 *  parse the sentence in 'span' of Document 'doc'.  The
	 *  sentence must have been tokenized and tagged with Penn tags.
	 *  The parse is returned as a nested set of ParseTreeNodes and is
	 *  also added as 'constit' annotations on the Document.
	 */

	public static ParseTreeNode parse (Document doc, Span span) {
		// run part-of-speech tagger
		JetTest.tagger.annotate(doc, span, "tagger");
		int start = span.start();
		int end = span.end();
		buildParserInput (doc, start, end);
		fixHyphenatedItems (doc);
		if (wordDefns.size() == 0) {
			System.out.println ("StatParse:  no tokens in span");
			return null;
		}
		// convert tokens to SexpList for parser
		SexpList sentence = new SexpList();
		for (int i=0; i<words.size(); i++) {
			String word = (String) words.get(i);
			String pos = (String) pennPOS.get(i);
			SexpList posSx = new SexpList();
			posSx.add(Symbol.get(pos));
			SexpList wordSx = new SexpList();
			wordSx.add(Symbol.get(word));
			wordSx.add(posSx);
			sentence.add(wordSx);
		}
		// invoke Bikel parser
		System.out.println ("Sentence = " + sentence.toString());
		SexpList parseTreeSexp;
		try {
			parseTreeSexp = (SexpList) parser.parse(sentence);
		} catch (Exception e) {
			System.out.println (e);
			System.out.println ("No parse possible.");
			return null;
		}
		if (parseTreeSexp == null) {
			System.out.println ("No parse possible.");
			return null;
		}
		System.out.println ("Parse = " + parseTreeSexp);
		// convert returned parse tree to Jet parseTree and to
		// a set of annotations
		if (findHeads && (headFinder == null))
			try {
				headFinder = new HeadFinder();
			} catch (IOException e) {
				System.out.println ("StatParser: " + e);
				System.out.println ("Unable to generate heads.");
			}
		nextToken = 0;
		ParseTreeNode parseTree = makeParseTree(doc, parseTreeSexp);
		// parseTree.printTree();
		Annotation rootAnnotation = ParseTreeNode.makeParseAnnotations(doc, parseTree);
		deleteUnusedConstits (doc, span, rootAnnotation);
		// add link from sentence annotation
		Vector anns = doc.annotationsAt(start, "sentence");
		if (anns != null && anns.size() > 0) {
			Annotation sentAnn = (Annotation) anns.get(0);
			sentAnn.put("parse", rootAnnotation);
		}
		return parseTree;
	}

	/**
	 *  deletes all annotations of type 'constit' within span 'span' of
	 *  Document 'doc' which are not descendants of 'rootAnnotation'.  This
	 *  method is used to delete unused word definitions after a parse tree
	 *  has been built.
	 */

	private static void deleteUnusedConstits (Document doc,
	    Span span, Annotation rootAnnotation) {
		Set annotationsInTree = descendants(rootAnnotation);
		Vector constits = doc.annotationsOfType("constit", span);
		if (constits == null)
			return;
		for (int i=0; i<constits.size(); i++) {
			Annotation a = (Annotation) constits.get(i);
			if (annotationsInTree == null || !annotationsInTree.contains(a))
				doc.removeAnnotation(a);
		}
	}

	/**
	 *  build the arrays 'words', 'spans', 'wordDefns', and 'pennPOS' for
	 *  the parser:
	 *    words[i] = the i-th word string, for the PTB parser
	 *               (normalized to PTB form, such as -LRB- for '('
	 *    spans[i] = the span of the i-th word string
	 *    wordDefns[i] = the Jet word defn of the i-th sentence element
	 *                   (if there are several defns, takes the first one)
	 *    pennPOS[i]   = the PTB POS for the i-th sentence element
	 */

	private static void buildParserInput (Document doc, int start, int end) {
		wordDefns = new ArrayList();
		pennPOS = new ArrayList();
		words = new ArrayList();
		spans = new ArrayList();
		int posn = Tokenizer.skipWSX(doc, start, end);
		while (posn < end) {
			// get wordDefn from constit
			// (get first non-hidden constit)
			Vector constits = doc.annotationsAt(posn, "constit");
			Annotation constit = null;
			String cat = null;
			if (constits != null) {
				for (int i=0; i<constits.size(); i++) {
					Annotation a = (Annotation) constits.get(i);
					if (a.get("hidden") == null) {
						constit = a;
						cat = (String) constit.get("cat");
						break;
					}
				}
			}
			wordDefns.add(constit);
			// get Penn POS
			String pos = ptbPOS (doc, posn, constit, cat);
			pennPOS.add(pos);
			// get span:  from constit if present, else from token
			Span wspan;
			if (constit != null) {
				wspan = constit.span();
			} else {
				Annotation token = doc.tokenAt(posn);
				if (token == null) break;
				wspan = token.span();
			}
			spans.add(wspan);
			String word = doc.text(wspan).trim();
			// TreeBank encodes dash as "--", while ACE encodes it as an underscore
			if (word.equals("_"))
				word = "--";
			else if (word.equals("("))
				word = "-LRB-";
			else if (word.equals(")"))
				word = "-RRB-";
			// Bikel parser won't work with " token
			else if (word.equals("\""))
				word = "''";
			words.add(word);
			posn = wspan.end();
		}
	}

	/**
	 *  determine the Penn POS for a word (for use by Bikel parser).
	 *  The word starts at position 'posn' and has constituent annotation
	 *  'constit' which has cat feature 'cat'.
	 */

	private static String ptbPOS (Document doc, int posn,
	                              Annotation constit, String cat) {
		// is Jet defn a name?
		if (cat == "name") {
			//  yes ... pos = NNP
			return "NNP";
		} else {
			//  no ... look up Penn pos from tagger
			Vector v = doc.annotationsAt(posn, "tagger");
			Annotation a = (Annotation) v.get(0);
			String pos = (String) a.get("cat");
			String textSpanned = doc.text(a).trim().toLowerCase();
			//  get all Jet defns
			Vector constits = doc.annotationsAt(posn, "constit");
			// if there are no Jet defns, use Penn POS
			if  (constits == null || constits.size() == 0)
				return pos;
			// if Penn POS from tagger is compatible with some Jet defn, use Penn POS
			for (int ic=0; ic<constits.size(); ic++) {
				Annotation jetDefn = (Annotation) constits.get(ic);
				if (a.span().equals(jetDefn.span())) {
					FeatureSet[] z = Retagger.ptbToJetFS(textSpanned, pos);
					for (int i=0; i<z.length; i++) {
						if (z[i].subsetOf(jetDefn.attributes())) {
							return pos;
						}
					}
				}
			}
			// Penn POS is not compatible with any Jet defn;  try to
			//   determine Penn POS from Jet POS
			if (constit != null) {
				String p = Retagger.jetToPtbPos(constit.attributes());
				if (p != null)
					return p;
			}
			// if that is not possible, use Penn POS from tagger
			return pos;
		}
	}

	/**
	 *  for hyphenated forms X-Y, which are treated as three separate tokens
	 *  by the ACE tokenizer, create a single constituent with category 'hyphword'
	 *  and Penn POS JJ.
	 */

	private static void fixHyphenatedItems (Document doc) {
		for (int i=1; i<words.size()-1; i++) {
			if (words.get(i).equals("-") &&
			    ((Span)spans.get(i-1)).end() == ((Span)spans.get(i)).start() &&
			    ((Span)spans.get(i)).end() == ((Span)spans.get(i+1)).start()) {
			  // create constituent for hyphen
			  Annotation hyphenDefn = new Annotation ("constit", (Span)spans.get(i),
				                                         new FeatureSet ("cat", "-"));
				doc.addAnnotation(hyphenDefn);
				wordDefns.set(i, hyphenDefn);
				// create new constituent for hyphen and surrounding words,
				// with children = current wordDenfs
				Span hwSpan = new Span(((Span)spans.get(i-1)).start(), ((Span)spans.get(i+1)).end());
				Annotation[] children = new Annotation[3];
				children[0] = (Annotation) wordDefns.get(i-1);
				if (children[0] == null)
					children[0] = new Annotation ("constit", (Span)spans.get(i-1),
				                                new FeatureSet ("cat", "?"));
				children[1] = (Annotation) wordDefns.get(i);
				children[2] = (Annotation) wordDefns.get(i+1);
				if (children[2] == null)
					children[2] = new Annotation ("constit", (Span)spans.get(i+1),
				                                new FeatureSet ("cat", "?"));
				Annotation hwDefn =
					new Annotation("constit", hwSpan,
					               new FeatureSet("cat", "hyphword", "children", children));
				doc.addAnnotation(hwDefn);
				// replace current constits in ArrayList by new item
				spans.set(i-1, hwSpan);
				spans.remove(i+1);
				spans.remove(i);
				words.set(i-1, doc.text(hwSpan).trim());
				words.remove(i+1);
				words.remove(i);
				pennPOS.set(i-1, "JJ");
				pennPOS.remove(i+1);
				pennPOS.remove(i);
				wordDefns.set(i-1, hwDefn);
				wordDefns.remove(i+1);
				wordDefns.remove(i);
			}
		}
	}

	/*
	 *  converts a parse tree in the form of nested Sexp's to
	 *  nested ParseTreeNodes.  The variable 'nextToken' must be
	 *  initialized to the first token spanned before calling this
	 *  function.
	 */

	private static ParseTreeNode makeParseTree (Document doc, Sexp sx) {
		if (!sx.isList()) {
			System.out.println ("StatParse:  invalid Sexp for parse node " + sx.toString());
			return null;
		}
		SexpList s = (SexpList) sx;
		if (s.length() < 2) {
			System.out.println ("StatParse:  invalid Sexp for parse node " + s.toString());
			return null;
		}
		Sexp catSx = s.get(0);
		if (!catSx.isSymbol()) {
			System.out.println ("StatParse:  invalid Sexp for parse node " + s.toString());
			return null;
		}
		String pennPOS = ((Symbol) catSx).toString().intern();
		String cat = pennPOS.toLowerCase().intern();
		Sexp wordSx = s.get(1);
		if (wordSx.isSymbol()) {
			// leaf node
			String expectedWord = ((Symbol) wordSx).toString();
			String word = (String) words.get(nextToken);
			while (!word.equals(expectedWord)) {
				System.out.println ("StatParser:  parse skips " + word + " in sentence.");
				nextToken++;
				if (nextToken >= words.size()) {
					System.out.println ("*** Unable to align sentence and parse tree.");
					return null;
				}
				word = (String) words.get(nextToken);
			}
			Annotation wordDefn = buildWordDefn(doc, nextToken, pennPOS);
			nextToken++;
			return new ParseTreeNode (cat, null, wordDefn.start(), wordDefn.end(), wordDefn, word);
		} else {
			// internal node
			int head = 0;
			if (findHeads) {
				head = headFinder.findHead(sx);
			}
			int childCount = s.length() - 1;
			int startToken = nextToken;
			int start = ((Span)spans.get(startToken)).start();
			ParseTreeNode[] children = new ParseTreeNode[childCount];
			for (int iChild = 0; iChild < childCount; iChild++) {
				children[iChild] = makeParseTree(doc, s.get(iChild+1));
			}
			int end = start;
			if (nextToken > startToken)
				end = ((Span)spans.get(nextToken - 1)).end();
			return new ParseTreeNode (cat, children, start, end, head);
		}
	}

	private static Annotation buildWordDefn (Document doc, int itoken, String pennPOS) {
		// special wordDefns ... name and hyphword ... keep
		Annotation wordDefn = (Annotation) wordDefns.get(itoken);
		if (wordDefn != null) {
			String cat = (String) wordDefn.get("cat");
			if (cat == "name" || cat == "hyphword") {
				return wordDefn;
			}
		}
		// look for Jet defn compatible with pennPOS
		String word = (String) words.get(itoken);
		Span span = (Span) spans.get(itoken);
		Vector jetAnns = doc.annotationsAt(span.start(), "constit");
		if (jetAnns != null) {
			for (int i=0; i<jetAnns.size(); i++) {
				Annotation jetAnn = (Annotation) jetAnns.get(i);
				if (jetAnn.get("hidden") != null) continue;
				if (Retagger.compatible(word, pennPOS, jetAnn))
					return jetAnn;
			}
		}
		// no compatible Jet defn
		//    use first Jet defn
		if (pennPOS != "POS" && wordDefn != null)
			return wordDefn;
		// if no Jet defn at all
		// create Jet defn from Penn POS
		// try to create one by mapping Penn POS
		FeatureSet[] FSpenn = Retagger.ptbToJetFS(word, pennPOS);
		if (FSpenn.length > 0) {
			FeatureSet jetFS = new FeatureSet(FSpenn[0]);
			String cat = (String) jetFS.get("cat");
			if (cat == "n" || cat == "v" || cat == "tv" || cat == "ving" || cat == "ven")
				jetFS.put("pa",new FeatureSet("head", word.toLowerCase().intern()));
			wordDefn = new Annotation ("constit", span, jetFS);
			doc.addAnnotation(wordDefn);
			return wordDefn;
		}
		// otherwise use Penn POS (in lower case)
		wordDefn = new Annotation ("constit", span,
		                           new FeatureSet ("cat", pennPOS.toLowerCase().intern()));
		doc.addAnnotation(wordDefn);
		return wordDefn;
	}

	/**
	 *  returns a Set containing the parse tree node and all of its
	 *  descendants (its children, the children of its children, etc.).
	 *
	 *  @param node an Annotation representing a parse tree node
	 *              (an Annotation of type 'constit').
	 */

	public static Set descendants (Annotation node) {
		HashSet d = new HashSet();
		d.add(node);
		Annotation[] children = ParseTreeNode.children(node);
		if (children != null) {
			for (int i=0; i<children.length; i++) {
				if (children[i] != null) d.addAll(descendants(children[i]));
			}
		}
		// Added "if (children[i] != null)" 11/21/04, MDW, adapted to new RG version 9/11/05.
		// Child id references in parsed file sometimes reference nonexistent constit id's.
		// (Example: aquaint corpus file 20000101_NYT85, constit 48 has nonexistent child 55.)
		// Until this can be fixed, we weed out these null children.
		// TODO: fix null children produced by parser.
		return d;
	}

	/**
	 * @param collection
	 * @param parseCollection
	 * @param parseDir
	 */
	private static void parseCollection(String collection, String parsedCollection, String parseDir, boolean relative)
		throws IOException {
		System.out.println("Starting ACE Jet...");
		JetTest.initializeFromConfig("props/ace just parser 05.properties");
		Chunker.loadModel();
		// turn off traces
		Pat.trace = false;
		// open text collection
		DocumentCollection col = new DocumentCollection(collection);
		col.open();
		for (int docCount = 0; docCount < col.size(); docCount++) {
			// process file 'currentDoc'
			// if (docCount != 0) continue;
			ExternalDocument doc = col.get(docCount);
			System.out.println ("\nProcessing document " + docCount + ": " + doc.fileName());
			// read test document
			doc.setAllTags(true);
			doc.open();
			// process document
			SpecialZoner.findSpecialZones (doc);
			AceJet.Ace.monocase = AceJet.Ace.allLowerCase(doc);
			System.out.println (">>> Monocase is " + AceJet.Ace.monocase);
			Jet.HMM.BigramHMMemitter.useBigrams = AceJet.Ace.monocase;
			Jet.HMM.HMMstate.otherPreference = AceJet.Ace.monocase ? 1.0 : 0.0;
			Control.processDocument (doc, null, docCount == -1, docCount);
			// remove name and tagger annotations -- not needed after parsing
			doc.removeAnnotationsOfType ("ENAMEX");
			clearInputAnnotations (doc);
			doc.removeAnnotationsOfType ("tagger");
		}
		if (relative) col.saveAs (parsedCollection, parseDir);
        else col.saveAsAbsolute (parsedCollection, parseDir);
	}

	/**
	 *  for ACE:  erase all the characters within ANNOTATION ... /ANNOTATION
	 */

	public static void clearInputAnnotations (Document doc) {
		Vector anns = doc.annotationsOfType("ANNOTATION");
		if (anns == null) return;
		for (int i=0; i<anns.size(); i++) {
			Annotation ann = (Annotation) anns.get(i);
			int start = ann.span().start();
			int end = ann.span().end();
			for (int j=start; j<end; j++) {
				if (!Character.isWhitespace(doc.charAt(i))) doc.setCharAt(i,' ');
			}
		}
		doc.removeAnnotationsOfType ("ANNOTATION");
	}
}
