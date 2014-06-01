package th.algorithms.louvain;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.graphstream.algorithm.Algorithm;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;


/**
 * Implementation of the Louvain algorithm.
 * @reference Fast unfolding of communities in large networks
 *            Vincent D. Blondel, Jean-Loup Guillaume, Renaud Lambiotte and Etienne Lefebvre.
 * @author Anastasis Andronidis <anastasis90@yahoo.gr>
 * @author Ilias Trichopoulos <itrichop@csd.auth.gr>
 */
public class CommunityDetectionLouvain implements Algorithm {
    
    private Graph graph;
    private final Map<String, Entry<Double,Double>> communitySiStotMap = new HashMap<String, Entry<Double,Double>>(30);
    private Double m = 0.0;

    private void initEdgesAndCalculateM(Edge e) {
        if (e.hasAttribute("weight")) {
            Double tmp = Double.parseDouble((String) e.getAttribute("weight"));
            e.changeAttribute("weight", tmp);
            this.m += tmp;
        } else {
            e.addAttribute("weight", 1.0);
            this.m += 1.0;
        }
        //edge.addAttribute("ui.label", edge.getAttribute("weight"));
    }
    
    private void calculateKi(Edge e) {
        Node[] nodes = {e.getNode0(), e.getNode1()};
        
        for (Node n : nodes) {
            if (n.hasAttribute("ki")) {
                Double partKi = (Double) n.getAttribute("ki") + (Double) e.getAttribute("weight");
                n.changeAttribute("ki", partKi);
            } else {
                n.addAttribute("ki", (Double) e.getAttribute("weight"));
            }
            n.setAttribute("community", String.valueOf(n.getIndex()));
        }
    }
    
    private Double calculateDeltaQ(Double Sin, Double Stot, Double ki, Double kiin, Double m) {
        Double doubleM = m * 2;
        Double firstFraction = (Sin + kiin) / doubleM;
        Double secondFraction = Math.pow((Stot + ki) / doubleM, 2.0);
        Double firstStatement = firstFraction - secondFraction;
        Double thirdFraction = Sin / doubleM;
        Double fourthFraction = Math.pow(Stot / doubleM, 2.0);
        Double fifthFraction = Math.pow(ki / doubleM, 2.0);
        Double secondStatement = thirdFraction - fourthFraction - fifthFraction;

        return firstStatement - secondStatement;
    }
    
    private Double calculateKiin(Node n, String targetCommunity) {
        Double kiin = 0.0;
        Iterator<Node> it = n.getNeighborNodeIterator();
        while (it.hasNext()) {
            Node nn = it.next();

            if (((String) nn.getAttribute("community")).equals(targetCommunity)) {
                kiin += (Double) n.getEdgeBetween(nn).getAttribute("weight");
            }
        }
        
        return kiin;
    }
    
    private Double getSelfLoopWeight(Node n) {
        Edge selfloop = n.getEdgeBetween(n);
        if (selfloop != null) {
            return (Double) selfloop.getAttribute("weight");
        }

        return 0.0;
    }
    
    public void init(Graph graph) {
        this.graph = graph;
        
        // calculate m
        // Add weight to each edge
        for (Edge e : graph.getEdgeSet()) {
            initEdgesAndCalculateM(e);
            calculateKi(e);
        }
        
        // Try to move every node into the community of every neighbour
        for (Node n : this.graph) {
            Iterator<Node> it = n.getNeighborNodeIterator();
        
            Double maxDeltaQ = 0.0;
            Node bestNodeToGo = null;
            while (it.hasNext()) {
                Double Si = 0.0, Stot = 0.0, kiin = 0.0;
                Double ki = (Double) n.getAttribute("ki");
                Node nn = it.next();
                
                String com = (String) nn.getAttribute("community");
                if (this.communitySiStotMap.containsKey(com)) {
                    Si = this.communitySiStotMap.get(com).getKey();
                    Stot = this.communitySiStotMap.get(com).getValue();
                    
                    kiin = calculateKiin(n, com);
                } else {
                    Si = getSelfLoopWeight(nn);
                    
                    Stot = (Double) nn.getAttribute("ki");
                    kiin = (Double) n.getEdgeBetween(nn).getAttribute("weight");
                }
                
                double deltaQ = calculateDeltaQ(Si, Stot, ki, kiin, m);
                if (deltaQ > maxDeltaQ) {
                    maxDeltaQ = deltaQ;
                    bestNodeToGo = nn;
                }
            }
            
            if (maxDeltaQ > 0) {
                String myCommunity = (String) n.getAttribute("community");
                String bestCommunityToGo = (String) bestNodeToGo.getAttribute("community");
                
                // If  we are in a multi-community, we subtract ourselfs from that
                // community
                if (this.communitySiStotMap.containsKey(myCommunity)) {
                    // Get our node out of our community
                    Double oldSi = this.communitySiStotMap.get(myCommunity).getKey();
                    oldSi -= calculateKiin(n, myCommunity);
                    
                    Double oldStot = this.communitySiStotMap.get(myCommunity).getValue();
                    oldStot -= (Double) n.getAttribute("ki");
                    oldStot += calculateKiin(n, myCommunity);
                    oldStot -= getSelfLoopWeight(n);
                    
                    this.communitySiStotMap.put(myCommunity, new SimpleEntry<Double, Double>(oldSi, oldStot));
                } 
                
                // We are now alone, and we need to add ourselfs into a new
                // community
                Double tmpSi = 0.0, tmpStot = 0.0;

                // If the node we are going into, is in a multi-community
                if (this.communitySiStotMap.containsKey(bestCommunityToGo)) {
                    tmpSi += this.communitySiStotMap.get(bestCommunityToGo).getKey();
                    tmpSi += calculateKiin(n, bestCommunityToGo);
                    tmpSi += getSelfLoopWeight(n);

                    tmpStot += this.communitySiStotMap.get(bestCommunityToGo).getValue();
                    tmpStot += (Double) n.getAttribute("ki");
                    tmpStot -= calculateKiin(n, bestCommunityToGo);
                    // If the node we are going into, is solo
                } else {
                    tmpSi += (Double) n.getEdgeBetween(bestNodeToGo).getAttribute("weight");
                    tmpSi += getSelfLoopWeight(n);
                    tmpSi += getSelfLoopWeight(bestNodeToGo);

                    tmpStot += (Double) n.getAttribute("ki");
                    tmpStot += (Double) bestNodeToGo.getAttribute("ki");
                    tmpStot -= (Double) n.getEdgeBetween(bestNodeToGo).getAttribute("weight");
                }

                this.communitySiStotMap.put(bestCommunityToGo, new SimpleEntry<Double, Double>(tmpSi, tmpStot));
                n.changeAttribute("community", bestCommunityToGo);
            }
        }
    }

    public void compute() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}