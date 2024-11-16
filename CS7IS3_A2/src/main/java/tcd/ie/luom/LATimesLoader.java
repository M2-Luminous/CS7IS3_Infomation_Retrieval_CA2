package tcd.ie.luom;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

public class LATimesLoader implements DatasetLoader  {

    // Removed the IndexWriter writer field and constructor argument

    public LATimesLoader() {
        // No-argument constructor
    }

    @Override
    public List<Document> load(String pathToFedRegister) throws IOException {
        System.out.println("Loading LATimes ...");
		List<Document> ret = new ArrayList<>();

		// List all files in the specified directory (not just directories)
		File[] files = new File(pathToFedRegister).listFiles(File::isFile);

		if (files == null || files.length == 0) {
			System.out.println("No files found in path: " + pathToFedRegister);
		} else {
			//System.out.println(files.length + " files found in path: " + pathToFedRegister);
			for (File file : files) {
				ret.addAll(parseDocument(file));
			}
		}
		System.out.println("Loading LATimes Done!");
		return ret;
	}

    public List<Document> parseDocument(File file) throws IOException {
        // Parse the HTML content using JSoup
        org.jsoup.nodes.Document htmlDoc = Jsoup.parse(file, "UTF-8");

	List<Document> ret = new ArrayList<>();
        Elements docs = htmlDoc.select("DOC");
        for (Element doc : docs) {
            Document luceneDoc = new Document();

            // Extracting key fields and adding to Lucene Document
            addField(luceneDoc, "docno", doc.select("DOCNO").text(), true);
            addField(luceneDoc, "docid", doc.select("DOCID").text(), true);
            addField(luceneDoc, "date", doc.select("DATE P").text(), true);
            addField(luceneDoc, "section", doc.select("SECTION P").text(), true);
            addField(luceneDoc, "headline", doc.select("HEADLINE P").text(), false);
            addField(luceneDoc, "byline", doc.select("BYLINE P").text(), false);
            addField(luceneDoc, "text", doc.select("TEXT P").text(), false);

	    // Add the document to the index
            ret.add(luceneDoc);
        }
	return ret;
    }

    private void addField(Document doc, String name, String value, boolean isKeyword) {
        if (value != null && !value.isEmpty()) {
            Field field = isKeyword ? new StringField(name, value, Field.Store.YES) :
                    new TextField(name, value, Field.Store.YES);
            doc.add(field);
        }
    }
}
