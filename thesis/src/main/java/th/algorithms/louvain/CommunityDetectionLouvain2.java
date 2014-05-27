package th.algorithms.louvain;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import org.graphstream.algorithm.measure.Modularity;
import org.graphstream.algorithm.measure.NormalizedMutualInformation;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.GraphParseException;
import org.graphstream.ui.graphicGraph.stylesheet.StyleConstants.Units;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;
import th.algorithms.louvain.utils.HyperCommunity;
import th.algorithms.louvain.utils.HyperCommunityManager;
import th.algorithms.louvain.utils.WeightMap;
import th.utils.UIToolbox;

/**
 * Implementation of the Louvain algorithm.
 *
 * @reference Fast unfolding of communities in large networks Vincent D.
 * Blondel, Jean-Loup Guillaume, Renaud Lambiotte and Etienne Lefebvre.
 * @author Ilias Trichopoulos <itrichop@csd.auth.gr>
 */
public class CommunityDetectionLouvain2 {

    //private Graph graph, // Graph used for the calculations
    //finalGraph;  // The final graph printed
    private HyperCommunityManager manager;
//    private List<Map<String, HyperCommunity>> communitiesPerPhase; // Every item contains
    // a map between community id and community object

    private Modularity modularity;
    private double maxModularity,
            newModularity,
            initialModularity,
            deltaQ,
            globalMaxQ,
            globalNewQ;
    private String oldCommunity,
            bestCommunity;
    // Used for colors.
    private Random color;
    private int r, g, b;

    private Iterator<Node> neighbours;
    private int step;
    private String fileName;

    // Sprites used to display the results on the screen.
    private SpriteManager sm;
    private Sprite communitiesCount,
            modularityCount,
            nmiCount;

    private NormalizedMutualInformation nmi;

    private boolean debug = false;
    private Double totalGraphEdgeWeight;
    private Map<String, HyperCommunity> communities;

    /**
     * Initializing global variables.
     *
     * @param fileName the input path of the file.
     * @throws IOException
     * @throws GraphParseException
     */
    public void init(Graph graph) throws IOException, GraphParseException {

        this.totalGraphEdgeWeight = 0.0;
        this.manager = new HyperCommunityManager();

        // Mapping between community id and community object
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
            //edge.addAttribute("ui.label", edge.getAttribute("weight"));
        }

