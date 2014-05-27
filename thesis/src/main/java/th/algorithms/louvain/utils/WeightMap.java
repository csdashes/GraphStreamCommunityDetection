package th.algorithms.louvain.utils;

import java.util.HashMap;

/**
 *
 * @author Ilias Trichopoulos <itrichop@csd.auth.gr>
 */
public class WeightMap extends HashMap<String, Double> {

    public WeightMap(int cap) {
        super(cap);
    }
    
    public WeightMap(WeightMap wm) {
        super(wm);
    }
    
    public void increase(String communityId, Double weight) {
        Double currentWeight = this.get(communityId);
        if (currentWeight == null) {
            this.put(communityId, weight);
        } else {
            currentWeight += weight;
            this.put(communityId, currentWeight);
        }
    }
    
    public void decrease(String communityId, Double weight) {
        Double currentWeight = this.get(communityId);
        if (currentWeight != null) {
            currentWeight -= weight;
            this.put(communityId, currentWeight);
        }
    }
    
    public Double getWeight(String communityId) {
        return this.get(communityId);
    }
    
    public void init(String communityId) {
        if(!this.containsKey(communityId)) {
            this.put(communityId, 0.0);
        }
    }
}
