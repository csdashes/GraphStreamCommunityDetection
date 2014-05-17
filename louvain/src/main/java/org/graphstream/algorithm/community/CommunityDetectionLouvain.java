package org.graphstream.algorithm.community;

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

/**
 * Implementation of the Louvain algorithm.
 * @reference Fast unfolding of communities in large networks
 *            Vincent D. Blondel, Jean-Loup Guillaume, Renaud Lambiotte and Etienne Lefebvre.
 * @author Ilias Trichopoulos <itrichop@csd.auth.gr>
 */
public class CommunityDetectionLouvain {

    private Graph graph, // Graph used for the calculations
            finalGraph;  // The final graph printed
    private HyperCommunityManager manager;
    private List<Map<String, HyperCommunity>> communitiesPerPhase; // Every item contains
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

    /**
     * Initializing global variables.
     *
     * @param fileName the input path of the file.
     * @throws IOException
     * @throws GraphParseException
     */
    public void init(String fileName) throws IOException, GraphParseException {

        communitiesPerPhase = new ArrayList<Map<String, HyperCommunity>>();
        manager = new HyperCommunityManager();

        globalMaxQ = -0.5; // making sure to have the lowest value
        this.fileName = fileName;
    }

    /**
     * The controller of the algorithm.
     *
     * @throws IOException
     * @throws GraphParseException
     */
    public void execute() throws IOException, GraphParseException {

        this.graph = step1Init();                 // Initializing the algorithm.
        globalNewQ = findCommunities(this.graph); // Calculate the modularity after the first phase.
        finalGraph = this.graph;                  // Keep history of the first graph created.
        while (globalNewQ > globalMaxQ) {         // As long as the modularity is not the maximum
            globalMaxQ = globalNewQ;
            this.graph = foldingCommunities(this.graph); // go to the second phase (folding)
            globalNewQ = findCommunities(this.graph);    // and get the new modularity
        }
        printFinalGraph(this.graph); // After reaching the maximum modularity, 
        // print the graph on the screen.
    }

