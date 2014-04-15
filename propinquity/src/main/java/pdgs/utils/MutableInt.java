/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pdgs.utils;

/**
 *
 * @author Anastasis Andronidis <anastasis90@yahoo.gr>
 */
public class MutableInt {
    
    private int value = 1; // note that we start at 1 since we're counting

    public MutableInt increment() {
        ++this.value;
        return this;
    }

    public MutableInt decrement() {
        --this.value;
        return this;
    }
    
    public int get() {
        return this.value;
    }

    @Override
    public String toString() {
        return new Integer(this.get()).toString();
    }
}
