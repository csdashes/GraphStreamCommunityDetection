package pdgs.propinquitydynamics;

import java.io.IOException;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.GraphParseException;

public class App {
    public static void main(String[] args) throws IOException, GraphParseException {
        Graph graph = new DefaultGraph("Propinquity Dynamics");
    }
}
