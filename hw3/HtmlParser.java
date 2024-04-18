import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
import java.util.*;
import java.io.IOException;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;

class StockData {
    private int date;
    private ArrayList<String> stockList;
    private ArrayList<Double> prices;

    public StockData(int date, ArrayList<String> stockList, ArrayList<Double> prices) {
        this.date = date;
        this.stockList = stockList;
        this.prices = prices;
    }

    public int getDate() {
        return this.date;
    }
    
    public ArrayList<String> getStockList() {
        return this.stockList;
    }

    public ArrayList<Double> getPrices() {
        return this.prices;
    }
}

class CsvTable {
    private ArrayList<ArrayList<String>> dataTable = new ArrayList<ArrayList<String>>();

    public void appendRow(Object... rowDatas) {
        ArrayList<String> newRow = new ArrayList<String>();
        DecimalFormat df = new DecimalFormat("#.##");
        for (Object data : rowDatas) {
            if (data instanceof Double) {
                newRow.add(df.format(Double.parseDouble(data.toString())));
            }
            else {
                newRow.add(data.toString());
            }
        }
        this.dataTable.add(newRow);
    }

    public void appendData(Object... datas) {
        DecimalFormat df = new DecimalFormat("#.##");
        ArrayList<String> row = dataTable.get(dataTable.size()-1);
        for (Object data : datas) {
            if (data instanceof Double) {
                row.add(df.format(Double.parseDouble(data.toString())));
            }
            else {
                row.add(data.toString());
            }
        }
        dataTable.set(dataTable.size() - 1, row);
    }

    public void createNewRow() {
        this.dataTable.add(new ArrayList<String>());
    }

    public ArrayList<ArrayList<String>> getTable() {
        return this.dataTable;
    }

    public ArrayList<String> parseToCsvLines() {
        ArrayList<String> csvLines = new ArrayList<String>();
        for (ArrayList<String> row : dataTable) {
            String line = "";
            for (String data : row) {
                line += "%s,".formatted(data);
            }
            if (line == "") {
                csvLines.add("");
            }
            else {
                csvLines.add(line.substring(0, line.length() - 1));
            }
        }
        return csvLines;
    }
}

class StockCrawler {
    static final private String URL = "https://pd2-hw3.netdb.csie.ncku.edu.tw/";

    public static StockData crawlStockData() {
        Document doc;
        try {
            doc = Jsoup.connect(URL).get();
        }
        catch (IOException e) {
            return null;
        }
        Elements stockNames = doc.select("th");
        Elements stockPrices = doc.select("td");
        ArrayList<String> names = new ArrayList<String>();
        ArrayList<Double> prices = new ArrayList<Double>();
        for (int i = 0; i < stockNames.size(); i++) {
            names.add(stockNames.get(i).text());
            prices.add(Double.parseDouble(stockPrices.get(i).text()));
        }
        StockData returnData = new StockData(Integer.parseInt(doc.title().substring(3)), names, prices);
        return returnData;
    }
}

class CsvDataParser {
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

    public static void writeFile(String filePath, String fileContent) {
        writeFile(filePath, fileContent, false);
    }

    public static String readFile(String filePath) {
        String fileContent = null;
        try {
            fileContent = Files.readString(Paths.get(filePath));
        }
        catch (IOException e) {
            ;
        }
        return fileContent;
    }
    
    public static ArrayList<String> readAsLines(String filePath) {
        String fileContent = readFile(filePath);
        if (fileContent != null) {
            return new ArrayList<String>(Arrays.asList(fileContent.split("\n")));
        }
        return new ArrayList<String>();
    }

