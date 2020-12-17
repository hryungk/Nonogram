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
    private Status[][] answer;          // Answer that this solver produces
    private final int m, n;             // size of row and column, respectively
    private enum RowCol {Row, Column}   // State whether row or column    
    private ArrayInfo[] row_arrays;     // An array of array information objects for row arrays
    private ArrayInfo[] col_arrays;     // An array of array information objects for column arrays
    
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
        
        
        answer = new Status[m][n];
        for (int i = 0; i < m; i++) // Initialize answer as Empty
            for (int j = 0; j < n; j++)
                answer[i][j] = Status.Empty;        
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
                    Status[] temp = findArraySolution(RowCol.Row,i, m);
                    System.arraycopy(temp, 0, answer[i], 0, n); 
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
                    Status[] temp = findArraySolution(RowCol.Column, j, n);
                    for (int i = 0; i < n; i++)
                    {
                        answer[i][j] = temp[i]; 
                        noEmpty = noEmpty && (answer[i][j] != Status.Empty);
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
                result = result && (answer[i][j] == SOLUTION[i][j]);
        return result;
    } // end isCorrect
    
    /** Construct a string that shows the answer that this solver produces.
     * @return A String containing the answer to the problem.
     */
    public String toString()
    {
        String result = "Answer to " + m + " x " + n + " Nonogram Puzzle:\n";
        for (int i = 0; i < m; i++)            
            result += printCells(answer[i]) + "\n";                    
        return result;
    } // end toString   
    
    /** Tries to solve the given array.
     * @param rowcol  Indicator whether a row or a column is being investigated
     * @param idx The index of row/column in the 2D array
     * @param k The length of the row or column
     * @return The updated status of the given array
     */
    private Status[] findArraySolution(RowCol rowcol, int idx, int k)
    {   
        ArrayInfo curArrayInfo;                       
        Status[] arrayAnswer = new Status[k];    // The answer for the curArray          
        if (rowcol == RowCol.Row)           // When inspecting a row
        {
            curArrayInfo = row_arrays[idx];
            arrayAnswer = answer[idx];            
        }
        else // (rowcol == RowCol.Column)   // When inspecting a column
        {
            curArrayInfo = col_arrays[idx];            
            for (int i = 0; i < k; i++)
                arrayAnswer[i] = answer[i][idx];
        }        
        int b = curArrayInfo.getBeg();  // effective beginning index
        int e = curArrayInfo.getEnd();  // effective end index
        // Push beginning and end index inward if there are consecutive Falses.
        int[] be = updateEnds(b, e,curArrayInfo, arrayAnswer);            
        b = be[0]; 
        e = be[1];
        int[] oldbe = be;
        
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
            /*  When the sum is one short in the beginning, fill up in between cells. */
            // Only for the first time            
            if (sum + (a - 1) == ke - 1 && arrayAnswer[b+1] == Status.Empty)    
            {
                System.out.println("\tsum of numbers and spaces == one short of effective row/column length");
                fillUpWhenOneShort(curArrayInfo, arrayAnswer);
                //System.out.println("\t" + printCells(arrayAnswer));
            } // end if
            
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
            sum = curArrayInfo.getSum();        // sum of numbers in curArray            
            numT = numOfStatus(b, e, arrayAnswer, Status.True);  // The number of True cells    
            numE = numOfStatus(b, e, arrayAnswer, Status.Empty); // The number of Empty cells                         
            if (!Arrays.equals(oldbe, be))    // If anything is updated, print out the array.
                System.out.println("\tAfter removing ends:\n\tb = " + b + ", e = " + e + ", ke = " + ke + ", a = " + a);
            
            if (a == 1) // When there is only one number in the array
            {
                System.out.println("\tThere is only one number in the array.");
                // Fill up between the first and the last true
                int firstTrue = getFirstStatus(b-1, e, arrayAnswer, Status.True);
                int lastTrue = getLastStatus(firstTrue, e+1, arrayAnswer, Status.True);                
                if (b <= firstTrue && firstTrue <= e && firstTrue != lastTrue)    
                {
                    System.out.println("\t\tFill up between first true: " + firstTrue + " and last true: " + lastTrue);
                    for (int i = firstTrue; i <= lastTrue; i++)
                        if (arrayAnswer[i] == Status.Empty)
                            arrayAnswer[i] = Status.True;
                    System.out.println("\t" + printCells(arrayAnswer)); 
                }
                /* Check whether this array is solved. */
                solved = checkAllTrue(curArrayInfo, arrayAnswer, solved);
            } // end if
            
            /* Investigate by sections according to the numbers in the array. */
            if (!solved)
            {
                int p;          // The last cell index in the previous false cluster
                int q = b-1;     // The first cell index in the next false cluster
                int s = 0;
                while (s < a && !solved) // loop through numbers in the array (sections)
                //for (int s = 0; s < a; s++) // loop through numbers in the array (sections)
                {       
                    int x = curArray[s];    // current number
                    System.out.println("\tCurrent number: " + x);

                    // Determine current section (p, q), exclusive.  
                    int[] pq = getpq(arrayAnswer,curArrayInfo,s,q);
                    p = pq[0];
                    q = pq[1];
                    if (sum + (a - 1) == ke - 1)    // when one short
                    {
                        int[][] pqList = getpqOneShort(curArrayInfo);
                        int pOneShort = pqList[0][s];
                        int qOneShort = pqList[1][s];
                        
                        p = Math.max(p, pOneShort);
                        q = Math.min(q, qOneShort);                        
                    } // end if
                    System.out.println("\tp = " + p + ", q = " + q); 

                    // Procede only when the index is within boundary
                    if (p < e+1 && q <= e+1) 
                    {                    
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
                        int lenTrue = curTrue[1];    // Length of current True cluster
                        if (lenTrue != 0)
                            System.out.println("\tCurrent True Cluster: from " + locTrue + " to " + (locTrue + lenTrue - 1));
                        
                        // If current true cluster is bigger than x, skip to the next number in the array
                        if (lenTrue > x)
                        {
                            System.out.println("\tCurrent true cluster > x; moving to the next number in the array...");
                            s++;
                            x = curArray[s];                            
                            System.out.println("\tCurrent number: " + x);
                        } // end if
                        
                        // Find a true cluster whose length is the same as x and in a later section.
                        boolean foundLaterSameTrue = false;
                        for (int i = 0; i < trueClusterLen.length; i++)
                            foundLaterSameTrue = foundLaterSameTrue || (begIdxTrue[i] > q && trueClusterLen[i] == x);

                        // Find an empty cluster whose length is greather than or equal to x and in a previous section.
                        boolean foundEarlierLargerEmpty = false;
                        for (int i = 0; i < emptyClusterLen.length; i++)
                            foundEarlierLargerEmpty = foundEarlierLargerEmpty || (begIdxEmpty[i] < p && emptyClusterLen[i] >= x);

                        // Find an empty cluster whose length is the same as x and in the current section.
                        // Or empty cluster + true cluster == x
                        boolean foundThisSameEmpty = false;
                        int ie = 0;
                        while (!foundThisSameEmpty && ie < emptyClusterLen.length)
                        {
                            foundThisSameEmpty = (p < begIdxEmpty[ie] && begIdxEmpty[ie] < q  && emptyClusterLen[ie] == x);
                            foundThisSameEmpty = foundThisSameEmpty || (lenTrue + emptyClusterLen[ie] == x);
                            if (!foundThisSameEmpty)
                                ie++;
                        } // end while

                        // Find an empty cluster whose length is greather than or equal to x and in a next section.
                        // Only if the empty cluster doesn't follow a true cluster whose length is larger than x
                        // (because if that's the case, the later empty cluster doesn't belong to x)
                        boolean foundLaterLargerEmpty = false;
                        for (int i = 0; i < emptyClusterLen.length; i++)
                        {
                            int curBegIdxEmpty = begIdxEmpty[i];
                            int curEmptyClusterLen = emptyClusterLen[i];
                            foundLaterLargerEmpty = foundLaterLargerEmpty || (curBegIdxEmpty > q && curEmptyClusterLen >= x);
                            // Check whether there is a true cluster coming before the empty cluster whose longer than x
                            // (because then x doesn't belong to this empty cluster. e.g., _ _ _ X X O O O _ _ and current number is 2)
                            int[] thisTrue = getCurrentTrueCluster(q, curBegIdxEmpty, begIdxTrue, trueClusterLen);
                            int thisLocTrue = thisTrue[0];   // Beginning index of True cluster (first one if there are multiple, -1 if none)
                            int thisLenTrue = thisTrue[1];    // Length of True cluster
                            if (thisLocTrue != -1)  // there exist true cluster between q and current empty cluster
                                foundLaterLargerEmpty = foundLaterLargerEmpty && (thisLenTrue <= x);
                        } // end for

                        int kp = q - p - 1; // The number of empty cells between p and q   
                        int i0 =  p + 1 + kp - x;   // Beginning index of true cells                    

                        /* Fill up the middle (2x-k') cells. */
                        // When there are more than one number left in the array but it is alreay filled in the middle of the row/column, 
                        // (so that the code still doesn't know it is filled), and there are multiple locations for the last number to be,
                        // we should skip this. Example is _XXOXX_XOO and array is [1,1]
                        if (!solved && (x > kp/2) && (kp >= x) && (i0 >= b) &&
                            !(sum - numT == x && a >= 2) && !(kp < numE && a == 1) &&
                            !foundLaterSameTrue && !foundEarlierLargerEmpty && 
                            !foundLaterLargerEmpty || (q == e+1 && foundThisSameEmpty && !foundEarlierLargerEmpty))// && (s < a-1 && x != curArray[s+1]))
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
                        if (!solved && x > kp && !(s > 0 && kp >= curArray[s-1])) 
                        {
                            System.out.println("\tMake this section false");
                            for (int i = p + 1; i < q; i++)
                                if (arrayAnswer[i] == Status.Empty)
                                    arrayAnswer[i] = Status.False;         
                            System.out.println("\t" + printCells(arrayAnswer)); 
                        } // end if     

                        /* Make remaining empty cells False when x is the last. */
                        // When x is the last number in the array and there are still 
                        // empty clusters whose length is less than x in later sections,
                        // make them false.
                        if (!solved && (s == a-1) && (q < e)) 
                        {
                            // Check whether there is a true cluster in this section
                            boolean foundThisTrue = (locTrue != -1);                        
                            // Find a true cluster whose length is the same as x and in a later section.
                            boolean foundLaterTrue = false;
                            for (int i = 0; i < trueClusterLen.length; i++)
                                foundLaterTrue = foundLaterTrue || (begIdxTrue[i] > q);                        
                            // Find an empty cluster whose length is smaller than x and in the next section.
                            boolean foundLaterSmallerEmpty = false;
                            for (int i = 0; i < emptyClusterLen.length; i++)
                            {
                                int curBegIdxEmpty = begIdxEmpty[i];
                                int curEmptyClusterLen = emptyClusterLen[i];
                                foundLaterSmallerEmpty = (curBegIdxEmpty > q && curEmptyClusterLen < x);
                                if ((curBegIdxEmpty > q) && !foundLaterTrue && (lenTrue <= x) &&
                                    (foundLaterSmallerEmpty || (foundThisTrue && s > 0 && curArray[s-1] != x))) // when true cluster is found, we don't know whether x belongs to this cluster if x is the same as previous number
                                {
                                    System.out.println("\tMake later sections false");
                                    for (int ii = curBegIdxEmpty; ii < curBegIdxEmpty + curEmptyClusterLen; ii++)
                                        if (arrayAnswer[ii] == Status.Empty)
                                            arrayAnswer[ii] = Status.False;     
                                    System.out.println("\t" + printCells(arrayAnswer)); 
                                } // end if                            
                            } // end for                        
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
                            curArrayInfo = makeFalseAround(x, kp, p, q, arrayAnswer, 
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
                        if (!solved && (p == -1 || arrayAnswer[p] == Status.False) && arrayAnswer[p+1] == Status.True && (x != lenTrue)) 
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
                        } // end if   
                        // When q is False and q-1 is True
                        // e.g., [2, 3] X _ _ _ _ X _ _ _ O
                        // e.g., [2, 2] X _ _ _ _ X _ _ _ O
                        // e.g., [1, 2] X _ _ _ _ X _ _ _ O
                        // e.g., [2, 1] X _ _ _ _ X _ _ _ O
                        if (!solved && (q == k || arrayAnswer[q] == Status.False) && arrayAnswer[q-1] == Status.True && (x != lenTrue))
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
                            System.out.println("\tFor section [" + p + ", " + q +"]:");
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
    
    private int[] getpq(Status[] arrayAnswer, ArrayInfo curArrayInfo, int s, int q)
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
    } // end getpq
    
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
        int b = curArrayInfo.getBeg();
        int e = curArrayInfo.getEnd();
        int a = curArrayInfo.getNum();
        int firstTrue = getFirstStatus(p, e, arrayAnswer, Status.True);
        
        int numTCur = 0;    // Number of True in the current section
        int lastTrue = firstTrue;
        for (int i = firstTrue; i < q; i++)
        {
            if (arrayAnswer[i] == Status.True)
            {
                numTCur++;
                lastTrue = i;
            } // end if
        } // end for
        int remainingT = x - numTCur;   // Remaining number of true
         // System.out.println("\tfirstTrue = " + firstTrue +
         //       ", numTCur = " + numTCur + ", lastTrue = " + 
         //       lastTrue + ", remainingT = " + remainingT);
        //System.out.println("\tnumT = " + numT + ", sum = " + sum);
        if (firstTrue < e+1 && firstTrue <= q && q <= e+1)
        {                        
            if (remainingT == 0 && numT == sum)    // All true are found
            {
                System.out.println("\tSolution is found for the array; Remaining number of True == 0");
                if (arrayAnswer[firstTrue-1] == Status.Empty)
                    arrayAnswer[firstTrue-1] = Status.False;
                solved = true;
            }
            else    // Make cells in this section that are farther than remainingT false
            {                               
                if (a < 2)  // when there's only one number in the array
                {
                    System.out.println("\tMake far cells false.");
                    //System.out.println("Code proceeded to remainingT != 0");    
                    if (p == b-1 || arrayAnswer[p] == Status.False)
                        for (int i = p+1; i < firstTrue - remainingT;i++)
                            arrayAnswer[i] = Status.False;
                    
                    if (q == e+1 || arrayAnswer[q] == Status.False)                                
                        for (int i = lastTrue + remainingT + 1; i < q; i++)
                            arrayAnswer[i] = Status.False;                                    
                }
                else   // when there are more than 1 numbers in the array
                {                                
                    //if (firstTrue <= x+b && s == 0) // belongs to the first number                                    
                } // end if
                System.out.println("\t" + printCells(arrayAnswer)); 
            } // end if
        } // end if
        return solved;
    } // end makeFarCellsFalse
    
    // If there is a true cluster whose length is maximum of the numbers, make false around it
    // e.g., [1,2] _ _ _ _ _ O O _ _ _ --> _ _ _ _ X O O X _ _ 
    private ArrayInfo makeFalseAround(int x, int kp, int p, int q,
                                Status[] arrayAnswer, ArrayInfo curArrayInfo)
    {        
        int a = curArrayInfo.getNum();
        int b = curArrayInfo.getBeg();
        int e = curArrayInfo.getEnd();
        int[] curArray = curArrayInfo.getArray();        
        int firstTrue = getFirstStatus(p, e, arrayAnswer, Status.True);
        
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
        
        if (firstTrue <= e)  // when there is true cell
        {
            int it = firstTrue; // True cell iterator             
            // Find a true cluster 
            while (it < e && arrayAnswer[it + 1] == Status.True)
                it++;
            int lenT = it - firstTrue + 1;  // the length of true cluster
            // When lenT is the largest number in the array or x == numT
            // Or when the number of true clusters == a && currentTrueCluster == x
            if (curArrayInfo.isMax(lenT) || (trueClusterLen.length == a && lenCurTrue == x)
                    || (locCurTrue > sum && lenCurTrue == x && s == a-1))  
            {
                System.out.println("\tMake false around true cells.");
                if (firstTrue-1 >= b)   // Make false right before the true cluster
                    arrayAnswer[firstTrue-1] = Status.False;
                if (it+1 <= e)  // Make false right after the true cluster
                    arrayAnswer[it+1] = Status.False;
                // when first number in the array, make false to the beginning
                if (curArrayInfo.indexOf(lenT) == 0 && (kp < x))
                {
                    System.out.println("\t\tFisrt number in the array, make false to the beginning");
                    int iFalse = firstTrue - 2;    // False iterator
                    while (iFalse >= b)
                    {
                        arrayAnswer[iFalse] = Status.False;
                        iFalse--;
                    } // end while                                
                    curArrayInfo.setBeg(firstTrue);    // Update beginning to the firstTrue
                    finishFirstNumber(curArray, arrayAnswer, curArrayInfo);
                } // end if
                // when last number in the array, make false to the end
                else if (curArrayInfo.indexOf(lenT) == a-1)     
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
        
        int firstTrue, lastTrue, lenT;
        int i = b;
        while (i <= e)
        {
            firstTrue = getFirstStatus(i - 1, e, arrayAnswer, stat);
            lastTrue = getLastStatusCluster(firstTrue, e, arrayAnswer, stat);            
            lenT = lastTrue - firstTrue + 1;
            if (lenT > 0)
            {
                begStatusIdx_temp.add(firstTrue);
                lenStatus_temp.add(lenT);
            } // end if
            i = lastTrue + 1;
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
        int lenTrue = 0;    // Length of True cluster
        int i = 0;
        boolean foundTrueCluster = false;
        while (i < trueClusterLen.length && !foundTrueCluster)                    
        {
            foundTrueCluster = p < begIdxTrue[i] && begIdxTrue[i] < q;
            if (foundTrueCluster)
            {
                locTrue = begIdxTrue[i];
                lenTrue = trueClusterLen[i];
            } 
            else
                i++;
        } // end for
        int[] result = {locTrue, lenTrue};
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
    
    public class ArrayInfo
    {
        private int[] thisArray; // Array
        private boolean arraySolved; // indicates whether this array is solved
        private int gridLen;  // length of row/column
        private int num;    // The number of numbers in the array (array size)
        private int begIdx, endIdx;   // beginning and end index
        private int arraySum;    // Sum of all the numbers in the array
        
        public ArrayInfo(int[] array, int size)
        {
            thisArray = array;
            arraySolved = false;
            gridLen = size;
            num = array.length;
            begIdx = 0;
            endIdx = size-1;
            setSum();   // Calculate summation of numbers            
        }
        
        public int[] getArray()
        {
            return thisArray;
        }
        
        private void setArray(int[] newArray)
        {
            thisArray = newArray;
        }
        
        public void removeFirstInArray()
        {            
            int[] newArray = new int[num-1]; // One size smaller            
            for (int i = 0; i < num-1; i++)
                newArray[i] = thisArray[i+1];
            int newBeg = begIdx + thisArray[0] + 1;            
            setBeg(newBeg);            
            setArray(newArray); // Update the array            
            setNum(newArray.length);  // Reduce the array size  
            setSum();

            System.out.println("\t\tb = " + getBeg());
        }
        public void removeLastInArray()
        {            
            int[] newArray = new int[num-1]; // One size smaller            
            System.arraycopy(thisArray, 0, newArray, 0, num-1);
            //for (int i = 0; i < num-1; i++)
            //    newArray[i] = thisArray[i];
            int newEnd = endIdx - (thisArray[num-1]+1);
            //if (newEnd >= getBeg())
            setEnd(newEnd);            
            setArray(newArray); // Update the array            
            setNum(newArray.length);  // Reduce the array size  
            setSum();

            System.out.println("\t\te = " + getEnd());
        }
        
        /** Checks whether the array contains a number.
         * @param aNum  An integer of interest to be found
         * @return true if aNum is found in thisArray
         */
        public boolean contains(int aNum)
        {
            boolean found = false;
            int i = 0;
            while (!found && i < num)
            {
                found = (thisArray[i] == aNum);            
                i++;
            }
            return found;
        } // end contains
        
        /** Returns the index of a number
         * @param aNum  An integer of interest to be found
         * @return An integer containing the index of aNum;
         *         -1 if not found
         */
        public int indexOf(int aNum)
        {
            int result = -1;
            boolean found = false;
            int i = 0;
            while (!found && i < num)
            {
                found = (thisArray[i] == aNum);  
                if (found)
                    result = i;
                else
                    i++;
            } // end while  
            if (i == num)
                result = -1;
            return result;
        } // end indexOf
        
        /** Checks if a number is the largest in the array
         * @param aNum  An integer of interest
         * @return true if aNum is the largest in the array
         */
        public boolean isMax(int aNum)
        {            
            // Find largest number
            int curMax = thisArray[0];
            for (int i = 0; i < num; i++)
                curMax = Math.max(thisArray[i], curMax);
            
            return (curMax == aNum);
        } // end isMax
        
        /** Return the summation of array number from beg to end.
         * @param beg   An integer containing the beginning index
         * @param end   An integer containing the beginning index
         * @return A summation of numbers in the array from beg to end
         */
        public int sumArray(int beg, int end)
        {
            assert beg >= 0 && end < num;
            int sum = 0;
            for (int i = beg; i <= end; i++)
                sum += thisArray[i];
            return sum;            
        } // end sumArray
        /** Return the summation of array number from beg to the last number.
         * @param beg   An integer containing the beginning index         
         * @return A summation of numbers in the array from beg to end
         */
        public int sumArrayFrom(int beg)
        {
            return sumArray(beg, num-1);
        } // end sumArray
        
        /** Checks whether the array is solved.
         * @return A boolean true if the array is solved. 
         */
        public boolean isSolved()
        {
            return arraySolved;
        }
        
        public void setSolved(boolean TF)
        {
            arraySolved = TF;
        }
        
        public int getLength()
        {
            return gridLen;
        }
        
        private void setLength(int newLen)
        {
            gridLen = newLen;
        }
        
        public int getNum()
        {
            return num;
        }        
        
        private void setNum(int newNum)
        {
            num = newNum;
        }
        
        public int getBeg()
        {
            return begIdx;
        }
        public void setBeg(int newB)
        {
            begIdx = newB;
            setLength(getEnd() - newB + 1); // Update the effective length of grid
        }
        
        public int getEnd()
        {
            return endIdx;
        }
        public void setEnd(int newE)
        {
            endIdx = newE;
            setLength(newE - getBeg() + 1); // Update the effective length of grid            
        }
        
        public int getSum()
        {
            return arraySum;
        }
        private void setSum()
        {
            arraySum = sumArrayFrom(0);
            //for (int i = 0; i < num; i++)                    
            //    arraySum += thisArray[i];         
        }
    } // end ArrayInfo
} // end NonogramSolution_v1_0
