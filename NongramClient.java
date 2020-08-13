/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author HRK
 */
import java.io.*;
public class NongramClient 
{
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException 
    {
        int probNum = 10;
        System.out.println("<Problem " + probNum + ">");
        NonogramProblem newProblem = new NonogramProblem(probNum);        
        NonogramSolution_v1_2 newSolution = new NonogramSolution_v1_2(newProblem);
        
        newSolution.solve();
        System.out.println();
        System.out.println(newSolution);
        System.out.println("Is the answer to the puzzle correct?: " + newSolution.isCorrect());
    } // end main    
}
