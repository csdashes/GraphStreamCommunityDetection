package pdgs.propinquitydynamics;

import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;
import org.graphstream.algorithm.Algorithm;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import pdgs.utils.MutableInt;
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

        // Superstep 0 + 1
        // We are ready to calculate the Angle Propinquity.
        // We emulate the BSP by iterating all nodes. We know
        // that each node sends messages to update pm map. So
        // we will run in each node that will accept a message
        // and we will do the updates
        for (Node n : this.graph.getEachNode()) {
            Set<Integer> Nr = n.getAttribute("Nr");

            for (Integer nn : Nr) {
                PU(nn, Nr, '+', true);
            }
        }

        if (this.debug) {
            System.out.println("PHASE 1");
            System.out.println("After Angle Propinquity");
            debug();
        }

        // Superstep 1 + 2 + 3
        // We need to DN (donate neighbors) to out neighbors. By this move
        // we can calculate the Conjugate Propinquity. If your neighbor (B) has
        // a common neighbor (C) with you (A), then this common neighbor (C) can
        // understand that (A) and (B) are connected. Therefore, (C) can increase
        // it's conjugate propinquity with any other vertex (D) that has (A) and (B)
        // as common neighbors.
        for (Node n : this.graph.getEachNode()) {
            Set<Integer> Nr = n.getAttribute("Nr");

            Iterator<Node> neighIt = n.getNeighborNodeIterator();

            while (neighIt.hasNext()) {
                Node neigh = neighIt.next();
                // Note: IDs are unique! There is no way to have equal
                if (neigh.getIndex() < n.getIndex()) {
                    continue;
                }

                Set<Integer> neighNr = neigh.getAttribute("Nr");

                // Nc <- Nr ^ Sr
                Set<Integer> Nc = Sets.intersection(Nr, neighNr);

                for (Integer nn : Nc) {
                    PU(nn, Nc, '+', true);
                }
            }
        }

        if (this.debug) {
            System.out.println("After Conjugate Propinquity");
            debug();
        }

    }

    // PHASE 2
    public void compute() {
        // Superstep 0 first part
        // Init apropriate sets (Nd, Ni).
        for (Node n : this.graph.getEachNode()) {
            Set<Integer> Ni = new LinkedHashSet<Integer>(10);
            Set<Integer> Nd = new LinkedHashSet<Integer>(10);
            n.setAttribute("Ni", Ni);
            n.setAttribute("Nd", Nd);

            Set<Integer> Nr = n.getAttribute("Nr");
            PropinquityMap pm = n.getAttribute("pm");
            for (Entry<Integer, MutableInt> row : pm.entrySet()) {
                Integer nodeID = row.getKey();
                Integer propinquity = row.getValue().get();

                if (propinquity < this.a && Nr.contains(nodeID)) {
                    Nd.add(nodeID);
                    Nr.remove(nodeID);
                } else if (propinquity >= this.b && !Nr.contains(nodeID)) {
                    Ni.add(nodeID);
                }
            }
        }

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
