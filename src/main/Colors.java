/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.awt.Color;

/**
 *
 * @author HRK
 */
public class Colors {
    
    public static Color lightBlue = lightBlue();
    public static Color gray = gray();
    public static Color navyBlue = navyBlue();
    
    // Returns a light blue color
    private static Color lightBlue()
    {        
        float[] HSB = Color.RGBtoHSB(234, 238, 249, null);
        return Color.getHSBColor(HSB[0], HSB[1], HSB[2]);
    }
    
    // Returns a light blue color
    private static Color gray()
    {        
        float[] HSB = Color.RGBtoHSB(214, 218, 228, null);
        return Color.getHSBColor(HSB[0], HSB[1], HSB[2]);
    }
    
    // Returns a navy blue color
    private static Color navyBlue()
    {        
        float[] HSB = Color.RGBtoHSB(51, 73, 97, null);
        return Color.getHSBColor(HSB[0], HSB[1], HSB[2]);
    }
}
