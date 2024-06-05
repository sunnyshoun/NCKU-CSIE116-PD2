import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
        return MyFileIO.readContainDocSet("CorpusDatas/%s/containDocMap".formatted(corpusName));
    }
}

class TrieNode {
    public HashMap<Character, TrieNode> childs = new HashMap<>();
    public transient int count;
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
        try {
            File f = new File(filePath);
            f.getParentFile().mkdirs();
            FileOutputStream fileOut = new FileOutputStream(filePath + ".ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(tfidfDB);
            out.close();
            fileOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeObj(HashMap<String, HashSet<Integer>> containDocMap, String filePath) {
        try {
            File f = new File(filePath);
            f.getParentFile().mkdirs();
            FileOutputStream fileOut = new FileOutputStream(filePath + ".ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(containDocMap);
            out.close();
            fileOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static ArrayList<HashMap<String, Double>> readtfidfDB(String filePath) {
        ArrayList<HashMap<String, Double>> tfidfDB = null;
        try {
            FileInputStream fileIn = new FileInputStream(filePath + ".ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            tfidfDB = (ArrayList<HashMap<String, Double>>) in.readObject();
            in.close();
            fileIn.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return tfidfDB;
    }

    @SuppressWarnings("unchecked")
    public static HashMap<String, HashSet<Integer>> readContainDocSet(String filePath) {
        HashMap<String, HashSet<Integer>> tfidfDB = null;
        try {
            FileInputStream fileIn = new FileInputStream(filePath + ".ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            tfidfDB = (HashMap<String, HashSet<Integer>>) in.readObject();
            in.close();
            fileIn.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return tfidfDB;
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