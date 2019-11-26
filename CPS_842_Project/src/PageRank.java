import java.util.Arrays;
import java.util.HashMap;

public class PageRank {

	private static HashMap<Integer, String> citations;
	private HashMap<Integer, Double> pageRanks;
	private static double[][] aMatrix;
	private static final double dampingFactor = 0.85; 
	
	// This constructor builds the page rank file from Invert
	public PageRank(HashMap<Integer, String> citations) {
		this.citations = citations;
		buildMatrix();
	}
	
	// This constructor is when getting page rank scores for a document during search
	public PageRank() {
		// pageRanks = read page ranks file
	}
	
	public static void buildMatrix() {
		aMatrix = new double[3204][3204];
		
		for (int i = 1; i <= 3204; i++) {
			if (citations.containsKey(i)) {
				String[] citationLine = citations.get(i).split(",");
				
				
				for (int j = 0; j < citationLine.length; j++) {
					String[] citeLine = citationLine[j].split("[\\p{Punct}\\s]+");
					
					int small = Integer.parseInt(citeLine[0]);
					int big = Integer.parseInt(citeLine[2]);
					if (small > big) {
						int temp = small;
						small = big;
						big = temp;
					}
					
					aMatrix[big - 1][small - 1] = 1.0;
				}
			}
		}
		
		for (int i = 0; i < 3204; i++) {
			boolean skip = true;
			for (int j = 0; j < 3204; j++) {
				if (aMatrix[i][j] == 1.0) {
					skip = false;
					break;
				}
			}
			if (skip) {
				for (int j = 0; j < 3204; j++)
					aMatrix[i][j] = 1.0 / 3204.0;
			}
			else {
				double count = 0.0;
				for (int j = 0; j < 3204; j++) {
					if (aMatrix[i][j] == 1.0)
						count = count + 1.0;
				}
				for (int j = 0; j < 3204; j++) {
					aMatrix[i][j] = aMatrix[i][j] / count;
				}
				for (int j = 0; j < 3204; j++) {
					aMatrix[i][j] = aMatrix[i][j] * dampingFactor;
				}
				for (int j = 0; j < 3204; j++) {
					aMatrix[i][j] = aMatrix[i][j] + ((1- dampingFactor) / 3204);
				}
			}
		}
		
		
		test();	
	}
	
	public double getPageRank(int docNum) {
		return pageRanks.get(docNum);
	}
	
	public static void test() {
		for (int i = 0; i < 3204; i++) {
			System.out.print(i  + ":  ");
			for (int j = 0; j < 3204; j++) {
				System.out.print(aMatrix[i][j] + " ");
			}
			System.out.println();
		}
		/*for (int i = 0; i < 3204; i++) {
			System.out.print(i + 1 + ": " );
			System.out.println(aMatrix[1395][i]);
		}*/
		
	}
	
	public static void main(String[] args) {
		
	}

}
