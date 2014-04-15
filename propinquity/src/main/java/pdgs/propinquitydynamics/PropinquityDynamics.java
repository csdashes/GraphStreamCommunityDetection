package pdgs.propinquitydynamics;

import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;
import org.graphstream.algorithm.Algorithm;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

/**
 *
 * @author Anastasis Andronidis <anastasis90@yahoo.gr>
 */
public class PropinquityDynamics implements Algorithm {
    Graph graph;
    private int a,b,e;
    private boolean debug = false;

    public void set(int a, int b) {
        this.a = a;
        this.b = b;
    }
    
    // PHASE 1
    public void init(Graph graph) {
    }

    // PHASE 2
    public void compute() {
    }

    /**
     * @return the a
     */
    public int getA() {
        return a;
    }

    /**
     * @param a the a to set
     */
    public void setA(int a) {
        this.a = a;
    }

    /**
     * @return the b
     */
    public int getB() {
        return b;
    }

    /**
     * @param b the b to set
     */
    public void setB(int b) {
        this.b = b;
    }

    /**
     * @return the e
     */
    public int getE() {
        return e;
    }

    /**
     * @param e the e to set
     */
    public void setE(int e) {
        this.e = e;
    }
    
    public void debugOn() {
        this.debug = true;
    }

    public void debugOff() {
        this.debug = false;
    }

    private void debug() {
        for (Node n : this.graph.getEachNode()) {
            System.out.println("Node: " + n.getIndex());
            System.out.println("Nr: " + n.getAttribute("Nr"));
            System.out.println("Ni: " + n.getAttribute("Ni"));
            System.out.println("Nd: " + n.getAttribute("Nd"));
            System.out.println("pm: " + n.getAttribute("pm"));
        }
    }

    void getResults() {
        for (Node n : this.graph.getEachNode()) {
            System.out.println("For Node: " + n.getIndex() + " pm: " + n.getAttribute("pm"));
        }
    }
}
