package org.vb.hotspotter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.TreeMap;

import static java.util.Objects.nonNull;

public class Reporter {
    
    private Configuration configuration = Configuration.getInstance();
    private Map<String, Integer> mapFileLineCounts = Maps.newHashMap();
    private ReportWriter reportWriter = null;

    public Reporter(String propertiesFile) {
        configuration.init(loadProperties(propertiesFile));
        reportWriter = new ReportWriter(configuration);
    }

    public Reporter(Properties properties) {
        configuration.init(properties);
    }

    public static void main(String[] args) throws IOException, GitAPIException {
        Reporter reporter = new Reporter("/" + args[0] + ".properties");
        reporter.process();
    }

    private Properties loadProperties(String propertiesFileName) {
        final Properties properties = new Properties();
        InputStream input = null;
        try {

            input = this.getClass().getResourceAsStream(propertiesFileName);
            // load a properties file
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return properties;
    }

    public void process() throws IOException, GitAPIException {
        final Map<String, List<CommitInfo>> fileCommits = findFileCommits();
        final List<ReportLineItem> lines = getReportData(fileCommits);
        normalize(lines);
        reportWriter.writeReport(sort(lines));
        reportWriter.writeRawData(fileCommits, mapFileLineCounts);
    }


    private List<ReportLineItem> getReportData(final Map<String, List<CommitInfo>> fileCommmits) {
        final List<ReportLineItem> lines = Lists.newArrayList();
        ReportLineItem reportLineItem;
        List<CommitInfo> commitInfos;
        int commitCount = 0;
        for (String path : fileCommmits.keySet()) {
            reportLineItem = new ReportLineItem();
            reportLineItem.setPath(path);
            commitInfos = fileCommmits.get(path);
            commitCount = commitInfos.size();
            if (commitCount < configuration.getMinCommitCountToBeReported()) continue;
            reportLineItem.setCommitCount(commitCount);
            reportLineItem.setCommitsAge(calculateCommitsAge(commitInfos));
            reportLineItem.calculateAvgCommitAge();
            reportLineItem.setAuthors(findAuthorsFromCommits(commitInfos));
            lines.add(reportLineItem);
        }
        processCountOfLines(lines);
        return lines;
    }

    private Map<String, Integer> findAuthorsFromCommits(List<CommitInfo> commitInfos) {
        Map<String, Integer> authors = Maps.newHashMap();
        String key = null;
        for (CommitInfo commitInfo : commitInfos) {
            key = commitInfo.getAuthorEmail();
            if (authors.containsKey(key)) {
                authors.put(key, authors.get(key) + 1);
            } else {
                authors.put(key, 1);
            }
        }
        return authors;
    }

    private void processCountOfLines(List<ReportLineItem> lines) {
        int countOfLines;
        for (ReportLineItem lineItem : lines) {
            countOfLines = Utils.findCountOfLines(configuration.getRepositoryPath() + "/" + lineItem.getPath());
            mapFileLineCounts.put(lineItem.getPath(), countOfLines);
            lineItem.setLinesCount(countOfLines);
        }
    }


    private void normalize(List<ReportLineItem> lines) {
        double maxCommitCount = 0;
        double maxCommitsAge = 0;
        double maxAvgCommitAge = 0;
        double maxLinesCount = 0;
        for (ReportLineItem line : lines) {
            if (line.getAvgCommitAge() > maxAvgCommitAge) maxAvgCommitAge = line.getAvgCommitAge();
            if (line.getCommitCount() > maxCommitCount) maxCommitCount = line.getCommitCount();
            if (line.getCommitsAge() > maxCommitsAge) maxCommitsAge = line.getCommitsAge();
            if (line.getLinesCount() > maxLinesCount) maxLinesCount = line.getLinesCount();
        }
        double normalisationFactor = maxAvgCommitAge / maxCommitCount;

        for (ReportLineItem line : lines) {
            line.setNormalisedAvgCommitAge(line.getAvgCommitAge() / normalisationFactor);
            line.setRecency(maxCommitCount - line.getNormalisedAvgCommitAge());
            line.setNormalisedCommitCount(line.getCommitCount() * (100 / maxCommitCount));
            line.setNormalisedCommitsAge(line.getCommitsAge() * (100 / maxCommitsAge));
            line.setNormalisedLinesCount(line.getLinesCount() * (100 / maxLinesCount));
        }
    }

    private List<ReportLineItem> sort(List<ReportLineItem> lines) {
        Collections.sort(lines, (o1, o2) -> {
            if (o1.getCommitCount() > o2.getCommitCount()) {
                return -1;
            } else if (o1.getCommitCount() < o2.getCommitCount()) {
                return 1;
            } else {
                return 0;
            }
        });
        return lines;
    }

    private long calculateCommitsAge(final List<CommitInfo> commitInfos) {
        long commitsAge = 0;
        for (CommitInfo commitInfo : commitInfos) {
            commitsAge += commitInfo.getAgeInDays();
        }
        return commitsAge;
    }


    private Map<String, List<CommitInfo>> findFileCommits() {
        final Repository repository = getRepository();
        Git git = new Git(repository);

        DiffFormatter df = getDiffFormatter(repository);

        Map<String, List<CommitInfo>> fileCommits = new TreeMap<String, List<CommitInfo>>();
        String newPath = null;

        final Iterable<RevCommit> revCommits;
        try {
            final LogCommand logCommand = git.log();
            if (nonNull(configuration.getSourcePath())) {
                logCommand.addPath(configuration.getSourcePath());
            }
            logCommand.setMaxCount(configuration.getMaxCommitsCount());
            revCommits = logCommand.call();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
        int counter = 0;
        String comment = null;
        for (RevCommit revCommit : revCommits) {
            if (revCommit.getParentCount() == 0) continue;
            RevCommit parent = revCommit.getParent(0);
            ++counter;
            List<DiffEntry> diffs = null;
            try {
                diffs = df.scan(parent.getTree(), revCommit.getTree());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            CommitInfo commitInfo = null;
            for (DiffEntry diff : diffs) {
                newPath = diff.getNewPath();
                comment = revCommit.getShortMessage();
                if (shouldExcludeFile(newPath)) continue;
                if (shouldExcludeCommitsWithComments(comment)) continue;
                List<CommitInfo> commitInfos = fileCommits.get(newPath);
                if (Objects.isNull(commitInfos)) {
                    commitInfos = new ArrayList<CommitInfo>();
                }

                commitInfo = new CommitInfo();
                commitInfo.setId(revCommit.getName());
                commitInfo.setAuthorEmail(revCommit.getCommitterIdent().getEmailAddress());
                commitInfo.setComment(comment);
                commitInfo.setTime(revCommit.getCommitterIdent().getWhen());

                if (commitInfo.getAgeInDays() > configuration.getIgnoreCommitsOlderThanDays()) continue;
                System.out.println(counter + "|" + commitInfo + "|" + newPath + "|" + diff.getChangeType());

                commitInfos.add(commitInfo);
                fileCommits.put(newPath, commitInfos);
            }
        }
        return fileCommits;
    }

    private boolean shouldExcludeFile(String path) {
        for (String exclusion : configuration.getExclusionsPathContains()) {
            if (path.contains(exclusion)) return true;
        }
        for (String exclusion : configuration.getExclusionsCommentsStartsWith()) {
            if (path.startsWith(exclusion)) return true;
        }
        for (String exclusion : configuration.getExclusionsFileExtensions()) {
            if (path.endsWith(exclusion)) return true;
        }
        if (!path.contains(configuration.getSourcePath())) return true;
        //if(!path.contains("HedgingServiceImpl.java")) return true;
        return false;
    }

    private boolean shouldExcludeCommitsWithComments(String comment) {
        for (String exclusion : configuration.getExclusionsCommentsStartsWith()) {
            if (comment.startsWith(exclusion)) return true;
        }
        return false;
    }

    private static DiffFormatter getDiffFormatter(final Repository repository) {
        DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
        df.setRepository(repository);
        df.setDiffComparator(RawTextComparator.DEFAULT);
        df.setDetectRenames(true);
        return df;
    }

    private Repository getRepository() {
        try {
            return new FileRepositoryBuilder().setGitDir(new File(configuration.getRepositoryPath() + "/.git")).setMustExist(true).build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
