// -*- tab-width: 4 -*-
package Jet.Format;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Jet.Lisp.FeatureSet;
import Jet.Parser.ParseTreeNode;
import Jet.Tipster.Annotation;
import Jet.Tipster.Document;
import Jet.Tipster.Span;
import Jet.Util.IOUtils;

/**
 * Penn Treebank Parser. The parser reads Penn Treebank corpus from Reader and
 * build Jet.Tipster.Document from parse tree.
 * 
 * @author Akira ODA
 */
public class PTBReader {
	static Pattern tagNamePattern = Pattern.compile(
			"([^-=]+) (?: - ([\\-a-zA-Z]+)*)? (?: [-=] ([\\-\\d]+))?", Pattern.COMMENTS);

	static Pattern specialTagNamePattern = Pattern.compile("-.*-");

	private static final Map<String, String> TRANSFORM_TABLE;

	private static final Set<String> PUNCTUATIONS;

	private static final Set<String> NO_FOLLOWING_SPACE;

	private static final Set<String> DELETE_PREVIOUS_SPACE;

	/**
	 * If true, backslashes are treated as escape character.
	 */
	private boolean backslashAsEscapeChar = true;

	/**
	 * If true, add tokens when read corpus.
	 */
	private boolean isAddingTokens = false;

	static {
		TRANSFORM_TABLE = new HashMap<String, String>();
		TRANSFORM_TABLE.put("-LRB-", "(");
		TRANSFORM_TABLE.put("-LCB-", "{");
		TRANSFORM_TABLE.put("-LSB-", "[");

		TRANSFORM_TABLE.put("-RRB-", ")");
		TRANSFORM_TABLE.put("-RCB-", "}");
		TRANSFORM_TABLE.put("-RSB-", "]");

		PUNCTUATIONS = new HashSet<String>();
		PUNCTUATIONS.add(".");
		PUNCTUATIONS.add(",");
		PUNCTUATIONS.add("?");
		PUNCTUATIONS.add("!");

		NO_FOLLOWING_SPACE = new HashSet<String>();
		NO_FOLLOWING_SPACE.add("(");
		NO_FOLLOWING_SPACE.add("{");
		NO_FOLLOWING_SPACE.add("[");

		DELETE_PREVIOUS_SPACE = new HashSet<String>();
		DELETE_PREVIOUS_SPACE.add(")");
		DELETE_PREVIOUS_SPACE.add("}");
		DELETE_PREVIOUS_SPACE.add("]");
		DELETE_PREVIOUS_SPACE.add(".");
		DELETE_PREVIOUS_SPACE.add(",");
	}

	/**
	 * Determines annotate span for each node in parse tree, and adds
	 * annotations to document.
	 * 
	 * @param tree
	 * @param doc
	 */
	public void addAnnotations(ParseTreeNode tree, Document doc, Span span) {
		List<ParseTreeNode> terminalNodes = getTerminalNodes(tree);
		String text = doc.text();
		int offset = span.start();

		for (ParseTreeNode terminal : terminalNodes) {
			while (offset < span.end() && Character.isWhitespace(text.charAt(offset))) {
				offset++;
			}

			if (text.substring(offset).startsWith(terminal.word)) {
				int endOffset = offset + terminal.word.length();
				while (endOffset < span.end() && Character.isWhitespace(text.charAt(endOffset))) {
					endOffset++;
				}
				terminal.start = offset;
				terminal.end = endOffset;
				offset = endOffset;
			} else {
				throw new RuntimeException("cannot determine parse tree offset");
			}
		}

		determineNonTerminalSpans(tree, span.start());
		setAnnotations(tree, doc);
	}

	/**
	 * Determines annotate span for each node in parse tree, and adds
	 * annotations to document.
	 * 
	 * @param trees
	 *            list of parse tree
	 * @param doc
	 *            document to be added annotation
	 * @param targetAnnotation
	 *            name of annotation to determine spans to add parse tree
	 *            annotations.
	 * @param span
	 *            target span.
	 */
	public void addAnnotations(List<ParseTreeNode> trees, Document doc, String targetAnnotation,
			Span span) {
		List<Annotation> targetList = (List<Annotation>) doc.annotationsOfType(targetAnnotation,
				span);
		Comparator<Annotation> cmp = new Comparator<Annotation>() {
			public int compare(Annotation a, Annotation b) {
				return a.span().compareTo(b.span());
			}
		};

		Collections.sort(targetList, cmp);
		for (int i = 0; i < targetList.size(); i++) {
			addAnnotations(trees.get(i), doc, targetList.get(i).span());
		}
	}

