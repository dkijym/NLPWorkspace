import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBException;

import nlp.Mail.From;
import nlp.Mail.Message;
import nlp.Mail.Thread;
import nlp.Mail.To;
import AceJet.AceDocument;
import AceJet.AceEntity;
import AceJet.AceEntityMention;
import uk.ac.shef.wit.simmetrics.*;
import com.google.common.collect.Sets;

//Use the monge/ or jaro similarity metrics with .9 (?)


public class CorpusStatistics {

	static HashMap<String, ArrayList<String>> tfidfEmails;
	public static HashSet<String> getUniqueNamesEmail() throws JAXBException {
		//mapping of emails to a list of email threads they are present in
		tfidfEmails = new HashMap<String, ArrayList<String>>();

		HashSet<String> uniqueNames = new HashSet<String>();

		for (Thread t : new ThreadCollection(AceJetRunner.IMPORT_DIRECTORYPATH, 70647)) {
			ArrayList<String> uniqueNamesInThread = new ArrayList<String>();
			if (t != null) {
				for (Message m : t.getMessage()) {
					for (Object o : m.getFromOrTo()) {
						String name;
						if (o.getClass() == From.class)
							name =((From) o).getName();
						else
							name = ((To) o).getName();

						if(tfidfEmails.containsKey(name))
						{
							tfidfEmails.get(name).add(t.getID());
						}
						else
						{
							tfidfEmails.put(name, new ArrayList<String>());
							tfidfEmails.get(name).add(t.getID());
						}
						uniqueNames.add(name);
					}
				}
			}
		}
		return uniqueNames;
	}
public static HashMap<String, Set<String>> tfidfMentions;

	public static HashSet<String> getNamesMenetioned() {

		//mapping of emails to a list of email threads they are present in
		tfidfMentions = new HashMap<String, Set<String>>();

		HashSet<String> uniqueNamesMen = new HashSet<String>();
		File dir = new File(APF_DIRECTORY);
		System.out.println(dir.isDirectory());
		File[] threads = dir.listFiles();
		int limit = 100000;
		int i = 0;
		for (File f : threads) {
			if (i > limit)
				break;
			if (f.getName().contains("train") || f.getName().contains("test")) {
				String path = SOURCE_DIRECTORY
						+ f.getName().replace(".apf", "");

				AceDocument d = new AceDocument(path, f.getAbsolutePath());

				ArrayList<AceEntity> entities = d.entities;
				for (AceEntity e : entities)
					if (e.subtype.equals("Individual")) {
						// System.out.println("----");
						for (AceEntityMention o : (ArrayList<AceEntityMention>) e.mentions) {
							String cleanText = o.text.replace('\n', ' ')
									.replace('"', ' ');
							// System.out.println(cleanText);
							uniqueNamesMen.add(cleanText);

							if(tfidfMentions.containsKey(cleanText))
							{
								tfidfMentions.get(cleanText).add(f.getAbsolutePath());
							}
							else
							{
								tfidfMentions.put(cleanText, new HashSet<String>());
								tfidfMentions.get(cleanText).add(f.getAbsolutePath());
							}
						}
					}

			}
			i++;
		}
		return uniqueNamesMen;
	}

	public static HashSet<String> getUniqueGoalEmails() {
		return new HashSet<String>();
	}

	public static void getAllUniqueNERNames() throws JAXBException {

		//HashSet<String> uniqueNamesMen = getNamesMenetioned();
		HashSet<String> uniqueNames = getUniqueNamesEmail();
		HashSet<String> uniqueGoalNames = getUniqueGoalEmails();
		PrintWriter stats = null;
		try {
			stats = new PrintWriter(new BufferedWriter(new FileWriter(
					"stats.txt")));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(String s:tfidfEmails.keySet())
		{
			System.out.println(s+" : "+ tfidfEmails.get(s).size());
		}

		/*stats.write("Unique Names " + uniqueNames.size() + "\n");
		for (String s : uniqueNames) {
			stats.write(s + "\n");
			System.out.println(s);
		}
		stats.write("Names Mentioned: " + uniqueNamesMen.size() + "\n");
		for (String s : uniqueNamesMen) {
			stats.write(s + "\n");
			System.out.println(s);
		}
		stats.write("Intersection: "
				+ Sets.intersection(uniqueNamesMen, uniqueNames).size() + "\n");
		System.out.println("-----------------------");
		for (String s : Sets.intersection(uniqueNamesMen, uniqueNames)) {
			System.out.println(s);
			stats.write(s + "\n");
		}
		stats.close();
		*/
	}

	// {email => { list of docs}}

	private static String APF_DIRECTORY = "C:\\Users\\reuben\\workspace\\NLPResearch\\NER";
	private static String SOURCE_DIRECTORY = "C:\\Users\\reuben\\workspace\\NLPResearch\\Out\\";

	public void createHistograms()
	{
	//	org.jfree.chart.
	}
	public static void main(String args[])
	{

		try {
			getAllUniqueNERNames();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
