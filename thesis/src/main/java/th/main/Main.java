package th.main;

import java.io.IOException;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.GraphParseException;
import org.graphstream.util.parser.ParseException;
import th.algorithms.propinquitydynamics.PropinquityDynamics;
import th.utils.ExtractCommunities;
import th.utils.UIToolbox;
import th.utils.Utils;

public class Main {

    public static void ErdosSubgraphPDwithAbsoluteFractionsAndMaxToMin() throws IOException, GraphParseException {
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
        Utils.FractionWithNumberOfEdges(graph);
        //Utils.FractionWithTotalPropinquity(graph);
        
        // Use our custom extraction algorithm to retrive internal communities
        ExtractCommunities.MaxToMin(graph);
        UIToolbox.ColorCommunities(graph);
    }

    public static void main(String[] args) throws IOException, GraphParseException, ParseException {
        Graph graph = new DefaultGraph("Propinquity Dynamics");
        graph.display();
        graph.read("../data/erdos02-subset.gml");

//        String[] monitoredIDs = {"220","409", "403"};
        PropinquityDynamics pd = new PropinquityDynamics();
        pd.set(2, 10);
//        pd.debugOn(monitoredIDs);
//        pd.statisticsOn();
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
        Utils.FractionWithTotalPropinquity(graph);

        Graph originGraph = new DefaultGraph("Propinquity Dynamics");
        originGraph.display();
        originGraph.read("../data/erdos02-subset.gml");
        Utils.CopyCommunities(graph, originGraph);
        UIToolbox.ColorCommunities(originGraph);

//        Utils.ExportGraphIntoGML(graph, "../data/export");
        
        
        // ex-Louvain Main
//        CommunityDetectionLouvain louvain = new CommunityDetectionLouvain();
//        louvain.init("data/polbooks.gml");
//        louvain.init("data/smalltest.gml");
//        louvain.init("data/dolphins.gml");
//        louvain.init("../data/karate.gml");
//        louvain.init("../data/smalltest.dgs");
//        louvain.init("../data/export.gml");
//        louvain.execute();
    }
}
