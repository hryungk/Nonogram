
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

    /** Returns the current row/column array.
     * @return An integer array containing the row/column array.
     */
    public int[] getArray()
    {
        return thisArray;
    } // end getArray

    // Mutator for the row/column array.
    private void setArray(int[] newArray)
    {
        thisArray = newArray;
    }

    /** Removes the first number in the row/column array. */
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
    
    /** Removes the last number in the row/column array. */
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
     * @return A Boolean true if the array is solved. 
     */
    public boolean isSolved()
    {
        return arraySolved;
    }

    /** Sets the current array solved or not. 
     * @param TF A Boolean to set the current array. True if solved, false otherwise.
     */
    public void setSolved(boolean TF)
    {
        arraySolved = TF;
    }

    /** Returns the length of the grid.
     * @return An integer containing the length of the grid.
     */
    public int getLength()
    {
        return gridLen;
    }

    // Mutator for the grid length.
    private void setLength(int newLen)
    {
        gridLen = newLen;
    }

    /** Returns the number of numbers in the row/column array.
     * @return An integer containing the size of the row/column array.
     */
    public int getNum()
    {
        return num;
    }        
    
    // Mutator for the size of the row/column array.
    private void setNum(int newNum)
    {
        num = newNum;
    }

    /** Returns the beginning index of the array answer. 
     * @return An integer containing the effective beginning index of the array answer.
     */
    public int getBeg()
    {
        return begIdx;
    }
    // Mutator for the beginning index.
    public void setBeg(int newB)
    {
        begIdx = newB;
        setLength(getEnd() - newB + 1); // Update the effective length of grid
    }

    /** Returns the end index of the array answer. 
     * @return An integer containing the effective end index of the array answer.
     */
    public int getEnd()
    {
        return endIdx;
    }
    // Mutator for the end index.
    public void setEnd(int newE)
    {
        endIdx = newE;
        setLength(newE - getBeg() + 1); // Update the effective length of grid            
    }

    /** Returns the summation of numbers in the row/column array.
     * @return An integer containing the sum of all numbers in the row/column array.
     */
    public int getSum()
    {
        return arraySum;
    }
    // Mutator for sum of row/column array numbers.
    private void setSum()
    {
        arraySum = sumArrayFrom(0);
        //for (int i = 0; i < num; i++)                    
        //    arraySum += thisArray[i];         
    }
} // end ArrayInfo