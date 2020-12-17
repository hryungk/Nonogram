/*
 * A class that solves a Nonogram puzzle.
 */

/**
 *
 * @author Hyunryung Kim    hryungk@gmail.com
 */
import java.util.Arrays;
public class NonogramSolution 
{   
    private final int[][] ROW_ARRAYS;          // Row arrays of the puzzle
    private final int[][] COL_ARRAYS;          // Column arrays of the puzzle   
    private final Status[][] SOLUTION;          // Answer to the puzzle
    private Status[][] answer;
    private final int m, n;             // size of row and column, respectively
//    private enum RowCol {Row, Column}   // State whether row or column    
    private ArrayInfo[] row_arrays; // An array of array information objects for row arrays
    private ArrayInfo[] col_arrays; // An array of array information objects for column arrays
    public NonogramSolution(NonogramProblem newProblem)
    {        
        ROW_ARRAYS = newProblem.getRowArray();
        COL_ARRAYS = newProblem.getColumnArray();
        
        m = ROW_ARRAYS.length;
        n = COL_ARRAYS.length;   
        
        row_arrays = new ArrayInfo[m];
        for (int i = 0; i < m; i++)
            row_arrays[i] = new ArrayInfo(ROW_ARRAYS[i], m);
        col_arrays = new ArrayInfo[n];
        for (int j = 0; j < n; j++)
            col_arrays[j] = new ArrayInfo(COL_ARRAYS[j], n);
        
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
                    "\t\t\tLoop "+ loopCount +
                               "\n=======================================================");
            System.out.println("------------------------ Row" +
                               " ------------------------");            
            for (int i = 0; i < m; i++) // Scan rows
            {                
                if (!row_arrays[i].isSolved())
                {
                    System.out.println("Array " + Arrays.toString(row_arrays[i].getArray()) + ":");
                    Status[] temp = findArraySolution(RowCol.Row,i, m);
                    System.arraycopy(temp, 0, answer[i], 0, n); 
                    //for (int j = 0; j < n; j++)
                    //    answer[i][j] = temp[j]; 
                }
                else
                {                    
                    System.out.println("Array "
                                       + Arrays.toString(row_arrays[i].getArray()) 
                                       + " is solved.");
                }
            } // end for
            System.out.println("------------------------ Column" +
                               " ------------------------");
            for (int j = 0; j < n; j++) // Scan columns
            {
                if (!col_arrays[j].isSolved())
                {
                    System.out.println("Array " + Arrays.toString(col_arrays[j].getArray()) + ":");
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
                                       + Arrays.toString(col_arrays[j].getArray()) 
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
    public Status[] findArraySolution(RowCol rowcol, int idx, int k)
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
        int[] curArray = curArrayInfo.getArray(); // Row/column array        
        int a = curArray.length;            // The number of numbers in curArray
        k = curArrayInfo.getLength();       // Effective length of grid
        boolean solved = false;             // true if curArray is solved        
        int sum = 0;                        // sum of numbers in curArray
        for (int i = 0; i < a; i++)                    
            sum += curArray[i];
        int numT = 0;                       // The number of True cells
        int numE = 0;                       // The number of Empty cells   
        for (int i = 0; i < k; i++)
        {
            if (arrayAnswer[i] == Status.True)
                numT++;                               
            if (arrayAnswer[i] == Status.Empty)
                    numE++;              
        } // end for
        
        // Start investigation!
        if (sum + (a - 1) == k) // initial condition automatically solves
        {
            int count = 0;      // index count for the answer array
            for (int s = 0; s < a; s++) // loop through numbers in the array (sections)
            {
                int x = curArray[s];    // current number
                System.out.println("\tCurrent number: " + x);
                for (int xi = 0; xi < x; xi++)  // Fill up the cells as many as x
                {
                    arrayAnswer[count] = Status.True;
                    count++;
                } // end for
                if (count < k)  // After filling up cells, the immediate cell 
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
            System.out.println("\tSolution is found for the array " + Arrays.toString(curArray));
            
            // Make all empty cells False
            for (int i = 0; i < k; i++) 
                if (arrayAnswer[i] == Status.Empty)
                    arrayAnswer[i] = Status.False;
            solved = true;
            System.out.println("\t" + printCells(arrayAnswer));
        }
        else if (sum + (a - 1) == k - 1)    // When the sum is one short in the beginning
        {
            int x = curArray[0];  // current number    
            System.out.println("\tCurrent number: " + x);
            // Section 1: from 0 to x            
            for (int i = 1; i < x; i++)
                arrayAnswer[i] = Status.True;
            int count = x + 1;  // count of total cells investigated
            int y;      // current number   
            if (a > 1)  // when there are more than one number
            {
                y = curArray[1];        // current number         
                for (int s = 1; s < a-1; s++) // loop through numbers in the array (sections)
                {   // Section s: from (x+1) to (x+1+y) 
                    x = curArray[s-1];      // previous number
                    y = curArray[s];    // current number         
                    System.out.println("\tCurrent number: " + y);
                    for (int i = x+2; i < x+1+y; i++)
                        arrayAnswer[i] = Status.True;  
                    count += y + 1;
                } // end for

                // Last section: from count to (k-1)
                y = curArray[a-1];
                System.out.println("\tCurrent number: " + y);
                for (int i = count+1; i < k-1; i++)
                    arrayAnswer[i] = Status.True;                
            } // end if
            System.out.println("\t" + printCells(arrayAnswer));
        }
        else if (arrayAnswer[0] == Status.True)  // first cell is filled
        {
            int x = curArray[0];
            System.out.println("\tCurrent number: " + x);
            for (int i = 1; i < x; i++)
                arrayAnswer[i] = Status.True;
            arrayAnswer[x] = Status.False;      
            // Remove the first number in this array
            curArrayInfo.removeNumber(0);
            System.out.println("\t" + printCells(arrayAnswer));
        } // end if
        else if (arrayAnswer[k-1] == Status.True)    // last cell is filled
        {
            int x = curArray[a-1];
            System.out.println("\tCurrent number: " + x);
            for (int i = k-x; i < k-1; i++)
                arrayAnswer[i] = Status.True;
            arrayAnswer[k-x-1] = Status.False;                            
            // Remove the last number in this array
            curArrayInfo.removeNumber(a-1);                
            System.out.println("\t" + printCells(arrayAnswer));
        } // end if           
        else
        {   
            int p;          // The last cell index in the previous false cluster
            int q = -1;     // The first cell index in the next false cluster
            
            for (int s = 0; s < a; s++) // loop through numbers in the array (sections)
            {       
                int x = curArray[s];    // current number
                System.out.println("\tCurrent number: " + x);
                
                p = q;
                while (p < k && arrayAnswer[p+1] == Status.False)
                    p++;                
                q = p + 1;
                while(q < k && arrayAnswer[q] == Status.Empty)
                    q++;
                
                //System.out.println("p = " + p + ", q = " + q);
                if (p < k && q <= k) // procede only when the index is within boundary
                {
                    if (p > -1 && arrayAnswer[p] == Status.True)  // push back p by one when true
                        p--;
                    if (q < k && arrayAnswer[q] == Status.True)  // push forward q to the next true cluster when true
                    {
                        q++;
                        while(q < k && arrayAnswer[q] == Status.True)
                            q++;
                        while(q < k && arrayAnswer[q] == Status.Empty)
                            q++;
                        if (s == a - 1) // When x is last number in the array
                        {   // Find the first false
                            while(q < k && arrayAnswer[q] != Status.False)
                                q++;
                        } // end if
                    } // end if
                    System.out.println("\tp = " + p + ", q = " + q);                    
                    
                    int kp = q - p - 1; // The number of empty cells between p and q                    
                    if (x > kp/2)             
                    {
                        for (int i = p + 1 + kp - x; i <= p + x; i++)
                            if (arrayAnswer[i] == Status.Empty)
                                arrayAnswer[i] = Status.True;
                        if (x > kp)
                            for (int i = p + 1; i < q; i++)
                                if (arrayAnswer[i] == Status.Empty)
                                    arrayAnswer[i] = Status.False;                
                    } // end if      
                    
                    int firstTrue = p+1;
                    while (firstTrue < k && arrayAnswer[firstTrue] != Status.True)
                        firstTrue++;
                    if (firstTrue < k && q <= k)
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
                        //System.out.println("numTCur = " + numTCur + ", lastTrue = " + lastTrue + ", remainingT = " + remainingT);
                        
                        if (remainingT == 0)    // All true are found
                        {
                            if (arrayAnswer[firstTrue-1] == Status.Empty)
                                arrayAnswer[firstTrue-1] = Status.False;
                        }
                        else    // Make cells in this section that are farther than remainingT false
                        {     
                            //System.out.println("Code proceeded to remainingT != 0");    
                            if (p == -1 || arrayAnswer[p] == Status.False)
                                for (int i = p+1; i < firstTrue - remainingT-1;i++)
                                    arrayAnswer[i] = Status.False;
                            //System.out.println("q==k?" + (q==k));
                            //System.out.println("q = " + q + ", k = " + k);
                            if (q == k || arrayAnswer[q] == Status.False)
                            {
                                //System.out.println("Code proceeded to q == k");
                                for (int i = lastTrue + remainingT + 1; i < q; i++)
                                    arrayAnswer[i] = Status.False;    
                            }
                        }
                    } // end if
                } // end if
                System.out.println("\t" + printCells(arrayAnswer));
            } // end for
        } // end if
               
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
    
    
    public class ArrayInfo
    {
        private int[] thisArray; // Array
        private boolean arraySolved; // indicates whether this array is solved
        private int gridLen;  // length of row/column
        private int num;    // The number of numbers in the array (array size)
        
        public ArrayInfo(int[] array, int size)
        {
            thisArray = array;
            arraySolved = false;
            gridLen = size;
            num = array.length;
        }
        
        public int[] getArray()
        {
            return thisArray;
        }
        
        private void setArray(int[] newArray)
        {
            thisArray = newArray;
        }
        
        // Removes the number at the index idx in the array 
        public void removeNumber(int idx)
        {
            int numToRemove = thisArray[idx];
            int[] newArray = new int[num-1]; // One size smaller
            int count = 0;  // counter for the newArray array
            for (int i = 0; i < num; i++)
            {
                if (i != idx)
                {
                    newArray[count] = thisArray[i];
                    count++;
                } // end if
            } // end for
            setArray(newArray); // Update the array
            setLength(gridLen-(numToRemove+1));   // Reduce the grid length 
            setNum(newArray.length);  // Reduce the array size
        }        
        public void removeFirst()
        {            
            int[] newArray = new int[num-1]; // One size smaller            
            for (int i = 0; i < num-1; i++)
                newArray[i] = thisArray[i+1];
            setLength(gridLen-(thisArray[0] + 1));   // Reduce the grid length             
            setArray(newArray); // Update the array            
            setNum(newArray.length);  // Reduce the array size            
        }
        public void removeLast()
        {            
            int[] newArray = new int[num-1]; // One size smaller            
            for (int i = 0; i < num-1; i++)
                newArray[i] = thisArray[i];
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
    } // end ArrayInfo
}
