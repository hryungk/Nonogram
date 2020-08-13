/*
 * A class that solves a Nonogram puzzle.
 */

/**
 *
 * @author Hyunryung Kim    hryungk@gmail.com
 */
import java.util.Arrays;
import java.util.ArrayList;
public class NonogramSolution_v0_5 
{   
    private final int[][] PROB_ROW;          // Row arrays of the puzzle
    private final int[][] PROB_COL;          // Column arrays of the puzzle   
    private final Status[][] SOLUTION;          // Answer to the puzzle
    private Status[][] answer;
    private final int m, n;             // size of row and column, respectively
    private enum RowCol {Row, Column}   // State whether row or column    
    private ArrayInfo[] row_arrays; // An array of array information objects for row arrays
    private ArrayInfo[] col_arrays; // An array of array information objects for column arrays
    public NonogramSolution_v0_5(NonogramProblem newProblem)
    {        
        PROB_ROW = newProblem.getRowArray();
        PROB_COL = newProblem.getColumnArray();
        
        m = PROB_ROW.length;
        n = PROB_COL.length;   
        
        row_arrays = new ArrayInfo[m];
        for (int i = 0; i < m; i++)
            row_arrays[i] = new ArrayInfo(PROB_ROW[i], m);
        col_arrays = new ArrayInfo[n];
        for (int j = 0; j < n; j++)
            col_arrays[j] = new ArrayInfo(PROB_COL[j], n);
        
        SOLUTION = newProblem.getSolution();
        answer = new Status[m][n];
        for (int i = 0; i < m; i++) // Initialize answer as Empty
            for (int j = 0; j < n; j++)
                answer[i][j] = Status.Empty;        
    } // end constructor    
    
