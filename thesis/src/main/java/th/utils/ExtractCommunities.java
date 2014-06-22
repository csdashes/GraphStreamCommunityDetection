package th.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

/**
 *
 * @author Anastasis Andronidis <anastasis90@yahoo.gr>
 * @author Ilias Trichopoulos <itrichop@csd.auth.gr>
 */
public class ExtractCommunities {

    private static SortedMap<Integer, Integer> GetNeighborCommunityFrequencies(Node n) {
        Map<Integer, Integer> map = new HashMap<>(10);
        SortedMap<Integer, Integer> sorted_map = new TreeMap<>(Collections.reverseOrder());

        Iterator<Node> it = n.getNeighborNodeIterator();
        while (it.hasNext()) {
            Node nn = it.next();

            if (nn.hasAttribute("community") && nn.getAttribute("community") != null) {
                Collection<Integer> communities;
                if (nn.getAttribute("community") instanceof Collection<?>) {
                    communities = (Collection<Integer>) nn.getAttribute("community");
                } else {
                    communities = Arrays.asList((Integer) nn.getAttribute("community"));
                }

                communities.stream().forEach((com) -> {
                    map.compute(com, (k, v) -> v == null ? 1 : v + 1);
                });
            }
        }

        map.entrySet().stream().forEach((e) -> {
            sorted_map.put(e.getValue(), e.getKey());
        });
        return sorted_map;
    }

    public static void Shark(Graph graph) {
        boolean uncommunitizedVertExist;

        do {
            uncommunitizedVertExist = false;
            for (Node n : graph) {
                if (n.getDegree() > 0 && n.getAttribute("community") == null) {
                    uncommunitizedVertExist = true;
                    SortedMap<Integer, Integer> neighborCommunites = GetNeighborCommunityFrequencies(n);
                    if (neighborCommunites.size() > 0) {
                        Integer com = neighborCommunites.entrySet().iterator().next().getValue();
                        n.addAttribute("community", com);
                    }
                }
            }
        } while (uncommunitizedVertExist);
    }

    private static boolean AreEqual(Double d1, Double d2) {
        return Math.round(d1 * 100.0) / 100.0 == Math.round(d2 * 100.0) / 100.0;
    }

    private static boolean AreWeTheStrongestEdge(Node n, Double w) {
        // We add only if we are the strongest edge to this vertex
        boolean areWeTheStrongestEdge = true;
        for (Edge ne : n.getEdgeSet()) {
            boolean c = Math.round(w * 100.0) / 100.0 < Math.round((Double) ne.getAttribute("weight") * 100.0) / 100.0;
            if (c) {
                areWeTheStrongestEdge = false;
            }
        }
        return areWeTheStrongestEdge;
    }

    private static void AddNextSteps(Node n, Queue<Node> head, SortedMap<Double, Set<Node>> consequent, Double currentSearchWeight, int community, boolean overlap) {
        for (Edge e : n.getEachEdge()) {
            Double weight = e.getAttribute("weight");
            Node neightbor = e.getOpposite(n);

            if (AreWeTheStrongestEdge(neightbor, weight)) {
                if (neightbor.hasAttribute("visited")) {
                    if (overlap && neightbor.hasAttribute("community")) {
                        ((Set<Integer>) neightbor.getAttribute("community")).add(community);
                    }
                } else if (weight < currentSearchWeight) {
                    consequent.computeIfAbsent(weight, (k) -> new HashSet<>(30)).add(neightbor);
                    // If we are equal, go and it to head
                } else if (AreEqual(weight, currentSearchWeight)) {
                    if (!head.contains(neightbor)) {
                        head.add(neightbor);
                    }
                }
            }
        }
    }

    private static void AddNodeToCommunity(Node n, int community, boolean overlap) {
        if (overlap) {
            Set<Integer> s = new HashSet<>(4);
            s.add(community);
            n.addAttribute("community", s);
        } else {
            n.addAttribute("community", community);
        }
    }

    private static void WeightedBFS(Node n, int community, Double currentSearchWeight, boolean overlap) {
        SortedMap<Double, Set<Node>> subsequent = new TreeMap<>(Collections.reverseOrder());
        Queue<Node> head = new LinkedList<>();

        // Find max weight for first iter.
        AddNextSteps(n, head, subsequent, currentSearchWeight, community, overlap);
        n.addAttribute("visited", 1);
        AddNodeToCommunity(n, community, overlap);

        while (!subsequent.isEmpty() || !head.isEmpty()) {
            while (!head.isEmpty()) {
                Node next = head.poll();
                AddNextSteps(next, head, subsequent, currentSearchWeight, community, overlap);
                next.addAttribute("visited", 1);
                AddNodeToCommunity(next, community, overlap);
            }

            if (!subsequent.isEmpty()) {
                currentSearchWeight = subsequent.firstKey();
                head.addAll(subsequent.remove(subsequent.firstKey()));
            }
        }
    }

    public static int MaxToMin(Graph graph, boolean overlap) {
        SortedMap<Double, Set<Node>> groupedVertices = new TreeMap<>(Collections.reverseOrder());

        graph.getEdgeSet().stream().forEach((e) -> {
            Double weight = e.getAttribute("weight");
            groupedVertices.computeIfAbsent(weight, (k) -> new HashSet<>(30)).addAll(Arrays.asList(e.getNode0(), e.getNode1()));
        });

        int communityNum = 0;
        for (Entry<Double, Set<Node>> en : groupedVertices.entrySet()) {
            for (Node n : en.getValue()) {
                if (!n.hasAttribute("visited")) {
                    WeightedBFS(n, communityNum++, en.getKey(), overlap);
                }
            }
        }

        // Delete visited attribute
        for (Node n : graph) {
            n.removeAttribute("visited");
        }

        return communityNum;
    }

    public static int BFS(Graph graph) {
        return BFS(graph, new Integer[0]);
    }

    /**
     * Find disjoined communities by BFS. The first community has number 1.
     *
     * @param graph The graph that we will extract the communities.
     * @param fixedIDs If we want some vertices to be in a custom community,
     * then this array should contain they IDs.
     *
     * @return The number of communities detected.
     */
    public static int BFS(Graph graph, Integer[] fixedIDs) {
        int communityNum = 0;

        if (fixedIDs.length > 0) {
            communityNum++;
        }
        for (Integer id : fixedIDs) {
            graph.getNode(id).setAttribute("visited", 1);
            graph.getNode(id).setAttribute("community", communityNum);
        }

        for (Node n : graph.getEachNode()) {
            if (!n.hasAttribute("visited")) {
                n.setAttribute("visited", 1);
                if (n.getDegree() == 0) {
                    continue;
                }
                n.setAttribute("community", ++communityNum);

                // Go for BFS
                Iterator<Node> breadth = n.getBreadthFirstIterator();
                while (breadth.hasNext()) {
                    Node next = breadth.next();
                    if (!next.hasAttribute("visited")) {
                        next.setAttribute("visited", 1);
                        next.setAttribute("community", communityNum);
                    }
                }
            }
        }

        // Delete visited attribute
        for (Node n : graph.getEachNode()) {
            n.removeAttribute("visited");
        }

        return communityNum;
    }
}
