package th.utils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Element;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.AdjacencyListGraph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSink;
import org.graphstream.stream.file.FileSinkDGS;
import org.graphstream.stream.file.FileSinkGML;
import org.graphstream.util.parser.ParseException;

/**
 *
 * @author Ilias Trichopoulos <itrichop@csd.auth.gr>
 * @author Anastasis Andronidis <anastasis90@yahoo.gr>
 */
public class Utils {

    public static int DeleteLonelyVertices(Graph graph) {
        int deleted = 0;
        for (Node n : graph) {
            if (n.getDegree() == 0) {
                deleted++;
                graph.removeNode(n);
            }
        }
        return deleted;
    }

    public static void CopyCommunities(Graph sourceGraph, Graph targetGraph) throws ParseException {
        // Check if the graphs have the same amount of vertices
        if (sourceGraph.getNodeCount() != targetGraph.getNodeCount()) {
            throw new org.graphstream.util.parser.ParseException("Graphs have different number of vertices! sourceGraph: "
                    + sourceGraph.getNodeCount() + " targetGraph: "
                    + targetGraph.getNodeCount());
        }

        for (Node n : sourceGraph) {
            targetGraph.getNode(n.getIndex()).setAttribute("community", (Integer) n.getAttribute("community"));
        }
    }

    public static void ExportGraphIntoGML(Graph graph, String fileName) throws IOException {
        for (Node n : graph.getEachNode()) {
            n.addAttribute("ui_label", n.getAttribute("ui.label"));
            n.addAttribute("ui_style", n.getAttribute("ui.style"));
            n.removeAttribute("ui.label");
            n.removeAttribute("ui.style");
        }
        for (Edge e : graph.getEachEdge()) {
            e.addAttribute("weight", e.getAttribute("ui.label"));
            e.addAttribute("ui_label", e.getAttribute("ui.label"));
            e.addAttribute("ui_style", e.getAttribute("ui.style"));
            e.removeAttribute("ui.label");
            e.removeAttribute("ui.style");
        }

        FileSinkGML gml = new FileSinkGML();
        gml.writeAll(graph, fileName + ".gml");
    }

    public static void ExportToDGS(String graphFilePath, String groundTruthFilePath) throws IOException {
        Graph graph = new SingleGraph("asd", false, true);

        try {
            List<String> lines = Files.readAllLines(Paths.get(graphFilePath),
                    Charset.defaultCharset());
            for (String line : lines) {
                if (!line.startsWith("#")) {
                    String[] a = line.split("\t", 2);

                    graph.addEdge(a[0] + "and" + a[1], a[0], a[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (groundTruthFilePath != null && !groundTruthFilePath.isEmpty()) {
            List<String> lines = Files.readAllLines(Paths.get(groundTruthFilePath),
                    Charset.defaultCharset());

            int i = 0;
            for (String line : lines) {
                String[] a = line.split("\t");

                for (String s : a) {
                    if (graph.getNode(s).hasAttribute("groundTruth")) {
                        List<Integer> l = graph.getNode(s).getAttribute("groundTruth");
                        l.add(i);
                    } else {
                        List<Integer> l = new ArrayList<Integer>(100);
                        l.add(i);
                        graph.getNode(s).addAttribute("groundTruth", l);
                    }
                }
                i++;
            }
        }

        FileSink fs = new FileSinkDGS();

        File theFile = new File(graphFilePath);
        fs.writeAll(graph, "../data/" + theFile.getName().split("\\.")[0] + ".dgs");
    }

    /**
     * Clone a given graph with same node/edge structure and same attributes.
     * Copied from the latest nightly build of gs-core (should be dropped when
     * updating to gs-code 1.3 and use Graphs.clone(Graph graph) instead).
     *
     * @param g the graph to clone
     *
     * @return a copy of g
     */
    public static Graph clone(Graph g) {
        Graph copy;

        try {
            Class<? extends Graph> cls = g.getClass();
            copy = cls.getConstructor(String.class).newInstance(g.getId());
        } catch (Exception e) {
            System.err.printf("*** WARNING *** can not create a graph of %s\n",
                    g.getClass().getName());

            copy = new AdjacencyListGraph(g.getId());
        }

        copyAttributes(g, copy);

        for (int i = 0; i < g.getNodeCount(); i++) {
            Node source = g.getNode(i);
            Node target = copy.addNode(source.getId());

            copyAttributes(source, target);
        }

        for (int i = 0; i < g.getEdgeCount(); i++) {
            Edge source = g.getEdge(i);
            Edge target = copy.addEdge(source.getId(), source.getSourceNode()
                    .getId(), source.getTargetNode().getId(), source
                    .isDirected());

            copyAttributes(source, target);
        }

        return copy;
    }

    /**
     * Copy the attributes from the given source Element to the given targer
     * Element.
     *
     * @param source Element to copy attributes from.
     * @param target Element to copy attributes to.
     */
    public static void copyAttributes(Element source, Element target) {
        for (String key : source.getAttributeKeySet()) {
            Object value = source.getAttribute(key);
            value = checkedArrayOrCollectionCopy(value);

            target.setAttribute(key, value);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object checkedArrayOrCollectionCopy(Object o) {
        if (o == null) {
            return null;
        }

        if (o.getClass().isArray()) {

            Object c = Array.newInstance(o.getClass().getComponentType(),
                    Array.getLength(o));

            for (int i = 0; i < Array.getLength(o); i++) {
                Object t = checkedArrayOrCollectionCopy(Array.get(o, i));
                Array.set(c, i, t);
            }

            return c;
        }

        if (Collection.class.isAssignableFrom(o.getClass())) {
            Collection<?> t;

            try {
                t = (Collection<?>) o.getClass().newInstance();
                t.addAll((Collection) o);

                return t;
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return o;
    }
}
