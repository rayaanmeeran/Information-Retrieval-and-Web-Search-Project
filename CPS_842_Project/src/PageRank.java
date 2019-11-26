import java.util.HashMap;

public class PageRank {

	private HashMap<Integer, String> citations;
	private HashMap<Integer, Double> pageRanks;
	private double[][] aMatrix;
	
	// This constructor builds the page rank file from Invert
	public PageRank(HashMap<Integer, String> citations) {
		this.citations = citations;
	}
	
	// This constructor is when getting page rank scores for a document during search
	public PageRank() {
		// pageRanks = read page ranks file
	}
	
	public void buildMatrix() {
		aMatrix = new double[3204][3204];
	}
	
	public double getPageRank(int docNum) {
		return pageRanks.get(docNum);
	}
	
	public static void main(String[] args) {

	}

}
