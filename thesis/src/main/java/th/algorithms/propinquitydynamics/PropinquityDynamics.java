package th.algorithms.propinquitydynamics;

import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import org.graphstream.algorithm.Algorithm;
import org.graphstream.algorithm.measure.NormalizedMutualInformation;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import static th.algorithms.propinquitydynamics.utils.CalculationTable.CalculateCdd;
import static th.algorithms.propinquitydynamics.utils.CalculationTable.CalculateCii;
import static th.algorithms.propinquitydynamics.utils.CalculationTable.CalculateCrd;
import static th.algorithms.propinquitydynamics.utils.CalculationTable.CalculateCri;
import static th.algorithms.propinquitydynamics.utils.CalculationTable.CalculateCrr;
import th.algorithms.propinquitydynamics.utils.MutableInt;
import th.algorithms.propinquitydynamics.utils.PropinquityMap;
import th.utils.UIToolbox;

/**
 *
 * @author Anastasis Andronidis <anastasis90@yahoo.gr>
 * @author Ilias Trichopoulos  <itrichop@csd.auth.gr>
 */
public class PropinquityDynamics implements Algorithm {

    Graph graph;
    private int a, b, e = -1;
    private boolean debug = false, statistics = false;
    private String[] debugIDs;

    private void debug(String[] ids) {
        for (String id : ids) {
            Node n = this.graph.getNode(id);
            System.out.println("Node: " + n.getIndex());
            System.out.println("Nr: " + n.getAttribute("Nr"));
            System.out.println("Ni: " + n.getAttribute("Ni"));
            System.out.println("Nd: " + n.getAttribute("Nd"));
            System.out.println("pm: " + n.getAttribute("pm"));
        }
    }

    private Set<Integer> getNeightboursOf(Node n) {
        Set<Integer> out = new HashSet<Integer>(10);
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
            n.setAttribute("ui.label", n.getIndex() + "#" + n.getId());
            n.setAttribute("ui.style", "size:20px;");

            // The propinquity map
            PropinquityMap pm = new PropinquityMap(100);
            // Get all neightbours of the current node.
            // And init Nr.
            Set<Integer> Nr = getNeightboursOf(n);

            n.setAttribute("pm", pm);
            n.setAttribute("Nr", Nr);
        }

        // The paper algorithm does not include the propinquity increase of
        // the direct neighbours
        for (Node n : this.graph.getEachNode()) {
            Set<Integer> Nr = n.getAttribute("Nr");
            PropinquityMap pm = n.getAttribute("pm");

            for (Integer nn : Nr) {
                pm.increase(nn);
            }
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
            debug(this.debugIDs);
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
            debug(this.debugIDs);
        }

