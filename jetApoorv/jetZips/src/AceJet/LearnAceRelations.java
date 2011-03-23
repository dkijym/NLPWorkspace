// -*- tab-width: 4 -*-
package AceJet;

import java.io.IOException;

//Author:       Ralph Grishman
//Date:         March 10, 2005

/**
 *  invoke LearnRelations for ACE data.
 */

public class LearnAceRelations {

	/* // for 2004
	static final String ACEdir =
	    "C:/Documents and Settings/Ralph Grishman/My Documents/ACE/";
	static final String rootDir =
	    ACEdir + "relations/";
	static final String patternFile = rootDir + "patterns05x.log";
	*/ // for 2005
	static final String ACEdir =
	    "C:/Documents and Settings/Ralph Grishman/My Documents/ACE 05/V4/";
	static final String rootDir =
	    ACEdir + "../models/";
	static final String patternFile = rootDir + "patterns05-8.log";

	public static void main (String[] args) throws IOException {
		// don't accidentally overwrite pattern file
		// if (true) return;

		String configFile = null;
		if (LearnRelations.useParser) {
			if (LearnRelations.useParseCollection) {
				configFile = "props/ace use parses.properties";
			} else {
				configFile = "props/ace parser.properties";
			}
		} else {
			configFile = "props/ME ace.properties";
		}
		LearnRelations relLearner = new LearnRelations (configFile, patternFile);

		// relLearner.learnFromFileList (ACEdir + "training all but eeld.txt", ACEdir, ACEdir);
		// relLearner.learnFromFileList (ACEdir + "sep02 all.txt", ACEdir, ACEdir);
		// relLearner.learnFromFileList (ACEdir + "aug03 all.txt", ACEdir, ACEdir);
		// relLearner.learnFromFileList (ACEdir + "training04 nwire 21andup.txt", ACEdir, ACEdir);
		// relLearner.learnFromFileList (ACEdir + "training04 bnews 21andup.txt", ACEdir, ACEdir);
		/*  final 2004 training
		relLearner.learnFromFileList (ACEdir + "training04 nwire.txt", ACEdir, ACEdir);
		relLearner.learnFromFileList (ACEdir + "training04 bnews.txt", ACEdir, ACEdir);
		relLearner.learnFromFileList (ACEdir + "training04 chinese.txt", ACEdir, ACEdir);
		relLearner.learnFromFileList (ACEdir + "training04 arabic.txt", ACEdir, ACEdir);
		*/
		// 2005 training
		relLearner.learnFromFileList (ACEdir + "tail.txt", ACEdir, ACEdir);

		relLearner.finish();
	}
}
