/*
 * CPS 842 Project
 * Rayaan Meeran 500749720 (Section 02)
 * John Gomes 500754885 (Section 01)
 * */

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class Search {

	private static HashMap<String, Integer> dictionary;
	private static HashMap<String, String> posting;
	private static String query;

	private static String[] wordVector;
	private static double[] vector;
	private static double[] dfVector;

	private static HashMap<Integer, double[]> documents;
	private static LinkedHashMap<Integer, Double> similarities;

	private static String stopWordsPath;
	private static boolean useStopWords;
	private static boolean useStemming;

	private static int N;

	private static double w1;
	private static double w2;

	private static double[] pagerankVector;

	/**
	 * Search constructor method
	 * 
	 * @param dictionary
	 * @param posting
	 * @param query
	 * @param stopWordsPath
	 * @throws Exception
	 */
	public Search(HashMap<String, Integer> dictionary, HashMap<String, String> posting, String query,
			String stopWordsPath, String pagerankPath, Double w1, Double w2) throws Exception {
		this.dictionary = dictionary;
		this.posting = posting;
		this.query = query;
		this.stopWordsPath = stopWordsPath;

		this.wordVector = getWordVector(query);
		this.vector = getOccurrenceVector(query, wordVector);
		this.dfVector = getDFvector(wordVector);

		this.documents = new HashMap<Integer, double[]>();
		this.similarities = new LinkedHashMap<Integer, Double>();

		this.N = 3204;

		this.w1 = w1;
		this.w2 = w2;

		PageRank pagerank = new PageRank(pagerankPath);
		pagerankVector = pagerank.getPageRanksVector();

		getDocumentVectors(wordVector);
		search();
	}

	public String getQuery() {
		return this.query;
	}

	/**
	 * Cosine similarity method between 2 vectors
	 * 
	 * @param vector1
	 * @param vector2
	 * @param df      Term document frequency
	 * @param N       Number of documents
	 * @return return cosine similarity between vector1 and vector2
	 */
	public static double cosineSimilarity(double[] vector1, double[] vector2, double[] df, int N) {

		double[] tf1 = calculateTF(vector1);
		double[] tf2 = calculateTF(vector2);
		double[] idf = calculateIDF(N, df);
		double[] w1 = calculateWeights(tf1, idf);
		double[] w2 = calculateWeights(tf2, idf);

		double sum1 = 0;
		double sum2 = 0;
		double vector1Len;
		double vector2Len;
		double nw1[] = new double[vector1.length];
		double nw2[] = new double[vector2.length];
		double result = 0;

		for (int i = 0; i < w1.length; i++) {
			sum1 += w1[i] * w1[i];
			sum2 += w2[i] * w2[i];
		}

		vector1Len = Math.sqrt(sum1);
		vector2Len = Math.sqrt(sum2);

		for (int i = 0; i < nw1.length; i++) {
			if (vector1Len != 0)
				nw1[i] = w1[i] / vector1Len;
			else
				nw1[i] = 0;

			if (vector2Len != 0)
				nw2[i] = w2[i] / vector2Len;
			else
				nw2[i] = 0;
		}

		for (int i = 0; i < vector1.length; i++) {
			result += nw1[i] * nw2[i];
		}

		return result;
	}

	/**
	 * Calculate TF array
	 * 
	 * @param vector
	 * @return TF values as array
	 */
	public static double[] calculateTF(double[] vector) {
		double[] tf = new double[vector.length];
		for (int i = 0; i < vector.length; i++) {
			if (vector[i] != 0)
				tf[i] = 1 + Math.log10(vector[i]);
			else
				tf[i] = 0;
		}
		return tf;
	}

	/**
	 * Calculates IDF array
	 * 
	 * @param N  Number of documents
	 * @param df document frequency of term
	 * @return IDF values as an array
	 */
	public static double[] calculateIDF(int N, double df[]) {
		double[] idf = new double[df.length];
		for (int i = 0; i < df.length; i++) {
			if (df[i] != 0)
				idf[i] = Math.log10(N / df[i]);
			else
				idf[i] = 0;

		}
		return idf;
	}

	/**
	 * Calculate weights array
	 * 
	 * @param tf
	 * @param idf
	 * @return w Weights value as an array
	 */
	public static double[] calculateWeights(double[] tf, double[] idf) {
		double[] w = new double[tf.length];
		for (int i = 0; i < w.length; i++) {
			w[i] = tf[i] * idf[i];
		}
		return w;
	}

	/**
	 * 
	 * @param query
	 * @return
	 */
	public static String[] getWordVector(String query) {

		useStopWords();
		useStemming();

		// Splits query into its terms alphabetically
		String[] queryTerms = query.split("[\\p{Punct}\\s]+");

		for (int i = 0; i < queryTerms.length; i++) {
			queryTerms[i] = queryTerms[i].toLowerCase();
			if (useStemming) {
				Stemmer stem = new Stemmer();
				for (int j = 0; j < queryTerms[i].length(); j++) {
					stem.add(queryTerms[i].charAt(j));
				}
				stem.stem();
				queryTerms[i] = stem.toString();
			}
		}
		Arrays.sort(queryTerms);

		// Goes through query terms to get all terms only once
		List<String> uniqueQuery = new ArrayList<String>();

		File stopWordsFile = new File(stopWordsPath);
		ArrayList<String> stopWords = readStopWords(stopWordsFile);

		for (int i = 0; i < queryTerms.length; i++) {
			if (useStopWords) {
				if (stopWords.contains(queryTerms[i]))
					continue;
			}
			if (!uniqueQuery.contains(queryTerms[i]))
				uniqueQuery.add(queryTerms[i]);
		}

		// Converts list string list into string array
		String[] wordVector = new String[uniqueQuery.size()];
		for (int i = 0; i < uniqueQuery.size(); i++) {
			wordVector[i] = uniqueQuery.get(i);
		}

		return wordVector;
	}

	/**
	 * 
	 * @param query
	 * @param wordVector
	 * @return
	 */
	public static double[] getOccurrenceVector(String query, String[] wordVector) {
		String[] queryTerms = query.split("[\\p{Punct}\\s]+");
		// Gets number of occurrences for each term
		double[] vector = new double[wordVector.length];
		for (int i = 0; i < vector.length; i++) {
			vector[i] = 0;
			for (int j = 0; j < queryTerms.length; j++) {
				if (queryTerms[j].equals(wordVector[i]))
					vector[i]++;
			}
		}
		return vector;
	}

	/**
	 * Gets DF vector
	 * 
	 * @param wordVector
	 * @return
	 */
	public static double[] getDFvector(String[] wordVector) {
		double[] df = new double[wordVector.length];

		for (int i = 0; i < wordVector.length; i++) {
			if (dictionary.containsKey(wordVector[i]))
				df[i] = dictionary.get(wordVector[i]);
			else
				df[i] = 0;
		}
		return df;
	}

	public static void getDocumentVectors(String[] wordVector) {

		for (int i = 0; i < wordVector.length; i++) {

			if (posting.containsKey(wordVector[i])) {
				String[] postingDocs = posting.get(wordVector[i]).split(" ");

				for (int j = 0; j < postingDocs.length; j++) {
					String[] doc = postingDocs[j].split("\\|");
					int docID = Integer.parseInt(doc[0]);

					if (documents.containsKey(docID)) {
						double[] temp = documents.get(docID);
						temp[i] = temp[i] + Integer.parseInt(doc[1]);
						documents.replace(docID, temp);
					} else {
						double[] temp = new double[wordVector.length];
						for (int k = 0; k < wordVector.length; k++)
							temp[k] = 0;
						temp[i] = temp[i] + Integer.parseInt(doc[1]);
						documents.put(docID, temp);
					}
				}
			}
		}
	}

	/**
	 * Read stopWords file
	 * 
	 * @param stopWordsFile
	 * @return
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
	 * Check to use stop words
	 */
	public static void useStopWords() {
		if (dictionary.containsKey("the") || dictionary.containsKey("and"))
			useStopWords = false;
		else
			useStopWords = true;
	}

	/**
	 * Check to use Stemming
	 */
	public static void useStemming() {
		if (dictionary.containsKey("somewher") || dictionary.containsKey("categor"))
			useStemming = true;
		else
			useStemming = false;
	}

	/**
	 * Search for query and get scores
	 */
	public static void search() {

		HashMap<Integer, Double> tempSim = new HashMap<Integer, Double>();
		for (Entry<Integer, double[]> entry : documents.entrySet()) {
			double[] docVector = entry.getValue();
			double cosine = cosineSimilarity(docVector, vector, dfVector, N);
			double pagerankValue = pagerankVector[entry.getKey() - 1];
			double score = (w1 * cosine) + (w2 * pagerankValue);
			tempSim.put(entry.getKey(), score);
		}

		tempSim.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
				.forEachOrdered(x -> similarities.put(x.getKey(), x.getValue()));
	}

	/**
	 * Get Scores
	 * 
	 * @return
	 */
	public static LinkedHashMap<Integer, Double> getSimilarities() {
		return similarities;
	}

	/**
	 * Get documents
	 * 
	 * @return
	 */
	public static HashMap<Integer, double[]> getDocuments() {
		return documents;
	}
}