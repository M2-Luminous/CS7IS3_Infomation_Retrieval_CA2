package tcd.ie.luom;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class App {

    // Directly include the constants here
    public static final String PARSED_DOCUMENT_LOCATION = "parsed-documents/fr94_docs/"; // Matches Fr94Parser
    public static final String DATASET_LOCATION = "dataset/fr94/";

    public static void main(String[] args) throws IOException {
        
        // Create the base directory for parsed documents if it does not exist
        File baseDir = new File(PARSED_DOCUMENT_LOCATION);
        baseDir.mkdirs();

        // List all files in the dataset directory
        File[] file = new File(DATASET_LOCATION).listFiles();
        
        // Check if directory exists to avoid NullPointerException
        if (file == null) {
            System.err.println("Dataset directory not found: " + DATASET_LOCATION);
            return;
        }
        
        ArrayList<String> files1 = new ArrayList<>();
        
        // Add all files from each subdirectory in 'dataset/fr94/'
        for (File files : file) {
            if (files.isDirectory()) {
                System.out.println("Directory: " + files.getPath());
                for (File f : files.listFiles()) {
                    files1.add(f.getAbsolutePath());
                }
            }
        }

        // Parse each file and write content to a new document in the parsed directory
        for (String f : files1) {
            try {
                System.out.println("Processing file: " + f);
                
                File input = new File(f);
                Document doc = Jsoup.parse(input, "UTF-8", "");

                // Remove 'docid' elements
                doc.select("docid").remove();

                // Select 'doc' elements for processing
                Elements docs = doc.select("doc");

                for (Element e : docs) {
                    // Extract the 'Docno' tag content
                    String DocNo = e.getElementsByTag("Docno").text();

                    // Create a new file for each document based on 'Docno'
                    File result = new File(PARSED_DOCUMENT_LOCATION + DocNo);
                    PrintWriter writer = new PrintWriter(result, "UTF-8");
                    writer.println(e.text());
                    writer.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
