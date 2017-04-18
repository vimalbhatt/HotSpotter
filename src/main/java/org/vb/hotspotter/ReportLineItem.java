package org.vb.hotspotter;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Set;

public class ReportLineItem {
    private String path;
    private String abbrvPath;
    private double commitCount;
    private double normalisedCommitCount;
    private double commitsAge;
    private double normalisedCommitsAge;
    private double avgCommitAge;
    private double normalisedAvgCommitAge;
    private double recency;
    private int linesCount;
    private double normalisedLinesCount;
    private Map<String, Integer> authors = Maps.newHashMap();

    public void calculateAvgCommitAge() {
        setAvgCommitAge(getCommitsAge() / getCommitCount());
    }

    public String getPath() {
        return path;
    }

    public String getAbbrvPath() {
        return abbrvPath;
    }

    public Map<String, Integer> getAuthors() {
        return authors;
    }
    
    public void setPath(String path) {
        if (path.contains("/com/")) {
            abbrvPath = abbreviatePackage(path);
        }
        this.path = path;
    }

    protected String abbreviatePackage(String text) {
        StringBuilder result = new StringBuilder();
        final String[] words = text.split("/");
        for (String word : words) {
            if (word.trim().length() == 0) continue;
            if (word.contains(".")) {
                result.append(word);
            } else {
                result.append(word.charAt(0));
                result.append(".");
            }
        }
        return result.toString();
    }

    public double getCommitCount() {
        return commitCount;
    }

    public void setCommitCount(double commitCount) {
        this.commitCount = commitCount;
    }

    public double getCommitsAge() {
        return commitsAge;
    }

    public void setCommitsAge(double commitsAge) {
        this.commitsAge = commitsAge;
    }

    public double getAvgCommitAge() {
        return avgCommitAge;
    }

    private void setAvgCommitAge(double avgCommitAge) {
        this.avgCommitAge = avgCommitAge;
    }

    public double getNormalisedAvgCommitAge() {
        return normalisedAvgCommitAge;
    }

    public void setNormalisedAvgCommitAge(double normalisedAvgCommitAge) {
        this.normalisedAvgCommitAge = normalisedAvgCommitAge;
    }

    public double getRecency() {
        return recency;
    }

    public void setRecency(double recency) {
        this.recency = recency;
    }

    public int getLinesCount() {
        return linesCount;
    }

    public void setLinesCount(int linesCount) {
        this.linesCount = linesCount;
    }

    public double getNormalisedCommitCount() {
        return normalisedCommitCount;
    }

    public void setNormalisedCommitCount(double normalisedCommitCount) {
        this.normalisedCommitCount = normalisedCommitCount;
    }

    public double getNormalisedCommitsAge() {
        return normalisedCommitsAge;
    }

    public void setNormalisedCommitsAge(double normalisedCommitsAge) {
        this.normalisedCommitsAge = normalisedCommitsAge;
    }

    public double getNormalisedLinesCount() {
        return normalisedLinesCount;
    }

    public void setNormalisedLinesCount(double normalisedLinesCount) {
        this.normalisedLinesCount = normalisedLinesCount;
    }

    public void setAuthors(Map<String, Integer> authors) {
        this.authors = authors;
    }
    
    public String getAuthorsLine() {
        StringBuilder text = new StringBuilder();
        for(String author : authors.keySet()) {
            text.append(author).append("[").append(authors.get(author)).append("]|");
        }
        return text.toString();
    }

    public String getAuthorCommitsLine(final Set<String> authorsInSequence) {
        StringBuilder text = new StringBuilder();
        for(String author : authorsInSequence) {
            text.append(getAuthorCommitCount(author)).append(",");
        }
        return text.toString();
    }
    private int getAuthorCommitCount(String author) {
        return authors.containsKey(author)?authors.get(author):0;
    }
    
}
