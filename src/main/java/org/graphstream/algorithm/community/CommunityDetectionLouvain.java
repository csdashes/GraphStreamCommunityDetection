/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.graphstream.algorithm.community;

import java.io.IOException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.GraphParseException;

/**
 *
 * @author Ilias Trichopoulos <itrichop@csd.auth.gr>
 */
public class CommunityDetectionLouvain {
    
    private Graph graph;
    
    public void findCommunities(String fileName) throws IOException, GraphParseException {
            
            graph = new SingleGraph("communities");
            graph.display(true);  // display the nodes in a nice aesthetic way
            graph.read(fileName); // import from the text file
            int i = 0;
	    for (Node node : graph) {
                i++;
                node.addAttribute("ui.label", node.getId()); // add a lablel in every node.
            }   
	}
}
