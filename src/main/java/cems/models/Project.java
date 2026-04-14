package cems.models;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
public class Project implements Cloneable {
    
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private int progressPercent;

    // --- PROTOTYPE PATTERN ---
    @Override
    public Project clone() {
        try {
            Project cloned = (Project) super.clone();
            cloned.id = null; // Detach from database so it creates a new row
            cloned.progressPercent = 0; // Reset progress for the new team
            cloned.title = this.title + " (Clone)";
            return cloned;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    // Safety check to prevent duplicate assignment crashes
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return Objects.equals(id, project.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getProgressPercent() { return progressPercent; }
    public void setProgressPercent(int progressPercent) { this.progressPercent = progressPercent; }
}