import java.io.FileReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class test {
    public static void main(String[] args) {
        String filePath = args[0];
        List<Trie> tries = new ArrayList<>();
        Trie total_trie = new Trie();
        File outputFile = new File("output.txt");
        if (outputFile.exists()) {
            outputFile.delete();
        }
        try {
            FileReader fileReader = new FileReader(filePath);
            BufferedReader bufferReader = new BufferedReader(fileReader);
            String line = null;
            Trie trie = new Trie();
            int count = 0;
            while ((line = bufferReader.readLine()) != null) {
                String processed = line.replaceAll("[^a-zA-Z]", " ").toLowerCase();
                String[] words = processed.split("\\s+");
                // DynamicTrieArray tries = new DynamicTrieArray();

                for (String word : words) {
                    if (!word.isEmpty()) {
                        if (trie.insert(word) == true) {
                            total_trie.insert(word);
                        }
                    }
                }
                
                // Increment the line counter
                count++;

                // Check if we have processed 5 lines, if so, reset for next batch
                if (count == 5) {
                    tries.add(trie); // Add the current Trie to the list
                    trie = new Trie(); // Start a new Trie for the next group
                    count = 0; // Reset line counter
                }

            }
            bufferReader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        String filepath2 = args[1];
        try {
            FileReader filereader1 = new FileReader(filepath2);
            BufferedReader bufferedReader1 = new BufferedReader(filereader1);
            String line1;
            String line2;

            line1 = bufferedReader1.readLine();
            String[] checkPhrase = line1.split("\\s+");

            line2 = bufferedReader1.readLine();
            String[] checkPhraseNumber = line2.split("\\s+");
            bufferedReader1.close();

            for (int i = 0; i < checkPhraseNumber.length; i++) {
                Tool.WriteFile(Tool.tfIdfCalculate(checkPhrase[i],tries.get(Integer.parseInt(checkPhraseNumber[i])), tries, total_trie));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}

class TrieNode {
    TrieNode[] children = new TrieNode[26];
    int count = 0;
}

class Trie {
    TrieNode root = new TrieNode();
    private int totalWordsInserted = 0;

    // 插入一個單詞到 Trie
    public boolean insert(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            if (node.children[c - 'a'] == null) {
                node.children[c - 'a'] = new TrieNode();
            }
            node = node.children[c - 'a'];
        }
        totalWordsInserted++;
        node.count++;
        if(node.count == 1) {
            return true; //該字串沒被insert過，回傳true
        }
        return false; //該字串已被insert過
    }

    public int totalWordsInserted() {
        return totalWordsInserted;
    }

    public int countWord(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            int index = c - 'a';
            if (node.children[index] == null) {
                return 0;
            }
            node = node.children[index];
        }
        return node.count;
    }
}

class Tool {
    public static void WriteFile(double term) {

        try {

            File file = new File("./output.txt");
            if (!file.exists()) {
                file.createNewFile();
            }
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file,true))) {
                bw.write(String.format("%.5f", term) + " ");
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public static double tf(Trie trie, String word) {
        int number_term_in_doc = trie.countWord(word);
        int total = trie.totalWordsInserted();
        return (double)number_term_in_doc / total;
    }

    public static double idf(List<Trie> tries,Trie total_trie, String word) {

        int number_doc_contain_term = total_trie.countWord(word);
        return Math.log((double)tries.size() / number_doc_contain_term);
    }

    public static double tfIdfCalculate(String word,Trie docTrie, List<Trie> tries, Trie total_trie) {
        return tf(docTrie, word) * idf(tries, total_trie, word);
    }
}

// class DynamicTrieArray {
// private Trie[] array;
// // private int size;
// private int capacity;

// public DynamicTrieArray() {
// capacity = 10; // Initial capacity
// array = new Trie[capacity];
// size = 0;
// }

// public void add(Trie trie) {
// if (size == capacity) {
// resize();
// }
// array[size++] = trie;
// }

// private void resize() {
// capacity = capacity * 2; // Double the capacity
// Trie[] newArray = new Trie[capacity];
// for (int i = 0; i < size; i++) {
// newArray[i] = array[i];
// }
// array = newArray;
// }

// public Trie get(int index) {
// if (index < 0 || index >= size) {
// throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
// }
// return array[index];
// }

// public int getSize() {
// return size;
// }
// }