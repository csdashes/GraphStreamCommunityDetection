package th.main;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.GraphParseException;
import org.graphstream.util.parser.ParseException;
import th.algorithms.louvain.CommunityDetectionLouvain;
import th.algorithms.propinquitydynamics.PropinquityDynamics;
import static th.algorithms.propinquitydynamics.utils.Utils.FractionWithNumberOfEdges;
import static th.algorithms.propinquitydynamics.utils.Utils.FractionWithTotalPropinquity;
import static th.algorithms.propinquitydynamics.utils.Utils.MaxPropinquityToNonNeighbor;
import static th.algorithms.propinquitydynamics.utils.Utils.SetPDWeights;
import th.utils.ExtractCommunities;
import static th.utils.ExtractCommunities.Shark;
import th.utils.Menu;
import static th.utils.Metrics.GetModularity;
import static th.utils.Metrics.GetNMI;
import th.utils.Statistics;
import static th.utils.Statistics.DegreeStatistics;
import static th.utils.Statistics.MaxPropinquity;
import th.utils.UIToolbox;
import th.utils.Utils;
import static th.utils.Utils.FindLonelyVertices;
import static th.utils.Utils.InitWeights;
import static th.utils.Utils.ResetCommunities;

/**
 *
 * @author Anastasis Andronidis <anastasis90@yahoo.gr>
 * @author Ilias Trichopoulos <itrichop@csd.auth.gr>
 */
public class AppManager {

    public void printUserMenu() throws IOException, GraphParseException, ParseException {
        int methodSelection, datasetSelection;
        String datasetFile = null;
        boolean flag = true;

        while (flag) {
            methodSelection = Menu.printMenu();
            if (methodSelection != 0) {
                datasetSelection = Menu.printDatasetMenu();
                switch (datasetSelection) {
                    case 1:
                        datasetFile = "../data/polbooks.gml";
                        break;
                    case 2:
                        datasetFile = "../data/dolphins.gml";
                        break;
                    case 3:
                        datasetFile = "../data/karate.gml";
                        break;
                    case 4:
                        datasetFile = "../data/erdos02.gml";
                        break;
                    case 5:
                        datasetFile = "../data/erdos02-subset.gml";
                        break;
                    case 0:
                        return;
                }
            }
            switch (methodSelection) {
                case 1:
                    //Execute 1st function
                    ErdosSubgraphPDwithAbsoluteFractionsAndMaxToMin(datasetFile);
                    break;
                case 2:
                    //Execute 2st function
                    ErdozSubgraphwithOriginalPDAndTwoDisplays(datasetFile);
                    break;
                case 3:
                    //Execute 3rd function
                    LouvainExample(datasetFile);
                    break;
                case 4:
                    //Execute 4rd function
                    PDOriginalStatics(datasetFile);
                    break;
                case 0:
                    //Exit
                    return;
            }
        }

//        String[] datasets = {"../data/polbooks.gml", "../data/dolphins.gml", "../data/karate.gml"};      
//        for (String dataset : datasets) {
//            RangeAB(dataset);
//        }
//        CompareExtractions(datasets[2]);
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

        int com;
//        com = ExtractCommunities.BFS(graph);
//        ResetCommunities(graph);
//        System.out.println("BFS found: " + com);

        SetPDWeights(graph);
        com = ExtractCommunities.MaxToMin(graph);
        System.out.println("MaxToMin(normal weihts) found: " + com);

//        FractionWithNumberOfEdges(graph);
//        com = ExtractCommunities.MaxToMin(graph);
//        System.out.println("MaxToMin(PD/degree) found: " + com);
//        FractionWithTotalPropinquity(graph);
//        com = ExtractCommunities.MaxToMin(graph);
//        System.out.println("MaxToMin(PD/SumPD) found: " + com);

        Graph originGraph = new DefaultGraph("Propinquity Dynamics");
        originGraph.display();
        originGraph.read(datasetFile);
        Utils.CopyCommunities(graph, originGraph);
        
        Shark(originGraph);

        UIToolbox.ColorCommunities(originGraph);

    }

