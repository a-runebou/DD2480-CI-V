package com.ci;

/**
 * The BuildEntry class represents a single build entry in the database.
 * It contains information about the commit SHA, branch, build result, description, and date of the build.
 */
public class BuildEntry {
    public int id;
    public String sha;
    public String branch;
    public String buildResult;
    public String buildDescription;
    public String buildDate;

    
    public BuildEntry(int id, String sha, String branch, String result, String description, String date) {
        this.id = id;
        this.sha = sha;
        this.branch = branch;
        this.buildResult = result;
        this.buildDescription = description;
        this.buildDate = date;
    }

    /**
     * Returns a string representation of the BuildEntry object, including the SHA, branch, build result, description, and date.
     * @return a string representation of the BuildEntry object
     */
    @Override
    public String toString() {
        return "Entry: sha = " + sha + ", branch = " + branch + ", result = " +buildResult + ", description = " + buildDescription + ", date = " + buildDate;
    }
}
