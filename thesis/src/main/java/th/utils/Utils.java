package th.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
 * @author Anastasis Andronidis <anastasis90@yahoo.gr>
 * @author Ilias Trichopoulos <itrichop@csd.auth.gr>
 */
public class Utils {

    private static String join(List<String> s, String delimiter) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < s.size() - 1; i++) {
            builder.append(s.get(i));
            builder.append(delimiter);
        }

        builder.append(s.get(s.size() - 1));

        return builder.toString();
    }

    public static void DumpCommunities(Graph graph, String overlapCommunitiesFilePath) throws FileNotFoundException, UnsupportedEncodingException {
        try (PrintWriter writer = new PrintWriter(overlapCommunitiesFilePath, "UTF-8")) {
            Map<Integer, List<String>> comMap = new HashMap<>(30);

            for (Node n : graph) {
                // TODO: This is also a waste of resources. Too many copies
                List<Integer> nodeCommunities;
                if (n.getAttribute("community") instanceof HashSet<?>) {
                    nodeCommunities = new ArrayList<>((HashSet<Integer>) n.getAttribute("community"));
                } else {
                    Integer com = (Integer) n.getAttribute("community");
                    nodeCommunities = new ArrayList<>(Arrays.asList(com));
                }

                for (Integer com : nodeCommunities) {
                    // TODO: this is not optimal. in every iteration we create a wasted ArrayList
                    comMap.merge(com, new ArrayList<>(Arrays.asList(n.getId())), (v1, v2) -> {
                        v1.addAll(v2);
                        return v1;
                    });
                }
            }

            comMap.forEach((k, v) -> {
                writer.println(join(v, "\t"));
            });
        }
    }

    public static void InitWeights(Graph graph) {
        for (Edge e : graph.getEachEdge()) {
            e.addAttribute("weight", 1.0);
        }
    }

    public static void ResetCommunities(Graph graph) {
        for (Node n : graph) {
            n.removeAttribute("community");
        }
    }

    public static int FindLonelyVertices(Graph graph) {
        int solo = 0;

        for (Node n : graph) {
            if (n.getDegree() == 0) {
                solo++;
            }
        }

        return solo;
    }

    public static int DeleteLonelyVertices(Graph graph) {
        int deleted = 0;
        boolean flag;

        do {
            flag = false;
            for (Node n : graph) {
                if (n.getDegree() == 0) {
                    deleted++;
                    flag = true;
                    graph.removeNode(n);
                }
            }
        } while (flag);
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
            if (n.getAttribute("community") instanceof HashSet<?>) {
                targetGraph.getNode(n.getIndex()).setAttribute("community", (HashSet<Integer>) n.getAttribute("community"));
            } else {
                targetGraph.getNode(n.getIndex()).setAttribute("community", (Integer) n.getAttribute("community"));
            }
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
