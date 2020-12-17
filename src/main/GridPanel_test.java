package main;

//********************************************************************************
//  GridPanel.java      Author: Hyunryung Kim
//
//  Represents a grid size of M x N for a Nonogram puzzle.
//********************************************************************************

import java.awt.*;
import javax.swing.*;

public class GridPanel_test extends JPanel
{
    final int CELL_SIZE = 40;
    private final int M = 10;
    private final int N = 10;
    JPanel[][] gridColor;
    JLabel[][] xLabels;
    //----------------------------------------------------------------------------
    //  Sets up this grid panel with panels with a label for each cell.
    //----------------------------------------------------------------------------
    public GridPanel_test() 
    {   
        GridLayout layout = new GridLayout(M, N);
        setLayout(layout);
        setBackground(Color.white);
        setPreferredSize(new Dimension(CELL_SIZE * M, CELL_SIZE * N));        
        gridColor = new JPanel[M][N];
        xLabels = new JLabel[M][N];
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
            {
                JPanel p = createNewCell();
                JLabel l = createNewLabel();
                p.add(l);
                add(p);
                gridColor[i][j] = p;
                xLabels[i][j] = l;                
            } // end for
    } // end constructor    
    
    /** Make cell (i, j) true.
     * @param i An integer containing an index of row component
     * @param j An integer containing an index of column component
     */
    public void makeTrue(int i, int j)
    {
        JPanel p = gridColor[i][j];
        p.setBackground(Color.black);
    } // end makeTrue
    
    /** Make cell (i, j) false.
     * @param i An integer containing an index of row component
     * @param j An integer containing an index of column component
     */
    public void makeFalse(int i, int j)
    {
        JLabel l = xLabels[i][j];        
        l.setText("X");
    } // end makeFalse
    
    // Returns a new panel for a grid cell.
    private JPanel createNewCell()
    {
        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createLineBorder(Color.black, 1));
        p.setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));        
        p.setBackground(Color.white);           
        return p;
    } // end createNewCell
    
    // Returns a new label with an empty string.
    private JLabel createNewLabel()
    {
        JLabel l = new JLabel("");
        l.setFont(new Font("Helvetica", Font.PLAIN, 35));        
        return l;
    } // end createnewLabel
}
