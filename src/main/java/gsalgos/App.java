package gsalgos;

import java.io.IOException;
import org.graphstream.algorithm.community.CommunityDetectionLouvain;
import org.graphstream.stream.GraphParseException;

public class App 
{
    public static void main( String[] args ) throws IOException, GraphParseException
    {
        (new CommunityDetectionLouvain()).findCommunities("data/smalltest.gml");
    }
    
}