    private void RangeAB(String datasetFile) throws IOException, GraphParseException, ParseException {
        File theFile = new File(datasetFile);
        try (PrintWriter writer = new PrintWriter("../exports/" + theFile.getName().split("\\.")[0] + ".csv", "UTF-8")) {
            writer.println("a,b,UncommunitizedVertices,NumberofIterations,"
                    + "BFScom,BFSNMI,BFSModularity,"
                    + "MaxToMinNormalWeihtsCom,MaxToMinNormalWeihtsNMI,MaxToMinNormalWeihtsModularity,"
                    + "MaxToMinP/degreeCom,MaxToMinP/degreeNMI,MaxToMinP/degreeModularity,"
                    + "MaxToMinP/SumPCom,MaxToMinP/SumPNMI,MaxToMinP/SumPModularity");
            
            // Init an origin graph, so we can calculate metrics
            Graph originGraph = new DefaultGraph("Propinquity Dynamics");
            originGraph.read(datasetFile);
            InitWeights(originGraph);
            
            System.out.println("Dataset: " + datasetFile);
            
            // Find max degree
            Graph tmp_graph = new DefaultGraph("Propinquity Dynamics");
            tmp_graph.read(datasetFile);
            double[] degreeStats = DegreeStatistics(tmp_graph);
            System.out.println("Max Degree: " + degreeStats[0]);
            System.out.println("Avg Degree: " + degreeStats[1]);
            
            int maxB = MaxPropinquity(datasetFile);
            System.out.println("Max propinquity: " + degreeStats[1]);
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
                    String toCSV = a + "," + b + "," + uncommunitized + "," + i + ",";
                    System.out.println("For a: " + a + " and b: " + b);
                    System.out.println("Un-communitized Vertices: " + uncommunitized + " Number of Iterations: " + i);
                    
                    int com = ExtractCommunities.BFS(graph);
                    Utils.CopyCommunities(graph, originGraph);
                    // Must be at least one community
                    if (com > 0) {
                        Shark(originGraph);
                    }
                    double nmi = GetNMI(originGraph);
                    double modularity = GetModularity(originGraph);
                    ResetCommunities(graph);
                    toCSV += com + "," + nmi + "," + modularity + ",";
//                System.out.println("BFS found: " + com + " with NMI: " + nmi + " and Modularity: " + modularity);
                    
                    SetPDWeights(graph);
                    com = ExtractCommunities.MaxToMin(graph);
                    Utils.CopyCommunities(graph, originGraph);
                    Shark(originGraph);
                    nmi = GetNMI(originGraph);
                    modularity = GetModularity(originGraph);
                    ResetCommunities(graph);
                    toCSV += com + "," + nmi + "," + modularity + ",";
//                System.out.println("MaxToMin (normal weihts) found: " + com + " with NMI: " + nmi + " and Modularity: " + modularity);
                    
                    FractionWithNumberOfEdges(graph);
                    com = ExtractCommunities.MaxToMin(graph);
                    Utils.CopyCommunities(graph, originGraph);
                    Shark(originGraph);
                    nmi = GetNMI(originGraph);
                    modularity = GetModularity(originGraph);
                    ResetCommunities(graph);
                    toCSV += com + "," + nmi + "," + modularity + ",";
//                System.out.println("MaxToMin (P/degree) found: " + com + " with NMI: " + nmi + " and Modularity: " + modularity);
                    
                    FractionWithTotalPropinquity(graph);
                    com = ExtractCommunities.MaxToMin(graph);
                    Utils.CopyCommunities(graph, originGraph);
                    Shark(originGraph);
                    nmi = GetNMI(originGraph);
                    modularity = GetModularity(originGraph);
                    toCSV += com + "," + nmi + "," + modularity;
//                System.out.println("MaxToMin (P/SumP) found: " + com + " with NMI: " + nmi + " and Modularity: " + modularity);
                    
                    writer.println(toCSV);
                }
            }
        }
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
        int com = ExtractCommunities.MaxToMin(graph);

        Graph originGraph = new DefaultGraph("Propinquity Dynamics");
        originGraph.display();
        originGraph.read(datasetFile);
        Utils.CopyCommunities(graph, originGraph);

        int uncommunitized = UIToolbox.ColorCommunities(originGraph);
        System.out.println("Number of communities: " + com + " Un-communitized Vertices: " + uncommunitized + " Number of Iterations: " + i);

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
        int com = ExtractCommunities.MaxToMin(graph);
        UIToolbox.ColorCommunities(graph);
        System.out.println("Number of communities: " + com);
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
        Utils.CopyCommunities(graph, originGraph);

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

        Utils.ExportGraphIntoGML(graph, "../data/export");
    }
}
