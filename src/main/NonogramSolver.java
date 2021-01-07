package main;

//********************************************************************************
//  LayoutDemo.java      Author: Lewis/Loftus
//
//  Demonstrates the use of flow, border, grid, and box layouts.
//********************************************************************************

import java.io.IOException;
import javax.swing.*;

public class NonogramSolver 
{
    //----------------------------------------------------------------------------
    // Sets up a frame containing a border layout panel. The panel on each
    // border represents row, column, and grids.
    //----------------------------------------------------------------------------
    public static void main(String[] args) throws IOException 
    {                   
        JFrame frame = new JFrame("Nonogram Demo");        
        
//        JPanel tPanel = new TitlePanel(frame);
        JPanel tPanel = new DropDownPanel(frame);
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(tPanel);
        frame.pack();
        frame.setVisible(true);
    }
}
