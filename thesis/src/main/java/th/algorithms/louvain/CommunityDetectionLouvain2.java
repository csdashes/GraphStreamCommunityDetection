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

        // Add attribute "trueCommunityNodes" to every node, because later each
        // node will represent a community (after the folding phase) so we want 
        // to keep track of the contents of each community. Used to revert to
        // the original graph.        
        for (Node node : graph) {
            Set<Integer> trueCommunityNodes = new HashSet<Integer>();
            trueCommunityNodes.add(node.getIndex());
            node.addAttribute("trueCommunityNodes", trueCommunityNodes);
        }

//        this.communitiesPerPhase = new ArrayList<Map<String, HyperCommunity>>();
        this.manager = new HyperCommunityManager();
    }

    /**
     * The controller of the algorithm.
     *
     * @throws IOException
     * @throws GraphParseException
     */
    public void execute() throws IOException, GraphParseException {

    }

    public void findCommunities(Graph graph) {
        
        modularity = new Modularity("community", "weight");
        modularity.init(graph);

        // Mapping between community id and community object
        this.communities = new HashMap<String, HyperCommunity>();

        for (Node node : graph) {
            node.addAttribute("ui.label", node.getIndex()); // Add a label in every node
            // with the index id of the node.
            // Every node belongs to a different community.
            HyperCommunity community = manager.communityFactory();
            community.addNode(node.getIndex());
            // Add community attribute to each node, so modularity alg can identify 
            //which nodes belong to each community.
            node.addAttribute("community", community.getId());

            // Add the newly created community to the map
            this.communities.put(community.getId(), community);

            // This will keep track of the summary of the edge weights between each node
            // to each community.
            node.addAttribute("nodeToCommunityEdgesWeights", new WeightMap(node.getDegree())); //the allocation for the WeightMap, should be the same as the node's degree.
        }

        // Add the new map of communities in an arraylist so the communities will
        // not be mixed through the recursive steps of the algorithm.
        //this.communitiesPerPhase.add(this.communities);
        for (Node node : graph) {
            String nodeCommunityId = (String) node.getAttribute("community");
            HyperCommunity nodeCommunity = communities.get(nodeCommunityId);
            WeightMap nodeToCommunityEdgesWeights = (WeightMap) node.getAttribute("nodeToCommunityEdgesWeights");

            // to self
            nodeToCommunityEdgesWeights.init(nodeCommunityId);

            neighbours = node.getNeighborNodeIterator();
            while (neighbours.hasNext()) {

                Node neighbour = neighbours.next();
                String neighbourCommunityId = (String) neighbour.getAttribute("community");

                Edge edgeBetween = node.getEdgeBetween(neighbour);
                Double edgeBetweenWeight = (Double) edgeBetween.getAttribute("weight");

                nodeCommunity.increaseEdgeWeightToCommunity(neighbourCommunityId, edgeBetweenWeight);

                nodeToCommunityEdgesWeights.increase(neighbourCommunityId, edgeBetweenWeight);
            }

            Double edgesWeightSumIncidentToNode = 0.0;

            Iterator<Edge> neighbourEdges = node.getEachEdge().iterator();
            while (neighbourEdges.hasNext()) {
                // Calculate ki
                edgesWeightSumIncidentToNode += (Double) neighbourEdges.next().getAttribute("weight");
                node.setAttribute("edgesWeightSumIncidentToNode", edgesWeightSumIncidentToNode);
            }

//            if (this.debug) {
//                System.out.println("Community " + nodeCommunityId + ": " + nodeToCommunityEdgesWeights);
//                System.out.println("Node " + node.getIndex() + ": " + nodeToCommunityEdgesWeights);
//                System.out.println("");
//            }
        }
        int changes = 0;
        do {
            initialModularity = modularity.getMeasure();
            changes = 0;
            for (Node node : graph) {
                Double maxDeltaQ = 0.0;

                String nodeCommunityId = (String) node.getAttribute("community");
                String bestCommunityToGo = nodeCommunityId;
                Double ki = (Double) node.getAttribute("edgesWeightSumIncidentToNode");
                WeightMap nodeToCommunityEdgesWeights = (WeightMap) node.getAttribute("nodeToCommunityEdgesWeights");

                // For every neighbour node of the node, test if putting it to it's
                // community, will increase the modularity.
                neighbours = node.getNeighborNodeIterator();
                while (neighbours.hasNext()) {

                    Node neighbour = neighbours.next();
                    String neighbourCommunityId = (String) neighbour.getAttribute("community");
                    HyperCommunity neighbourCommunity = this.communities.get(neighbourCommunityId);

                    Double Sin = neighbourCommunity.getInnerEdgesWeightCount();
                    Double Stot = neighbourCommunity.getAllOuterEdgesWeightCount() + Sin;
                    Double kiin = nodeToCommunityEdgesWeights.getWeight(neighbourCommunityId);
                    Double m = this.totalGraphEdgeWeight;
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

                    if (this.debug) {
                        System.out.println("Node " + node.getIndex()
                                + ": Old community=" + nodeCommunityId
                                + ", New community=" + bestCommunityToGo);
                    }
                    changes++;
                }
            }
            deltaQ = modularity.getMeasure() - initialModularity;
        } while (deltaQ > 0); // Loop until there is no improvement in modularity
//        } while (changes > 0); // Loop until there is no change

//        if (this.debug) {
//            for (Node node : graph) {
//                System.out.println("Node " + node.getIndex() + ", community: " + (String) node.getAttribute("community"));
//            }
//        }
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
        nodeCommunity.decreaseInnerEdgesWeightCount(weightToCurrentCommunity);
        nodeCommunity.increaseEdgeWeightToCommunity(newCommunityId, weightToCurrentCommunity);
        nodeCommunity.decreaseEdgeWeightToCommunity(newCommunityId, weightToNewCommunity);

        nodeCommunity.removeNode(node.getIndex());

        // Update lists in the new community
        newCommunity.increaseInnerEdgesWeightCount(weightToNewCommunity);
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

                // OF these communities
                toCommunity = this.communities.get(toCommunityId);
                toCommunity.decreaseEdgeWeightToCommunity(nodeCommunityId, weightToCommunity);
                toCommunity.increaseEdgeWeightToCommunity(newCommunityId, weightToCommunity);
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

//    private void initializeWeightCountersToCommunities() {
//
//    }
//
    public Graph foldingCommunities(Graph graph) {

        // Group nodes by community and count the edge types. Knowing the number
        // of each edge type (inner and outer) is necessary in order to create
        // the folded graph.
//        Map<String, HyperCommunity> communities = communitiesPerPhase.get(communitiesPerPhase.size() - 1);
//        ListMultimap<String, Node> multimap = ArrayListMultimap.create(); // CommunityId to nodes (contents of community)
//        HyperCommunity community;
//        for (Node node : graph) {
//            String nodeCommunityId = (String) node.getAttribute("community");
//            multimap.put(nodeCommunityId, node);
//            HyperCommunity nodeCommunity = this.communities.get(nodeCommunityId); // Get the community object 
//            // from the node's attribute.
//            //nodeCommunity.increaseNodesCount();  // increase the count of the community's nodes by 1.
//            neighbours = node.getNeighborNodeIterator();
//            while (neighbours.hasNext()) {
//                // If the neighbour and the node have the same community attribute then increase
//                // the inner edges of the community, otherwise increase the outer edges of the 
//                // community to the neighbour's community.
//                Node neighbour = neighbours.next();
//                double edgeWeightBetweenThem = (Double) node.getEdgeBetween(neighbour).getAttribute("weight");
//                String neighbourCommunity = neighbour.getAttribute("community");
//                if (neighbourCommunity.equals(node.getAttribute("community"))) {
////                    community.increaseInnerEdgesWeightCount(edgeWeightBetweenThem);
//                    nodeCommunity.addNodesSet((HashSet<Integer>) node.getAttribute("trueCommunityNodes"));
//                } else {
//                    //community.increaseEdgeWeightToCommunity(neighbourCommunity, edgeWeightBetweenThem);
//                }
//            }
//        }
        // Remove from the map the communities with 0 nodes.
//        for (Iterator<Entry<String, HyperCommunity>> it = this.communities.entrySet().iterator(); it.hasNext();) {
//            Entry<String, HyperCommunity> entry = it.next();
//            if (this.communities.get(entry.getKey()).getNodesCount() == 0) {
//                it.remove();
//            }
//        }
        // Finilize the inner edges weight count (divide it by 2)
        for (Entry<String, HyperCommunity> entry : this.communities.entrySet()) {
            this.communities.get(entry.getKey()).finilizeInnerEdgesWeightCount();
        }

        // Creation of the folded graph.
        Graph foldedGraph = new SingleGraph("communitiesPhase2");

        String edgeIdentifierWayOne,
                edgeIdentifierWayTwo,
                edgeIdentifierSelfie;
        Entry<String, HyperCommunity> communityEntry;
//        Entry<String, Double> edgeWeightToCommunity;
        List<String> edgeIdentifiers = new ArrayList<String>(); // Keep a list of edge ids so we
        // don't add the same edge twice.
        Edge edge;
        double innerEdgesWeight;

        // For every community
        for (Iterator<Entry<String, HyperCommunity>> it = this.communities.entrySet().iterator(); it.hasNext();) {
            communityEntry = it.next();
            HyperCommunity hp = communityEntry.getValue();
            if (hp.getNodesCount() != 0) {
                // and for every community that the above community is connected to,
                // create the two nodes and the between them edge, with a weight equal
                // to the total weight of the outer edges between these two communities.
                Map<String, Double> edgesWeightsToCommunities = hp.getEdgeWeightToCommunity();
                for (Iterator<Entry<String, Double>> edgesWeightsToCommunitiesIt = edgesWeightsToCommunities.entrySet().iterator(); edgesWeightsToCommunitiesIt.hasNext();) {
                    Entry<String, Double> edgesWeightsToCommunity = edgesWeightsToCommunitiesIt.next();
                    String toCommunityId = edgesWeightsToCommunity.getKey();
                    HyperCommunity toCommunity = this.communities.get(toCommunityId);
                    Double edgesWeight = edgesWeightsToCommunity.getValue();
                    if (edgesWeight != 0.0) {
                        edgeIdentifierWayOne = hp.getId() + ":" + toCommunityId;
                        edgeIdentifierWayTwo = toCommunityId + ":" + hp.getId();
                        if (!edgeIdentifiers.contains(edgeIdentifierWayOne) && !edgeIdentifiers.contains(edgeIdentifierWayTwo)) {
                            if (foldedGraph.getNode(hp.getId()) == null) {
                                foldedGraph.addNode(hp.getId()).addAttribute("trueCommunityNodes", hp.getCommunityNodes());
                            }
                            if (foldedGraph.getNode(toCommunityId) == null) {
                                foldedGraph.addNode(toCommunityId).addAttribute("trueCommunityNodes", toCommunity.getCommunityNodes());
                            }
                            edge = foldedGraph.addEdge(edgeIdentifierWayOne,
                                    hp.getId(),
                                    toCommunityId);
                            edge.addAttribute("weight", edgesWeight);
                            edge.addAttribute("ui.label", edgesWeight);

                            edgeIdentifiers.add(edgeIdentifierWayOne);
                            edgeIdentifiers.add(edgeIdentifierWayTwo);
                        }
                    }
                }
            }
            // Add a self-edge to every node, with a weight that represents the total 
            // sum of the inner edges weights of the community.
//            innerEdgesWeight = hp.getInnerEdgesWeightCount();
//            edgeIdentifierSelfie = hp.getId() + ":" + hp.getId();
//            edge = foldedGraph.addEdge(edgeIdentifierSelfie,
//                    hp.getId(),
//                    hp.getId());
//            edge.addAttribute("weight", Double.parseDouble(String.valueOf(innerEdgesWeight)));
        }

        return foldedGraph;
    }

    /**
     * After the maximum modularity was reached, revert to the original graph,
     * printing the communities (by using the same color in the nodes of the
     * same community).
     *
     * @param graph the graph where each node represents a final community.
     * @param finalGraph
     */
    public void printFinalGraph(Graph foldedGraph, Graph finalGraph, Double modularity, Double nmi) {
        UIToolbox ui = new UIToolbox(finalGraph);
        // Cleaning up the communities of the original graph
        // TO-DO: could be done by a function.
        for (Node node : finalGraph) {
            node.addAttribute("community", "");
            node.addAttribute("ui.style", "size: 20px;");
        }

        ui.addSprite("Communities", foldedGraph.getNodeCount(), 20);
        // Creating the communities count on the display screen.
//        sm = new SpriteManager(finalGraph);
//        communitiesCount = sm.addSprite("CC");
//        communitiesCount.setPosition(Units.PX, 20, 20, 0);
//        communitiesCount.setAttribute("ui.label",
//                String.format("Communities: %d", foldedGraph.getNodeCount()));
//        communitiesCount.setAttribute("ui.style", "size: 0px; text-color: rgb(150,100,100); text-size: 20;");

//        finalGraph.display(true); // display the graph on the screen.
//        modularity.init(finalGraph);
        // Creating the modularity count on the display screen.
        ui.addSprite("Modularity", modularity, 60);
        ui.addSprite("NMI", nmi, 100);
//        modularityCount = sm.addSprite("MC");
//        modularityCount.setPosition(Units.PX, 20, 60, 0);
//        modularityCount.setAttribute("ui.style", "size: 0px; text-color: rgb(150,100,100); text-size: 20;");

//        nmiCount = sm.addSprite("NMIC");
//        nmiCount.setPosition(Units.PX, 20, 100, 0);
//        nmiCount.setAttribute("ui.style", "size: 0px; text-color: rgb(150,100,100); text-size: 20;");
        // Color every node of a community with the same random color.
        color = new Random();
        for (Node community : foldedGraph) {
            r = color.nextInt(255);
            g = color.nextInt(255);
            b = color.nextInt(255);
            Set<Integer> communityNodes = (Set<Integer>) community.getAttribute("trueCommunityNodes");
            for (Iterator<Integer> node = communityNodes.iterator(); node.hasNext();) {
                Node n = finalGraph.getNode(node.next());
                n.addAttribute("community", community.getId());
                n.addAttribute("ui.style", "fill-color: rgb(" + r + "," + g + "," + b + "); size: 20px;");
//                modularityCount.setAttribute("ui.label",
//                        String.format("Modularity: %f", modularity.getMeasure()));
//                nmiCount.setAttribute("ui.label",
//                        String.format("NMI: %f", nmi.getMeasure()));
//                sleep();
            }
        }

        // If an edge connects nodes that belong to different communities, color
        // it gray.
        for (Edge edge : finalGraph.getEachEdge()) {
            if (!edge.getNode0().getAttribute("community").equals(edge.getNode1().getAttribute("community"))) {
                edge.addAttribute("ui.style", "fill-color: rgb(236,236,236);");
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
