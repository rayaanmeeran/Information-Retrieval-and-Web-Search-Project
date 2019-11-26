/*
 * CPS 842 Assignment 1
 * Rayaan Meeran 500749720 (Section 02)
 * John Gomes 500754885 (Section 01)
 * */

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import javax.swing.*;

public class Invert {

	/* Global GUI components */
	private static JFrame frame;
	private static JButton runButton;
	private static JButton cancelButton;
	private static JRadioButton useStopWordsRadioButton;
	private static JRadioButton useStemmerRadioButton;
	private static JTextField stopWordsPathTextField;
	private static JTextField documentPathTextField;
	private static JLabel stopWordsPathLabel;
	private static JLabel documentsPathLabel;

	static HashMap<String, Integer> dictionary;
	static HashMap<String, String> posting;
	static boolean useStopWords;
	static boolean usePorterStemming;
	static String stopWordsPath;
	static String documentsPath;
	
	static HashMap<Integer, String> citations;

	public static void main(String[] args) throws Exception {
		/* Create GUI to run inverting */
		createGUI();
	}

	/**
	 * Run main inverting program
	 * 
	 * @throws IOException
	 */
	public static void runInvert() throws IOException {

		dictionary = new HashMap<String, Integer>();
		posting = new HashMap<String, String>();
		
		citations = new HashMap<Integer, String>();

		if (useStopWordsRadioButton.isSelected() == true)
			useStopWords = true;
		else
			useStopWords = false;

		if (useStemmerRadioButton.isSelected() == true)
			usePorterStemming = true;
		else
			usePorterStemming = false;

		/* Read Stop Words file */
		stopWordsPath = stopWordsPathTextField.getText();
		File stopWordsFile = new File(stopWordsPath);

		ArrayList<String> stopWords = readStopWords(stopWordsFile);

		/* Read documents file */
		documentsPath = documentPathTextField.getText();
		File documentsFile = new File(documentsPath);
		BufferedReader br = new BufferedReader(new FileReader(documentsFile));

		String line = br.readLine();
		while (!(line == null)) {

			int docNum = Integer.parseInt(line.substring(3));

			/* Gets title of document */
			String title = "";
			line = br.readLine();
			if (line.equals(".T")) {
				line = br.readLine();
				title = title + line;
				while (true) {
					line = br.readLine();
					if (line.equals(".B") || line.equals(".W"))
						break;
					title = title + " " + line;
				}
			}

			/* Gets abstract of document */
			String abs = "";
			if (line.equals(".W")) {
				line = br.readLine();
				abs = abs + line;
				while (true) {
					line = br.readLine();
					if (line.equals(".B"))
						break;
					abs = abs + " " + line;
				}
			}

			String[] titleTerms = title.split("[\\p{Punct}\\s]+");
			String[] absTerms = abs.split("[\\p{Punct}\\s]+");

			putIntoDictionaryAndPosting(docNum, titleTerms, absTerms, usePorterStemming);
			
			
			while(!line.equals(".X")) {
				if (line == null || line.substring(0, 2).equals(".I"))
					break;
				line = br.readLine();
			}
			
			if (line.equals(".X")) {
				while (true) {
					line = br.readLine();
					if (line == null || line.substring(0, 2).equals(".I"))
						break;
					
					String[] citeLine = line.split("[\\p{Punct}\\s]+");
					if (Integer.parseInt(citeLine[1]) == 5) {
						if (!(Integer.parseInt(citeLine[0]) == Integer.parseInt(citeLine[2]))) {
							if (citations.containsKey(docNum)) {
								String temp = citations.get(docNum);
								citations.put(docNum, temp + "," + line);
							}
							else
								citations.put(docNum, line);
						}
					}
				}
			}
			//System.out.println(docNum + ": " + citations.get(docNum));
		}
		
		PageRank pagerank = new PageRank(citations);

		/* If useStopWords = true then remove all stop words from the dictionary */
		if (useStopWords) {
			for (String word : stopWords) {
				dictionary.remove(word);
				posting.remove(word);
			}
		}
		dictionary.remove("");
		posting.remove("");

		/* Puts the HashMap into a treemap so it can be sorted alphabetically */
		TreeMap<String, Integer> sortedDictionary = new TreeMap<>(dictionary);
		TreeMap<String, String> sortedPosting = new TreeMap<>(posting);

		/* Writing the dictionary to a file called dictionary.txt */
		File writeDictionaryFile = new File("dictionary.txt");
		BufferedWriter writer = new BufferedWriter(new FileWriter(writeDictionaryFile));

		Set<Entry<String, Integer>> mappings = sortedDictionary.entrySet();
		for (Entry<String, Integer> mapping : mappings) {
			String dictionaryLine = mapping.getKey() + ":" + mapping.getValue() + "\n";
			writer.write(dictionaryLine);
		}
		writer.close();

		/* Writing the posting to a file called posting.txt */
		File writePostingFile = new File("posting.txt");
		writer = new BufferedWriter(new FileWriter(writePostingFile));

		Set<Entry<String, String>> mappings2 = sortedPosting.entrySet();
		for (Entry<String, String> mapping : mappings2) {
			String postingLine = mapping.getKey() + ":" + mapping.getValue() + "\n";
			writer.write(postingLine);
		}
		writer.close();

		/* Check if the path is correct */
		System.out.printf("\nDictionary file is located at %s%n", writeDictionaryFile.getAbsolutePath());
		System.out.printf("\nPosting file is located at %s%n", writePostingFile.getAbsolutePath());
	}

