package tcd.ie.luom;

import tcd.ie.luom.FR94Loader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.ConcurrentMergeScheduler;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class IndexProgram {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexProgram.class);
    private static final String INDEX_LOCATION = "Index/FR94Index";
    private static final String DOCUMENTS_LOCATION = "dataset/fr94/";

    public static void main(String[] args) {
        try {
            // Initialize analyzer and similarity
            Analyzer analyzer = new EnglishAnalyzer();
            BM25Similarity similarity = new BM25Similarity();

            // Create index directory
            File indexDir = new File(INDEX_LOCATION);
            if (indexDir.exists()) {
                FileUtils.deleteDirectory(indexDir); // Clean up old index
            }
            Directory dir = FSDirectory.open(Paths.get(INDEX_LOCATION));

            // Configure IndexWriter
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            config.setOpenMode(OpenMode.CREATE);
            config.setSimilarity(similarity);
            config.setMergeScheduler(new ConcurrentMergeScheduler());

            IndexWriter indexWriter = new IndexWriter(dir, config);

            // Load documents using FR94Loader
            FR94Loader loader = new FR94Loader();
            List<Document> documents = loader.load(DOCUMENTS_LOCATION);

            // Index documents
            if (!documents.isEmpty()) {
                indexWriter.addDocuments(documents);
                LOGGER.info("Successfully indexed {} documents", documents.size());
            } else {
                LOGGER.warn("No documents found to index.");
            }

            // Close the index writer
            indexWriter.close();
            LOGGER.info("Indexing completed successfully.");

        } catch (IOException e) {
            LOGGER.error("Error during indexing", e);
        }
    }
}
