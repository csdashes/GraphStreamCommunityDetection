/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pdgs.utils;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.ui.spriteManager.SpriteManager;

/**
 *
 * @author Ilias Trichopoulos <itrichop@csd.auth.gr>
 */
public class UIToolbox {
    
    private SpriteManager sm;
    
    public UIToolbox(Graph graph) {
        this.sm = new SpriteManager(graph);
    }
    
    /**
     * Method to add styling attributes (ui.label, ui.style) to a node
     * @param n
     */
    private void styleNode(Node n) {
        n.setAttribute("ui.label", n.getIndex());
        n.setAttribute("ui.style", "size:20px;");
    }
}
