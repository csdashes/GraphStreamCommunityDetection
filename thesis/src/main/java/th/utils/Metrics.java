package th.utils;

import org.graphstream.algorithm.measure.Modularity;
import org.graphstream.algorithm.measure.NormalizedMutualInformation;
import org.graphstream.graph.Graph;

/**
 *
 * @author Anastasis Andronidis <anastasis90@yahoo.gr>
 * @author Ilias Trichopoulos <itrichop@csd.auth.gr>
 */
public class Metrics {

    public static Double GetModularity(Graph graph) {
        Modularity modularity;
        modularity = new Modularity("community", "weight");
        modularity.init(graph);
        return modularity.getMeasure();
    }

    public static Double GetNMI(Graph graph) {
        NormalizedMutualInformation nmi;
        nmi = new NormalizedMutualInformation("community", "groundTruth");
        nmi.init(graph);
        return nmi.getMeasure();
    }
}
