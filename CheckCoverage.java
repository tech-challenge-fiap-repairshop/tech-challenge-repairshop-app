import javax.xml.parsers.*;
import org.w3c.dom.*;
import java.io.*;
import java.util.*;

public class CheckCoverage {
    public static void main(String[] args) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File("target/site/jacoco/jacoco.xml"));
        
        NodeList classes = doc.getElementsByTagName("class");
        List<String[]> list = new ArrayList<>();
        int totalMissed = 0;
        int totalCovered = 0;
        
        for (int i = 0; i < classes.getLength(); i++) {
            Element clazz = (Element) classes.item(i);
            String name = clazz.getAttribute("name");
            NodeList counters = clazz.getChildNodes();
            for (int j = 0; j < counters.getLength(); j++) {
                Node node = counters.item(j);
                if (node.getNodeName().equals("counter")) {
                    Element counter = (Element) node;
                    if (counter.getAttribute("type").equals("INSTRUCTION")) {
                        int missed = Integer.parseInt(counter.getAttribute("missed"));
                        int covered = Integer.parseInt(counter.getAttribute("covered"));
                        totalMissed += missed;
                        totalCovered += covered;
                        if (missed > 0) {
                            list.add(new String[]{name, String.valueOf(missed), String.valueOf(covered)});
                        }
                    }
                }
            }
        }
        
        list.sort((a, b) -> Integer.compare(Integer.parseInt(b[1]), Integer.parseInt(a[1])));
        System.out.println("Total Missed: " + totalMissed + " Total Covered: " + totalCovered + " Pct: " + (totalCovered * 100.0 / (totalMissed + totalCovered)));
        for (int i = 0; i < Math.min(20, list.size()); i++) {
            System.out.println(list.get(i)[0] + " - Missed: " + list.get(i)[1] + " Covered: " + list.get(i)[2]);
        }
    }
}
