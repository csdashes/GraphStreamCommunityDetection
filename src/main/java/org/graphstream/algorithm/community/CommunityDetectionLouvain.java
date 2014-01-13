/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.graphstream.algorithm.community;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.graphstream.algorithm.measure.Modularity;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.GraphParseException;

/**
 *
 * @author Ilias Trichopoulos <itrichop@csd.auth.gr>
 */
public class CommunityDetectionLouvain {
    
    private Graph graph, graphPhase2;
    HashMap<Node, Integer> map; // Map every node to an index number.
    HyperCommunity[] communityPerNode; // Each cell contains the community of the node
                                       // who has index the number of the cell.
    private Modularity modularity;
    double  maxModularity, 
            newModularity,
            initialModularity,
            deltaQ;
    String  oldCommunity,
            bestCommunity;
    
    // Used for colors.
    Random color;
    int r,g,b;
    
    Map<String,ArrayList<Node>> mySortedMap;
    
    public ListMultimap<String, Node> findCommunities(String fileName) throws IOException, GraphParseException {
            
            graph = new SingleGraph("communities");
            //graph.display(true);  // Display the nodes in a nice aesthetic way.
            graph.read(fileName); // Import from the text file.
            
            int N = graph.getNodeCount();
            communityPerNode = new HyperCommunity[N];
            
	    for (Node node : graph) {
                node.addAttribute("ui.label", node.getId()); // Add a label in every node.
                
                // Every node belongs to a different community.
                communityPerNode[node.getIndex()] = new HyperCommunity();
                
                // Add community attribute to each node, so modularity alg can identify which
                // nodes belong to each community.
                node.addAttribute("community", communityPerNode[node.getIndex()].getId());
                
                // Since every node is a community by itself, increase the outerEdgesCount of the
                // community by the number of edges that leave the node.
                communityPerNode[node.getIndex()].increaseOuterEdgesCount(node.getOutDegree());
            }
            
            // Add an initial weight of 1.0 in each edge
            for (Edge edge : graph.getEdgeSet()) {
                edge.addAttribute("weight", 1.0);
            }
            
            modularity = new Modularity("community","weight");
            modularity.init(graph);
            
            do {
                initialModularity = modularity.getMeasure();
                for (Node node : graph) {

                    maxModularity = -2.0;
                    newModularity = -2.0;
                    oldCommunity = node.getAttribute("community");
                    bestCommunity = oldCommunity;

                    Iterator<Node> neighbours = node.getNeighborNodeIterator();
                    System.out.println("Node " + node.getId());
                    while(neighbours.hasNext()) {

                        Node neighbour = neighbours.next();

                        // Put the node in the neighbour's community.
                        node.changeAttribute("community", neighbour.getAttribute("community"));

                        // Calculate new modularity
                        newModularity = modularity.getMeasure();
                        System.out.println("To " + neighbour.getId() + " (belongs to community "+ neighbour.getAttribute("community") +")" +", mod: " + newModularity);

                        // Find the community that if the node is transfered to, the modularity gain
                        // is the maximum.
                        // In case of tie, the breaking rule is always to take the one that was checked last.
                        if(newModularity > maxModularity) {
                            maxModularity = newModularity;
                            bestCommunity = node.getAttribute("community");
                        }
                    }

                    // Mode node to the best community
                    if(node.getAttribute("community") != bestCommunity) {
                        node.changeAttribute("community", bestCommunity);
                    }
                    System.out.println("best community to go: " + node.getAttribute("community"));
                    System.out.println("");
                }
                deltaQ = modularity.getMeasure() - initialModularity;
            } while(deltaQ > 0); // Loop until there is no improvement in modularity
            
            // Group nodes by community
            ListMultimap<String, Node> multimap = ArrayListMultimap.create();
            for(Node node : graph) {
                multimap.put((String)node.getAttribute("community"), node);
            }
            
            // Color the nodes of one community with the same (random) color.
            color = new Random();
            for(String community : multimap.keySet()) {
                r = color.nextInt(255);
                g = color.nextInt(255);
                b = color.nextInt(255);
                
                List<Node> communityNodes = multimap.get(community);
                Iterator<Node> iterator = communityNodes.iterator();
                while(iterator.hasNext()) {
                    iterator.next().addAttribute("ui.style", "fill-color: rgb("+r+","+g+","+b+"); size: 20px;");
                }
            }
            
            return multimap;
	}
    
    public void shrinkCommunities(ListMultimap<String, Node> multimap) {
        
        graphPhase2 = new SingleGraph("communitiesPhase2");
        graphPhase2.display(true);
        graphPhase2.setAutoCreate(true); // configuration to create nodes automatically
                                         // when edges are created.
        
//        for(String community : multimap.keySet()) {
            
//            List<Node> communityNodes = multimap.get(community);
//            Iterator<Node> iterator = communityNodes.iterator();
//            while(iterator.hasNext()) {
//                iterator.next().addAttribute("ui.style", "fill-color: rgb("+r+","+g+","+b+"); size: 20px;");
//            }
//        }
    }
}
