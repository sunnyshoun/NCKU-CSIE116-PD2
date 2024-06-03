import java.io.*;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;

public class BuildIndex {
    public static void main(String[] args) {
        String corpusName = getCorpusName(args[0]);
        ArrayList<String> docs = readAsDocs(args[0]);
        Indexer.buildTrie(corpusName, docs);
    }

    public static ArrayList<String> readAsDocs(String filePath) {
        ArrayList<String> docs = new ArrayList<>();
        try {
            String fileContent = Files.readString(Paths.get(filePath));
            fileContent = fileContent.toLowerCase();
            fileContent = fileContent.replaceAll("[^a-z\n]+", " ");
            int i = 1;
            String docContent = "";
            for(String line : fileContent.split("\\s?\n")) {
                docContent += line;
                if (i % 5 == 0) {
                    docs.add(docContent);
                    docContent = "";
                }
                i++;
            }
            if(docContent != "") {
                docs.add(docContent);
            }
        }
        catch (IOException e) {
            ;
        }
        return docs;
    }

    public static String getCorpusName(String filePath) {
        File f = new File(filePath);
        return f.getName().substring(0, f.getName().indexOf("."));
    }
}