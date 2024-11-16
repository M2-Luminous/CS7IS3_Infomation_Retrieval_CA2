package tcd.ie.luom;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import tcd.ie.luom.TopicsParser.TopicsParser;
import tcd.ie.luom.TopicsParser.Topic;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Query;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.BooleanClause;
import java.util.Map;
import java.util.HashMap;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.io.PrintWriter;



public class Searcher {
    private static final String RESULT_LOCATION = "result/";
    private static final String INDEX_LOCATION = "Index";
    private static final String FR94_LOCATION = "dataset/fr94/";
    private static final String FBIS_LOCATION = "dataset/fbis/";
    private static final String LATIMES_LOCATION = "dataset/latimes/";
    private static final String FT_LOCATION = "dataset/ft/";
    private static int MAX_RESULTS = 1000;

    public static void main(String[] args) {
        try {
            BM25Similarity similarity = new BM25Similarity();
            EnglishAnalyzer analyzer = new EnglishAnalyzer();
            File resultDir = new File(RESULT_LOCATION);
            if (!resultDir.exists()){
                resultDir.mkdirs();
            }

            executeQueries(analyzer, similarity);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void executeQueries(Analyzer analyzer, BM25Similarity similarity) throws IOException {
        try {
            Directory dir = FSDirectory.open(Paths.get(INDEX_LOCATION));
            IndexReader indexReader = DirectoryReader.open(dir);
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);
            indexSearcher.setSimilarity(similarity);
            Map<String, Float> boost = new HashMap<>();
            TopicsParser topicParser = new TopicsParser();
            List<Topic> topics = topicParser.parse("dataset/topics");
            QueryParser parser = new MultiFieldQueryParser(new String[]{"headline", "text"}, analyzer, boost);
            PrintWriter writer = new PrintWriter(new File(RESULT_LOCATION + "result.txt"), "UTF-8");
            for (Topic topic : topics) {
                BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
	            Query title = parser.parse(topic.getTitle());
                booleanQuery.add(new BoostQuery(title, 4f), BooleanClause.Occur.SHOULD);
	            Query description = parser.parse(topic.getDesc());
                booleanQuery.add(new BoostQuery(description, 2f), BooleanClause.Occur.SHOULD);
	            String narrative = topic.getNarr();
                BreakIterator iterator = BreakIterator.getSentenceInstance();
                iterator.setText(narrative);
                int index = 0;
                while (iterator.next() != BreakIterator.DONE) {
                    String sentence = narrative.substring(index, iterator.current());
                    if (sentence.length() > 0) {
                        Query narrativeQuery = parser.parse(parser.escape(sentence));
                        if (!sentence.contains("not relevant") && !sentence.contains("irrelevant")) {
                            booleanQuery.add(new BoostQuery(narrativeQuery, 1.4f), BooleanClause.Occur.SHOULD);
                        } else {
                            booleanQuery.add(new BoostQuery(narrativeQuery, 2f), BooleanClause.Occur.FILTER);
                        }
                    }
                index = iterator.current();
                }

                ScoreDoc[] hits = indexSearcher.search(booleanQuery.build(), MAX_RESULTS).scoreDocs;

		        for (int i = 0; i < hits.length; i++)
		        {
		        	writer.println(topic.getNum() + " 0 " + indexSearcher.doc(hits[i].doc).get("docno") + " " + i + " " + hits[i].score + " STANDARD");
		        }
            }
            writer.close();
            indexReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }  
    }
}