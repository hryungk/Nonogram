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
            
            /* Find True cell clusters [b, e], inclusive. */
            int[][] trueArrays = findStatusClusters(arrayAnswer, curArrayInfo, Status.True);
            int[] begIdxTrue = trueArrays[0];
            int[] trueClusterLen = trueArrays[1];
            int numTrueClusters = begIdxTrue.length;
            int[] endIdxTrue = new int[numTrueClusters];
            for (int ii = 0 ;ii < numTrueClusters; ii++)
                endIdxTrue[ii] = begIdxTrue[ii] + trueClusterLen[ii] - 1;
            
            /* Find Empty cell clusters [b, e], inclusive. */
            int[][] emptyArrays = findStatusClusters(arrayAnswer, curArrayInfo, Status.Empty);
            int[] begIdxEmpty = emptyArrays[0];
            int[] emptyClusterLen = emptyArrays[1];
            int numEmptyClusters = begIdxEmpty.length;
            int[] endIdxEmpty = new int[numEmptyClusters];
            for (int ii = 0 ;ii < numEmptyClusters; ii++)
                endIdxEmpty[ii] = begIdxEmpty[ii] + emptyClusterLen[ii] - 1;
            //int maxEmptyLen = emptyClusterLen[0];            
            
            /* When there is only one number in the array */
            if (a == 1 && !solved) 
            {
                System.out.println("\t\tThere is only one number in the array.");
                // Fill up between the first and the last true
                int locCurTrue = getFirstStatus(b-1, e, arrayAnswer, Status.True);
                int lastCurTrue = getLastStatus(locCurTrue, e+1, arrayAnswer, Status.True);                
                if (b <= locCurTrue && locCurTrue <= e && locCurTrue != lastCurTrue)    
                {
                    System.out.println("\t\t\tFill up between first true: " + locCurTrue + " and last true: " + lastCurTrue);
                    for (int i = locCurTrue; i <= lastCurTrue; i++)
                        if (arrayAnswer[i] == Status.Empty)
                            arrayAnswer[i] = Status.True;
                    System.out.println("\t\t" + printCells(arrayAnswer)); 
                } // end if
                
                // Make small empty clusters false.                 
                for (int i = 0; i < emptyClusterLen.length; i++)
                {
                    int curBegIdxEmpty = begIdxEmpty[i];
                    int curEmptyClusterLen = emptyClusterLen[i];
                    int curEndIdxEmpty = curBegIdxEmpty + curEmptyClusterLen - 1;
                    boolean isBetweenFalses  = 
                    ((curBegIdxEmpty - 1 == -1) || (arrayAnswer[curBegIdxEmpty - 1] == Status.False)) &&
                    ((curEndIdxEmpty+1 == k) || (arrayAnswer[curEndIdxEmpty+1] == Status.False));                    
                    
                    // Find a true cluster whose length is the same as x and in a later section.
                    boolean foundLaterTrue = false;
                    for (int ii = 0; ii < trueClusterLen.length; ii++)
                        foundLaterTrue = foundLaterTrue || (begIdxTrue[ii] > curEndIdxEmpty);                      
                    
                    if ((curEmptyClusterLen < curArray[0] || foundLaterTrue) && isBetweenFalses) 
                    {
                        System.out.println("\t\t\tMake smaller empty clusters false");
                        for (int ii = curBegIdxEmpty; ii < curBegIdxEmpty + curEmptyClusterLen; ii++)
                            if (arrayAnswer[ii] == Status.Empty)
                                arrayAnswer[ii] = Status.False;     
                        System.out.println("\t\t" + printCells(arrayAnswer)); 
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
            
            if (!solved)
            {
                makeTrueBetweenTrues(curArrayInfo, arrayAnswer);
                /* Check whether this array is solved. */
                solved = checkAllTrue(curArrayInfo, arrayAnswer, solved);
            }
            
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
                    
                    /* Determine current section. */
                    // When the section ended where there are still numbers in the array to be investigated,
                    // use the previous p and q.
                    // Otherwise, use a new pair of p and q.
                    int kp = q - p - 1; // The number of empty cells between p and q
                    if (!(a > 1 && s > 0 && q == e + 1))
                    {             
                        // Determine current section (p, q), exclusive.                          
                        int[] pq = getpq(arrayAnswer,curArrayInfo,s,q);
                        p = pq[0];
                        q = pq[1];                        
                        kp = q - p - 1; // The number of empty cells between p and q
                        
                         // pass the section if it's smaller than x
                        if (kp < x && q <= e+1 &&  
                            (closedWithFalse(p, q, k, arrayAnswer) && trueExists(p,q,arrayAnswer))) // But not when the section is closed with falses and empty.--> may have to close it.
                        {
                            pq = getpq(arrayAnswer,curArrayInfo,s,q);
                            p = pq[0];
                            q = pq[1]; 
                            kp = q - p - 1; // The number of empty cells between p and q
                        }                        
                    } // end if
                    //kp = q - p - 1; // The number of empty cells between p and q   
                    
                    // Redefine the section when there are multiple true clusters in the current general section
                    // [5,3,1] _ _ _ _ O _ O _ _ _ O _ _ O _: x = 5 in (-1,6) to (-1,9)
                    int[] pq = getpq_general(curArrayInfo, s);                        
                    int pGeneral = pq[0];
                    int qGeneral = pq[1];
                    // Check whether there is a true cluster that belongs to the current general section.
                    int[] curTrueGeneral = getCurrentStatusCluster(x, pGeneral, qGeneral, Status.True, curArrayInfo, arrayAnswer);                    
                    int numCurTrueGeneral = curTrueGeneral[2];    // Number of true clusters in the current section
                    int fromHeadToToeGeneral = curTrueGeneral[3]; // The length from the first to the last true cell in the current section.
                    // But when there are false cells in the general section, this doesn't apply.
                    // [3,2,1,1] _ _ O _ X _ O O X _ X X X X _
                    int[] curFalseGeneral = getCurrentStatusCluster(x, pGeneral, qGeneral, Status.False, curArrayInfo, arrayAnswer);
                    int thisLocFalse = curFalseGeneral[0];   // Beginning index of False cluster (first one if there are multiple, -1 if none)
                    if (numCurTrueGeneral > 1 && fromHeadToToeGeneral <= x && thisLocFalse == -1)
                    {   // If there are multiple true clusters && the total length of them are less than x, use the q from general sectioning.
                        q = qGeneral;
                        kp = q - p - 1;
                    }
                    
                    System.out.println("\tSection "+ (s+1) +" / "+a+": p = " + p + ", q = " + q + ", k' = " + kp);
                    

                    /* Procede only when the index is within boundary */
                    if (p < e+1 && q <= e+1) 
                    {                    
                        /* Find True cell clusters [b, e], inclusive. */
                        trueArrays = findStatusClusters(arrayAnswer, curArrayInfo, Status.True);
                        begIdxTrue = trueArrays[0];
                        trueClusterLen = trueArrays[1];
                        numTrueClusters = begIdxTrue.length;
                        endIdxTrue = new int[numTrueClusters];
                        for (int ii = 0 ;ii < numTrueClusters; ii++)
                            endIdxTrue[ii] = begIdxTrue[ii] + trueClusterLen[ii] - 1;

                        /* Find Empty cell clusters [b, e], inclusive. */
                        emptyArrays = findStatusClusters(arrayAnswer, curArrayInfo, Status.Empty);
                        begIdxEmpty = emptyArrays[0];
                        emptyClusterLen = emptyArrays[1];
                        numEmptyClusters = begIdxEmpty.length;
                        int maxEmptyLen = 0;                        
                        for (int i = 0; i < numEmptyClusters; i++)
                            maxEmptyLen = Math.max(maxEmptyLen, emptyClusterLen[i]);

                        // Check whether there is a true cluster that belongs this section.
                        int[] curTrue = getCurrentStatusCluster(x, p, q, Status.True, curArrayInfo, arrayAnswer);
                        int locCurTrue = curTrue[0];   // Beginning index of current True cluster (-1 if none)
                        int lenCurTrue = curTrue[1];    // Length of current True cluster
                        if (lenCurTrue != 0)
                            System.out.println("\t\tCurrent True Cluster: from " + locCurTrue + " to " + (locCurTrue + lenCurTrue - 1));
                                                
                        
                        // If current true cluster is bigger than x, skip to the next number in the array
                        if (lenCurTrue > x && s < a-1)
                        {
                            System.out.println("\t\tCurrent true cluster > x; moving to the next number in the array...");
                            s++;
                            x = curArray[s];                            
                            System.out.println("\t\tCurrent number: " + x);
                            pq = getpq_general(curArrayInfo, s);
                            p = pq[0];
                            pq = getpq(arrayAnswer,curArrayInfo,s,q);                            
                            q = pq[1]; 
                            kp = q - p - 1; // The number of empty cells between p and q
                            System.out.println("\t\tp = " + p + ", q = " + q + ", k' = " + kp);
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
                        
                        int sumFromX = getSumFromX(s, curArrayInfo, 1);   // Summation of numbers + 1 space from x to the end in the array                        
                        int sumToX = getSumToX(s, curArrayInfo, 1); // Summation of numbers + 1 space from first number to x  
                        
                        // Find a true cluster whose length is the same as x and in a later section.
                        // [1,2,6] _ X O _ O O X _ O O O O O _ X --> Don't fill  
                        // [1,1,1] _ X X X X X _ X O X X X X _ _ --> Don't fill
                        // [2, 2, 4] _ _ _ X X X O O X X _ O O O _ --> _ O _ X X X O O X X _ O O O _
                        // [1,1,1] X X X X X X _ X O X X X X _ _ --> X X X X X X O X O X X X X _ _
                        boolean foundValidLaterSameTrue = false;
                        for (int i = 0; i < numTrueClusters; i++)
                        {
                            boolean foundLaterSameTrue = (begIdxTrue[i] >= q && trueClusterLen[i] == x);                            
                            boolean isValidLoc = sumFromX <= e - begIdxTrue[i] + 1;
                            boolean isThereRoomForAll = numEmptyClusters >= a;
                            foundValidLaterSameTrue = foundValidLaterSameTrue || (foundLaterSameTrue && isValidLoc && isThereRoomForAll);
                        } // end for                        

                        // Find an empty cluster whose length is greather than or equal to x and in an earlier section.
                        // Also, the last index of the previous section should be larger than sumToX
                        // [2,2,4] _ _ _ _ _ _ _ O _ X _ O O _ _ --> _ _ _ _ _ _ _ O _ X _ O O _ _ False
                        // [2,2,4] _ _ _ _ _ _ _ O _ _ X O O _ _ --> _ _ _ _ _ _ _ O _ _ X O O _ _ True
                        // [1, 4, 3] _ _ _ _ _ _ _ _ X _ _ O _ _ X --> False for x = 4 in (1, 8)
                        boolean foundValidEarlierLargerEmpty = false;
                        int earlierLargerEmptyLen = 0;
                        for (int i = 0; i < numEmptyClusters; i++)
                        {
                            boolean foundEarlierLargerEmpty = (begIdxEmpty[i] < p) && (emptyClusterLen[i] >= x) && (endIdxEmpty[i] < p || q == e + 1);
                            boolean isLocValid = sumToX < p+1;                            
                            foundValidEarlierLargerEmpty = foundValidEarlierLargerEmpty || (foundEarlierLargerEmpty && isLocValid);
                            if (foundEarlierLargerEmpty)
                                earlierLargerEmptyLen = Math.max(earlierLargerEmptyLen,emptyClusterLen[i]); // In case there are multiple numbers meeting the requirement, choose bigger number.
                        } // end for
                        
                        // Find an empty cluster whose length is greather than or equal to x and in an earlier section.
                        // But ignore if two neighboring empty clusters are around a true cluster.
                        // e.g. [2,5] _ _ _ _ O _ _ _ _ X _ _ _ _ _ --> True            
                        // e.g. [2,5,1] _ _ _ _ _ O _ _ _ _ _ _ _ X O --> True
                        // [2,3,1] _ _ _ _ O O X O _ _ X _ _ X _ --> True for x = 3 at (6, 10)
                        // [1, 4, 3] _ _ _ _ _ _ _ _ X _ _ O _ _ X --> False for x = 4 in (1, 8)
                        boolean foundEffectiveEarlierLargerEmpty = false;
                        int earlierEffectiveLargerEmptyLen = 0;
                        for (int i = 0; i < numEmptyClusters-1; i++)
                        {                            
                            int curBeg = begIdxEmpty[i];
                            int curLen = emptyClusterLen[i];
                            int curEnd = curBeg + curLen - 1;
                            int nextBeg = begIdxEmpty[i+1];
                            int nextLen = emptyClusterLen[i+1];
                            int nextEnd = nextBeg + nextLen - 1;
                            boolean emptyBetweenO = true;
                            boolean followedByTrue = false;
                            int trueLen = 0;
                            for (int ii = curEnd+1; ii < nextBeg;ii++)
                            {
                                boolean isTrue = arrayAnswer[ii] == Status.True;
                                if (isTrue)
                                    trueLen++;
                                if (trueLen == 1)
                                    followedByTrue = true;
                                emptyBetweenO = emptyBetweenO && isTrue;
                            } // end for
                            
                            boolean temp = (curBeg < p) && (curLen >= x || (emptyBetweenO && s>0 && x+curArray[s-1]+1 <= nextEnd - curBeg + 1)) && (curEnd < p || q == e + 1);
                            boolean isLocValid = (b+sumToX < p+1) || (-1 < p && arrayAnswer[p] != Status.False);   
                            foundEffectiveEarlierLargerEmpty = foundEffectiveEarlierLargerEmpty || (temp && isLocValid);
                            if (foundEffectiveEarlierLargerEmpty)
                            {
                                int effectiveLen = curLen;
                                if (emptyBetweenO)  // when a true cluster is between two empty clusters
                                    effectiveLen = nextEnd - curBeg + 1;
                                else if (followedByTrue)    // if only a true cluster follows the current empty cluster
                                    effectiveLen = curLen + trueLen;
                                earlierEffectiveLargerEmptyLen = Math.max(earlierEffectiveLargerEmptyLen,effectiveLen); // In case there are multiple numbers meeting the requirement, choose bigger number.
                            } // end if
                        } // end for

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
                        int earlierLargerTrueLen = 0;
                        for (int i = 0; i < numTrueClusters; i++)
                        {
                            boolean tempFound = (begIdxTrue[i] < p && trueClusterLen[i] > x);
                            if (tempFound)
                                earlierLargerTrueLen = trueClusterLen[i];
                            foundEarlierLargerTrue = foundEarlierLargerTrue || tempFound;                            
                        } // end for
                        
                        // Find an empty cluster whose length is greater than x in the current section.
                        // Or empty cluster + true cluster >= x
                        boolean foundThisLargerEmpty = false;
                        int thisLargerEmptyLen = 0;
                        ie = 0;
                        while (!foundThisLargerEmpty && ie < numEmptyClusters)
                        {
                            foundThisLargerEmpty = (p <= begIdxEmpty[ie] && begIdxEmpty[ie] < q)  && 
                                    (emptyClusterLen[ie] > x || lenCurTrue + emptyClusterLen[ie] >= x);
                            if (foundThisLargerEmpty)
                                thisLargerEmptyLen = lenCurTrue + emptyClusterLen[ie];
                            else
                                ie++;                            
                        } // end while                        
                        
                        // Find an empty cluster whose length is greather than or equal to x and in a later section.
                        // Only if the empty cluster doesn't follow a true cluster whose length is larger than x
                        // (because if that's the case, the later empty cluster doesn't belong to x)
                        boolean foundValidLaterLargerEmpty = false;
                        int validLaterLargerEmptyLen = 0;
                        for (int i = 0; i < numEmptyClusters; i++)
                        {
                            int curBegIdxEmpty = begIdxEmpty[i];
                            int curEmptyClusterLen = emptyClusterLen[i];
                            boolean foundLaterLargerEmpty = (curBegIdxEmpty > q && curEmptyClusterLen >= x);// && b+sumFromX >= curBegIdxEmpty);
                            
                            // Check whether there is a true cluster coming before the empty cluster whose longer than x
                            // (because then x doesn't belong to this later empty cluster. e.g., _ _ _ X X O O O _ _ and current number is 2)
                            int[] thisTrue = getCurrentStatusCluster(x, q, curBegIdxEmpty, Status.True, curArrayInfo, arrayAnswer);
                            int thisLocTrue = thisTrue[0];   // Beginning index of True cluster (first one if there are multiple, -1 if none)
                            int thisLenTrue = thisTrue[1];    // Length of True cluster
                            //if (thisLocTrue != -1)  // there exist true cluster between q and current empty cluster
                            //    foundLaterLargerEmpty = foundLaterLargerEmpty && (thisLenTrue <= x);
                            boolean isAfterSmallerTrueCluster = (thisLocTrue != -1) && (thisLenTrue <= x);// && (thisLocTrue < b+sumFromX);                            
                            
                            // Check whether this later larger empty is located where x can be.
                            // e.g., [4, 1, 2] _ _ O O _ X X X _ _ X _ _ _ _  --> not valid 
                            boolean isLocValid = sumFromX <= e - curBegIdxEmpty + 1;
                            
                            boolean temp = foundLaterLargerEmpty && isLocValid && ((thisLocTrue == -1) || isAfterSmallerTrueCluster);
                            if (temp && curEmptyClusterLen >= x)
                                validLaterLargerEmptyLen = curEmptyClusterLen;
                            foundValidLaterLargerEmpty = foundValidLaterLargerEmpty || temp;
                        } // end for  
                        
                        
                        // Find an empty cluster whose length is greather than or equal to x and in a later section.
                        // But ignore if two neighboring empty clusters are around a true cluster.
                        // e.g. [2,3,3] _ _ X _ _ _ X _ _ O _ O O _ X --> Don't fill (2,6)
                        boolean foundEffectiveLaterLargerEmpty = false;
                        int laterEffectiveLargerEmptyLen = 0;
                        for (int i = 0; i < numEmptyClusters-1; i++)
                        {
                            boolean emptyBetweenO = true;
                            int curBeg = begIdxEmpty[i];
                            int curLen = emptyClusterLen[i];
                            int curEnd = curBeg + curLen - 1;
                            int nextBeg = begIdxEmpty[i+1];
                            int nextLen = emptyClusterLen[i+1];
                            int nextEnd = nextBeg + nextLen - 1;
                            for (int ii = curEnd+1; ii < nextBeg;ii++)
                                emptyBetweenO = emptyBetweenO && (arrayAnswer[ii] == Status.True);
                            
                            boolean temp = (q < curBeg) && 
                                    ((q+sumFromX <= e)||(q <= e && arrayAnswer[q] == Status.True && q+sumFromX-1 <= e)) && //[3,6]  X _ _ O _ O _ _ _ O _ _ _ _ _ : x = 3, (0,5) when q is true, include q to count effective length of next empty length
                                    (curLen >= x || (emptyBetweenO && x < nextEnd - curBeg + 1));
                            // Check whether this later larger empty is located where x can be.
                            boolean isLocValid = sumFromX <= e - curBeg + 1;
                            
                            foundEffectiveLaterLargerEmpty = foundEffectiveLaterLargerEmpty || (temp && isLocValid);
                            if (foundEffectiveLaterLargerEmpty)
                                laterEffectiveLargerEmptyLen = Math.max(laterEffectiveLargerEmptyLen,curLen); // In case there are multiple numbers meeting the requirement, choose bigger number.
                        } // end for
                                                
                        // Find consecutive non-false cluster up to the current section.
                        // [3, 3, 2] _ _ _ _ _ _ _ _ _ _ O X X _ _ --> 11 for s = 1 (3, 11)
                        int conscNonFalseLen = 0;
                        int begConscNonFalse = -1;
                        int idxLastNonFalse = -1;
                        int ii = q-1;
                        boolean foundNonFalse = false;
                        while (ii >= b && !foundNonFalse)
                        {
                            foundNonFalse = arrayAnswer[ii] != Status.False;
                            if (foundNonFalse)
                                idxLastNonFalse = ii;                            
                            else
                                ii--;
                        } // end while
                        for (int i = idxLastNonFalse; i >= b; i--)
                        {
                            if (arrayAnswer[i] != Status.False)
                            {
                                conscNonFalseLen++;
                                begConscNonFalse = i;
                            }
                        } // end for
                        
                        // Check whether empty clusters are adjacent to true clusters.
                        // In this case, it is not a true empty cluster, rather a potential true cluster.
                        boolean emptyBetweenO = false;
                        for (int i = 0; i < numEmptyClusters; i++)
                        {
                            int curBegIdxEmpty = begIdxEmpty[i];
                            int curEmptyClusterLen = emptyClusterLen[i];
                            int curEndIdxEmpty = curBegIdxEmpty+curEmptyClusterLen-1;
                            if (curBegIdxEmpty >= p && curBegIdxEmpty>=1) // > 0
                                emptyBetweenO = emptyBetweenO || (arrayAnswer[curBegIdxEmpty-1] == Status.True);
                            if (curEndIdxEmpty+1 < k) // If any of empty cluster follows immediately after true, the empty cluster count may not be correct.
                                emptyBetweenO = emptyBetweenO || (arrayAnswer[curEndIdxEmpty+1] == Status.True);
                        } // end for
                        
                        // [1,2,6] _ X O _ O O X _ O O O O O _ X --> Don't fill                        
                        boolean foundThisSameTrue = false;
                        for (int i = 0; i < numTrueClusters; i++)
                        {
                            int curBegIdxTrue = begIdxTrue[i];
                            int curTrueClusterLen = trueClusterLen[i];
                            foundThisSameTrue = foundThisSameTrue || 
                                    (p < curBegIdxTrue && curBegIdxTrue + curTrueClusterLen <= q && curTrueClusterLen == x);
                        } // end for
                        
                        // [1,4,2] _ _ _ _ O O _ X X _ _ _ _ _ _ --> _ _ _ O O O _ X X _ _ _ _ _ _
                        // When there is a true cluster in the current section, if sum+(a-1) > kp, fill up.
                        boolean foundThisTrue = false;
                        for (int i = 0; i < numTrueClusters; i++)
                        {
                            int curBegIdxTrue = begIdxTrue[i];
                            int curTrueClusterLen = trueClusterLen[i];
                            foundThisTrue = foundThisTrue || (p < curBegIdxTrue && curBegIdxTrue + curTrueClusterLen <= q);
                        } // end for
                        
                        // When there is a valid true in the later section, don't fill up.
                        // [2, 3] _ _ X _ _ _ X X X _ X _ O O _
                        // [2, 3, 3] _ _ X _ O X X O _ O X O O _ X
                        // [1,1,1] X X X X X X _ X O X X X X _ _ --> X X X X X X O X O X X X X _ _
                        boolean foundLaterTrue = false;
                        for (int i = 0; i < numTrueClusters; i++)
                        {
                            int curBegIdxTrue = begIdxTrue[i];
                            int curTrueClusterLen = trueClusterLen[i];
                            boolean isLocValid = sumFromX <= e - curBegIdxTrue + 1;
                            boolean isThereRoomForAll = numEmptyClusters >= a;
                            boolean temp = (q < curBegIdxTrue) && (curTrueClusterLen <= x);
                            foundLaterTrue = foundLaterTrue || (temp && isLocValid && isThereRoomForAll);
                        } // end for
                        
                        int i0 =  p + 1 + kp - x;   // Beginning index of true cells                         
                        
                        // When number of numbers == number of true clusters, each of true clusters'
                        // length should be smaller to respective numbers.
                        
                        // Check whether true clusters are far enough from each other
                        boolean overlap = false; 
                        if (numTrueClusters == a) 
                        {          
                            boolean eachTrueMatchNum = true;
                            for (int i = 0; i < numTrueClusters; i++)        
                                eachTrueMatchNum = eachTrueMatchNum & (curArray[i] >= trueClusterLen[i]);
                            if (eachTrueMatchNum)
                            {
                                int[][] trueRange = getTrueRange(p, q, s, k, curArrayInfo, arrayAnswer);
                                int[] idxBeg = trueRange[0];  // An array of beginning index of each number's possible range
                                int[] idxEnd = trueRange[1]; // An array of end index of each number's possible range  

                                if (a > 1)
                                {
                                    for (int i = 0; i < a-1; i++)
                                    {
                                        int tempMax = Math.max(curArray[i], curArray[i+1]);
                                        overlap = overlap || (idxBeg[i+1] <= idxEnd[i] && endIdxTrue[i+1]-begIdxTrue[i]+1 <= tempMax);                
                                    } // end for
                                } // end if
                            } // end if
                        } // end if
                        
                        boolean isEnclosed = closedWithFalse(p, q, k, arrayAnswer);
                        boolean isBigForTwo = (s < a-1 && kp >= x + curArray[s+1]+1)
                                            || (s > 0 && kp >= x + curArray[s-1]+1)
                                            || (a == 1 && kp >= x);
                        
                        boolean foundX = isNumberLocated(s, k, arrayAnswer,curArrayInfo);
                        
                        
                        /* Make cells in this section that are farther than remainingT false */
                        // e.g., [3, 2] _ _ _ O _ _ X _ _ X --> X _ _ O _ _ X _ _ X
                        if (!solved)
                        {
                            solved = makeFarCellsFalse(x, p, q, s, k, curArrayInfo, numT, sum, 
                                                       solved, arrayAnswer);
                            /* Check whether this array is solved. */
                            solved = checkAllTrue(curArrayInfo, arrayAnswer, solved);
                        } // end if
                        
                        
                        /* Fill up the middle (2x-k') cells. */
                        // When there are more than one number left in the array but it is alreay filled in the middle of the row/column, 
                        // (so that the code still doesn't know it is filled), and there are multiple locations for the last number to be,
                        // we should skip this. Example is _XXOXX_XOO and array is [1,1,2]    
                        // [3.6] X _ _ O _ O _ _ _ O _ _ _ _ _ --> X _ _ O _ O _ _ _ O O _ _ _ _ for (sum - numT == x && numTrueClusters <= a && a >= 2 && isBigForTwo)
                        // [1, 1, 3] _ _ _ X _ X _ _ _ X --> _ _ _ X _ X O O O X
                        // [2, 2] _ _ X X X X _ _ _ _ --> O O X X X X _ _ _ _
                        // [1, 2, 1] O X _ _ _ X _ _ _ _ --> Don't fill
                        // [1, 1, 2] _ _ X O X _ _ O _ X --> Don't fill                                                
                        // [1, 1, 2] _ X O X X X _ O _ X --> O X O X X X _ O _ X (should be fixed P6, modify with foundLaterSameTrue)    
                        if (!solved && (x > kp/2) && (kp >= x) && (i0 >= b) && !overlap && (sumToX > earlierLargerEmptyLen) && (!foundX) && // !multipleXExists(x,curArrayInfo)) &&// [4,1,3] _ _ _ _ _ _ _ _ _ _ O _ _ _ _
                            (!((sum - numT == x && numTrueClusters <= a && a >= 2 && isBigForTwo) || (kp < numE && a == 1) || foundValidLaterSameTrue ||
                            (numEmptyClusters == a && emptyClusterLen[s] == x && numT==x) ||    // [1,1,2] _XXOXX_XOO
                            foundValidEarlierLargerEmpty || foundValidLaterLargerEmpty || // [3] _ _ _ _ _ _ X _ _ _           
                            foundThisSameTrue || foundLaterTrue || // (foundLaterTrue && s == a - 1)
                            foundEffectiveEarlierLargerEmpty || foundEffectiveLaterLargerEmpty) || // [2, 5] _ _ _ _ O _ _ _ _ X _ _ _ _ _                   
                            (numEmptyClusters == a && a >= 2 && foundValidLaterLargerEmpty && sumFromX > validLaterLargerEmptyLen && (validLaterLargerEmptyLen!= 1 && (!emptyBetweenO || (isEnclosed && !isBigForTwo && foundThisTrue)))) ||// [1,2,1] _ _ _ X _ _ X _ _ _ --> _ _ _ X O O X _ _ _ // [4,3,2] X _ _ _ O _ _ _ _ _ X _ _ _ _ --> X _ _ _ O _ _ O O _ X _ _ _ _ // [1,1,2] X _ X X O X X X X _ X O O _ X --> don't fill
                            (a > 1 && s == a - 1 && foundEarlierLargerTrue && foundThisSameEmpty && !foundEffectiveLaterLargerEmpty && !foundEffectiveEarlierLargerEmpty) ||// [1,2,1] _ _ _ _ X X O O X _ --> _ _ _ _ X X O O X O
                            (numEmptyClusters == a && q == e+1 && foundThisSameEmpty && !emptyBetweenO && numTrueClusters < a-1) || // [1, 1, 3] _ _ _ X _ X _ _ _ X --> _ _ _ X _ X O O O X // [4,2,2] _ _ O O _ _ _ _ _ X O O X _ _ //[1,4,1] _ _ _ _ _ _ O _ _ _ _ _ X _ X
                            (q == e+1 && x == kp && foundThisTrue))) //[4,1,3] _ _ _ O _ _ _ _ _ X O O _ X X --> _ _ _ O _ _ _ _ _ X O O O X X
                            //(foundThisTrue && sum + (a-1) > kp)))// [1,4,2] _ _ _ _ O O _ X X _ _ _ _ _ _ --> don't fill
                            //(foundLaterSameTrue && foundThisSameEmpty && validLaterLargerEmptyLen <= x)))
                        {
                            // When there is a true cluster whose length is the same as x and not in the current section, don't fill up.
                            System.out.println("\t\tFill up middle (2x-k') cells");
                            for (int i = i0; i <= p + x; i++)
                                if (arrayAnswer[i] == Status.Empty)
                                    arrayAnswer[i] = Status.True;   
                            numT = numOfStatus(b, e, arrayAnswer, Status.True); // Update numT
                            System.out.println("\t\t" + printCells(arrayAnswer)); 
                            /* Check whether this array is solved. */
                            solved = checkAllTrue(curArrayInfo, arrayAnswer, solved);

                        } // end if 

                        /* Make False When the section length is less than x. */
                        // When there is less number of empty cells than the current number, make false.
                        // But when there is a later section where x actually belongs, skip this part.
                        // Also, only when this section is defined between false cells.
                        // [1,1,2] O X X _ X O X X X X _ O _ _ X --> O X X X X O X X X X _ O _ _ X
                        // [2,3,1] _ _ _ _ O O X _ X _ X _ _ X _ --> don't know
                        // [2,5] _ _ _ _ _ _ _ _ _ _ X _ _ _ _ --> _ _ _ _ _ _ _ _ _ _ X X X X X
                        // [2,3] _ _ _ _ _ _ X X _ _ X _ _ _ _ --> don't know
                        // [2,2] _ _ _ _ _ _ X _ X X X X _ _ _ --> _ _ _ _ _ _ X X X X X X _ _ _
                        // [1,2,1,2] O X _ X O O _ _ _ X _ _ _ _ _ --> O X X X O O _ _ _ X _ _ _ _ _
                        // [1,4,1] _ _ _ _ O _ _ _ _ X X O X _ X --> _ _ _ _ O _ _ _ _ X X O X X X
                        // X O X _ X _ X O O _ _ _ X _ _ 
                        boolean canPreviousNumFit = (s > 0 && curArray[s-1] <= kp && curArray[s-1] + x <= e - p - 1);
                        boolean canNextNumFit = (s < a-1 && curArray[s+1] <= kp && b+x+curArray[s+1]+1 < q);
                        if (!solved && (x > kp || (a-1 == s && foundX)) && !canPreviousNumFit&& !canNextNumFit // || numT>getSumToX(s, curArrayInfo, 0))
                            && (closedWithFalse(p, q, k, arrayAnswer))) 
                        {
                            System.out.println("\t\tMake this section false");
                            for (int i = p + 1; i < q; i++)
                                if (arrayAnswer[i] == Status.Empty)
                                    arrayAnswer[i] = Status.False;         
                            System.out.println("\t\t" + printCells(arrayAnswer)); 
                        } // end if    

                        
                        /* Make cells in this section that are farther than remainingT false */
                        // e.g., [3, 2] _ _ _ O _ _ X _ _ X --> X _ _ O _ _ X _ _ X
                        if (!solved)
                        {
                            solved = makeFarCellsFalse(x, p, q, s, k, curArrayInfo, numT, sum, 
                                                       solved, arrayAnswer);
                            /* Check whether this array is solved. */
                            solved = checkAllTrue(curArrayInfo, arrayAnswer, solved);
                        } // end if

                        
                        /* If there is a true cluster whose length is maximum of the numbers, make false around it */
                        // e.g., [1,2] _ _ _ _ _ O O _ _ _ --> _ _ _ _ X O O X _ _                               
                        if (!solved)
                        {
                            curArrayInfo = makeFalseAround(s, p, q, k, arrayAnswer, 
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
                        // [3, 2] _ _ _ X O _ _ _ _ _ -->  _ _ _ X O O _ _ _ _ (when minimum > 1)
                        if(!solved)
                        {
                            fillUpFromEnds(s, p, q, k, curArray, a, sum, sumToX, sumFromX, solved, lenCurTrue, 
                                    earlierEffectiveLargerEmptyLen, thisLargerEmptyLen, conscNonFalseLen, begConscNonFalse,
                                    foundEarlierLargerTrue, curArrayInfo, arrayAnswer);
                            solved = checkAllTrue(curArrayInfo, arrayAnswer, solved);
                        } // end if

                        /* Update parameters. */
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
                            System.out.println("\t\tFor section (" + p + ", " + q +"):");
                            System.out.println("\t\t" + printCells(arrayAnswer)); 
                        } // end if                        
                        else
                        {
                            System.out.println("\t\tReached end of the grid.");
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
                    // If we reach the last number in the array but there are more sections left,
                    // investigate the later section with the last number.
                    if (s == a && q - 1 < e)                    
                        s--;
                    
                } // end while                
            } // end if
            if (solved)
                System.out.println("\t\t" + printCells(arrayAnswer));
        } // end if
        // Update beginning and end index
        oldbe = be;
        be = updateEnds(b, e, curArrayInfo, arrayAnswer);
        b = be[0];
        e = be[1];
        ke = curArrayInfo.getLength();
        if (!Arrays.equals(oldbe, be))    // If anything is updated, print out the array.
            System.out.println("\t\tUpdating Ends:\n\tb = " + b + ", e = " + e + ", ke = " + ke);        
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
        System.out.println("\t\tRemoving the first number in the array");
        int x = curArray[0];
        int b = curArrayInfo.getBeg();
        int e = curArrayInfo.getEnd();
        System.out.println("\t\t\tCurrent number: " + x);
        for (int i = b+1; i < b+x; i++)
            if (arrayAnswer[i] == Status.Empty)
                arrayAnswer[i] = Status.True;
        int i0 = b + x;
        if (i0 <= e)
            if (arrayAnswer[i0] == Status.Empty)
                arrayAnswer[i0] = Status.False;      
        System.out.println("\t\t" + printCells(arrayAnswer)); 
        // Remove the first number from this array
        curArrayInfo.removeFirstInArray();
    } // end finishFirstNumber
    
    // Fill up true cells and make false in front of this true cluster.
    private void finishLastNumber(int[] curArray, Status[] arrayAnswer,  ArrayInfo curArrayInfo)
    {
        System.out.println("\t\tRemoving the last number in the array");
        int a = curArrayInfo.getNum();
        int x = curArray[a-1];
        int b = curArrayInfo.getBeg();
        int e = curArrayInfo.getEnd();        
        System.out.println("\t\t\tCurrent number: " + x);
        for (int i = e+1-x; i < e+1-1; i++)
            if (arrayAnswer[i] == Status.Empty)
                arrayAnswer[i] = Status.True;
        int i0 = e+1-x-1;
        if (i0 >= b)
            arrayAnswer[i0] = Status.False;          
        System.out.println("\t\t" + printCells(arrayAnswer)); 
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
    
    // General rules on filling up the grids
    private int[] getpq_general(ArrayInfo curArrayInfo, int s)
    {        
        int b = curArrayInfo.getBeg();
        int e = curArrayInfo.getEnd();
        int a = curArrayInfo.getNum();     
                   
        int sumToXm1 = getSumToX(s-1, curArrayInfo, 0);  // sum of numbers from x_0 to x_(s-1)                
        int sumFromX = getSumFromX(s+1, curArrayInfo, 0); // sum of numbers from x_(s+1) to x_(a-1)
        int p = b + sumToXm1 + s - 1;
        int q = e - (sumFromX + (a - (s + 1))) + 1;              
        
        int[] pq = {p, q};
        return pq;
    }// end getpq_general
    
    private int[] getpq_conditional(Status[] arrayAnswer, ArrayInfo curArrayInfo, int s, int q)
    {
        int b = curArrayInfo.getBeg();
        int e = curArrayInfo.getEnd();
        int a = curArrayInfo.getNum();        
                
        int p;
        
        if (-1 < q && q < e+1 && arrayAnswer[q] == Status.False)
            p = Math.max(q, b-1); 
        else
        {   // If it's not false at q, move one back.
            p = Math.max(q-1, b-1); 
            int sumFromX = getSumFromX(s, curArrayInfo, 1);
            //int sumToX = getSumToX(s, curArrayInfo, 1);
            int[] pq = getpq_general(curArrayInfo, s);
            int pGeneral = pq[0];
            if (p > (e - sumFromX))// In case previous q would have gone too far
                p = Math.min(p, pGeneral); 
        }
        
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
            while (p > -1 && arrayAnswer[p] == Status.Empty)  // push back p if empty
                p--; 
            while (q < e+1 && arrayAnswer[q] == Status.True) // push forward q to the next true cluster when true
                q++;
            while (q < e+1 && arrayAnswer[q] == Status.Empty)
                q++;            
            if (s == a - 1) // When x is last number in the array
            {   // Find the first false
                while(q < e+1 && arrayAnswer[q] != Status.False)
                    q++;
            } // end if 
        } // end if
                
        int[] result = {p, q};
        return result;
    } // end getpq_conditional
    
    // If there are a-1 number of X clusters and distance between them are sufficient, use this criteria.
    // [2,3,6] O O X _ _ O _ O X O O O O _ _ --> divide by X
    // [3,2,1,1] _ _ O _ X _ O O X _ X X X X _ --> divide by X clusters
    private int[] getpq_byX(Status[] arrayAnswer, ArrayInfo curArrayInfo, int s)
    {
        // Find True cell clusters [b, e], inclusive.
        int[][] falseArrays = findStatusClusters(arrayAnswer, curArrayInfo, Status.False);
        int[] begIdxFalse = falseArrays[0];
        int[] falseClusterLen = falseArrays[1];
        int numFalseClusters = begIdxFalse.length;
        int[] endIdxfalse = new int[numFalseClusters];
        for (int i = 0 ;i < numFalseClusters; i++)
            endIdxfalse[i] = begIdxFalse[i] + falseClusterLen[i] - 1;
        
        int a = curArrayInfo.getNum();
        int b = curArrayInfo.getBeg();
        int e = curArrayInfo.getEnd();
        int[] curArray = curArrayInfo.getArray();
        boolean isNumXAm1 = numFalseClusters == a-1;    // true if number of false clusters is one short of number of numbers.
        boolean isAtEnd = false;    // true if the first or the last false cell is at the end
        if (isNumXAm1)  // Check if all Xs are length of 1.
        {
            isAtEnd = true;
            for (int i = 0; i < numFalseClusters; i++)
            {                
                boolean temp = (i == 0 && begIdxFalse[i] == b) || // The first X shouldn't be at the beginning.
                        ((i == numFalseClusters-1) && (endIdxfalse[i] == e)); // The last X shouldn't be at the end.                
                isAtEnd = temp && isAtEnd;
            } // end for
        } // end if       
        // Check whether the distance between Xs are big enough for each number in the row/column array.        
        int x = curArray[s];
        boolean goodDistance;
        int p = -1, q = -1; 
        if(isNumXAm1 && !isAtEnd && numFalseClusters != 0)
        {   
            if (s == 0)
            {
                p = b-1;
                q = begIdxFalse[s];
            }
            else if (s == a-1)
            {
                p = endIdxfalse[s-1];
                q = e+1;
            }
            else
            {
                p = endIdxfalse[s-1];
                q = begIdxFalse[s];
            } // end if
            goodDistance = q - p - 1 >= x;  
            if (!goodDistance)
            {
                p = -1;
                q = -1;
            }
        } // end if
        int[] result = {p, q};
        return result;
    } // end getpq_byX
    
    private int[] getpq(Status[] arrayAnswer, ArrayInfo curArrayInfo, int s, int q)
    {
        // Determine current section (p, q), exclusive.  
        // First determine sections based on true/false cells in the current row/column.
        int[] pq = getpq_conditional(arrayAnswer,curArrayInfo,s,q);
        int p = pq[0];
        q = pq[1];
        // Find p and q with the general sectioning method.
        pq = getpq_general(curArrayInfo, s);
        int pGeneral = pq[0];
        int qGeneral = pq[1];
        
        int b = curArrayInfo.getBeg();
        int e = curArrayInfo.getEnd();
        int numF = numOfStatus(b, e, arrayAnswer, Status.False); // The number of False cells    
        
        // Determine p and q value by choosing the smallest window when there are falsed sections.
        // [4,2,2] _ _ _ _ _ _ _ _ _ _ O O _ _ _ --> s = 2 in (7,15)
        if (numF == 0)
        {
            p = pGeneral;
            q = qGeneral;
        }
        else
        {
            p = Math.max(p, pGeneral);
            q = Math.min(q, qGeneral);
        }// end if

        // If there are more true clusters than numbers in the array, use general regime.
        /* Find True cell clusters [b, e], inclusive. */
        int a = curArrayInfo.getNum();
        int[][] trueArrays = findStatusClusters(arrayAnswer, curArrayInfo, Status.True);
        int[] begIdxTrue = trueArrays[0];
        int numTrueClusters = begIdxTrue.length;
        if (numTrueClusters > a)
        {
            p = pGeneral;
            q = qGeneral;   
        } // end if
        
        // If there are a-1 number of single Xs and distance between them are sufficient, use this criteria.
        pq = getpq_byX(arrayAnswer, curArrayInfo, s);
        int pByX = pq[0];
        int qByX = pq[1];
        if (pByX != -1 && qByX != -1)
        {
            p = pByX;
            q = qByX;
        } // end if
        
        int[] result = {p, q};
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
    public boolean makeFarCellsFalse(int x, int p, int q, int s, int k, ArrayInfo curArrayInfo,
                                int numT, int sum, boolean solved,  
                                Status[] arrayAnswer)
    {   
        // Check whether there is a true cluster that belongs this section.
        int[] curTrue = getCurrentStatusCluster(x, p, q, Status.True, curArrayInfo, arrayAnswer);
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
                makeFalseBetweenTrues(p, q, s, k, curArrayInfo, arrayAnswer);
                //System.out.println("\t" + printCells(arrayAnswer)); 
                makeFalseBetweenTruesOneApart(curArrayInfo, arrayAnswer);
                //System.out.println("\t" + printCells(arrayAnswer)); 
            } // end if
        } // end if
        return solved;
    } // end makeFarCellsFalse
    
    // If there is a true cluster whose length is maximum of the numbers, make false around it
    // e.g., [1,2] _ _ _ _ _ O O _ _ _ --> _ _ _ _ X O O X _ _     
    private ArrayInfo makeFalseAround(int s, int p, int q, int k,
                                Status[] arrayAnswer, ArrayInfo curArrayInfo)
    {        
        int a = curArrayInfo.getNum();
        int b = curArrayInfo.getBeg();
        int e = curArrayInfo.getEnd();
        int[] curArray = curArrayInfo.getArray();        
        int kp = q - p - 1; // The number of empty cells between p and q   
        int x = curArray[s];    // Index of x in the array
        int sum = curArrayInfo.sumArray(0,s-1) + s;
        //int locCurTrue = getFirstStatus(p, e, arrayAnswer, Status.True);
        
        // Find True cell clusters [b, e], inclusive.
        int[][] trueArrays = findStatusClusters(arrayAnswer, curArrayInfo, Status.True);
        int[] begIdxTrue = trueArrays[0];
        int[] trueClusterLen = trueArrays[1];
        int numTrueClusters = begIdxTrue.length;
        int[] endIdxTrue = new int[numTrueClusters];
        for (int i = 0 ;i < numTrueClusters; i++)
            endIdxTrue[i] = begIdxTrue[i] + trueClusterLen[i] - 1;
        // Check whether there is a true cluster that belongs this section.
        int[] curTrue = getCurrentStatusCluster(x, p, q, Status.True, curArrayInfo, arrayAnswer);        
        int locCurTrue = curTrue[0];   // Beginning index of current True cluster (first one if there are multiple, -1 if none)
        int lenCurTrue = curTrue[1];    // Length of current True cluster       
        
        //if (locCurTrue <= e)  // when there is true cell
        if (locCurTrue != -1)  // when there is true cell
        {
            int endCurTrue = locCurTrue + lenCurTrue - 1;
            
            // When lenCurTrue is the largest number in the array or x == numT
            // Or when the number of true clusters == a && currentTrueCluster == x
            // [2, 1, 1]	_ _ _ _ _ O _ _ X O should not make false around.
            
            // Find the last true cluster whose length is the same as or less than x and in a later section.
            boolean foundLastTrueClusterLater = false; // there is later true cluster which belongs to the last number
            boolean belongsToLastNum;
            boolean foundLaterLessTrue;            
            int endLastTrue = 0;
            for (int i = 0; i < numTrueClusters; i++)
            {                
                foundLaterLessTrue = (begIdxTrue[i] > q && trueClusterLen[i] <= x);
                belongsToLastNum = endIdxTrue[i] >= e - curArray[a-1];
                boolean temp = foundLaterLessTrue && belongsToLastNum;
                if (temp)                                    
                    endLastTrue = endIdxTrue[i] + trueClusterLen[i] - 1;
                
                foundLastTrueClusterLater = foundLastTrueClusterLater || temp;                
            } // end for                        
            // [1,1,4] _ _ _ _ _ _ _ _ _ O _ O _ _ _ --> don't know
            boolean thisTrueMightBelongToLast = (endLastTrue != 0) && (endLastTrue-locCurTrue+1 <= curArray[a-1]); 
            
            // When [4,2,2]
            // _ _ _ _ _ _ _ _ _ _ O O _ _ _ --> _ _ _ _ _ _ _ _ _ X O O X _ _
            // _ _ _ _ _ _ _ _ O O _ _ _ _ _ --> _ _ _ _ _ _ _ X O O X _ _ _ _
            // _ _ _ _ _ _ _ O O _ _ _ _ _ _ -> Don't know..
            int sumFromX = getSumFromX(s, curArrayInfo, 1);            
            
            // Check if x is an absolute minimum
            boolean isAbsMin = true;
            int minNum = x;
            if (a > 1)  // when multiple numbers exist
            {
                for (int i = 0; i < a; i++)
                {
                    if (i != s)
                    {
                        isAbsMin = isAbsMin && (minNum < curArray[i]);
                        minNum = Math.min(minNum, curArray[i]);
                    } // end if
                } // end for
            } // end if
            
            // Check whether true clusters are far enough from each other
            boolean overlap = false; 
            if (numTrueClusters == a) 
            {
                // When number of numbers == number of true clusters, each of true clusters'
                // length should be smaller to respective numbers.
                boolean eachTrueMatchNum = true;
                for (int i = 0; i < numTrueClusters; i++)        
                    eachTrueMatchNum = eachTrueMatchNum & (curArray[i] >= trueClusterLen[i]);
                if (eachTrueMatchNum)
                {
                    int[][] trueRange = getTrueRange(p, q, s, k, curArrayInfo, arrayAnswer);
                    int[] idxBeg = trueRange[0];  // An array of beginning index of each number's possible range
                    int[] idxEnd = trueRange[1]; // An array of end index of each number's possible range  

                    if (a > 1)
                    {
                        for (int i = 0; i < a-1; i++)
                        {
                            int tempMax = Math.max(curArray[i], curArray[i+1]);
                            overlap = overlap || (idxBeg[i+1] <= idxEnd[i] && endIdxTrue[i+1]-begIdxTrue[i]+1 <= tempMax); 
                        }
                    } // end if
                } // end if
            } // end if
                        
            boolean isBigForTwo = (s < a-1 && kp >= x + curArray[s+1]+1)
                                || (s > 0 && kp >= x + curArray[s-1]+1)
                                || (a == 1 && kp >= x);                       
            // Only when either side of the section is closed with False
            isBigForTwo = isBigForTwo && (isPFalse(p,arrayAnswer) || isQFalse(q,k,arrayAnswer));
                                   
            // Checks whether this true belongs to x
            // [1,3,1,1,2] O X _ _ _ _ O _ X O X O _ _ _ --> don't know        
            // [1,3,1,2,1] _ _ _ _ _ _ O _ _ O _ O _ _ _ --> don't know            
            // [4,2,2] _ _ _ _ _ _ _ _ O O _ _ _ _ _ --> _ _ _ _ _ _ _ X O O X _ _ _ _
            // Earlier sections can't contain sumToX --> this true should be x.
            boolean doSumToXFitEarlier = doEarlierXsFit(s+1, locCurTrue-1, curArrayInfo, arrayAnswer);
            if (s < a-1)
                doSumToXFitEarlier = doSumToXFitEarlier && (lenCurTrue != curArray[s+1]);            
            // But if the last number is at the last section, only up to s-1 need to fit in the earlier larger empty.
            boolean doSumToXm1FitEarlier = s == a-1 && q == e+1 && doEarlierXsFit(s, locCurTrue-1, curArrayInfo, arrayAnswer);
            boolean thisTrueBelongsToX = (s < a-1 && !doSumToXFitEarlier) || 
                                         (doSumToXm1FitEarlier);// && !isBigForTwo);
            // when there is a previous number, x|s-1 to x|a-1 should not fit from locCurTrue so that previous number belongs to the earlier section.             
            if (0 < s) 
            {
                boolean doSumFromXm1FitLater = doLaterXsFit(s-2, locCurTrue-1, curArrayInfo, arrayAnswer);
                thisTrueBelongsToX = thisTrueBelongsToX && !doSumFromXm1FitLater;
            }
            
            
            if (!overlap && // [1, 7] _ _ _ _ _ O _ _ O _ _ _ _ _ _ --> Don't know  (overlap)    
                (curArrayInfo.isMax(lenCurTrue) || 
                (lenCurTrue == x && numTrueClusters == a) || // [4,1,3] O O O O _ O _ O O O _ _ _ _ _ [4,1,3] _ _ _ _ _ O O O O _ O _ O O O
                (lenCurTrue == x && s == a-1 && locCurTrue > sum && endCurTrue > e - (x + 1)) || 
                (lenCurTrue == x && foundLastTrueClusterLater && !thisTrueMightBelongToLast) || 
                (lenCurTrue == x && (endCurTrue + sumFromX >= e || q == e + 1) && !isAbsMin && (thisTrueBelongsToX)) || // && !isBigForTwo inside // [4,2,2]_ _ _ _ _ _ _ _ O O _ _ _ _ _ --> _ _ _ _ _ _ _ X O O X _ _ _ _
                (lenCurTrue == x && s == 0 && p == b + 1 && !doEarlierXsFit(s+1, locCurTrue-1, curArrayInfo, arrayAnswer)))) // [1,2,1] X X _ O _ _ _ _ _ _ _ _ _ X X --> X X X O X _ _ _ _ _ _ _ _ X X
                // [4,2,2]_ _ _ _ _ _ _ _ O O _ _ _ _ _ --> _ _ _ _ _ _ _ X O O X _ _ _ _
                // [4,1,3] _ _ _ _ _ _ _ _ _ _ O _ _ _ _ --> don't know
                // [4,1,3] _ _ _ _ _ _ _ _ _ O _ _ _ _ _  --> don't know       
                // [1,4,1] _ _ _ _ _ _ O _ _ _ _ _ X _ _ --> don't know     
                // [1,3,1,1,2] O X _ _ _ _ O _ X O X O _ _ _ --> don't know
                // [1,3,1,2,1] _ _ _ _ _ _ O _ _ O _ O _ _ _ --> don't know
                // [1,2,1] X X _ O _ _ _ _ _ _ _ _ _ X X --> X X X O X _ _ _ _ _ _ _ _ X X
                // [1,2,1] X X _ _ O _ _ _ _ _ _ _ _ X X --> X X X O X _ _ _ _ _ _ _ _ X X                
                // [2,4,2] X X _ _ O O _ _ _ _ _ _ _ X X --> X X _ X O O X _ _ _ _ _ _ X X
            {
                System.out.println("\t\tMake false around true cells.");
                if (locCurTrue-1 >= b)   // Make false right before the true cluster
                    arrayAnswer[locCurTrue-1] = Status.False;
                if (endCurTrue+1 <= e)  // Make false right after the true cluster
                    arrayAnswer[endCurTrue+1] = Status.False;
                // when first number in the array, make false to the beginning
                if (curArrayInfo.indexOf(lenCurTrue) == 0 && s == 0 && (kp < x))
                {
                    System.out.println("\t\t\tFisrt number in the array, make false to the beginning");
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
                    int iFalse = endCurTrue + 2;    // False iterator
                    while (iFalse <= e)
                    {
                        arrayAnswer[iFalse] = Status.False;
                        iFalse++;
                    } // end while                                
                    curArrayInfo.setEnd(endCurTrue);    // Update end to the last true
                    finishLastNumber(curArray, arrayAnswer, curArrayInfo);
                } // end if
                System.out.println("\t\t" + printCells(arrayAnswer)); 
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
    private int getEndIdxCurStatusCluster(int firstStatus, int e, Status[] arrayAnswer, Status stat)
    {        
        int lastStatus = firstStatus - 1;
        while(lastStatus + 1 <= e && arrayAnswer[lastStatus + 1] == stat)
            lastStatus++;
        return lastStatus;
    } // end getEndIdxCurStatusCluster
    
    // Returns the index of the first occurrence of the not-Status stat between p+1 and e.
    // e.g., X _ O _ O O _ X _ _ --> Finding first not-false, p = 0, e = 9 --> firstNotStatus = 1
    private int getFirstNotStatus(int p, int e, Status[] arrayAnswer, Status stat)
    {
        int firstNotStatus = p+1;
        while (firstNotStatus < e+1 && arrayAnswer[firstNotStatus] == stat)
            firstNotStatus++;
        return firstNotStatus;
    } // end getFirstNotStatus
    
    // Returns the index of the last occurrence of the not-Status stat between b and q-1.
    // e.g., X _ O _ O O _ X _ _  --> firstNotStatus = 1, q = 9 --> lastStatus = 8
    private int getLastNotStatus(int b, int q, Status[] arrayAnswer, Status stat)
    {        
        int lastNotStatus = q-1;
        while(lastNotStatus >= b && arrayAnswer[lastNotStatus] == stat)
            lastNotStatus--;
        return lastNotStatus;
    } // end getLastNotStatus
    
    // Returns the index of the last occurrence of the not-Status cluster, starting from firstStatus
    // e.g., X _ O _ O O _ X _ _ --> firstNotStatus = 1, e = 9 --> endIdxCurNotStatus = 2
    private int getEndIdxCurNotStatusCluster(int firstNotStatus, int e, Status[] arrayAnswer, Status stat)
    {        
        int endIdxCurNotStatus = firstNotStatus - 1;
        while(endIdxCurNotStatus + 1 <= e && arrayAnswer[endIdxCurNotStatus + 1] != stat)
            endIdxCurNotStatus++;
        return endIdxCurNotStatus;
    } // end getEndIdxCurNotStatusCluster
    
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
        return findStatusClusters(b, e, arrayAnswer, stat);
    } // end findStatusClusters
    
    // Returns an array of beginning index of stat cells and 
    //         an array of length of the stat clusters at [b, e], inclusive.
    private int[][] findStatusClusters(int b, int e, Status[] arrayAnswer, Status stat)
    {        
        ArrayList<Integer> begStatusIdx_temp = new ArrayList<>();
        ArrayList<Integer> lenStatus_temp = new ArrayList<>();        
        
        int locCurStatus, lastCurStatus, lenCurStatus;
        int i = b;
        while (i <= e)
        {
            locCurStatus = getFirstStatus(i - 1, e, arrayAnswer, stat);
            lastCurStatus = getEndIdxCurStatusCluster(locCurStatus, e, arrayAnswer, stat);            
            lenCurStatus = lastCurStatus - locCurStatus + 1;
            if (lenCurStatus > 0)
            {
                begStatusIdx_temp.add(locCurStatus);
                lenStatus_temp.add(lenCurStatus);
            } // end if
            i = lastCurStatus + 1;
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
    
    
    // Returns an array of beginning index of not-stat cells and 
    //         an array of length of the not-stat clusters at [b, e], inclusive.
    // O _ O _ _ X _ X O X X X X _ _ --> [[0,6,8,13],[5,1,1,2]] for stat = Status.False
    private int[][] findNotStatusClusters(int b, int e, Status[] arrayAnswer, Status stat)
    {        
        ArrayList<Integer> begNotStatusIdx_temp = new ArrayList<>();
        ArrayList<Integer> lenNotStatus_temp = new ArrayList<>();        
        
        int locCurNotStatus, lastCurNotStatus, lenCurNotStatus;
        int i = b;
        while (i <= e)
        {
            locCurNotStatus = getFirstNotStatus(i - 1, e, arrayAnswer, stat);
            lastCurNotStatus = getEndIdxCurNotStatusCluster(locCurNotStatus, e, arrayAnswer, stat);            
            lenCurNotStatus = lastCurNotStatus - locCurNotStatus + 1;
            if (lenCurNotStatus > 0)
            {
                begNotStatusIdx_temp.add(locCurNotStatus);
                lenNotStatus_temp.add(lenCurNotStatus);
            } // end if
            i = lastCurNotStatus + 1;
        } // end while
        int arrayLen = begNotStatusIdx_temp.size();
        int[] begNotStatusIdx = new int[arrayLen];
        int[] lenNotStatus = new int[arrayLen];
        for (i = 0; i < arrayLen; i++)
        {
            begNotStatusIdx[i] = begNotStatusIdx_temp.get(i);
            lenNotStatus[i] = lenNotStatus_temp.get(i);
        } // end for
        
        int[][] statArrays = {begNotStatusIdx, lenNotStatus};
        return statArrays;
    } // end findNotStatusClusters
    
    
    // Check whether there is a status cluster that belongs this section (p, q), exclusive.
    // Returns an array of integers containing the location and length of the current true cluster.
    // If multiple exist, it's either the first occurance or the one with the same length as x.
    private int[] getCurrentStatusCluster(int x, int p, int q, Status stat, ArrayInfo curArrayInfo, Status[] arrayAnswer)
    {
         /* Find Status cell clusters [b, e], inclusive. */
        int[][] statArrays = findStatusClusters(arrayAnswer, curArrayInfo, stat);
        int[] begIdxStat = statArrays[0];
        int[] statClusterLen = statArrays[1];
        int numStatClusters = begIdxStat.length;
        int locCurStat = -1;   // Beginning index of Stat cluster (first one if there are multiple, -1 if none)
        int lenCurStat = 0;    // Length of Stat cluster        
        int numCurStat = 0;    // number of stat clusters in the current section        
        int head = -1;  // Index of the first stat cell in the current section
        int toe = -1;  // Index of the last stat cell in the current section
        int fromHeadToToe = toe - head; // The length from the first to the last stat cell in the current section.
        boolean foundStatCluster;// = false;
        boolean foundValidSameLen = false;        
         // if there is a stat cluster whose length is the same as x, that will be the current stat cluster.
        int i = 0; 
        while (i < numStatClusters && !foundValidSameLen)
        //for (int i = 0; i < numStatClusters; i++)
        {
            foundStatCluster = p < begIdxStat[i] && begIdxStat[i] < q;    
            boolean foundSameLen = statClusterLen[i] == x;
            foundValidSameLen = foundStatCluster && foundSameLen;
            if (foundStatCluster)
            {   // First occurance or found the same length cluster
                if (numCurStat == 0 || foundSameLen)
                {
                    locCurStat = begIdxStat[i];
                    lenCurStat = statClusterLen[i];                    
                } // end if
                numCurStat++;
                
                // Update the first and the last stat cell.
                if (numCurStat == 1)    // found the first stat cluster
                {
                    head = locCurStat;
                    toe = locCurStat + lenCurStat - 1;
                }
                else if (numCurStat > 1) // When more than one found, choose the last stat cell in the section.                
                    toe = locCurStat + lenCurStat - 1;
                
                fromHeadToToe = toe - head + 1;
            } // end if
            i++;
        } // end while
        int[] result = {locCurStat, lenCurStat, numCurStat, fromHeadToToe};
        return result;
    } // end getCurrentStatusCluster
   
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
            //b = curArrayInfo.getBeg();
            curArray = curArrayInfo.getArray();
            be = updateEnds(b, e, curArrayInfo, arrayAnswer);
            b = be[0];
        } // end while
        be = updateEnds(b, e, curArrayInfo, arrayAnswer);        
        b = be[0];
        e = be[1];
        // When the last cell is filled
        while (e >= b && arrayAnswer[e+1-1] == Status.True)   
        {
            finishLastNumber(curArray, arrayAnswer, curArrayInfo);
            //e = curArrayInfo.getEnd();
            curArray = curArrayInfo.getArray();
            be = updateEnds(b, e, curArrayInfo, arrayAnswer);
            e = be[1];
        } // end while
        be = updateEnds(b, e, curArrayInfo, arrayAnswer);            
        return be;
    } // end removeEnds
    
    
    /* Make remaining empty cells False when x is the last. */
    // When x is the last number in the array and there are still 
    // empty clusters whose length is less than x in later sections,
    // make them false.
    // Precondition: s == a-1
    private void makeLaterEmptyFalse(int s, int x, int p, int q, ArrayInfo curArrayInfo, Status[] arrayAnswer)
    {            
        int[] curArray = curArrayInfo.getArray();
        int b = curArrayInfo.getBeg();
        int e = curArrayInfo.getEnd();
        int numT = numOfStatus(b, q-1, arrayAnswer, Status.True);     // The number of True cells from beginning to current section
        
        // Find True cell clusters [b, e], inclusive.
        int[][] trueArrays = findStatusClusters(arrayAnswer, curArrayInfo, Status.True);
        int[] begIdxTrue = trueArrays[0];
        int[] trueClusterLen = trueArrays[1];

        // Find Empty cell clusters [b, e], inclusive.
        int[][] emptyArrays = findStatusClusters(arrayAnswer, curArrayInfo, Status.Empty);
        int[] begIdxEmpty = emptyArrays[0];
        int[] emptyClusterLen = emptyArrays[1];

        // Check whether there is a true cluster that belongs this section.
        int[] curTrue = getCurrentStatusCluster(x, p, q, Status.True, curArrayInfo, arrayAnswer);
        int locCurTrue = curTrue[0];   // Beginning index of current True cluster (first one if there are multiple, -1 if none)
        int lenCurTrue = curTrue[1];    // Length of current True cluster
        
        // Check whether there is a true cluster in this section
        boolean foundThisTrue = (locCurTrue != -1);                        
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
                (curBegIdxEmpty > 0 && arrayAnswer[curBegIdxEmpty-1] != Status.True) && // Shouldn't follow right after a true cluster
                ((foundLaterSmallerEmpty && numT != 0) || (foundThisTrue && s > 0 && curArray[s-1] != x))) // when true cluster is found, we don't know whether x belongs to this cluster if x is the same as previous number
                // [1,2,1,2] _ _ _ _ _ O X O _ X _ _ _ _ _ --> don't know
                // [2, 3] _ _ X _ _ _ X X _ _ X _ _ _ _ --> don't know
            {
                System.out.println("\t\tMake later sections false");
                for (int ii = curBegIdxEmpty; ii < curBegIdxEmpty + curEmptyClusterLen; ii++)
                    if (arrayAnswer[ii] == Status.Empty)
                        arrayAnswer[ii] = Status.False;     
                System.out.println("\t\t" + printCells(arrayAnswer)); 
            } // end if                            
        } // end for 
    } // end makeLaterEmptyFalse
    
    private void makeFalseBetweenTrues(int p, int q, int s, int k, ArrayInfo curArrayInfo, Status[] arrayAnswer)
    {       
        int b = curArrayInfo.getBeg();
        int e = curArrayInfo.getEnd();
        int a = curArrayInfo.getNum();
        int[] curArray = curArrayInfo.getArray();
        
        // Find True cell clusters [b, e], inclusive.
        int[][] trueArrays = findStatusClusters(arrayAnswer, curArrayInfo, Status.True);
        int[] begIdxTrue = trueArrays[0];
        int[] trueClusterLen = trueArrays[1];        
        int numTrueClusters = begIdxTrue.length;
        int[] endIdxTrue = new int[numTrueClusters];
        for (int i = 0 ;i < numTrueClusters; i++)
            endIdxTrue[i] = begIdxTrue[i] + trueClusterLen[i] - 1;
        
        int x; // current number
        // When the number of true clusters is same as number of numbers in array
        // And only when there are multiple numbers (single number is dealt in makeFarCellsFalse)        
        if (numTrueClusters == a) 
        {      
            // When number of numbers == number of true clusters, each of true clusters'
            // length should be smaller to respective numbers.
            boolean eachTrueMatchNum = true;
            for (int i = 0; i < numTrueClusters; i++)        
                eachTrueMatchNum = eachTrueMatchNum & (curArray[i] >= trueClusterLen[i]);
            
            if (eachTrueMatchNum)
            {
                int[][] trueRange = getTrueRange(p, q, s, k, curArrayInfo, arrayAnswer);
                int[] idxBeg = trueRange[0];  // An array of beginning index of each number's possible range
                int[] idxEnd = trueRange[1]; // An array of end index of each number's possible range  

                // Check whether true clusters are far enough from each other
                boolean overlap = false; // false when a == 1
                if (a > 1)
                {                
                    for (int i = 0; i < a-1; i++)
                    {
                        int tempMax = Math.max(curArray[i], curArray[i+1]);
                        overlap = overlap || (idxBeg[i+1] <= idxEnd[i] && endIdxTrue[i+1]-begIdxTrue[i]+1 <= tempMax); 
                    } // end for
                } // end if

                if (!overlap)
                {
                    System.out.println("\t\tMake false between true clusters.");
                    // Section 1: 
                    x = curArray[0];  // current number    
                    System.out.println("\t\t\tCurrent number: " + x);
                    // Make false around possible true range
                    for (int i = b; i < idxBeg[0]; i++)   // Make false before the range (only for first x)
                        arrayAnswer[i] = Status.False;
                    if (a > 1)
                    {
                        for (int i = idxEnd[0]+1; i < idxBeg[1]; i++)   // Make false after the range
                            arrayAnswer[i] = Status.False;
                        System.out.println("\t\t" + printCells(arrayAnswer)); 

                        if (a > 2)  // When there 3 or more, consider middle sections
                        {                    
                            for (int si = 1; si < a-1; si++) // loop through numbers in the array (sections)
                            {   // Section si:               
                                x = curArray[si];    // current number         
                                System.out.println("\t\t\tCurrent number: " + x);
                                for (int i = idxEnd[si]+1; i < idxBeg[si+1]; i++) // Make false after the range
                                    arrayAnswer[i] = Status.False;                      
                                System.out.println("\t\t" + printCells(arrayAnswer));                     
                            } // end for
                        } // end if
                    } // end if
                    // Last section: 
                    x = curArray[a-1];            
                    System.out.println("\t\t\tCurrent number: " + x);
                    for (int i = idxEnd[a-1]+1; i < e+1; i++) // Make false after the range
                        arrayAnswer[i] = Status.False;              
                    System.out.println("\t\t" + printCells(arrayAnswer));             
                } // end if
            } // end if
        }
        else    // when number of true cluster != number of numbers in array
        {
            // Find the longest true cluster            
            int longestTrueBeg = begIdxTrue[0]; // Beginning index of longest True cluster (first one if there are multiple, -1 if none)
            int longestTrueLen = trueClusterLen[0]; // Length of longest True cluster
            for (int i = 0; i < numTrueClusters; i++)
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
                System.out.println("\t\t\tCurrent number: " + x);
                System.out.println("\t\tMake false to the end.");
                for (int i = idxEnd+1; i < e+1; i++) // Make false after the range                                    
                    arrayAnswer[i] = Status.False;                    
                System.out.println("\t\t" + printCells(arrayAnswer)); 
                
                // Make false around second largest true cluster
                // e. g.,  _ O _ _ _ O O O O _ -> X O X _ _ O O O O _ 
                if (numTrueClusters > 1)
                {
                    for (int i = 0; i < numTrueClusters-1; i++)
                    {
                        if (trueClusterLen[i] == largestExceptLast)
                        {                            
                            if (begIdxTrue[i]-1 >= b)   
                            {   // Make false right before the true cluster
                                System.out.println("\t\tMake false around true cluster.");
                                arrayAnswer[begIdxTrue[i]-1] = Status.False;
                            }
                            if (endIdxTrue[i]+1 <= e)  // Make false right after the true cluster
                                arrayAnswer[endIdxTrue[i]+1] = Status.False;
                            System.out.println("\t\t" + printCells(arrayAnswer)); 
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
                System.out.println("\t\t\tCurrent number: " + x);
                // Make false around possible true range
                System.out.println("\t\tMake false to the front.");
                for (int i = b; i < idxBeg; i++)   // Make false before the range (only for first x)    
                    arrayAnswer[i] = Status.False;                                    
                System.out.println("\t\t" + printCells(arrayAnswer)); 
                
                // Make false around second largest true cluster
                // e. g., _ O O O O _ _ O _ _ -> _ O O O O _ X O X _
                if (numTrueClusters > 1)
                {
                    for (int i = 1; i < numTrueClusters; i++)
                    {
                        if (trueClusterLen[i] == largestExceptFirst)
                        {                            
                            if (begIdxTrue[i]-1 >= b)   // Make false right before the true cluster
                            {
                                System.out.println("\t\tMake false around true cluster.");
                                arrayAnswer[begIdxTrue[i]-1] = Status.False;
                            }
                            if (endIdxTrue[i]+1 <= e)  // Make false right after the true cluster
                                arrayAnswer[endIdxTrue[i]+1] = Status.False;
                            System.out.println("\t\t" + printCells(arrayAnswer)); 
                        } // end if
                    } // end for
                } // end if
            } // end if
            
            // Check whether there is a true cluster that belongs this section.
            x = curArray[s];
            int[] curTrue = getCurrentStatusCluster(x, p, q, Status.True, curArrayInfo, arrayAnswer);
            int locCurTrue = curTrue[0];   // Beginning index of current True cluster (first one if there are multiple, -1 if none)
            int lenCurTrue = curTrue[1];    // Length of current True cluster
            int kp = q - p - 1;            
            int sumToX = getSumToX(s, curArrayInfo, 1); // Summation of numbers + 1 space from first number to x            
            int sumFromX = getSumFromX(s, curArrayInfo, 1); // Summation of numbers + 1 space from first number to x            
            int remainingT = x - lenCurTrue;
            // If x belongs to this true cluster, make false to the beginning of the section
            //if (sumToX > kp && sumFromX > kp && locCurTrue != -1)
            //{                                
            //    System.out.println("\tMake far cells false.");
            //    for (int i = p+1; i < locCurTrue - remainingT; i++)   // Make false before the range    
            //        arrayAnswer[i] = Status.False;                                    
            //    for (int i = locCurTrue + lenCurTrue +remainingT; i < q; i++)   // Make false after the range   
            //        arrayAnswer[i] = Status.False;   
            //    System.out.println("\t" + printCells(arrayAnswer)); 
            //} // end if
             
            boolean isBigForTwo = (s < a-1 && kp >= x + curArray[s+1]+1)
                                    || (s > 0 && kp >= x + curArray[s-1]+1)
                                    || (a == 1 && kp >= x);
            // [1, 4, 2] _ _ _ O O O _ _ X _ _ _ _ _ _  --> _ _ _ O O O _ X X _ _ _ _ _ _            
            if (locCurTrue != -1 && q <= e && arrayAnswer[q] == Status.False && 
                (s > 0 && curArray[s-1] < lenCurTrue) && 
                (s < a - 1 && q <= locCurTrue + x + curArray[s+1]))// [1,2,1,2] _ _ _ _ O O _ _ _ X _ _ _ _ _ --> don't know
            {
                System.out.println("\t\tMake far cells false.");
                for (int i = locCurTrue + lenCurTrue +remainingT; i < q; i++)   // Make false after the range   
                    arrayAnswer[i] = Status.False;         
                System.out.println("\t\t" + printCells(arrayAnswer)); 
            }            
            
            // [4, 5] _ _ _ _ _ _ X _ _ _ O O O O _  -->  _ _ _ _ _ _ X X X _ O O O O _
            // [2, 3, 3] _ _ _ _ _ _ X _ _ _ _ O O _ X --> don't know
            if (locCurTrue != -1 && p >= b && arrayAnswer[p] == Status.False && (s > 0 && kp < sumToX) && !isBigForTwo)
            {
                System.out.println("\t\tMake far cells false.");
                for (int i = p+1; i < locCurTrue - remainingT; i++)   // Make false after the range   
                    arrayAnswer[i] = Status.False;         
                System.out.println("\t\t" + printCells(arrayAnswer)); 
            }
            
        }// end if
    } // end makeFalseBetweenTrues
    
    // Make an empty space false between two true clusters.
    private void makeFalseBetweenTruesOneApart(ArrayInfo curArrayInfo, Status[] arrayAnswer)
    {           
        int a = curArrayInfo.getNum();
        int[] curArray = curArrayInfo.getArray();
        int xMax = curArray[0]; // Largest number in the array
        for (int i = 0; i < a; i++)
            xMax = Math.max(xMax, curArray[i]);
        
        // Find True cell clusters [b, e], inclusive.
        int[][] trueArrays = findStatusClusters(arrayAnswer, curArrayInfo, Status.True);
        int[] begIdxTrue = trueArrays[0];
        int[] trueClusterLen = trueArrays[1];
        int numTrueClusters = begIdxTrue.length;
        int[] endIdxTrue = new int[numTrueClusters];
        for (int i = 0; i < numTrueClusters;i++)
            endIdxTrue[i] = begIdxTrue[i] + trueClusterLen[i] - 1;
        
        for (int i = 0; i < numTrueClusters-1; i++)
        {
            int gap = begIdxTrue[i+1] - endIdxTrue[i] - 1;    // gap between this and next true clusters
            // When the gap is only 1 empty cell && combined length of this and 
            // next true cluster is greather than maximum number in the array
            if (gap == 1 && (endIdxTrue[i+1] - begIdxTrue[i] + 1> xMax))
            {                
                if (arrayAnswer[begIdxTrue[i+1]-1] == Status.Empty)
                {
                    arrayAnswer[begIdxTrue[i+1]-1] = Status.False;
                    System.out.println("\t\tThe two true clusters are separate.");
                    System.out.println("\t\t" + printCells(arrayAnswer)); 
                }
            }
        } // end for
    } // end makeFalseBetweenTruesOneApart
    
    // Make an empty space true between two true clusters.
    // [1, 8] _ _ _ _ O O _ O O O _ _ _ _ _ --> 	_ _ _ _ O O O O O O _ _ _ _ _ 
    // [1, 8] _ _ _ _ O O _ _ _ O O O _ _ _ --> 	_ _ _ _ O O O O O O O O _ _ _ 
    // [4, 3] X X _ _ _ O _ O O _ _ O O _ _ --> X X _ _ _ O O O O _ _ O O _ _ 
    // [1,5,4] _ _ _ O _ O O _ O O _ _ O _ _  --> _ _ _ O _ O O O O O _ _ O _ _ 
    private void makeTrueBetweenTrues(ArrayInfo curArrayInfo, Status[] arrayAnswer)
    {           
        int a = curArrayInfo.getNum();
        int[] curArray = curArrayInfo.getArray();
        int[] maxInArray = findMax(curArray);
        int xMax = maxInArray[0];// Largest number in the array
        int maxIdx = maxInArray[1];   // Index of the largest number in the array
        
        // Find True cell clusters [b, e], inclusive.
        int[][] trueArrays = findStatusClusters(arrayAnswer, curArrayInfo, Status.True);
        int[] begIdxTrue = trueArrays[0];
        int[] trueClusterLen = trueArrays[1];
        int numTrueClusters = begIdxTrue.length;
        int[] endIdxTrue = new int[numTrueClusters];
        for (int i = 0; i < numTrueClusters;i++)
            endIdxTrue[i] = begIdxTrue[i] + trueClusterLen[i] - 1;
        
        int b = curArrayInfo.getBeg();
        int e = curArrayInfo.getEnd();
        // Check true clusters within the possible true range for each number.        
        for (int s = 0; s < a; s++)
        {
            int x = curArray[s];                       
            int[] pq = getpq_general(curArrayInfo, s);            
            int p = pq[0];
            int q = pq[1];
            if (p-1 >= b && arrayAnswer[p-1] == Status.False)
                p--;
            if (q+1 <= e && arrayAnswer[q+1] == Status.False)
                q++;
            int kp = q - p - 1;            
            
            int numTrueThis = 0; // Count the number of true clusters in this section
            int firstIdx = -1;  // The first location of true cluster in this section
            int lastIdx = -1;
            for (int it = 0; it < numTrueClusters; it++)
            {
                if (p < begIdxTrue[it] && begIdxTrue[it] < q)
                {
                    numTrueThis++;
                    lastIdx = it;
                    if (numTrueThis == 1)
                        firstIdx = it;
                }
            } // end for
            
            if (numTrueThis >= 2)   // Consider only when there are multiple true clusters.
            {
                int[] combinedLengthList = new int[numTrueThis-1];
                int numOfLength = 0;
                for (int it = firstIdx; it < lastIdx; it++)
                {
                    // Length from it_th to (it+1)_th true cluster
                    int lengthTwoTrue = endIdxTrue[it+1] - begIdxTrue[it] + 1; 
                    combinedLengthList[numOfLength] = lengthTwoTrue;
                    numOfLength++;
                } // end for
                
                // Choose the pair that makes the largest true cluster                
                int lenMaxIdx = 0; // Largest number in the array  
                int lenMax = combinedLengthList[lenMaxIdx]; // Index of the largest number in the array                       
                for (int ii = 0; ii < numTrueThis-1; ii++)
                {
                    int curNum = combinedLengthList[ii];                    
                    if (lenMax <= curArray[s])
                    {
                        if (curNum > lenMax)
                        {
                            lenMaxIdx = ii;
                            lenMax = curNum;
                        } // end if
                    } 
                    else
                    {
                        lenMaxIdx = ii;
                        lenMax = curNum;
                    }// end if
                } // end for
                
                int idxToLook = firstIdx + lenMaxIdx;   // Choose this and the next true clusters
                
                int gap = begIdxTrue[idxToLook+1] - endIdxTrue[idxToLook] - 1;    // gap between this and next true clusters
                // Check if there is an empty cluster between the two true clusters.
                boolean allEmpty = true;
                for (int ii = endIdxTrue[idxToLook]+1; ii < begIdxTrue[idxToLook+1]; ii++)
                    allEmpty = allEmpty && arrayAnswer[ii] == Status.Empty;
                // True if true clusters are bigger than numbers other than xMax.
                boolean biggerThanAll = true;
                for (int ia = 0; ia < a; ia++)
                {
                    if (ia != maxIdx)
                        biggerThanAll =  biggerThanAll && (curArray[ia] < trueClusterLen[idxToLook]) && (curArray[ia] < trueClusterLen[idxToLook+1]);
                } // end for
                // Length from ith to (idxToLook+1)th true cluster
                int lengthTwoTrue = endIdxTrue[idxToLook+1] - begIdxTrue[idxToLook] + 1;                 
                
                boolean isBigForTwo = (s < a-1 && kp >= x + curArray[s+1]+1)
                                    || (s > 0 && kp >= x + curArray[s-1]+1)
                                    || (a == 1 && kp >= x);
                // When the gap is only 1 empty cell && combined length of this and 
                // next true cluster is less than largest number in the array   
                // [10, 2] _ _ O O O O O O O O _ O X _ _ --> _ _ O O O O O O O O O O X _ _
                // [3, 2, 2, 2] X _ _ O _ O _ X X X _ _ _ _ _ --> X _ _ O _ O _ X X X _ _ _ _ _
                // [3, 6] X _ _ O _ O _ _ _ O _ _ _ _ _ --> don't know
                // X _ _ _ _ _ _ O _ O _ O _ _ _ --> X _ _ _ _ _ _ O _ O O O _ _ _
                // X _ _ _ _ _ O _ O _ _ O _ _ _ --> X _ _ _ _ _ O _ O O O O _ _ _
                // X _ _ _ O _ O _ _ _ O _ _ _ _ --> X _ _ _ _ _ O _ O O O O _ _ _
                if ((lengthTwoTrue <= xMax) && allEmpty && 
                    (biggerThanAll || (a < numTrueClusters && begIdxTrue[idxToLook]-p < x && x > q - begIdxTrue[idxToLook+1]) || 
                    (!isBigForTwo && 
                     !(e - endIdxTrue[idxToLook+1] <= x && lengthTwoTrue > x) && // _ _ _ _ _ _ _ _ _ _ O _ O _ X
                     (e - begIdxTrue[idxToLook+1]+1 < x)))) // [1, 3, 6] _ _ _ _ _ _ _ O _ O O O O _ _
                {                       
                    arrayAnswer[begIdxTrue[idxToLook+1]-1] = Status.True;                    
                    System.out.println("\t\tThe two true clusters are together.");
                    System.out.println("\t\tCurrent number: " + x); 
                    System.out.println("\t\tp = " + p + ", q = " + q + ", k' = " + kp);
                    System.out.println("\t\t" + printCells(arrayAnswer)); 
                }
            } // end if

            // Update True cell clusters [b, e], inclusive.
            trueArrays = findStatusClusters(arrayAnswer, curArrayInfo, Status.True);
            begIdxTrue = trueArrays[0];
            trueClusterLen = trueArrays[1];
            numTrueClusters = begIdxTrue.length;
            endIdxTrue = new int[numTrueClusters];
            for (int ii = 0; ii < numTrueClusters;ii++)
                endIdxTrue[ii] = begIdxTrue[ii] + trueClusterLen[ii] - 1;            
        } // end for                
    } // end makeTrueBetweenTrues
    
    
    
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
                System.out.println("\t\tMake first few cells true.");
                for (int i = endIdx0 + 1; i < b + x0; i++)
                    arrayAnswer[i] = Status.True;
                System.out.println("\t\t" + printCells(arrayAnswer)); 
            } // end if
            
            int begIdxe = begIdxTrue[numTrueClusters-1];
            int endIdxe = begIdxe + trueClusterLen[numTrueClusters-1] - 1;
            int xe = curArrayInfo.getArray()[a-1];            
            if ((trueClusterLen[numTrueClusters-1] < xe) && (begIdxe > e - xe + 1) && (begIdxe - 1 > e - xe) )
            {
                System.out.println("\t\tMake last few cells true.");
                for (int i = begIdxe - 1; i > e - xe; i--)
                    arrayAnswer[i] = Status.True;
                System.out.println("\t\t" + printCells(arrayAnswer)); 
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
                System.out.println("\t\tMake first few cells false.");
                for (int i = b; i < begIdx0 - remainingT0; i++)
                    arrayAnswer[i] = Status.False;
                System.out.println("\t\t" + printCells(arrayAnswer)); 
            } // end if
            
            int begIdxe = begIdxTrue[numTrueClusters-1];
            int endIdxe = begIdxe + trueClusterLen[numTrueClusters-1] - 1;
            int xe = curArrayInfo.getArray()[a-1];     
            int remainingTe = xe - trueClusterLen[numTrueClusters-1];
            if ((trueClusterLen[numTrueClusters-1] < xe) && (endIdxe >= e - xe) && (e > endIdxe + remainingTe))
            {
                System.out.println("\t\tMake last few cells false.");
                for (int i = e; i > endIdxe + remainingTe; i--)
                    arrayAnswer[i] = Status.False;
                System.out.println("\t\t" + printCells(arrayAnswer)); 
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
        
        System.out.println("\t\tFill up by general rules...");
        int x = curArray[0];  // current number    
        System.out.println("\t\t\tCurrent number: " + x);
        // Fill up the definite true cells
        // Section 1         
        toFill = 2 * x - (ke - ((sum - x) + (a - 1))); // number of cells to make true
        sumX = 0;  // sum of numbers from x_0 to x_(i-1)
        i0 = b + sumX + x - toFill;
        is = b + sumX + x;
        for (int i = i0; i < is; i++)
            arrayAnswer[i] = Status.True;
        System.out.println("\t\t" + printCells(arrayAnswer)); 
        
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
                    System.out.println("\t\t\tCurrent number: " + x);
                    for (int i = i0; i < is; i++)
                        arrayAnswer[i] = Status.True;                      
                    System.out.println("\t\t" + printCells(arrayAnswer));                     
                } // end for
            } // end if
            // Last section
            sumX += x;
            x = curArray[a-1];  
            toFill = 2 * x - (ke - ((sum - x) + (a - 1)));                     
            i0 = b + sumX + (a-1) + x - toFill;
            is = b + sumX + (a-1) + x;
            System.out.println("\t\t\tCurrent number: " + x);
            for (int i = i0; i < is; i++)
                arrayAnswer[i] = Status.True;  
            System.out.println("\t\t" + printCells(arrayAnswer)); 
        } // end if        
    }// end fillUp    
    
    // When the number of true clusters is same as number of numbers in array
    // And only when there are multiple numbers (single number is dealt in makeFarCellsFalse)        
    // (Precondition:  numTrueClusters == a && eachTrueMatchNum)
    // Returns a 2D array containing beginning and end index of possible range for each true cluster
    private int[][] getTrueRange(int p, int q, int s, int k, ArrayInfo curArrayInfo, Status[] arrayAnswer)
    {
        int a = curArrayInfo.getNum();
        int b = curArrayInfo.getBeg();
        int e = curArrayInfo.getEnd();
        int[] curArray = curArrayInfo.getArray();
        int[] idxBeg = new int[a];  // An array of beginning index of each number's possible range
        int[] idxEnd = new int[a]; // An array of end index of each number's possible range            
        
        // Find True cell clusters [b, e], inclusive.
        int[][] trueArrays = findStatusClusters(arrayAnswer, curArrayInfo, Status.True);
        int[] begIdxTrue = trueArrays[0];
        int[] trueClusterLen = trueArrays[1];        
        int numTrueClusters = begIdxTrue.length;
        int[] endIdxTrue = new int[numTrueClusters];
        for (int i = 0; i < numTrueClusters;i++)
            endIdxTrue[i] = begIdxTrue[i] + trueClusterLen[i] - 1;
        
        for (int i = 0; i < a; i++) // Find beginning & end index of possible true range
        {
            int x = curArray[i];  // current number
            int curTrueBeg = begIdxTrue[i]; // Beginning index of current True cluster (first one if there are multiple, -1 if none)
            int curTrueLen = trueClusterLen[i]; // Length of current True cluster
            int curTrueEnd = endIdxTrue[i]; // Last index of current True cluster 
            int remainingT = x - curTrueLen; // Remaining number of true

            int curIdxBeg = Math.max(b,curTrueBeg - remainingT);
            int curIdxEnd = Math.min(e,curTrueEnd + remainingT);     
            
            // [4, 1, 3] _ _ _ O _ X O _ _ X O O O X X --> for x = 4 [0,4], for x=1 [6,6]
            // [1,2,3,1] O X _ _ _ X O _ _ X O O _ X O --> for x = 2 [6,7], for x = 3 [10,12]
            if (i == s && p < curTrueBeg && curTrueBeg < q)
            {
                if (isPFalse(p, arrayAnswer))
                    curIdxBeg = Math.max(curIdxBeg, p+1);
                if (isQFalse(q, k, arrayAnswer))
                    curIdxEnd = Math.min(curIdxEnd, q-1);
            } // end if
            idxBeg[i] = curIdxBeg;
            idxEnd[i] = curIdxEnd;
        } // end for
        
        int[][] result = {idxBeg, idxEnd};
        return result;
    }// end getTrueRange
    
    // Returns the sum of number from the first number to x.
    // If space == 1, returns the least amount of space that numbers from the first to here would take.
    //                i.e., sum from the first to sth number in the array + 1 space for each.
    // If space == 0, it is simply sum from 1st to sth number in the array.
    private int getSumToX(int s, ArrayInfo curArrayInfo, int space)
    {        
        return getSumFromAToB(0, s, curArrayInfo, space);
    } // end getSumToX
    
    // Returns the sum of number from x to the last number. 
    // If space == 1, returns the least amount of space that numbers from here to the last would take.
    //                i.e., sum from the sth to the last number in the array + 1 space for each.
    // If space == 0, it is simply sum from sth to last number in the array.
    private int getSumFromX(int s, ArrayInfo curArrayInfo, int space)
    {        
        int a = curArrayInfo.getNum();
        return getSumFromAToB(s, a-1, curArrayInfo, space);
    } // end getSumFromX
    
    // Returns the sum of number from a to the b.
    // If space == 1, returns the least amount of space that numbers from here to the last would take.
    //                i.e., sum from the sth to the last number in the array + 1 space for each.
    // If space == 0, it is simply sum from sth to last number in the array.
    private int getSumFromAToB(int s, int t, ArrayInfo curArrayInfo, int space)
    {
        int[] curArray = curArrayInfo.getArray();
        int a = curArrayInfo.getNum();
        int sumFromX = 0;   // Summation of numbers + 1 space from x to the end in the array
        for (int i = s; i <= t; i++)
            sumFromX += curArray[i] + space;
        sumFromX -= space;  // last number doesn't need one space
        return sumFromX;
    } // end getSumFromX
    
    private boolean closedWithFalse(int p, int q, int k, Status[] arrayAnswer)
    {
        return isPFalse(p, arrayAnswer) && isQFalse(q, k, arrayAnswer);
    } // end closedWithFalse
    // Returns true if arrayAnswer[p] is False or the beginning of the array
    private boolean isPFalse(int p, Status[] arrayAnswer)
    {
        return (p == -1 || arrayAnswer[p] == Status.False);
    } // end isPFalse
    // Returns true if arrayAnswer[q] is False or the end of the array
    private boolean isQFalse(int q, int k, Status[] arrayAnswer)
    {
        return (q == k || arrayAnswer[q] == Status.False);
    } // end isQFalse
    
    // Returns true if the section (p,q) is filled with true cells. 
    private boolean fullOfTrue(int p, int q, Status[] arrayAnswer)
    {
        boolean result = true;
        for (int i = p+1; i < q; i++)
            result = result && arrayAnswer[i] == Status.True;
        return result;
    } // end fullOfTrue
    
    // Returns true if there is at least one true cell in the section (p,q).
    private boolean trueExists(int p, int q, Status[] arrayAnswer)
    {
        boolean result = false;
        for (int i = p+1; i < q; i++)
            result = result || (arrayAnswer[i] == Status.True);
        return result;
    } // end fullOfTrue
    
    // Returns the largest number in an integer array and its location.
    private int[] findMax(int[] curArray)
    {
        int maxIdx = 0; // Index of the largest number in the array
        int xMax = curArray[maxIdx]; // Largest number in the array        
        int a = curArray.length;
        for (int i = 0; i < a; i++)
        {
            int curNum = curArray[i];
            if (curNum > xMax)
            {
                xMax = curNum;
                maxIdx = i;
            } // end if
        } // end for
        
        int[] result = {xMax, maxIdx};
        return result;
    } // end findMax
    
    // Returns true if true cluster for x is found at a valid location.
    // e.g., true cluster is found if XOOOX is found in the current array for x = 3
    // [4,2,2] _ _ _ _ _ _ _ _ _ X O O X _ _ --> true for s = 1, false for s = 2
    // [1,4,1] _ _ _ _ O _ _ _ _ X X O X _ X --> false for s = 0, true for s = 2
    // [1,1,1] _ _ _ _ _ X _ X O X X X X _ _ --> false for s = 0, true for s = 1, true for s = 2
    private boolean isNumberLocated(int s, int k, Status[] arrayAnswer, ArrayInfo curArrayInfo)
    {
        int a = curArrayInfo.getNum();
        assert 0 <= s && s < a;
        boolean result = false;
        int[] curArray = curArrayInfo.getArray();
        int x = curArray[s];
        int sumToX = getSumToX(s, curArrayInfo, 1); // Summation of numbers + 1 space from first number to x
        int sumToXm1 = sumToX - x - 1;  // Summation of numbers + 1 space from first number to previous to x 
        int sumFromX = getSumFromX(s, curArrayInfo, 1); // Summation of numbers + 1 space from first number to x
        int b = curArrayInfo.getBeg();  // effective beginning index
        int e = curArrayInfo.getEnd();  // effective end index  
        
        int sp = getIdxOfSameNum(s, curArrayInfo); // index of number same as x before x (-1 if not found)
        int count = b;
        int qLoc = count + x + 1;
        boolean temp;
        boolean isSpPossible = sp != -1 && isNumberLocated(sp,k,arrayAnswer,curArrayInfo);
        if (!isSpPossible)           
        {
            while (qLoc <= e && !result)
            {
                boolean closedWithFalse = closedWithFalse(count, qLoc, k, arrayAnswer);
                boolean foundTrueCluster = fullOfTrue(count, qLoc, arrayAnswer);            
                boolean isLocValid = (count > b+sumToXm1) && (count + sumFromX <= e);                
                // Also the previous numbers should be able to fit into earlier empty clusters.                
                // [1,1,1] _ _ X X X X O X _ X _ _ _ _ _: true for s = 1, false for s = 2
                boolean earlierXsFit = doEarlierXsFit(s, count, curArrayInfo, arrayAnswer);
                // Also the later numbers should be able to fit into later empty clusters.
                // [1,1,1] _ _ _ _ _ X _ X O X X X X _ _: false for s = 0, true for s = 1
                boolean laterXsFit = doLaterXsFit(s, qLoc, curArrayInfo, arrayAnswer);                                                
                temp = closedWithFalse && foundTrueCluster && isLocValid && earlierXsFit && laterXsFit;            
                result = result || temp;            
                count++;
                qLoc = count + x + 1;
            } // end while
        }
        return result;
    } // end isNumberLocated
        
    // Checks whether numbers from 0 to s-1 fit in arrayAnswer[b ~ count-1]
    // [1,1,1] _ _ X X X X O X _ X _ _ _ _ _: true for s = 1, false for s = 2
    // Precondition: 0 < s <= a-1
    private boolean doEarlierXsFit(int s, int count, ArrayInfo curArrayInfo, Status[] arrayAnswer)
    {
        boolean result;                
        int b = curArrayInfo.getBeg();
        
        /* Find not-False cell clusters [b, count-1], inclusive. */
        int[][] notFalseArrays = findNotStatusClusters(b, count-1, arrayAnswer, Status.False);
        int[] begIdxNotFalse = notFalseArrays[0];
        int[] notFalseClusterLen = notFalseArrays[1];
        int numNotFalseClusters = begIdxNotFalse.length;        
        
        if (numNotFalseClusters == 0 || s <= 0)
            result = false;
        else
        {       
            int[] curArray = curArrayInfo.getArray();        
            int[] array = new int[s];
            System.arraycopy(curArray, 0, array, 0, s);
            //for (int i = 0; i < s; i++)
            //    array[i] = curArray[i];

            int size = count - b;
            ArrayInfo newArrayInfo = new ArrayInfo(array, size);
            int sumNew = newArrayInfo.getSum();
            int aNew = newArrayInfo.getNum();

            if (numNotFalseClusters == 1)   // the last not-Flase cluster is the only not-False cluster
                result = notFalseClusterLen[0] >= sumNew + aNew - 1;
            else
            {
                int i0 = 0;
                int ix = -1;
                for (int i = 0; i < numNotFalseClusters; i++)
                {                    
                    int curNotFalseClusterLen = notFalseClusterLen[i];                    
                    while (ix+1 < aNew && getSumFromAToB(i0, ix+1, newArrayInfo, 1) <= curNotFalseClusterLen)
                        ix++;
                    i0 = ix+1;
                } // end for
                result = i0 == aNew;    // if i0 reached the array size, non-False clusters can contain the array.
            } // end if
        } // end if
        return result;
    } // end doEarlierXsFit    
    
    // Checks whether numbers from s+1 to the end fit in arrayAnswer[qLoc+1 ~ e]
    // [1,1,1] _ _ _ _ _ X _ X O X X X X _ _: false for s = 0, true for s = 1
    // Precondition: 0 <= s < a-1
    private boolean doLaterXsFit(int s, int qLoc, ArrayInfo curArrayInfo, Status[] arrayAnswer)
    {
        boolean result;        
        int a = curArrayInfo.getNum();
        int e = curArrayInfo.getEnd();        
        
        /* Find not-False cell clusters [qLoc+1, e], inclusive. */
        int[][] notFalseArrays = findNotStatusClusters(qLoc+1, e, arrayAnswer, Status.False);
        int[] begIdxNotFalse = notFalseArrays[0];
        int[] notFalseClusterLen = notFalseArrays[1];
        int numNotFalseClusters = begIdxNotFalse.length;        
                
        if (numNotFalseClusters == 0 || s >= a-1)
            result = false;
        else
        {                        
            int[] curArray = curArrayInfo.getArray();        
            int[] array = new int[a-(s+1)];
            for (int i = 0; i < a-(s+1); i++)
                array[i] = curArray[i+s+1];
            
            int size = e - qLoc;
            ArrayInfo newArrayInfo = new ArrayInfo(array, size);
            int sumNew = newArrayInfo.getSum();
            int aNew = newArrayInfo.getNum();
            
            if (numNotFalseClusters == 1)   // the last not-Flase cluster is the only not-False cluster
                result = notFalseClusterLen[0] >= sumNew + aNew - 1;
            else
            {
                int i0 = 0;
                int ix = -1;
                for (int i = 0; i < numNotFalseClusters; i++)
                {                    
                    int curNotFalseClusterLen = notFalseClusterLen[i];                    
                    while (ix+1 < aNew && getSumFromAToB(i0, ix+1, newArrayInfo, 1) <= curNotFalseClusterLen)
                        ix++;
                    i0 = ix+1;
                } // end for
                result = i0 == aNew;    // if i0 reached the array size, non-False clusters can contain the array.
            } // end if
        } // end if
        return result;
    } // end doLaterXsFit
    
    // Returns true if there are more than one x in the array
    // [1, 2, 1] --> True for x = 1
    // [1, 2, 3] --> False
    private boolean multipleXExists(int x, ArrayInfo curArrayInfo)
    {        
        int a = curArrayInfo.getNum();
        int[] curArray = curArrayInfo.getArray();
        int countX = 0;
        for (int i = 0; i < a; i++)
        {
            if (curArray[i] == x)
                countX++;
        } // end for
        
        return countX > 1;
    } // end multipleXExists
    
    // Returns the index of the latest same number as x before x.
    // [1, 2, 1] --> -1 for s = 0 and s = 1, 0 for s = 2
    // [1, 1, 1] --> -1 for s = 0, 0 for s = 1, 1 for s = 2
    private int getIdxOfSameNum(int s, ArrayInfo curArrayInfo)
    {
        int[] curArray = curArrayInfo.getArray();
        int x = curArray[s];    // The number of interest
        int result = -1;
        for (int i = 0; i < s; i++)
        {
            if (curArray[i] == x)
                result = i;
        } // end for
        
        return result;
    } // end getIdxOfSameNum
    
    /* If the boundary is false and end is true, fill up the section with the current x. */
    // When p is False and p+1 is True
    // e.g., [2, 3] X _ _ _ _ X O _ _ _ 
    // e.g., [2, 2] X _ _ _ _ X O _ _ _ 
    // e.g., [1, 2] X _ _ _ _ X O _ _ _ 
    // e.g., [2, 1] X _ _ _ _ X O _ _ _ 
    // e.g., [1, 1, 2] _ _ _ O _ X O _ _ X 
    // [2, 1] _ _ _ X O _ _ _ X X --> Don't fill
    // [3, 2] _ _ _ X O _ _ _ _ _ -->  _ _ _ X O O _ _ _ _ (when minimum > 1)
    // [1,4,1] _ _ _ _ O _ _ _ _ X X O _ _ X --> _ _ _ _ O _ _ _ _ X X O X _ X
    private void fillUpFromEnds(int s, int p, int q, int k, int[] curArray, int a, int sum, int sumToX, int sumFromX, boolean solved, 
            int lenCurTrue, int earlierEffectiveLargerEmptyLen, int thisLargerEmptyLen, int conscNonFalseLen, int begConscNonFalse,
            boolean foundEarlierLargerTrue, ArrayInfo curArrayInfo, Status[] arrayAnswer)
    {        
        int kp = q - p - 1;
        int x = curArray[s];
        int b = curArrayInfo.getBeg();
        int e = curArrayInfo.getEnd();
        int xMin = curArray[0]; // Largest number in the array
        for (int i = 0; i < a; i++)
            xMin = Math.min(xMin, curArray[i]);
        int sumToNext = sumToX;
        if (s < a-1) // if not last number
            sumToNext += curArray[s+1] + 1; // add the next number        
        // [4, 2, 2] _ _ O _ X _ _ _ _ O X _ _ _ _ --> _ _ O _ X _ _ X O O X _ _ _ _        
        for (int i = 0; i < s; i++)
        {
            int xCur = curArray[i];
            int sumToXCur = getSumToX(i, curArrayInfo, 1);
            if (sumToXCur < begConscNonFalse)
                sumToNext -= (xCur + 1);
        } // end for
        
        // Earlier larger empty can't contain sumToX --> this true should be x.
        // But if the last number is at the last section, only up to s-1 need to fit in the earlier larger empty.
        // [1,3,1,1,2] O X _ _ _ _ O _ X O X O _ _ _ --> don't know        
        boolean thisTrueBelongsToX = (s+1 < a && !doEarlierXsFit(s+1, p, curArrayInfo, arrayAnswer)) || // (sumToX > earlierEffectiveLargerEmptyLen) ||
                                     (s == a-1 && q == e+1 && doEarlierXsFit(s, p, curArrayInfo, arrayAnswer));//sumToX - x - 1 < earlierEffectiveLargerEmptyLen); 
        if (!solved && isPFalse(p, arrayAnswer) && arrayAnswer[p+1] == Status.True
            && (x != lenCurTrue || (x == 1 && x == lenCurTrue))
            && p != q && sum + (a-1) > kp
            && thisTrueBelongsToX && (sumFromX > thisLargerEmptyLen || x == xMin || foundEarlierLargerTrue)) 
        {
            System.out.println("\t\tp is false and p+1 is true");
            for (int i = p+1; i <= p+x; i++)  // Fill up from left
            {
                if (arrayAnswer[i] == Status.Empty)
                    arrayAnswer[i] = Status.True;
            } // end for
            if (p+x+1 <= e)  // Right end should be False
                if (arrayAnswer[p+x+1] == Status.Empty)
                    arrayAnswer[p+x+1] = Status.False;
            System.out.println("\t\t" + printCells(arrayAnswer)); 
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
        // [3, 3, 2] _ _ _ _ _ _ _ _ _ _ O O X _ _ --> Don't fill    
        // [3, 3, 2] _ _ _ _ _ _ _ _ _ _ O X X _ _ --> Don't fill
        // [4, 2, 2] X _ _ _ O _ _ _ _ O X _ _ _ _ --> X _ _ _ O _ _ X O O X _ _ _ _        
        else if (!solved && isQFalse(q, k, arrayAnswer) 
                && arrayAnswer[q-1] == Status.True && (x != lenCurTrue)
                && (p != q) && (sum + (a-1) > kp) && (sumToNext > conscNonFalseLen) // && (sumToX > earlierEffectiveLargerEmptyLen)
                && (sumFromX > thisLargerEmptyLen || x == xMin))
        {
            System.out.println("\t\tq is false and q-1 is true");
            for (int i = q-1; i >= q-x; i--)  // Fill up from right
            {
                if (arrayAnswer[i] == Status.Empty)
                    arrayAnswer[i] = Status.True;
            } // end for
            if (q-x-1 >= b)  // Left end should be False
                if (arrayAnswer[q-x-1] == Status.Empty)
                    arrayAnswer[q-x-1] = Status.False;
            System.out.println("\t\t" + printCells(arrayAnswer)); 
            /* Check whether this array is solved. */
            solved = checkAllTrue(curArrayInfo, arrayAnswer, solved);
        } // end if

        // When minimum number > 1, fill up to minimum number and not close.
        // [3, 2] _ _ _ _ _ O X _ _ --> _ _ _ _ O O X _ _ (when minimum > 1)
        if (!solved && isQFalse(q, k, arrayAnswer) 
            && arrayAnswer[q-1] == Status.True && (x != lenCurTrue)
            && (p != q) && (sum + (a-1) > kp)
            && (xMin > 1) && (sumFromX > thisLargerEmptyLen))
        {
            int xCur = xMin;                            
            System.out.println("\t\tq is false and q-1 is true");
            for (int i = q-1; i >= q-xCur; i--)  // Fill up from right
            {
                if (arrayAnswer[i] == Status.Empty)
                    arrayAnswer[i] = Status.True;
            } // end for                            
            System.out.println("\t\t" + printCells(arrayAnswer));             
        } // end if
        
    } // end fillUpFromEnds
} // end NonogramSolution_v2_01
