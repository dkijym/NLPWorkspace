// -*- tab-width: 4 -*-
package Jet.HMM;

import java.io.*;
import java.util.*;

/**
 *  adds words from a file to an HMM.
 */

class HMMAugmentor {

	static final String home = "";
	static final String ACEdir = home + "ACE/";
	static String originalHMMfile = home + "HMM/NE/ACEname04bigramHMM.txt";
	static String newHMMfile = home + "HMM/NE/ACEname04bigramxHMM.txt";
	static String nameListFile = ACEdir + "qq.txt";
	// map from state to map from token to count
	// static String[] stateSet = {"b-person", "m-person", "e-person", "i-person"};
	// static int[] initialCount = new int[stateSet.length];
	static HashMap stateUnigramTables = new HashMap();
	static HashMap stateBigramTables = new HashMap();
	static HMM hmm;

	public static void main (String[] args) throws IOException {
		// read augmentation file, collect sets
		BufferedReader reader = new BufferedReader (new FileReader(nameListFile));
		String line;
		while ((line = reader.readLine()) != null) {
			String[] ray = line.split(" \\| ");
			if (ray.length != 3) continue;
			String name = ray[0];
			String type = ray[1];
			String head = ray[2];
			String[] tokens = name.split(" ");
			if (type.equals("PERSON") || type.equals("LASTNAME")) {
				if (tokens.length == 1) {
					addTokenToState (tokens[0], "i-person");
				} else {
					addTokenToState (tokens[0], "b-person");
					for (int i=1; i<tokens.length-1; i++)
						addBigramToState (tokens[i-1], tokens[i], "m-person");
					addBigramToState (tokens[tokens.length-2], tokens[tokens.length-1], "e-person");
				}
			} else if (type.equals("COMPANY") || type.equals("ORGANIZATION")) {
				if (tokens.length == 1) {
					addTokenToState (tokens[0], "i-org");
				} else {
					addTokenToState (tokens[0], "b-org");
					for (int i=1; i<tokens.length-1; i++)
						addBigramToState (tokens[i-1], tokens[i], "m-org");
					addBigramToState (tokens[tokens.length-2], tokens[tokens.length-1], "e-org");
				}
			} else if (type.equals("GPE") || type.equals("CITY")) {
				if (tokens.length == 1) {
					addTokenToState (tokens[0], "i-gpe");
				} else {
					addTokenToState (tokens[0], "b-gpe");
					for (int i=1; i<tokens.length-1; i++)
						addBigramToState (tokens[i-1], tokens[i], "m-gpe");
					addBigramToState (tokens[tokens.length-2], tokens[tokens.length-1], "e-gpe");
				}
			}
		}
		// load HMM
		hmm = new HMM(BigramHMMemitter.class); // WordFeatureHMMemitter.class);
		Reader HMMreader = new BufferedReader (new FileReader(originalHMMfile));
		hmm.load(HMMreader);
		/*
		// save state counts
		for (int i=0; i<stateSet.length; i++) {
			HMMstate state = hmm.getState(stateSet[i]);
			WordFeatureHMMemitter emitter = (WordFeatureHMMemitter) state.emitter;
			initialCount[i] = emitter.count;
		}
		*/
		// augment HMM

		Iterator it = stateUnigramTables.keySet().iterator();
		while (it.hasNext()) {
			String state = (String) it.next();
			TreeSet tokenSet = (TreeSet) stateUnigramTables.get(state);
			System.out.println ("For state " + state + " no of tokens = " + tokenSet.size());
			addUnigramEmitters (state, tokenSet);
		}
		/*
		Iterator bit = stateBigramTables.keySet().iterator();
		while (bit.hasNext()) {
			String state = (String) bit.next();
			HashSet bigramSet = (HashSet) stateBigramTables.get(state);
			System.out.println ("For state " + state + " no of bigrams = " + bigramSet.size());
			addBigramEmitters (state, bigramSet);
		}
		*/
		/*
		// adjust cache counts
		for (int i=0; i<stateSet.length; i++) {
			HMMstate state = hmm.getState(stateSet[i]);
			WordFeatureHMMemitter emitter = (WordFeatureHMMemitter) state.emitter;
			HashMap cacheCount = emitter.cacheCount;
			int newCount = emitter.count;
			Iterator cacheIterator = cacheCount.entrySet().iterator();
			while (cacheIterator.hasNext()) {
				Map.Entry entry = (Map.Entry) cacheIterator.next();
				String type = (String) entry.getKey();
				int oldCacheCount = ((Integer) entry.getValue()).intValue();
				int newCacheCount = (oldCacheCount * newCount) / initialCount[i];
				entry.setValue(new Integer(newCacheCount));
			}
		}
		*/
		// save HMM
		PrintWriter pw = new PrintWriter (new FileOutputStream (newHMMfile));
		hmm.store(pw);
	}

	static void addTokenToState (String token, String state) {
		addBigramToState ("", token, state);
	}

	static void addBigramToState (String priorToken, String token, String state) {
		TreeSet tokenSet = (TreeSet) stateUnigramTables.get(state);
		if (tokenSet == null) {
			tokenSet = new TreeSet();
			stateUnigramTables.put(state, tokenSet);
		}
		tokenSet.add(token);

		HashSet bigramSet = (HashSet) stateBigramTables.get(state);
		if (bigramSet == null) {
			bigramSet = new HashSet();
			stateBigramTables.put(state, bigramSet);
		}
		String[] pair = new String[] {priorToken, token};
		bigramSet.add(pair);
	}


	static void addUnigramEmitters (String state, TreeSet tokenSet) {
		HMMstate currentState = hmm.getState(state);
		Iterator it = tokenSet.iterator();
		while (it.hasNext()) {
			String token = (String) it.next();
			currentState.incrementEmitCount(token, "", 1);
		}
	}

	static void addBigramEmitters (String state, HashSet bigramSet) {
		HMMstate currentState = hmm.getState(state);
		Iterator it = bigramSet.iterator();
		while (it.hasNext()) {
			String[] pair = (String[]) it.next();
			currentState.incrementEmitCount(pair[1], pair[0], 1);
		}
	}


}
