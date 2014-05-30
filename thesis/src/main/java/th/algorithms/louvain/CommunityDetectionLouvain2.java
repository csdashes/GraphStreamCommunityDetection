package th.algorithms.louvain;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.graphstream.algorithm.measure.Modularity;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.GraphParseException;
import th.algorithms.louvain.utils.HyperCommunity;
import th.algorithms.louvain.utils.HyperCommunityManager;
import th.algorithms.louvain.utils.WeightMap;

/**
 * Implementation of the Louvain algorithm.
 *
 * @reference Fast unfolding of communities in large networks Vincent D.
 * Blondel, Jean-Loup Guillaume, Renaud Lambiotte and Etienne Lefebvre.
 * @author Ilias Trichopoulos <itrichop@csd.auth.gr>
 */
public class CommunityDetectionLouvain2 {

    private HyperCommunityManager manager;
    private Modularity modularity;
    private boolean debug = false;
    private Double totalGraphEdgeWeight;
    private Map<String, HyperCommunity> communities;

    /**
     * Initializing global variables.
     *
     * @param graph
     * @throws IOException
     * @throws GraphParseException
     */
    public void init(Graph graph) throws IOException, GraphParseException {

        this.totalGraphEdgeWeight = 0.0;
        this.manager = new HyperCommunityManager();

        // Mapping between community CID and community object
        this.communities = new HashMap<String, HyperCommunity>();

        // Add weight to each edge
        for (Edge edge : graph.getEdgeSet()) {
            if (edge.hasAttribute("weight")) {
                Double tmp = Double.parseDouble((String) edge.getAttribute("weight"));
                edge.changeAttribute("weight", tmp);
                this.totalGraphEdgeWeight += tmp;
            } else {
                edge.addAttribute("weight", 1.0);
                this.totalGraphEdgeWeight += 1.0;
            }
            edge.addAttribute("ui.label", edge.getAttribute("weight"));
        }

        generateCommunityForEachNode(graph);
        initWeightsToCommunities(graph);
    }

    public void generateCommunityForEachNode(Graph graph) {
        for (Node node : graph) {

            // Every node belongs to a different community.
            // Generate new community
            HyperCommunity community = manager.communityFactory();

            // Add current node to the community's content nodes
            community.addNode(node.getIndex());

            // Add community attribute to each node, so modularity alg can identify 
            // which nodes belong to each community.
            node.addAttribute("community", community.getCID());

            node.addAttribute("ui.label", node.getId() + "(C" + community.getCID() + ")");
            node.addAttribute("ui.style", "text-size:18px;size:18px;");

            // This attribute will be used for extracting the communities
            node.addAttribute("nodeAsCommunity", community.getCID());

            // Initialize inner edges weight to 0.0
            community.increaseEdgeWeightToCommunity(community.getCID(), 0.0);

            // Add the newly created community to the map
            this.communities.put(community.getCID(), community);

            // Node to community (edges weight) mapping
            node.addAttribute("nodeToCommunityEdgesWeights", new WeightMap(node.getDegree())); //the allocation for the WeightMap, should be the same as the node's degree.

            // Initialize total incident edges weight to node
            initKi(node);
        }
    }

    /**
     * Count the total summary of weights of the edges that are incident to the
     * node.
     *
     * @param node
     */
    public void initKi(Node node) {
        Double ki = 0.0;
        Iterator<Edge> neighbourEdges = node.getEachEdge().iterator();
        while (neighbourEdges.hasNext()) {
            ki += (Double) neighbourEdges.next().getAttribute("weight");
        }
        node.setAttribute("ki", ki);
    }

    /**
     * Initialize map between the node and the summary of edges weight from this
     * node to any community. Also initialize map between community and the
     * summary of edges weight from this community to any community.
     *
     * @param graph
     */
    public void initWeightsToCommunities(Graph graph) {
        for (Node node : graph) {
            String nodeCommunityId = (String) node.getAttribute("community");
            HyperCommunity nodeCommunity = communities.get(nodeCommunityId);
            WeightMap nodeToCommunityEdgesWeights = (WeightMap) node.getAttribute("nodeToCommunityEdgesWeights");
            nodeToCommunityEdgesWeights.init(nodeCommunityId);
            Iterator<Node> neighbours = node.getNeighborNodeIterator();
            while (neighbours.hasNext()) {
                Node neighbour = neighbours.next();
                String neighbourCommunityId = (String) neighbour.getAttribute("community");
                Edge edgeBetween = node.getEdgeBetween(neighbour);
                Double edgeBetweenWeight = (Double) edgeBetween.getAttribute("weight");

                nodeToCommunityEdgesWeights.increase(neighbourCommunityId, edgeBetweenWeight);

                nodeCommunity.increaseEdgeWeightToCommunity(neighbourCommunityId, edgeBetweenWeight);
            }
        }
    }

