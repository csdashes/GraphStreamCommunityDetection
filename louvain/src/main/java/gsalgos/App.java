package gsalgos;

import java.io.IOException;
import org.graphstream.algorithm.community.CommunityDetectionLouvain;
import org.graphstream.stream.GraphParseException;

public class App 
{
    public static void main( String[] args ) throws IOException, GraphParseException
    {
        CommunityDetectionLouvain louvain = new CommunityDetectionLouvain();
//        louvain.init("data/polbooks.gml");
//        louvain.init("data/smalltest.gml");
//        louvain.init("data/dolphins.gml");
//        louvain.init("../data/karate.gml");
//        louvain.init("../data/smalltest.dgs");
        louvain.init("../data/export.gml");
        louvain.execute();
    }
    
}
