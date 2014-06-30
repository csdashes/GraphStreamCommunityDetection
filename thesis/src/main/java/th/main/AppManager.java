package th.main;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.GraphParseException;
import org.graphstream.stream.file.FileSink;
import org.graphstream.stream.file.FileSinkGML;
import org.graphstream.util.parser.ParseException;
import th.algorithms.louvain.CommunityDetectionLouvain;
import th.algorithms.propinquitydynamics.LocalPropinquityDynamics;
import th.algorithms.propinquitydynamics.PropinquityDynamics;
import static th.algorithms.propinquitydynamics.utils.Utils.FractionWithNumberOfEdges;
import static th.algorithms.propinquitydynamics.utils.Utils.FractionWithTotalPropinquity;
import static th.algorithms.propinquitydynamics.utils.Utils.MaxPropinquityToNonNeighbor;
import static th.algorithms.propinquitydynamics.utils.Utils.SetPDWeights;
import th.utils.ExtractCommunities;
import static th.utils.ExtractCommunities.Shark;
import th.utils.FileUtils;
import th.utils.GraphUtils;
import static th.utils.GraphUtils.CopyCommunities;
import static th.utils.GraphUtils.FindLonelyVertices;
import static th.utils.GraphUtils.InitWeights;
import static th.utils.GraphUtils.ResetCommunities;
import th.utils.Menu;
import th.utils.Metrics;
import static th.utils.Metrics.GetModularity;
import static th.utils.Metrics.GetNMI;
import th.utils.Statistics;
import static th.utils.Statistics.DegreeStatistics;
import static th.utils.Statistics.MaxPropinquityToGraph;
import th.utils.Statistics.RangeABStatistics;
import th.utils.UIToolbox;

/**
 *
 * @author Anastasis Andronidis <anastasis90@yahoo.gr>
 * @author Ilias Trichopoulos <itrichop@csd.auth.gr>
 */
public class AppManager {

    public void printUserMenu() throws IOException, GraphParseException, ParseException, InterruptedException {
        int methodSelection, datasetSelection;
        String datasetFile = null;
        boolean flag = true;

//        while (flag) {
//            methodSelection = Menu.printMenu();
//            if (methodSelection != 0) {
//                datasetSelection = Menu.printDatasetMenu();
//                switch (datasetSelection) {
//                    case 1:
//                        datasetFile = "../data/polbooks.gml";
//                        break;
//                    case 2:
//                        datasetFile = "../data/dolphins.gml";
//                        break;
//                    case 3:
//                        datasetFile = "../data/karate.gml";
//                        break;
//                    case 4:
//                        datasetFile = "../data/erdos02.gml";
//                        break;
//                    case 5:
//                        datasetFile = "../data/erdos02-subset.gml";
//                        break;
//                    case 0:
//                        return;
//                }
//            }
//            switch (methodSelection) {
//                case 1:
//                    //Execute 1st function
//                    ErdosSubgraphPDwithAbsoluteFractionsAndMaxToMin(datasetFile);
//                    break;
//                case 2:
//                    //Execute 2st function
//                    ErdozSubgraphwithOriginalPDAndTwoDisplays(datasetFile);
//                    break;
//                case 3:
//                    //Execute 3rd function
//                    LouvainExample(datasetFile);
//                    break;
//                case 4:
//                    //Execute 4rd function
//                    PDOriginalStatics(datasetFile);
//                    break;
//                case 0:
//                    //Exit
//                    return;
//            }
//        }

//        String[] datasets = {"../data/polbooks.gml", "../data/dolphins.gml", "../data/karate.gml"};      
//        for (String dataset : datasets) {
//            RangeAB(dataset);
//        }
//        CompareExtractions(datasets[2]);
        PDLocalAB("../data/karate.gml");
        PDLocalAB("../data/dolphins.gml");
        PDLocalAB("../data/polbooks.gml");
        PDLocalAB("../data/football.gml");
    }

