/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author gener
 */
package todolistapp;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

public class Task implements Serializable {
    private String description;
    private boolean isCompleted;
    private Date dueDate;
    private int priority;
    private String category;
    private String recurrence;
    private List<Task> subtasks;
    private int progress;

    public Task(String description, Date dueDate, int priority, String category, String recurrence) {
        this.description = description;
        this.isCompleted = false;
        this.dueDate = dueDate;
        this.priority = priority;
        this.category = category;
        this.recurrence = recurrence;
        this.subtasks = new ArrayList<>();
        this.progress = 0;
    }

    public void markCompleted() {
        isCompleted = true;
        progress = 100;
        for (Task subtask : subtasks) {
            subtask.markCompleted();
        }
    }

    public void editTask(String newDescription, Date newDueDate, int newPriority, String newCategory, String newRecurrence) {
        this.description = newDescription;
        this.dueDate = newDueDate;
        this.priority = newPriority;
        this.category = newCategory;
        this.recurrence = newRecurrence;
        this.progress = 0;
    }

    public void updateProgress(int newProgress) {
        this.progress = Math.max(0, Math.min(100, newProgress));
        if (progress == 100) {
            markCompleted();
        }
    }

    public void applyRecurrence() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dueDate);

        switch (recurrence) {
            case "Daily" -> cal.add(Calendar.DATE, 1);
            case "Weekly" -> cal.add(Calendar.DATE, 7);
            case "Monthly" -> cal.add(Calendar.MONTH, 1);
        }

        dueDate = cal.getTime();
        isCompleted = false;
        progress = 0;
    }

    public void addSubtask(Task subtask) {
        subtasks.add(subtask);
    }

    public String getDescription() {
        return description;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public int getPriority() {
        return priority;
    }

    public String getCategory() {
        return category;
    }

    public String getRecurrence() {
        return recurrence;
    }

    public int getProgress() {
        return progress;
    }

    public List<Task> getSubtasks() {
        return subtasks;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(isCompleted ? "[✔]" : "[ ]").append(" ").append(description)
          .append(" (Due: ").append(new SimpleDateFormat("yyyy-MM-dd").format(dueDate))
          .append(", Priority: ").append(priority)
          .append(", Category: ").append(category)
          .append(", Recurrence: ").append(recurrence)
          .append(", Progress: ").append(progress).append("%)");

        if (!subtasks.isEmpty()) {
            sb.append("\n  Subtasks:");
            for (Task subtask : subtasks) {
                sb.append("\n    - ").append(subtask.toString());
            }
        }

        return sb.toString();
    }
}
