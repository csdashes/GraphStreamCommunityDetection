/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package th.examples;

/**
 *
 * @author Ilias Trichopoulos <itrichop@csd.auth.gr>
 */
import java.io.IOException;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.GraphParseException;
import th.propinquitydynamics.PropinquityDynamics;

public class Example {
    public static void main(String[] args) throws IOException, GraphParseException {
        Graph graph = new DefaultGraph("Propinquity Dynamics");
        graph.display();
        graph.read("../data/example.gml");
        graph.addAttribute("ui.stylesheet", "url('../data/example.css')");
        for (Node n : graph.getEachNode()) {
            n.setAttribute("ui.label", n.getId());
        }
        
        graph.getNode("1").addAttribute("ui.class", "n");
        graph.getNode("2").addAttribute("ui.class", "nr");
        graph.getNode("3").addAttribute("ui.class", "nr");
        graph.getNode("4").addAttribute("ui.class", "nr");
        graph.getNode("5").addAttribute("ui.class", "ni");
        graph.getNode("6").addAttribute("ui.class", "ni");
        graph.getNode("7").addAttribute("ui.class", "nd");
        graph.getNode("8").addAttribute("ui.class", "nd");
    }
}
