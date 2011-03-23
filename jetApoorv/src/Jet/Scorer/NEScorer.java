// -*- tab-width: 4 -*-
package Jet.Scorer;

import java.util.*;
import java.io.*;
import Jet.Tipster.*;
import Jet.Console;

/**
 *  compute recall/precision scores for named entities.
 */

public class NEScorer {

	/**
	 *  computes the recall/precision of 'testCollection' with respect to
	 *  'keyCollection' (which should have the same documents) with respect
	 *  to the name annotations in 'tagsToScore'.  Reports both per-document
	 *  and total scores to System.out.
	 */

	public static void scoreCollection (NameTagger tagger,
			String testCollection, String keyCollection, String[] tagsToScore) {
		DocumentCollection testCol = new DocumentCollection(testCollection);
		testCol.open();
		DocumentCollection keyCol = new DocumentCollection(keyCollection);
		keyCol.open();
		if (testCol.size() != keyCol.size()) {
			System.out.println (" ** Test and key collections have different sizes, cannot evaluate.");
			return;
		}
		int tagsInResponses = 0;
		int tagsInKeys = 0;
		int matchingTags = 0;
		int matchingAttrs = 0;
		for (int i=0; i<testCol.size(); i++) {
			// open test document
			ExternalDocument testDoc = testCol.get(i);
			testDoc.setAllTags (true);
			testDoc.open();
			testDoc.stretchAll();
			// annotate test document
			System.out.println ("Annotating " + testDoc.fileName());
			tagger.tagDocument (testDoc);
			// display with 'view'
			if (i < 100) new View (testDoc, i);
			// open key document and display it
			ExternalDocument keyDoc = keyCol.get(i);
			keyDoc.setAllTags (true);
			keyDoc.open();
			keyDoc.stretchAll();
			Vector<Annotation> textSpans = keyDoc.annotationsOfType("TEXT");
			if (textSpans == null) {
				System.out.println ("No <TEXT> in " + testDoc.fileName() + ", cannot be scored.");
				continue;
			}
			Span textSpan = textSpans.get(0).span();
			for (String tag : tagsToScore)
				eraseAnnotationsOutside (keyDoc, tag, textSpan);
			if (i < 100) new View (keyDoc, i + 100);
			// score zones
			SGMLScorer scorer = new SGMLScorer(testDoc, keyDoc);
			for (String tag : tagsToScore) {
				scorer.match(tag);
				Console.println (scorer.report());
			}
			System.out.println ("Total tags in response:  " + scorer.totalTagsInDoc1);
			System.out.println ("Total tags in key:       " + scorer.totalTagsInDoc2);
			System.out.println ("Matching tags:           " + scorer.totalMatchingTags);
			System.out.println ("Matching attributes:     " + scorer.totalMatchingAttrs);
			System.out.println ("Type recall:             " +
			                    (float) scorer.totalMatchingTags / scorer.totalTagsInDoc2);
			System.out.println ("Type precision:          " +
			                    (float) scorer.totalMatchingTags / scorer.totalTagsInDoc1);
			System.out.println ("Attribute recall:        " +
			                    (float) scorer.totalMatchingAttrs / scorer.totalTagsInDoc2);
			System.out.println ("Attribute precision:     " +
			                    (float) scorer.totalMatchingAttrs / scorer.totalTagsInDoc1);
			tagsInResponses += scorer.totalTagsInDoc1;
			tagsInKeys      += scorer.totalTagsInDoc2;
			matchingTags    += scorer.totalMatchingTags;
			matchingAttrs   += scorer.totalMatchingAttrs;
		}
		System.out.println ("Overall Type Recall:          " +
		                    (float) matchingTags / tagsInKeys);
		System.out.println ("Overall Type Precision:       " +
		                    (float) matchingTags / tagsInResponses);
		System.out.println ("Overall Attribute Recall:     " +
		                    (float) matchingAttrs / tagsInKeys);
		System.out.println ("Overall Attribute Precision:  " +
		                    (float) matchingAttrs / tagsInResponses);
	}

	private static void eraseAnnotationsOutside (Document doc, String type, Span span) {
		Vector v = doc.annotationsOfType(type);
		if (v == null) return;
		v = (Vector) v.clone();
		for (int i=0; i<v.size(); i++) {
			Annotation a = (Annotation) v.get(i);
			if (!a.span().within(span)) {
				doc.removeAnnotation(a);
			}
		}
	}
}
