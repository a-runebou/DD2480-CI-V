package com.ci;

public class BuildEntry {
    public String sha;
    public String branch;
    public String buildResult;
    public String buildDescription;
    public String buildDate;

    public BuildEntry(String sha, String branch, String result, String description, String date) {
        this.sha = sha;
        this.branch = branch;
        this.buildResult = result;
        this.buildDescription = description;
        this.buildDate = date;
    }

    @Override
    public String toString() {
        return "Entry: sha = " + sha + ", branch = " + branch + ", result = " +buildResult + ", description = " + buildDescription + ", date = " + buildDate;
    }
}