	/**
	 * Reads stop words from provided file and returns it as an ArrayList
	 * 
	 * @param stopWordsFile
	 * @return stop words ArrayList
	 */
	public static ArrayList<String> readStopWords(File stopWordsFile) {
		Scanner stopWordsScan;
		try {
			stopWordsScan = new Scanner(stopWordsFile);
			ArrayList<String> stopWords = new ArrayList<String>();
			while (stopWordsScan.hasNext())
				stopWords.add(stopWordsScan.next());
			stopWordsScan.close();
			// System.out.println(Arrays.toString(stopWords.toArray()));
			return stopWords;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Method to add terms to dictionary or posting HashMap
	 * 
	 * @param docNum
	 * @param titleTerms
	 * @param absTerms
	 * @param usePorterStemming
	 */
	public static void putIntoDictionaryAndPosting(int docNum, String[] titleTerms, String[] absTerms,
			boolean usePorterStemming) {
		HashMap<String, Integer> docFreq = new HashMap<String, Integer>();
		HashMap<String, String> docPos = new HashMap<String, String>();

		int position = 1;
		List<String> words = new ArrayList<String>();

		/* Takes every term from title */
		for (String s : titleTerms) {
			s = s.toLowerCase();

			/* Porter Stemming algorithm using Stemmer.java */
			if (usePorterStemming) {
				Stemmer stem = new Stemmer();
				for (int i = 0; i < s.length(); i++) {
					stem.add(s.charAt(i));
				}
				stem.stem();
				s = stem.toString();
			}

			/* Adding to dictionary */
			if (words.contains(s)) {
			} else if (dictionary.containsKey(s)) {
				int i = dictionary.get(s);
				dictionary.put(s, i + 1);
				words.add(s);
			} else {
				dictionary.put(s, 1);
				words.add(s);
			}

			/* Adding to temporary posting */
			if (docFreq.containsKey(s)) {
				int i = docFreq.get(s);
				docFreq.put(s, i + 1);
				String temp = docPos.get(s);
				docPos.put(s, temp + "," + Integer.toString(position));
			} else {
				docFreq.put(s, 1);
				docPos.put(s, Integer.toString(position));
			}
			position++;
		}

		/* Takes every term from title */
		for (String s : absTerms) {
			s = s.toLowerCase();

			/* Porter Stemming algorithm using Stemmer.java */
			if (usePorterStemming) {
				Stemmer stem = new Stemmer();
				for (int i = 0; i < s.length(); i++) {
					stem.add(s.charAt(i));
				}
				stem.stem();
				s = stem.toString();
			}

			/* Adding to dictionary */
			if (words.contains(s)) {
			} else if (dictionary.containsKey(s)) {
				int i = dictionary.get(s);
				dictionary.put(s, i + 1);
				words.add(s);
			} else {
				dictionary.put(s, 1);
				words.add(s);
			}

			/* Adding to temporary posting */
			if (docFreq.containsKey(s)) {
				int i = docFreq.get(s);
				docFreq.put(s, i + 1);
				String temp = docPos.get(s);
				docPos.put(s, temp + "," + Integer.toString(position));
			} else {
				docFreq.put(s, 1);
				docPos.put(s, Integer.toString(position));
			}
			position++;
		}

		/* Adding to posting */
		Iterator iterator = docPos.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry mapElement = (Map.Entry) iterator.next();
			if (posting.containsKey(mapElement.getKey())) {
				String s = posting.get(mapElement.getKey());
				posting.put(mapElement.getKey().toString(), s + " " + Integer.toString(docNum) + "|"
						+ Integer.toString(docFreq.get(mapElement.getKey())) + "|" + mapElement.getValue());
			} else
				posting.put(mapElement.getKey().toString(), Integer.toString(docNum) + "|"
						+ Integer.toString(docFreq.get(mapElement.getKey())) + "|" + mapElement.getValue());
		}
	}

	/**
	 * Creates GUI to run program
	 */
	public static void createGUI() {

		/* Creating instance of JFrame */
		frame = new JFrame();

		/* Creating instances of GUI components */
		useStopWordsRadioButton = new JRadioButton("Use Stop Words");
		useStemmerRadioButton = new JRadioButton("Use Stemmer");
		stopWordsPathTextField = new JTextField("common_words");
		documentPathTextField = new JTextField("cacm.all");
		stopWordsPathLabel = new JLabel("Stop Words Path");
		documentsPathLabel = new JLabel("Documents Path");
		runButton = new JButton("Run");

		/* x axis, y axis, width, height */
		useStopWordsRadioButton.setBounds(80, 50, 250, 40);
		useStemmerRadioButton.setBounds(80, 100, 250, 40);
		stopWordsPathLabel.setBounds(80, 150, 250, 40);
		stopWordsPathTextField.setBounds(80, 200, 250, 40);
		documentsPathLabel.setBounds(80, 250, 250, 40);
		documentPathTextField.setBounds(80, 300, 250, 40);
		runButton.setBounds(80, 350, 250, 40);

		/* Event Listeners */
		runButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					runInvert();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});

		/* Add components to JFrame */
		frame.add(useStopWordsRadioButton);
		frame.add(useStemmerRadioButton);
		frame.add(stopWordsPathLabel);
		frame.add(stopWordsPathTextField);
		frame.add(documentsPathLabel);
		frame.add(documentPathTextField);
		frame.add(runButton);

		/* Frame size */
		frame.setSize(500, 500);
		frame.setLayout(null);
		/* Make the frame visible */
		frame.setVisible(true);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}
}
