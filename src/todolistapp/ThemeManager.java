/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package todolistapp;

/**
 *
 * @author gener
 */
import javax.swing.*;
import java.awt.*;

public class ThemeManager {
    private static boolean isDarkMode = false;

    public static void toggleDarkMode() {
        isDarkMode = !isDarkMode;
    }

    public static boolean isDarkMode() {
        return isDarkMode;
    }

    public static void applyTheme(JFrame frame, JList<?> taskList, JComboBox<?>... combos) {
        Color bgColor = isDarkMode ? Color.DARK_GRAY : Color.WHITE;
        Color fgColor = isDarkMode ? Color.WHITE : Color.BLACK;

        frame.getContentPane().setBackground(bgColor);
        taskList.setBackground(bgColor);
        taskList.setForeground(fgColor);

        for (JComboBox<?> combo : combos) {
            combo.setBackground(bgColor);
            combo.setForeground(fgColor);
        }

        frame.repaint();
    }
}
