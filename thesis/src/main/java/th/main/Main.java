package th.main;

import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.GraphParseException;
import org.graphstream.util.parser.ParseException;
import th.algorithms.propinquitydynamics.PropinquityDynamics;
import th.algorithms.propinquitydynamics.utils.PropinquityMap;

public class Main {

    public static void main(String[] args) throws IOException, GraphParseException, ParseException {
        // We must create a menu. I am too lazy right now to do it -.-
        // I've got your back bro!
        AppManager appmana = new AppManager();
        appmana.printUserMenu();
        System.exit(0);
        
//        Graph graph = new DefaultGraph("Propinquity Dynamics");
//        graph.display();
//        graph.read("../data/karate.gml");
//
//        PropinquityDynamics pd = new PropinquityDynamics();
//
//        pd.init(graph);
//        
//        for (Edge edge : graph.getEachEdge()) {
//            Node[] nodes = {edge.getNode0(), edge.getNode1()};
//
//            int maxNumEdges = 0;
//            for (Node node : nodes) {
//                if (node.getEdgeSet().size() > maxNumEdges) {
//                    maxNumEdges = node.getEdgeSet().size();
//                }
//            }
//
//            // get the propinquity
//            int prop = ((PropinquityMap) nodes[0].getAttribute("pm")).get(nodes[1].getIndex()).get();
//
//            edge.setAttribute("ui.label", prop);
//            edge.setAttribute("ui.style", "text-color:red;text-style:bold; text-size:12;size:" + prop  + ";");
//            edge.setAttribute("weight", prop);
//        }
    }
}
