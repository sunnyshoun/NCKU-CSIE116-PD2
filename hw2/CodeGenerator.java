import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map; 
import java.util.regex.*;

class JavaWriter {
    public static void writeFileByMap(Map<String, String> FileMap) {
        for (Map.Entry<String, String> entry : FileMap.entrySet()) {
            writeFile(entry.getKey(), entry.getValue());
        }
    }

    public static void writeFile(String fileName, String fileContent) {
        File file = new File(fileName+".java");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                bw.write(fileContent);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class MermaidReader {
    private String[] contentLines;

    public MermaidReader(String filePath) {
        try {
            String fileString = Files.readString(Paths.get(filePath));
            fileString = fileString.replaceAll("\\s", " ");
            fileString = fileString.replaceAll("\\{", "{\n");
            fileString = fileString.replaceAll("\\}", "\n}");
            fileString = fileString.replaceAll(" *,", ", ");
            fileString = fileString.replaceAll(" *\\( *", "(");
            fileString = fileString.replaceAll(" *\\)", ") ");
            fileString = fileString.replaceAll(" *\\[ *", "[");
            fileString = fileString.replaceAll(" *\\]", "] ");
            fileString = fileString.replaceAll(":", " : ");
            fileString = fileString.replaceAll(" +", " ");
            this.contentLines = fileString.split("\n");
        }
        catch (IOException e) {
            System.err.println("無法讀取文件：" + filePath);
            e.printStackTrace();
            return;
        }
    }

    public String[] getContent() {
        return this.contentLines;
    }
}

class MermaidParser {
    private Map<String, String> outputReasult = new HashMap<String, String>();
    private Map<String, String> cmdTable = new HashMap<String, String>();

    public MermaidParser(String[] fileContent) {
        this.cmdTable.put("+", "public");
        this.cmdTable.put("-", "private");
        this.cmdTable.put("int", " {return 0;}");
        this.cmdTable.put("boolean", " {return false;}");
        this.cmdTable.put("String", " {return \"\";}");
        this.cmdTable.put("void", " {;}");
        this.parseToJava(fileContent);
    }
    
    private ArrayList<String> regexRetrieve(String s, String regex) {
        ArrayList<String> matchStrings = new ArrayList<String>();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(s);
        if (matcher.find()) {
            int groupIndex = 1;
            while (true) {
                try {
                    matchStrings.add(matcher.group(groupIndex++));
                }
                catch (java.lang.IndexOutOfBoundsException e) {
                    break;
                }
            }
        }
        return matchStrings;
    }

    private void parseToJava(String[] fileContent){
        String inClass = "";
        ArrayList<String> re;
        for (String line : fileContent) {
            line = line.trim();
            //not in block
            if(inClass == "") {
                re = this.regexRetrieve(line, "class (\\w+)");
                if (re.size() > 0) {
                    String className = re.get(0);
                    if(line.contains("{")) {
                        inClass = className;
                    }
                    if (outputReasult.containsKey(className) == false) {
                        outputReasult.put(className, "public class %s {".formatted(className));
                    }
                    continue;
                }

                re = this.regexRetrieve(line, "(\\w+) : ([+-]) *([\\w() ,\\[\\]]+) ([\\w\\[\\]]+(?=$))");
                if (re.size() == 0) {
                    re = this.regexRetrieve(line, "(\\w+) : ([+-]) *([\\w() ,\\[\\]]+)()");
                }
                if(re.size() == 0) {
                    continue;
                }
            }
            //in block
            else{
                if(line.contains("}")) {
                    inClass = "";
                    continue;
                }
                re = this.regexRetrieve(line, "([+-]) *([\\w() ,\\[\\]]+) ([\\w\\[\\]]+(?=$))");
                if (re.size() == 0) {
                    re = this.regexRetrieve(line, "([+-]) *([\\w() ,\\[\\]]+)()");
                }
                if(re.size() == 0) {
                    continue;
                }
            }
            String className = inClass;
            if (className == "") {
                className = re.get(0);
                re.remove(0);
            }
            String access = re.get(0);
            String type = re.get(1);
            String name = re.get(2);
            if (type.contains("(")) {
                type = (name == "") ? "void" : name;
                name = re.get(1);
            }
            if (outputReasult.containsKey(className) == false) {
                outputReasult.put(className, "public class %s {".formatted(className));
            }
            String content = outputReasult.get(className);
            outputReasult.put(className, content + this.parseStament(access, type, name));
        }
        for (Map.Entry<String, String> entry : this.outputReasult.entrySet()) {
            String className = entry.getKey();
            String content = entry.getValue();
            this.outputReasult.put(className, content+"\n}");
        }
    }

    public String parseStament(String access, String type, String name) {
        ArrayList<String> re;
        String parseResult = "\n    %s %s %s".formatted(
            this.cmdTable.get(access),
            type,
            name
        );
        if(name.contains("(")) {
            re = this.regexRetrieve(name, "^(get|set)([A-Z][\\w]*)([\\w() \\[\\]]+ ([\\w]+))*");
            if(re.size() > 0) {
                char c[] = re.get(1).toCharArray();
                c[0] = Character.toLowerCase(c[0]);
                String variableName = new String(c);
                if(re.get(0).equals("get")) {
                    parseResult += " {\n        return %s;\n    }".formatted(variableName);
                }
                else {
                    parseResult += " {\n        this.%s = %s;\n    }".formatted(
                        variableName,
                        re.get(3)
                    );
                }
            }
            else {
                parseResult += this.cmdTable.get(type);
            }
        }
        else {
            parseResult += ";";
        }
        return parseResult;
    }

    public Map<String, String> getOutput(){
        return outputReasult;
    }
}

public class CodeGenerator {
    public static void main(String[] args) {
        // 讀取文件
        if (args.length == 0) {
            System.err.println("請輸入檔案名稱");
            return;
        }
        String fileName = args[0];
        MermaidReader reader = new MermaidReader(fileName);
        MermaidParser parser = new MermaidParser(reader.getContent());
        
        // 寫入文件
        JavaWriter.writeFileByMap(parser.getOutput());
    }
}