/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.graphstream.algorithm.community;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import org.graphstream.graph.Node;

/**
 *
 * @author Ilias Trichopoulos <itrichop@csd.auth.gr>
 */
public class HyperCommunity extends Community {
    
    LinkedList<Integer> nodesIndexes;
    private int nodesCount;
    private int innerEdgesCount;
    private Map<String,Integer> outerEdgesCount;
    
    public HyperCommunity() {
        super();
        this.nodesCount = 0;
        this.innerEdgesCount = 0;
        this.outerEdgesCount = new HashMap<String,Integer>();
    }
    
    public void increaseNodesCount() {
        this.nodesCount++;
    }
    
    public void descreaseNodeCount() {
        this.nodesCount--;
    }
    
    public int getNodesCount() {
        return this.nodesCount;
    }
    
    public void seed(int nodeIndex) {
        nodesIndexes.add(nodeIndex);
    }
    
    public String getAttribute() {
        return String.valueOf(Integer.parseInt(this.getId()) + 1);
    }

    /**
     * @return the outerEdgesCount map
     */
    public Map<String,Integer> getOuterEdgesCount() {
        return outerEdgesCount;
    }
    
    /**
     * @return the outerEdgesCount of the given community
     */
    public int getOuterEdgesCount(String communityId) {
        return outerEdgesCount.get(communityId);
    }

    /**
     * @param communityId the community that the outer edge is connected to.
     * Increase the outerEdgesCount to the given community by 1.
     */
    public void increaseOuterEdgesCount(String communityId) {
        this.increaseOuterEdgesCount(communityId,1);
    }
    
    /**
     * Increase the outerEdgeCount by the number of the input.
     * @param communityId the community that the outer edge is connected to.
     * @param number the number to increase the outerEgdesCount
     */
    public void increaseOuterEdgesCount(String communityId, int number) {
        if(outerEdgesCount.containsKey(communityId)) {
            outerEdgesCount.put(communityId, outerEdgesCount.get(communityId) + number);
        } else {
            outerEdgesCount.put(communityId, number);
        }
    }
    
    /**
     * @param communityId the community that the outer edge is connected to.
     * Decrease the outerEdgesCount to the given community by 1.
     */
    public void decreaseOuterEdgesCount(String communityId) {
        this.decreaseOuterEdgesCount(communityId, 1);
    }
    
    /**
     * Decrease the outerEdgeCount by the number of the input.
     * @param communityId the community that the outer edge is connected to.
     * @param number the number to decrease the outerEgdesCount.
     */
    public void decreaseOuterEdgesCount(String communityId, int number) {
        // there might be a problem here if there is a mistake in the implementation of the
        // algorithm. There should be always a key in order to decrease it's outer edges.
        // It should not go below 0.
        outerEdgesCount.put(communityId, outerEdgesCount.get(communityId) - number);
    }

    /**
     * @return the innerEdgesCount
     */
    public int getInnerEdgesCount() {
        return innerEdgesCount;
    }

    /**
     * Increase the innerEdgesCount by 1.
     */
    public void increaseInnerEdgesCount() {
        this.increaseInnerEdgesCount(1);
    }
    
    /**
     * Increase the innerEdgeCount by the number of the input.
     * @param number the number to increase the innerEgdesCount
     */
    public void increaseInnerEdgesCount(int number) {
        this.innerEdgesCount += number;
    }
    
    /**
     * Decrease the innerEdgesCount by 1.
     */
    public void decreaseInnerEdgesCount() {
        this.decreaseInnerEdgesCount(1);
    }
    
    /**
     * Decrease the innerEdgeCount by the number of the input.
     * @param number the number to decrease the innerEgdesCount
     */
    public void decreaseInnerEdgesCount(int number) {
        this.innerEdgesCount -= number;
    }
    
    public void initOuterEdgesCount(Node node) {
        Iterator<Node> neighbours = node.getNeighborNodeIterator();
        while(neighbours.hasNext()) {
            this.outerEdgesCount.put((String)neighbours.next().getAttribute("community"),1);
        }
    }
}
