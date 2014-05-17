package pdgs.propinquitydynamics;

import java.io.IOException;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.GraphParseException;
import pdgs.utils.Utils;

public class App {
    public static void main(String[] args) throws IOException, GraphParseException {
        Graph graph = new DefaultGraph("Propinquity Dynamics");
        graph.display();
        graph.read("../data/erdos02-subset.gml");

//        String[] monitoredIDs = {"220","409", "403"};
        PropinquityDynamics pd = new PropinquityDynamics();
        pd.set(2, 10);
//        pd.debugOn(monitoredIDs);
//        pd.statisticsOn();
        pd.init(graph);
        
        for (int i = 0; i < 2; i++) {
            pd.compute();            
        }
        
        pd.applyFinalTopology();
        pd.getResultsWithFractionWeights();
        
        // Erdos02-subgraph specific vertices
        Integer[] fixedCommunity = {10,11};
        Utils.colorCommunities(graph, fixedCommunity);
//        Utils.exportGraphIntoGML(graph, "../data/export");
    }
}