    public static ArrayList<StockData> readAsStockDataList(String filePath) {
        ArrayList<StockData> sdList = new ArrayList<StockData>();
        ArrayList<String> csvLines = readAsLines(filePath);
        if (csvLines.size() != 0) {
            ArrayList<String> stockList = new ArrayList<String>(Arrays.asList(csvLines.get(0).split(",")));
            for (int i = 1; i < 31; i++) {
                String[] priceDatas = csvLines.get(i).split(",");
                ArrayList<Double> priceList = new ArrayList<Double>();
                for (String price : priceDatas) {
                    if (price.equals("null")) {
                        priceList.add(null);
                    }
                    else {
                        priceList.add(Double.parseDouble(price));
                    }            
                }
                sdList.add(new StockData(i, stockList, priceList));
            }
        }
        return sdList;
    }

    public static void writeByLines(String filePath, ArrayList<String> csvLines, boolean appendMode) {
        String fileContent = "";
        for (String line : csvLines) {
            fileContent += line + "\n";
        }
        writeFile(filePath, fileContent, appendMode);
    }

    public static void addStockData(String filePath, StockData sd) {
        ArrayList<String> csvLines = readAsLines(filePath);
        if (csvLines.size() == 0) {
            String stockRow = "";
            for (String stockName : sd.getStockList()) {
                stockRow += stockName + ",";
            }
            stockRow = stockRow.substring(0, stockRow.length() - 1);
            csvLines.add(stockRow);
            String nullLine = stockRow.replaceAll("\\w+", "null");
            for (int i = 0; i < 30; i++) {
                csvLines.add(nullLine);
            }
        }
        int dataIndex = sd.getDate();
        String dataContent = "";
        for (Double price : sd.getPrices()) {
            dataContent += "%.2f,".formatted(price);
        }
        dataContent = dataContent.substring(0, dataContent.length() - 1);
        csvLines.set(dataIndex, dataContent);
        writeByLines(filePath, csvLines, false);
    }
    
    public static ArrayList<String> getStockList(String filePath) {
        return new ArrayList<String>(Arrays.asList(readAsLines(filePath).get(0).split(",")));
    }

    public static ArrayList<Double> getStockPrice(String filePath, String StockName, int startDate, int endDate) {
        ArrayList<StockData> sdList = readAsStockDataList(filePath);
        ArrayList<Double> stockPrices = new ArrayList<Double>();
        int column = sdList.get(startDate).getStockList().indexOf(StockName);
        for (int i = startDate; i <= endDate; i++) {
            stockPrices.add(sdList.get(i - 1).getPrices().get(column));
        }
        return stockPrices;
    }
}

class myMath {
    public static double pow(double x, int n) {
        double result = 1;
        for (int i = 0; i < n; i++) {
            result = result * x;
        }
        return result;
    }

    public static double round(double x, int n) {
        x = (double)(int) (x * pow(10, n + 1));
        x += ((int) x % 10) / 5 * 10;
        return (x - (x % 10)) / (pow(10, n + 1));
    }

    public static double abs(double x) {
        if (x < 0) {
            return x * -1;
        }
        return x;
    }

    public static double avg(Object... nums) {
        double sum = 0;
        for (Object num : nums) {
            sum += ((Number) num).doubleValue();
        }
        return sum / nums.length;
    }

    public static double sqrt(double x) {
        double xhalf = 0.5d * x;
        long xbit = Double.doubleToLongBits(x);
        xbit = 0x5fe6ec85e7de30daL - (xbit >> 1);
        x = Double.longBitsToDouble(xbit);
        for (int i = 0; i < 10; i++) {
            x *= (1.5d - xhalf * x * x);
        }
        return round(1 / x, 2);
    }

    public static double standardDeviation(ArrayList<Double> datas) {
        double dataAvg = avg(datas.toArray());
        double sum = 0;
        for (double num : datas) {
            sum += pow(num - dataAvg, 2);
        }
        return sqrt(sum / (datas.size() - 1));
    }
}

