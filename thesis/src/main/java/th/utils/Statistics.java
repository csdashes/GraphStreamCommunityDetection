package th.utils;

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

    public static void PDStatistics(Graph graph, int a, int b) {
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

        int test = 0;
        for (Entry<Integer, Integer> entry : edgeWeights.entrySet()) {
            System.out.println((entry.getValue()) + " entries are " + entry.getKey());
            test += entry.getValue();
        }
        
        System.out.println("==================");

        for (Entry<Integer, Integer> entry : totalPDstats.entrySet()) {
            System.out.println((entry.getValue() / 2) + " entries are " + entry.getKey());
        }
    }
}
