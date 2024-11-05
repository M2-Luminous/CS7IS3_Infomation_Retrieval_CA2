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

public class FR94Loader {

    public static final String DATASET_LOCATION = "dataset/fr94/";

    // Method to load and preprocess Federal Register documents
    public List<Document> load(String pathToFedRegister) throws IOException {
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

                    document.select("ADDRESS").remove();
                    document.select("SIGNER").remove();
                    document.select("SIGNJOB").remove();
                    document.select("BILLING").remove();
                    document.select("FRFILING").remove();
                    document.select("DATE").remove();
                    document.select("RINDOCK").remove();

                    String docno = document.select("DOCNO").text();
                    String text = document.select("TEXT").text();
                    String title = document.select("DOCTITLE").text();

                    // New fields based on additional tags
                    String supplem = document.select("SUPPLEM").text();
                    String summary = document.select("SUMMARY").text();
                    String further = document.select("FURTHER").text();
                    String USdept = document.select("USDEPT").text();
                    String agency = document.select("AGENCY").text();
                    String USBureau = document.select("USBUREAU").text();

                    fedRegisterDocList.add(createLuceneDocument(docno, text, title, supplem, summary, further, USdept, agency, USBureau));
                }
            }
        }
        System.out.println("Loading FR94 Done!");
        return fedRegisterDocList;
    }

    // Helper method to create a Lucene Document from parsed fields
    private Document createLuceneDocument(String docno, String text, String title, String supplem, String summary, String further, String USdept, String agency, String USBureau) {
        Document doc = new Document();
        doc.add(new StringField("docno", docno, Field.Store.YES));
        doc.add(new TextField("text", text, Field.Store.YES));
        doc.add(new TextField("headline", title, Field.Store.YES));

        // Adding new fields
        doc.add(new TextField("summary", supplem, Field.Store.YES));
        doc.add(new TextField("summary", summary, Field.Store.YES));
        doc.add(new TextField("further", further, Field.Store.YES));
        doc.add(new TextField("USdept", USdept, Field.Store.YES));
        doc.add(new StringField("agency", agency, Field.Store.YES));
        doc.add(new TextField("USBureau", USBureau, Field.Store.YES));

        return doc;
    }
}