class LinearRegression extends myMath{
    private ArrayList<Double> prices;
    private ArrayList<Integer> times;
    private double avgPrice;
    private double avgTime;
    public LinearRegression(ArrayList<Double> prices, int startTime, int endTime) {
        this.prices = prices;
        this.times = new ArrayList<Integer>();
        for (int i = startTime; i <= endTime; i++) {
            this.times.add(i);
        }
        this.avgPrice = avg(prices.toArray());
        this.avgTime = avg(this.times.toArray());
    }

    public double getSlope() {
        double a = 0;
        double b = 0;
        for (int i = 0; i < this.times.size(); i++) {
            a += (this.times.get(i) - this.avgTime) * (this.prices.get(i) - this.avgPrice);
            b += pow(this.times.get(i) - this.avgTime, 2);
        }
        return a / b;
    }

    public double getIntercept() {
        return this.avgPrice - getSlope() * this.avgTime;
    }
}

public class HtmlParser {
    public static void main(String[] args) {
        if (args[0].equals("0")) {
            StockData sd = StockCrawler.crawlStockData();
            CsvDataParser.addStockData("data.csv", sd);
        }
        else if (args[0].equals("1")){
            if (args[1].equals("0")) {
                ArrayList<String> csvLines = CsvDataParser.readAsLines("data.csv");
                ArrayList<String> outputData = new ArrayList<String>();
                for (String line : csvLines) {
                    if (line.startsWith("null") == false) {
                        outputData.add(line);
                    }
                }
                CsvDataParser.writeByLines("output.csv", outputData, false);
            }
            else {
                String stockName = args[2];
                int startDate = Integer.parseInt(args[3]);
                int endDate = Integer.parseInt(args[4]);
                CsvTable table = new CsvTable();

                if (args[1].equals("3")) {
                    ArrayList<StockData> sdList = CsvDataParser.readAsStockDataList("data.csv");
                    ArrayList<String> stockNames = sdList.get(0).getStockList();
                    Map<String, Double> stockStdDevMap = new HashMap<String, Double>();
                    for (int i = 0; i < stockNames.size(); i++) {
                        ArrayList<Double> prices = new ArrayList<Double>();
                        for (int j = startDate; j <= endDate; j++) {
                            prices.add(sdList.get(j - 1).getPrices().get(i));
                        }
                        stockStdDevMap.put(stockNames.get(i), myMath.round(myMath.standardDeviation(prices), 2));
                    }
                    List<Map.Entry<String, Double>> list = new ArrayList<>(stockStdDevMap.entrySet());
                    Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
                        @Override
                        public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                            return Double.compare(o2.getValue(), o1.getValue());
                        }
                    });
                    table.createNewRow();
                    ArrayList<Double> top3StdDev = new ArrayList<Double>();
                    int i = 0;
                    for (Map.Entry<String, Double> entry : list) {
                        if (i == 3) {
                            break;
                        }
                        table.appendData(entry.getKey());
                        top3StdDev.add(entry.getValue());
                        i++;
                    }
                    table.appendData(startDate, endDate);
                    table.appendRow(top3StdDev.toArray());
                    CsvDataParser.writeByLines("output.csv", table.parseToCsvLines(), true);
                }
                else {
                    table.appendRow(stockName, startDate, endDate);
                    ArrayList<Double> prices = CsvDataParser.getStockPrice("data.csv", stockName, startDate, endDate);
                    if (args[1].equals("1")) {
                        table.createNewRow();
                        for (int i = 0; i < endDate - (3 + startDate); i++) {
                            double priceSum = 0;
                            for (int j = i; j < i + 5; j++) {
                                priceSum += prices.get(j);
                            }
                            table.appendData(priceSum / 5.0);
                        }
                    }
                    else if (args[1].equals("2")) {
                        table.appendRow(myMath.standardDeviation(prices));
                    }
                    else if (args[1].equals("4")) {
                        LinearRegression LR = new LinearRegression(prices, startDate, endDate);
                        table.appendRow(LR.getSlope(), LR.getIntercept());
                    }
                    CsvDataParser.writeByLines("output.csv", table.parseToCsvLines(), true);
                }
            }
        }
    }
}
