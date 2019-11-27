/*
 * CPS 842 Project
 * Rayaan Meeran 500749720 (Section 02)
 * John Gomes 500754885 (Section 01)
 * */

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

public class GUI {

	/* Global GUI components */
	private static JFrame frame;
	private static JLabel dictionaryPathLabel;
	private static JTextField dictionaryPathTextField;
	private static JLabel postingsPathLabel;
	private static JTextField postingsPathTextField;
	private static JLabel documentsPathLabel;
	private static JTextField documentPathTextField;
	private static JLabel stopWordsPathLabel;
	private static JTextField stopWordsPathTextField;
	private static JLabel pagerankPathLabel;
	private static JTextField pagerankPathTextField;
	private static JLabel w1Label;
	private static JTextField w1TextField;
	private static JLabel w2Label;
	private static JTextField w2TextField;
	private static JLabel enterQueryLabel;
	private static JTextField queryTextField;
	private static JButton searchButton;

	private static HashMap<String, Integer> dictionary;
	private static HashMap<String, String> posting;
	private static LinkedHashMap<Integer, Double> similarities;

	private static double w1;
	private static double w2;

	/**
	 * Main: create GUI
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		createGUI();
	}

	/**
	 * Run the search for the query and output the similarities and doc files
	 * 
	 * @throws Exception
	 */
	public static void runSearch() throws Exception {
		boolean isDouble = true;
		try { // Error checking for correct values of w1 and w2. If error set both to 0.5
			double value1 = Double.parseDouble(w1TextField.getText());
			double value2 = Double.parseDouble(w1TextField.getText());
		} catch (NumberFormatException e) {
			isDouble = false;
		}
		if (isDouble) {
			if ((Double.parseDouble(w1TextField.getText()) > 1.0 || Double.parseDouble(w1TextField.getText()) < 0.0)
					&& (Double.parseDouble(w2TextField.getText()) > 1.0
							|| Double.parseDouble(w2TextField.getText()) < 0.0)) {
				w1 = 0.5;
				w2 = 0.5;
			} else {
				w1 = Double.parseDouble(w1TextField.getText());
				w2 = Double.parseDouble(w2TextField.getText());
				if (w1 + w2 != 1.0) {
					w1 = 0.5;
					w2 = 0.5;
				}
			}
		} else {
			w1 = 0.5;
			w2 = 0.5;
		}

		String query = queryTextField.getText();

		dictionary = new HashMap<String, Integer>();
		posting = new HashMap<String, String>();

		/* Read dictionary and postings files */
		readInputFile(dictionaryPathTextField.getText());
		readInputFile(postingsPathTextField.getText());

		Search search = new Search(dictionary, posting, query, stopWordsPathTextField.getText(),
				pagerankPathTextField.getText(), w1, w2);
		similarities = search.getSimilarities();
		String documentsPath = documentPathTextField.getText();
		File documentsFile = new File(documentsPath);

		getTitleAndAuthor(similarities, documentsFile);

	}

	/**
	 * Method to read the input files for the test
	 * 
	 * @param filename
	 * @throws Exception
	 */
	public static void readInputFile(String filename) throws Exception {
		File file = new File(filename);
		BufferedReader br = new BufferedReader(new FileReader(file));

		String line = br.readLine();
		while (!(line == null)) {
			String[] temp = line.split(":");
			if (filename.contains("dictionary.txt")) {
				dictionary.put(temp[0], Integer.parseInt(temp[1]));
			} else {
				posting.put(temp[0], temp[1]);
			}
			line = br.readLine();
		}
	}

