//********************************************************************************
//  LayoutDemo.java      Author: Lewis/Loftus
//
//  Demonstrates the use of flow, border, grid, and box layouts.
//********************************************************************************

import java.io.IOException;
import javax.swing.*;

public class NonogramLayout 
{
    //----------------------------------------------------------------------------
    // Sets up a frame containing a tabbed pane. The panel on each
    // tab demonstrates a different layout manager.
    //----------------------------------------------------------------------------
    public static void main(String[] args) throws IOException 
    {   
        JFrame frame = new JFrame("Nonogram Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        int probNum = 11;
        int rowNum = 10;
        int colNum = 10;
        System.out.println("<Problem " + rowNum + " x " + colNum + " - " + probNum + ">");
        BorderPanel panel = new BorderPanel(rowNum, colNum, probNum);

        frame.getContentPane().add(panel);
        frame.pack();
        frame.setVisible(true);
    }
}
