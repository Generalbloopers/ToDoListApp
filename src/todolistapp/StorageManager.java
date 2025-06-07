/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author gener
 */
package todolistapp;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class StorageManager {
    private static final String TASK_FILE = "tasks.ser";
    private static final String EXPORT_FILE = "tasks.csv";
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public static void saveTasks(List<Task> tasks) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(TASK_FILE))) {
            oos.writeObject(tasks);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static List<Task> loadTasks() {
        List<Task> tasks = new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(TASK_FILE))) {
            Object obj = ois.readObject();
            if (obj instanceof List<?>) {
                tasks = (List<Task>) obj;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No saved tasks found. Starting fresh.");
        }
        return tasks;
    }

    public static void exportTasks(List<Task> tasks) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(EXPORT_FILE))) {
            writer.println("Description,Due Date,Priority,Completed");
            for (Task task : tasks) {
                writer.printf("%s,%s,%d,%b%n",
                        task.getDescription(),
                        FORMAT.format(task.getDueDate()),
                        task.getPriority(),
                        task.isCompleted());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Task> importTasks() {
        List<Task> tasks = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(EXPORT_FILE))) {
            reader.readLine(); // Skip header
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                String description = parts[0];
                Date dueDate = FORMAT.parse(parts[1]);
                int priority = Integer.parseInt(parts[2]);
                boolean completed = Boolean.parseBoolean(parts[3]);

                Task task = new Task(description, dueDate, priority, "Other", "None");
                if (completed) task.markCompleted();
                tasks.add(task);
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return tasks;
    }
}