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

public class Eval {

	/* Global GUI components */
	private static JFrame frame;
	private static JLabel queryFilePathLabel;
	private static JTextField queryFilePathTextField;
	private static JLabel qrelsFilePathLabel;
	private static JTextField qrelsFilePathTextField;
	private static JLabel dictionaryPathLabel;
	private static JTextField dictionaryPathTextField;
	private static JLabel postingsPathLabel;
	private static JTextField postingsPathTextField;
	private static JLabel stopWordsPathLabel;
	private static JTextField stopWordsPathTextField;
	private static JLabel pagerankPathLabel;
	private static JTextField pagerankPathTextField;
	private static JLabel w1Label;
	private static JTextField w1TextField;
	private static JLabel w2Label;
	private static JTextField w2TextField;
	private static JButton runButton;

	private static HashMap<String, Integer> dictionary;
	private static HashMap<String, String> posting;

	private static HashMap<Integer, String> queries;
	private static HashMap<Integer, ArrayList<Integer>> qrels;

	private static HashMap<Integer, LinkedHashMap> scoreQueries;

	private static HashMap<Integer, Double> mapValues;
	private static HashMap<Integer, Double> rPrecisions;

	private static double w1;
	private static double w2;

	public static void main(String[] args) throws Exception {
		/* Create GUI */
		createGUI();
	}

	/**
	 * Method to read the input files for the test
	 * 
	 * @param filename
	 * @throws Exception
	 */
	public static void readQueryFile() throws Exception {
		String queryPath = queryFilePathTextField.getText();
		File documentsFile = new File(queryPath);
		BufferedReader br = new BufferedReader(new FileReader(documentsFile));

		queries = new HashMap<Integer, String>();
		String line = br.readLine();
		while (!(line == null)) {

			int qid = Integer.parseInt(line.substring(3));

			if (qid == 0)
				break;

			String query = "";
			line = br.readLine();

			line = br.readLine();
			query = query.concat(line);
			while (true) {
				line = br.readLine();
				if (line.contains(".N") || line.contains(".A"))
					break;
				query = query + " " + line;
			}

			queries.put(qid, query);
			while (true) {
				line = br.readLine();
				if (line.contains(".I")) {
					break;
				}
			}
		}
	}

	/**
	 * Read the qrels.text file
	 * 
	 * @throws Exception
	 */
	public static void readQrelsFile() throws Exception {
		String qrelsPath = qrelsFilePathTextField.getText();
		File qrelsFile = new File(qrelsPath);
		BufferedReader br = new BufferedReader(new FileReader(qrelsFile));

		qrels = new HashMap<Integer, ArrayList<Integer>>();

		String line = br.readLine();
		while (!(line == null)) {
			String[] lineSplit = line.split(" ");
			int queryNum = Integer.parseInt(lineSplit[0]);
			int docNum = Integer.parseInt(lineSplit[1]);

			if (qrels.containsKey(queryNum)) {
				ArrayList<Integer> docs = qrels.get(queryNum);
				docs.add(docNum);
				qrels.replace(queryNum, docs);
			} else {
				ArrayList<Integer> docs = new ArrayList<Integer>();
				docs.add(docNum);
				qrels.put(queryNum, docs);
			}
			line = br.readLine();

		}
	}

	/**
	 * Read dictionary and posting files
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
	 * Get the cosine similarities into a data structure
	 * 
	 * @throws Exception
	 */
	public static void computeScores() throws Exception {
		dictionary = new HashMap<String, Integer>();
		posting = new HashMap<String, String>();
		scoreQueries = new HashMap<Integer, LinkedHashMap>();

		/* Read dictionary and postings files */
		readInputFile(dictionaryPathTextField.getText());
		readInputFile(postingsPathTextField.getText());

		for (int i = 1; i <= queries.size(); i++) {
			Search search = new Search(dictionary, posting, queries.get(i), stopWordsPathTextField.getText(),
					pagerankPathTextField.getText(), w1, w2);
			scoreQueries.put(i, search.getSimilarities());
		}
	}

	/**
	 * Get MAP and R Precision values
	 */
	public static void getMapandR() {
		mapValues = new HashMap<Integer, Double>();
		rPrecisions = new HashMap<Integer, Double>();

		for (int i = 1; i <= scoreQueries.size(); i++) {
			double mapValue = 0;
			double rPrecision = 0;
			LinkedHashMap<Integer, Double> scores = scoreQueries.get(i);
			ArrayList<Integer> relevantDocs = qrels.get(i);
			ArrayList<Integer> retrievedDocs = new ArrayList<Integer>();

			scores.entrySet().forEach(entry -> {
				retrievedDocs.add(entry.getKey());
			});

			if (relevantDocs == null) {
				mapValues.put(i, 0.0);
				rPrecisions.put(i, 0.0);
				continue;
			}

			double count = 0;
			for (int j = 1; j <= retrievedDocs.size(); j++) {
				int docNum = retrievedDocs.get(j - 1);

				if (relevantDocs.contains(docNum)) {
					count++;
					mapValue = mapValue + (count / (double) j);
				}
			}
			mapValue = mapValue / (double) retrievedDocs.size();
			rPrecision = (double) count / (double) retrievedDocs.size();

			mapValues.put(i, mapValue);
			rPrecisions.put(i, rPrecision);
		}
	}