	/**
	 * Create the GUI
	 */
	public static void createGUI() {

		/* Creating instance of JFrame */
		frame = new JFrame();

		/* Creating instances of GUI components */
		dictionaryPathLabel = new JLabel("Dictionary Path");
		dictionaryPathTextField = new JTextField("dictionary.txt");
		postingsPathLabel = new JLabel("Postings Path");
		postingsPathTextField = new JTextField("posting.txt");
		documentsPathLabel = new JLabel("Documents Path");
		documentPathTextField = new JTextField("cacm.all");
		stopWordsPathLabel = new JLabel("Stop Words Path");
		stopWordsPathTextField = new JTextField("common_words");
		pagerankPathLabel = new JLabel("PageRank File Path");
		pagerankPathTextField = new JTextField("pagerank.txt");
		w1Label = new JLabel("W1");
		w1TextField = new JTextField("0.5");
		w2Label = new JLabel("W2");
		w2TextField = new JTextField("0.5");
		enterQueryLabel = new JLabel("Enter query");
		queryTextField = new JTextField();

		searchButton = new JButton("Search");

		/* x axis, y axis, width, height */
		dictionaryPathLabel.setBounds(80, 40, 250, 30);
		dictionaryPathTextField.setBounds(80, 80, 250, 30);
		postingsPathLabel.setBounds(80, 120, 250, 30);
		postingsPathTextField.setBounds(80, 160, 250, 30);
		documentsPathLabel.setBounds(80, 200, 250, 30);
		documentPathTextField.setBounds(80, 240, 250, 30);
		stopWordsPathLabel.setBounds(80, 280, 250, 30);
		stopWordsPathTextField.setBounds(80, 320, 250, 30);
		pagerankPathLabel.setBounds(80, 360, 250, 30);
		pagerankPathTextField.setBounds(80, 400, 250, 30);
		w1Label.setBounds(80, 440, 100, 30);
		w1TextField.setBounds(80, 480, 100, 30);
		w2Label.setBounds(220, 440, 100, 30);
		w2TextField.setBounds(220, 480, 100, 30);
		enterQueryLabel.setBounds(80, 520, 250, 30);
		queryTextField.setBounds(80, 560, 250, 30);
		searchButton.setBounds(80, 600, 250, 30);

		/* Event Listeners */
		searchButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					runSearch();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});

		/* Add components to JFrame */
		frame.add(dictionaryPathLabel);
		frame.add(dictionaryPathTextField);
		frame.add(postingsPathLabel);
		frame.add(postingsPathTextField);
		frame.add(documentsPathLabel);
		frame.add(documentPathTextField);
		frame.add(stopWordsPathLabel);
		frame.add(stopWordsPathTextField);
		frame.add(pagerankPathLabel);
		frame.add(pagerankPathTextField);
		frame.add(w1Label);
		frame.add(w1TextField);
		frame.add(w2Label);
		frame.add(w2TextField);
		frame.add(enterQueryLabel);
		frame.add(queryTextField);
		frame.add(searchButton);

		frame.setSize(400, 750);
		frame.setLayout(null);
		/* Make the frame visible */
		frame.setVisible(true);

		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}

	/**
	 * Get the title and the author from the documents file when running search
	 * 
	 * @param similarities
	 * @param documentsFile
	 * @throws Exception
	 */
	public static void getTitleAndAuthor(LinkedHashMap<Integer, Double> similarities, File documentsFile)
			throws Exception {
		ArrayList<Integer> docIDs = new ArrayList<Integer>();
		ArrayList<Integer> docIDsUnsorted = new ArrayList<Integer>();
		similarities.entrySet().forEach(entry -> {
			docIDs.add(entry.getKey());
			docIDsUnsorted.add(entry.getKey());
		});

		HashMap<Integer, String> titleMap = new HashMap<Integer, String>();
		HashMap<Integer, String> authorMap = new HashMap<Integer, String>();

		Collections.sort(docIDs);

		BufferedReader br = new BufferedReader(new FileReader(documentsFile));

		for (int i = 0; i < docIDs.size(); i++) {
			String title = "";
			String author = "";

			String line = " ";

			while (!(line == null)) {
				line = br.readLine();

				try {
					if (line.equals(".I " + docIDs.get(i).toString())) {
						line = br.readLine();
						if (line.equals(".T")) {
							line = br.readLine();
							title = title + line;
							while (true) {
								line = br.readLine();
								if (line.equals(".B") || line.equals(".W")) {
									titleMap.put(docIDs.get(i), title);
									break;
								}
								title = title + " " + line;
							}
						}

						while (true) {
							line = br.readLine();
							if (line.equals(".A")) {
								line = br.readLine();
								author = author + line;
								while (true) {
									line = br.readLine();
									if (line.equals(".N") || line.equals(".X") || line.equals(".C")
											|| line.equals(".K")) {
										authorMap.put(docIDs.get(i), author);
										break;
									}
									author = author + " " + line;
								}
								break;
							}

						}

						break;
					}
				} catch (Exception e) {
					br = new BufferedReader(new FileReader(documentsFile));
				}
			}
		}

		createResultsFrame(docIDsUnsorted, titleMap, authorMap);

	}

	/**
	 * Create a JFrame to show the results
	 * 
	 * @param docIDsUnsorted
	 * @param titleMap
	 * @param authorMap
	 */
	public static void createResultsFrame(ArrayList<Integer> docIDsUnsorted, HashMap<Integer, String> titleMap,
			HashMap<Integer, String> authorMap) {

		/* New JFrame to show result output after searching */
		JFrame resultFrame = new JFrame();

		/* Set frame size */
		resultFrame.setSize(600, 800);
		resultFrame.setVisible(true);

		/* Flow layout for the JFrame */
		resultFrame.getContentPane().setLayout(new FlowLayout());

		/* New text area to display results */
		JTextArea textArea = new JTextArea(42, 52);
		for (int i = 0; i < docIDsUnsorted.size(); i++) {
			textArea.append("Rank: " + (i + 1) + ", Doc ID: " + docIDsUnsorted.get(i) + ", Score: "
					+ similarities.get(docIDsUnsorted.get(i)) + "\n" + "Title: " + titleMap.get(docIDsUnsorted.get(i))
					+ "\n" + "Author: " + authorMap.get(docIDsUnsorted.get(i)) + "\n\n");
		}
		/* Make text area a JScrollPane for results that are long */
		JScrollPane scrollableTextArea = new JScrollPane(textArea);

		scrollableTextArea.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollableTextArea.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		/* Add scrollable text area to JFrame */
		resultFrame.getContentPane().add(scrollableTextArea);
	}

}