    private void PDOriginalStatics(String datasetFile) throws IOException, GraphParseException {
        Graph graph = new DefaultGraph("Propinquity Dynamics");
        graph.read(datasetFile);
        String filename = "";

        PropinquityDynamics pd = new PropinquityDynamics();
        pd.set(2, 10);

        pd.init(graph);

        File theFile = new File(datasetFile);
//        Statistics.PDStatistics(graph, theFile.getName().split("\\.")[0], 2, 10);
        Statistics.maxPDToAnyNode(graph, theFile.getName().split("\\.")[0]);
        Statistics.maxPDToAnyNeighbor(graph, theFile.getName().split("\\.")[0]);
//        Statistics.exportNodePDStatistics(graph, filename);
    }

    private void CompareExtractions(String datasetFile) throws IOException, GraphParseException, ParseException {
        int a = 0, b = 6;
        Graph graph = new DefaultGraph("Propinquity Dynamics");
        graph.read(datasetFile);

        PropinquityDynamics pd = new PropinquityDynamics();
        pd.set(a, b);

        pd.init(graph);

        int i = 0;
        // We need to be sure that we dont have an infinite loop
        while (i < 100 && !pd.didAbsoluteConvergence()) {
            pd.compute();
            i++;
        }
        pd.applyFinalTopology();

        int uncommunitized = FindLonelyVertices(graph);
        System.out.println("a" + a + "b" + b);
        System.out.println("Un-communitized Vertices: " + uncommunitized + " Number of Iterations: " + i);

        int com, overlaps;
        int[] output;
//        com = ExtractCommunities.BFS(graph);
//        ResetCommunities(graph);
//        System.out.println("BFS found: " + com);

        SetPDWeights(graph);
        output = ExtractCommunities.MaxToMin(graph, true);
        com = output[0];
        overlaps = output[1];
        System.out.println("MaxToMin(normal weihts) found: " + com + " communities. And: " + overlaps + " overlaps");

//        FractionWithNumberOfEdges(graph);
//        com = ExtractCommunities.MaxToMin(graph);
//        System.out.println("MaxToMin(PD/degree) found: " + com);
//        FractionWithTotalPropinquity(graph);
//        com = ExtractCommunities.MaxToMin(graph, true);
//        System.out.println("MaxToMin(PD/SumPD) found: " + com);

        Graph originGraph = new DefaultGraph("Propinquity Dynamics");
        originGraph.display();
        originGraph.read(datasetFile);
//        GraphUtils.ParseOverlapCommunities(originGraph, "groundTruth");
        CopyCommunities(graph, originGraph);
        
        Shark(originGraph);

        UIToolbox.ColorCommunities(originGraph);
        double nmi = GetNMI(originGraph);
        System.out.println("NMI: " + nmi);

        File theFile = new File(datasetFile);
//        FileUtils.DumpCommunitiesAndGroundTruth(originGraph, "../exports/" + theFile.getName().split("\\.")[0] + ".communities.txt",  "../exports/" + theFile.getName().split("\\.")[0] + ".groundTruth.txt", "community", "groundTruth");
    }

