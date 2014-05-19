package th.main;

import java.io.IOException;
import org.graphstream.stream.GraphParseException;
import org.graphstream.util.parser.ParseException;

public class Main {

    public static void main(String[] args) throws IOException, GraphParseException, ParseException {
        // We must create a menu. I am too lazy right now to do it -.-
        // I've got your back bro!
        AppManager appmana = new AppManager();
        appmana.printUserMenu();
        System.exit(0);
    }
}
