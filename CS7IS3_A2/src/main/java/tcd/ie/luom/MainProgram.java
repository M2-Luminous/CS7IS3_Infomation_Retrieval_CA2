package tcd.ie.luom;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.search.similarities.*;

import java.util.Scanner;

public class MainProgram {

    private static final String RESULT_LOCATION = "result/";

    public static void main(String[] args) {
        try {
            // Get user choices for analyzer and similarity model
            String selectedAnalyzer = chooseAnalyzer();
            int similarityModelChoice = chooseSimilarityModel();
            Similarity similarityModel = getSimilarityModel(similarityModelChoice);

            // Update result file name based on choices
            String resultFileName = RESULT_LOCATION + "result-" + selectedAnalyzer + "-" + getSimilarityModelName(similarityModelChoice) + ".txt";

            System.out.println("Indexing with " + selectedAnalyzer + " analyzer and " + getSimilarityModelName(similarityModelChoice) + " similarity...");
            IndexProgram.indexDatasets(selectedAnalyzer, similarityModel);

            System.out.println("Searching with " + selectedAnalyzer + " analyzer and " + getSimilarityModelName(similarityModelChoice) + " similarity...");
            Searcher.searchWithConfig(selectedAnalyzer, similarityModel, resultFileName);

            System.out.println("Execution completed! Results are stored in " + resultFileName);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String chooseAnalyzer() {
        System.out.println("Please select the type of Analyzer:\n1. Standard Analyzer\n2. English Analyzer");
        int userChoice = new Scanner(System.in).nextInt();
        return userChoice == 1 ? "sd" : "en";
    }

    public static int chooseSimilarityModel() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please select the type of Similarity Model:\n1. BM25\n2. Classic (VSM)\n3. Boolean\n4. LMDirichlet\n5. IBS");
        return scanner.nextInt();
    }

    public static Similarity getSimilarityModel(int similarityModel) {
        switch (similarityModel) {
            case 1:
                return new BM25Similarity(1.2f, 0.75f);
            case 2:
                return new ClassicSimilarity();
            case 3:
                return new BooleanSimilarity();
            case 4:
                return new LMDirichletSimilarity();
            case 5:
                return new IBSimilarity(new DistributionLL(), new LambdaDF(), new NormalizationH1());
            default:
                return new BM25Similarity();
        }
    }

    public static String getSimilarityModelName(int similarityModel) {
        switch (similarityModel) {
            case 1:
                return "bm25";
            case 2:
                return "classic";
            case 3:
                return "boolean";
            case 4:
                return "lm";
            case 5:
                return "ibs";
            default:
                return "bm25";
        }
    }
}