    private void RangeAB(String datasetFile, boolean printTooNMI) throws IOException, GraphParseException, ParseException {
        RangeABStatistics fu = new RangeABStatistics(printTooNMI);
        String filename = "../exports/" + new File(datasetFile).getName().split("\\.")[0];
        if (!printTooNMI) filename += ".csv";
        fu.init(filename);

        // Init an origin graph, so we can calculate metrics
        Graph originGraph = new DefaultGraph("Propinquity Dynamics");
        originGraph.read(datasetFile);
        InitWeights(originGraph);

        System.out.println("Dataset: " + datasetFile);

        // Find max degree
        Graph tmp_graph = new DefaultGraph("Propinquity Dynamics");
        tmp_graph.read(datasetFile);
        double[] degreeStats = DegreeStatistics(tmp_graph);
        System.out.println("#nodes: " + tmp_graph.getNodeCount());
        System.out.println("#edges: " + tmp_graph.getEdgeCount());
        System.out.println("Max Degree: " + degreeStats[0]);
        System.out.println("Avg Degree: " + degreeStats[1]);

        int maxB = MaxPropinquityToGraph(datasetFile);
        System.out.println("Max propinquity: " + maxB);
        for (int b = 0; b <= maxB; b++) {
            for (int a = 0; a <= b; a++) {
                Graph graph = new DefaultGraph("Propinquity Dynamics");
                graph.read(datasetFile);

                PropinquityDynamics pd = new PropinquityDynamics();
                pd.set(a, b);

                pd.init(graph);

                int i = 0;
                // We need to be sure that we dont have an infinite loop
                while (i < 100 && !pd.didAbsoluteConvergence()) {
                    pd.compute();
                    i++;
                }
                pd.applyFinalTopology();

                int uncommunitized = FindLonelyVertices(graph);
                System.out.println("For a: " + a + " and b: " + b);
                System.out.println("Un-communitized Vertices: " + uncommunitized + " Number of Iterations: " + i);
                fu.append(a + "," + b + "," + uncommunitized + "," + i + ",");

                int[] output = new int[2];
                int sharkOverlaps = 0;
                output[0] = ExtractCommunities.BFS(graph);
                GraphUtils.CopyCommunities(graph, originGraph);
                // Must be at least one community
                if (output[0] > 0) Shark(originGraph);
//                System.out.println("Communities: " + output[0] + " Overlaping nodes: " + output[1]);
                ResetCommunities(graph);
                fu.append(originGraph, output, sharkOverlaps);
//                System.out.println("BFS found: " + com + " with NMI: " + nmi + " and Modularity: " + modularity);

                SetPDWeights(graph);
                output = ExtractCommunities.MaxToMin(graph, true);
                GraphUtils.CopyCommunities(graph, originGraph);
                if (output[0] > 0) Shark(originGraph);
//                System.out.println("Communities: " + output[0] + " Overlaping nodes: " + output[1]);
                ResetCommunities(graph);
                fu.append(originGraph, output, sharkOverlaps);
//                System.out.println("MaxToMin (normal weihts) found: " + com + " with NMI: " + nmi + " and Modularity: " + modularity);

                FractionWithNumberOfEdges(graph);
                output = ExtractCommunities.MaxToMin(graph, true);
                GraphUtils.CopyCommunities(graph, originGraph);
                if (output[0] > 0) Shark(originGraph);
//                System.out.println("Communities: " + output[0] + " Overlaping nodes: " + output[1]);
                ResetCommunities(graph);
                fu.append(originGraph, output, sharkOverlaps);
//                System.out.println("MaxToMin (P/degree) found: " + com + " with NMI: " + nmi + " and Modularity: " + modularity);

                FractionWithTotalPropinquity(graph);
                output = ExtractCommunities.MaxToMin(graph, true);
                GraphUtils.CopyCommunities(graph, originGraph);
                if (output[0] > 0) Shark(originGraph);
//                System.out.println("Communities: " + output[0] + " Overlaping nodes: " + output[1]);
                fu.append(originGraph, output, sharkOverlaps);
//                System.out.println("MaxToMin (P/SumP) found: " + com + " with NMI: " + nmi + " and Modularity: " + modularity);
                
                fu.finishEntry();
            }
        }
        fu.finish();
    }
    
    private void PDLocalAB(String datasetFile) throws IOException, GraphParseException, ParseException {
        int[] output = new int[2];
        Graph graph = new DefaultGraph("Propinquity Dynamics");
        graph.display();
        graph.read(datasetFile);
        LocalPropinquityDynamics lpd = new LocalPropinquityDynamics();
        lpd.statisticsOn();
        lpd.init(graph);
        int i = 0;
        // We need to be sure that we dont have an infinite loop
        while (i < 100 && !lpd.didAbsoluteConvergence()) {
            lpd.compute();
            i++;
        }
        lpd.applyFinalTopology();
        SetPDWeights(graph);
        output = ExtractCommunities.MaxToMin(graph, true);
        Graph originGraph = new DefaultGraph("Propinquity Dynamics");
        originGraph.display();
        originGraph.read(datasetFile);
        GraphUtils.CopyCommunities(graph, originGraph);
        UIToolbox ui = new UIToolbox(originGraph);
        ui.addSprite("NMI", Metrics.GetNMI(originGraph), 60);
        int uncommunitized = UIToolbox.ColorCommunities(originGraph);
    }

