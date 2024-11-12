package tcd.ie.luom;

import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import tcd.ie.luom.TopicsParser.TopicsParser;
import tcd.ie.luom.TopicsParser.Topic;

import java.util.List;
import java.io.File;
import java.nio.file.Paths;

public class TestParser {
    public static void main(String[] args) {
        try {
            // Step 1: Initialize EnglishAnalyzer and RAMDirectory
            // EnglishAnalyzer analyzer = new EnglishAnalyzer();
	    TopicsParser parser = new TopicsParser();
            List<Topic> topics = parser.parse("/opt/proj/proj/topics");

	    System.out.println("Number: " + topics.get(0).getNum());
	    System.out.println("Title: " + topics.get(0).getTitle());
	    System.out.println("Description: " + topics.get(0).getDesc());
	    System.out.println("Narrative: " + topics.get(0).getNarr());

            
            System.out.println("Parsing topics successfully.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

