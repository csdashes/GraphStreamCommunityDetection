/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.graphstream.algorithm.community;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Ilias Trichopoulos <itrichop@csd.auth.gr>
 */
public class HyperCommunityManager {
    
    public HyperCommunityManager() {
        
    }
    
    /**
     * Creates a new community.
     * @return the newly created community.
     */
    public HyperCommunity communityFactory() {
        HyperCommunity community = new HyperCommunity();
        return community;
    }
    
    public void finilizeCommunityEdgeCounters() {
        // to-do
    }
    
    /**
     * Get the community by the community attribute of a node.
     * @param attr the community attribute of a node.
     * @return the community.
     */
//    public Community getCommunityByAttr(String attr) {
//        return communities.get(attr);
//    }
}
