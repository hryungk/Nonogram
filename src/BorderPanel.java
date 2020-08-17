//********************************************************************************
//  BorderPanel.java      Author: Lewis/Loftus
//
//  Puts together the grids and row && column arrays.
//********************************************************************************

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import javax.swing.*;
import java.util.Arrays;

public class BorderPanel extends JPanel
{
    private final int DELAY = 2000;
    private final Timer timer;
    private final GridPanel gp;
    private final Status[][] answer;          // Answer that this solver produces
    private final int m, n;             // size of row and column, respectively
    private final NonogramProblem newProblem;
    private final NonogramSolution_v2_0 newSolution;
    
    private final ArrayInfo[] row_arrays;     // An array of array information objects for row arrays
    private final ArrayInfo[] col_arrays;     // An array of array information objects for column arrays
    private final int[][] PROB_ROW;     // Row arrays of the problem
    private final int[][] PROB_COL;     // Column arrays of the problem 
    private boolean noEmpty = false;    // true if there is no empty cells
    private int loopCount = 0;
    //----------------------------------------------------------------------------
    // Sets up this panel with a button in each area of a border
    // layout to show how it affects their position, shape, and size.
    //----------------------------------------------------------------------------
    public BorderPanel(int probNum) throws IOException
    {   
        // Parameters for the problem
        newProblem = new NonogramProblem(probNum);        
        newSolution = new NonogramSolution_v2_0(newProblem);
        // Load problem definition from newProblem
        PROB_ROW = newProblem.getRowArray();
        PROB_COL = newProblem.getColumnArray();        
        m = newProblem.getRowArray().length;    // Row length of the problem
        n = newProblem.getColumnArray().length;    // Column length of the problem
        row_arrays = new ArrayInfo[m];        
        for (int i = 0; i < m; i++)
            row_arrays[i] = new ArrayInfo(PROB_ROW[i], m);
        col_arrays = new ArrayInfo[n];
        for (int j = 0; j < n; j++)
            col_arrays[j] = new ArrayInfo(PROB_COL[j], n);
        
        answer = new Status[m][n];
        for (int i = 0; i < m; i++) // Initialize answer as Empty
            for (int j = 0; j < n; j++)
                answer[i][j] = Status.Empty;   
        
        // Set up the border panel
        setLayout(new BorderLayout());        
        setBackground(Color.white);   
        timer = new Timer(DELAY, new ReboundListener());                
        
        gp = new GridPanel(m, n);
        //gp.setBorder(BorderFactory.createLineBorder(Color.black, 1));
        //GridLayout layout = (GridLayout)gp.getLayout();
        //layout.setVgap(0);
        
        BoxPanel row = new BoxPanel(RowCol.Row, PROB_ROW);        
        BoxPanel column = new BoxPanel(RowCol.Column, PROB_COL);        
        
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
        @Override
        public void actionPerformed(ActionEvent event)
        {
            if (noEmpty)
                timer.stop();
            else // if (!noEmpty)    // Keep solving arrays until there is no empty cell
            {                 
                System.out.println("==============================================" +
                               "=========\n\t\t\tLoop "+ (loopCount+1) +
                               "\n============================================" +
                               "===========");
                System.out.println("----------------------- Row - " + (loopCount+1) +
                               " -----------------------"); 
                for (int i = 0; i < m; i++) // Scan rows
                {                
                    if (!row_arrays[i].isSolved())
                    {           
                        System.out.println("Array " + Arrays.toString(PROB_ROW[i]) +
                            " -> " + Arrays.toString(row_arrays[i].getArray()) + ":");
                        Status[] temp = newSolution.findArraySolution(RowCol.Row,i, m, answer,row_arrays[i]);
                        System.arraycopy(temp, 0, answer[i], 0, n); 
                        //for (int j = 0; j < n; j++)
                        //    answer[i][j] = temp[j]; 
                    } // end if
                    else
                        System.out.println("Array " + Arrays.toString(PROB_ROW[i]) 
                                           + " is solved.");
                } // end for
               
                System.out.println("--------------------- Column - " + (loopCount+1) +
                               " ---------------------");
                noEmpty = true;     // initialize noEmpty every loop       
                for (int j = 0; j < n; j++) // Scan columns
                {
                    if (!col_arrays[j].isSolved())
                    {                        
                        System.out.println("Array " + Arrays.toString(PROB_COL[j]) +
                            "->" + Arrays.toString(col_arrays[j].getArray()) + ":");
                        Status[] temp = newSolution.findArraySolution(RowCol.Column, j, n, answer, col_arrays[j]);
                        for (int i = 0; i < n; i++)
                        {
                            answer[i][j] = temp[i]; 
                            noEmpty = noEmpty && (answer[i][j] != Status.Empty);
                        } // end for
                    } // end if
                    else                                    
                        System.out.println("Array " + Arrays.toString(PROB_COL[j]) 
                                           + " is solved.");
                } // end for
                
                for (int i = 0; i < m; i++) // Scan rows
                    for (int j = 0; j < n; j++) // Scan columns
                    {
                        if (answer[i][j] == Status.True)
                            gp.makeTrue(i, j);
                        else if (answer[i][j] == Status.False)
                            gp.makeFalse(i, j);
                    }
                loopCount++;            
            } // end if  
        } // end actionPerformed
    } // end ReboundListener            
} // end BorderPanel
