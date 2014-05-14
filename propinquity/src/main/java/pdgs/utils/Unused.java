<<<<<<< HEAD
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pdgs.utils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.file.FileSink;
import org.graphstream.stream.file.FileSinkGML;

/**
 *
 * @author Anastasis Andronidis <anastasis90@yahoo.gr>
 */
public class Unused {
    
    private void readFileToGML(Graph graph) throws IOException {
        String fileName = "../data/erdos-names.txt";

        try {
            List<String> lines = Files.readAllLines(Paths.get(fileName),
                    Charset.defaultCharset());
            for (String line : lines) {
                String[] a = line.split(" ", 2);

                Node n = graph.addNode(a[0]);
                n.addAttribute("name", a[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        fileName = "../data/erdos-edges.txt";

        try {
            List<String> lines = Files.readAllLines(Paths.get(fileName),
                    Charset.defaultCharset());
            for (String line : lines) {
                String[] a = line.split(" ");

                for (int i = 1; i < a.length; i++) {
                    graph.addEdge(a[0] + " " + a[i], a[0], a[i]);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        graph.display();

        FileSink fs = new FileSinkGML();

        fs.writeAll(graph, "../data/erdos02.gml");
        
        
        String[] idArray = {"95", "76", "467", "3784", "1031", "2661", "401", "4295", "6297", "2022", "421", "6927", "220", "409", "403", "503", "423", "259", "416", "451", "482", "485", "6255", "5520", "5374", "361", "787", "321", "320"};

        Graph subgraph = new DefaultGraph("erdos part");

        for (String id : idArray) {
            Node n = subgraph.addNode(id);
            n.addAttribute("label", graph.getNode(id).getAttribute("name"));
        }

        for (String id : idArray) {
            Iterator<Edge> it_n = graph.getNode(id).getEdgeIterator();

            while (it_n.hasNext()) {
                Edge e = it_n.next();

                try {
                    subgraph.addEdge(e.getId(), e.getSourceNode().getId(), e.getTargetNode().getId());
                } catch (Exception ex) {
                }
            }
        }

        subgraph.display();

    }
}
=======
///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//
//package pdgs.utils;
//
//import java.io.IOException;
//import java.nio.charset.Charset;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.util.List;
//import org.graphstream.graph.Graph;
//import org.graphstream.graph.Node;
//import org.graphstream.stream.file.FileSink;
//import org.graphstream.stream.file.FileSinkGML;
//
///**
// *
// * @author Anastasis Andronidis <anastasis90@yahoo.gr>
// */
//public class Unused {
//    
//    private void readFileToGML(Graph graph) throws IOException {
//        String fileName = "../data/erdos-names.txt";
//
//        try {
//            List<String> lines = Files.readAllLines(Paths.get(fileName),
//                    Charset.defaultCharset());
//            for (String line : lines) {
//                String[] a = line.split(" ", 2);
//
//                Node n = graph.addNode(a[0]);
//                n.addAttribute("name", a[1]);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        fileName = "../data/erdos-edges.txt";
//
//        try {
//            List<String> lines = Files.readAllLines(Paths.get(fileName),
//                    Charset.defaultCharset());
//            for (String line : lines) {
//                String[] a = line.split(" ");
//
//                for (int i = 1; i < a.length; i++) {
//                    graph.addEdge(a[0] + " " + a[i], a[0], a[i]);
//                }
//
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        graph.display();
//
//        FileSink fs = new FileSinkGML();
//
//        fs.writeAll(graph, "../data/erdos02.gml");
//    }
//}
>>>>>>> FETCH_HEAD
