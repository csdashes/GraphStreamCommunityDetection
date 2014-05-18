/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pdgs.utils;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.ui.graphicGraph.stylesheet.StyleConstants;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;

/**
 * This class provides styling methods for several graph elements and graph 
 * Viewers.
 * To use it, create a UIToolbox object, providing a Graph object (where the 
 * styling will be applied) as a parameter.
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
    public static void styleNode(Node n) {
        n.setAttribute("ui.label", n.getIndex());
        n.setAttribute("ui.style", "size:20px;");
    }
    
    /**
     * Add a sprite in the graph Viewer window. Default X coordinate: 20.
     * @param spriteId the id for the specific Sprite.
     * @param spriteName the text that will be displayed as name of the sprite.
     * @param spriteValue the value (int) next to the name of the sprite.
     * @param spritePosition the Y coordinate of the sprite. Default X position is 20. 
     */
    public void addSprite(String spriteId, String spriteName, Integer spriteValue, int spritePosition) {
        Sprite spr = this.sm.addSprite(spriteId);
        spr.setPosition(StyleConstants.Units.PX, 20, spritePosition, 0);
        spr.setAttribute("ui.label",
                String.format(spriteName+": %d", spriteValue));
        spr.setAttribute("ui.style", "size: 0px; text-color: rgb(150,100,100); text-size: 20;");
    }
    
    /**
     * Remove a Sprite from the graph Viewer window.
     * @param spriteId the id of the sprite.
     */
    public void removeSprite(String spriteId) {
        this.sm.removeSprite(spriteId);
    }
}
