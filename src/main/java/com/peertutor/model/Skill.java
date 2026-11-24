package com.peertutor.model;

/**
 * Represents a subject a tutor can teach (tutor profile entry).
 */
public class Skill {

    private int    skillId;
    private int    userId;
    private int    subjectId;
    private String description;

    // Convenience fields populated via JOIN
    private String subjectName;
    private String tutorName;

    public Skill() {}

    public Skill(int skillId, int userId, int subjectId, String description) {
        this.skillId     = skillId;
        this.userId      = userId;
        this.subjectId   = subjectId;
        this.description = description;
    }

    public int    getSkillId()     { return skillId;     }
    public int    getUserId()      { return userId;      }
    public int    getSubjectId()   { return subjectId;   }
    public String getDescription() { return description; }
    public String getSubjectName() { return subjectName; }
    public String getTutorName()   { return tutorName;   }

    public void setSkillId(int skillId)          { this.skillId     = skillId;     }
    public void setUserId(int userId)            { this.userId      = userId;      }
    public void setSubjectId(int subjectId)      { this.subjectId   = subjectId;   }
    public void setDescription(String desc)      { this.description = desc;        }
    public void setSubjectName(String name)      { this.subjectName = name;        }
    public void setTutorName(String name)        { this.tutorName   = name;        }

    @Override
    public String toString() {
        return subjectName != null ? subjectName : "Subject #" + subjectId;
    }
}
