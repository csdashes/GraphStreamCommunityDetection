package pdgs.propinquitydynamics;

import java.io.IOException;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.GraphParseException;

public class App {
    public static void main(String[] args) throws IOException, GraphParseException {
        Graph graph = new DefaultGraph("Propinquity Dynamics");
        graph.display();
        graph.read("../data/karate.gml");

        String[] monitoredIDs = {"220","409", "403"};
        PropinquityDynamics pd = new PropinquityDynamics();
        pd.set(2, 4);
        pd.debugOn(monitoredIDs);
        pd.statisticsOn();
        pd.init(graph);
        
        for (int i = 0; i < 40; i++) {
            pd.compute();            
        }
                
        pd.getResults();
    }
}