    public HashMap<String, String> findCommunities(Graph graph, Modularity modularity) {

        HashMap<String, String> changes = new HashMap<String, String>(10);
        Double loopDeltaQ = 0.0;

        double initialM = modularity.getMeasure();
        do {
            for (Node node : graph) {
                Double maxDeltaQ = 0.0;

                // Get the community of this node.
                String nodeCommunityId = (String) node.getAttribute("community");
                HyperCommunity nodeCommunity = this.communities.get(nodeCommunityId);

                // Initialize that the best community to go is the one that it 
                // already has.
                String bestCommunityToGo = nodeCommunityId;

                // Get the weight map between node and communities.
                WeightMap nodeToCommunityEdgesWeights = (WeightMap) node.getAttribute("nodeToCommunityEdgesWeights");

                Double ki = (Double) node.getAttribute("ki");
                Double m = this.totalGraphEdgeWeight;

                Double oldCommunitySin = nodeCommunity.getEdgeWeightToCommunity(nodeCommunityId);
                Double oldCommunityStot = nodeCommunity.getTotalEdgesWeight();
                Double oldCommunityKiin = nodeToCommunityEdgesWeights.getWeight(nodeCommunityId);

                // We have to try to adopt each neighbor's community and see if
                // there is a gain in modularity.
                Iterator<Node> neighbours = node.getNeighborNodeIterator();
                while (neighbours.hasNext()) {
                    Node neighbour = neighbours.next();

                    // Get neighbor's community.
                    String neighbourCommunityId = (String) neighbour.getAttribute("community");
                    HyperCommunity neighbourCommunity = this.communities.get(neighbourCommunityId);

                    Double newCommunitySin = neighbourCommunity.getEdgeWeightToCommunity(neighbourCommunityId);
                    Double newCommunityStot = neighbourCommunity.getTotalEdgesWeight();
                    Double newCommunityKiin = nodeToCommunityEdgesWeights.getWeight(neighbourCommunityId)*2;

                    if (this.debug) {
                        System.out.println("Departure community Sin:\t" + oldCommunitySin);
                        System.out.println("Departure community Stot:\t" + oldCommunityStot);
                        System.out.println("To departure community kiin:\t" + oldCommunityKiin);
                        System.out.println("Destination community Sin:\t" + newCommunitySin);
                        System.out.println("Destination community Stot:\t" + newCommunityStot);
                        System.out.println("To destination community kiin:\t" + newCommunityKiin);
                        System.out.println("ki:\t" + ki);
                        System.out.println("m:\t" + m);
                    }
                    Double deltaQ = calculateDeltaQ(oldCommunitySin, oldCommunityStot, oldCommunityKiin,
                            newCommunitySin, newCommunityStot, newCommunityKiin, ki, m);

                    if (this.debug) {
                        System.out.println("If node " + node.getId() + " goes to Community " + neighbourCommunityId);
                        System.out.println("then DeltaQ: " + deltaQ + "\n");
                    }
//                    double iii = modularity.getMeasure();
//                    node.changeAttribute("community", neighbourCommunityId);
//                    double aaa = modularity.getMeasure();
//                    System.out.println("Modularity Delta Q: " + (aaa - iii));
                    if (deltaQ > maxDeltaQ) {
                        maxDeltaQ = deltaQ;
                        bestCommunityToGo = neighbourCommunityId;
                    }
                }

                // Move node to the best community (if not already in) and update node and community lists.
                if (!nodeCommunityId.equals(bestCommunityToGo)) {
                    incrementalUpdate(node, bestCommunityToGo);
                    node.changeAttribute("community", bestCommunityToGo);

                    // Update the list of changes so we can extract later the
                    // communities from the original graph.
                    String nodeAsCommunity = (String) node.getAttribute("nodeAsCommunity");
                    changes.put(nodeAsCommunity, bestCommunityToGo);

                    if (this.debug) {
                        System.out.println("Node " + node.getIndex()
                                + ": Old community=" + nodeCommunityId
                                + ", New community=" + bestCommunityToGo);
                    }
                }
            }
            double newM = modularity.getMeasure();
            loopDeltaQ = newM - initialM;
            initialM = newM;
        } while (loopDeltaQ > 0); // Loop until there is no improvement in modularity

        if (this.debug) {
            for (Node node : graph) {
                System.out.println("Node " + node.getIndex() + ", community: " + (String) node.getAttribute("community"));
            }
            System.out.println("Communities: " + this.communities);
        }
        return changes;
    }

