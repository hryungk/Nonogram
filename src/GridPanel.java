//********************************************************************************
//  GridPanel.java      Author: Hyunryung Kim
//
//  Represents a grid size of m x n for a Nonogram puzzle.
//********************************************************************************

import java.awt.*;
import javax.swing.*;

public class GridPanel extends JPanel
{
    final int CELL_SIZE = 40;
    JPanel[][] gridColor;
    JLabel[][] xLabels;
    //----------------------------------------------------------------------------
    //  Sets up this grid panel with panels with a label for each cell.
    //----------------------------------------------------------------------------
    public GridPanel(int m, int n) 
    {   
        GridLayout layout = new GridLayout(m, n, 0, 0);
        setLayout(layout);
        setBackground(Color.white);
        //setBorder(BorderFactory.createLineBorder(Color.black, 1));
        setPreferredSize(new Dimension(CELL_SIZE * m, CELL_SIZE * n));        
        gridColor = new JPanel[m][n];
        xLabels = new JLabel[m][n];
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
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
        p.setBackground(navyBlue());
        p.setBorder(BorderFactory.createLineBorder(Color.black, 1));
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
        p.setBorder(BorderFactory.createLineBorder(gray(), 1));
        p.setPreferredSize(new Dimension(CELL_SIZE, CELL_SIZE));        
        p.setBackground(Color.white);           
        return p;
    } // end createNewCell
    
    // Returns a new label with an empty string.
    private JLabel createNewLabel()
    {
        JLabel l = new JLabel("");
        l.setFont(new Font("Helvetica", Font.PLAIN, 35));           
        l.setForeground(navyBlue());
        return l;
    } // end createnewLabel
    
    // Returns a navy blue color
    private Color navyBlue()
    {        
        float[] HSB = Color.RGBtoHSB(51, 73, 97, null);
        return Color.getHSBColor(HSB[0], HSB[1], HSB[2]);
    }
    
    // Returns a light blue color
    private Color gray()
    {        
        float[] HSB = Color.RGBtoHSB(214, 218, 228, null);
        return Color.getHSBColor(HSB[0], HSB[1], HSB[2]);
    }
}
