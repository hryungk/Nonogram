/*
 * A class that solves a Nonogram problem.
 */

/**
 *
 * @author Hyunryung Kim    hryungk@gmail.com
 */
import java.util.Arrays;
import java.util.ArrayList;
public class NonogramSolution
{   
    private final int[][] PROB_ROW;     // Row arrays of the problem
    private final int[][] PROB_COL;     // Column arrays of the problem   
    private final Status[][] SOLUTION;  // Solution to the problem
    private Status[][] myAnswer;          // Answer that this solver produces
    private final int m, n;             // size of row and column, respectively
    private final ArrayInfo[] row_arrays;     // An array of array information objects for row arrays
    private final ArrayInfo[] col_arrays;     // An array of array information objects for column arrays
    
    /** Initializes parameters by loading a new problem.
     * @param newProblem A Nonogram problem to solve
     */
    public NonogramSolution(NonogramProblem newProblem)
    {        
        // Load problem definition from newProblem
        PROB_ROW = newProblem.getRowArray();
        PROB_COL = newProblem.getColumnArray();
        SOLUTION = newProblem.getSolution();
        
        m = PROB_ROW.length;    // Row length of the problem
        n = PROB_COL.length;    // Column length of the problem
        
        row_arrays = new ArrayInfo[m];
        for (int i = 0; i < m; i++)
            row_arrays[i] = new ArrayInfo(PROB_ROW[i], m);
        col_arrays = new ArrayInfo[n];
        for (int j = 0; j < n; j++)
            col_arrays[j] = new ArrayInfo(PROB_COL[j], n);
        
        
        myAnswer = new Status[m][n];
        for (int i = 0; i < m; i++) // Initialize answer as Empty
            for (int j = 0; j < n; j++)
                myAnswer[i][j] = Status.Empty;        
    } // end constructor    
    
    /** Solves the given problem.
     *  Tries to solve row by row, column by column until all is solved.
     */
    public void solve()
    {
        boolean noEmpty = false;    // true if there is no empty cells
        int loopCount = 0;
        while (!noEmpty)    // Keep solving arrays until there is no empty cell
        {
            noEmpty = true;     // initialize noEmpty every loop
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
                    Status[] temp = findArraySolution(RowCol.Row,i, m, myAnswer, row_arrays[i]);
                    System.arraycopy(temp, 0, myAnswer[i], 0, n); 
                    //for (int j = 0; j < n; j++)
                    //    answer[i][j] = temp[j]; 
                }
                else
                    System.out.println("Array " + Arrays.toString(PROB_ROW[i]) 
                                       + " is solved.");
            } // end for
            