        generateCommunityForEachNode(graph);
        initWeightsToCommunities(graph);
    }

    public void generateCommunityForEachNode(Graph graph) {
        for (Node node : graph) {
            //node.addAttribute("ui.label", node.getId()); // Add a label in every node
            // with the id of the node.
            // Every node belongs to a different community.
            HyperCommunity community = manager.communityFactory();
            community.addNode(node.getIndex());
            // Add community attribute to each node, so modularity alg can identify 
            //which nodes belong to each community.
            node.addAttribute("community", community.getCID());
            node.addAttribute("nodeAsCommunity", community.getCID());
            community.increaseEdgeWeightToCommunity(community.getCID(), 0.0);

            // Add the newly created community to the map
            this.communities.put(community.getCID(), community);

            // This will keep track of the summary of the edge weights between each node
            // to each community.
            node.addAttribute("nodeToCommunityEdgesWeights", new WeightMap(node.getDegree())); //the allocation for the WeightMap, should be the same as the node's degree.

            initKi(node);

        }
    }

    public void initKi(Node node) {
        Double ki = 0.0;
        Iterator<Edge> neighbourEdges = node.getEachEdge().iterator();
        while (neighbourEdges.hasNext()) {
            ki += (Double) neighbourEdges.next().getAttribute("weight");
        }
        node.setAttribute("ki", ki);
    }

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

    public HashMap<String, String> findCommunities(Graph graph) {

        HashMap<String, String> changes = new HashMap<String, String>(10);
        modularity = new Modularity("community", "weight");
        modularity.init(graph);

        double initialM = modularity.getMeasure();
        do {
            for (Node node : graph) {
                Double maxDeltaQ = 0.0;

                String nodeCommunityId = (String) node.getAttribute("community");
                String bestCommunityToGo = nodeCommunityId;
                WeightMap nodeToCommunityEdgesWeights = (WeightMap) node.getAttribute("nodeToCommunityEdgesWeights");

                Double ki = (Double) node.getAttribute("ki");
                Double m = this.totalGraphEdgeWeight;

                Iterator<Node> neighbours = node.getNeighborNodeIterator();
                while (neighbours.hasNext()) {

                    Node neighbour = neighbours.next();
                    String neighbourCommunityId = (String) neighbour.getAttribute("community");
                    HyperCommunity neighbourCommunity = this.communities.get(neighbourCommunityId);

                    Double Sin = neighbourCommunity.getInnerEdgesWeightCount();
                    Double Stot = neighbourCommunity.getTotalEdgesWeight();
                    Double kiin = nodeToCommunityEdgesWeights.getWeight(neighbourCommunityId);
//                    if (this.debug) {
//                        System.out.println("Sin:\t" + Sin);
//                        System.out.println("Stot:\t" + Stot);
//                        System.out.println("kiin:\t" + kiin);
//                        System.out.println("ki:\t" + ki);
//                        System.out.println("m:\t" + m);
//                    }

                    Double deltaQ = calculateDeltaQ(Sin, Stot, ki, kiin, m);

                    if (deltaQ > maxDeltaQ) {
                        maxDeltaQ = deltaQ;
                        bestCommunityToGo = neighbourCommunityId;
                    }

//                    if (this.debug) {
//                        System.out.println("If node " + node.getIndex() + " goes to community " + neighbourCommunityId + ": " + deltaQ);
//                        System.out.println("");
//                    }
                }
                // Move node to the best community (if not already in) and update node and community lists.
                if (!nodeCommunityId.equals(bestCommunityToGo)) {
                    incrementalUpdate(node, bestCommunityToGo);
                    node.changeAttribute("community", bestCommunityToGo);
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
            deltaQ = newM - initialM;
            initialM = newM;
        } while (deltaQ > 0); // Loop until there is no improvement in modularity

        if (this.debug) {
            for (Node node : graph) {
                System.out.println("Node " + node.getIndex() + ", community: " + (String) node.getAttribute("community"));
            }
            System.out.println("Communities: " + this.communities);
        }
        return changes;
    }

    private HyperCommunity getNodeCommunity(Node node) {
        String nodeCommunityId = (String) node.getAttribute("community");
        return this.communities.get(nodeCommunityId);
    }

    private void incrementalUpdate(Node node, String newCommunityId) {
        String nodeCommunityId = (String) node.getAttribute("community");
        HyperCommunity nodeCommunity = this.communities.get(nodeCommunityId);
        HyperCommunity newCommunity = this.communities.get(newCommunityId);
        WeightMap nodeToCommunityEdgesWeights = (WeightMap) node.getAttribute("nodeToCommunityEdgesWeights");
        Double weightToCurrentCommunity = nodeToCommunityEdgesWeights.getWeight(nodeCommunityId);
        Double weightToNewCommunity = nodeToCommunityEdgesWeights.getWeight(newCommunityId);

        // Update lists in the current community
//        nodeCommunity.decreaseInnerEdgesWeightCount(weightToCurrentCommunity);
        nodeCommunity.decreaseEdgeWeightToCommunity(nodeCommunityId, weightToCurrentCommunity);
        nodeCommunity.increaseEdgeWeightToCommunity(newCommunityId, weightToCurrentCommunity);
        nodeCommunity.decreaseEdgeWeightToCommunity(newCommunityId, weightToNewCommunity);

        nodeCommunity.removeNode(node.getIndex());

        // Update lists in the new community
//        newCommunity.increaseInnerEdgesWeightCount(weightToNewCommunity);
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
            } else {
//                newCommunity.increaseEdgeWeightToCommunity(newCommunityId,weightToCommunity);
            }
        }

        neighbours = node.getNeighborNodeIterator();
        while (neighbours.hasNext()) {
            Node neighbour = neighbours.next();
            WeightMap neighbourToCommunityEdgesWeights = (WeightMap) neighbour.getAttribute("nodeToCommunityEdgesWeights");

            Edge edgeBetween = node.getEdgeBetween(neighbour);
            Double edgeBetweenWeight = (Double) edgeBetween.getAttribute("weight");

            neighbourToCommunityEdgesWeights.increase(newCommunityId, edgeBetweenWeight);
            neighbourToCommunityEdgesWeights.decrease(nodeCommunityId, edgeBetweenWeight);
//            if(this.debug) {
//                System.out.println("Node " + neighbour.getIndex() + ": " + neighbourToCommunityEdgesWeights);
//            }
        }

        // remove community from the map if it has no nodes after the update
        if (nodeCommunity.getNodesCount() == 0) {
            this.communities.remove(nodeCommunityId);
        }

    }

    private Double calculateDeltaQ(Double Sin, Double Stot, Double ki, Double kiin, Double m) {
        Double doubleM = m * 2;
        Double firstFraction = (Sin + kiin) / doubleM;
        Double secondFraction = Math.pow((Stot + ki) / doubleM, 2.0);
        Double firstStatement = firstFraction - secondFraction;
        Double thirdFraction = Sin / doubleM;
        Double fourthFraction = Math.pow(Stot / doubleM, 2.0);
        Double fifthFraction = Math.pow(ki / doubleM, 2.0);
        Double secondStatement = thirdFraction - fourthFraction - fifthFraction;

        return firstStatement - secondStatement;
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

    public void debugOn() {
        this.debug = true;
    }
}