        if (this.statistics) {
            // We use this class for just to make our work faster
            PropinquityMap stats = new PropinquityMap(100);

            for (Node n : this.graph.getEachNode()) {
                PropinquityMap pm = n.getAttribute("pm");

                for (MutableInt i : pm.values()) {
                    stats.increase(i.get());
                }
            }

            System.out.println(stats);
        }
    }

    // PHASE 2
    public void compute() {
        // Init e to count topology differences
        this.e = 0;
        
        // Superstep 0 first part
        // Init apropriate sets (Nd, Ni).
        for (Node n : this.graph.getEachNode()) {
            Set<Integer> Ni = new HashSet<Integer>(10);
            Set<Integer> Nd = new HashSet<Integer>(10);
            n.setAttribute("Ni", Ni);
            n.setAttribute("Nd", Nd);

            Set<Integer> Nr = n.getAttribute("Nr");
            PropinquityMap pm = n.getAttribute("pm");
            for (Entry<Integer, MutableInt> row : pm.entrySet()) {
                Integer nodeIndex = row.getKey();
                Integer propinquity = row.getValue().get();

                if (propinquity <= this.a && Nr.contains(nodeIndex)) {
                    Nd.add(nodeIndex);
                    Nr.remove(nodeIndex);
                    this.e++;
                } else if (propinquity >= this.b && !Nr.contains(nodeIndex)) {
                    Ni.add(nodeIndex);
                    this.e++;
                }
            }
        }

        // We take care of the direct connections here. If we delete a neightbor,
        // we must decrease the propinquity etc...
        for (Node n : this.graph.getEachNode()) {
            PropinquityMap pm = n.getAttribute("pm");
            Set<Integer> Ni = n.getAttribute("Ni");
            Set<Integer> Nd = n.getAttribute("Nd");

            for (Integer id : Ni) {
                pm.increase(id);
            }
            for (Integer id : Nd) {
                pm.decrease(id);
            }
        }

        if (this.debug) {
            System.out.println("PHASE 2");
            System.out.println("After initialization");
            debug(this.debugIDs);
        }

        // Superstep 0 second part
        // Here again we update the Angle Propinquity. The difference is that we
        // need to take care of nodes that should be inserted as new neighbors and
        // delete neighbors that should not be with us any longer
        //
        // Nr will contain all nodes that have enough points to stay as 
        // our neigbours (NOTE: not more than b but more than a points) and 
        // there where our neightbors from the beginning.
        // Ni will contain all nodes that we don't have as neighbors in the inital
        // topology, but we want them to be in the same cluster with us.
        // Nd will contain all nodes that are our neighbors and we don't want them
        // to be any more.
        for (Node n : this.graph.getEachNode()) {
            Set<Integer> Nr = n.getAttribute("Nr");
            Set<Integer> Ni = n.getAttribute("Ni");
            Set<Integer> Nd = n.getAttribute("Nd");

            // The Ni nodes are new nodes to us. This means than we need to
            // inform our current (by current means after cleaning Nr list with Nd)
            // neighbours. We are the Angle Propinquity between our new Ni neighbours
            // and our old Nr neighbrous.
            // The same works for Nd.
            for (Integer u_i : Nr) {
                PU(u_i, Ni, '+');
                PU(u_i, Nd, '-');
            }

            // On the other hand, we need to inform our new neigbours to include
            // our current and the other new neighbours.
            for (Integer u_i : Ni) {
                PU(u_i, Nr, '+');
                PU(u_i, Ni, '+', true);
            }
            // When it comes to Nd, we need to take back all points we
            // added so far.
            for (Integer u_i : Nd) {
                PU(u_i, Nr, '-');
                PU(u_i, Nd, '-', true);
            }
        }

        if (this.debug) {
            System.out.println("After Angle Propinquity");
            debug(this.debugIDs);
        }

        // Now it's time to calculate the Conjugate Propinquity in the same we
        // discussed before in the Phase 1. The only difference is that we need
        // to take into consideration again the Nd and Ni.
        for (Node n : this.graph.getEachNode()) {
            // Superstep 1 second part
            Set<Integer> Nr = n.getAttribute("Nr");
            Set<Integer> Nd = n.getAttribute("Nd");
            Set<Integer> Ni = n.getAttribute("Ni");

            for (Integer nn : Nr) {
                if (nn > n.getIndex()) {
                    Set<Integer> nnNr = this.graph.getNode(nn).getAttribute("Nr");
                    Set<Integer> nnNi = this.graph.getNode(nn).getAttribute("Ni");
                    Set<Integer> nnNd = this.graph.getNode(nn).getAttribute("Nd");

                    if (nnNr.contains(n.getIndex())) {
                        // Calculate Crr, Cri and Crd according to Table 1
                        Set<Integer> Crr = CalculateCrr(Nr, nnNr);
                        Set<Integer> Cri = CalculateCri(Nr, Ni, nnNr, nnNi);
                        Set<Integer> Crd = CalculateCrd(Nr, Nd, nnNr, nnNd);

                        for (Integer u_i : Crr) {
                            // PU(u_i,Cri,+), PU(u_i,Crd,−)
                            PU(u_i, Cri, '+');
                            PU(u_i, Crd, '-');
                        }
                        for (Integer u_i : Cri) {
                            // PU(u_i,Crr,+), PU(u_i,Cri−{u_i},+)
                            PU(u_i, Crr, '+');
                            PU(u_i, Cri, '+', true);
                        }
                        for (Integer u_i : Crd) {
                            // PU(u_i,Crr,-), PU(u_i,Crd−{u_i},-)
                            PU(u_i, Crr, '-');
                            PU(u_i, Crd, '-', true);
                        }
                    }

                    // Note: We might not those 2 here :/
                    // Please look superstep 2 page 1003
                    // paper: Parallel Community Detection on Large 
                    // Networks with Propinquity Dynamics
//                    if (nnNi.contains(n.getIndex())) {
//                        Set<Integer> Cii = CalculateCii(Nr, Ni, nnNr, nnNi);
//
//                        for (Integer u_i : Cii) {
//                            PU(u_i, Cii, '+', true);
//                        }
//                    }
//                    if (nnNd.contains(n.getIndex())) {
//                        Set<Integer> Cdd = CalculateCdd(Nr, Nd, nnNr, nnNd);
//
//                        for (Integer u_i : Cdd) {
//                            PU(u_i, Cdd, '-', true);
//                        }
//                    }
                }
            }

            for (Integer nn : Ni) {
                if (nn > n.getIndex()) {
                    Set<Integer> nnNr = this.graph.getNode(nn).getAttribute("Nr");
                    Set<Integer> nnNi = this.graph.getNode(nn).getAttribute("Ni");

                    if (nnNi.contains(n.getIndex())) {
                        Set<Integer> Cii = CalculateCii(Nr, Ni, nnNr, nnNi);

                        for (Integer u_i : Cii) {
                            PU(u_i, Cii, '+', true);
                        }
                    }
                }
            }

            for (Integer nn : Nd) {
                if (nn > n.getIndex()) {
                    Set<Integer> nnNr = this.graph.getNode(nn).getAttribute("Nr");
                    Set<Integer> nnNd = this.graph.getNode(nn).getAttribute("Nd");

                    if (nnNd.contains(n.getIndex())) {
                        Set<Integer> Cdd = CalculateCdd(Nr, Nd, nnNr, nnNd);

                        for (Integer u_i : Cdd) {
                            PU(u_i, Cdd, '-', true);
                        }
                    }
                }
            }
        }

        if (this.debug) {
            System.out.println("After Conjugate Propinquity");
            debug(this.debugIDs);
        }

        // Finishing step. Reset Nr.
        for (Node n : this.graph.getEachNode()) {
            Set<Integer> Nr = n.getAttribute("Nr");
            Set<Integer> Ni = n.getAttribute("Ni");

            n.setAttribute("Nr", Sets.union(Nr, Ni).copyInto(new HashSet<Integer>(20)));
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
     * The variable <b>e</b> counts the global additions and removals of edges.
     * We say that if <b>e</b> is 0, then propinquity dynamics algorithm
     * has converged.
     * 
     * @return the number of added or removed edges in one phase 2 loop
     */
    public int getE() {
        return e;
    }

    /**
     * Helper function to easy declare that if <b>e</b> == 0, then return true.
     * 
     * @return true if e == 0
     */    
    public boolean didAbsoluteConvergence() {
        return this.e == 0;
    }

    /**
     * Helper function to easy declare that if <b>e</b> <= <b>threshold</b>, then return true.
     *
     * @param threshold the threshold we want to say that the algorithm 
     *                  has converged
     * @return true if e <= threshold
     */
    public boolean didConvergence(int threshold) {
        return this.e <= threshold;
    }

    public void debugOn(String[] ids) {
        this.debug = true;
        this.debugIDs = ids;
    }

    public void debugOff() {
        this.debug = false;
    }

    public void statisticsOn() {
        this.statistics = true;
    }

    public void statisticsOff() {
        this.statistics = false;
    }

    public void applyFinalTopology() {
        // Remove all edges to rebuild the graph based on Nr
        for (Edge edge : this.graph.getEdgeSet()) {
            this.graph.removeEdge(edge.getIndex());
        }
        
        for (Node n : this.graph.getEachNode()) {
            Set<Integer> Nr = n.getAttribute("Nr");
            
            for (Integer neighborIndex : Nr) {
                if (n.getEdgeBetween(neighborIndex) == null) {
                    this.graph.addEdge(n.getId() + "and" + neighborIndex, n, this.graph.getNode(neighborIndex));
                }
            }            
        }
    }
    
        public void applyNMI(Graph graph) {
        NormalizedMutualInformation nmi;
        nmi = new NormalizedMutualInformation("community","groundTruth");
        nmi.init(graph);
        
        UIToolbox ui = new UIToolbox(graph);
        ui.addSprite("NMI", nmi.getMeasure(), 100);
    }
}
