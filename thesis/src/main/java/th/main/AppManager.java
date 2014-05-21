package th.main;

import java.io.IOException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.GraphParseException;
import org.graphstream.util.parser.ParseException;
import th.algorithms.louvain.CommunityDetectionLouvain;
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
        int selection;
        boolean flag = true;

        while (flag) {
            selection = Menu.printMenu();
            switch (selection) {
                case 1:
                    //Execute 1st function
                    ErdosSubgraphPDwithAbsoluteFractionsAndMaxToMin();
                    break;
                case 2:
                    //Execute 2st function
                    ErdozSubgraphwithOriginalPDAndTwoDisplays();
                    break;
                case 3:
                    //Execute 3rd function
                    LouvainExample();
                    break;
                case 0:
                    //Exit
                    return;
            }
        }
    }

    private void ErdosSubgraphPDwithAbsoluteFractionsAndMaxToMin() throws IOException, GraphParseException {
        Graph graph = new DefaultGraph("Propinquity Dynamics");
        graph.display();
        graph.read("../data/erdos02-subset.gml");

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

    private void ErdozSubgraphwithOriginalPDAndTwoDisplays() throws IOException, ParseException, GraphParseException {
        Graph graph = new DefaultGraph("Propinquity Dynamics");
        graph.display();
        graph.read("../data/erdos02-subset.gml");

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

    private void LouvainExample() throws IOException, GraphParseException {
        // ex-Louvain Main
        CommunityDetectionLouvain louvain = new CommunityDetectionLouvain();
//        louvain.init("data/polbooks.gml");
//        louvain.init("data/smalltest.gml");
//        louvain.init("data/dolphins.gml");
        louvain.init("../data/karate.gml");
//        louvain.init("../data/smalltest.dgs");
//        louvain.init("../data/export.gml");
        louvain.execute();
    }

    public static void WriteToFileExample() throws IOException, GraphParseException {
        Graph graph = new DefaultGraph("Propinquity Dynamics");
        graph.read("../data/erdos02-subset.gml");

        graph.removeNode(7);

        Utils.ExportGraphIntoGML(graph, "../data/export");
    }
}
