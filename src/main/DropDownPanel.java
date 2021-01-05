/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/** The title panel when first start the application.
 * Give the user choices between randomly choose a puzzle or a specific number.
 * @author HRK
 */
public class DropDownPanel extends JPanel {
    
    private JComboBox solveCombo, sizeCombo, numCombo;
    private JFrame frame;    
    private int rNum, cNum, pNum;
    private int[] gameSize = {10, 15};   // either 5x5 or 10x10
    private final int P_MAX = 11;   // Number of problems available to choose from
    private Dimension D = new Dimension(0, 5);
    
    public DropDownPanel(JFrame frame) {
        
        this.frame = frame;
        
        rNum = cNum = pNum = -1;
        
        showTitlePanel();        
    }
    
    private void showTitlePanel() {
        JLabel label = new JLabel("How do you want to choose the puzzle?");
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        
        // Create the list of strings of puzzle selection for the combo box options.
        String[] solveOptions = {"Make a selection...", 
                    "Choose a specific puzzle.","Randomly choose a puzzle."};
        
        solveCombo = new JComboBox(solveOptions);
        solveCombo.setAlignmentX(Component.CENTER_ALIGNMENT);
        solveCombo.addActionListener(new SolveComboListener(this));
                
        // Set up this panel.
        setPreferredSize(new Dimension(350, 70));        
        setBackground(Colors.lightBlue);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(Box.createRigidArea(D)); 
        add(label);
        add(Box.createRigidArea(D));        
        add(solveCombo);     
        add(Box.createRigidArea(D)); 
    }
        
    private class SolveComboListener implements ActionListener {
        
        private JPanel panel;
        
        public SolveComboListener(JPanel p) {
            panel = p;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            
            panel.removeAll();
            
            int selected = solveCombo.getSelectedIndex();
            
            JLabel label1 = new JLabel();
            label1.setAlignmentX(Component.RIGHT_ALIGNMENT);
            label1.setFont(new Font("Arial", Font.BOLD, 16));
            
            // Create a button.        
            JButton gen = new JButton("Generate");
            gen.setForeground(Colors.navyBlue);
            gen.setMnemonic('g');
            
            if (selected == 1) {            
                
                label1.setText("Choosing a specific puzzle...");

                JLabel label1_1 = new JLabel("Select the size of the puzzle:");
                label1_1.setAlignmentX(Component.CENTER_ALIGNMENT);

                JPanel labels = new JPanel();
                labels.add(label1);
                labels.add(Box.createRigidArea(D)); 
                labels.add(label1_1);
                labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));
                labels.setBackground(Colors.lightBlue);

                // Create the list of strings of puzzle size for the combo box options.
                String[] sizeOptions = {"Make a selection...", "10 x 10", "15 x 15"};

                sizeCombo = new JComboBox(sizeOptions);
                sizeCombo.setAlignmentX(Component.CENTER_ALIGNMENT);
                sizeCombo.addActionListener(new SizeComboListener());

                JLabel label1_2 = new JLabel("Select the puzzle number:");
                label1_2.setAlignmentX(Component.CENTER_ALIGNMENT);

                // Create the list of strings of puzzle number for the combo box options.
                String[] numOptions = new String[P_MAX+1];
                numOptions[0] = "Make a selection...";
                for (int i = 1; i <= P_MAX; i++)
                    numOptions[i] = Integer.toString(i);

                numCombo = new JComboBox(numOptions);
                numCombo.setAlignmentX(Component.CENTER_ALIGNMENT);
                numCombo.addActionListener(new NumberComboListener());
                
                // Add button listener.
                gen.addActionListener(new SpecificButtonListener(panel));
                                
                // Set up the panel.
                panel.setPreferredSize(new Dimension(350, 170)); 
                panel.add(Box.createRigidArea(D));        
                panel.add(labels);
                panel.add(sizeCombo);
                panel.add(Box.createRigidArea(D));
                panel.add(label1_2);
                panel.add(numCombo);
                panel.add(Box.createRigidArea(D));                
                
            } else if (selected == 2) {
 
                label1.setText("Randomly choosing a puzzle...");
                
                // Add button listener.
                gen.addActionListener(new RandomButtonListener(panel));
                
                // Set up the panel.
                panel.add(Box.createRigidArea(D));
                panel.add(label1);
                panel.add(Box.createRigidArea(D));
                panel.setPreferredSize(new Dimension(350, 60)); 
            }
            
            // Create a button.        
            JButton back = new JButton("Go back");
            back.setForeground(Colors.navyBlue);
            back.setMnemonic('b');
            back.addActionListener(new BackButtonListener(panel));
            
            
            JPanel buttons = new JPanel();
            buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
            buttons.add(gen);
            buttons.add(Box.createRigidArea(new Dimension(5, 0)));
            buttons.add(back);
            buttons.setAlignmentX(Component.CENTER_ALIGNMENT);
            buttons.setBackground(Colors.lightBlue);
            
            panel.add(buttons);
            
            frame.pack();
        }        
    }
    
    private class SizeComboListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            int selected = sizeCombo.getSelectedIndex();
            if (selected == 1)
                rNum = 10;
            else if (selected == 2)
                rNum = 15;            
            cNum = rNum;
        }        
    }
    
    private class NumberComboListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            pNum = numCombo.getSelectedIndex();            
        }        
    }
    
    
    private class SpecificButtonListener implements ActionListener {
        
        private JPanel panel;
        public SpecificButtonListener(JPanel p) {
            panel = p;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {            
            
            String message = "";
            if (rNum == -1 && pNum == -1)
                message = "Please select a puzzle size AND a problem number.";
            else if (rNum == -1)
                message = "Please select a puzzle size.";
            else if (pNum == -1)
                message = "Please select a problem number.";
            
            if (message.isEmpty()) {
                System.out.println("<Problem " + rNum + " x " + cNum + " - " + pNum + ">");

                frame.remove(panel);

                try {
                    BorderPanel bPanel = new BorderPanel(rNum, cNum, pNum, frame);
                    frame.getContentPane().add(bPanel);
                    frame.pack();
                    frame.setVisible(true);                      
                } catch (IOException ex) {
                    Logger.getLogger(DropDownPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else
                JOptionPane.showMessageDialog(null, message);
        }
    }

    private class RandomButtonListener implements ActionListener {
        
        private JPanel panel;
        public RandomButtonListener(JPanel p) {
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
                Logger.getLogger(DropDownPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private class BackButtonListener implements ActionListener {

        private JPanel panel;
        public BackButtonListener(JPanel p) {
            panel = p;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            panel.removeAll();
            showTitlePanel();
            frame.pack();
        }        
    }
}
