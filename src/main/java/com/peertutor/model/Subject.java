package com.peertutor.model;

/**
 * Represents an academic subject.
 */
public class Subject {

    private int    subjectId;
    private String subjectName;

    public Subject() {}

    public Subject(int subjectId, String subjectName) {
        this.subjectId   = subjectId;
        this.subjectName = subjectName;
    }

    public int    getSubjectId()   { return subjectId;   }
    public String getSubjectName() { return subjectName; }

    public void setSubjectId(int subjectId)        { this.subjectId   = subjectId;   }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    /** Used by ComboBox to display the name. */
    @Override
    public String toString() { return subjectName; }
}