    private void PDOriginalAndMaxToMin(String datasetFile) throws IOException, GraphParseException, ParseException {
        Graph graph = new DefaultGraph("Propinquity Dynamics");
        graph.display();
        graph.read(datasetFile);

        PropinquityDynamics pd = new PropinquityDynamics();
        pd.set(2, 20);

        pd.init(graph);

        int i = 0;
        // We need to be sure that we dont have an infinite loop
        while (i < 100 && !pd.didAbsoluteConvergence()) {
            pd.compute();
            i++;
        }
        pd.applyFinalTopology();

        // Use our custom extraction algorithm to retrive internal communities
        SetPDWeights(graph);
        int[] com = ExtractCommunities.MaxToMin(graph, false);

        Graph originGraph = new DefaultGraph("Propinquity Dynamics");
        originGraph.display();
        originGraph.read(datasetFile);
        GraphUtils.CopyCommunities(graph, originGraph);

        int uncommunitized = UIToolbox.ColorCommunities(originGraph);
        System.out.println("Number of communities: " + com[0] + " Un-communitized Vertices: " + uncommunitized + " Number of Iterations: " + i);

    }

    private void ErdosSubgraphPDwithAbsoluteFractionsAndMaxToMin(String datasetFile) throws IOException, GraphParseException {
        Graph graph = new DefaultGraph("Propinquity Dynamics");
        graph.display();
        graph.read(datasetFile);

        PropinquityDynamics pd = new PropinquityDynamics();
        pd.set(2, 20);

        pd.init(graph);

        int i = 0;
        // We need to be sure that we dont have an infinite loop
        while (i < 100 && !pd.didAbsoluteConvergence()) {
            pd.compute();
            i++;
        }
        pd.applyFinalTopology();

        // Set the edge weight to fractions
        FractionWithNumberOfEdges(graph);
        //Utils.FractionWithTotalPropinquity(graph);

        // Use our custom extraction algorithm to retrive internal communities
        int[] com = ExtractCommunities.MaxToMin(graph, false);
        UIToolbox.ColorCommunities(graph);
        System.out.println("Number of communities: " + com[0]);
    }

    private void ErdozSubgraphwithOriginalPDAndTwoDisplays(String datasetFile) throws IOException, ParseException, GraphParseException {
        Graph graph = new DefaultGraph("Propinquity Dynamics");
        graph.display();
        graph.read(datasetFile);

        PropinquityDynamics pd = new PropinquityDynamics();
        pd.set(2, 10);
        pd.init(graph);

        int i = 0;
        // We need to be sure that we dont have an infinite loop
        while (i < 100 && !pd.didAbsoluteConvergence()) {
            pd.compute();
            i++;
        }

        pd.applyFinalTopology();

        // Erdos02-subgraph specific vertices
        Integer[] fixedCommunity = {10, 11};
        ExtractCommunities.BFS(graph, fixedCommunity);
        UIToolbox.ColorCommunities(graph);
        FractionWithTotalPropinquity(graph);

        Graph originGraph = new DefaultGraph("Propinquity Dynamics");
        originGraph.display();
        originGraph.read("../data/erdos02-subset.gml");
        GraphUtils.CopyCommunities(graph, originGraph);

        UIToolbox.ColorCommunities(originGraph);
    }

    private void LouvainExample(String datasetFile) throws IOException, GraphParseException {

        CommunityDetectionLouvain louvain = new CommunityDetectionLouvain();
        louvain.init(datasetFile);
        louvain.execute();
    }

    public static void WriteToFileExample() throws IOException, GraphParseException {
        Graph graph = new DefaultGraph("Propinquity Dynamics");
        graph.read("../data/erdos02-subset.gml");

        graph.removeNode(7);
        
        FileUtils.ExportGraphIntoGML(graph, "../data/export");
    }
    
    private static void MTXtoGML() throws IOException {
        String fileName = "../data/polblogs.mtx";
        Graph graph = new DefaultGraph("Propinquity Dynamics", false, true, 1500, 19000);
        
        List<String> lines = Files.readAllLines(Paths.get(fileName),
                Charset.defaultCharset());
        for (String line : lines) {
            String[] a = line.split(" ");
            
            graph.addEdge(a[0] + " " + a[1], a[0], a[1], true);
        }
        graph.display();
        
        System.out.println(graph.getNodeCount());
        System.out.println(graph.getEdgeCount());
        
//        FileSink fs = new FileSinkGML();
//        fs.writeAll(graph, "../data/polblogs2.gml");
    }
}
