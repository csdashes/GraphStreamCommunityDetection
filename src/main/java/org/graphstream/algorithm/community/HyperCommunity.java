/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.graphstream.algorithm.community;

import java.util.LinkedList;

/**
 *
 * @author Ilias Trichopoulos <itrichop@csd.auth.gr>
 */
public class HyperCommunity extends Community {
    
    LinkedList<Integer> nodesIndexes;
    
    public HyperCommunity() {
        super();
    }
    
    public void seed(int nodeIndex) {
        nodesIndexes.add(nodeIndex);
    }
}
