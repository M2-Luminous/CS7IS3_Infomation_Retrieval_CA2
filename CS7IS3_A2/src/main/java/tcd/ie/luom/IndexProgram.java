package tcd.ie.luom;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tcd.ie.luom.FR94Loader;
import tcd.ie.luom.FBISLoader;
import tcd.ie.luom.LATimesLoader;
import tcd.ie.luom.FTLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class IndexProgram {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexProgram.class);
    private static final String INDEX_LOCATION = "Index";
    private static final String FR94_LOCATION = "dataset/fr94/";
    private static final String FBIS_LOCATION = "dataset/fbis/";
    private static final String LATIMES_LOCATION = "dataset/latimes/";
    private static final String FT_LOCATION = "dataset/ft/";

    public static void main(String[] args) {
        try {
            Analyzer analyzer = new EnglishAnalyzer();
            BM25Similarity similarity = new BM25Similarity();

            // Clean up old index if it exists
            File indexDir = new File(INDEX_LOCATION);
            if (indexDir.exists()) {
                org.apache.commons.io.FileUtils.deleteDirectory(indexDir); // Clean up old index
            }

            // Initialize indexing for each dataset sequentially with batching
            indexDataset(FT_LOCATION, new FTLoader(), analyzer, similarity, true);
            indexDataset(LATIMES_LOCATION, new LATimesLoader(), analyzer, similarity, false);
            indexDataset(FR94_LOCATION, new FR94Loader(), analyzer, similarity, false);
            indexDataset(FBIS_LOCATION, new FBISLoader(), analyzer, similarity, false);

            LOGGER.info("Indexing completed successfully for all datasets.");

        } catch (IOException e) {
            LOGGER.error("Error during indexing", e);
        }
    }

    private static void indexDataset(String datasetLocation, DatasetLoader loader, Analyzer analyzer, BM25Similarity similarity, boolean isFirstDataset) throws IOException {
        LOGGER.info("Starting to index dataset at location: {}", datasetLocation);

        OpenMode openMode = isFirstDataset ? OpenMode.CREATE : OpenMode.APPEND;
        int batchSize = 50000;

        try (Directory dir = FSDirectory.open(Paths.get(INDEX_LOCATION))) {
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            config.setOpenMode(openMode);
            config.setSimilarity(similarity);

            try (IndexWriter indexWriter = new IndexWriter(dir, config)) {
                List<Document> batch = new ArrayList<>(batchSize);

                // Load documents from loader
                List<Document> documents = loader.load(datasetLocation);
                for (Document doc : documents) {
                    batch.add(doc);

                    // When batch is full, index and commit it, then clear the batch to release memory
                    if (batch.size() >= batchSize) {
                        indexWriter.addDocuments(batch);
                        indexWriter.commit();  // Commit the batch to free memory
                        // LOGGER.info("Indexed batch of {} documents from {}", batch.size(), datasetLocation);
                        batch.clear();  // Clear batch for next chunk
                    }
                }

                // Commit any remaining documents in the batch
                if (!batch.isEmpty()) {
                    indexWriter.addDocuments(batch);
                    indexWriter.commit();
                    // LOGGER.info("Indexed final batch of {} documents from {}", batch.size(), datasetLocation);
                }

                LOGGER.info("Successfully indexed documents from {}", datasetLocation);
            }
        }
    }
}

// DatasetLoader interface remains unchanged
interface DatasetLoader {
    List<Document> load(String pathToDataset) throws IOException;
}
