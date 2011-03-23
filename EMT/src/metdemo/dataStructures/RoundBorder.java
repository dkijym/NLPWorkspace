/*
 * Created on Mar 6, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package metdemo.dataStructures;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.border.AbstractBorder;

/**
 * @author Shlomo
 * 
 * Ability to make round borders on the components
 */


public class RoundBorder extends AbstractBorder {
    public void paintBorder(Component c, Graphics g, int x, int y, int width,
            int height) {
        // g.setColor ( c.getForeground() ) ;

        //  g.drawRoundRect ( x , y , width , height , 25, 25) ;
        Color oldColor = g.getColor();

        g.setColor(Color.black);
        g.drawRoundRect(x, y, width - 1, height - 1, 20, 20);
        g.setColor(oldColor);
    }

    public Insets getBorderInsets(Component c) {
        return new Insets(4, 4, 4, 4);
    }

    public Insets getBorderInsets(Component c, Insets insets) {
        insets.left = insets.top = insets.right = insets.bottom = 4;
        return insets;
    }

}