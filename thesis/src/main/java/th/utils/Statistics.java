package th.utils;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import th.algorithms.propinquitydynamics.utils.MutableInt;
import th.algorithms.propinquitydynamics.utils.PropinquityMap;

/**
 *
 * @author Anastasis Andronidis <anastasis90@yahoo.gr>
 */
public class Statistics {

    public static void PDStatistics(Graph graph, String graphName, int a, int b) throws FileNotFoundException, UnsupportedEncodingException {
        Map<Integer, Integer> totalPDstats = new TreeMap<Integer, Integer>();
        Map<Integer, Integer> edgeWeights = new TreeMap<Integer, Integer>();
        
        int maxDegree=-1, minDegree=Integer.MAX_VALUE, asum=0, bsum=0, nutralsum=0,
                oneEdgeVertices = 0, largestNdList = 0, largestNiList = 0;
                
        for (Node n : graph) {
            // count graph degree
            int degree = n.getDegree();
            if (degree > maxDegree) {
                maxDegree = degree;
            } else if (degree < minDegree) {
                minDegree = degree;
            }
            
            if (degree == 1) {
                oneEdgeVertices++;
            }
    
            // total PD distribution count
            PropinquityMap pm = n.getAttribute("pm");
            for (MutableInt i : pm.values()) {
                if (totalPDstats.containsKey(i.get())) {
                    totalPDstats.put(i.get(), totalPDstats.get(i.get())+1);
                } else {
                    totalPDstats.put(i.get(), 1);
                }
            }

            // count items that will be delete/stay/added
            Set<Integer> Nr = n.getAttribute("Nr");
            int NdListSize = 0, NiListSize = 0;
            for (Entry<Integer, MutableInt> row : pm.entrySet()) {
                Integer nodeIndex = row.getKey();
                Integer propinquity = row.getValue().get();

                if (propinquity <= a && Nr.contains(nodeIndex)) {
                    Nr.remove(nodeIndex);
                    asum++;
                    NdListSize++;
                } else if (propinquity >= b && !Nr.contains(nodeIndex)) {
                    bsum++;
                    NiListSize++;
                }
            }
            if (NdListSize > largestNdList) {
                largestNdList = NdListSize;
            }
            if (NiListSize > largestNiList) {
                largestNiList = NiListSize;
            }
            nutralsum += Nr.size();
        }

        for (Edge e : graph.getEachEdge()) {
            int prop = ((PropinquityMap) e.getNode0().getAttribute("pm")).getInt(e.getNode1().getIndex());
            if (edgeWeights.containsKey(prop)) {
                edgeWeights.put(prop, edgeWeights.get(prop) + 1);
            } else {
                edgeWeights.put(prop, 1);
            }
        }

        System.out.println("Propinquity dynamics statistics");
        System.out.println("==================");
        System.out.println("#vertices: " + graph.getNodeCount());
        System.out.println("#edges: " + graph.getEdgeCount());
        System.out.println("# of one edge vertices: " + oneEdgeVertices);
        System.out.println("Max degree: " + maxDegree);
        System.out.println("Min degree: " + minDegree);
        System.out.println("==================");
        System.out.println("After initialize with a=" + a + " and b=" + b);
        System.out.println("#items that will be delete: " + asum/2);
        System.out.println("Largest Nd list: " + largestNdList);
        System.out.println("#items that will be stay as are: " + nutralsum/2);
        System.out.println("#items that will be added: " + bsum/2);
        System.out.println("Largest Ni list: " + largestNiList);
        System.out.println("==================");

        PrintWriter writer = new PrintWriter("../exports/" + graphName + "-edgesPD.csv", "UTF-8");
        writer.println("number of edges,propinquity value");
        for (Entry<Integer, Integer> entry : edgeWeights.entrySet()) {
            writer.println((entry.getValue()) + "," + entry.getKey());
        }
        writer.close();
        
        PrintWriter writer2 = new PrintWriter("../exports/" + graphName + "-PD.csv", "UTF-8");
        writer2.println("number of entries,propinquity value");
        for (Entry<Integer, Integer> entry : totalPDstats.entrySet()) {
            writer2.println((entry.getValue() / 2) + "," + entry.getKey());
        }
        writer2.close();
    }
    
    /**
     * Find the maximum propinquity value of each node to any other.
     * @param graph The input graph.
     * @param graphName The name of the graph to be used for the export file.
     * @throws java.io.FileNotFoundException
     * @throws java.io.UnsupportedEncodingException
     */
    public static void maxPDToAnyNode(Graph graph, String graphName) throws FileNotFoundException, UnsupportedEncodingException {
        Map<Integer, Integer> maxPDPerNode = new TreeMap<Integer, Integer>();
        for (Node n : graph) {
            Integer localMaxPD = 0;
            PropinquityMap pm = (PropinquityMap) n.getAttribute("pm");
            for (Entry<Integer, MutableInt> row : pm.entrySet()) {
                Integer pdValue = row.getValue().get();
                if (pdValue > localMaxPD) {
                    localMaxPD = pdValue;
                }
            }
            maxPDPerNode.put(n.getIndex(),localMaxPD);
        }
        PrintWriter writer = new PrintWriter("../exports/" + graphName + "-maxPDToAnyNode.csv", "UTF-8");
        writer.println("node index,max propinquity value");
        for (Entry<Integer, Integer> entry : maxPDPerNode.entrySet()) {
            writer.println(entry.getKey() + "," + (entry.getValue()));
        }
        writer.close();
    }
}
