import java.io.IOException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;

public class Indexer {
    public static void buildCorpusData(String corpusName, ArrayList<String> docs) {
        int totalDocNum = docs.size();
        Trie allTrie = new Trie();
        ArrayList<HashMap<String, Double>> tfidfDB = new ArrayList<>();
        HashMap<String, HashSet<Integer>> containDocMap = new HashMap<>();
        for (int i = 0; i < docs.size(); i++) {
            String[] words = docs.get(i).split(" ");
            int docWordNum = words.length;
            Trie docTrie = new Trie();
            HashMap<String, Double> TfIdfMap = new HashMap<>();
            for (String term : words) {
                if (docTrie.insert(term) == true) {
                    allTrie.insert(term);
                }
            }
            for (String term : words) {
                if (containDocMap.containsKey(term) == false) {
                    containDocMap.put(term, new HashSet<Integer>());
                }
                containDocMap.get(term).add(i);
                TfIdfMap.put(term, TFIDF.tf(term, docWordNum, docTrie));
            }
            tfidfDB.add(TfIdfMap);
        }
        for (int i = 0; i < tfidfDB.size(); i++) {
            for (Map.Entry<String, Double> entry : tfidfDB.get(i).entrySet()) {
                double idf = TFIDF.idf(entry.getKey(), totalDocNum, allTrie);
                tfidfDB.get(i).put(entry.getKey(), TFIDF.tf_idf(entry.getValue(), idf));
            }
        }
        MyFileIO.writeObj(tfidfDB, "CorpusDatas/%s/tfidfDB".formatted(corpusName));
        MyFileIO.writeObj(containDocMap, "CorpusDatas/%s/containDocMap".formatted(corpusName));
    }

    public static ArrayList<HashMap<String, Double>> getTfIdfMap(String corpusName) {
        return MyFileIO.readtfidfDB("CorpusDatas/%s/tfidfDB".formatted(corpusName));
    }

    public static HashMap<String, HashSet<Integer>> getContainDocMap(String corpusName) {
        return MyFileIO.readContainDocMap("CorpusDatas/%s/containDocMap".formatted(corpusName));
    }
}

class TrieNode {
    public HashMap<Character, TrieNode> childs = new HashMap<>();
    public int count;
}

class Trie {
    private TrieNode root;

    public Trie() {
        this.root = new TrieNode();
    }

    public boolean insert(String word) {
        if (word == null || word.length() == 0) {
            return false;
        }

        TrieNode curNode = this.root;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (curNode.childs.containsKey(c) == false) {
                curNode.childs.put(c, new TrieNode());
            }
            curNode = curNode.childs.get(c);
        }
        curNode.count++;
        if (curNode.count == 1) {
            return true;
        }
        return false;
    }

    public TrieNode getNode(String word) {
        if (word == null || word.length() == 0) {
            return null;
        }

        TrieNode curNode = this.root;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (curNode.childs.containsKey(c) == false) {
                return null;
            }
            curNode = curNode.childs.get(c);
        }
        if (curNode.count == 0) {
            return null;
        }
        return curNode;
    }
}

class MyFileIO {

    public static void writeObj(ArrayList<HashMap<String, Double>> tfidfDB, String filePath) {
        delFile(filePath + ".ser");
        for (HashMap<String, Double> map : tfidfDB) {
            String fileContent = "";
            for (Map.Entry<String, Double> entry : map.entrySet()) {
                fileContent += "%s:%s,".formatted(entry.getKey(), Double.toString(entry.getValue()));
            }
            fileContent += "\n";
            writeFile(fileContent, filePath + ".ser", true);
        }
    }

    public static void writeObj(HashMap<String, HashSet<Integer>> containDocMap, String filePath) {
        delFile(filePath + ".ser");
        for (Map.Entry<String, HashSet<Integer>> entry : containDocMap.entrySet()) {
            String fileContent = entry.getKey() + ":";
            for (Integer docID : entry.getValue()) {
                fileContent += "%d,".formatted(docID);
            }
            fileContent += "\n";
            writeFile(fileContent, filePath + ".ser", true);
        }
    }

    public static void writeFile(String fileContent, String filePath, boolean appendMode) {
        try {
            File file = new File(filePath);
            file.getParentFile().mkdirs();
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, appendMode))) {
                bw.write(fileContent);
            }
        }
        catch (IOException e) {
            ;
        }
    }

    public static void delFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }

    public static ArrayList<HashMap<String, Double>> readtfidfDB(String filePath) {
        ArrayList<HashMap<String, Double>> tfidfDB = new ArrayList<>();
        try {
			BufferedReader reader = new BufferedReader(new FileReader(filePath + ".ser"));
			String line = reader.readLine();
			while (line != null && line.length() > 0) {
				HashMap<String, Double> map = new HashMap<>();
                for (String pair : line.split(",")) {
                    if (pair.length() > 0) {
                        String[] pairArr = pair.split(":");
                        map.put(pairArr[0], Double.valueOf(pairArr[1]));
                    }
                }
                tfidfDB.add(map);
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
        return tfidfDB;
    }

    public static HashMap<String, HashSet<Integer>> readContainDocMap(String filePath) {
        HashMap<String, HashSet<Integer>> containDocMap = new HashMap<>();

        try {
			BufferedReader reader = new BufferedReader(new FileReader(filePath + ".ser"));
			String line = reader.readLine();
			while (line != null && line.length() > 0) {
				HashSet<Integer> set = new HashSet<>();
                String[] pairArr = line.split(":");
                for (String setVars : pairArr[1].split(",")) {
                    if (setVars.length() > 0) {
                        set.add(Integer.valueOf(setVars));
                    }
                }
                containDocMap.put(pairArr[0], set);
				line = reader.readLine();
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
        return containDocMap;
    }
}

class TFIDF {
    public static double tf(String term, int docWordNum, Trie trie) {
        TrieNode node = trie.getNode(term);
        if (node == null) {
            return 0.0;
        }
        double wordNumInDoc = (double) node.count;
        return wordNumInDoc / docWordNum;
    }

    public static double idf(String term, int totalDocNum, Trie allTrie) {
        TrieNode node = allTrie.getNode(term);
        if (node == null) {
            return 0.0;
        }
        double containTermDocNum = (double) node.count;
        return Math.log(totalDocNum / containTermDocNum);
    }
    
    public static double tf_idf(double tf, double idf) {
        return tf * idf;
    }
}