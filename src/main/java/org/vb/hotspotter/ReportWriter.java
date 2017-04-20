package org.vb.hotspotter;

import com.google.common.collect.Sets;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.vb.hotspotter.Utils.getFileName;

public class ReportWriter {
    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final String COL_SEPARATOR = "|";
    private final Configuration configuration;

    public ReportWriter(Configuration configuration) {
        this.configuration = configuration;
    }

    public void writeRawData(final Map<String, List<CommitInfo>> fileCommits, final Map<String, Integer> mapFileLineCounts) {
        final File file = new File("raw-data.csv");
        final FileWriter fileWriter;
        try {
            if (file.exists()) {
                file.delete();
            }

            fileWriter = new FileWriter(file);
            fileWriter.append("file")
                    .append(COL_SEPARATOR)
                    .append("commitId")
                    .append(COL_SEPARATOR)
                    .append("author")
                    .append(COL_SEPARATOR)
                    .append("comment")
                    .append(COL_SEPARATOR)
                    .append("time")
                    .append(COL_SEPARATOR)
                    .append("ageInDays")
                    .append(COL_SEPARATOR)
                    .append("loc")
                    .append(COL_SEPARATOR)
                    .append("path")
                    .append(LINE_SEPARATOR);

            String commitDate = null;

            for (String path : fileCommits.keySet()) {
                final List<CommitInfo> commitInfos = fileCommits.get(path);

                for (CommitInfo commitInfo : commitInfos) {
                    commitDate = new SimpleDateFormat(configuration.getDateFormat()).format(commitInfo.getTime());
                    fileWriter.append(getFileName(path) + COL_SEPARATOR + commitInfo.getId() + COL_SEPARATOR + commitInfo.getAuthorEmail() + COL_SEPARATOR + commitInfo.getComment() + COL_SEPARATOR + commitDate + COL_SEPARATOR + commitInfo.getAgeInDays()
                            + COL_SEPARATOR
                            + (mapFileLineCounts.containsKey(path) ? mapFileLineCounts.get(path) : "")
                            + COL_SEPARATOR + path + LINE_SEPARATOR);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void writeReport(List<ReportLineItem> lines) {
        final File file = new File(configuration.getReportFileName());
        final FileWriter fileWriter;
        try {
            if (file.exists()) {
                file.delete();
            }
            fileWriter = new FileWriter(file);
            Set<String> authors = getAuthors(lines);
            fileWriter.append("path,commitCount,recency,normalisedAvgCommitAge,avgCommitAge,commitsAge,loc,Nloc,NcomitCount,NcommitAge,fullPath,")
                    .append(getAuthorsLine(authors))
                    .append(LINE_SEPARATOR);
            for (ReportLineItem line : lines) {
                fileWriter.append(line.getAbbrvPath() + COL_SEPARATOR + line.getCommitCount() + COL_SEPARATOR + line.getRecency() + COL_SEPARATOR + line.getNormalisedAvgCommitAge() + COL_SEPARATOR + line.getAvgCommitAge() + COL_SEPARATOR + line.getCommitsAge()
                        + COL_SEPARATOR + line.getLinesCount()
                        + COL_SEPARATOR + line.getNormalisedLinesCount()
                        + COL_SEPARATOR + line.getNormalisedCommitCount()
                        + COL_SEPARATOR + line.getNormalisedCommitsAge()
                        + COL_SEPARATOR + line.getPath()
                        + COL_SEPARATOR + line.getAuthorCommitsLine(authors)
                        + COL_SEPARATOR + line.getAuthorsLine()
                        + LINE_SEPARATOR);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Set<String> getAuthors(final List<ReportLineItem> lines) {
        final Set<String> authors = Sets.newTreeSet();
        for (ReportLineItem line : lines) {
            for (String author : line.getAuthors().keySet()) {
                authors.add(author);
            }
        }
        return authors;
    }

    private String getAuthorsLine(final Set<String> authors) {
        StringBuilder authorsText = new StringBuilder();
        for (String author : authors) {
            authorsText.append(author).append(COL_SEPARATOR);
        }
        return authorsText.toString();
    }

}