	/**
	 * Loads parse tree corpus from Penn Treebank corpus.
	 * 
	 * This method loads parse tree, but not determine annotation span and not
	 * set annotation.
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 * @throws InvalidFormatException
	 */
	public List<ParseTreeNode> loadParseTrees(Reader in) throws IOException, InvalidFormatException {
		List<ParseTreeNode> list = new ArrayList<ParseTreeNode>();
		PushbackReader input = new PushbackReader(in);

		while (true) {
			skipWhitespace(input);
			if (lookAhead(input) == -1) {
				break;
			}

			ParseTreeNode node = readNode(input);
			list.add(node);
		}

		return list;
	}

	/**
	 * Builds Jet.Tipster.Document object from Penn treebank corpus.
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 * @throws InvalidFormatException
	 */
	public Treebank load(Reader in) throws IOException, InvalidFormatException {

		List<ParseTreeNode> trees = new ArrayList<ParseTreeNode>();
		PushbackReader input = new PushbackReader(in);

		int start = 0;
		while (true) {
			skipWhitespace(input);
			if (lookAhead(input) == -1) {
				break;
			}

			ParseTreeNode tree = readNode(input);
			trees.add(tree);
			determineSpans(tree, start);
			setAnnotations(tree, null);
			start = tree.end;
		}

		String text = buildDocumentString(trees);
		Document doc = new Document(text);
		for (ParseTreeNode tree : trees) {
			doc.annotate("sentence", new Span(tree.start, tree.end), new FeatureSet());
			annotate(doc, tree);
		}

		return new Treebank(doc, trees);
	}

