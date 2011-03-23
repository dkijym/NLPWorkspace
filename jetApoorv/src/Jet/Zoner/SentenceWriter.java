// -*- tab-width: 4 -*-
//Title:        JET
//Version:      1.30
//Copyright:    Copyright (c) 2005
//Author:       Ralph Grishman
//Description:  A Java-based Information Extraction Tool

package Jet.Zoner;

import Jet.Tipster.*;
import AceJet.Ace;
import java.util.*;
import java.io.*;

/**
 *  write a Document out, one sentence per line.
 *  Each sentence is preceded by the character offset of the sentence in
 *  the document.
 */

public class SentenceWriter {

	static final String home =
	  "C:/Documents and Settings/Ralph Grishman/My Documents/";
	static final String ACEdir =
	  home + "ACE 05/V4/"; // "ACE 05/eval/";
	static String dataDir =
		ACEdir;
	static String outputDir =
		ACEdir + "sents/";
	static String fileList =
		ACEdir + "allSgm.txt"; // "evalSgm.txt";
	static boolean writeXML = false;

	/**
	 *  generate sentence list files for a list of documents.
	 *  Takes 3 or 4 command line parameters:                                  <BR>
	 *  filelist:  a list of the files to be processed                         <BR>
	 *  dataDir:  the path of the directory containing the document            <BR>
	 *  outputDir:  the path of the directory to contain the output            <BR>
	 *  writeXML:  if present, sentences are written in an XML format with
	 *            offsets only                                                 <BR>
	 *  For each <I>file</I> in <I>filelist</I>, the document is read from
	 *  <I>dataDir/file</I> and the sentence file is written to
	 *  <I>outputDir/file</I>.sent .  Only sentences within TEXT XML elements
	 *  are written.
	 */

	public static void main (String[] args) throws IOException {
		if (args.length > 0) {
			if (args.length < 3 || args.length > 4) {
				System.out.println ("SentenceWriter must have 3 or 4 arguments:");
				System.out.println ("  filelist  dataDirectory  outputDirectory [XMLflag]");
				System.exit(1);
			}
			fileList  = args[0];
			dataDir   = args[1];
			outputDir = args[2];
			writeXML  = args.length == 4;
		}
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
			String textFileName = dataDir + currentDoc;
			ExternalDocument doc = new ExternalDocument("sgml", textFileName);
			doc.setAllTags(true);
			doc.open();
			String sentFileName = outputDir + currentDoc + ".sent";
			PrintWriter writer = new PrintWriter (new FileWriter (sentFileName));
			writeSents (doc, currentDoc, writer);
			writer.close();
		}
	}

	private static void writeSents (ExternalDocument doc, String currentDocPath, PrintWriter writer) {
		doc.annotateWithTag ("TEXT");
		SpecialZoner.findSpecialZones (doc);
		Vector textSegments = doc.annotationsOfType ("TEXT");
		if (textSegments == null) {
			System.out.println ("No <TEXT> in " + currentDocPath + ", skipped.");
			return;
		}
		Iterator it = textSegments.iterator ();
		while (it.hasNext ()) {
			Annotation ann = (Annotation)it.next ();
			Span textSpan = ann.span ();
			// check document case
			Ace.monocase = Ace.allLowerCase(doc);
			// split into sentences
			SentenceSplitter.split (doc, textSpan);
		}
		if (writeXML) {
			String currentDoc = currentDocPath;
			if (currentDocPath.indexOf('/') >= 0)
					currentDoc = currentDocPath.substring(currentDocPath.lastIndexOf('/')+1);
			writer.print   ("<source_file URI=\"" + currentDoc + "\"");
			writer.println (" SOURCE=\"newswire\" TYPE=\"text\" AUTHOR=\"NYU\">");
			String docId = Ace.getDocId(doc);
			if (docId == null) {
				if (currentDoc.endsWith(".sgm")) {
					docId = currentDoc.substring(0, currentDoc.length() - 4);
				} else {
					docId = currentDoc;
				}
			}
			writer.println ("<document DOCID=\"" + docId + "\">");
		}
		Vector sentences = doc.annotationsOfType ("sentence");
		if (sentences == null) return;
		Iterator is = sentences.iterator ();
		while (is.hasNext ()) {
			Annotation sentence = (Annotation)is.next ();
			Span sentenceSpan = sentence.span();
			String sentenceText = doc.text(sentenceSpan).trim().replace('\n',' ');
			if (writeXML) {
				//  for XML output form, remove trailing whitespace and give
				//  ACE offset (end - 1)
				doc.shrink(sentence);
				writer.println ("  <sentence>" +
				                "<charseq START=\"" + sentenceSpan.start() + "\"" +
			                  " END=\"" + (sentenceSpan.end()-1) + "\"></charseq>" +
			                  "</sentence>");
				}
			else
				writer.println (sentenceSpan.start() + " " + sentenceText);
		}
		if (writeXML) {
			writer.println ("</document>");
			writer.println ("</source_file>");
		}
	}
}