    private void incrementalUpdate(Node node, String newCommunityId) {
        String nodeCommunityId = (String) node.getAttribute("community");
        HyperCommunity nodeCommunity = this.communities.get(nodeCommunityId);
        HyperCommunity newCommunity = this.communities.get(newCommunityId);
        WeightMap nodeToCommunityEdgesWeights = (WeightMap) node.getAttribute("nodeToCommunityEdgesWeights");
        Double weightToCurrentCommunity = nodeToCommunityEdgesWeights.getWeight(nodeCommunityId);
        Double weightToNewCommunity = nodeToCommunityEdgesWeights.getWeight(newCommunityId);

        // Update lists in the current community
        nodeCommunity.decreaseEdgeWeightToCommunity(nodeCommunityId, weightToCurrentCommunity);
        nodeCommunity.increaseEdgeWeightToCommunity(newCommunityId, weightToCurrentCommunity);
        nodeCommunity.decreaseEdgeWeightToCommunity(newCommunityId, weightToNewCommunity);

        nodeCommunity.removeNode(node.getIndex());

        // Update lists in the new community
        newCommunity.increaseEdgeWeightToCommunity(newCommunityId, weightToNewCommunity);
        newCommunity.increaseEdgeWeightToCommunity(nodeCommunityId, weightToCurrentCommunity);
        newCommunity.decreaseEdgeWeightToCommunity(nodeCommunityId, weightToNewCommunity);

        newCommunity.addNode(node.getIndex());

        //For the rest of the communites that the currect node is connected to, we have to update the lists
        // OF these communities and FOR these communities
        HyperCommunity toCommunity;
        for (Entry<String, Double> nodeToCommunityEdgesWeight : nodeToCommunityEdgesWeights.entrySet()) {
            String toCommunityId = nodeToCommunityEdgesWeight.getKey();
            Double weightToCommunity = nodeToCommunityEdgesWeight.getValue();
            // exclude the already calculated current node community and new community
            if ((!toCommunityId.equals(nodeCommunityId) && !toCommunityId.equals(newCommunityId))
                    && weightToCommunity != 0.0) {
                nodeCommunity.decreaseEdgeWeightToCommunity(toCommunityId, weightToCommunity);
                newCommunity.increaseEdgeWeightToCommunity(toCommunityId, weightToCommunity);

                // QUICK FIX. NEED TO FIND PROPER WAY. PROBLEM WITH SELF-EDGES.
                if (this.communities.containsKey(toCommunityId)) {
                    // OF these communities
                    toCommunity = this.communities.get(toCommunityId);
                    toCommunity.decreaseEdgeWeightToCommunity(nodeCommunityId, weightToCommunity);
                    toCommunity.increaseEdgeWeightToCommunity(newCommunityId, weightToCommunity);
                }
            }
        }

        Iterator<Node> neighbours = node.getNeighborNodeIterator();
        while (neighbours.hasNext()) {
            Node neighbour = neighbours.next();
            WeightMap neighbourToCommunityEdgesWeights = (WeightMap) neighbour.getAttribute("nodeToCommunityEdgesWeights");

            Edge edgeBetween = node.getEdgeBetween(neighbour);
            Double edgeBetweenWeight = (Double) edgeBetween.getAttribute("weight");

            neighbourToCommunityEdgesWeights.increase(newCommunityId, edgeBetweenWeight);
            neighbourToCommunityEdgesWeights.decrease(nodeCommunityId, edgeBetweenWeight);
            if (this.debug) {
                System.out.println("Node " + neighbour.getIndex() + ": " + neighbourToCommunityEdgesWeights);
            }
        }

        // remove community from the map if it has no nodes after the update
        if (nodeCommunity.getNodesCount() == 0) {
            this.communities.remove(nodeCommunityId);
        }

    }