	/**
	 * Get average MAP value
	 * 
	 * @return Average MAP value
	 */
	public static double calculateAvgMAP() {
		double result = 0.0;
		for (int i = 1; i <= mapValues.size(); i++) {
			result += mapValues.get(i);
		}
		return result / mapValues.size();
	}

	/**
	 * Get average R Precision value
	 * 
	 * @return Average precision value
	 */
	public static double calculateAvgRPrecision() {
		double result = 0.0;
		for (int i = 1; i <= rPrecisions.size(); i++) {
			result += rPrecisions.get(i);
		}
		return result / rPrecisions.size();
	}

	/**
	 * Create the GUI
	 */
	public static void createGUI() {

		/* Creating instance of JFrame */
		frame = new JFrame();

		/* Creating instances of GUI components */
		queryFilePathLabel = new JLabel("query.text Path");
		queryFilePathTextField = new JTextField("query.text");
		qrelsFilePathLabel = new JLabel("qrels.text Path");
		qrelsFilePathTextField = new JTextField("qrels.text");
		dictionaryPathLabel = new JLabel("Dictionary Path");
		dictionaryPathTextField = new JTextField("dictionary.txt");
		postingsPathLabel = new JLabel("Postings Path");
		postingsPathTextField = new JTextField("posting.txt");
		stopWordsPathLabel = new JLabel("Stop Words Path");
		stopWordsPathTextField = new JTextField("common_words");
		pagerankPathLabel = new JLabel("PageRank File Path");
		pagerankPathTextField = new JTextField("pagerank.txt");
		w1Label = new JLabel("W1");
		w1TextField = new JTextField("0.5");
		w2Label = new JLabel("W2");
		w2TextField = new JTextField("0.5");

		runButton = new JButton("Run");

		/* x axis, y axis, width, height */
		queryFilePathLabel.setBounds(80, 40, 250, 30);
		queryFilePathTextField.setBounds(80, 80, 250, 30);
		qrelsFilePathLabel.setBounds(80, 120, 250, 30);
		qrelsFilePathTextField.setBounds(80, 160, 250, 30);
		dictionaryPathLabel.setBounds(80, 200, 250, 30);
		dictionaryPathTextField.setBounds(80, 240, 250, 30);
		postingsPathLabel.setBounds(80, 280, 250, 30);
		postingsPathTextField.setBounds(80, 320, 250, 30);
		stopWordsPathLabel.setBounds(80, 360, 250, 30);
		stopWordsPathTextField.setBounds(80, 400, 250, 30);
		pagerankPathLabel.setBounds(80, 440, 250, 30);
		pagerankPathTextField.setBounds(80, 480, 250, 30);
		w1Label.setBounds(80, 520, 100, 30);
		w1TextField.setBounds(80, 560, 100, 30);
		w2Label.setBounds(220, 520, 100, 30);
		w2TextField.setBounds(220, 560, 100, 30);
		runButton.setBounds(80, 600, 250, 40);

		/* Event Listeners */
		runButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try { // Error checking for correct values of w1 and w2. If error set both to 0.5
					boolean isDouble = true;
					try {
						double value1 = Double.parseDouble(w1TextField.getText());
						double value2 = Double.parseDouble(w1TextField.getText());
					} catch (NumberFormatException e1) {
						isDouble = false;
					}
					if (isDouble) {
						if ((Double.parseDouble(w1TextField.getText()) > 1.0
								|| Double.parseDouble(w1TextField.getText()) < 0.0)
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

					readQueryFile();
					readQrelsFile();
					computeScores();
					getMapandR();
					/* New JFrame to show result output after searching */
					JFrame resultFrame = new JFrame();

					/* Set frame size */
					resultFrame.setSize(600, 800);
					resultFrame.setVisible(true);

					/* Flow layout for the JFrame */
					resultFrame.getContentPane().setLayout(new FlowLayout());

					/* New text area to display results */
					JTextArea textArea = new JTextArea(42, 52);

					for (int i = 1; i <= mapValues.size(); i++) {
						textArea.append("Query " + i + "\nMap Value = " + mapValues.get(i) + "\nR Precision = "
								+ rPrecisions.get(i) + "\n\n");
					}
					textArea.append("\nAverage MAP = " + calculateAvgMAP() + "\n");
					textArea.append("Average R Precision = " + calculateAvgRPrecision() + "\n\n\n");

					/* Make text area a JScrollPane for results that are long */
					JScrollPane scrollableTextArea = new JScrollPane(textArea);

					scrollableTextArea.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
					scrollableTextArea.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

					/* Add scrollable text area to JFrame */
					resultFrame.getContentPane().add(scrollableTextArea);

				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});

		/* Add components to JFrame */
		frame.add(queryFilePathLabel);
		frame.add(queryFilePathTextField);
		frame.add(qrelsFilePathLabel);
		frame.add(qrelsFilePathTextField);
		frame.add(dictionaryPathLabel);
		frame.add(dictionaryPathTextField);
		frame.add(postingsPathLabel);
		frame.add(postingsPathTextField);
		frame.add(stopWordsPathLabel);
		frame.add(stopWordsPathTextField);
		frame.add(pagerankPathLabel);
		frame.add(pagerankPathTextField);
		frame.add(w1Label);
		frame.add(w1TextField);
		frame.add(w2Label);
		frame.add(w2TextField);
		frame.add(runButton);

		frame.setSize(400, 750);
		frame.setLayout(null);
		/* Make the frame visible */
		frame.setVisible(true);

		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}
}
