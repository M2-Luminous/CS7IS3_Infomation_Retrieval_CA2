package tcd.ie.luom;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.similarities.*;
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

    private static final String INDEX_LOCATION = "Index";
    private static final int MAX_RESULTS = 1000;

    public static void searchWithConfig(String analyzerChoice, Similarity similarityModel, String resultFileName) {
        try {
            Analyzer analyzer = getAnalyzer(analyzerChoice);

            try (Directory dir = FSDirectory.open(Paths.get(INDEX_LOCATION));
                 IndexReader indexReader = DirectoryReader.open(dir);
                 PrintWriter writer = new PrintWriter(new File(resultFileName), "UTF-8")) {

                IndexSearcher indexSearcher = new IndexSearcher(indexReader);
                indexSearcher.setSimilarity(similarityModel);

                TopicsParser topicParser = new TopicsParser();
                List<Topic> topics = topicParser.parse("dataset/topics");

                Map<String, Float> fieldBoosts = createFieldBoostMap();
                QueryParser parser = new MultiFieldQueryParser(new String[]{"headline", "text"}, analyzer, fieldBoosts);

                for (Topic topic : topics) {
                    BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();

                    // Boost title
                    if (!topic.getTitle().isEmpty()) {
                        Query title = parser.parse(QueryParser.escape(topic.getTitle()));
                        booleanQuery.add(new BoostQuery(title, 4.0f), BooleanClause.Occur.SHOULD);
                    }

                    // Boost description
                    if (!topic.getDesc().isEmpty()) {
                        Query description = parser.parse(QueryParser.escape(topic.getDesc()));
                        booleanQuery.add(new BoostQuery(description, 1.7f), BooleanClause.Occur.SHOULD);
                    }

                    // Process narrative
                    List<String> narrativeParts = splitNarrative(topic.getNarr());
                    if (!narrativeParts.get(0).isEmpty()) {
                        Query relevantNarrative = parser.parse(QueryParser.escape(narrativeParts.get(0)));
                        booleanQuery.add(new BoostQuery(relevantNarrative, 1.2f), BooleanClause.Occur.SHOULD);
                    }
                    if (!narrativeParts.get(1).isEmpty()) {
                        Query irrelevantNarrative = parser.parse(QueryParser.escape(narrativeParts.get(1)));
                        booleanQuery.add(new BoostQuery(irrelevantNarrative, 2.0f), BooleanClause.Occur.FILTER);
                    }

                    // Execute search
                    ScoreDoc[] hits = indexSearcher.search(booleanQuery.build(), MAX_RESULTS).scoreDocs;

                    for (int i = 0; i < hits.length; i++) {
                        Document doc = indexSearcher.doc(hits[i].doc);
                        writer.printf("%s Q0 %s %d %f STANDARD%n",
                                topic.getNum(),
                                doc.get("docno"),
                                (i + 1),
                                hits[i].score);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Analyzer getAnalyzer(String analyzerChoice) {
        return "en".equals(analyzerChoice) ? new EnglishAnalyzer() : new StandardAnalyzer();
    }

    private static Map<String, Float> createFieldBoostMap() {
        Map<String, Float> boostMap = new HashMap<>();
        boostMap.put("headline", 0.08f);
        boostMap.put("text", 0.92f);
        return boostMap;
    }

    private static List<String> splitNarrative(String narrative) {
        StringBuilder relevantNarr = new StringBuilder();
        StringBuilder irrelevantNarr = new StringBuilder();
        List<String> splitParts = new ArrayList<>();

        BreakIterator bi = BreakIterator.getSentenceInstance();
        bi.setText(narrative);
        int index = 0;

        while (bi.next() != BreakIterator.DONE) {
            String sentence = narrative.substring(index, bi.current()).trim();
            if (sentence.contains("not relevant") || sentence.contains("irrelevant")) {
                irrelevantNarr.append(sentence.replaceAll("not|NOT|irrelevant", "")).append(" ");
            } else {
                relevantNarr.append(sentence).append(" ");
            }
            index = bi.current();
        }

        splitParts.add(relevantNarr.toString().trim());
        splitParts.add(irrelevantNarr.toString().trim());
        return splitParts;
    }
}
