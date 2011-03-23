// -*- tab-width: 4 -*-
package Jet.Zoner;

import Jet.Tipster.*;
import java.util.*;

/**
 *  a representation of a document as a set of sentences.  This functionality
 *  is used by annotators which operate across sentences, such as reference
 *  resolution.
 */

public class SentenceSet {

	/**
	 *  the sentence annotations
	 */
	private Vector<Annotation> sentences;
	/**
	 *  the position of the start of each sentence.
	 */
	private int [] sentenceBoundaries;

	/**
	 *  create a new SentenceSet from a document by retrieving all the
	 *  <B>sentence</B> annotations.  Note:  this assumes the sentence
	 *  annotations were created in text order.
	 */

	public SentenceSet (Document doc) {
		sentences = doc.annotationsOfType("sentence");
		sentenceBoundaries = new int[sentences.size()];
		for (int i=0; i<sentences.size(); i++)
			sentenceBoundaries[i] = sentences.get(i).span().start();
	}

	public Vector<Annotation> sentences () {
		return sentences;
	}

	public int size () {
		return sentences.size();
	}

	/*
	 *  returns the number of sentence boundaries between two positions
	 *  in the document, 'posn1' and 'posn2'.
	 */

	public int sentencesBetween (int posn1, int posn2) {
		int count = 0;
		for (int i=0; i<sentenceBoundaries.length; i++)
			if (posn1 < sentenceBoundaries[i] && sentenceBoundaries[i] <= posn2)
				count++;
		return count;
	}

	/**
	 *  returns the number of the sentence containing character 'posn'
	 */

	public int sentenceNumber (int posn) {
		for (int i=1; i<sentenceBoundaries.length; i++) {
			if (posn < sentenceBoundaries[i]) return i-1;
		}
		return sentenceBoundaries.length-1;
	}

	public int pseudoHobbsDistance (int posn1, int posn2) {
		if (sentencesBetween(posn1, posn2) == 0) {
			return posn2 - posn1;
		} else {
			for (int i=0; i<sentenceBoundaries.length -1; i++)
				if (posn1 >= sentenceBoundaries[i] && posn1< sentenceBoundaries[i+1])
					// + 1 is required in the following formula so that the first character of the
					// preceding sentence is further away than the first character of the current
					// sentence
					return (posn1 - sentenceBoundaries[i]) + (posn2 - sentenceBoundaries[i+1]) + 1;
			return 99999;
		}
	}

}
