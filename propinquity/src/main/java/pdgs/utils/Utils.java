/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pdgs.utils;

import java.io.IOException;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.stream.file.FileSinkGML;

/**
 *
 * @author Ilias Trichopoulos <itrichop@csd.auth.gr>
 */
public class Utils {
    
    /**
     * Take the propinquity between two vertices and divide it with the biggest
     * number of edges between the two. Then set the fraction as the weight of
     * the edge.
     * 
     * @param graph
     */
    public static void propToNumEdges(Graph graph) {
        for (Edge edge : graph.getEachEdge()) {
            Node[] nodes = {edge.getNode0(), edge.getNode1()};

            int maxNumEdges = 0;
            for (Node node : nodes) {
                if (node.getEdgeSet().size() > maxNumEdges) {
                    maxNumEdges = node.getEdgeSet().size();
                }
            }

            // get the propinquity
            int prop = ((PropinquityMap) nodes[0].getAttribute("pm")).get(nodes[1].getIndex()).get();
            double weight = (double) prop / (double) maxNumEdges;

            edge.setAttribute("ui.label", String.format("%.2f", weight));
            edge.setAttribute("ui.style", "text-color:red;text-style:bold; text-size:12;size:" + weight * 3 + ";");
        }
    }

    /**
     * Take the propinquity between two vertices and divide it with the summary
     * of each outgoing edge weight, of each vertex. Then set the smaller
     * fraction as the weight of the edge.
     * 
     * @param graph
     */
    public static void propToTotalProp(Graph graph) {
        for (Edge edge : graph.getEachEdge()) {
            Node[] nodes = {edge.getNode0(), edge.getNode1()};

            int maxPropSum = 0;
            for (Node node : nodes) {
                PropinquityMap node0pm = node.getAttribute("pm");
                Set<Integer> node0Nr = node.getAttribute("Nr");

                Integer node0PropSum = 0;
                for (Integer n : node0Nr) {
                    node0PropSum += node0pm.get(n).get();
                }

                if (node0PropSum > maxPropSum) {
                    maxPropSum = node0PropSum;
                }
            }

            int prop = ((PropinquityMap) nodes[0].getAttribute("pm")).get(nodes[1].getIndex()).get();
            double weight = (double) prop / (double) maxPropSum;

            edge.setAttribute("ui.label", String.format("%.2f", weight));
            edge.setAttribute("ui.style", "text-color:red;text-style:bold; text-size:12;size:" + weight * 10 + ";");
        }
    }
    
    public static void colorCommunities(Graph graph, Integer[] ids) {
        // Used for colors.
        Random color = new Random();

        int fixedColor = color.nextInt(255);
        for (Integer id : ids) {
            graph.getNode(id).setAttribute("visited", 1);
            graph.getNode(id).addAttribute("ui.style", "fill-color: rgb(" + fixedColor + "," + fixedColor + "," + fixedColor + "); size: 20px;");            
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
