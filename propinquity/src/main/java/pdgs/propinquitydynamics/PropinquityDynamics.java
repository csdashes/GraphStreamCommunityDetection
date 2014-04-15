package pdgs.propinquitydynamics;

import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;
import org.graphstream.algorithm.Algorithm;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import pdgs.utils.PropinquityMap;

/**
 *
 * @author Anastasis Andronidis <anastasis90@yahoo.gr>
 */
public class PropinquityDynamics implements Algorithm {
    Graph graph;
    private int a,b,e;
    private boolean debug = false;

    private Set<Integer> getNeightboursOf(Node n) {
        Set<Integer> out = new LinkedHashSet<Integer>(10);
        Iterator<Node> it = n.getNeighborNodeIterator();
        while (it.hasNext()) {
            out.add(it.next().getIndex());
        }
        return out;
    }

    private void PU(Integer u_i, Set<Integer> set, char operator) {
        PU(u_i, set, operator, false);
    }

    private void PU(Integer u_i, Set<Integer> set, char operator, boolean skip) {
        if (operator == '+') {
            PUup(u_i, set, skip);
        } else {
            PUdown(u_i, set, skip);
        }
    }

    private void PUdown(Integer u_i, Set<Integer> set, boolean skip) {
        PropinquityMap pm = this.graph.getNode(u_i).getAttribute("pm");

        for (Integer pu : set) {
            if (skip) {
                if (u_i != pu) {
                    pm.decrease(pu);
                }
            } else {
                pm.decrease(pu);
            }
        }
    }

    private void PUup(Integer u_i, Set<Integer> set, boolean skip) {
        PropinquityMap pm = this.graph.getNode(u_i).getAttribute("pm");

        for (Integer pu : set) {
            if (skip) {
                if (u_i != pu) {
                    pm.increase(pu);
                }
            } else {
                pm.increase(pu);
            }
        }
    }

    public void set(int a, int b) {
        this.a = a;
        this.b = b;
    }
    
    // PHASE 1
    public void init(Graph graph) {
        this.graph = graph;

        // Init data in each node
        for (Node n : this.graph.getEachNode()) {
            n.setAttribute("ui.label", n.getIndex());
            n.setAttribute("ui.style", "size:20px;");

            // The propinquity map
            PropinquityMap pm = new PropinquityMap(100);
            // The decompose sets.
            Set<Integer> Nr; //, Ni, Nd;

            // Get all neightbours of the current node.
            // And init Nr.
            Nr = getNeightboursOf(n);

            n.setAttribute("pm", pm);
            n.setAttribute("Nr", Nr);
        }

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
