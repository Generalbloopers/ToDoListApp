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
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

class Task implements Serializable {
    String description;
    boolean isCompleted;
    Date dueDate;
    int priority;
    String category;

    Task(String description, Date dueDate, int priority, String category) {
        this.description = description;
        this.isCompleted = false;
        this.dueDate = dueDate;
        this.priority = priority;
        this.category = category;
    }

    void markCompleted() {
        isCompleted = true;
    }

    void editTask(String newDescription, Date newDueDate, int newPriority, String newCategory) {
        this.description = newDescription;
        this.dueDate = newDueDate;
        this.priority = newPriority;
        this.category = newCategory;
    }

    @Override
    public String toString() {
        return (isCompleted ? "[✔]" : "[ ]") + " " + description + " (Due: " + new SimpleDateFormat("yyyy-MM-dd").format(dueDate) + ", Priority: " + priority + ", Category: " + category + ")";
    }
}

public class ToDoListApp {
    private static final String FILE_NAME = "tasks.ser";
    private static final String EXPORT_FILE = "tasks.csv";
    private static final ArrayList<Task> tasks = new ArrayList<>();
    private static JFrame frame;
    private static DefaultListModel<String> taskListModel;
    private static JList<String> taskList;
    private static JComboBox<String> filterOptions;
    private static Timer notificationTimer = new Timer(true);
    private static boolean isDarkMode = false;
    private static JComboBox<String> categoryFilter;
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ToDoListApp::createAndShowGUI);
        loadTasks();
        startNotificationChecker();
    }

    private static void createAndShowGUI() {
        frame = new JFrame("To-Do List");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 500);
        frame.setLayout(new BorderLayout());
        
        filterOptions = new JComboBox<>(new String[]{"All", "Completed", "Pending", "Sort by Due Date", "Sort by Priority"});
        filterOptions.addActionListener(e -> updateTaskList());
        
        categoryFilter = new JComboBox<>(new String[]{"All", "Work", "Personal", "Urgent", "Other"});
        categoryFilter.addActionListener(e -> updateTaskList());
        
        taskListModel = new DefaultListModel<>();
        taskList = new JList<>(taskListModel);
        taskList.setFont(new Font("Arial", Font.PLAIN, 14));
        
        JButton addButton = new JButton("Add Task");
        JButton completeButton = new JButton("Complete Task");
        JButton removeButton = new JButton("Remove Task");
        JButton editButton = new JButton("Edit Task");
        JButton exportButton = new JButton("Export Tasks");
        JButton importButton = new JButton("Import Tasks");
        JButton themeButton = new JButton("Toggle Dark Mode");
        
        addButton.setFont(new Font("Arial", Font.BOLD, 12));
        completeButton.setFont(new Font("Arial", Font.BOLD, 12));
        removeButton.setFont(new Font("Arial", Font.BOLD, 12));
        editButton.setFont(new Font("Arial", Font.BOLD, 12));
        exportButton.setFont(new Font("Arial", Font.BOLD, 12));
        importButton.setFont(new Font("Arial", Font.BOLD, 12));
        themeButton.setFont(new Font("Arial", Font.BOLD, 12));
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(3, 3, 5, 5));
        buttonPanel.add(addButton);
        buttonPanel.add(completeButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(editButton);
        buttonPanel.add(exportButton);
        buttonPanel.add(importButton);
        buttonPanel.add(themeButton);
        
        JPanel topPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        topPanel.add(filterOptions);
        topPanel.add(categoryFilter);
        
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(new JScrollPane(taskList), BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
        
        addButton.addActionListener(e -> addTask());
        completeButton.addActionListener(e -> completeTask());
        removeButton.addActionListener(e -> removeTask());
        editButton.addActionListener(e -> editTask());
        exportButton.addActionListener(e -> exportTasks());
        importButton.addActionListener(e -> importTasks());
        themeButton.addActionListener(e -> toggleTheme());
        
        updateTaskList();
    }



    private static void addTask() {
    String description = JOptionPane.showInputDialog("Enter task description:");
    if (description == null || description.trim().isEmpty()) return;

    String dateInput = JOptionPane.showInputDialog("Enter due date (yyyy-MM-dd):");
    Date dueDate;
    try {
        dueDate = new SimpleDateFormat("yyyy-MM-dd").parse(dateInput);
    } catch (Exception e) {
        dueDate = new Date();
    }

    String priorityInput = JOptionPane.showInputDialog("Enter priority level (1-5, 1 = Highest):");
    int priority = Integer.parseInt(priorityInput);

    // Ensure category selection is handled properly
    String[] categories = {"Work", "Personal", "Urgent", "Other"};
    String category = (String) JOptionPane.showInputDialog(frame, 
        "Select task category:", "Category", 
        JOptionPane.QUESTION_MESSAGE, null, 
        categories, "Other");

    if (category == null) category = "Other"; // Default category if none selected

    tasks.add(new Task(description, dueDate, priority, category));
    saveTasks();
    updateTaskList();
}

    private static void completeTask() {
        int index = taskList.getSelectedIndex();
        if (index != -1) {
            tasks.get(index).markCompleted();
            saveTasks();
            updateTaskList();
        }
    }

    private static void removeTask() {
        int index = taskList.getSelectedIndex();
        if (index != -1) {
            tasks.remove(index);
            saveTasks();
            updateTaskList();
        }
    }

    private static void editTask() {
        int index = taskList.getSelectedIndex();
        if (index != -1) {
            Task task = tasks.get(index);
            String newDescription = JOptionPane.showInputDialog("Edit task description:", task.description);
            String newDateInput = JOptionPane.showInputDialog("Edit due date (yyyy-MM-dd):", new SimpleDateFormat("yyyy-MM-dd").format(task.dueDate));
            String newPriorityInput = JOptionPane.showInputDialog("Edit priority level (1-5, 1 = Highest):", String.valueOf(task.priority));
            
            Date newDueDate;
            try {
                newDueDate = new SimpleDateFormat("yyyy-MM-dd").parse(newDateInput);
            } catch (Exception e) {
                newDueDate = task.dueDate;
            }
            
            int newPriority = Integer.parseInt(newPriorityInput);
            String newCategory = null;
            task.editTask(newDescription, newDueDate, newPriority, newCategory);
            saveTasks();
            updateTaskList();
        }
    }

    private static void updateTaskList() {
        taskListModel.clear();
        List<Task> filteredTasks = new ArrayList<>(tasks);
        
        String filter = (String) filterOptions.getSelectedItem();
        if ("Completed".equals(filter)) {
            filteredTasks.removeIf(task -> !task.isCompleted);
        } else if ("Pending".equals(filter)) {
            filteredTasks.removeIf(task -> task.isCompleted);
        } else if ("Sort by Due Date".equals(filter)) {
            filteredTasks.sort(Comparator.comparing(task -> task.dueDate));
        } else if ("Sort by Priority".equals(filter)) {
            filteredTasks.sort(Comparator.comparingInt(task -> task.priority));
        }
        
        for (Task task : filteredTasks) {
            taskListModel.addElement(task.toString());
        }
    }


    private static void saveTasks() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(tasks);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadTasks() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            Object obj = ois.readObject();
            if (obj instanceof ArrayList) {
                tasks.addAll((ArrayList<Task>) obj);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No previous tasks found, starting fresh.");
        }
    }

     private static void startNotificationChecker() {
        notificationTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkDueTasks();
            }
        }, 0, 60000); // Check every minute
    }

            private static void checkDueTasks() {
        Date now = new Date();
        for (Task task : tasks) {
            if (!task.isCompleted && task.dueDate.before(new Date(now.getTime() + 3600000))) { // Due within an hour
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, "Task Due Soon: " + task.description, "Reminder", JOptionPane.WARNING_MESSAGE));
            }
        }
    }

    private static void exportTasks() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(EXPORT_FILE))) {
            writer.println("Description,Due Date,Priority,Completed");
            for (Task task : tasks) {
                writer.println(task.description + "," + new SimpleDateFormat("yyyy-MM-dd").format(task.dueDate) + "," + task.priority + "," + task.isCompleted);
            }
            JOptionPane.showMessageDialog(frame, "Tasks exported successfully!", "Export", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error exporting tasks!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void importTasks() {
        try (BufferedReader reader = new BufferedReader(new FileReader(EXPORT_FILE))) {
            tasks.clear();
            reader.readLine(); // Skip header
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                String description = parts[0];
                Date dueDate;
                try {
                    dueDate = new SimpleDateFormat("yyyy-MM-dd").parse(parts[1]);
                } catch (ParseException e) {
                    continue; // Skip invalid dates
                }
                int priority = Integer.parseInt(parts[2]);
                boolean isCompleted = Boolean.parseBoolean(parts[3]);
                String catagory = null;
                Task task = new Task(description, dueDate, priority, catagory);
                if (isCompleted) task.markCompleted();
                tasks.add(task);
            }
            updateTaskList();
            JOptionPane.showMessageDialog(frame, "Tasks imported successfully!", "Import", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error importing tasks!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void toggleTheme() {
        isDarkMode = !isDarkMode;
        Color bgColor = isDarkMode ? Color.DARK_GRAY : Color.WHITE;
        Color fgColor = isDarkMode ? Color.WHITE : Color.BLACK;
        
        frame.getContentPane().setBackground(bgColor);
        taskList.setBackground(bgColor);
        taskList.setForeground(fgColor);
        
        filterOptions.setBackground(bgColor);
        filterOptions.setForeground(fgColor);
        
        frame.repaint();
    }
}

