package main;

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
        int probNum = 11;
        int rowNum = 10;
        int colNum = 10;
        System.out.println("<Problem " + rowNum + " x " + colNum + " - " + probNum + ">");        
        
        NonogramProblem newProblem = new NonogramProblem(rowNum, colNum, probNum);        
        NonogramSolution newSolution = new NonogramSolution(newProblem);
        
        newSolution.solve();
        System.out.println();
        System.out.println(newSolution);
        System.out.println("Is the answer to the puzzle correct?: " + newSolution.isCorrect());
    } // end main    
}
