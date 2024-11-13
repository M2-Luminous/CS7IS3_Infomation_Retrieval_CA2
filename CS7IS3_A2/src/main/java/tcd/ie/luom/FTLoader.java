package tcd.ie.luom;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FTLoader implements DatasetLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(FTLoader.class);

    public FTLoader() {
        // No-argument constructor
    }

    @Override
    public List<Document> load(String pathToFT) throws IOException {
        System.out.println("Loading Financial Times...");
        List<Document> documents = new ArrayList<>();

        File ftDir = new File(pathToFT);
        if (!ftDir.exists()) {
            throw new IOException("Dataset directory not found: " + pathToFT);
        }

        // Process all subdirectories
        File[] subdirs = ftDir.listFiles(File::isDirectory);
        if (subdirs != null) {
            for (File subdir : subdirs) {
                // LOGGER.info("Processing directory: {}", subdir.getName());
                File[] files = subdir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isFile() && !file.getName().startsWith("!")) {
                            try {
                                documents.addAll(parseDocument(file));
                            } catch (Exception e) {
                                LOGGER.error("Error processing file: " + file.getName(), e);
                            }
                        }
                    }
                }
            }
        }

        System.out.println("Loading Financial Times Done! Loaded " + documents.size() + " documents");
        return documents;
    }

    private List<Document> parseDocument(File file) throws IOException {
        List<Document> documents = new ArrayList<>();

        // Parse the file using JSoup
        org.jsoup.nodes.Document htmlDoc = Jsoup.parse(file, "UTF-8");
        Elements docs = htmlDoc.select("DOC");

        for (Element doc : docs) {
            Document luceneDoc = new Document();

            // Extract and add required fields
            // Using StringField for fields that shouldn't be tokenized
            addField(luceneDoc, "DOCNO", doc.select("DOCNO").text(), true);
            addField(luceneDoc, "PROFILE", doc.select("PROFILE").text(), true);

            // Parse and format the date
            String dateText = doc.select("DATE").text().trim();
            addField(luceneDoc, "DATE", dateText, true);

            // Using TextField for fields that should be tokenized
            addField(luceneDoc, "HEADLINE", doc.select("HEADLINE").text(), false);
            addField(luceneDoc, "BYLINE", doc.select("BYLINE").text(), false);
            addField(luceneDoc, "DATELINE", doc.select("DATELINE").text(), false);
            addField(luceneDoc, "TEXT", doc.select("TEXT").text(), false);

            // Publication information
            addField(luceneDoc, "PUB", doc.select("PUB").text(), true);
            addField(luceneDoc, "PAGE", doc.select("PAGE").text(), true);

            documents.add(luceneDoc);
        }

        return documents;
    }

    private void addField(Document doc, String name, String value, boolean isKeyword) {
        if (value != null && !value.isEmpty()) {
            Field field = isKeyword
                    ? new StringField(name, value, Field.Store.YES)
                    : new TextField(name, value, Field.Store.YES);
            doc.add(field);
        }
    }
}
