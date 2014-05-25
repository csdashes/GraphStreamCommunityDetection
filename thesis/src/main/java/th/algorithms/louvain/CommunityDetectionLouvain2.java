package th.algorithms.louvain;

import th.algorithms.louvain.utils.HyperCommunityManager;
import th.algorithms.louvain.utils.HyperCommunity;
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
 *
 * @reference Fast unfolding of communities in large networks Vincent D.
 * Blondel, Jean-Loup Guillaume, Renaud Lambiotte and Etienne Lefebvre.
 * @author Ilias Trichopoulos <itrichop@csd.auth.gr>
 */
public class CommunityDetectionLouvain2 {

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

    private boolean debug = false;
    private Double totalGraphEdgeWeight;

    /**
     * Initializing global variables.
     *
     * @param fileName the input path of the file.
     * @throws IOException
     * @throws GraphParseException
     */
    public void init(String fileName) throws IOException, GraphParseException {

        this.graph = new SingleGraph("communities");
        this.graph.read(fileName); // Import from the text file.
        //this.graph.display();
        
        this.totalGraphEdgeWeight = 0.0;
        
        // Add weight to each edge
        for (Edge edge : this.graph.getEdgeSet()) {
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

        // Add attribute "trueCommunityNodes" to every node, because later each
        // node will represent a community (after the folding phase) so we want 
        // to keep track of the contents of each community. Used to revert to
        // the original graph.        
        for (Node node : this.graph) {
            Set<Integer> trueCommunityNodes = new HashSet<Integer>();
            trueCommunityNodes.add(node.getIndex());
            node.addAttribute("trueCommunityNodes", trueCommunityNodes);
        }

        this.communitiesPerPhase = new ArrayList<Map<String, HyperCommunity>>();
        this.manager = new HyperCommunityManager();

        this.globalMaxQ = -0.5; // making sure to have the lowest value
        this.fileName = fileName;
        
    }

    /**
     * The controller of the algorithm.
     *
     * @throws IOException
     * @throws GraphParseException
     */
    public void execute() throws IOException, GraphParseException {

        this.globalNewQ = findCommunities(); // Calculate the modularity after the first phase.
//        finalGraph = this.graph;                  // Keep history of the first graph created.
//        while (this.globalNewQ > this.globalMaxQ) {         // As long as the modularity is not the maximum
//            this.globalMaxQ = this.globalNewQ;
//            this.graph = foldingCommunities(this.graph); // go to the second phase (folding)
//            globalNewQ = findCommunities();    // and get the new modularity
//        }
//        printFinalGraph(this.graph); // After reaching the maximum modularity, 
        // print the graph on the screen.
    }

    public double findCommunities() {

        // Mapping between community id and community object
        Map<String, HyperCommunity> communities = new HashMap<String, HyperCommunity>();

        for (Node node : this.graph) {
            node.addAttribute("ui.label", node.getIndex()); // Add a label in every node
            // with the index id of the node.
            // Every node belongs to a different community.
            HyperCommunity community = manager.communityFactory();

            // Add community attribute to each node, so modularity alg can identify 
            //which nodes belong to each community.
            node.addAttribute("community", community.getId());

            // Add the newly created community to the map
            communities.put(community.getId(), community);

            // This will keep track of the summary of the edge weights between each node
            // to each community.
            node.addAttribute("nodeToCommunityEdgesWeights", new HashMap<String, Double>());
        }

        // Add the new map of communities in an arraylist so the communities will
        // not be mixed through the recursive steps of the algorithm.
        this.communitiesPerPhase.add(communities);

        for (Node node : this.graph) {
            String nodeCommunityId = (String) node.getAttribute("community");
            HyperCommunity nodeCommunity = communities.get(nodeCommunityId);
            HashMap<String, Double> nodeToCommunityEdgesWeights = (HashMap<String, Double>) node.getAttribute("nodeToCommunityEdgesWeights");

            neighbours = node.getNeighborNodeIterator();
            while (neighbours.hasNext()) {

                Node neighbour = neighbours.next();
                String neighbourCommunityId = (String) neighbour.getAttribute("community");

                Edge edgeBetween = node.getEdgeBetween(neighbour);
                Double edgeBetweenWeight = (Double) edgeBetween.getAttribute("weight");

                nodeCommunity.increaseEdgeWeightToCommunity(neighbourCommunityId, edgeBetweenWeight);

                nodeToCommunityEdgesWeights.put(neighbourCommunityId, edgeBetweenWeight);
            }

            Double edgesWeightSumIncidentToNode = 0.0;

            Iterator<Edge> neighbourEdges = node.getEachEdge().iterator();
            while (neighbourEdges.hasNext()) {
                // Calculate ki
                edgesWeightSumIncidentToNode += (Double) neighbourEdges.next().getAttribute("weight");
                node.setAttribute("edgesWeightSumIncidentToNode", edgesWeightSumIncidentToNode);
            }

            if (this.debug) {
                System.out.println("Community " + nodeCommunityId + ": " + nodeToCommunityEdgesWeights);
                System.out.println("Node " + node.getIndex() + ": " + nodeToCommunityEdgesWeights);
            }
        }

        do {
            //initialModularity = modularity.getMeasure();
            for (Node node : graph) {
//                maxModularity = -0.5;
//                newModularity = -0.5;
//                oldCommunity = node.getAttribute("community");
//                bestCommunity = oldCommunity;
                Double maxDeltaQ = 0.0;
                String bestCommunityToGo = null;

                Double ki = (Double) node.getAttribute("edgesWeightSumIncidentToNode");
                HashMap<String, Double> nodeToCommunityEdgesWeights = (HashMap<String, Double>) node.getAttribute("nodeToCommunityEdgesWeights");


                // For every neighbour node of the node, test if putting it to it's
                // community, will increase the modularity.
                neighbours = node.getNeighborNodeIterator();
                while (neighbours.hasNext()) {

                    Node neighbour = neighbours.next();
                    String neighbourCommunityId = (String) neighbour.getAttribute("community");
                    HyperCommunity neighbourCommunity = communities.get(neighbourCommunityId);

                    Double Sin = neighbourCommunity.getInnerEdgesWeightCount();
                    Double Stot = neighbourCommunity.getAllOuterEdgesWeightCount();
                    Double kiin = nodeToCommunityEdgesWeights.get(neighbourCommunityId);
                    Double m = this.totalGraphEdgeWeight;
                    
                    Double deltaQ = calculateDeltaQ(Sin,Stot,ki,kiin,m);
                    
                    if(deltaQ > maxDeltaQ) {
                        maxDeltaQ = deltaQ;
                        bestCommunityToGo = neighbourCommunityId;
                    }
                    
                    if(this.debug) {
                        System.out.println("Node " + node.getIndex() + " goes to community " + neighbourCommunityId + ": " + deltaQ);
                    }
                }
            }
        } while (false);

        return 0;
    }
    
    private Double calculateDeltaQ(Double Sin, Double Stot, Double ki, Double kiin, Double m) {
        Double doubleM = m*2;
        Double firstFraction = (Sin + kiin)/doubleM;
        Double secondFraction = Math.pow((Stot  + ki)/doubleM,2.0);
        Double firstStatement = firstFraction - secondFraction;
        Double thirdFraction = Sin/doubleM;
        Double fourthFraction = Math.pow(Stot/doubleM, 2.0);
        Double fifthFraction = Math.pow(ki/doubleM, 2.0);
        Double secondStatement = thirdFraction - fourthFraction - fifthFraction;
        
        return firstStatement - secondStatement;
    }

//    private void initializeWeightCountersToCommunities() {
//
//    }
//
//    public Graph foldingCommunities(Graph graph) {
//
//    }
//
//    public void printFinalGraph(Graph graph) {
//
//    }
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
