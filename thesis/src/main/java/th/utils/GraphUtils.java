package th.utils;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Element;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.AdjacencyListGraph;
import org.graphstream.util.parser.ParseException;

/**
 *
 * @author Anastasis Andronidis <anastasis90@yahoo.gr>
 * @author Ilias Trichopoulos <itrichop@csd.auth.gr>
 */
public class GraphUtils {

    public static void ParseOverlapCommunities(Graph graph) {
        String attribute = "groundTruth";

        for (Node n : graph) {
            if (n.hasAttribute(attribute) && n.getAttribute(attribute) instanceof String) {
                String attr = (String) n.getAttribute(attribute);
                String[] sa = attr.split("\\[", 2)[1].split("\\]", 2)[0].split(", ");

                Set<Integer> set = new HashSet<>(4);
                for (String s : sa) {
                    set.add(Integer.parseInt(s));
                }

                n.setAttribute(attribute, set);
            }
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
                if (((HashSet<Integer>) n.getAttribute("community")).size() == 1) {
                    targetGraph.getNode(n.getIndex()).setAttribute("community", ((HashSet<Integer>) n.getAttribute("community")).stream().findFirst().get());
                } else {
                    targetGraph.getNode(n.getIndex()).setAttribute("community", (HashSet<Integer>) n.getAttribute("community"));
                }
            } else {
                targetGraph.getNode(n.getIndex()).setAttribute("community", (Integer) n.getAttribute("community"));
            }
        }
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
