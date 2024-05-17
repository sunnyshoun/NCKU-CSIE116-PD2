import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

public class TFIDFCalculator {
    public static void main(String[] args) {
        TcData tcData = MyFileIO.readTcData(args[1]);
        ArrayList<String[]> docs = MyFileIO.parseDocs(MyFileIO.readAsDocs(args[0]));
        Trie allTrie = new Trie(0);
        ArrayList<Trie> tries = new ArrayList<>();
        for (int i = 0; i < docs.size(); i++) {
            tries.add(new Trie(docs.get(i).length));
            for (String word : docs.get(i)) {
                if (tries.get(i).insert(word) == 1) {
                    allTrie.insert(word);
                }
            }
        }
        String outputResult = "";
        for (int i = 0; i < tcData.targetNum; i++) {
            int DocIndex = tcData.targetDocs[i];
            String targetTerm = tcData.targetWords[i];
            double tfIdf = TFIDF.tfIdfCalculate(allTrie, tries, DocIndex, targetTerm);
            outputResult += "%.5f ".formatted(tfIdf);
        }
        MyFileIO.writeFile("output.txt", outputResult, false);
    }
}

class TrieNode {
    public TrieNode[] childs = new TrieNode[26];
    public int ContainNum = 0;
}

class Trie {
    private TrieNode root;
    private int wordNum = 0;

    public Trie(int wordNum) {
        this.root = new TrieNode();
        this.wordNum = wordNum;
    }

    public int insert(String word) {
        if (word == null || word.length() == 0) {
            return -1;
        }

        TrieNode curNode = this.root;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (curNode.childs[c - 'a'] == null) {
                curNode.childs[c - 'a'] = new TrieNode();
            }
            curNode = curNode.childs[c - 'a'];
        }
        int isNew = (curNode.ContainNum == 0) ? 1 : 0;
        curNode.ContainNum += 1;
        return isNew;
    }

    public int getWordNum() {
        return this.wordNum;
    }

    public int getContainNum(String word) {
        TrieNode node = getTrieNode(word);
        if (node == null) {
            return 0;
        }
        return node.ContainNum;
    }

    public TrieNode getTrieNode(String word) {
        if (word == null || word.length() == 0) {
            return null;
        }

        TrieNode curNode = root;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (curNode.childs[c - 'a'] == null) {
                return null;
            } 
            curNode = curNode.childs[c - 'a'];
        }
        return curNode;
    }
}

class TcData {
    public String[] targetWords = null;
    public int[] targetDocs = null;
    public int targetNum = 0;

    public String toString() {
        String result = "";
        for (int i = 0; i < this.targetWords.length; i++) {
            result += "%s : %d\n".formatted(this.targetWords[i], this.targetDocs[i]);
        }
        return result;
    }
}

class TFIDF {
    public static double tf(ArrayList<Trie> tries, int docIndex, String term) {
        Trie t = tries.get(docIndex);
        double number_term_in_doc = t.getContainNum(term);
        return number_term_in_doc / t.getWordNum();
    }
    public static double idf(Trie allTrie, ArrayList<Trie> tries, String term) {
        double number_doc_contain_term = allTrie.getContainNum(term);
        return Math.log(tries.size() / number_doc_contain_term);
    }
    public static double tfIdfCalculate(Trie allTrie, ArrayList<Trie> tries, int docIndex, String term) {
        return tf(tries, docIndex, term) * idf(allTrie, tries, term);
    }
}

class MyFileIO {
    public static void writeFile(String filePath, String fileContent, boolean appendMode) {
        File file = new File(filePath);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
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

    public static TcData readTcData(String tcPath) {
        TcData tcData = new TcData();
        try (BufferedReader br = new BufferedReader(new FileReader(tcPath))) {
            tcData.targetWords = br.readLine().split(" ");
            String[] numbers = br.readLine().split(" ");
            int[] targetDocs = new int[numbers.length];
            for (int i = 0; i < numbers.length; i++) {
                targetDocs[i] = Integer.parseInt(numbers[i]);
            }
            tcData.targetDocs = targetDocs;
            tcData.targetNum = targetDocs.length;
        }
        catch (IOException e) {
            ;
        }
        return tcData;
    }

    public static ArrayList<String> readAsDocs(String filePath) {
        ArrayList<String> docs = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            int i = 1;
            String line, docContent = "";
            while ((line = br.readLine()) != null) {
                docContent += line;
                if (i % 5 == 0) {
                    docs.add(docContent);
                    docContent = "";
                }
                i++;
            }
        }
        catch (IOException e) {
            ;
        }
        return docs;
    }

    public static ArrayList<String[]> parseDocs(ArrayList<String> docs) {
        ArrayList<String[]> newDocs = new ArrayList<>();
        for (String content : docs) {
            newDocs.add(ContentParse(content));
        }
        return newDocs;
    }

    public static String[] ContentParse(String content) {
        return content.replaceAll("[^a-zA-Z]+", " ").toLowerCase().trim().split(" ");
    }
}
