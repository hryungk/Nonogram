/*
 * A class that solves a Nonogram puzzle.
 */

/**
 *
 * @author Hyunryung Kim    hryungk@gmail.com
 */
import java.util.Arrays;
public class NonogramSolution_v0_1 
{   
    private final int[][] PROB_ROW;          // Row arrays of the puzzle
    private final int[][] PROB_COL;          // Column arrays of the puzzle   
    private final Status[][] SOLUTION;          // Answer to the puzzle
    private Status[][] answer;
    private final int m, n;             // size of row and column, respectively
    private enum RowCol {Row, Column}   // State whether row or column    
    private ArrayInfo[] row_arrays; // An array of array information objects for row arrays
    private ArrayInfo[] col_arrays; // An array of array information objects for column arrays
    public NonogramSolution_v0_1(NonogramProblem newProblem)
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
        System.out.println("b = " + b + ", e = " + e);
        
        int[] curArray = curArrayInfo.getArray(); // Row/column array        
        int a = curArray.length;            // The number of numbers in curArray
        int ke = curArrayInfo.getLength();       // Effective length of grid
        boolean solved = false;             // true if curArray is solved        
        int sum = 0;                        // sum of numbers in curArray
        for (int i = 0; i < a; i++)                    
            sum += curArray[i];
        int numT = 0;                       // The number of True cells    
        int numE = 0;                       // The number of Empty cells    
        for (int i = b; i < e+1; i++)
        {
            if (arrayAnswer[i] == Status.True)
                numT++;
            if (arrayAnswer[i] == Status.Empty)
                numE++;
        } // end for
        
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
        else if (sum + (a - 1) == ke - 1)    // When the sum is one short in the beginning
        {
            System.out.println("\tsum of numbers and spaces == one short of effective row/column length");            
            
            int x = curArray[0];  // current number    
            System.out.println("\tCurrent number: " + x);
            // Section 1: from 0 to x            
            for (int i = b+1; i < b+x; i++)
                arrayAnswer[i] = Status.True;
            int count = b+x + 1;  // count of total cells investigated
            int y;      // current number   
            if (a > 1)  // when there are more than one number
            {
                if (a > 2)
                {
                    //y = curArray[1];        // current number         
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
            if (arrayAnswer[b] == Status.True)  // first cell is filled    
                removeBeg(curArray, arrayAnswer, curArrayInfo);        
            if (arrayAnswer[e+1-1] == Status.True)    // last cell is filled
                removeEnd(curArray, arrayAnswer, curArrayInfo);
            solved = checkAllTrue(curArrayInfo, arrayAnswer, solved);
            System.out.println("\t" + printCells(arrayAnswer));
        }
        else if (arrayAnswer[b] == Status.True)  // first cell is filled
        {
            removeBeg(curArray, arrayAnswer, curArrayInfo);
            solved = checkAllTrue(curArrayInfo, arrayAnswer, solved);            
            System.out.println("\t" + printCells(arrayAnswer));   
        } // end if
        else if (arrayAnswer[e+1-1] == Status.True)    // last cell is filled
        {
            removeEnd(curArray, arrayAnswer, curArrayInfo);
            solved = checkAllTrue(curArrayInfo, arrayAnswer, solved);
            System.out.println("\t" + printCells(arrayAnswer));   
        } // end if           
        else
        {   
            int p;          // The last cell index in the previous false cluster
            int q = b-1;     // The first cell index in the next false cluster
            
            for (int s = 0; s < a; s++) // loop through numbers in the array (sections)
            {       
                int x = curArray[s];    // current number
                System.out.println("\tCurrent number: " + x);
                
                p = q;
                while (p < e+1 && arrayAnswer[p+1] == Status.False)
                    p++;                
                q = p + 1;
                while(q < e+1 && arrayAnswer[q] == Status.Empty)
                    q++;
                
                //System.out.println("p = " + p + ", q = " + q);
                if (p < e+1 && q <= e+1) // procede only when the index is within boundary
                {
                    if (p > -1 && arrayAnswer[p] == Status.True)  // push back p by one when true
                        p--;
                    if (q < e+1 && arrayAnswer[q] == Status.True)  // push forward q to the next true cluster when true
                    {
                        q++;
                        while(q < e+1 && arrayAnswer[q] == Status.True)
                            q++;
                        while(q < e+1 && arrayAnswer[q] == Status.Empty)
                            q++;
                        if (s == a - 1) // When x is last number in the array
                        {   // Find the first false
                            while(q < e+1 && arrayAnswer[q] != Status.False)
                                q++;
                        } // end if
                    } // end if
                    System.out.println("\tp = " + p + ", q = " + q);                    
                    
                    int kp = q - p - 1; // The number of empty cells between p and q   
                    int i0 =  p + 1 + kp - x;   // Beginning index of true cells
                    
                    // When there are more than one number left in the array but it is alreay filled in the middle of the row/column, 
                    // (so that the code still doesn't know it is filled), and there are multiple locations for the last number to be,
                    // we should skip this. Example is _XXOXX_XOO and array is [1,1]
                    if ((x > kp/2) && (i0 >= b) && !(sum - numT == x && a >= 2))// && (numE <= x)
                    {
                        for (int i = i0; i <= p + x; i++)
                            if (arrayAnswer[i] == Status.Empty)
                                arrayAnswer[i] = Status.True;
                        
                    } // end if      
                    if (x > kp) // When there is less number of empty cells than the current number, make false
                    {
                        for (int i = p + 1; i < q; i++)
                            if (arrayAnswer[i] == Status.Empty)
                                arrayAnswer[i] = Status.False;                
                    } // end if
                    
                    int firstTrue = p+1;
                    while (firstTrue < e+1 && arrayAnswer[firstTrue] != Status.True)
                        firstTrue++;
                    if (firstTrue < e+1 && firstTrue <= q && q <= e+1)
                    {
                        int numTCur = 0;    // Number of True in the current section
                        int lastTrue = firstTrue;
                        for (int i = firstTrue; i < q; i++)
                            if (arrayAnswer[i] == Status.True)
                            {
                                numTCur++;
                                lastTrue = i;
                            }
                        int remainingT = x - numTCur;   // Remaining number of true
                         // System.out.println("\tfirstTrue = " + firstTrue +
                         //       ", numTCur = " + numTCur + ", lastTrue = " + 
                         //       lastTrue + ", remainingT = " + remainingT);
                        //System.out.println("\tnumT = " + numT + ", sum = " + sum);
                        if (remainingT == 0 && numT == sum)    // All true are found
                        {
                            System.out.println("\tSolution is found for the array; Remaining number of True == 0");
                            if (arrayAnswer[firstTrue-1] == Status.Empty)
                                arrayAnswer[firstTrue-1] = Status.False;
                            solved = true;
                        }
                        else    // Make cells in this section that are farther than remainingT false
                        {     
                            //System.out.println("Code proceeded to remainingT != 0");    
                            if (p == b-1 || arrayAnswer[p] == Status.False)
                                for (int i = p+1; i < firstTrue - remainingT-1;i++)
                                    arrayAnswer[i] = Status.False;
                            //System.out.println("q==ke?" + (q==ke));
                            //System.out.println("q = " + q + ", ke = " + ke);
                            if (q == e+1 || arrayAnswer[q] == Status.False)
                            {
                                //System.out.println("Code proceeded to q == ke");
                                for (int i = lastTrue + remainingT + 1; i < q; i++)
                                    arrayAnswer[i] = Status.False;    
                            } // end if                            
                        } // end if
                    } // end if
                } // end if     
                
                // Update number of True and Empty
                numT = 0;
                numE = 0;
                for (int i = b; i < e+1; i++)   
                {
                    if (arrayAnswer[i] == Status.True)
                        numT++;
                    if (arrayAnswer[i] == Status.Empty)
                        numE++;
                } // end for
                //System.out.println("\tnumT = " + numT + ", sum = " + sum);
                System.out.println("\t" + printCells(arrayAnswer));
            } // end for
            solved = checkAllTrue(curArrayInfo, arrayAnswer, solved);
            if (solved)
                System.out.println("\t" + printCells(arrayAnswer));
        } // end if
        // Make beginning index the first non-false cell
        b = curArrayInfo.getBeg();  // effective beginning index
        e = curArrayInfo.getEnd();  // effective end index
        while (b <= e && arrayAnswer[b] == Status.False)
            b++;
        curArrayInfo.setBeg(b);
        // Make end index the first non-false cell from the end
        while (e >= b && arrayAnswer[e] == Status.False)
            e--;
        curArrayInfo.setEnd(e);
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
        }        
        return result;
    }    
    
