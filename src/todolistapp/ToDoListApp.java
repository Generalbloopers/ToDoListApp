/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package todolistapp;

/**
 *
 * @author gener
 */

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;


public class ToDoListApp {
    private static final ArrayList<Task> tasks = new ArrayList<>();
    private static final Timer notificationTimer = new Timer(true);

    private static JFrame frame;
    private static DefaultListModel<String> taskListModel;
    private static JList<String> taskList;
    private static JComboBox<String> filterOptions;
    private static JComboBox<String> categoryFilter;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            tasks.addAll(StorageManager.loadTasks());
            createAndShowGUI();
            startNotificationChecker();
        });
    }

    private static void createAndShowGUI() {
        frame = new JFrame("To-Do List");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(550, 500);
        frame.setLayout(new BorderLayout());

        filterOptions = new JComboBox<>(new String[]{"All", "Completed", "Pending", "Sort by Due Date", "Sort by Priority"});
        filterOptions.addActionListener(e -> updateTaskList());

        categoryFilter = new JComboBox<>(new String[]{"All", "Work", "Personal", "Urgent", "Other"});
        categoryFilter.addActionListener(e -> updateTaskList());

        taskListModel = new DefaultListModel<>();
        taskList = new JList<>(taskListModel);
        taskList.setFont(new Font("Arial", Font.PLAIN, 14));
        taskList.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton[] buttons = {
            createButton("Add Task", ToDoListApp::addTask),
            createButton("Add Subtask", ToDoListApp::addSubtask),
            createButton("Update Progress", ToDoListApp::updateProgress),
            createButton("Complete Task", ToDoListApp::completeTask),
            createButton("Remove Task", ToDoListApp::removeTask),
            createButton("Edit Task", ToDoListApp::editTask),
            createButton("Export Tasks", () -> {
                StorageManager.exportTasks(tasks);
                JOptionPane.showMessageDialog(frame, "Exported successfully.");
            }),
            createButton("Import Tasks", () -> {
                tasks.clear();
                tasks.addAll(StorageManager.importTasks());
                updateTaskList();
                JOptionPane.showMessageDialog(frame, "Imported successfully.");
            }),
            createButton("Toggle Dark Mode", () -> {
                ThemeManager.toggleDarkMode();
                ThemeManager.applyTheme(frame, taskList, filterOptions, categoryFilter);
            })
        };

        JPanel buttonPanel = new JPanel(new GridLayout(3, 3, 8, 8));
        for (JButton b : buttons) buttonPanel.add(b);

        JPanel topPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        topPanel.add(filterOptions);
        topPanel.add(categoryFilter);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(new JScrollPane(taskList), BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        ThemeManager.applyTheme(frame, taskList, filterOptions, categoryFilter);
        frame.setVisible(true);
        updateTaskList();
    }

    private static JButton createButton(String label, Runnable action) {
        JButton button = new JButton(label);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.addActionListener(e -> action.run());
        return button;
    }

    private static void updateTaskList() {
        taskListModel.clear();
        List<Task> filteredTasks = new ArrayList<>(tasks);

        String filter = (String) filterOptions.getSelectedItem();
        if ("Completed".equals(filter)) {
            filteredTasks.removeIf(task -> !task.isCompleted());
        } else if ("Pending".equals(filter)) {
            filteredTasks.removeIf(Task::isCompleted);
        } else if ("Sort by Due Date".equals(filter)) {
            filteredTasks.sort(Comparator.comparing(Task::getDueDate));
        } else if ("Sort by Priority".equals(filter)) {
            filteredTasks.sort(Comparator.comparingInt(Task::getPriority));
        }

        String category = (String) categoryFilter.getSelectedItem();
        if (!"All".equals(category)) {
            filteredTasks.removeIf(task -> !category.equals(task.getCategory()));
        }

        for (Task task : filteredTasks) {
            taskListModel.addElement(task.toString());
        }
    }

    private static void addTask() {
        Task task = promptTaskDetails();
        if (task != null) {
            tasks.add(task);
            StorageManager.saveTasks(tasks);
            updateTaskList();
        }
    }

    private static void addSubtask() {
        int index = taskList.getSelectedIndex();
        if (index == -1) return;
        Task parent = tasks.get(index);
        Task subtask = promptTaskDetails();
        if (subtask != null) {
            parent.addSubtask(subtask);
            StorageManager.saveTasks(tasks);
            updateTaskList();
        }
    }

    private static Task promptTaskDetails() {
        try {
            String description = JOptionPane.showInputDialog("Enter task description:");
            if (description == null || description.trim().isEmpty()) return null;

            String dateStr = JOptionPane.showInputDialog("Enter due date (yyyy-MM-dd):");
            Date dueDate = new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);

            int priority = Integer.parseInt(JOptionPane.showInputDialog("Enter priority (1-5):"));

            String[] categories = {"Work", "Personal", "Urgent", "Other"};
            String category = (String) JOptionPane.showInputDialog(frame, "Category:", "Category",
                    JOptionPane.QUESTION_MESSAGE, null, categories, "Other");

            String[] recurrence = {"None", "Daily", "Weekly", "Monthly"};
            String recur = (String) JOptionPane.showInputDialog(frame, "Recurrence:", "Recurrence",
                    JOptionPane.QUESTION_MESSAGE, null, recurrence, "None");

            return new Task(description, dueDate, priority, category, recur);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Invalid input.", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private static void completeTask() {
        int index = taskList.getSelectedIndex();
        if (index != -1) {
            Task task = tasks.get(index);
            task.markCompleted();
            task.applyRecurrence();
            StorageManager.saveTasks(tasks);
            updateTaskList();
        }
    }

    private static void removeTask() {
        int index = taskList.getSelectedIndex();
        if (index != -1) {
            tasks.remove(index);
            StorageManager.saveTasks(tasks);
            updateTaskList();
        }
    }

    private static void editTask() {
        int index = taskList.getSelectedIndex();
        if (index != -1) {
            Task task = tasks.get(index);
            try {
                String newDesc = JOptionPane.showInputDialog("Edit description:", task.getDescription());
                String dateStr = JOptionPane.showInputDialog("Edit due date (yyyy-MM-dd):",
                        new SimpleDateFormat("yyyy-MM-dd").format(task.getDueDate()));
                int priority = Integer.parseInt(JOptionPane.showInputDialog("Edit priority:", task.getPriority()));

                String[] categories = {"Work", "Personal", "Urgent", "Other"};
                String category = (String) JOptionPane.showInputDialog(frame, "Edit category:", "Category",
                        JOptionPane.QUESTION_MESSAGE, null, categories, task.getCategory());

                String[] recurrence = {"None", "Daily", "Weekly", "Monthly"};
                String recur = (String) JOptionPane.showInputDialog(frame, "Edit recurrence:", "Recurrence",
                        JOptionPane.QUESTION_MESSAGE, null, recurrence, task.getRecurrence());

                Date dueDate = new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
                task.editTask(newDesc, dueDate, priority, category, recur);
                StorageManager.saveTasks(tasks);
                updateTaskList();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame, "Edit failed.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void updateProgress() {
        int index = taskList.getSelectedIndex();
        if (index == -1) return;
        Task task = tasks.get(index);
        try {
            int progress = Integer.parseInt(JOptionPane.showInputDialog("Enter progress (0–100):", task.getProgress()));
            task.updateProgress(progress);
            StorageManager.saveTasks(tasks);
            updateTaskList();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Invalid input.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void startNotificationChecker() {
        notificationTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Date now = new Date();
                for (Task task : tasks) {
                    if (!task.isCompleted() && task.getDueDate().before(new Date(now.getTime() + 3600000))) {
                        SwingUtilities.invokeLater(() ->
                            JOptionPane.showMessageDialog(frame, "Task Due Soon: " + task.getDescription(),
                                    "Reminder", JOptionPane.WARNING_MESSAGE));
                    }
                }
            }
        }, 0, 60000);
    }
}

