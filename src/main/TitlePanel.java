/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** The title panel when first start the application.
 * Give the user choices between randomly choose a puzzle or a specific number.
 * @author HRK
 */
public class TitlePanel extends JPanel {
    
    private JFrame frame;
    private JButton push;
    private JLabel label;
    private int rNum, cNum, pNum;
    private int[] gameSize = {10, 15};   // either 5x5 or 10x10
    private final int P_MAX = 11;   // Number of problems available to choose from
    
    public TitlePanel(JFrame frame) {
        
        this.frame = frame;
        frame.add(this);
        
        push = new JButton("Solve");
        push.setForeground(Colors.navyBlue);
        push.addActionListener(new ButtonListener(this));
        
        label = new JLabel("Randomly choose a puzzle.");
        label.setForeground(Colors.navyBlue);
        
        add(push);
        add(label);
        
        setPreferredSize(new Dimension(300, 40));        
        setBackground(Colors.lightBlue);
    }

    private class ButtonListener implements ActionListener{
        
        private JPanel panel;
        public ButtonListener(JPanel p) {
            panel = p;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            int sizeIdx = (int) (Math.random() * gameSize.length);
            rNum = gameSize[sizeIdx];
            cNum = rNum;
            pNum = (int) (Math.random() * P_MAX) + 1;
            
            System.out.println("<Problem " + rNum + " x " + cNum + " - " + pNum + ">");
                        
            frame.remove(panel);
                        
            try {
                BorderPanel bPanel = new BorderPanel(rNum, cNum, pNum, frame);
                frame.getContentPane().add(bPanel);
                frame.pack();
                frame.setVisible(true);                
            } catch (IOException ex) {
                Logger.getLogger(TitlePanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
