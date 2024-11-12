package tcd.ie.luom.TopicsParser;

import javax.xml.bind.*;
import java.io.File;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class TopicsParser {

    public static List<Topic> parse(String filePath) throws Exception {
        // Step 1: Preprocess the XML to fix its structure
        String fixedXml = fixXml(filePath);

        // Step 2: Initialize JAXB Context and Unmarshaller
        JAXBContext context = JAXBContext.newInstance(TopicsWrapper.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();

        // Step 3: Unmarshal the corrected XML content
        TopicsWrapper wrapper = (TopicsWrapper) unmarshaller.unmarshal(new StringReader(fixedXml));

        // Step 4: Clean and process topics
        for (Topic topic : wrapper.getTopics()) {
            topic.setNum(extractContent(cleanText(topic.getNum()), "Number"));
            topic.setTitle(cleanText(topic.getTitle()));
            topic.setDesc(extractContent(cleanText(topic.getDesc()), "Description"));
            topic.setNarr(extractContent(cleanText(topic.getNarr()), "Narrative"));
        }

        return wrapper.getTopics();
    }

    /**
     * Preprocesses the XML file to add missing closing tags and wrap <top> elements within <topics>.
     *
     * @param filePath Path to the original XML file.
     * @return A well-formed XML string.
     * @throws Exception If an error occurs while reading the file.
     */
    private static String fixXml(String filePath) throws Exception {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        StringBuilder sb = new StringBuilder();
        sb.append("<topics>");

        String currentTag = null;

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue; // Skip empty lines
            }

            if (line.startsWith("<top>")) {
                sb.append("<top>");
                continue;
            }

            if (line.startsWith("</top>")) {
                // Close any open tag before closing </top>
                if (currentTag != null) {
                    sb.append("</").append(currentTag).append(">");
                    currentTag = null;
                }
                sb.append("</top>");
                continue;
            }

            if (line.startsWith("<")) {
                // Start of a new tag
                // Close the previous tag if it's still open
                if (currentTag != null) {
                    sb.append("</").append(currentTag).append(">");
                }

                // Extract tag name
                int endIndex = line.indexOf(">");
                if (endIndex > 1) {
                    currentTag = line.substring(1, endIndex).trim();
                    sb.append("<").append(currentTag).append(">");
                    // Extract content after '>'
                    String content = line.substring(endIndex + 1).trim();
                    sb.append(content);
                }
                continue;
            } else {
                // Content line
                if (currentTag != null) {
                    sb.append(" ").append(line);
                }
            }
        }

        // Close any remaining open tag
        if (currentTag != null) {
            sb.append("</").append(currentTag).append(">");
        }

        sb.append("</topics>");
        return sb.toString();
    }

    /**
     * Cleans the input text by replacing multiple whitespace characters with a single space and trimming.
     *
     * @param text The input text to clean.
     * @return The cleaned text.
     */
    private static String cleanText(String text) {
        if (text == null) return "";
        // Replace multiple spaces and newlines with a single space
        return text.replaceAll("\\s+", " ").trim();
    }

    /**
     * Extracts the content after a specified label (e.g., "Description:" or "Narrative:").
     *
     * @param line  The line containing the label and content.
     * @param label The label to look for.
     * @return The extracted content after the label, or an empty string if the label is not found.
     */
    public static String extractContent(String line, String label) {
        // Check if the line starts with the label, e.g., "Description:" or "Narrative:"
        if (line.startsWith(label)) {
            // Split the line on the first occurrence of `:` and join remaining parts
            String[] parts = line.split(":", 2); // Limit to 2 splits to avoid splitting within the content
            if (parts.length > 1) {
                return parts[1].trim(); // Trim any leading/trailing whitespace
            }
        }
        return "";
    }
}