            System.out.println("--------------------- Column - " + (loopCount+1) +
                               " ---------------------");
            for (int j = 0; j < n; j++) // Scan columns
            {
                if (!col_arrays[j].isSolved())
                {
                    System.out.println("Array " + Arrays.toString(PROB_COL[j]) +
                            "->" + Arrays.toString(col_arrays[j].getArray()) + ":");
                    Status[] temp = findArraySolution(RowCol.Column, j, n, myAnswer, col_arrays[j]);
                    for (int i = 0; i < n; i++)
                    {
                        myAnswer[i][j] = temp[i]; 
                        noEmpty = noEmpty && (myAnswer[i][j] != Status.Empty);
                    } // end for
                }
                else                                    
                    System.out.println("Array " + Arrays.toString(PROB_COL[j]) 
                                       + " is solved.");
            } // end for
            loopCount++;            
        } // end while
    } // end solve
    
    /** Checks whether the solution is correct.
     *  Compares the answer to the given solution for the problem.
     * @return true if the answer is the same as the solution.
     */
    public boolean isCorrect()
    {        
        boolean result = true;
        for (int i = 0; i < m; i ++)
            for (int j = 0; j < n; j++)
                result = result && (myAnswer[i][j] == SOLUTION[i][j]);
        return result;
    } // end isCorrect
    
    /** Construct a string that shows the answer that this solver produces.
     * @return A String containing the answer to the problem.
     */
    @Override
    public String toString()
    {
        String result = "Answer to " + m + " x " + n + " Nonogram Puzzle:\n";
        for (int i = 0; i < m; i++)            
            result += printCells(myAnswer[i]) + "\n";                    
        return result;
    } // end toString   
    
    /** Tries to solve the given array.
     * @param rowcol  Indicator whether a row or a column is being investigated
     * @param idx The index of row/column in the 2D array
     * @param k The length of the row or column
     * @param answer A 2D array of status of each cell of the grid. 
     * @param curArrayInfo  An ArrayInfo object containing current information of the row/col array.
     * @return The updated status of the given array
     */
    public Status[] findArraySolution(RowCol rowcol, int idx, int k, Status[][] answer, ArrayInfo curArrayInfo)
    {           
        Status[] arrayAnswer = new Status[k];    // The answer for the curArray          
        if (rowcol == RowCol.Row)           // When inspecting a row                    
            arrayAnswer = answer[idx];                    
        else // (rowcol == RowCol.Column)   // When inspecting a column                    
            for (int i = 0; i < k; i++)
                arrayAnswer[i] = answer[i][idx];
                
        int b = curArrayInfo.getBeg();  // effective beginning index
        int e = curArrayInfo.getEnd();  // effective end index
        // Push beginning and end index inward if there are consecutive Falses.
        int[] be = updateEnds(b, e,curArrayInfo, arrayAnswer);            
        b = be[0]; 
        e = be[1];
        int[] oldbe;// = be;
        
        int[] curArray = curArrayInfo.getArray(); // Row/column array        
        int a = curArrayInfo.getNum();            // The number of numbers in curArray
        int ke = curArrayInfo.getLength();       // Effective length of this row/column
        System.out.println("\tBeginning of the array:");
        System.out.println("\tb = " + b + ", e = " + e + ", ke = " + ke);
        System.out.println("\t" + printCells(arrayAnswer));
        
        boolean solved = false;             // true if curArray is solved        
        int sum = curArrayInfo.getSum();                        // sum of numbers in curArray        
        int numT = numOfStatus(b, e, arrayAnswer, Status.True);     // The number of True cells    
        int numE = numOfStatus(b, e, arrayAnswer, Status.Empty);    // The number of Empty cells
        int numF = numOfStatus(b, e, arrayAnswer, Status.False);    // The number of False cells
        
        // Start investigation!
        if (sum + (a - 1) == ke) // current condition automatically solves
        {
            System.out.println("\tSolution is found for the array; sum of numbers and spaces == effective length of row/column.");            
                        
            int count = b;      // index count for the answer array
            for (int s = 0; s < a; s++) // loop through numbers in the array (sections)
            {
                int x = curArray[s];    // current number
                System.out.println("\tCurrent number: " + x);
                for (int xi = 0; xi < x; xi++)  // Fill up the cells as many as x
                {
                    arrayAnswer[count] = Status.True;
                    count++;
                } // end for
                if (count < e+1)  // After filling up cells, the immediate cell 
                {               // after the section must be false unless last
                    arrayAnswer[count] = Status.False;
                    count++;
                } // end if
            } // end for
            solved = true;
            System.out.println("\t" + printCells(arrayAnswer));
        } 
        else if (sum == numT)   // When all true cells are found
        {            
            System.out.println("\tSolution is found for the array; sum of numbers == number of True");
                       
            // Make all empty cells False
            for (int i = b; i < e+1; i++) 
                if (arrayAnswer[i] == Status.Empty)
                    arrayAnswer[i] = Status.False;
            solved = true;
            System.out.println("\t" + printCells(arrayAnswer));
        }
        else if (sum == numE && numF + numE == ke || (numE + numT == sum)) // When there are only empty and false cells and all numbers in the array fit into empty cells
        {
            System.out.println("\tSolution is found for the array; sum of numbers == number of Empty OR number of Empty+True");
                       
            // Make all empty cells True
            for (int i = b; i < e+1; i++) 
                if (arrayAnswer[i] == Status.Empty)
                    arrayAnswer[i] = Status.True;
            solved = true;
            System.out.println("\t" + printCells(arrayAnswer));
        }
        else
        {   
            /*  Check the first and the last cell */    
            oldbe = be;
            be = removeEnds(curArrayInfo, arrayAnswer);
            b = be[0];
            e = be[1];
            solved = checkAllTrue(curArrayInfo, arrayAnswer, solved);    
            
            // Update parameters
            curArray = curArrayInfo.getArray(); // Row/column array        
            a = curArrayInfo.getNum();          // The number of numbers in curArray
            ke = curArrayInfo.getLength();      // Effective length of grid                            
            if (!Arrays.equals(oldbe, be))    // If anything is updated, print out the array.
                System.out.println("\tAfter removing ends:\n\tb = " + b + ", e = " + e + ", ke = " + ke + ", a = " + a);
            
            /*  Fill up by general rules. */                
            if (!solved)
            {
                fillUp(curArrayInfo, arrayAnswer);
                solved = checkAllTrue(curArrayInfo, arrayAnswer, solved);    
            } // end if
            
            /* When there is only one number in the array */
            if (a == 1 && !solved) 
            {
                System.out.println("\tThere is only one number in the array.");
                // Fill up between the first and the last true
                int locCurTrue = getFirstStatus(b-1, e, arrayAnswer, Status.True);
                int lastCurTrue = getLastStatus(locCurTrue, e+1, arrayAnswer, Status.True);                
                if (b <= locCurTrue && locCurTrue <= e && locCurTrue != lastCurTrue)    
                {
                    System.out.println("\t\tFill up between first true: " + locCurTrue + " and last true: " + lastCurTrue);
                    for (int i = locCurTrue; i <= lastCurTrue; i++)
                        if (arrayAnswer[i] == Status.Empty)
                            arrayAnswer[i] = Status.True;
                    System.out.println("\t" + printCells(arrayAnswer)); 
                } // end if
                
                // Make small empty clusters false. 
                // Find Empty cell clusters [b, e], inclusive.
                int[][] emptyArrays = findStatusClusters(arrayAnswer, curArrayInfo, Status.Empty);
                int[] begIdxEmpty = emptyArrays[0];
                int[] emptyClusterLen = emptyArrays[1];
                for (int i = 0; i < emptyClusterLen.length; i++)
                {
                    int curBegIdxEmpty = begIdxEmpty[i];
                    int curEmptyClusterLen = emptyClusterLen[i];
                    boolean isBetweenFalses  = 
                    ((curBegIdxEmpty - 1 == -1) || (arrayAnswer[curBegIdxEmpty - 1] == Status.False)) &&
                    ((curBegIdxEmpty + curEmptyClusterLen == k) || (arrayAnswer[curBegIdxEmpty + curEmptyClusterLen] == Status.False));
                    
                    if (curEmptyClusterLen < curArray[0] && isBetweenFalses) 
                    {
                        System.out.println("\t\tMake smaller empty clusters false");
                        for (int ii = curBegIdxEmpty; ii < curBegIdxEmpty + curEmptyClusterLen; ii++)
                            if (arrayAnswer[ii] == Status.Empty)
                                arrayAnswer[ii] = Status.False;     
                        System.out.println("\t" + printCells(arrayAnswer)); 
                    } // end if                            
                } // end for 
                
                /* Check whether this array is solved. */
                solved = checkAllTrue(curArrayInfo, arrayAnswer, solved);
            } // end if
            
            /* When there is a true cluster close to the front/end, fill up to possible cell.
               e. g., [3, 3] _ O _ _ _ _ _ _ _ --> _ O O _ _ _ _ _ _
               e. g., [3, 3] _ _ _ _ _ _ _ O _ --> _ _ _ _ _ _ O O _ */
            if (!solved)
            {
                fillUpEnd(curArrayInfo, arrayAnswer);
                /* Check whether this array is solved. */
                solved = checkAllTrue(curArrayInfo, arrayAnswer, solved);
            } // end if
            
            /* When there is a true cluster just away from the front/end, make first few false.
               e. g., [3, 4] _ _ _ O O _ _ _ _ --> X X _ O O _ _ _ _ _
               e. g., [4, 3] _ _ _ _ _ O _ _ _ --> _ _ _ _ _ O _ _ X */
            if (!solved)
            {
                falseEnd(curArrayInfo, arrayAnswer);
                /* Check whether this array is solved. */
                solved = checkAllTrue(curArrayInfo, arrayAnswer, solved);
            } // end if
            
            if (!solved)
            {
                /*  Check the first and the last cell */    
                oldbe = be;
                be = removeEnds(curArrayInfo, arrayAnswer);
                b = be[0];
                e = be[1];
                solved = checkAllTrue(curArrayInfo, arrayAnswer, solved); 
            } // end if
            
            /* Investigate by sections according to the numbers in the array. */
            if (!solved)
            {
                // Update parameters
                curArray = curArrayInfo.getArray(); // Row/column array        
                a = curArrayInfo.getNum();          // The number of numbers in curArray
                ke = curArrayInfo.getLength();      // Effective length of grid
                sum = curArrayInfo.getSum();        // sum of numbers in curArray            
                numT = numOfStatus(b, e, arrayAnswer, Status.True);  // The number of True cells    
                numE = numOfStatus(b, e, arrayAnswer, Status.Empty); // The number of Empty cells                         
                if (!Arrays.equals(oldbe, be))    // If anything is updated, print out the array.
                    System.out.println("\tAfter removing ends:\n\tb = " + b + ", e = " + e + ", ke = " + ke + ", a = " + a);
                
                int p = -1;      // The last cell index in the previous false cluster
                int q = b-1;     // The first cell index in the next false cluster
                int s = 0;
                while (s < a && !solved) // loop through numbers in the array (sections)
                //for (int s = 0; s < a; s++) // loop through numbers in the array (sections)
                {       
                    int x = curArray[s];    // current number
                    System.out.println("\tCurrent number: " + x);
                    
                    // When the section ended where there are still numbers in the array to be investigated,
                    // use the previous p and q.
                    // Otherwise, use a new pair of p and q.
                    if (!(a > 1 && s > 0 && q == e + 1))
                    {
                        // Determine current section (p, q), exclusive.  
                        // First determine sections based on true/false cells in the current row/column.
                        int[] pq = getpq_conditional(arrayAnswer,curArrayInfo,s,q);
                        p = pq[0];
                        q = pq[1];
                        // Find p and q with the general sectioning method.
                        pq = getpq(curArrayInfo, s);
                        int pGeneral = pq[0];
                        int qGeneral = pq[1];
                        // Determine p and q value by choosing the smallest window
                        p = Math.max(p, pGeneral);
                        q = Math.min(q, qGeneral);                        
                        
                    } // end if
                    int kp = q - p - 1; // The number of empty cells between p and q   
                    System.out.println("\tp = " + p + ", q = " + q + ", k' = " + kp);
                    

                    // Procede only when the index is within boundary
                    if (p < e+1 && q <= e+1) 
                    {                    
                        // Find True cell clusters [b, e], inclusive.
                        int[][] trueArrays = findStatusClusters(arrayAnswer, curArrayInfo, Status.True);
                        int[] begIdxTrue = trueArrays[0];
                        int[] trueClusterLen = trueArrays[1];
                        int numTrueClusters = begIdxTrue.length;

                        // Find Empty cell clusters [b, e], inclusive.
                        int[][] emptyArrays = findStatusClusters(arrayAnswer, curArrayInfo, Status.Empty);
                        int[] begIdxEmpty = emptyArrays[0];
                        int[] emptyClusterLen = emptyArrays[1];
                        int numEmptyClusters = begIdxEmpty.length;
                        int maxEmptyLen = emptyClusterLen[0];
                        for (int i = 0; i < numEmptyClusters; i++)
                            maxEmptyLen = Math.max(maxEmptyLen, emptyClusterLen[i]);

                        // Check whether there is a true cluster that belongs this section.
                        int[] curTrue = getCurrentTrueCluster(p, q, begIdxTrue, trueClusterLen);
                        int locTrue = curTrue[0];   // Beginning index of current True cluster (first one if there are multiple, -1 if none)
                        int lenCurTrue = curTrue[1];    // Length of current True cluster
                        if (lenCurTrue != 0)
                            System.out.println("\tCurrent True Cluster: from " + locTrue + " to " + (locTrue + lenCurTrue - 1));
                        
                        // If current true cluster is bigger than x, skip to the next number in the array
                        if (lenCurTrue > x && s < a-1)
                        {
                            System.out.println("\tCurrent true cluster > x; moving to the next number in the array...");
                            s++;
                            x = curArray[s];                            
                            System.out.println("\tCurrent number: " + x);
                        } // end if
                        
                        /* Make remaining empty cells False when x is the last. */
                        // When x is the last number in the array and there are still 
                        // empty clusters whose length is less than x in later sections,
                        // make them false.
                        if (!solved && (s == a-1) && (q < e)) 
                        {
                            makeLaterEmptyFalse(s, x, p, q, curArrayInfo, arrayAnswer);  
                            // Update parameters
                            be = updateEnds(b, e,curArrayInfo, arrayAnswer);            
                            b = be[0]; 
                            e = be[1];
                            curArray = curArrayInfo.getArray(); // Row/column array        
                            a = curArrayInfo.getNum();          // The number of numbers in curArray
                            //ke = curArrayInfo.getLength();      // Effective length of grid
                            sum = curArrayInfo.getSum();        // sum of numbers in curArray            
                            numT = numOfStatus(b, e, arrayAnswer, Status.True);  // The number of True cells    
                            numE = numOfStatus(b, e, arrayAnswer, Status.Empty); // The number of Empty cells  
                        } // end if                         
                        
                        // Find a true cluster whose length is the same as x and in a later section.
                        boolean foundLaterSameTrue = false;
                        for (int i = 0; i < numTrueClusters; i++)
                            foundLaterSameTrue = foundLaterSameTrue || (begIdxTrue[i] > q && trueClusterLen[i] == x);

                        // Find an empty cluster whose length is greather than or equal to x and in a previous section.
                        boolean foundEarlierLargerEmpty = false;
                        int earlierLargerEmptyLen = 0;
                        for (int i = 0; i < numEmptyClusters; i++)
                        {
                            foundEarlierLargerEmpty = foundEarlierLargerEmpty || (begIdxEmpty[i] < p && emptyClusterLen[i] >= x);
                            if (foundEarlierLargerEmpty)
                                earlierLargerEmptyLen = Math.max(earlierLargerEmptyLen,emptyClusterLen[i]); // In case there are multiple numbers meeting the requirement, choose bigger number.
                        }

                        // Find an empty cluster whose length is the same as x and in the current section.
                        // Or empty cluster + true cluster == x
                        boolean foundThisSameEmpty = false;
                        int ie = 0;
                        while (!foundThisSameEmpty && ie < numEmptyClusters)
                        {
                            foundThisSameEmpty = (p < begIdxEmpty[ie] && begIdxEmpty[ie] < q  && emptyClusterLen[ie] == x);
                            foundThisSameEmpty = foundThisSameEmpty || (lenCurTrue + emptyClusterLen[ie] == x);
                            if (!foundThisSameEmpty)
                                ie++;
                        } // end while
                        
                        // Find an empty cluster whose length is greather than or equal to x and in a previous section.
                        boolean foundEarlierLargerTrue = false;
                        for (int i = 0; i < numTrueClusters; i++)
                            foundEarlierLargerTrue = foundEarlierLargerTrue || (begIdxTrue[i] < p && trueClusterLen[i] > x);

                        // Find an empty cluster whose length is greather than or equal to x and in a next section.
                        // Only if the empty cluster doesn't follow a true cluster whose length is larger than x
                        // (because if that's the case, the later empty cluster doesn't belong to x)
                        boolean foundLaterLargerEmpty = false;
                        int laterLargerEmptyLen = 0;
                        for (int i = 0; i < numEmptyClusters; i++)
                        {
                            int curBegIdxEmpty = begIdxEmpty[i];
                            int curEmptyClusterLen = emptyClusterLen[i];
                            foundLaterLargerEmpty = foundLaterLargerEmpty || (curBegIdxEmpty > q && curEmptyClusterLen >= x);
                            if (foundLaterLargerEmpty && curEmptyClusterLen >= x)
                                laterLargerEmptyLen = curEmptyClusterLen;
                            // Check whether there is a true cluster coming before the empty cluster whose longer than x
                            // (because then x doesn't belong to this empty cluster. e.g., _ _ _ X X O O O _ _ and current number is 2)
                            int[] thisTrue = getCurrentTrueCluster(q, curBegIdxEmpty, begIdxTrue, trueClusterLen);
                            int thisLocTrue = thisTrue[0];   // Beginning index of True cluster (first one if there are multiple, -1 if none)
                            int thisLenTrue = thisTrue[1];    // Length of True cluster
                            if (thisLocTrue != -1)  // there exist true cluster between q and current empty cluster
                                foundLaterLargerEmpty = foundLaterLargerEmpty && (thisLenTrue <= x);
                        } // end for  
                        
                        // Check whether empty clusters are adjacent to true clusters.
                        // In this case, it is not a true empty cluster, rather a potential true cluster.
                        boolean emptyBetweenO = false;
                        for (int i = 0; i < numEmptyClusters; i++)
                        {
                            int curBegIdxEmpty = begIdxEmpty[i];
                            int curEmptyClusterLen = emptyClusterLen[i];
                            if (curBegIdxEmpty > 0)
                                emptyBetweenO = emptyBetweenO || (arrayAnswer[curBegIdxEmpty-1] == Status.True);
                            if (curBegIdxEmpty+curEmptyClusterLen < k)
                                emptyBetweenO = emptyBetweenO || (arrayAnswer[curBegIdxEmpty+curEmptyClusterLen] == Status.True);
                        } // end for

                        int i0 =  p + 1 + kp - x;   // Beginning index of true cells 
                        int sumFromX = 0;   // Summation of numbers + 1 space from x to the end in the array
                        for (int i = s; i < a; i++)
                            sumFromX += curArray[i] + 1;
                        sumFromX -= 1;  // last number doesn't need one space
                        int sumToX = 0; // Summation of numbers + 1 space from first number to x
                        for (int i = 0; i <= s; i++)
                            sumToX += curArray[i] + 1;
                        sumToX -= 1;
                        /* Fill up the middle (2x-k') cells. */
                        // When there are more than one number left in the array but it is alreay filled in the middle of the row/column, 
                        // (so that the code still doesn't know it is filled), and there are multiple locations for the last number to be,
                        // we should skip this. Example is _XXOXX_XOO and array is [1,1,2]                                                                   
                        // [1, 1, 3] _ _ _ X _ X _ _ _ X --> _ _ _ X _ X O O O X
                        // [2, 2] _ _ X X X X _ _ _ _ --> O O X X X X _ _ _ _
                        // [1, 2, 1] O X _ _ _ X _ _ _ _ --> Don't fill
                        // [1, 1, 2] _ _ X O X _ _ O _ X --> Don't fill                                                
                        // [1, 1, 2] _ X O X X X _ O _ X --> O X O X X X _ O _ X (should be fixed P6, modify with foundLaterSameTrue)    
                        if (!solved && (x > kp/2) && (kp >= x) && (i0 >= b) &&
                            (!((sum - numT == x && a >= 2) || (kp < numE && a == 1) || foundLaterSameTrue ||
                            (numEmptyClusters == a && emptyClusterLen[s] == x && numT==x) ||    // [1,1,2] _XXOXX_XOO
                            foundEarlierLargerEmpty || foundLaterLargerEmpty) || // [3] _ _ _ _ _ _ X _ _ _
                            (numEmptyClusters == a && a >= 2 && sumFromX > laterLargerEmptyLen && !emptyBetweenO && sumToX > earlierLargerEmptyLen) ||// [1,2,1] _ _ _ X _ _ X _ _ _ --> _ _ _ X O O X _ _ _
                            (a > 1 && s == a - 1 && foundEarlierLargerTrue && foundThisSameEmpty) ||// [1,2,1] _ _ _ _ X X O O X _ --> _ _ _ _ X X O O X O
                            (numEmptyClusters == a && q == e+1 && foundThisSameEmpty && foundEarlierLargerEmpty && sumToX > earlierLargerEmptyLen)))// [1, 1, 3] _ _ _ X _ X _ _ _ X --> _ _ _ X _ X O O O X
                            //(foundLaterSameTrue && foundThisSameEmpty && laterLargerEmptyLen <= x)))
                        {
                            // When there is a true cluster whose length is the same as x and not in the current section, don't fill up.
                            System.out.println("\tFill up middle (2x-k') cells");
                            for (int i = i0; i <= p + x; i++)
                                if (arrayAnswer[i] == Status.Empty)
                                    arrayAnswer[i] = Status.True;   
                            numT = numOfStatus(b, e, arrayAnswer, Status.True); // Update numT
                            System.out.println("\t" + printCells(arrayAnswer)); 
                            /* Check whether this array is solved. */
                            solved = checkAllTrue(curArrayInfo, arrayAnswer, solved);

                        } // end if 

                        /* Make False When the section length is less than x. */
                        // When there is less number of empty cells than the current number, make false.
                        // But when there is a later section where x actually belongs, skip this part.
                        // Also, only when this section is defined between false cells.
                        if (!solved && x > kp && !(s > 0 && kp >= curArray[s-1])
                            && ((p == -1 || arrayAnswer[p] == Status.False) && (q == k || arrayAnswer[q] == Status.False))) 
                        {
                            System.out.println("\tMake this section false");
                            for (int i = p + 1; i < q; i++)
                                if (arrayAnswer[i] == Status.Empty)
                                    arrayAnswer[i] = Status.False;         
                            System.out.println("\t" + printCells(arrayAnswer)); 
                        } // end if    

                        /* Make cells in this section that are farther than remainingT false */
                        // e.g., [3, 2] _ _ _ O _ _ X _ _ X --> X _ _ O _ _ X _ _ X
                        if (!solved)
                        {
                            solved = makeFarCellsFalse(x, p, q, curArrayInfo, numT, sum, 
                                                       solved, arrayAnswer);
                            /* Check whether this array is solved. */
                            solved = checkAllTrue(curArrayInfo, arrayAnswer, solved);
                        } // end if

                        
                        /* If there is a true cluster whose length is maximum of the numbers, make false around it */
                        // e.g., [1,2] _ _ _ _ _ O O _ _ _ --> _ _ _ _ X O O X _ _                                       
                        if (!solved)
                        {
                            curArrayInfo = makeFalseAround(x, p, q, arrayAnswer, 
                                                           curArrayInfo);
                            /* Check whether this array is solved. */
                            solved = checkAllTrue(curArrayInfo, arrayAnswer, solved);
                        } // end if   
                        
                        
                        /* If the boundary is false and end is true, fill up the section with the current x. */
                        // When p is False and p+1 is True
                        // e.g., [2, 3] X _ _ _ _ X O _ _ _ 
                        // e.g., [2, 2] X _ _ _ _ X O _ _ _ 
                        // e.g., [1, 2] X _ _ _ _ X O _ _ _ 
                        // e.g., [2, 1] X _ _ _ _ X O _ _ _ 
                        // e.g., [1, 1, 2] _ _ _ O _ X O _ _ X 
                        // [2, 1] _ _ _ X O _ _ _ X X --> Don't fill
                        if (!solved && (p == -1 || arrayAnswer[p] == Status.False) 
                            && arrayAnswer[p+1] == Status.True && (x != lenCurTrue)
                            && p != q && sum + (a-1) > kp) 
                        {
                            System.out.println("\tp is false and p+1 is true");
                            for (int i = p+1; i <= p+x; i++)  // Fill up from left
                            {
                                if (arrayAnswer[i] == Status.Empty)
                                    arrayAnswer[i] = Status.True;
                            } // end for
                            if (p+x+1 <= e)  // Right end should be False
                                if (arrayAnswer[p+x+1] == Status.Empty)
                                    arrayAnswer[p+x+1] = Status.False;
                            System.out.println("\t" + printCells(arrayAnswer)); 
                            /* Check whether this array is solved. */
                            solved = checkAllTrue(curArrayInfo, arrayAnswer, solved);
                        }
                        // When q is False and q-1 is True
                        // e.g., [2, 3] X _ _ _ _ X _ _ _ O
                        // e.g., [2, 2] X _ _ _ _ X _ _ _ O
                        // e.g., [1, 2] X _ _ _ _ X _ _ _ O
                        // e.g., [2, 1] X _ _ _ _ X _ _ _ O
                        // [1, 1, 2] _ _ X O X X O _ X X when p = q = 5, x = 1 (second)
                        // [2, 1]_ _ _ O X _ _ _ X X --> Don't fill
                        else if (!solved && (q == k || arrayAnswer[q] == Status.False) 
                                && arrayAnswer[q-1] == Status.True && (x != lenCurTrue)
                                && p != q && sum + (a-1) > kp)
                        {
                            System.out.println("\tq is false and q-1 is true");
                            for (int i = q-1; i >= q-x; i--)  // Fill up from right
                            {
                                if (arrayAnswer[i] == Status.Empty)
                                    arrayAnswer[i] = Status.True;
                            } // end for
                            if (q-x-1 >= b)  // Left end should be False
                                if (arrayAnswer[q-x-1] == Status.Empty)
                                    arrayAnswer[q-x-1] = Status.False;
                            System.out.println("\t" + printCells(arrayAnswer)); 
                            /* Check whether this array is solved. */
                            solved = checkAllTrue(curArrayInfo, arrayAnswer, solved);
                        } // end if

                        // Update parameters
                        a = curArrayInfo.getNum();
                        b = curArrayInfo.getBeg();
                        e = curArrayInfo.getEnd();
                        curArray = curArrayInfo.getArray(); // Row/column array 
                        // Update number of True and Empty before proceeding to the next number
                        sum = curArrayInfo.getSum();
                        numT = numOfStatus(b, e, arrayAnswer, Status.True);
                        numE = numOfStatus(b, e, arrayAnswer, Status.Empty);
                        if (!solved)
                        {
                            System.out.println("\tFor section (" + p + ", " + q +"):");
                            System.out.println("\t" + printCells(arrayAnswer)); 
                        } // end if                        
                        else
                        {
                            System.out.println("\tReached end of the grid.");
                        } // end if
                        if (!solved)
                        {
                            /*  Check the first and the last cell. */ 
                            oldbe = be;
                            be = removeEnds(curArrayInfo, arrayAnswer);
                            b = be[0];
                            e = be[1];                        

                            // Update parameters
                            curArray = curArrayInfo.getArray(); // Row/column array        
                            a = curArrayInfo.getNum();          // The number of numbers in curArray
                            ke = curArrayInfo.getLength();      // Effective length of grid
                            sum = curArrayInfo.getSum();        // sum of numbers in curArray            
                            numT = numOfStatus(b, e, arrayAnswer, Status.True);  // The number of True cells    
                            numE = numOfStatus(b, e, arrayAnswer, Status.Empty); // The number of Empty cells         
                            if (!Arrays.equals(oldbe, be))    // If anything is updated, print out the array.
                            {
                                System.out.println("\tAfter removing ends:\n\tb = " + b + ", e = " + e + ", ke = " + ke);
                                if (oldbe[0] < b)   // first number is removed or this section was made false
                                    s--;
                            }
                            /* Check whether this array is solved. */
                            solved = checkAllTrue(curArrayInfo, arrayAnswer, solved);
                        } // end if                            
                    } // end if
                    s++;
                } // end while                
            } // end if
            if (solved)
                System.out.println("\t" + printCells(arrayAnswer));
        } // end if
        // Update beginning and end index
        oldbe = be;
        be = updateEnds(b, e, curArrayInfo, arrayAnswer);
        b = be[0];
        e = be[1];
        ke = curArrayInfo.getLength();
        if (!Arrays.equals(oldbe, be))    // If anything is updated, print out the array.
            System.out.println("\tUpdating Ends:\n\tb = " + b + ", e = " + e + ", ke = " + ke);        
        // Update solved arrays
        curArrayInfo.setSolved(solved);     
        
        if (rowcol == RowCol.Row)           // When inspecting a row
        {
            myAnswer[idx] = arrayAnswer;
            row_arrays[idx] = curArrayInfo;
        }
        else // (rowcol == RowCol.Column)   // When inspecting a column                    
        {
            for (int i = 0; i < k; i++)
                myAnswer[i][idx] = arrayAnswer[i];
            col_arrays[idx] = curArrayInfo;
        } // end if
        myAnswer = answer;
        return arrayAnswer;
    } // end findArraySolution
    
    private String printCells(Status[] anArray)
    {
        String result = "";
        for (var curCell : anArray)
        {   
            if (null != curCell)
                switch (curCell) {
                case True:
                    result += "O ";
                    break;
                case False:            
                    result += "X ";
                    break;
                case Empty:
                    result += "_ ";
                    break;
                default:
                    break;
            }            
        } // end for
        return result;
    } // end pritnCells
    
    // Fill up true cells and make false at the end of this true cluster.
    private void finishFirstNumber(int[] curArray, Status[] arrayAnswer, ArrayInfo curArrayInfo)
    {
        System.out.println("\tRemoving the first number in the array");
        int x = curArray[0];
        int b = curArrayInfo.getBeg();
        int e = curArrayInfo.getEnd();
        System.out.println("\t\tCurrent number: " + x);
        for (int i = b+1; i < b+x; i++)
            if (arrayAnswer[i] == Status.Empty)
                arrayAnswer[i] = Status.True;
        int i0 = b + x;
        if (i0 <= e)
            arrayAnswer[i0] = Status.False;      
        System.out.println("\t" + printCells(arrayAnswer)); 
        // Remove the first number from this array
        curArrayInfo.removeFirstInArray();
    } // end finishFirstNumber
    
    // Fill up true cells and make false in front of this true cluster.
    private void finishLastNumber(int[] curArray, Status[] arrayAnswer,  ArrayInfo curArrayInfo)
    {
        System.out.println("\tRemoving the last number in the array");
        int a = curArrayInfo.getNum();
        int x = curArray[a-1];
        int b = curArrayInfo.getBeg();
        int e = curArrayInfo.getEnd();        
        System.out.println("\t\tCurrent number: " + x);
        for (int i = e+1-x; i < e+1-1; i++)
            if (arrayAnswer[i] == Status.Empty)
                arrayAnswer[i] = Status.True;
        int i0 = e+1-x-1;
        if (i0 >= b)
            arrayAnswer[i0] = Status.False;          
        System.out.println("\t" + printCells(arrayAnswer)); 
        // Remove the last number from this array
        curArrayInfo.removeLastInArray();          
    } // end finishLastNumber
    
    private boolean checkAllTrue(ArrayInfo curArrayInfo, Status[] arrayAnswer, boolean solved)
    {        
        int b = curArrayInfo.getBeg();  // effective beginning index
        int e = curArrayInfo.getEnd();  // effective end index      
        //System.out.println("b = " + b + ", e = " + e);
        //int[] curArray = curArrayInfo.getArray(); // Row/column array        
        //int a = curArrayInfo.getNum();            // The number of numbers in curArray                
        int sum = curArrayInfo.getSum();                        // sum of numbers in curArray        
        int numT = numOfStatus(b, e, arrayAnswer, Status.True); // The number of True cells    
        
        if (sum == numT)   // When all true cells are found
        {            
            System.out.println("\tSolution is found for the array; sum of numbers == number of True (else)");
            
            // Make all empty cells False
            for (int i = b; i < e+1; i++) 
                if (arrayAnswer[i] == Status.Empty)
                    arrayAnswer[i] = Status.False;
            solved = true;
            //System.out.println("\t" + printCells(arrayAnswer));
        } // end if
        return solved;
    } // end checkAllTrue
    
    private int[] getpq_conditional(Status[] arrayAnswer, ArrayInfo curArrayInfo, int s, int q)
    {
        int b = curArrayInfo.getBeg();
        int e = curArrayInfo.getEnd();
        int a = curArrayInfo.getNum();        
        
        int p = Math.max(q, b-1);
        while (p+1 < e+1 && arrayAnswer[p+1] == Status.False)
            p++;                
        q = p + 1;
        while(q < e+1 && arrayAnswer[q] == Status.Empty)
            q++;

        //System.out.println("p = " + p + ", q = " + q);
        if (p < e+1 && q <= e+1) // procede only when the index is within boundary
        {
            if (p > -1 && arrayAnswer[p] == Status.True)  // push back p by one when true
                p--;  
            while (p > -1 && arrayAnswer[p] == Status.Empty)  // push back p by one when true
                p--; 
            while(q < e+1 && arrayAnswer[q] == Status.True) // push forward q to the next true cluster when true
                q++;
            while(q < e+1 && arrayAnswer[q] == Status.Empty)
                q++;
            if (s == a - 1) // When x is last number in the array
            {   // Find the first false
                while(q < e+1 && arrayAnswer[q] != Status.False)
                    q++;
            } // end if 
        } // end if
        int[] result = {p,q};
        return result;
    } // end getpq_conditional
    
    private int[] updateEnds(int b, int e, ArrayInfo curArrayInfo, Status[] arrayAnswer)
    {
        // Make beginning index the first non-false cell
        b = curArrayInfo.getBeg();  // effective beginning index        
        while (b <= e && arrayAnswer[b] == Status.False)
            b++;
        curArrayInfo.setBeg(b);        
        // Make end index the first non-false cell from the end
        e = curArrayInfo.getEnd();  // effective end index
        while (e >= b && arrayAnswer[e] == Status.False)
            e--;
        curArrayInfo.setEnd(e);
        //System.out.println("\tb = " + b + ", e = " + e);
        int[] result = {b,e};
        return result;
    } // end updateEnds
    
    // Make cells in this section that are farther than remainingT false
    public boolean makeFarCellsFalse(int x, int p, int q, ArrayInfo curArrayInfo,
                                int numT, int sum, boolean solved,  
                                Status[] arrayAnswer)
    {   
        //int b = curArrayInfo.getBeg();
        //int e = curArrayInfo.getEnd();
        //int a = curArrayInfo.getNum();
        
        // Find True cell clusters [b, e], inclusive.
        int[][] trueArrays = findStatusClusters(arrayAnswer, curArrayInfo, Status.True);
        int[] begIdxTrue = trueArrays[0];
        int[] trueClusterLen = trueArrays[1];

        // Check whether there is a true cluster that belongs this section.
        int[] curTrue = getCurrentTrueCluster(p, q, begIdxTrue, trueClusterLen);
        int locCurTrue = curTrue[0];   // Beginning index of current True cluster (first one if there are multiple, -1 if none)
        int lenCurTrue = curTrue[1];    // Length of current True cluster
         
        //int lastCurTrue = locCurTrue + lenCurTrue - 1;
        int remainingT = x - lenCurTrue;   // Remaining number of true
         // System.out.println("\tlocCurTrue = " + locCurTrue +
         //       ", lenCurTrue = " + lenCurTrue + ", lastCurTrue = " + 
         //       lastCurTrue + ", remainingT = " + remainingT);
        //System.out.println("\tnumT = " + numT + ", sum = " + sum);
        if (locCurTrue != -1)
        {                        
            if (remainingT == 0 && numT == sum)    // All true are found
            {
                System.out.println("\tSolution is found for the array; Remaining number of True == 0");
                if (arrayAnswer[locCurTrue-1] == Status.Empty)
                    arrayAnswer[locCurTrue-1] = Status.False;
                solved = true;
            }
            else    // Make cells in this section that are farther than remainingT false
            {  
                makeFalseBetweenTrues(curArrayInfo, arrayAnswer);
                //System.out.println("\t" + printCells(arrayAnswer)); 
                makeFalseBetweenTruesOneApart(curArrayInfo, arrayAnswer);
                //System.out.println("\t" + printCells(arrayAnswer)); 
            } // end if
        } // end if
        return solved;
    } // end makeFarCellsFalse
    
    // If there is a true cluster whose length is maximum of the numbers, make false around it
    // e.g., [1,2] _ _ _ _ _ O O _ _ _ --> _ _ _ _ X O O X _ _ 
    private ArrayInfo makeFalseAround(int x, int p, int q,
                                Status[] arrayAnswer, ArrayInfo curArrayInfo)
    {        
        int a = curArrayInfo.getNum();
        int b = curArrayInfo.getBeg();
        int e = curArrayInfo.getEnd();
        int[] curArray = curArrayInfo.getArray();        
        int kp = q - p - 1; // The number of empty cells between p and q   
        //int locCurTrue = getFirstStatus(p, e, arrayAnswer, Status.True);
        
        // Find True cell clusters [b, e], inclusive.
        int[][] trueArrays = findStatusClusters(arrayAnswer, curArrayInfo, Status.True);
        int[] begIdxTrue = trueArrays[0];
        int[] trueClusterLen = trueArrays[1];
        // Check whether there is a true cluster that belongs this section.
        int[] curTrue = getCurrentTrueCluster(p, q, begIdxTrue, trueClusterLen);
        int locCurTrue = curTrue[0];   // Beginning index of current True cluster (first one if there are multiple, -1 if none)
        int lenCurTrue = curTrue[1];    // Length of current True cluster
        
        int s = curArrayInfo.indexOf(x);    // Index of x in the array
        int sum = curArrayInfo.sumArray(0,s-1) + s;
        
        //if (locCurTrue <= e)  // when there is true cell
        if (locCurTrue != -1)  // when there is true cell
        {
            int it = locCurTrue + lenCurTrue - 1;
            // int it = locCurTrue; // True cell iterator; becomes the last index of true cluster
            // // Find a true cluster 
            // while (it < e && arrayAnswer[it + 1] == Status.True)
            //     it++;
            //int lenCurTrue = it - locCurTrue + 1;  // the length of true cluster
            // When lenCurTrue is the largest number in the array or x == numT
            // Or when the number of true clusters == a && currentTrueCluster == x
            // [2, 1, 1]	_ _ _ _ _ O _ _ X O should not make false around.
            
             // Find the last true cluster whose length is the same as or less than x and in a later section.
            boolean foundLastTrueClusterLater = false; // there is later true cluster which belongs to the last number
            boolean belongsToLastNum;
            boolean foundLaterLessTrue;
            for (int i = 0; i < trueClusterLen.length; i++)
            {
                int endIdx = begIdxTrue[i] + trueClusterLen[i] - 1;
                foundLaterLessTrue = (begIdxTrue[i] > q && trueClusterLen[i] <= x);
                belongsToLastNum = endIdx >= e - curArray[a-1];
                foundLastTrueClusterLater = foundLastTrueClusterLater || (foundLaterLessTrue && belongsToLastNum);                
            } // end for            
            
            if (curArrayInfo.isMax(lenCurTrue) || 
                (trueClusterLen.length == a && lenCurTrue == x) || 
                (lenCurTrue == x && s == a-1 && locCurTrue > sum && it > e - (x + 1)) || 
                (lenCurTrue == x && foundLastTrueClusterLater))
            {
                System.out.println("\tMake false around true cells.");
                if (locCurTrue-1 >= b)   // Make false right before the true cluster
                    arrayAnswer[locCurTrue-1] = Status.False;
                if (it+1 <= e)  // Make false right after the true cluster
                    arrayAnswer[it+1] = Status.False;
                // when first number in the array, make false to the beginning
                if (curArrayInfo.indexOf(lenCurTrue) == 0 && (kp < x))
                {
                    System.out.println("\t\tFisrt number in the array, make false to the beginning");
                    int iFalse = locCurTrue - 2;    // False iterator
                    while (iFalse >= b)
                    {
                        arrayAnswer[iFalse] = Status.False;
                        iFalse--;
                    } // end while                                
                    curArrayInfo.setBeg(locCurTrue);    // Update beginning to the locCurTrue
                    finishFirstNumber(curArray, arrayAnswer, curArrayInfo);
                } // end if
                // when last number in the array, make false to the end
                else if (curArrayInfo.indexOf(lenCurTrue) == a-1)     
                {
                    System.out.println("\t\tLast number in the array, make false to the end");
                    int iFalse = it + 2;    // False iterator
                    while (iFalse <= e)
                    {
                        arrayAnswer[iFalse] = Status.False;
                        iFalse++;
                    } // end while                                
                    curArrayInfo.setEnd(it);    // Update end to the last true
                    finishLastNumber(curArray, arrayAnswer, curArrayInfo);
                } // end if
                System.out.println("\t" + printCells(arrayAnswer)); 
            } // end if                        
        } // end if
        return curArrayInfo;
    } // end makeFalseAround
    
    // Returns the index of the first occurrence of the given Status stat between p+1 and e.
    // e.g., X _ O _ O O _ X _ _ --> Finding first true, p = 0, e = 9 --> 2
    private int getFirstStatus(int p, int e, Status[] arrayAnswer, Status stat)
    {
        int firstStatus = p+1;
        while (firstStatus < e+1 && arrayAnswer[firstStatus] != stat)
            firstStatus++;
        return firstStatus;
    } // end getFirstStatus
    
    // Returns the index of the last occurrence of the given Status stat between b and q-1.
    // e.g., X _ O _ O O _ X _ _  --> firstStatus = 2, e = 9 --> lastStatus = 5
    private int getLastStatus(int b, int q, Status[] arrayAnswer, Status stat)
    {        
        int lastStatus = q-1;
        while(lastStatus >= b && arrayAnswer[lastStatus] != stat)
            lastStatus--;
        return lastStatus;
    } // end getLastStatus
    
    // Returns the index of the last occurrence of the given Status cluster.
    // e.g., X _ O _ O O _ X _ _ --> firstStatus = 2, e = 9 --> lastStatus = 2
    private int getLastStatusCluster(int firstStatus, int e, Status[] arrayAnswer, Status stat)
    {        
        int lastStatus = firstStatus - 1;
        while(lastStatus + 1 <= e && arrayAnswer[lastStatus + 1] == stat)
            lastStatus++;
        return lastStatus;
    } // end getLastStatusCluster
    // Returns the number of cells with given Status stat between b and e, inclusive.
    private int numOfStatus(int b, int e, Status[] arrayAnswer, Status stat)
    {
        int numStat = 0;        
        for (int i = b; i < e+1; i++)   
        {
            if (arrayAnswer[i] == stat)
                numStat++;
        } // end for
        return numStat;
    } // end numOfStatus
    
    // Returns an array of beginning index of stat cells and 
    //         an array of length of the stat clusters
    private int[][] findStatusClusters(Status[] arrayAnswer, ArrayInfo curArrayInfo, Status stat)
    {
        int b = curArrayInfo.getBeg();
        int e = curArrayInfo.getEnd();        
        ArrayList<Integer> begStatusIdx_temp = new ArrayList<>();
        ArrayList<Integer> lenStatus_temp = new ArrayList<>();        
        
        int locCurTrue, lastCurTrue, lenCurTrue;
        int i = b;
        while (i <= e)
        {
            locCurTrue = getFirstStatus(i - 1, e, arrayAnswer, stat);
            lastCurTrue = getLastStatusCluster(locCurTrue, e, arrayAnswer, stat);            
            lenCurTrue = lastCurTrue - locCurTrue + 1;
            if (lenCurTrue > 0)
            {
                begStatusIdx_temp.add(locCurTrue);
                lenStatus_temp.add(lenCurTrue);
            } // end if
            i = lastCurTrue + 1;
        } // end while
        int arrayLen = begStatusIdx_temp.size();
        int[] begStatusIdx = new int[arrayLen];
        int[] lenStatus = new int[arrayLen];
        for (i = 0; i < arrayLen; i++)
        {
            begStatusIdx[i] = begStatusIdx_temp.get(i);
            lenStatus[i] = lenStatus_temp.get(i);
        } // end for
        
        int[][] statArrays = {begStatusIdx, lenStatus};
        return statArrays;
    } // end findStatusClusters
    
    // Check whether there is a true cluster that belongs this section (p, q), exclusive.
    private int[] getCurrentTrueCluster(int p, int q, int[] begIdxTrue, int[] trueClusterLen)
    {
        int locTrue = -1;   // Beginning index of True cluster (first one if there are multiple, -1 if none)
        int lenCurTrue = 0;    // Length of True cluster
        int i = 0;
        boolean foundTrueCluster = false;
        while (i < trueClusterLen.length && !foundTrueCluster)                    
        {
            foundTrueCluster = p < begIdxTrue[i] && begIdxTrue[i] < q;
            if (foundTrueCluster)
            {
                locTrue = begIdxTrue[i];
                lenCurTrue = trueClusterLen[i];
            } 
            else
                i++;
        } // end for
        int[] result = {locTrue, lenCurTrue};
        return result;
    } // end getCurrentTrueCluster
    
    private int[] removeEnds(ArrayInfo curArrayInfo, Status[] arrayAnswer)
    {
        int b = curArrayInfo.getBeg();
        int e = curArrayInfo.getEnd();
        int[] be = updateEnds(b, e, curArrayInfo, arrayAnswer);        
        b = be[0];
        e = be[1];
        
        int[] curArray = curArrayInfo.getArray();
        /*  Check the first and the last cell */
        // When the first cell is filled 
        while (b <= e && arrayAnswer[b] == Status.True)   
        {
            finishFirstNumber(curArray, arrayAnswer, curArrayInfo);  
            b = curArrayInfo.getBeg();
            curArray = curArrayInfo.getArray();
        } // end while
        be = updateEnds(b, e, curArrayInfo, arrayAnswer);        
        b = be[0];
        e = be[1];
        // When the last cell is filled
        while (e >= b && arrayAnswer[e+1-1] == Status.True)   
        {
            finishLastNumber(curArray, arrayAnswer, curArrayInfo);
            e = curArrayInfo.getEnd();
            curArray = curArrayInfo.getArray();
        } // end while
        be = updateEnds(b, e, curArrayInfo, arrayAnswer);            
        return be;
    } // end removeEnds
    
    private void fillUpWhenOneShort(ArrayInfo curArrayInfo, Status[] arrayAnswer)
    {
        int[] curArray = curArrayInfo.getArray();
        int b = curArrayInfo.getBeg();
        int e = curArrayInfo.getEnd();
        int a = curArrayInfo.getNum();
        
        int x = curArray[0];  // current number    
        System.out.println("\t\tCurrent number: " + x);
        // Fill up the definite true cells
        // Section 1: from 0 to x            
        for (int i = b+1; i < b+x; i++)
            arrayAnswer[i] = Status.True;
        System.out.println("\t" + printCells(arrayAnswer));         
        
        int count = b+x + 1;  // count of total cells investigated
        if (a > 1)  // when there are more than one number
        {
            if (a > 2)
            {                    
                for (int s = 1; s < a-1; s++) // loop through numbers in the array (sections)
                {   // Section s: from (count) to (count+x)                     
                    x = curArray[s];    // current number         
                    System.out.println("\t\tCurrent number: " + x);
                    for (int i = count+1; i < count+x; i++)
                        arrayAnswer[i] = Status.True;  
                    count += x + 1;
                    System.out.println("\t" + printCells(arrayAnswer));                     
                } // end for
            } // end if
            // Last section: from count to (ke-1)
            x = curArray[a-1];            
            System.out.println("\t\tCurrent number: " + x);
            for (int i = count+1; i < e+1-1; i++)
                arrayAnswer[i] = Status.True;  
            System.out.println("\t" + printCells(arrayAnswer)); 
        } // end if        
    }// end fillUpWhenOneShort
    
    private int[][] getpqOneShort(ArrayInfo curArrayInfo)
    {
        int[] curArray = curArrayInfo.getArray();
        int b = curArrayInfo.getBeg();
        int e = curArrayInfo.getEnd();
        int a = curArrayInfo.getNum();
        
        // For one short case, we predetermine the sections.
        int[] pList = new int[a];   // List of p
        int[] qList = new int[a];   // List of q
        
        int x = curArray[0];  // current number            
        pList[0] = b-1;
        qList[0] = b+x+1;
        
        int count = b+x + 1;  // count of total cells investigated         
        if (a > 1)  // when there are more than one number
        {
            if (a > 2)
            {                    
                for (int s = 1; s < a-1; s++) // loop through numbers in the array (sections)
                {   // Section s: from (count) to (count+x)                     
                    x = curArray[s];    // current number                             
                    count += x + 1;                    
                    pList[s] = qList[s-1]-1;
                    qList[s] = count;
                } // end for
            } // end if
            // Last section: from count to (ke-1)
            x = curArray[a-1];
            count += x + 1;
            pList[a-1] = qList[a-2]-1;
            qList[a-1] = count;
        } // end if
        int[][] pq = {pList, qList};
        return pq;
    }// end fillUpWhenOneShort
    
    /* Make remaining empty cells False when x is the last. */
    // When x is the last number in the array and there are still 
    // empty clusters whose length is less than x in later sections,
    // make them false.
    private void makeLaterEmptyFalse(int s, int x, int p, int q, ArrayInfo curArrayInfo, Status[] arrayAnswer)
    {            
        int[] curArray = curArrayInfo.getArray();
        
        // Find True cell clusters [b, e], inclusive.
        int[][] trueArrays = findStatusClusters(arrayAnswer, curArrayInfo, Status.True);
        int[] begIdxTrue = trueArrays[0];
        int[] trueClusterLen = trueArrays[1];

        // Find Empty cell clusters [b, e], inclusive.
        int[][] emptyArrays = findStatusClusters(arrayAnswer, curArrayInfo, Status.Empty);
        int[] begIdxEmpty = emptyArrays[0];
        int[] emptyClusterLen = emptyArrays[1];

        // Check whether there is a true cluster that belongs this section.
        int[] curTrue = getCurrentTrueCluster(p, q, begIdxTrue, trueClusterLen);
        int locTrue = curTrue[0];   // Beginning index of current True cluster (first one if there are multiple, -1 if none)
        int lenCurTrue = curTrue[1];    // Length of current True cluster
        
        // Check whether there is a true cluster in this section
        boolean foundThisTrue = (locTrue != -1);                        
        // Find a true cluster whose length is the same as x and in a later section.
        boolean foundLaterTrue = false;
        for (int i = 0; i < trueClusterLen.length; i++)
            foundLaterTrue = foundLaterTrue || (begIdxTrue[i] > q);                        
        // Find an empty cluster whose length is smaller than x and in the next section.
        boolean foundLaterSmallerEmpty;// = false;
        for (int i = 0; i < emptyClusterLen.length; i++)
        {
            int curBegIdxEmpty = begIdxEmpty[i];
            int curEmptyClusterLen = emptyClusterLen[i];
            foundLaterSmallerEmpty = (curBegIdxEmpty > q && curEmptyClusterLen < x);
            if ((curBegIdxEmpty > q) && !foundLaterTrue && (lenCurTrue <= x) &&
                (foundLaterSmallerEmpty || (foundThisTrue && s > 0 && curArray[s-1] != x))) // when true cluster is found, we don't know whether x belongs to this cluster if x is the same as previous number
            {
                System.out.println("\tMake later sections false");
                for (int ii = curBegIdxEmpty; ii < curBegIdxEmpty + curEmptyClusterLen; ii++)
                    if (arrayAnswer[ii] == Status.Empty)
                        arrayAnswer[ii] = Status.False;     
                System.out.println("\t" + printCells(arrayAnswer)); 
            } // end if                            
        } // end for 
    } // end makeLaterEmptyFalse
    
    private void makeFalseBetweenTrues(ArrayInfo curArrayInfo, Status[] arrayAnswer)
    {       
        int b = curArrayInfo.getBeg();
        int e = curArrayInfo.getEnd();
        int a = curArrayInfo.getNum();
        int[] curArray = curArrayInfo.getArray();
        
        // Find True cell clusters [b, e], inclusive.
        int[][] trueArrays = findStatusClusters(arrayAnswer, curArrayInfo, Status.True);
        int[] begIdxTrue = trueArrays[0];
        int[] trueClusterLen = trueArrays[1];        
        int trueArrayLen = begIdxTrue.length;
        int[] endIdxTrue = new int[trueArrayLen];
        for (int i = 0 ;i < trueArrayLen; i++)
        {
            endIdxTrue[i] = begIdxTrue[i] + trueClusterLen[i] - 1;
        }
        int x; // current number
        // When the number of true clusters is same as number of numbers in array
        // And only when there are multiple numbers (single number is dealt in makeFarCellsFalse)        
        if (trueArrayLen == a) 
        {   
            int[] idxBeg = new int[a];  // An array of beginning index of each number's possible range
            int[] idxEnd = new int[a]; // An array of end index of each number's possible range            
            for (int i = 0; i < a; i++) // Find beginning & end index of possible true range
            {
                x = curArray[i];  // current number
                int curTrueBeg = begIdxTrue[i]; // Beginning index of current True cluster (first one if there are multiple, -1 if none)
                int curTrueLen = trueClusterLen[i]; // Length of current True cluster
                int curTrueEnd = endIdxTrue[i]; // Last index of current True cluster 
                int remainingT = x - curTrueLen; // Remaining number of true

                idxBeg[i] = Math.max(b,curTrueBeg - remainingT);
                idxEnd[i] = Math.min(e,curTrueEnd + remainingT);                
            } // end for
            System.out.println("\tMake false between true clusters.");
            // Section 1: 
            x = curArray[0];  // current number    
            System.out.println("\t\tCurrent number: " + x);
            // Make false around possible true range
            for (int i = b; i < idxBeg[0]; i++)   // Make false before the range (only for first x)
                arrayAnswer[i] = Status.False;
            if (a > 1)
            {
                for (int i = idxEnd[0]+1; i < idxBeg[1]; i++)   // Make false after the range
                    arrayAnswer[i] = Status.False;
                System.out.println("\t" + printCells(arrayAnswer)); 

                if (a > 2)  // When there 3 or more, consider middle sections
                {                    
                    for (int s = 1; s < a-1; s++) // loop through numbers in the array (sections)
                    {   // Section s:               
                        x = curArray[s];    // current number         
                        System.out.println("\t\tCurrent number: " + x);
                        for (int i = idxEnd[s]+1; i < idxBeg[s+1]; i++) // Make false after the range
                            arrayAnswer[i] = Status.False;                      
                        System.out.println("\t" + printCells(arrayAnswer));                     
                    } // end for
                } // end if
            } // end if
            // Last section: 
            x = curArray[a-1];            
            System.out.println("\t\tCurrent number: " + x);
            for (int i = idxEnd[a-1]+1; i < e+1; i++) // Make false after the range
                arrayAnswer[i] = Status.False;              
            System.out.println("\t" + printCells(arrayAnswer));             
        }
        else    // when number of true cluster != number of numbers in array
        {
            // Find the longest true cluster            
            int longestTrueBeg = begIdxTrue[0]; // Beginning index of longest True cluster (first one if there are multiple, -1 if none)
            int longestTrueLen = trueClusterLen[0]; // Length of longest True cluster
            for (int i = 0; i < trueArrayLen; i++)
            {
                if (longestTrueLen < trueClusterLen[i])
                {
                    longestTrueBeg = begIdxTrue[i];
                    longestTrueLen = trueClusterLen[i];
                } // end if
            } // end for            
            int longestTrueEnd = longestTrueBeg + longestTrueLen - 1; // Last index of current True cluster
            
            // Find largest number except first and last in the array, respectively. 
            int largestExceptLast = curArray[0];
            int largestExceptFirst = curArray[a-1];
            for (int i = 0; i < a-1; i++)
            {
                largestExceptLast = Math.max(largestExceptLast,curArray[i]);
                largestExceptFirst = Math.max(largestExceptFirst, curArray[a-1-i]);
            } // end for
            
            // When the longest true cluster is bigger than any number in the array except the last,
            // make false to the end
            if (longestTrueLen > largestExceptLast && largestExceptLast < curArray[a-1])
            {   
                x = curArray[a-1];            
                int remainingT = x - longestTrueLen; // Remaining number of true
                int idxEnd = Math.min(e,longestTrueEnd + remainingT);
                System.out.println("\t\tCurrent number: " + x);
                System.out.println("\tMake false to the end.");
                for (int i = idxEnd+1; i < e+1; i++) // Make false after the range                                    
                    arrayAnswer[i] = Status.False;                    
                System.out.println("\t" + printCells(arrayAnswer)); 
                
                // Make false around second largest true cluster
                // e. g.,  _ O _ _ _ O O O O _ -> X O X _ _ O O O O _ 
                if (trueArrayLen > 1)
                {
                    for (int i = 0; i < trueArrayLen-1; i++)
                    {
                        if (trueClusterLen[i] == largestExceptLast)
                        {                            
                            if (begIdxTrue[i]-1 >= b)   
                            {   // Make false right before the true cluster
                                System.out.println("\tMake false around true cluster.");
                                arrayAnswer[begIdxTrue[i]-1] = Status.False;
                            }
                            if (endIdxTrue[i]+1 <= e)  // Make false right after the true cluster
                                arrayAnswer[endIdxTrue[i]+1] = Status.False;
                            System.out.println("\t" + printCells(arrayAnswer)); 
                        } // end if
                    } // end for
                } // end if
            } // end if
            // When the longest true cluster is bigger than any number in the array except the first,
            // hence belongs to the first number, make false to the front
            if (longestTrueLen > largestExceptFirst && largestExceptFirst < curArray[0])
            {                
                x = curArray[0];  // current number    
                int remainingT = x - longestTrueLen; // Remaining number of true
                int idxBeg = Math.max(b,longestTrueBeg - remainingT);
                System.out.println("\t\tCurrent number: " + x);
                // Make false around possible true range
                System.out.println("\tMake false to the front.");
                for (int i = b; i < idxBeg; i++)   // Make false before the range (only for first x)    
                    arrayAnswer[i] = Status.False;                                    
                System.out.println("\t" + printCells(arrayAnswer)); 
                
                // Make false around second largest true cluster
                // e. g., _ O O O O _ _ O _ _ -> _ O O O O _ X O X _
                if (trueArrayLen > 1)
                {
                    for (int i = 1; i < trueArrayLen; i++)
                    {
                        if (trueClusterLen[i] == largestExceptFirst)
                        {                            
                            if (begIdxTrue[i]-1 >= b)   // Make false right before the true cluster
                            {
                                System.out.println("\tMake false around true cluster.");
                                arrayAnswer[begIdxTrue[i]-1] = Status.False;
                            }
                            if (endIdxTrue[i]+1 <= e)  // Make false right after the true cluster
                                arrayAnswer[endIdxTrue[i]+1] = Status.False;
                            System.out.println("\t" + printCells(arrayAnswer)); 
                        } // end if
                    } // end for
                } // end if
            } // end if
        }// end if
    } // end makeFalseBetweenTrues
    
    private void makeFalseBetweenTruesOneApart(ArrayInfo curArrayInfo, Status[] arrayAnswer)
    {           
        int a = curArrayInfo.getNum();
        int[] curArray = curArrayInfo.getArray();
        int xMax = curArray[0];
        for (int i = 0; i < a; i++)
            xMax = Math.max(xMax, curArray[i]);
        
        // Find True cell clusters [b, e], inclusive.
        int[][] trueArrays = findStatusClusters(arrayAnswer, curArrayInfo, Status.True);
        int[] begIdxTrue = trueArrays[0];
        int[] trueClusterLen = trueArrays[1];
        int trueArrayLen = begIdxTrue.length;
        int[] endIdxTrue = new int[trueArrayLen];
        for (int i = 0; i < trueArrayLen;i++)
            endIdxTrue[i] = begIdxTrue[i] + trueClusterLen[i] - 1;
        
        for (int i = 0; i < trueArrayLen-1; i++)
        {
            int gap = begIdxTrue[i+1] - endIdxTrue[i] - 1;    // gap between this and next true clusters
            // When the gap is only 1 empty cell && combined length of this and 
            // next true cluster is greather than maximum number in the array
            if (gap == 1 && (endIdxTrue[i+1] - begIdxTrue[i] + 1> xMax))
            {                
                if (arrayAnswer[begIdxTrue[i+1]-1] == Status.Empty)
                {
                    arrayAnswer[begIdxTrue[i+1]-1] = Status.False;
                    System.out.println("\tThe two true clusters are separate.");
                    System.out.println("\t" + printCells(arrayAnswer)); 
                }
            }
        } // end for
    } // end makeFalseBetweenTruesOneApart
    
    // When there is a true cluster close to the front/end, fill up to possible cell.
    // e. g., [3, 3] _ O _ _ _ _ _ _ _ --> _ O O _ _ _ _ _ _
    // e. g., [3, 3] _ _ _ _ _ _ _ O _ --> _ _ _ _ _ _ O O _
    private void fillUpEnd(ArrayInfo curArrayInfo, Status[] arrayAnswer)
    {
        // Find True cell clusters [b, e], inclusive.
        int[][] trueArrays = findStatusClusters(arrayAnswer, curArrayInfo, Status.True);
        int[] begIdxTrue = trueArrays[0];
        int[] trueClusterLen = trueArrays[1];
        int numTrueClusters = begIdxTrue.length;
        
        if (numTrueClusters > 0)  // when there is a true cluster
        {            
            int b = curArrayInfo.getBeg();
            int e = curArrayInfo.getEnd();
            int a = curArrayInfo.getNum();
            
            int begIdx0 = begIdxTrue[0];
            int endIdx0 = begIdx0 + trueClusterLen[0] - 1;
            int x0 = curArrayInfo.getArray()[0];
            if ((trueClusterLen[0] < x0) && (endIdx0 < b + x0 - 1) && (endIdx0 + 1 < b + x0))
            {
                System.out.println("\tMake first few cells true.");
                for (int i = endIdx0 + 1; i < b + x0; i++)
                    arrayAnswer[i] = Status.True;
                System.out.println("\t" + printCells(arrayAnswer)); 
            } // end if
            
            int begIdxe = begIdxTrue[numTrueClusters-1];
            int endIdxe = begIdxe + trueClusterLen[numTrueClusters-1] - 1;
            int xe = curArrayInfo.getArray()[a-1];            
            if ((trueClusterLen[numTrueClusters-1] < xe) && (begIdxe > e - xe + 1) && (begIdxe - 1 > e - xe) )
            {
                System.out.println("\tMake last few cells true.");
                for (int i = begIdxe - 1; i > e - xe; i--)
                    arrayAnswer[i] = Status.True;
                System.out.println("\t" + printCells(arrayAnswer)); 
            } // end if
        } // end if
    } // end fillUpEnd
    
    // When there is a true cluster just away from the front/end, make first few false.
    // e. g., [3, 4] _ _ _ O O _ _ _ _ --> X X _ O O _ _ _ _ _
    // e. g., [4, 3] _ _ _ _ _ O _ _ _ --> _ _ _ _ _ O _ _ X
    private void falseEnd(ArrayInfo curArrayInfo, Status[] arrayAnswer)
    {
        // Find True cell clusters [b, e], inclusive.
        int[][] trueArrays = findStatusClusters(arrayAnswer, curArrayInfo, Status.True);
        int[] begIdxTrue = trueArrays[0];
        int[] trueClusterLen = trueArrays[1];
        int numTrueClusters = begIdxTrue.length;
        
        if (numTrueClusters > 0)  // when there is a true cluster
        {            
            int b = curArrayInfo.getBeg();
            int e = curArrayInfo.getEnd();
            int a = curArrayInfo.getNum();
            
            int begIdx0 = begIdxTrue[0];
            int endIdx0 = begIdx0 + trueClusterLen[0] - 1;
            int x0 = curArrayInfo.getArray()[0];
            int remainingT0 = x0 - trueClusterLen[0];
            if ((trueClusterLen[0] < x0) && (begIdx0 <= b + x0) && (b < begIdx0 - remainingT0))
            {
                System.out.println("\tMake first few cells false.");
                for (int i = b; i < begIdx0 - remainingT0; i++)
                    arrayAnswer[i] = Status.False;
                System.out.println("\t" + printCells(arrayAnswer)); 
            } // end if
            
            int begIdxe = begIdxTrue[numTrueClusters-1];
            int endIdxe = begIdxe + trueClusterLen[numTrueClusters-1] - 1;
            int xe = curArrayInfo.getArray()[a-1];     
            int remainingTe = xe - trueClusterLen[numTrueClusters-1];
            if ((trueClusterLen[numTrueClusters-1] < xe) && (endIdxe >= e - xe) && (e > endIdxe + remainingTe))
            {
                System.out.println("\tMake last few cells false.");
                for (int i = e; i > endIdxe + remainingTe; i--)
                    arrayAnswer[i] = Status.False;
                System.out.println("\t" + printCells(arrayAnswer)); 
            } // end if
        } // end if
    } // end falseEnd
    
    // General rules on filling up the grids
    // Precondition: s >= 0, a > 0
    private void fillUp(ArrayInfo curArrayInfo, Status[] arrayAnswer)
    {
        int[] curArray = curArrayInfo.getArray();
        int b = curArrayInfo.getBeg();
        int e = curArrayInfo.getEnd();
        int a = curArrayInfo.getNum();
        int ke = curArrayInfo.getLength();
        int sum = curArrayInfo.getSum();
        int toFill, sumX, i0, is;
        
        System.out.println("\tFill up by general rules...");
        int x = curArray[0];  // current number    
        System.out.println("\t\tCurrent number: " + x);
        // Fill up the definite true cells
        // Section 1         
        toFill = 2 * x - (ke - ((sum - x) + (a - 1))); // number of cells to make true
        sumX = 0;  // sum of numbers from x_0 to x_(i-1)
        i0 = b + sumX + x - toFill;
        is = b + sumX + x;
        for (int i = i0; i < is; i++)
            arrayAnswer[i] = Status.True;
        System.out.println("\t" + printCells(arrayAnswer)); 
        
        if (a > 1)  // when there are more than one number
        {
            if (a > 2)
            {                    
                for (int s = 1; s < a-1; s++) // loop through numbers in the array (sections)
                {   // Section s                   
                    sumX += x;
                    x = curArray[s];    // current number   
                    toFill = 2 * x - (ke - ((sum - x) + (a - 1)));                     
                    i0 = b + sumX + s + x - toFill;
                    is = b + sumX + s + x;
                    System.out.println("\t\tCurrent number: " + x);
                    for (int i = i0; i < is; i++)
                        arrayAnswer[i] = Status.True;                      
                    System.out.println("\t" + printCells(arrayAnswer));                     
                } // end for
            } // end if
            // Last section
            sumX += x;
            x = curArray[a-1];  
            toFill = 2 * x - (ke - ((sum - x) + (a - 1)));                     
            i0 = b + sumX + (a-1) + x - toFill;
            is = b + sumX + (a-1) + x;
            System.out.println("\t\tCurrent number: " + x);
            for (int i = i0; i < is; i++)
                arrayAnswer[i] = Status.True;  
            System.out.println("\t" + printCells(arrayAnswer)); 
        } // end if        
    }// end fillUp
    
    // General rules on filling up the grids
    private int[] getpq(ArrayInfo curArrayInfo, int s)
    {
        int[] curArray = curArrayInfo.getArray();
        int b = curArrayInfo.getBeg();
        int e = curArrayInfo.getEnd();
        int a = curArrayInfo.getNum();
        int sum = curArrayInfo.getSum();        
        
        int x = curArray[s];  // current number            
        int sumToX = 0;  // sum of numbers from x_0 to x_(s-1)        
        for (int i = 0; i < s; i++) 
            sumToX += curArray[i]; 
        int sumFromX = sum - sumToX - x; // sum of numbers from x_(s+1) to x_(a-1)
        int p = b + sumToX + s - 1;
        int q = e - (sumFromX + (a - (s + 1))) + 1;              
        
        int[] pq = {p, q};
        return pq;
    }// end getpq
} // end NonogramSolution_v1_2
