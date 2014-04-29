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
import java.util.List;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
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
    }
}
