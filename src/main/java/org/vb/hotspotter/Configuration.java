package org.vb.hotspotter;

import java.util.Properties;

import static java.util.Objects.nonNull;

public class Configuration {

    private static Configuration configuration = new Configuration();

    private String dateFormat = "yyyy-MM-dd";
    private String reportFileName;
    private String repositoryPath;
    private String sourcePath;
    private int minCommitCountToBeReported = 2;
    private int ignoreCommitsOlderThanDays = 730;
    private String[] exclusionsPathContains;
    private String[] exclusionsCommentsStartsWith;
    private String[] exclusionsFileExtensions;
    private int maxCommitsCount = 20000;

    public void init(final Properties properties) {
        this.repositoryPath = properties.getProperty("repository.path");
        this.sourcePath = properties.getProperty("source.path");
        this.reportFileName = properties.getProperty("report.file");
        this.dateFormat = properties.getProperty("date.format");
        String property = null;

        property = properties.getProperty("min.commit.count.to.report");
        if (nonNull(property)) this.minCommitCountToBeReported = Integer.parseInt(property);

        property = properties.getProperty("max.commit.count.for.analysis");
        if (nonNull(property)) this.maxCommitsCount = Integer.parseInt(property);

        property = properties.getProperty("ignore.commit.older.than.days");
        if (nonNull(property)) this.ignoreCommitsOlderThanDays = Integer.parseInt(property);

        property = properties.getProperty("exclusions.path.contains");
        if (nonNull(property)) this.exclusionsPathContains = property.split(",");
        property = properties.getProperty("exclusions.comments.prefix");
        if (nonNull(property)) this.exclusionsCommentsStartsWith = property.split(",");
        property = properties.getProperty("exclusions.file.extensions");
        if (nonNull(property)) this.exclusionsFileExtensions = property.split(",");
    }


    public String getRepositoryPath() {
        return repositoryPath;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public int getMinCommitCountToBeReported() {
        return minCommitCountToBeReported;
    }

    public int getIgnoreCommitsOlderThanDays() {
        return ignoreCommitsOlderThanDays;
    }

    public String[] getExclusionsPathContains() {
        return exclusionsPathContains;
    }

    public String[] getExclusionsCommentsStartsWith() {
        return exclusionsCommentsStartsWith;
    }

    public String[] getExclusionsFileExtensions() {
        return exclusionsFileExtensions;
    }


    public String getDateFormat() {
        return dateFormat;
    }

    public String getReportFileName() {
        return reportFileName;
    }

    public int getMaxCommitsCount() {
        return maxCommitsCount;
    }

    public static Configuration getInstance() {
        return configuration;
    }

}
