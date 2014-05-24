/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package th.utils;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import th.algorithms.propinquitydynamics.utils.PropinquityMap;

/**
 *
 * @author Anastasis Andronidis <anastasis90@yahoo.gr>
 */
public class Statistics {
    
    public static void PDStatistics(Graph graph) {
        for (Node n : graph) {
            PropinquityMap pm = n.getAttribute("pm");
        }
    }
    
}
