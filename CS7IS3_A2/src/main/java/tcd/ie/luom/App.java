package tcd.ie.luom;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class App {

    // Constants for dataset location
    public static final String DATASET_LOCATION = "dataset/fr94/";

    public static void main(String[] args) {
        try {
            // Load and preprocess the documents
            List<Document> documents = loadFR94Docs(DATASET_LOCATION);

            // Output success message and basic statistics
            if (documents.isEmpty()) {
                System.err.println("No documents were loaded. Please check the dataset path.");
            } else {
                System.out.println("Dataset successfully loaded and preprocessed.");
                System.out.println("Total Documents Loaded: " + documents.size());
                System.out.println("Average Text Length: " + calculateAverageTextLength(documents));
            }

        } catch (IOException e) {
            System.err.println("Error loading dataset: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method to load and preprocess Federal Register documents
    public static List<Document> loadFR94Docs(String pathToFedRegister) throws IOException {
        List<Document> fedRegisterDocList = new ArrayList<>();
        System.out.println("Loading FR94 ...");

        File[] directories = new File(pathToFedRegister).listFiles(File::isDirectory);
        if (directories == null) {
            throw new IOException("Dataset directory not found or is empty: " + pathToFedRegister);
        }

        for (File directory : directories) {
            File[] files = directory.listFiles();
            if (files == null) continue;

            for (File file : files) {
                org.jsoup.nodes.Document d = Jsoup.parse(file, null, "");

                Elements documents = d.select("DOC");

                for (Element document : documents) {

                    // Remove unwanted tags from the document
                    document.select("ADDRESS").remove();
                    document.select("SIGNER").remove();
                    document.select("SIGNJOB").remove();
                    document.select("BILLING").remove();
                    document.select("FRFILING").remove();
                    document.select("DATE").remove();
                    document.select("RINDOCK").remove();

                    // Extract relevant fields
                    String docno = document.select("DOCNO").text();
                    String text = document.select("TEXT").text();
                    String title = document.select("DOCTITLE").text();

                    // Add processed document to list
                    fedRegisterDocList.add(createLuceneDocument(docno, text, title));
                }
            }
        }
        System.out.println("Loading FR94 Done!");
        return fedRegisterDocList;
    }

    // Helper method to create a Lucene Document from parsed fields
    private static Document createLuceneDocument(String docno, String text, String title) {
        Document doc = new Document();
        doc.add(new StringField("docno", docno, Field.Store.YES));
        doc.add(new TextField("text", text, Field.Store.YES));
        doc.add(new TextField("headline", title, Field.Store.YES));
        return doc;
    }

    // Helper method to calculate average text length
    private static double calculateAverageTextLength(List<Document> docs) {
        return docs.stream()
                .mapToInt(doc -> doc.get("text").length())
                .average()
                .orElse(0.0);
    }
}
