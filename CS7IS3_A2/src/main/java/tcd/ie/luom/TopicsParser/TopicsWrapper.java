package tcd.ie.luom.TopicsParser;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import java.util.List;

@XmlRootElement(name = "topics")
@XmlAccessorType(XmlAccessType.FIELD)
public class TopicsWrapper {

    @XmlElement(name = "top")
    private List<Topic> topics;

    // Getter for topics
    public List<Topic> getTopics() {
        return topics;
    }

    // Setter for topics
    public void setTopics(List<Topic> topics) {
        this.topics = topics;
    }
}
