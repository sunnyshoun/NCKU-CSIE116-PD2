import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Scanner;

public class Indexer {
    public static void setTotalDocsNum(String corpusName, int totalNum) {
        String docsDataPath = "./DocsData/%s/totalDocsNum".formatted(corpusName, totalNum);
        MyFileIO.writeIntData(totalNum, docsDataPath);
    }

    public static int getTotalDocsNum(String corpusName) {
        String docsDataPath = "./DocsData/%s/totalDocsNum".formatted(corpusName);
        return MyFileIO.readIntData(docsDataPath);
    }

    public static void setDocWordNum(String corpusName, int docID, int num) {
        String docsDataPath = "./DocsData/%s/docWordNum/doc%d".formatted(corpusName, docID);
        MyFileIO.writeIntData(num, docsDataPath);
    }

    public static int getDocWordNum(String corpusName, int docID) {
        String docsDataPath = "./DocsData/%s/docWordNum/doc%d".formatted(corpusName, docID);
        return MyFileIO.readIntData(docsDataPath);
    }

    public static int getWordNum(String corpusName, int docID, String term) {
        String nodePath = "./DocsData/%s/Trie".formatted(corpusName);
        for (char c : term.toCharArray()) {
            nodePath += "/" + c;
        }
        nodePath += "/doc" + docID;
        return MyFileIO.readIntData(nodePath + "/doc" + docID);
    }

    public static void buildTrie(String corpusName, ArrayList<String> docs) {
        setTotalDocsNum(corpusName, docs.size());
        Trie trie = new Trie();
        for(int i = 0; i < docs.size(); i++) {
            String[] terms = docs.get(i).trim().split(" ");
            // setDocWordNum(corpusName, i, terms.length);
            for (String word : terms) {
                trie.insert(word, i);
            }
        }
        // MyFileIO.writeObj(allTrie, "./DocsData/%s/containDocsNum".formatted(corpusName));
        trie.serialize(null, "./DocsData/%s/containDocs".formatted(corpusName));
    }

    // public static Set<Integer> getContainDocs(String corpusName, String term) {
    //     String nodePath = "./DocsData/%s/Trie".formatted(corpusName);

    //     for (char c : term.toCharArray()) {
    //         nodePath += "/" + c;
    //     }
    //     return MyFileIO.readObj(nodePath);
    // }
}

class TrieNode {
    public HashMap<Character, TrieNode> childs = new HashMap<>();
    public HashSet<Integer> containDocs = new HashSet<>();
}

class Trie {
    private TrieNode root;

    public Trie() {
        this.root = new TrieNode();
    }

    public void insert(String word, int docID) {
        if (word == null || word.length() == 0) {
            return;
        }

        TrieNode curNode = this.root;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (curNode.childs.get(c) == null) {
                curNode.childs.put(c, new TrieNode());
            }
            curNode = curNode.childs.get(c);
        }
        curNode.containDocs.add(docID);
    }

    public void serialize(TrieNode curNode, String triePath) {
        if (curNode == null) {
            curNode = this.root;
        }
        for (Map.Entry<Character, TrieNode> entry : curNode.childs.entrySet()) {
            char c = entry.getKey();
            TrieNode nextNode = entry.getValue();
            MyFileIO.writeObj(nextNode.containDocs, triePath + "/" + c);
            serialize(nextNode, triePath + "/" + c);
        }
    }
}

class MyFileIO {

    public static void writeObj(HashSet<Integer> Set, String filePath) {
        try {
            File f = new File(filePath);
            f.getParentFile().mkdirs();
            FileOutputStream fileOut = new FileOutputStream(filePath + ".ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(Set);
            out.close();
            fileOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static HashSet<Integer> readObj(String filePath) {
        HashSet<Integer> set = null;
        try {
            FileInputStream fileIn = new FileInputStream(filePath + ".ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            set = (HashSet<Integer>) in.readObject();
            in.close();
            fileIn.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return set;
    }

    public static void writeIntData(int num, String filePath) {
        try {
            File f = new File(filePath);
            f.getParentFile().mkdirs();
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath + ".txt"));
            writer.write(Integer.toString(num));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int readIntData(String filePath) {
        int result = 0;
        try {
            File f = new File(filePath + ".txt");
            if (f.exists() == false) {
                return 0;
            }
            Scanner sr = new Scanner(f);
            if (sr.hasNextInt())
            {
                result = sr.nextInt();
            }
            sr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}