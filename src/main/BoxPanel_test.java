package main;

//********************************************************************************
//  BoxPanel.java      Author: Lewis/Loftus
//
//  Represents the row/column arrays.
//********************************************************************************

import java.awt.*;
import javax.swing.*;
import java.util.Scanner;

public class BoxPanel_test extends JPanel
{
    final int CELL_SIZE = 40;
    //----------------------------------------------------------------------------
    // Sets up this panel with some buttons to show how a vertical
    // box layout (and invisible components) affects their position.
    //----------------------------------------------------------------------------
    public BoxPanel_test(RowCol rc) 
    {   
        setBackground(Color.white);        
        // Get a list of arrays of row or column
        JPanel[] panels;
        if (rc == RowCol.Row)       
            panels = createRow();                    
        else
        {
            panels = createColumn();
            // Add an empty panel in front
            JPanel p = new JPanel();            
            p.setBackground(Color.white);
            p.setBorder(BorderFactory.createLineBorder(Color.white, 1));
            p.setPreferredSize(new Dimension(3*CELL_SIZE,3*CELL_SIZE));            
            add(p); 
        }
        
        for (int i = 0; i < panels.length; i++)
            add(panels[i]);                
    } // end constructor
    
    private JPanel[] createRow()
    {
        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));        
        String[] strings = {"2", "4", "3 3", "5 1 1", "3 1",  
                           "10", "10", "1", "3", "10"};
        JPanel[] panels = new JPanel[strings.length];
        
        for (int i = 0; i < strings.length; i++)
        {
            JPanel p = new JPanel();
            JLabel label = new JLabel(strings[i],SwingConstants.RIGHT);            
            label.setFont(new Font("Helvetica", Font.PLAIN, 20));
            p.add(label);            
            p.setBackground(Color.cyan);
            p.setPreferredSize(new Dimension(3*CELL_SIZE,CELL_SIZE));
            p.setBorder(BorderFactory.createLineBorder(Color.white, 1));               
            panels[i] = p; 
        }         
        return panels;
    }
    
    private JPanel[] createColumn()
    {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        String[] strings = {"4 1", "4 1", "4 1", "2 2 1", "2 2 1",  
                           "2 2 1", "2 2 2", "10", "2 2 2", "2 2 1"};
        
        JPanel[] panels = new JPanel[strings.length];
        for (int i = 0; i < strings.length; i++)
        {
            JPanel p = new JPanel();
            //p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            String str = strings[i];
            Scanner scan = new Scanner(str);
            while(scan.hasNextInt())
            {
                int num = scan.nextInt();    
                String numStr = String.valueOf(num);
                JLabel label = new JLabel(numStr + " ", SwingConstants.TRAILING);
                label.setFont(new Font("Helvetica", Font.PLAIN, 20));
                p.add(label);
            }
            p.setBackground(Color.cyan);
            p.setPreferredSize(new Dimension(CELL_SIZE,3*CELL_SIZE));
            p.setBorder(BorderFactory.createLineBorder(Color.white, 1));            
            panels[i] = p; 
        }         
        return panels;
    }
}