    // Calculate the modularity gain based on the equation: http://goo.gl/UZQtcX
    private Double calculateDeltaQ(Double oldCommunitySin, Double oldCommunityStot, Double oldCommunityKiin,
            Double newCommunitySin, Double newCommunityStot, Double newCommunityKiin, Double ki, Double m) {

        Double newQOfDepartureCommunity = calculateNewQOfDepartureCommunity(oldCommunitySin, oldCommunityStot, oldCommunityKiin, ki, m);
        Double newQOfDestinationCommunity = calculateNewQOfDestinationCommunity(newCommunitySin, newCommunityStot, newCommunityKiin, ki, m);
        Double oldQOfDepartureCommunity = calculateOldQ(oldCommunitySin, oldCommunityStot, m);
        Double oldQOfDestinationCommunity = calculateOldQ(newCommunitySin, newCommunityStot, m);

        return newQOfDepartureCommunity + newQOfDestinationCommunity - (oldQOfDepartureCommunity + oldQOfDestinationCommunity);
    }

    private Double calculateNewQOfDepartureCommunity(Double Sin, Double Stot, Double kiin,
            Double ki, Double m) {

        Double doubleM = m * 2;
        Double fraction1 = (Sin - kiin) / doubleM;
        Double fraction2 = Math.pow((Stot - ki) / doubleM, 2.0);

        return fraction1 - fraction2;
    }

    private Double calculateNewQOfDestinationCommunity(Double Sin, Double Stot, Double kiin,
            Double ki, Double m) {

        Double doubleM = m * 2;
        Double fraction1 = (Sin + kiin) / doubleM;
        Double fraction2 = Math.pow((Stot + ki) / doubleM, 2.0);

        return fraction1 - fraction2;
    }

    private Double calculateOldQ(Double Sin, Double Stot, Double m) {

        Double doubleM = m * 2;
        Double fraction1 = Sin / doubleM;
        Double fraction2 = Math.pow(Stot / doubleM, 2.0);

        return fraction1 - fraction2;
    }

    public Graph foldingCommunities(Graph graph) {

        // Creation of the folded graph.
        Graph foldedGraph = new SingleGraph("Louvain Folded Graph");

        // Add all nodes
        for (Entry<String, HyperCommunity> communityEntry : this.communities.entrySet()) {
            HyperCommunity hc = communityEntry.getValue();
            String hcId = hc.getCID();
            WeightMap edgesWeightsToCommunities = hc.getEdgeWeightToCommunityMap();

            if (foldedGraph.getNode(hcId) == null) {
                Node newNode = foldedGraph.addNode(hcId);
                WeightMap nodeToCommunityEdgesWeights = new WeightMap(edgesWeightsToCommunities);
                newNode.addAttribute("community", hcId);
                newNode.addAttribute("nodeAsCommunity", hcId);
                newNode.addAttribute("nodeToCommunityEdgesWeights", nodeToCommunityEdgesWeights);
                newNode.addAttribute("ki", hc.getTotalEdgesWeight());
                hc.clearCommunityNodes();
                hc.addNode(newNode.getIndex());
            }
        }

        // Add all edges
        for (Node newNode : foldedGraph) {
            HyperCommunity hc = this.communities.get(newNode.getId());
            WeightMap edgesWeightsToCommunities = hc.getEdgeWeightToCommunityMap();
            for (Entry<String, Double> edgesWeightsToCommunity : edgesWeightsToCommunities.entrySet()) {
                String neighbourHcId = edgesWeightsToCommunity.getKey();
                String newNeighbourNodeId = neighbourHcId;
                Double edgeWeight = edgesWeightsToCommunity.getValue();
                if (!newNode.hasEdgeBetween(newNeighbourNodeId)
                        && !newNeighbourNodeId.equals(newNode.getId())
                        && edgeWeight != 0.0) {
                    Edge newEdge = foldedGraph.addEdge(newNode.getId() + ":" + newNeighbourNodeId, newNode.getId(), newNeighbourNodeId);
                    newEdge.addAttribute("weight", edgeWeight);
                }
            }
        }

        return foldedGraph;
    }

    /**
     * Apply changes to the original graph.
     *
     * @param graph
     * @param changes
     */
    public void applyChanges(Graph graph, HashMap<String, String> changes) {
        for (Node node : graph) {
            String nodeCommunityId = (String) node.getAttribute("community");
            if (changes.containsKey(nodeCommunityId)) {
                node.addAttribute("community", changes.get(nodeCommunityId));
            }
        }
    }

    /**
     * Delaying a thread for 100ms. Used when displaying the communities on the
     * screen.
     */
    protected void sleep() {
        try {
            Thread.sleep(100);
        } catch (Exception e) {
        }
    }

    /**
     * Display debug messages on the console.
     */
    public void debugOn() {
        this.debug = true;
    }
}
