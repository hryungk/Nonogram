package main;

/*
 * A class that represents a Nonogram puzzle.
 */

/**
 * @author Hyunryuung Kim   hryungk@gmail.com
 */
import java.io.*;
import java.util.Scanner;
import java.util.Arrays;
public class NonogramProblem 
{    
    private int r; // Number of rows
    private int c; // Number of columns
    
    // A given problem         
    private int[][] rowArrays;
    private int[][] columnArrays;
    
    private Status[][] solution;  // Answer of this problem
    
    public NonogramProblem() throws IOException
    {
        this(1);
      
    } // end default constructor
    
    public NonogramProblem(int probNum) throws IOException
    {
        this(10, 10, probNum);        
    } // end  constructor
    
    public NonogramProblem(int rowNum, int colNum, int probNum) throws IOException
    {
        r = rowNum;
        c = colNum;
        readProblem(probNum);
        isValid();        
    } // end  constructor    
    
    // Return an array of row arrays.
    public int[][] getRowArray()
    {
        return rowArrays;        
    } // end getRowArray
    
    // Return an array of column arrays.
    public int[][] getColumnArray()
    {
        return columnArrays;
    } // end getColumnArray    
    
    // Return the solution to this problem.
    public Status[][] getSolution()
    {
        return solution;
    } // end getAnswer
    
    @Override
    public String toString()
    {
        String result = "";
        
        result += "Row arrays: ";   // Print out the row arrays
        for (int[] array : rowArrays)        
            result += Arrays.toString(array) + " ";
        result += "\n";        
                  
        result += "Column arrays: ";    // Print out the column arrays
        for (int[] array : columnArrays)        
            result += Arrays.toString(array) + " ";
        result += "\n";        
        
        result += solutionToString();
        return result;
    }
    
    
    private void readProblem(int probNum) throws IOException
    {
        String firstLine, secondLine, thirdLine;
        Scanner fileScan; 
        String path = "src/resources/Problems/Problem_" + r + "x" + c + "/";
        File input = new File(path + "Problem_" + r + "x" + c + "_" + probNum + ".txt");
        fileScan = new Scanner(input); // Read problem file
        fileScan.useDelimiter("}");
        
        // Scan the row arrays
        firstLine = fileScan.nextLine();    // Row arrays scanner
        rowArrays = arrayScanner(firstLine);
        r = rowArrays.length;        
        
        // Scan the column arrays
        secondLine = fileScan.nextLine();    // Column arrays scanner
        columnArrays = arrayScanner(secondLine);
        c = columnArrays.length;
                
        // Scan the solution
        thirdLine = fileScan.next();    // Row arrays scanner
        thirdLine = thirdLine.substring(1);
        solution = answerScanner(thirdLine, r, c);        
    } // end readProblem
    
    // Scan the array of row/column arrays and return it.
    private int[][] arrayScanner(String currentLine)
    {
        //System.out.println("current line: " + currentLine);
        Scanner lineScan, arrayScan; 
        lineScan = new Scanner(currentLine);  // Scan the row array line
        lineScan.useDelimiter("\\W{2,}");   // At least 2 occurrences of any 
                                            // character other than a letter, 
                                            // digit, or underscore
        int size = 0;     // Measure the size of the grid
        while(lineScan.hasNext())
        {
            lineScan.next(); // Pop the next text           
            size++;
        }
        int[][] currentArray = new int[size][];           
        int countArray = 0;     // Count the number of row arrays
        lineScan = new Scanner(currentLine);  // Scan the row array line
        lineScan.useDelimiter("\\W{2,}");
        while(lineScan.hasNext())
        {
            String arrayNumbers = lineScan.next(); // A row array
            //System.out.println("current text: " + arrayNumbers);  
            int tempArrayLen = arrayNumbers.length()/2+1;   // Temporary array size
            int[] tempArray = new int[tempArrayLen];     // Temporary row array
            //System.out.println("\tarray size: " + tempArrayLen);
            arrayScan = new Scanner(arrayNumbers);  // Number scanner of the row array
            arrayScan.useDelimiter("\\W");
            int countInt = 0;   // Count the number of numbers in the row array (actual array size)
            while (arrayScan.hasNextInt())
            {
                int curInt = arrayScan.nextInt();
                //System.out.println("\tcurrent number: " + curInt);
                tempArray[countInt] = curInt;    // Add the number to the temporary array
                countInt++;
            } // end while
            
            int[] actualArray;  // Actual row/column array to be stored
            if (countInt != tempArrayLen)   // When the numbers are more than two digits
            {
                actualArray = new int[countInt];
                System.arraycopy(tempArray, 0, actualArray, 0, countInt);
                //for (int i = 0; i < countInt; i++)
                //    actualArray[i] = tempArray[i];
            }
            else
                actualArray = tempArray;
            
            currentArray[countArray] = actualArray;
            countArray++;
        } // end while
        return currentArray;
    } // end arrayScanner
    
    // Scan the answer part from the file and return it.
    private Status[][] answerScanner(String currentLine, int rowSize, int colSize)
    {
        Status[][] solutionGrid = new Status[rowSize][colSize];
        Scanner scan = new Scanner(currentLine);
        //scan.useDelimiter("\\W");
        int i = 0;   // row counter
        int j = 0;   // column counter
        while (scan.hasNext())
        {
            String text = scan.next();
            //System.out.println("next text: " + text);            
            if (text.equals("T"))
            {
                solutionGrid[i][j] = Status.True;
                j++;
            }
            else if (text.equals("F"))
            {
                solutionGrid[i][j] = Status.False;
                j++;
            }
            
            if (j > 0 && j % colSize == 0)   // when column counter reaches the end of row
                i++;    // increment the row number
            j = j % colSize;
            
        } // end while
        return solutionGrid;
    } // end answerScanner    
    
    // Check whether the assertions for this problem are correct.
    //  Might throw an assertion error and program execution terminates.
    private void isValid()
    {
        int[] curArray;
        for (int i = 0; i < r; i++)
        {
            curArray = rowArrays[i];
            int a = curArray.length;    // The number of numbers in curArray
            assert 1 <= a && a <= (r + 1) / 2; 
            
            int sum = 0;                        // sum of numbers in curArray
            for (int ai = 0; ai < a; ai++)
            {            
                assert 1 <= curArray[ai] && curArray[ai] <= r;
                sum += curArray[ai];
            } // end for                           
            assert sum + (a - 1) <= r;
        } // end for
    } // end isValid
    
    private String solutionToString()
    {
        String result = "";
        result += "Solution:\n";    // Print out the solution
        for (int i = 0; i < r; i++)
        {
            for (int j = 0; j < c; j++)
            {                
                switch(solution[i][j])
                {
                    case True:
                        result += "T";
                        break;
                    case False:
                        result += "F";
                        break;
                    default:
                        result += "";
                        break;
                } // end switch
                result += " ";
            } // end for
            result += "\n";
        } // end for
        result += "\n";
        return result;
    } // end solutionToString    
}
