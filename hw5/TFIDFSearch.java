import java.io.*;
import java.util.*;


public class TFIDFSearch {
    public static void main(String[] args) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(args[1]));
			String line = reader.readLine();
            int i = 0, resultNum = 0;
			while (line != null) {
				if (i == 0) {
                    resultNum = Integer.parseInt(line);
                }
                else {
                    String[] queryStr = line.split(" AND | OR ");
                    if (line.contains("AND")) {
                        for (String s : queryStr) {
                            
                        }
                    }
                    else if (line.contains("OR")) {

                    }
                    else {

                    }
                }
				line = reader.readLine();
                i++;
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}

class TFIDF {
    public static double tf(String corpusName, String term, int docID) {
        double number_term_in_doc = Indexer.getWordNum(corpusName, docID, term);
        return number_term_in_doc / Indexer.getDocWordNum(corpusName, docID);
    }

    public static double idf(String corpusName, String term) {
        int total_num_of_docs = Indexer.getTotalDocsNum(corpusName);
        double number_doc_contain_term = Indexer.getContainDocs(corpusName, term).size();
        if (number_doc_contain_term == 0) {
            return 0.0;
        }
        return Math.log(total_num_of_docs / number_doc_contain_term);
    }
    
    public static double tf_idf(String corpusName, String term, int docID) {
        return tf(corpusName, term, docID) * idf(corpusName, term);
    }
}