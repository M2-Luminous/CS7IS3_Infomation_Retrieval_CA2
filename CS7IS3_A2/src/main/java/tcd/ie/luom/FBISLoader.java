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

public class FBISLoader implements DatasetLoader {

    public static final String DATASET_LOCATION = "dataset/fbis/";

    // Method to load and preprocess fbis documents
    @Override
    public List<Document> load(String pathToFedRegister) throws IOException {
        List<Document> fedRegisterDocList = new ArrayList<>();
        System.out.println("Loading FBIS ...");

        File[] fbis_files = new File(pathToFedRegister).listFiles();
        if (fbis_files == null) {
            throw new IOException("Dataset not found or is empty: " + pathToFedRegister);
        }

        for (File file : fbis_files) {

            org.jsoup.nodes.Document d = Jsoup.parse(file, null, "");

            Elements documents = d.select("DOC");

            for (Element document : documents) {
                // Remove unwanted tags from the document
                document.select("DATE1").remove();
                document.select("HT").remove();

                // Extract relevant fields
                String docno = document.select("DOCNO").text();
                String text = document.select("TEXT").text();
                String title = document.select("TI").text();

                // Add processed document to list
                fedRegisterDocList.add(createLuceneDocument(docno, text, title));
            }
        }
        System.out.println("Loading Fbis Done!");
        return fedRegisterDocList;
    }

    // Helper method to create a Lucene Document from parsed fields
    private Document createLuceneDocument(String docno, String text, String title) {
        Document doc = new Document();
        doc.add(new StringField("docno", docno, Field.Store.YES));
        doc.add(new TextField("text", text, Field.Store.YES));
        doc.add(new TextField("headline", title, Field.Store.YES));

        return doc;
    }
}