    public String toString()
    {
        String result = "Answer to " + m + " x " + n + " Nonogram Puzzle:\n";
        for (int i = 0; i < m; i++)            
            result += printCells(answer[i]) + "\n";                    
        return result;
    } // end toString    
    
    
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
    }
    private void removeEnd(int[] curArray, Status[] arrayAnswer,  ArrayInfo curArrayInfo)
    {
        System.out.println("\tRemoving the last number in the array");
        int a = curArray.length;
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
    }
    
    private boolean checkAllTrue(ArrayInfo curArrayInfo, Status[] arrayAnswer, boolean solved)
    {        
        int b = curArrayInfo.getBeg();  // effective beginning index
        int e = curArrayInfo.getEnd();  // effective end index      
        //System.out.println("b = " + b + ", e = " + e);
        int[] curArray = curArrayInfo.getArray(); // Row/column array        
        int a = curArray.length;            // The number of numbers in curArray                
        int sum = 0;                        // sum of numbers in curArray
        for (int i = 0; i < a; i++)                    
            sum += curArray[i];
        int numT = 0;                       // The number of True cells         
        for (int i = b; i < e+1; i++)
        {
            if (arrayAnswer[i] == Status.True)
                numT++;                           
        } // end for
        
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
    }
    
    public class ArrayInfo
    {
        private int[] thisArray; // Array
        private boolean arraySolved; // indicates whether this array is solved
        private int gridLen;  // length of row/column
        private int num;    // The number of numbers in the array (array size)
        private int begIdx, endIdx;   // beginning and end index
        
        public ArrayInfo(int[] array, int size)
        {
            thisArray = array;
            arraySolved = false;
            gridLen = size;
            num = array.length;
            begIdx = 0;
            endIdx = size-1;
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
            setBeg(begIdx + thisArray[0] + 1);
            setLength(gridLen-(thisArray[0] + 1));   // Reduce the grid length             
            setArray(newArray); // Update the array            
            setNum(newArray.length);  // Reduce the array size            
        }
        public void removeLast()
        {            
            int[] newArray = new int[num-1]; // One size smaller            
            System.arraycopy(thisArray, 0, newArray, 0, num-1);
            //for (int i = 0; i < num-1; i++)
            //    newArray[i] = thisArray[i];
            setEnd(endIdx - (thisArray[num-1]+1));
            setLength(gridLen-(thisArray[num-1] + 1));   // Reduce the grid length             
            setArray(newArray); // Update the array            
            setNum(newArray.length);  // Reduce the array size            
        }
        
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
        }
        
        public int getEnd()
        {
            return endIdx;
        }
        public void setEnd(int newE)
        {
            endIdx = newE;
        }
        
    } // end ArrayInfo
}
