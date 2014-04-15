/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pdgs.utils;

import java.util.HashMap;

/**
 *
 * @author Anastasis Andronidis <anastasis90@yahoo.gr>
 */
public class PropinquityMap extends HashMap<Integer, MutableInt> {
    private static final long serialVersionUID = 1L;
    
    public PropinquityMap(int cap) {
        super(cap);
    }
    
    public void increase(Integer k) {
        MutableInt count = this.get(k);
        if (count == null) {
            this.put(k, new MutableInt());
        } else {
            count.increment();
        }
    }

    public void decrease(Integer k) {
        MutableInt count = this.get(k);
        if (count == null) {
            // Go to -1 for first time.
            MutableInt newV = new MutableInt().decrement().decrement();
            this.put(k, newV);
        } else {
            count.decrement();
        }
    }

    public Integer getInt(Integer k) {
        return this.get(k).get();
    }
    
}
