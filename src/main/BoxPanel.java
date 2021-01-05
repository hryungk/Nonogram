package main;

//********************************************************************************
//  BoxPanel.java      Author: Hyunryung Kim
//
//  Represents the row/column arrays.
//********************************************************************************

import java.awt.*;
import javax.swing.border.*;
import java.util.Scanner;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class BoxPanel extends JPanel
{
    final int CELL_SIZE = 40;   
    String[] strings;   // An array of strings containing row/col arrays.
    //----------------------------------------------------------------------------
    // Sets up this panel with row/column arrays.    
    //----------------------------------------------------------------------------
    public BoxPanel(RowCol rc, int[][] givenArrays, int probNum) 
    {   
        strings = int2str(givenArrays);
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
            p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
            int aLen = givenArrays.length;
            JLabel pLabel1 = new JLabel(Integer.toString(aLen) + " x " + aLen);
            JLabel pLabel2 = new JLabel("Problem #" + probNum);
            pLabel1.setFont(new Font("Calibri", Font.BOLD, 20));
            pLabel2.setFont(new Font("Calibri", Font.BOLD, 20));
            pLabel1.setAlignmentX(Component.CENTER_ALIGNMENT);
            pLabel2.setAlignmentX(Component.CENTER_ALIGNMENT);
            p.add(pLabel1);
            p.add(pLabel2);
            p.setBackground(Color.white);
            p.setBorder(BorderFactory.createLineBorder(Color.white, 1));
            p.setPreferredSize(new Dimension(3*CELL_SIZE,3*CELL_SIZE));            
            add(p); 
        }
        
        for (JPanel panel : panels) 
            add(panel);                
        
    } // end constructor
    
    // Returns an array of panels, each of which contains a row array.
    private JPanel[] createRow()
    {        
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));         
        JPanel[] panels = new JPanel[strings.length];
        
        for (int i = 0; i < strings.length; i++)
        {
            JPanel p = new JPanel();
            JLabel label = new JLabel(strings[i],SwingConstants.RIGHT); 
            //label.setLayout(new FlowLayout(FlowLayout.RIGHT));
            label.setFont(new Font("Calibri", Font.PLAIN, 20));
            p.add(label);            
            p.setPreferredSize(new Dimension(3*CELL_SIZE,CELL_SIZE));
            p.setBackground(Colors.lightBlue);            
            setColors(p);               
            panels[i] = p; 
        }         
        return panels;
    } // end createRow
    
    // Returns an array of panels, each of which contains a column array.
    private JPanel[] createColumn()
    {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));        
        JPanel[] panels = new JPanel[strings.length];
        for (int i = 0; i < strings.length; i++)
        {
            JPanel p = new JPanel();
            String str = strings[i];
            Scanner scan = new Scanner(str);
            while(scan.hasNextInt())    // Add each number as a separate label
            {
                int num = scan.nextInt();    
                String numStr = String.valueOf(num);
                JLabel label = new JLabel(numStr + " ", SwingConstants.TRAILING);
                label.setFont(new Font("Calibri", Font.PLAIN, 20));
                p.add(label);
            }
            p.setPreferredSize(new Dimension(CELL_SIZE,3*CELL_SIZE));
            setColors(p);
            panels[i] = p; 
        }         
        return panels;
    } // end createColumn
    
    // Convert individual arrays into a string
    // e.g., [[1,2], [2], ...] --> ["1 2", "2", ...]
    private String[] int2str(int[][] arrays)
    {
        int a = arrays.length;
        String[] result = new String[a];
        for (int i = 0; i < a; i++)
        {
            int[] curArray = arrays[i];
            int arrayLen = curArray.length;
            String temp = "";
            for (int j = 0; j < arrayLen; j++)
            {
                temp += curArray[j];
                if (j < arrayLen-1)
                    temp += " ";
            } // end for
            result[i] = temp;
        } // end for
        System.out.println(Arrays.toString(result));
        return result;
    } //end int2str
    
    // Sets colors for the panel
    private void setColors(JPanel p)
    {
        p.setBackground(Colors.lightBlue);     
        Border b1 = BorderFactory.createLineBorder(Color.white,1);
        Border b2 = BorderFactory.createLineBorder(Colors.gray, 1);            
        p.setBorder(BorderFactory.createCompoundBorder(b1, b2)); 
    }    
} // end BoxPanel
