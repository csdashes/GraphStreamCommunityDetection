package th.main;

import java.io.IOException;
import java.util.HashMap;
import org.graphstream.algorithm.measure.Modularity;
import org.graphstream.algorithm.measure.NormalizedMutualInformation;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.GraphParseException;
import org.graphstream.util.parser.ParseException;
import th.algorithms.louvain.CommunityDetectionLouvain;
import th.algorithms.louvain.CommunityDetectionLouvain2;
import th.algorithms.louvain.utils.WeightMap;
import th.algorithms.propinquitydynamics.PropinquityDynamics;
import static th.algorithms.propinquitydynamics.utils.Utils.FractionWithNumberOfEdges;
import static th.algorithms.propinquitydynamics.utils.Utils.FractionWithTotalPropinquity;
import th.utils.ExtractCommunities;
import th.utils.Menu;
import th.utils.UIToolbox;
import th.utils.Utils;

/**
 *
 * @author Ilias Trichopoulos <itrichop@csd.auth.gr>
 */
public class AppManager {

    public void printUserMenu() throws IOException, GraphParseException, ParseException {
        int methodSelection, datasetSelection;
        String datasetFile = null;
        boolean flag = true;

        while (flag) {
            methodSelection = Menu.printMenu();
            if (methodSelection != 0) {
                datasetSelection = Menu.printDatasetMenu();
                switch (datasetSelection) {
                    case 1:
                        datasetFile = "../data/polbooks.gml";
                        break;
                    case 2:
                        datasetFile = "../data/dolphins.gml";
                        break;
                    case 3:
                        datasetFile = "../data/karate.gml";
                        break;
                    case 4:
                        datasetFile = "../data/erdos02.gml";
                        break;
                    case 5:
                        datasetFile = "../data/erdos02-subset.gml";
                        break;
                    case 0:
                        return;
                }
            }
            switch (methodSelection) {
                case 1:
                    //Execute 1st function
                    ErdosSubgraphPDwithAbsoluteFractionsAndMaxToMin(datasetFile);
                    break;
                case 2:
                    //Execute 2st function
                    ErdozSubgraphwithOriginalPDAndTwoDisplays(datasetFile);
                    break;
                case 3:
                    //Execute 3rd function
                    LouvainExample(datasetFile);
                    break;
                case 0:
                    //Exit
                    return;
            }
        }
    }

    private void ErdosSubgraphPDwithAbsoluteFractionsAndMaxToMin(String datasetFile) throws IOException, GraphParseException {
        Graph graph = new DefaultGraph("Propinquity Dynamics");
        graph.display();
        graph.read(datasetFile);

        PropinquityDynamics pd = new PropinquityDynamics();
        pd.set(2, 10);

        pd.init(graph);

        int i = 0;
        // We need to be sure that we dont have an infinite loop
        while (i < 100 && !pd.didAbsoluteConvergence()) {
            pd.compute();
            i++;
        }
        pd.applyFinalTopology();

        // Set the edge weight to fractions
        FractionWithNumberOfEdges(graph);
        //Utils.FractionWithTotalPropinquity(graph);

        // Use our custom extraction algorithm to retrive internal communities
        ExtractCommunities.MaxToMin(graph);
        UIToolbox.ColorCommunities(graph);
    }

    private void ErdozSubgraphwithOriginalPDAndTwoDisplays(String datasetFile) throws IOException, ParseException, GraphParseException {
        Graph graph = new DefaultGraph("Propinquity Dynamics");
        graph.display();
        graph.read(datasetFile);

        PropinquityDynamics pd = new PropinquityDynamics();
        pd.set(2, 10);
        pd.init(graph);

        int i = 0;
        // We need to be sure that we dont have an infinite loop
        while (i < 100 && !pd.didAbsoluteConvergence()) {
            pd.compute();
            i++;
        }

        pd.applyFinalTopology();

        // Erdos02-subgraph specific vertices
        Integer[] fixedCommunity = {10, 11};
        ExtractCommunities.BFS(graph, fixedCommunity);
        UIToolbox.ColorCommunities(graph);
        FractionWithTotalPropinquity(graph);

        Graph originGraph = new DefaultGraph("Propinquity Dynamics");
        originGraph.display();
        originGraph.read("../data/erdos02-subset.gml");
        Utils.CopyCommunities(graph, originGraph);

        UIToolbox.ColorCommunities(originGraph);
    }

    private void LouvainExample(String datasetFile) throws IOException, GraphParseException {
        double globalNewQ;
        double globalMaxQ = Double.NEGATIVE_INFINITY;
        HashMap<String, String> changes;
        long start = System.currentTimeMillis();
        Graph graph = new DefaultGraph("Louvain");

//        graph.read("../data/smalltest.dgs");
        graph.read(datasetFile);
        graph.display();

        CommunityDetectionLouvain2 louvain = new CommunityDetectionLouvain2();
//        louvain.debugOn();

        // Initialize louvain
        louvain.init(graph);

        // Keep a copy of the initial graph. The final communities will be displayed
        // on this graph
        Graph initialGraph = Utils.clone(graph);

        // Initialize modularity on the initial graph
        Modularity modularity = new Modularity("community", "weight");
        modularity.init(initialGraph);

        // Execute first phase of the algorithm.
        // It will return which changes need to be made in the initial graph
        // to represent the results of the first phase.
        changes = louvain.findCommunities(graph); // First Phase
        louvain.applyChanges(initialGraph, changes);

        //debugging
//        for(Node n : initialGraph) {
//                WeightMap nodeToCommunityEdgesWeights = (WeightMap) n.getAttribute("nodeToCommunityEdgesWeights");
//                System.out.println("Node "+n.getId()+", nodeToCommunityEdgesWeights" + nodeToCommunityEdgesWeights);
//        }
        // Get the updated modularity after the first phase
        globalNewQ = modularity.getMeasure();

        Graph folded = null;
        // As long as the modularity is not the maximum, perform the second
        // phase of the algorith (folding). The folding phase, will create a new
        // graph object (named "folded"), in which each node will represent a
        // community of the previous phase. Then, we execute the first phase,
        // using the folded graph, and we apply the changes on the initial graph.
        // If the new modularity, after the next run of the first phase, is larger
        // than the old one, execute the loop again, until no more improvment is
        // possible.
        while (globalNewQ > globalMaxQ) {
            globalMaxQ = globalNewQ;
            folded = louvain.foldingCommunities(graph);
            changes = louvain.findCommunities(folded);
            louvain.applyChanges(initialGraph, changes);
            globalNewQ = modularity.getMeasure();
            graph = folded;
        }

        System.out.println("\n===== " + (System.currentTimeMillis() - start) + " =====");

        // Color the communities on the initial graph and add the sprites.
        initialGraph.display();
        int communitiez = UIToolbox.ColorCommunities(initialGraph);
        UIToolbox ui = new UIToolbox(initialGraph);
        NormalizedMutualInformation nmi;
        nmi = new NormalizedMutualInformation("community", "groundTruth");
        nmi.init(initialGraph);
        ui.addSprite("NMI", nmi.getMeasure(), 100);
        ui.addSprite("Modularity", globalMaxQ, 60);
        ui.addSprite("Communities", communitiez, 20);
    }

    public static void WriteToFileExample() throws IOException, GraphParseException {
        Graph graph = new DefaultGraph("Propinquity Dynamics");
        graph.read("../data/erdos02-subset.gml");

        graph.removeNode(7);

        Utils.ExportGraphIntoGML(graph, "../data/export");
    }
}
