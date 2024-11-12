package tcd.ie.luom.TopicsParser;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;

@XmlAccessorType(XmlAccessType.FIELD)
public class Topic {

    @XmlElement(name = "num")
    private String num;

    @XmlElement(name = "title")
    private String title;

    @XmlElement(name = "desc")
    private String desc;

    @XmlElement(name = "narr")
    private String narr;

    // Getter and Setter for num
    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    // Getter and Setter for title
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    // Getter and Setter for desc
    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    // Getter and Setter for narr
    public String getNarr() {
        return narr;
    }

    public void setNarr(String narr) {
        this.narr = narr;
    }
}
