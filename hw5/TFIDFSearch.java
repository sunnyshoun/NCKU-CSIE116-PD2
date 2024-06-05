import java.io.*;
import java.util.*;


public class TFIDFSearch {
    public static void main(String[] args) {
		try {
            ArrayList<HashMap<String, Double>> tfidfDB = Indexer.getTfIdfMap(args[0]);
            HashMap<String, HashSet<Integer>> containDocMap = Indexer.getContainDocMap(args[0]);
			BufferedReader reader = new BufferedReader(new FileReader(args[1]));
			String line = reader.readLine();
            int i = 0, resultNum = 0;
            String outputContent = "";
			while (line != null && line.length() > 0) {
				if (i == 0) {
                    resultNum = Integer.parseInt(line);
                }
                else {
                    String[] queryStr = line.split(" AND | OR ");
                    HashSet<Integer> querySet = new HashSet<>();
                    HashMap<Integer, Double> queryResult = new HashMap<>();
                    for (int j = 0; j < queryStr.length; j++) {
                        String s = queryStr[j];
                        HashSet<Integer> compareSet = new HashSet<>();
                        if (containDocMap.containsKey(s)) {
                            compareSet = containDocMap.get(s);
                        }

                        if (j == 0) {
                            querySet.addAll(compareSet);
                        }
                        else if (line.contains("AND")) {
                            querySet.retainAll(compareSet);
                        }
                        else if (line.contains("OR")) {
                            querySet.addAll(compareSet);
                        }
                    }

                    for (int docID : querySet) {
                        queryResult.put(docID, 0.0);
                        for (String s : queryStr) {
                            // System.out.println(s + " : " + tfidfDB.get(docID).get(s));
                            if (tfidfDB.get(docID).containsKey(s)) {
                                double val = queryResult.get(docID);
                                queryResult.put(docID, val + tfidfDB.get(docID).get(s));
                            }
                        }
                    }
                    
                    List<Map.Entry<Integer, Double>> entryList = new ArrayList<>(queryResult.entrySet());
                    Collections.sort(entryList, new Comparator<Map.Entry<Integer, Double>>() {
                        @Override
                        public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
                            int valueCompare = Double.compare(o2.getValue(), o1.getValue());
                            if (valueCompare == 0) {
                                return Integer.compare(o1.getKey(), o2.getKey());
                            }
                            return valueCompare;
                        }
                    });
                    for (int j = 0; j < resultNum; j++) {
                        if (j < entryList.size()) {
                            outputContent += entryList.get(j).getKey() + " ";
                        }
                        else {
                            outputContent += "-1 ";
                        }
                    }
                    outputContent = outputContent.trim()+"\n";
                }
				line = reader.readLine();
                i++;
			}
			reader.close();
            OutputResult.writeResult(outputContent);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}

class OutputResult {
    public static void writeResult(String fileContent) {
        File file = new File("output.txt");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                bw.write(fileContent);
            }
        }
        catch (IOException e) {
            ;
        }
    }
}