    public void solve()
    {
        boolean noEmpty = false;    // true if there is no empty cells
        int loopCount = 0;
        while (!noEmpty)    // Keep solving arrays until there is no empty cell
        {
            noEmpty = true;     // initialize noEmpty every loop
            System.out.println("=======================================================\n" +
                    "\t\t\tLoop "+ (loopCount+1) +
                               "\n=======================================================");
            System.out.println("------------------------ Row - " + (loopCount+1) +
                               " ------------------------");            
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
                {                    
                    System.out.println("Array "
                                       + Arrays.toString(PROB_ROW[i]) 
                                       + " is solved.");
                }
            } // end for
            System.out.println("------------------------ Column - " + (loopCount+1) +
                               " ------------------------");
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
                {                    
                    System.out.println("Array "
                                       + Arrays.toString(PROB_COL[j]) 
                                       + " is solved.");
                } // end if
            } // end for
            loopCount++;            
        } // end while
    } // end solve
    
    public void testSolve()
    {        
        Status[] temp = findArraySolution(RowCol.Row, 7, m);
        //Status[] temp = findArraySolution(RowCol.Column, 3, n);
        System.out.println(printCells(temp));
    } // end testSolve
    
    
    public boolean isCorrect()
    {        
        boolean result = true;
        for (int i = 0; i < m; i ++)
            for (int j = 0; j < n; j++)
                result = result && (answer[i][j] == SOLUTION[i][j]);
        return result;
    } // end isCorrect
    
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
        // Update beginning and end index
        int[] be = updateEnds(b, e,curArrayInfo, arrayAnswer);            
        b = be[0]; 
        e = be[1];
        
        int[] curArray = curArrayInfo.getArray(); // Row/column array        
        int a = curArrayInfo.getNum();            // The number of numbers in curArray
        int ke = curArrayInfo.getLength();       // Effective length of grid
        System.out.println("\tb = " + b + ", e = " + e + ", ke = " + ke);
        
        boolean solved = false;             // true if curArray is solved        
        int sum = curArrayInfo.getSum();                        // sum of numbers in curArray        
        int numT = numOfStatus(b, e, arrayAnswer, Status.True);     // The number of True cells    
        int numE = numOfStatus(b, e, arrayAnswer, Status.Empty);    // The number of Empty cells
        int numF = numOfStatus(b, e, arrayAnswer, Status.False);    // The number of False cells
        
        // Start investigation!
        if (sum + (a - 1) == ke) // initial condition automatically solves
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
        else if (sum == numE && numF + numE == ke)
        {
            System.out.println("\tSolution is found for the array; sum of numbers == number of Empty");
                       
            // Make all empty cells True
            for (int i = b; i < e+1; i++) 
                if (arrayAnswer[i] == Status.Empty)
                    arrayAnswer[i] = Status.True;
            solved = true;
            System.out.println("\t" + printCells(arrayAnswer));
        }
        else if (sum + (a - 1) == ke - 1)    // When the sum is one short in the beginning
        {
            System.out.println("\tsum of numbers and spaces == one short of effective row/column length");            
            
            int x = curArray[0];  // current number    
            System.out.println("\tCurrent number: " + x);
            // Fill up the definite true cells
            // Section 1: from 0 to x            
            for (int i = b+1; i < b+x; i++)
                arrayAnswer[i] = Status.True;
            int count = b+x + 1;  // count of total cells investigated
            int y;      // current number   
            if (a > 1)  // when there are more than one number
            {
                if (a > 2)
                {                    
                    for (int s = 1; s < a-1; s++) // loop through numbers in the array (sections)
                    {   // Section s: from (x+1) to (x+1+y) 
                        x = curArray[s-1];      // previous number
                        y = curArray[s];    // current number         
                        System.out.println("\tCurrent number: " + y);
                        for (int i = b+x+2; i < b+x+1+y; i++)
                            arrayAnswer[i] = Status.True;  
                        count += y + 1;
                    } // end for
                } // end if
                // Last section: from count to (ke-1)
                y = curArray[a-1];
                System.out.println("\tCurrent number: " + y);
                for (int i = count+1; i < e+1-1; i++)
                    arrayAnswer[i] = Status.True;                
            } // end if
            
            // Check the first and the last cell
            while (b <= e && arrayAnswer[b] == Status.True)  // first cell is filled    
            {
                removeBeg(curArray, arrayAnswer, curArrayInfo);  
                b = curArrayInfo.getBeg();
                curArray = curArrayInfo.getArray();
            } // end while
            while (e >= b && arrayAnswer[e+1-1] == Status.True)    // last cell is filled
            {
                removeEnd(curArray, arrayAnswer, curArrayInfo);
                e = curArrayInfo.getEnd();
                curArray = curArrayInfo.getArray();
            } // end while
            // Update parameters
            a = curArrayInfo.getNum();
            sum = curArrayInfo.getSum();
            numT = numOfStatus(b, e, arrayAnswer, Status.True);
            numE = numOfStatus(b, e, arrayAnswer, Status.Empty);
            //numF = numOfStatus(b, e, arrayAnswer, Status.False);
            curArray = curArrayInfo.getArray(); // Row/column array
            
            // Fill inside the section
            int p;          // The last cell index in the previous false cluster
            int q = b-1;     // The first cell index in the next non-empty cluster
            for (int s = 0; s < a; s++) // loop through numbers in the array (sections)
            {       
                x = curArray[s];    // current number
                System.out.println("\tCurrent number: " + x);
                
                int[] pq = getpq(arrayAnswer,curArrayInfo,s,q);
                p = pq[0];
                q = pq[1];
                
                //System.out.println("p = " + p + ", q = " + q);
                if (p < e+1 && q <= e+1) // procede only when the index is within boundary
                {                    
                    int kp = q - p - 1; // The number of non-false cells between p and q   
                    int i0 =  p + 1 + kp - x;   // Beginning index of true cells
                    
                    // When there are more than one number left in the array but it is alreay filled in the middle of the row/column, 
                    // (so that the code still doesn't know it is filled), and there are multiple locations for the last number to be,
                    // we should skip this. Example is _XXOXX_XOO and array is [1,1]
                    // Last condition: Fill up only when one of p or q is false so that we know the section is deteremined and to be filled.
                    if ((x > kp/2) && (i0 >= b) && !(sum - numT == x && a >= 2) && !(a == 1 && numE > x) &&
                            ((p < -1 && arrayAnswer[p] == Status.False) || (q <= e && arrayAnswer[q] == Status.False)))
                    {
                        for (int i = i0; i <= p + x; i++)
                            if (arrayAnswer[i] == Status.Empty)
                                arrayAnswer[i] = Status.True;
                        
                    } // end if 
                    
                    // If there is a true cluster whose length is maximum of the numbers, make false around it
                    // e.g., [1,2] _ _ _ _ _ O O _ _ _ --> _ _ _ _ X O O X _ _ 
                    curArrayInfo = makeFalseAround(x, kp, p, arrayAnswer, 
                                                   curArrayInfo);
                    // Update parameters
                    a = curArrayInfo.getNum();
                    b = curArrayInfo.getBeg();
                    e = curArrayInfo.getEnd();
                    sum = curArrayInfo.getSum();
                    numT = numOfStatus(b, e, arrayAnswer, Status.True);
                    numE = numOfStatus(b, e, arrayAnswer, Status.Empty);
                    //numF = numOfStatus(b, e, arrayAnswer, Status.False);
                    curArray = curArrayInfo.getArray(); // Row/column array
                } // end if
            } // end for
            
            // Check the first and the last cell
            while (b <= e && arrayAnswer[b] == Status.True)  // first cell is filled    
            {
                removeBeg(curArray, arrayAnswer, curArrayInfo);  
                b = curArrayInfo.getBeg();
                curArray = curArrayInfo.getArray();
            } // end while
            while (e >= b && arrayAnswer[e+1-1] == Status.True)    // last cell is filled
            {
                removeEnd(curArray, arrayAnswer, curArrayInfo);
                e = curArrayInfo.getEnd();
                curArray = curArrayInfo.getArray();
            } // end while
            
            solved = checkAllTrue(curArrayInfo, arrayAnswer, solved);
            System.out.println("\t" + printCells(arrayAnswer));
        }
        else
        {   
            /*  Check the first and the last cell */
            // When the first cell is filled 
            while (b <= e && arrayAnswer[b] == Status.True)   
            {
                removeBeg(curArray, arrayAnswer, curArrayInfo);  
                b = curArrayInfo.getBeg();
                curArray = curArrayInfo.getArray();
            } // end while
            be = updateEnds(b, e, curArrayInfo, arrayAnswer);
            b = be[0];                        
            // When the last cell is filled
            while (e >= b && arrayAnswer[e+1-1] == Status.True)   
            {
                removeEnd(curArray, arrayAnswer, curArrayInfo);
                e = curArrayInfo.getEnd();
                curArray = curArrayInfo.getArray();
            } // end while
            be = updateEnds(b, e, curArrayInfo, arrayAnswer);            
            e = be[1];
            solved = checkAllTrue(curArrayInfo, arrayAnswer, solved);
            System.out.println("\t" + printCells(arrayAnswer)); 
            
            // Update parameters
            curArray = curArrayInfo.getArray(); // Row/column array        
            a = curArrayInfo.getNum();            // The number of numbers in curArray
            ke = curArrayInfo.getLength();       // Effective length of grid
            sum = curArrayInfo.getSum(); // sum of numbers in curArray            
            numT = numOfStatus(b, e, arrayAnswer, Status.True);  // The number of True cells    
            numE = numOfStatus(b, e, arrayAnswer, Status.Empty); // The number of Empty cells 
            //numF = numOfStatus(b, e, arrayAnswer, Status.False); // The number of False cells 
            System.out.println("\tb = " + b + ", e = " + e + ", ke = " + ke);
            
            
            int p;          // The last cell index in the previous false cluster
            int q = b-1;     // The first cell index in the next false cluster
            for (int s = 0; s < a; s++) // loop through numbers in the array (sections)
            {       
                int x = curArray[s];    // current number
                System.out.println("\tCurrent number: " + x);                
                
                // Find True cell clusters [b, e], inclusive.
                int[][] trueArrays = findStatusClusters(arrayAnswer, curArrayInfo, Status.True);
                int[] begIdxTrue = trueArrays[0];
                int[] trueClusterLen = trueArrays[1];
                
                // Find Empty cell clusters [b, e], inclusive.
                int[][] emptyArrays = findStatusClusters(arrayAnswer, curArrayInfo, Status.Empty);
                int[] begIdxEmpty = emptyArrays[0];
                int[] emptyClusterLen = emptyArrays[1];
                
                // Determine current section (p, q), exclusive.
                int[] pq = getpq(arrayAnswer,curArrayInfo,s,q);
                p = pq[0];
                q = pq[1];
                
                // Procede only when the index is within boundary
                if (p < e+1 && q <= e+1) 
                {
                    // Check whether there is a true cluster that belongs this section.
                    int[] curTrue = getCurrentTrueCluster(p, q, begIdxTrue, trueClusterLen);
                    int locTrue = curTrue[0];   // Beginning index of True cluster (first one if there are multiple, -1 if none)
                    int lenTrue = curTrue[1];    // Length of True cluster
                    if (lenTrue != 0)
                        System.out.println("\tCurrent True Cluster: from " + locTrue + " to " + (locTrue + lenTrue - 1));
                    
                    // Find a true cluster whose length is the same as x and in a later section.
                    boolean foundLaterSameTrue = false;
                    for (int i = 0; i < trueClusterLen.length; i++)
                        foundLaterSameTrue = foundLaterSameTrue || (begIdxTrue[i] > q && trueClusterLen[i] == x);
                    
                    // Find an empty cluster whose length is greather than or equal to x and in a previous section.
                    boolean foundEarlierLargerEmpty = false;
                    for (int i = 0; i < emptyClusterLen.length; i++)
                        foundEarlierLargerEmpty = foundEarlierLargerEmpty || (begIdxEmpty[i] <= p && emptyClusterLen[i] >= x);
                    
                    // Find an empty cluster whose length is greather than or equal to x and in a next section.
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
                    }
                    
                    int kp = q - p - 1; // The number of empty cells between p and q   
                    int i0 =  p + 1 + kp - x;   // Beginning index of true cells                    
                    
                    // When there are more than one number left in the array but it is alreay filled in the middle of the row/column, 
                    // (so that the code still doesn't know it is filled), and there are multiple locations for the last number to be,
                    // we should skip this. Example is _XXOXX_XOO and array is [1,1]
                    if ((x > kp/2) && (kp >= x) && (i0 >= b) 
                            && !(sum - numT == x && a >= 2) && !(kp < numE && a == 1) 
                            &&  !foundLaterSameTrue && !foundEarlierLargerEmpty && !foundLaterLargerEmpty)// && (s < a-1 && x != curArray[s+1]))
                    {
                        // when there is a true cluster whose length is the same as x and not in the current section, don't fill up.
                        System.out.println("\tFill up middle (2x-k') cells");
                        for (int i = i0; i <= p + x; i++)
                            if (arrayAnswer[i] == Status.Empty)
                                arrayAnswer[i] = Status.True;                        
                    } // end if 
                    
                    // When there is less number of empty cells than the current number, make false.
                    // But when there is a later section where x actually belongs, skip this part.
                    if (x > kp && !(s > 0 && kp >= curArray[s-1])) 
                    {
                        System.out.println("\tMake this section false");
                        for (int i = p + 1; i < q; i++)
                            if (arrayAnswer[i] == Status.Empty)
                                arrayAnswer[i] = Status.False;                
                    } // end if     
                    
                    // When x is the last number in the array and there are still 
                    // empty clusters whose length is less than x in later sections,
                    // make them false.
                    if (s == a-1 && q < e) 
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
                            } // end if
                        } // end for
                    } // end if   
                    
                    // Make cells in this section that are farther than remainingT false
                    // e.g., [3, 2] _ _ _ O _ _ X _ _ X --> X _ _ O _ _ X _ _ X
                    solved = makeFarCellsFalse(x, p, q, curArrayInfo, numT, sum, 
                                               solved, arrayAnswer);
                    
                    // If there is a true cluster whose length is maximum of the numbers, make false around it
                    // e.g., [1,2] _ _ _ _ _ O O _ _ _ --> _ _ _ _ X O O X _ _                                       
                    curArrayInfo = makeFalseAround(x, kp, p, arrayAnswer, 
                                                   curArrayInfo);
                    // Update parameters
                    a = curArrayInfo.getNum();
                    b = curArrayInfo.getBeg();
                    e = curArrayInfo.getEnd();
                    curArray = curArrayInfo.getArray(); // Row/column array 
                } // end if 
                
                // Update number of True and Empty before proceeding to the next number
                sum = curArrayInfo.getSum();
                numT = numOfStatus(b, e, arrayAnswer, Status.True);
                numE = numOfStatus(b, e, arrayAnswer, Status.Empty);
                //numF = numOfStatus(b, e, arrayAnswer, Status.False); // The number of False cells 
                
                //System.out.println("\tnumT = " + numT + ", sum = " + sum);
                System.out.println("\t" + printCells(arrayAnswer));
            } // end for
            
            // Check the first and the last cell
            while (b <= e && arrayAnswer[b] == Status.True)  // first cell is filled    
            {
                removeBeg(curArray, arrayAnswer, curArrayInfo);  
                b = curArrayInfo.getBeg();
                curArray = curArrayInfo.getArray();
            } // end while
            while (e >= b && arrayAnswer[e+1-1] == Status.True)    // last cell is filled
            {
                removeEnd(curArray, arrayAnswer, curArrayInfo);
                e = curArrayInfo.getEnd();
                curArray = curArrayInfo.getArray();
            } // end while
            
            solved = checkAllTrue(curArrayInfo, arrayAnswer, solved);
            if (solved)
                System.out.println("\t" + printCells(arrayAnswer));
        } // end if
        // Update beginning and end index
        updateEnds(b, e,curArrayInfo, arrayAnswer);
        
        // Update solved arrays
        curArrayInfo.setSolved(solved);           
        return arrayAnswer;
    } // end findArraySolution
    
    public String toString()
    {
        String result = "Answer to " + m + " x " + n + " Nonogram Puzzle:\n";
        for (int i = 0; i < m; i++)            
            result += printCells(answer[i]) + "\n";                    
        return result;
    } // end toString    
    
    
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
    
    private void removeBeg(int[] curArray, Status[] arrayAnswer, ArrayInfo curArrayInfo)
    {
        System.out.println("\tRemoving the first number in the array");
        int x = curArray[0];
        int b = curArrayInfo.getBeg();
        int e = curArrayInfo.getEnd();
        System.out.println("\tCurrent number: " + x);
        for (int i = b+1; i < b+x; i++)
            if (arrayAnswer[i] == Status.Empty)
                arrayAnswer[i] = Status.True;
        int i0 = b + x;
        if (i0 <= e)
            arrayAnswer[i0] = Status.False;      
        // Remove the first number in this array
        curArrayInfo.removeFirst();
    } // end removeBeg
    
    private void removeEnd(int[] curArray, Status[] arrayAnswer,  ArrayInfo curArrayInfo)
    {
        System.out.println("\tRemoving the last number in the array");
        int a = curArrayInfo.getNum();
        int x = curArray[a-1];
        int b = curArrayInfo.getBeg();
        int e = curArrayInfo.getEnd();        
        System.out.println("\tCurrent number: " + x);
        for (int i = e+1-x; i < e+1-1; i++)
            if (arrayAnswer[i] == Status.Empty)
                arrayAnswer[i] = Status.True;
        int i0 = e+1-x-1;
        if (i0 >= b)
            arrayAnswer[i0] = Status.False;                            
        // Remove the last number in this array
        curArrayInfo.removeLast();          
    } // end removeEnd
    
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
        
        int p = q;
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
            while(q < e+1 && arrayAnswer[q] == Status.True) // push forward q to the next true cluster when true
                q++;
            while(q < e+1 && arrayAnswer[q] == Status.Empty)
                q++;
            if (s == a - 1) // When x is last number in the array
            {   // Find the first false
                while(q < e+1 && arrayAnswer[q] != Status.False)
                    q++;
            } // end if

            System.out.println("\tp = " + p + ", q = " + q);  
        }
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
        System.out.println("\tb = " + b + ", e = " + e);
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
                    //System.out.println("q==ke?" + (q==ke));
                    //System.out.println("q = " + q + ", ke = " + ke);
                    if (q == e+1 || arrayAnswer[q] == Status.False)                                
                        for (int i = lastTrue + remainingT + 1; i < q; i++)
                            arrayAnswer[i] = Status.False;                                    
                }
                else   // when there are more than 1 numbers in the array
                {                                
                    //if (firstTrue <= x+b && s == 0) // belongs to the first number                                    
                } // end if
            } // end if
        } // end if
        return solved;
    } // end makeFarCellsFalse
    
    // If there is a true cluster whose length is maximum of the numbers, make false around it
    // e.g., [1,2] _ _ _ _ _ O O _ _ _ --> _ _ _ _ X O O X _ _ 
    private ArrayInfo makeFalseAround(int x, int kp, int p,
                                Status[] arrayAnswer, ArrayInfo curArrayInfo)
    {        
        int a = curArrayInfo.getNum();
        int b = curArrayInfo.getBeg();
        int e = curArrayInfo.getEnd();
        int[] curArray = curArrayInfo.getArray();        
        int firstTrue = getFirstStatus(p, e, arrayAnswer, Status.True);
        
        if (firstTrue <= e)  // when there is true cell
        {
            int it = firstTrue; // True cell iterator             
            // Find a true cluster 
            while (it < e && arrayAnswer[it + 1] == Status.True)
                it++;
            int lenT = it - firstTrue + 1;  // the length of true cluster
            if (curArrayInfo.isMax(lenT))  // When lenT is the largest number in the array or x == numT
            {
                System.out.println("\tMake false around true cells.");
                if (firstTrue-1 >= b)   // Make false right before the true cluster
                    arrayAnswer[firstTrue-1] = Status.False;
                if (it+1 <= e)  // Make false right after the true cluster
                    arrayAnswer[it+1] = Status.False;
                // when first number in the array, make false to the beginning
                if (curArrayInfo.indexOf(lenT) == 0 && (kp < x))
                {
                    int iFalse = firstTrue - 2;    // False iterator
                    while (iFalse >= b)
                    {
                        arrayAnswer[iFalse] = Status.False;
                        iFalse--;
                    } // end while                                
                    curArrayInfo.setBeg(firstTrue);    // Update beginning to the firstTrue
                    removeBeg(curArray, arrayAnswer, curArrayInfo);                                
                    //curArray = curArrayInfo.getArray(); // Row/column array 
                    //b = curArrayInfo.getBeg();
                    //ke = curArrayInfo.getLength();       // Effective length of grid
                } // end if
                // when last number in the array, make false to the end
                else if (curArrayInfo.indexOf(lenT) == a-1)     
                {
                    int iFalse = it + 2;    // False iterator
                    while (iFalse <= e)
                    {
                        arrayAnswer[iFalse] = Status.False;
                        iFalse++;
                    } // end while                                
                    curArrayInfo.setEnd(it);    // Update end to the last true
                    removeEnd(curArray, arrayAnswer, curArrayInfo);                                
                    //curArray = curArrayInfo.getArray(); // Row/column array 
                    //e = curArrayInfo.getEnd();
                    //ke = curArrayInfo.getLength();       // Effective length of grid
                } // end if
                //a = curArrayInfo.getNum();
            } // end if                        
        } // end if
        return curArrayInfo;
    } // end makeFalseAround
    
    // Returns the index of the first occurrence of the given Status stat.
    private int getFirstStatus(int p, int e, Status[] arrayAnswer, Status stat)
    {
        int firstStatus = p+1;
        while (firstStatus < e+1 && arrayAnswer[firstStatus] != stat)
            firstStatus++;
        return firstStatus;
    } // end getFirstStatus
    
    // Returns the index of the last occurrence of the given Status stat.
    private int getLastStatus(int firstStatus, int e, Status[] arrayAnswer, Status stat)
    {        
        int lastStatus = firstStatus - 1;
        while(lastStatus + 1 <= e && arrayAnswer[lastStatus + 1] == stat)
            lastStatus++;
        return lastStatus;
    }
    
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
            lastTrue = getLastStatus(firstTrue, e, arrayAnswer, stat);            
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
        
        public void removeFirst()
        {            
            int[] newArray = new int[num-1]; // One size smaller            
            for (int i = 0; i < num-1; i++)
                newArray[i] = thisArray[i+1];
            int newBeg = begIdx + thisArray[0] + 1;            
            setBeg(newBeg);            
            setArray(newArray); // Update the array            
            setNum(newArray.length);  // Reduce the array size  
            setSum();

            System.out.println("\tb = " + getBeg());
        }
        public void removeLast()
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

            System.out.println("\te = " + getEnd());
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
            arraySum = 0;
            for (int i = 0; i < num; i++)                    
                arraySum += thisArray[i];         
        }
    } // end ArrayInfo
} // end NonogramSolution5
