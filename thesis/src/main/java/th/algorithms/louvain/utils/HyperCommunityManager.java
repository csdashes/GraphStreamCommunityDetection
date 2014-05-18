package th.algorithms.louvain.utils;

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
}
