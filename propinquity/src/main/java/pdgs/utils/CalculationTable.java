/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pdgs.utils;

import com.google.common.collect.Sets;
import java.util.Set;

/**
 *
 * @author Anastasis Andronidis <anastasis90@yahoo.gr>
 */
public class CalculationTable {
    public static Set<Integer> CalculateCrr(Set<Integer> Nr, Set<Integer> nnNr) {
        
        return Sets.intersection(Nr, nnNr);
    }

    public static Set<Integer> CalculateCri(Set<Integer> Nr, Set<Integer> Ni, Set<Integer> nnNr, Set<Integer> nnNi) {
        // (Nr ^ nnNi) + (Ni ^ nnNr) + (Ni ^ nnNi)
        return Sets.union(Sets.union(Sets.intersection(Nr, nnNi), Sets.intersection(Ni, nnNr)), Sets.intersection(Ni, nnNi));
    }

    public static Set<Integer> CalculateCrd(Set<Integer> Nr, Set<Integer> Nd, Set<Integer> nnNr, Set<Integer> nnNd) {
        // (Nr ^ nnNd) + (Nd ^ nnNr) + (Nd ^ nnNd)
        return Sets.union(Sets.union(Sets.intersection(Nr, nnNd), Sets.intersection(Nd, nnNr)), Sets.intersection(Nd, nnNd));
    }

    public static Set<Integer> CalculateCii(Set<Integer> Nr, Set<Integer> Ni, Set<Integer> nnNr, Set<Integer> nnNi) {
        // (Nr + Ni) ^ (nnNr + nnNi)
        return Sets.intersection(Sets.union(Nr, Ni), Sets.union(nnNr, nnNi));
    }

    public static Set<Integer> CalculateCdd(Set<Integer> Nr, Set<Integer> Nd, Set<Integer> nnNr, Set<Integer> nnNd) {
        // (Nr + Nd) ^ (nnNr + nnNd)
        return Sets.intersection(Sets.union(Nr, Nd), Sets.union(nnNr, nnNd));
    }

}
