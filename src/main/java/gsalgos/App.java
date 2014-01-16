package gsalgos;

import java.io.IOException;
import org.graphstream.algorithm.community.CommunityDetectionLouvain;
import org.graphstream.stream.GraphParseException;

public class App 
{
    public static void main( String[] args ) throws IOException, GraphParseException
    {
        CommunityDetectionLouvain louvain = new CommunityDetectionLouvain();
        louvain.init("data/dolphins.gml");
//        while(!louvain.isModularityMaximized()) {
//            louvain.execute();
//        }
        for(int i=0;i<2;i++) {
            louvain.execute();
        }
    }
    
}
