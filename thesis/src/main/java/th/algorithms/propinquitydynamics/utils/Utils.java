/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package th.algorithms.propinquitydynamics.utils;

import java.util.Set;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

/**
 *
 * @author Anastasis Andronidis <anastasis90@yahoo.gr>
 * @author Ilias Trichopoulos <itrichop@csd.auth.gr>
 */
public class Utils {

    public static void SetPDWeights(Graph graph, boolean graphics) {
        SetPDWeights(graph, graphics, false);
    }

    public static void SetPDWeights(Graph graph, boolean graphics, boolean graphicWidth) {
        for (Edge edge : graph.getEachEdge()) {
            Node[] nodes = {edge.getNode0(), edge.getNode1()};

            // get the propinquity
            double prop = ((PropinquityMap) nodes[0].getAttribute("pm")).get(nodes[1].getIndex()).get();

            if (graphics) {
                edge.setAttribute("ui.label", String.format("%.2f", prop));
                if (graphicWidth) {
                    edge.setAttribute("ui.style", "text-color:red;text-style:bold; text-size:12;size:" + prop * 0.3 + ";");
                }
            }
            edge.setAttribute("weight", prop);

        }
    }

    /**
     * Take the propinquity between two vertices and divide it with the biggest
     * number of edges between the two. Then set the fraction as the weight of
     * the edge.
     *
     * @param graph
     */
    public static void FractionWithNumberOfEdges(Graph graph) {
        FractionWithNumberOfEdges(graph, false, false);
    }
    
    public static void FractionWithNumberOfEdges(Graph graph, boolean graphics, boolean graphicWidth) {
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

            if (graphics) {
                edge.setAttribute("ui.label", String.format("%.2f", weight));
                if (graphicWidth) {
                    edge.setAttribute("ui.style", "text-color:red;text-style:bold; text-size:12;size:" + weight * 3 + ";");
                }
            }
            edge.setAttribute("weight", weight);
        }
    }

    /**
     * Take the propinquity between two vertices and divide it with the summary
     * of each outgoing edge weight, of each vertex. Then set the smaller
     * fraction as the weight of the edge.
     *
     * @param graph
     */
    public static void FractionWithTotalPropinquity(Graph graph) {
        FractionWithTotalPropinquity(graph, false, false);
    }
    
    public static void FractionWithTotalPropinquity(Graph graph, boolean graphics, boolean graphicWidth) {
        for (Edge edge : graph.getEachEdge()) {
            Node[] nodes = {edge.getNode0(), edge.getNode1()};

            int maxPropSum = 0;
            for (Node node : nodes) {
                PropinquityMap pm = node.getAttribute("pm");
                Set<Integer> Nr = node.getAttribute("Nr");

                int propSum = 0;
                if (node.getAttribute("NrSum") != null) {
                    propSum = (Integer) node.getAttribute("NrSum");
                } else {
                    for (Integer n : Nr) {
                        propSum += pm.get(n).get();
                    }
                    node.setAttribute("NrSum", propSum);
                }

                if (propSum > maxPropSum) {
                    maxPropSum = propSum;
                }
            }

            int prop = ((PropinquityMap) nodes[0].getAttribute("pm")).get(nodes[1].getIndex()).get();
            double weight = (double) prop / (double) maxPropSum;

            if (graphics) {
                edge.setAttribute("ui.label", String.format("%.2f", weight));
                if (graphicWidth) {
                    edge.setAttribute("ui.style", "text-color:red;text-style:bold; text-size:12;size:" + weight * 10 + ";");
                }
            }
            edge.setAttribute("weight", weight);
        }
    }
}