	/**
	 * Builds Document object from Penn treebank corpus.
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 * @throws InvalidFormatException
	 */
	public Treebank load(File file) throws IOException, InvalidFormatException {
		Reader in = null;
		try {
			in = new BufferedReader(new FileReader(file));
			return load(in);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	/**
	 * Builds Document object from Penn treebank corpus.
	 * 
	 * @param file
	 * @param encoding
	 * @return
	 * @throws IOException
	 * @throws InvalidFormatException
	 */
	public Treebank load(File file, String encoding) throws IOException, InvalidFormatException {
		InputStream fin = null;
		Reader in = null;

		try {
			fin = new FileInputStream(file);
			in = new InputStreamReader(fin, encoding);
			in = new BufferedReader(in);

			return load(in);
		} finally {
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(fin);
		}
	}

	/**
	 * Sets a backslash is treated as escape character or not.
	 * 
	 * @param b
	 */
	public void setBackslashAsEscapeCharacter(boolean b) {
		this.backslashAsEscapeChar = b;
	}

	/**
	 * Sets a adding tokens automatically or not.
	 * 
	 * @param b
	 */
	public void setAddingToken(boolean b) {
		this.isAddingTokens = b;
	}

	/**
	 * Returns if node is null element.
	 */
	private static boolean isNullNode(ParseTreeNode node) {
		return node.category.equals("-none-");
	}

	/**
	 * Remove last whitespace character and modify annotation span.
	 * 
	 * @param annotations
	 * @param buffer
	 */
	private void modifyAnnotationEnd(List<Annotation> annotations, StringBuilder buffer) {
		ListIterator<Annotation> it = annotations.listIterator(annotations.size());

		if (buffer.length() == 0) {
			return;
		}

		if (!Character.isWhitespace(buffer.charAt(buffer.length() - 1))) {
			return;
		}

		while (it.hasPrevious()) {
			Annotation a = it.previous();
			if (a.end() != buffer.length()) {
				break;
			}

			Span span = new Span(a.start(), a.end() - 1);
			Annotation replacement = new Annotation(a.type(), span, a.attributes());
			it.set(replacement);
		}

		buffer.deleteCharAt(buffer.length() - 1);
	}

	/**
	 * Reads one node from a stream.
	 * 
	 * @param in
	 * @return readed node
	 * @throws IOException
	 * @throws InvalidFormatException
	 */
	private ParseTreeNode readNode(PushbackReader in) throws IOException, InvalidFormatException {
		int c = in.read();

		if (c != '(') {
			throw new InvalidFormatException();
		}

		if ((c = lookAhead(in)) == -1) {
			throw new InvalidFormatException();
		}

		if (Character.isWhitespace(c) || c == '(') {
			skipWhitespace(in);
			ParseTreeNode node = readNode(in);
			skipWhitespace(in);
			c = (char) in.read();
			if (c != ')') {
				throw new InvalidFormatException();
			}
			return node;
		}

		String tag = readTagName(in);
		String function = null;
		Matcher m = tagNamePattern.matcher(tag);
		if (m.matches()) {
			tag = m.group(1);
			function = m.group(2);
		} else if (!specialTagNamePattern.matcher(tag).matches()) {
			throw new InvalidFormatException(tag + " is invalid format.");
		}

		if (skipWhitespace(in) == 0) {
			return null;
		}

		ParseTreeNode node;

		if (lookAhead(in) == '(') {
			// has any child node (not terminal node)
			List<ParseTreeNode> children = new ArrayList<ParseTreeNode>();
			do {
				ParseTreeNode child = readNode(in);
				if (!isNullNode(child)) {
					children.add(child);
				}
				skipWhitespace(in);
			} while (lookAhead(in) != ')');

			node = new ParseTreeNode(tag, children.toArray(new ParseTreeNode[0]), 0, 0, 0, function);
		} else {
			// terminal node
			String word = readWord(in);
			node = new ParseTreeNode(tag, null, 0, 0, null, word, function);
		}

		skipWhitespace(in);
		if (in.read() != ')') {
			throw new InvalidFormatException();
		}

		return node;
	}

	/**
	 * skip whitespace characters
	 * 
	 * @param in
	 * @return count of skipped characters.
	 * @throws IOException
	 */
	private int skipWhitespace(PushbackReader in) throws IOException {
		int count = 0;
		int c;
		do {
			c = in.read();
			count++;
		} while (Character.isWhitespace(c) && c != -1);

		if (c != -1) {
			in.unread(c);
		}

		return count - 1;
	}

	/**
	 * Reads a tag name which is after opened parenthesis.
	 * 
	 * @param in
	 * @return readed token string
	 * @throws IOException
	 * @throws InvalidFormatException
	 */
	private String readTagName(PushbackReader in) throws IOException, InvalidFormatException {
		StringBuilder buffer = new StringBuilder();
		int c;

		while (true) {
			c = in.read();
			if (c == -1) {
				throw new InvalidFormatException();
			} else if (Character.isWhitespace(c)) {
				break;
			}

			buffer.append((char) c);
		}

		in.unread(c);

		if (buffer.length() == 0) {
			throw new InvalidFormatException();
		}

		return buffer.toString().toLowerCase();
	}

	/**
	 * Reads annotated token.
	 * 
	 * @param in
	 * @return readed token.
	 * @throws IOException
	 * @throws InvalidFormatException
	 */
	private String readWord(PushbackReader in) throws IOException, InvalidFormatException {
		int c;
		StringBuilder buffer = new StringBuilder();
		while (true) {
			c = in.read();

			if (c != -1 && backslashAsEscapeChar && c == '\\') {
				c = in.read();
			}

			if (c == ')') {
				break;
			} else if (c == -1) {
				throw new InvalidFormatException();
			}

			buffer.append((char) c);
		}

		in.unread(c);

		String word = buffer.toString();
		if (TRANSFORM_TABLE.containsKey(word)) {
			word = TRANSFORM_TABLE.get(word);
		}
		return word;
	}

	/**
	 * Look ahead next character.
	 * 
	 * @param in
	 * @return readed character
	 * @throws IOException
	 */
	private int lookAhead(PushbackReader in) throws IOException {
		int c = in.read();
		if (c != -1) {
			in.unread(c);
		}
		return c;
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.out.println("usage: java " + PTBReader.class.getName() + " ");
			System.exit(1);
		}

		File inputDir = new File(args[0]);
		File outputDir = new File(args[1]);
		PTBReader parser = new PTBReader();
		for (File file : getFiles(new File(args[0]), ".mrg")) {
			String outFilename = removeSuffix(getRelativePath(inputDir, file));
			File outFile = new File(outputDir, outFilename);
			outFile.getParentFile().mkdirs();

			Writer out = new FileWriter(outFile);
			Document doc = parser.load(file).getDocument();
			out.write(doc.text());
			out.close();
		}
	}

	private static List<File> getFiles(File dir, String suffix) throws IOException {
		List<File> list = new ArrayList<File>();

		for (File file : dir.listFiles()) {
			if (file.isFile() && file.getName().endsWith(suffix)) {
				list.add(file);
			} else if (file.isDirectory()) {
				list.addAll(getFiles(file, suffix));
			}
		}

		return list;
	}

	private static String getRelativePath(File base, File file) {
		return file.getAbsolutePath().substring(base.getAbsolutePath().length());
	}

	private static String removeSuffix(String filename) {
		int index = filename.lastIndexOf('.');
		if (index >= 0) {
			return filename.substring(0, index);
		} else {
			return filename;
		}
	}

	private String buildDocumentString(List<ParseTreeNode> trees) {
		StringBuilder buffer = new StringBuilder();

		for (ParseTreeNode tree : trees) {
			List<ParseTreeNode> terminals = getTerminalNodes(tree);
			for (ParseTreeNode terminal : terminals) {
				if (terminal.word != null) {
					buffer.append(terminal.word);
					while (buffer.length() < terminal.end) {
						buffer.append(' ');
					}
				}
			}

			// set last character to newline
			if (buffer.charAt(buffer.length() - 1) == ' ') {
				buffer.setCharAt(buffer.length() - 1, '\n');
			}
		}

		return buffer.toString();
	}

	private void determineSpans(ParseTreeNode tree, int offset) {
		List<ParseTreeNode> terminals = getTerminalNodes(tree);
		determineTerminalSpans(terminals, offset);
		determineNonTerminalSpans(tree, offset);
	}

	private void determineTerminalSpans(List<ParseTreeNode> terminals, int offset) {
		int start = offset;
		int n = terminals.size();

		for (int i = 0; i < n; i++) {
			ParseTreeNode current = terminals.get(i);
			ParseTreeNode prev = i > 0 ? terminals.get(i - 1) : null;

			String word = current.word;
			int end = start + (word != null ? word.length() + 1 : 0);
			if (!hasAfterSpace(word)) {
				end--;
			}
			if (hasBeforeSpace(word) && prev != null) {
				if (hasAfterSpace(prev.word)) {
					prev.end--;
					start--;
					end--;
				}
			}

			current.start = start;
			current.end = end;
			start = end;
		}
	}

	private int determineNonTerminalSpans(ParseTreeNode tree, int offset) {
		if (isTerminalNode(tree)) {
			return tree.end;
		} else {

			ParseTreeNode[] children = tree.children;
			if (children.length > 0) {
				for (ParseTreeNode child : children) {
					offset = determineNonTerminalSpans(child, offset);
				}

				tree.start = children[0].start;
				tree.end = children[children.length - 1].end;
			} else {
				tree.start = offset;
				tree.end = offset;
			}

			return tree.end;
		}
	}

	private boolean hasAfterSpace(String word) {
		if (NO_FOLLOWING_SPACE.contains(word)) {
			return false;
		} else {
			return true;
		}
	}

	private boolean hasBeforeSpace(String word) {
		if (DELETE_PREVIOUS_SPACE.contains(word)) {
			return true;
		} else if (isPartOfShortenedForm(word)) {
			return true;
		} else {
			return false;
		}
	}

	private boolean isPartOfShortenedForm(String word) {
		if (word != null) {
			return word.startsWith("'") || word.equals("n't");
		} else {
			return false;
		}
	}

	private void annotate(Document doc, ParseTreeNode node) {
		doc.addAnnotation(node.ann);
		if (node.children != null) {
			Annotation[] children = new Annotation[node.children.length];
			for (int i = 0; i < node.children.length; i++) {
				children[i] = node.children[i].ann;
			}
			node.ann.put("children", children);
			
			for (ParseTreeNode child : node.children) {
				annotate(doc, child);
			}
		}
		
		if (node.children == null && isAddingTokens) {
			// TODO: adds `case' property
			doc.annotate("token", node.ann.span(), new FeatureSet());
		}
	}

	/**
	 * Returns termninal node list in the parse tree.
	 * 
	 * @param tree
	 * @return
	 */
	private List<ParseTreeNode> getTerminalNodes(ParseTreeNode tree) {
		if (tree.children == null || tree.children.length == 0) {
			// terminal node
			if (tree.word != null) {
				return Collections.singletonList(tree);
			}
			return Collections.emptyList();
		} else {
			List<ParseTreeNode> list = new ArrayList<ParseTreeNode>();
			// non terminal node
			for (ParseTreeNode child : tree.children) {
				list.addAll(getTerminalNodes(child));
			}
			return list;
		}
	}

	/**
	 * Returns if node is terminal node.
	 * 
	 * @param node
	 * @return
	 */
	private boolean isTerminalNode(ParseTreeNode node) {
		return node.children == null;
	}

	/**
	 * Sets annotations for each node.
	 * 
	 * @param node
	 * @param doc
	 */
	private void setAnnotations(ParseTreeNode node, Document doc) {
		Span span = new Span(node.start, node.end);
		FeatureSet attrs = new FeatureSet();
		attrs.put("cat", node.category);
		if (node.head != 0) {
			attrs.put("head", node.head);
		}
		if (node.function != null) {
			attrs.put("func", node.function);
		}

		node.ann = new Annotation("constit", span, attrs);
		if (doc != null) {
			doc.addAnnotation(node.ann);
		}

		if (node.children != null) {
			for (ParseTreeNode child : node.children) {
				setAnnotations(child, doc);
			}
		}
	}
}