    /**
     * Initializing the first graph. Importing the file, adding weight = 1 to
     * each edge and adding attribute to each node indicating the contents of
     * it's community.
     *
     * @return the graph object imported from the file
     * @throws IOException
     * @throws GraphParseException
     */
    public Graph step1Init() throws IOException, GraphParseException {

        graph = new SingleGraph("communities");
        graph.read(fileName); // Import from the text file.

        // Add an initial weight of 1.0 in each edge
        for (Edge edge : graph.getEdgeSet()) {
            //edge.addAttribute("weight", 1.0);
            Double tmp = Double.parseDouble((String)edge.getAttribute("weight"));
            edge.changeAttribute("weight", tmp);
            edge.addAttribute("ui.label", edge.getAttribute("weight"));
        }

        // Add attribute "trueCommunityNodes" to every node, because later each
        // node will represent a community (after the folding phase) so we want 
        // to keep track of the contents of each community. Used to revert to
        // the original graph.
        for (Node node : graph) {
            Set<String> communityNodes = new HashSet<String>();
            communityNodes.add(node.getId());
            node.addAttribute("trueCommunityNodes", communityNodes);
        }
        
        nmi = new NormalizedMutualInformation("community","referenceCommunity");
        nmi.init(graph);

        return graph;
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
     * The first phase of the algorithm.
     *
     * @param graph the graph received from the initialization or the folding
     * phase
     * @return the new modularity value.
     */
    public double findCommunities(Graph graph) {

        modularity = new Modularity("community", "weight");
        modularity.init(graph);
       
        Map<String, HyperCommunity> communities = new HashMap<String, HyperCommunity>();

        for (Node node : graph) {
            node.addAttribute("ui.label", node.getId()); // Add a label in every node
            // with the id of the node.
            // Every node belongs to a different community.
            HyperCommunity community = manager.communityFactory();

            // Add community attribute to each node, so modularity alg can identify 
            //which nodes belong to each community.
            node.addAttribute("community", community.getAttribute());

            // Add the newly created community to the map
            communities.put(community.getAttribute(), community);

        }

        // Add the new map of communities in an arraylist so the communities will
        // not be mixed through the recursive steps of the algorithm.
        communitiesPerPhase.add(communities);

        do {
            initialModularity = modularity.getMeasure();
            for (Node node : graph) {
                maxModularity = -0.5;
                newModularity = -0.5;
                oldCommunity = node.getAttribute("community");
                bestCommunity = oldCommunity;

                // For every neighbour node of the node, test if putting it to it's
                // community, will increase the modularity.
                neighbours = node.getNeighborNodeIterator();
                while (neighbours.hasNext()) {

                    Node neighbour = neighbours.next();

                    // Put the node in the neighbour's community.
                    node.changeAttribute("community", neighbour.getAttribute("community"));

                    // Calculate new modularity
                    newModularity = modularity.getMeasure();

                    // Find the community that if the node is transfered to, the modularity gain
                    // is the maximum.
                    // In case of tie, the breaking rule is always to take the one that was checked last.
                    if (newModularity > maxModularity) {
                        maxModularity = newModularity;
                        bestCommunity = neighbour.getAttribute("community");
                    }
                }

                // Move node to the best community (if not already in)
                if (node.getAttribute("community") != bestCommunity) {
                    node.changeAttribute("community", bestCommunity);
                }
                // Commented for the moment. It could be used for performace improvement.
                // Count the inner and outer edges that the node that changes community is connected to, 
                // simmultaniously with the community calculation.
//                    if(node.getAttribute("community") != oldCommunity) {
//                        neighbours = node.getNeighborNodeIterator();
//                        while(neighbours.hasNext()) {
//                            String neighbourCommunity = neighbours.next().getAttribute("community");
//                            if(neighbourCommunity.equals(oldCommunity)) {
//                                innerEdgesToChange++;
//                            } else {
//                                if(outerEdgesToChange.containsKey(neighbourCommunity)) {
//                                    outerEdgesToChange.put(neighbourCommunity, outerEdgesToChange.get(neighbourCommunity) + 1);
//                                } else {
//                                    outerEdgesToChange.put(neighbourCommunity, 1);
//                                }
//                            }
//                        }
//                    }
//                    
//                    innerEdgesToChange = 0;
//                    outerEdgesToChange.clear();
            }
            deltaQ = modularity.getMeasure() - initialModularity;
        } while (deltaQ > 0); // Loop until there is no improvement in modularity
        
        System.out.println("NMI: " + nmi.getMeasure());

        return modularity.getMeasure(); // Return the maximum modularity.
    }

    /**
     * After the maximum modularity was reached, revert to the original graph,
     * printing the communities (by using the same color in the nodes of the
     * same community).
     *
     * @param graph the graph where each node represents a final community.
     */
    public void printFinalGraph(Graph graph) {

        // Cleaning up the communities of the original graph
        // TO-DO: could be done by a function.
        for (Node node : finalGraph) {
            node.addAttribute("community", "");
            node.addAttribute("ui.style", "size: 20px;");
        }

        // Creating the communities count on the display screen.
        sm = new SpriteManager(finalGraph);
        communitiesCount = sm.addSprite("CC");
        communitiesCount.setPosition(Units.PX, 20, 20, 0);
        communitiesCount.setAttribute("ui.label", // 3
                String.format("Communities: %d", graph.getNodeCount()));
        communitiesCount.setAttribute("ui.style", "size: 0px; text-color: rgb(150,100,100); text-size: 20;");

        finalGraph.display(true); // display the graph on the screen.
        modularity.init(finalGraph);

        // Creating the modularity count on the display screen.
        modularityCount = sm.addSprite("MC");
        modularityCount.setPosition(Units.PX, 20, 60, 0);
        modularityCount.setAttribute("ui.style", "size: 0px; text-color: rgb(150,100,100); text-size: 20;");
        
        nmiCount = sm.addSprite("NMIC");
        nmiCount.setPosition(Units.PX, 20, 100, 0);
        nmiCount.setAttribute("ui.style", "size: 0px; text-color: rgb(150,100,100); text-size: 20;");

        // Color every node of a community with the same random color.
        color = new Random();
        for (Node community : graph) {
            r = color.nextInt(255);
            g = color.nextInt(255);
            b = color.nextInt(255);
            Set<String> communityNodes = community.getAttribute("trueCommunityNodes");
            for (Iterator<String> node = communityNodes.iterator(); node.hasNext();) {
                Node n = finalGraph.getNode(node.next());
                n.addAttribute("community", community.getId());
                n.addAttribute("ui.style", "fill-color: rgb(" + r + "," + g + "," + b + "); size: 20px;");
                modularityCount.setAttribute("ui.label",
                        String.format("Modularity: %f", modularity.getMeasure()));
                nmiCount.setAttribute("ui.label",
                        String.format("NMI: %f", nmi.getMeasure()));
                sleep();
            }

        }

        // If an edge connects nodes that belong to different communities, color
        // it gray.
        for (Iterator<? extends Edge> it = finalGraph.getEachEdge().iterator(); it.hasNext();) {
            Edge edge = it.next();
            if (!edge.getNode0().getAttribute("community").equals(edge.getNode1().getAttribute("community"))) {
                edge.addAttribute("ui.style", "fill-color: rgb(236,236,236);");
            }
        }

    }

    /**
     * Second phase of the algorithm. Creating a graph where each node
     * represents a community.
     *
     * @param graph the output graph of the first phase.
     * @return the folded graph where each node represents a community.
     */
    public Graph foldingCommunities(Graph graph) {

        // Group nodes by community and count the edge types. Knowing the number
        // of each edge type (inner and outer) is necessary in order to create
        // the folded graph.
        Map<String, HyperCommunity> communities = communitiesPerPhase.get(communitiesPerPhase.size() - 1);
        ListMultimap<String, Node> multimap = ArrayListMultimap.create();
        HyperCommunity community;
        for (Node node : graph) {
            multimap.put((String) node.getAttribute("community"), node);
            community = communities.get(node.getAttribute("community")); // Get the community object 
            // from the node's attribute.
            community.increaseNodesCount();  // increase the count of the community's nodes by 1.
            neighbours = node.getNeighborNodeIterator();
            while (neighbours.hasNext()) {
                // If the neighbour and the node have the same community attribute then increase
                // the inner edges of the community, otherwise increase the outer edges of the 
                // community to the neighbour's community.
                Node neighbour = neighbours.next();
                double edgeWeightBetweenThem = (Double)node.getEdgeBetween(neighbour).getAttribute("weight");
                String neighbourCommunity = neighbour.getAttribute("community");
                if (neighbourCommunity.equals(node.getAttribute("community"))) {
                    community.increaseInnerEdgesCount();
                    community.increaseInnerEdgesWeightCount(edgeWeightBetweenThem);
                    community.addNodesSet((HashSet<String>) node.getAttribute("trueCommunityNodes"));
                } else {
                    //community.increaseOuterEdgesCount(neighbourCommunity);
                    community.increaseEdgeWeightToCommunity(neighbourCommunity, edgeWeightBetweenThem);
                }
            }
        }


        // Remove from the map the communities with 0 nodes.
        for (Iterator<Entry<String, HyperCommunity>> it = communities.entrySet().iterator(); it.hasNext();) {
            Entry<String, HyperCommunity> entry = it.next();
            if (communities.get(entry.getKey()).getNodesCount() == 0) {
                it.remove();
            }
        }

        // Finilize the inner edges count (divide it by 2)
        for (Iterator<Entry<String, HyperCommunity>> it = communities.entrySet().iterator(); it.hasNext();) {
            Entry<String, HyperCommunity> entry = it.next();
            communities.get(entry.getKey()).finilizeInnerEdgesCount();
        }

        // Creation of the folded graph.
        graph = new SingleGraph("communitiesPhase2");

        String edgeIdentifierWayOne,
                edgeIdentifierWayTwo,
                edgeIdentifierSelfie;
        Entry<String, HyperCommunity> communityEntry;
        Entry<String, Double> edgeWeightToCommunity;
        List<String> edgeIdentifiers = new ArrayList<String>(); // Keep a list of edge ids so we
                                                                // don't add the same edge twice.
        Edge edge;
        double innerEdgesWeight;

        // For every community
        for (Iterator<Entry<String, HyperCommunity>> it = communities.entrySet().iterator(); it.hasNext();) {
            communityEntry = it.next();
            // and for every community that the above community is connected to, create the two nodes and the between them
            // edge, with a weight equal to the number of the outer edges between these two communities.
            Map<String, Double> outerEdgesWeights = communityEntry.getValue().getEdgeWeightToCommunity();
            for (Iterator<Entry<String, Double>> outerEdgesWeightIt = outerEdgesWeights.entrySet().iterator(); outerEdgesWeightIt.hasNext();) {
                edgeWeightToCommunity = outerEdgesWeightIt.next();
                edgeIdentifierWayOne = communityEntry.getKey() + ":" + edgeWeightToCommunity.getKey();
                edgeIdentifierWayTwo = edgeWeightToCommunity.getKey() + ":" + communityEntry.getKey();
                if (!edgeIdentifiers.contains(edgeIdentifierWayOne) && !edgeIdentifiers.contains(edgeIdentifierWayTwo)) {
                    if (graph.getNode(communityEntry.getKey()) == null) {
                        graph.addNode(communityEntry.getKey()).addAttribute("trueCommunityNodes", communityEntry.getValue().getCommunityNodes());
                    }
                    if (graph.getNode(edgeWeightToCommunity.getKey()) == null) {
                        graph.addNode(edgeWeightToCommunity.getKey()).addAttribute("trueCommunityNodes", communities.get(edgeWeightToCommunity.getKey()).getCommunityNodes());
                    }
                    edge = graph.addEdge(edgeIdentifierWayOne,
                            communityEntry.getKey(),
                            edgeWeightToCommunity.getKey());
                    edge.addAttribute("weight", Double.parseDouble(String.valueOf(edgeWeightToCommunity.getValue())));
                    edge.addAttribute("ui.label", edgeWeightToCommunity.getValue());

                    edgeIdentifiers.add(edgeIdentifierWayOne);
                    edgeIdentifiers.add(edgeIdentifierWayTwo);
                }
            }
            
            // Add a self-edge to every node, with a weight that represents the inner edges of the community.
            innerEdgesWeight = communityEntry.getValue().getInnerEdgesWeightCount();
            edgeIdentifierSelfie = communityEntry.getKey() + ":" + communityEntry.getKey();
            edge = graph.addEdge(edgeIdentifierSelfie,
                    communityEntry.getKey(),
                    communityEntry.getKey());
            edge.addAttribute("weight", Double.parseDouble(String.valueOf(innerEdgesWeight)));
            // edge.addAttribute("ui.label", innerEdges); // Used only when displaying the graph (optional)
        }

        return graph;
    }
}