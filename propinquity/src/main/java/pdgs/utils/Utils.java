/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pdgs.utils;

import java.io.IOException;
import java.util.Iterator;
import java.util.Random;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.stream.file.FileSinkGML;

/**
 *
 * @author Ilias Trichopoulos <itrichop@csd.auth.gr>
 */
public class Utils {
    
    public static void colorCommunities(Graph graph, Integer[] ids) {
        // Used for colors.
        Random color = new Random();

        for (Integer id : ids) {
            int sad = color.nextInt(255);
            graph.getNode(id).setAttribute("visited", 1);
            graph.getNode(id).addAttribute("ui.style", "fill-color: rgb(" + sad + "," + sad + "," + sad + "); size: 20px;");            
        }

        for (Node n : graph.getEachNode()) {
            if (!n.hasAttribute("visited")) {
                int r = color.nextInt(255);
                int g = color.nextInt(255);
                int b = color.nextInt(255);

                n.setAttribute("visited", 1);
                n.addAttribute("ui.style", "fill-color: rgb(" + r + "," + g + "," + b + "); size: 20px;");
                Iterator<Node> breadth = n.getBreadthFirstIterator();
                while (breadth.hasNext()) {
                    Node next = breadth.next();
                    if (!next.hasAttribute("visited")) {
                        next.setAttribute("visited", 1);
                        next.addAttribute("ui.style", "fill-color: rgb(" + r + "," + g + "," + b + "); size: 20px;");
                    }
                }
            }
        }
    }
    
    public static void exportGraphIntoGML(Graph graph, String fileName) throws IOException {
        for (Node n : graph.getEachNode()) {
            n.addAttribute("ui_label", n.getAttribute("ui.label"));
            n.addAttribute("ui_style", n.getAttribute("ui.style"));
            n.removeAttribute("ui.label");
            n.removeAttribute("ui.style");
        }
        for (Edge e : graph.getEachEdge()) {
            e.addAttribute("weight", e.getAttribute("ui.label"));
            e.addAttribute("ui_label", e.getAttribute("ui.label"));
            e.addAttribute("ui_style", e.getAttribute("ui.style"));
            e.removeAttribute("ui.label");
            e.removeAttribute("ui.style");
        }

        FileSinkGML gml = new FileSinkGML();
        gml.writeAll(graph, fileName + ".gml");
    }
}
