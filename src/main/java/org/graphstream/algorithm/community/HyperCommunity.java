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
    private int innerEdgesCount;
    private int outerEdgesCount;
    
    public HyperCommunity() {
        super();
        this.innerEdgesCount = 0;
        this.outerEdgesCount = 0;
    }
    
    public void seed(int nodeIndex) {
        nodesIndexes.add(nodeIndex);
    }

    /**
     * @return the outerEdgesCount
     */
    public int getOuterEdgesCount() {
        return outerEdgesCount;
    }

    /**
     * Increase the outerEdgesCount by 1.
     */
    public void increaseOuterEdgesCount() {
        this.increaseOuterEdgesCount(1);
    }
    
    /**
     * Increase the outerEdgeCount by the number of the input.
     * @param number the number to increase the outerEgdesCount
     */
    public void increaseOuterEdgesCount(int number) {
        this.outerEdgesCount += number;
    }
    
    /**
     * Decrease the outerEdgesCount by 1.
     */
    public void decreaseOuterEdgesCount() {
        this.decreaseOuterEdgesCount(1);
    }
    
    /**
     * Decrease the outerEdgeCount by the number of the input.
     * @param number the number to decrease the outerEgdesCount
     */
    public void decreaseOuterEdgesCount(int number) {
        this.outerEdgesCount -= number;
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
}
