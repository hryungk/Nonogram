//********************************************************************************
//  BorderPanel_test.java      Author: Lewis/Loftus
//
//  Puts together the grids and row && column arrays.
//********************************************************************************

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class BorderPanel_test extends JPanel
{
    private final int DELAY = 1000;
    private Timer timer;
    private GridPanel_test gp;
    private int x, y, moveX, moveY;
    //----------------------------------------------------------------------------
    // Sets up this panel with a button in each area of a border
    // layout to show how it affects their position, shape, and size.
    //----------------------------------------------------------------------------
    public BorderPanel_test() 
    {   
        setLayout(new BorderLayout());        
        setBackground(Color.white);   
        timer = new Timer(DELAY, new ReboundListener());        
        moveX = moveY = 1;
        x = -moveX;
        y = -moveY;
        
        gp = new GridPanel_test();
        BoxPanel_test row = new BoxPanel_test(RowCol.Row);        
        BoxPanel_test column = new BoxPanel_test(RowCol.Column);
        
        
        add(gp, BorderLayout.CENTER);
        add(column, BorderLayout.NORTH);
        add(row, BorderLayout.WEST);
        timer.start();
    }
    
    //-----------------------------------------------------------------
    //  Draws the image in the current location.
    //-----------------------------------------------------------------
//    public void paintComponent(Graphics page)
//    {
//       super.paintComponent(page);       
//    }   
    
    
    //*****************************************************************
    //  Represents the action listener for the timer.
    //*****************************************************************
    private class ReboundListener implements ActionListener
    {
        //--------------------------------------------------------------
        //  Updates the position of the image and possibly the direction
        //  of movement whenever the timer fires an action event.
        //--------------------------------------------------------------
        public void actionPerformed(ActionEvent event)
        {
            x += moveX;
            y += moveY;
               
            if (x < 10 && y < 10)
            {
                //gp.makeTrue(x, y);
                gp.makeFalse(x, y);                
            }
            else
                timer.stop();
        }
    }            
}
