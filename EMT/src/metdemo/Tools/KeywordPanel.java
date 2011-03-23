package metdemo.Tools;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;


public class KeywordPanel extends JPanel {

	private KeywordVector m_vKeywords;
	private JTextField m_textWord;
	private String m_wordListFilename;
	private JList m_listKeywords;
	private Hashtable m_hash;
	private Hashtable m_hash_lower;
	private JTextField fileLocation;
	private String m_comment;
	static final String bufferText = new String("####$$$$BUFFER V1$$$####");


	public KeywordPanel(String wordlistFile) {
		this(wordlistFile, false);
	}

	public KeywordPanel(String wordlistFile, boolean shouldMakeNew) {
		m_comment = new String();
		m_vKeywords = new KeywordVector();
		m_wordListFilename = wordlistFile;
		m_hash = new Hashtable();
		m_hash_lower = new Hashtable();

		readList(m_wordListFilename, shouldMakeNew);

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.NONE;
		constraints.weightx = 1.0;
		setLayout(gridbag);
		constraints.anchor = GridBagConstraints.WEST;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.insets = new Insets(4, 4, 4, 4);
		JLabel filePlace = new JLabel("File Location:");


		gridbag.setConstraints(filePlace, constraints);
		add(filePlace);

		fileLocation = new JTextField(25);
		constraints.gridx = 0;
		constraints.gridy = 1;
		constraints.gridwidth = 2;
		gridbag.setConstraints(fileLocation, constraints);
		add(fileLocation);
		fileLocation.setText(wordlistFile);
		fileLocation.setEditable(false);
		JButton findFile = new JButton("Browse for File");
		findFile.setToolTipText("Select file");
		findFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(m_wordListFilename);
				chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

				chooser.setFileHidingEnabled(false);
				chooser.setMultiSelectionEnabled(false);
				int returnVal = chooser.showOpenDialog(KeywordPanel.this);
				String newfile = null;
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					//File file = fc.getSelectedFile();
					newfile = chooser.getSelectedFile().getPath();

					if (readList(newfile, true)) {
						m_wordListFilename = newfile;
						fileLocation.setText(newfile);
					} else {
						System.out.println("problem in readlist " + newfile);
					}
					//This is where a real application would open the file.
					//log.append("Opening: " + file.getName() + "." + newline);
				} else {

					//log.append("Open command cancelled by user." + newline);
				}

			}
		});



		constraints.gridx = 0;
		constraints.gridy = 2;
		constraints.gridwidth = 1;
		gridbag.setConstraints(findFile, constraints);
		add(findFile);

		JButton setComment = new JButton("Set Note");
		setComment.setToolTipText("Attach a comment to the word list");
		setComment.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {


				JTextArea inputtxt = new JTextArea(5, 20);
				inputtxt.setLineWrap(true);
				inputtxt.setWrapStyleWord(true);
				inputtxt.setText(m_comment);

				JOptionPane.showMessageDialog(KeywordPanel.this, inputtxt, "Type Note on Keyword File Below",
						JOptionPane.PLAIN_MESSAGE);



				m_comment = inputtxt.getText();

			}
		});
		constraints.gridx = 1;
		constraints.gridy = 2;
		constraints.gridwidth = 1;
		gridbag.setConstraints(setComment, constraints);
		add(setComment);


		constraints.anchor = GridBagConstraints.CENTER;

		m_listKeywords = new JList((javax.swing.ListModel) m_vKeywords);

		m_listKeywords.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		//TODO: problem is that <html> is rendered and shows up as blank..have
		// fixed it temp by having it replace "<html> with something else..see
		// the subclass
		m_listKeywords.setCellRenderer(new EMTnoHTMLCellRenderer());

		JScrollPane jsp = new JScrollPane(m_listKeywords);
		jsp.setPreferredSize(new Dimension(150, 250));
		constraints.gridwidth = 1;
		constraints.gridx = 0;
		constraints.gridy = 3;
		constraints.insets = new Insets(20, 20, 20, 20);
		gridbag.setConstraints(jsp, constraints);
		add(jsp);

		JPanel rightside = new JPanel();
		GridBagLayout gbRight = new GridBagLayout();
		rightside.setLayout(gbRight);
		m_textWord = new JTextField(10);
		constraints.insets = new Insets(5, 0, 5, 0);

		constraints.gridy = 0;

		JButton showComment = new JButton("Show Note");
		showComment.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, m_comment, "Note for " + m_wordListFilename,
						JOptionPane.INFORMATION_MESSAGE);
			}
		});
		gbRight.setConstraints(showComment, constraints);
		rightside.add(showComment);

		constraints.gridy = 1;
		gbRight.setConstraints(m_textWord, constraints);
		rightside.add(m_textWord);

		JButton butAdd = new JButton("Add");
		butAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String word = m_textWord.getText().trim();

				m_textWord.setText("");

				addNewWord(word);
			}
		});
		constraints.gridy = 2;
		// top left bottom right
		gbRight.setConstraints(butAdd, constraints);
		rightside.add(butAdd);

		JButton butRemove = new JButton("Remove");
		butRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int i = m_listKeywords.getSelectedIndex();
				if (i >= 0) {
					String word = (String) m_vKeywords.elementAt(i);
					m_vKeywords.removeElementAt(i);
					m_hash.remove(word);
					m_hash_lower.remove(word);

				}

			}
		});
		constraints.gridy = 3;
		gbRight.setConstraints(butRemove, constraints);
		rightside.add(butRemove);

		constraints.insets = new Insets(0, 0, 0, 0);
		constraints.gridx = 1;
		constraints.gridy = 3;
		gridbag.setConstraints(rightside, constraints);
		add(rightside);

	}

	public final void addNewWord(final String word) {
		if ((word.length() > 0) && !m_vKeywords.contains(word)) {
			int i = 0;
			while ((i < m_vKeywords.size()) && (word.compareTo((String)m_vKeywords.getElementAt(i)) > 0)) {
				i++;
			}
			m_vKeywords.insertElementAt(word, i);
			m_hash.put(word, "");
			m_hash_lower.put(word.toLowerCase(), "");

		}




	}

	public void clear() {
		m_vKeywords.clear();
		setHash();
	}

	public final void saveInput() {
		writeOutFile();
	}

	public String[] getList() {
		String v[] = new String[m_vKeywords.size()];
		for (int i = 0; i < m_vKeywords.size(); i++) {
			v[i] = ((String) m_vKeywords.getElementAt(i));
		}
		return v;

	}

	public Hashtable getHash(boolean lower) {
		if (lower) {
			return m_hash_lower;
		}

		return m_hash;

	}

	public void setHash() {
		m_hash = new Hashtable();
		m_hash_lower = new Hashtable();

		String temp = new String();

		for (int i = 0; i < m_vKeywords.size(); i++) {
			temp = ((String) m_vKeywords.getElementAt(i)).trim();
			if (temp.length() > 0) {
				if (!m_hash.containsKey(temp))
					m_hash.put(temp, "");
				if (!m_hash_lower.containsKey(temp.toLowerCase()))
					m_hash_lower.put(temp, "");
			}
		}

	}

	public final String getFileName() {
		return m_wordListFilename;
	}


	public final void cancelChanges() {
		readList(m_wordListFilename, false);
	}

	private final boolean readList(String fileList, boolean createFile) {
		
		java.net.URL fileURL = null;
		
		
		
		try {
			//System.out.println("in readlist " + fileList);
			m_vKeywords.clear();
			//try to get file
			fileURL = ClassLoader.getSystemResource(fileList);
			
			//System.out.println("1"+fileURL.toExternalForm());
			//lets try relative file from emt directory
			if (fileURL == null) {
				fileURL = ClassLoader.getSystemResource(new File(System.getProperty("user.dir"), fileList).toString());
				//System.out.println("2"+fileURL);

			}
			//must be fill path file
			if (fileURL == null) {
				
				fileURL = new URL("file:///" + fileList);
				//System.out.println("3"+fileURL);

			}

			if (fileURL == null && createFile) {
				File newone = new File(fileList);
				if (!newone.exists()) {
					if (!newone.createNewFile()) {
						return false;
					}
					setHash();
					return true;
				}
			}

			//last check to make sure we get something
			if (fileURL == null) {
				return false;
			}
			
			//lets try to open:
			String realname = fileURL.getFile();
			//check for special characters in the realname
			if(realname.indexOf('%') > 0){
				realname = fileList;
			}
			
			
			
			BufferedReader breader = new BufferedReader(new FileReader(realname));
			String word = new String("");

			if ((word = breader.readLine()) != null) {
				if (word.equals(bufferText)) {
					//while we dont see an end comment
					while ((word = breader.readLine()) != null) {

						if (word.equals(bufferText)) {
							break;
						}
						m_comment += word;
					}

				}
				if (!word.equals(bufferText)) {
					m_vKeywords.add(word.trim());
				}
			}

			while ((word = breader.readLine()) != null) {
				m_vKeywords.add(word.trim());
			}
			setHash();
			breader.close();
		} catch (IOException ex) {
			//thrown when trying to read non existant file
			if (createFile) {
				File newone = null;
				try {
					newone = new File(fileURL.getPath(), fileURL.getFile());
					//will throw exception if relative path
					if (!newone.exists()) {
						if (!newone.createNewFile()) {
							return false;
						}
						setHash();
						return true;
					}
				} catch (IOException e) {
					try {
						//this means we need to look relative to path
						newone = new File(System.getProperty("user.dir"), fileURL.getFile());
						if (!newone.exists()) {
							if (!newone.createNewFile()) {
								return false;
							}
							setHash();
							return true;
						}
					} catch (IOException e4) {

					}
					System.out.println("2" + e);
				}
			} else {
				JOptionPane.showMessageDialog(null, "Tried to load: " + fileList + " ..Failed to read in word list: "
						+ ex);
			}
			return false;
		}
		return true;
	}

	private void writeOutFile() {
		try {
			// backup file first in case we get nuked while writing
			CopyFile(new File(m_wordListFilename), new File(m_wordListFilename + ".bak"));

			File file = new File(m_wordListFilename);
			PrintWriter writer = new PrintWriter(new FileOutputStream(file, false));

			//write out the comment
			writer.println(bufferText);
			writer.println(m_comment);
			writer.println(bufferText);

			for (int i = 0; i < m_vKeywords.size(); i++) {
				writer.println((String) m_vKeywords.getElementAt(i));
			}
			writer.flush();
			writer.close();
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Couldn't commit changes to list: " + ex);
			cancelChanges();
		}
	}

	private void CopyFile(File in, File out) throws FileNotFoundException, IOException {
		FileInputStream inStream = new FileInputStream(in);
		FileOutputStream outStream = new FileOutputStream(out);
		byte[] buffer = new byte[1024];
		//	long fileLen = in.length();

		int amtRead;
		while ((amtRead = inStream.read(buffer)) != -1) {
			outStream.write(buffer, 0, amtRead);
		}

		inStream.close();
		outStream.close();
	}

	private class KeywordVector extends Vector implements javax.swing.ListModel {
		private Vector m_vListeners;

		public KeywordVector() {
			m_vListeners = new Vector();
		}

		public void addListDataListener(ListDataListener l) {
			m_vListeners.add(l);
		}

		public void removeListDataListener(ListDataListener l) {
			m_vListeners.remove(l);
		}

		public int getSize() {
			return size();
		}

		public Object getElementAt(int index) {
			return get(index);
		}

		public boolean add(Object o) {
			boolean ret = super.add(o);

			ListDataEvent evt = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, getSize() - 1, getSize() - 1);
			for (int i = 0; i < m_vListeners.size(); i++) {
				((ListDataListener) m_vListeners.get(i)).intervalAdded(evt);
			}

			return ret;
		}

		public void insertElementAt(Object obj, int index) {
			super.insertElementAt(obj, index);

			ListDataEvent evt = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, index, getSize() - 1);
			for (int i = 0; i < m_vListeners.size(); i++) {
				((ListDataListener) m_vListeners.get(i)).contentsChanged(evt);
			}
		}

		public void removeElementAt(int index) {
			super.removeElementAt(index);

			ListDataEvent evt = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, index, index);
			for (int i = 0; i < m_vListeners.size(); i++) {
				((ListDataListener) m_vListeners.get(i)).intervalRemoved(evt);
			}
		}

		public boolean contains(String s) {
			for (int i = 0; i < getSize(); i++) {
				if (((String) getElementAt(i)).equals(s)) {
					return true;
				}
			}
			return false;
		}
	}
}