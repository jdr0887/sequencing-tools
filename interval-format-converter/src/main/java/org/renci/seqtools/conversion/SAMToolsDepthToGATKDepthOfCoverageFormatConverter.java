package org.renci.seqtools.conversion;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SAMToolsDepthToGATKDepthOfCoverageFormatConverter implements Callable<Void> {

    private static final Logger logger = LoggerFactory.getLogger(SAMToolsDepthToGATKDepthOfCoverageFormatConverter.class);

    private static final HelpFormatter helpFormatter = new HelpFormatter();

    private static final Options cliOptions = new Options();

    private File input;

    private File intervals;

    private File output;

    public SAMToolsDepthToGATKDepthOfCoverageFormatConverter() {
        super();
    }

    public SAMToolsDepthToGATKDepthOfCoverageFormatConverter(File input, File intervals, File output) {
        super();
        this.input = input;
        this.intervals = intervals;
        this.output = output;
    }

    @Override
    public Void call() throws Exception {
        logger.info(this.toString());

        long startTime = System.currentTimeMillis();

        logger.info("reading intervals file");
        SortedSet<GATKDepthInterval> allIntervalSet = new TreeSet<GATKDepthInterval>();
        try (Stream<String> stream = Files.lines(this.intervals.toPath())) {
            stream.forEach(line -> {
                if (!line.startsWith("Targets")) {
                    allIntervalSet.add(new GATKDepthInterval(line));
                }
            });
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        logger.info("reading samtools depth file");
        Map<Pair<String, Integer>, SAMToolsDepthInterval> samtoolsDepthIntervalMap = new ConcurrentHashMap<>();
        try (Stream<String> stream = Files.lines(this.input.toPath())) {
            stream.parallel().forEach(line -> {

                SAMToolsDepthInterval samtoolsDepthInterval = new SAMToolsDepthInterval(line);
                samtoolsDepthIntervalMap.put(Pair.of(samtoolsDepthInterval.getContig(), samtoolsDepthInterval.getPosition()),
                        samtoolsDepthInterval);

            });
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        logger.info("samtoolsDepthIntervalMap.size(): {}", samtoolsDepthIntervalMap.size());

        logger.info("processing");
        allIntervalSet.forEach(gatkDepthInterval -> {

            for (int i = gatkDepthInterval.getStartPosition(); i < gatkDepthInterval.getEndPosition() + 1; i++) {
                SAMToolsDepthInterval samtoolsDepthInterval = samtoolsDepthIntervalMap.get(Pair.of(gatkDepthInterval.getContig(), i));

                if (samtoolsDepthInterval != null) {
                    gatkDepthInterval.getTotalCoverage().addAndGet(samtoolsDepthInterval.getCoverage());

                    if (samtoolsDepthInterval.getCoverage() >= 1) {
                        gatkDepthInterval.getSampleCountAbove1().incrementAndGet();
                    }

                    if (samtoolsDepthInterval.getCoverage() >= 2) {
                        gatkDepthInterval.getSampleCountAbove2().incrementAndGet();
                    }

                    if (samtoolsDepthInterval.getCoverage() >= 5) {
                        gatkDepthInterval.getSampleCountAbove5().incrementAndGet();
                    }

                    if (samtoolsDepthInterval.getCoverage() >= 8) {
                        gatkDepthInterval.getSampleCountAbove8().incrementAndGet();
                    }

                    if (samtoolsDepthInterval.getCoverage() >= 10) {
                        gatkDepthInterval.getSampleCountAbove10().incrementAndGet();
                    }

                    if (samtoolsDepthInterval.getCoverage() >= 15) {
                        gatkDepthInterval.getSampleCountAbove15().incrementAndGet();
                    }

                    if (samtoolsDepthInterval.getCoverage() >= 20) {
                        gatkDepthInterval.getSampleCountAbove20().incrementAndGet();
                    }

                    if (samtoolsDepthInterval.getCoverage() >= 30) {
                        gatkDepthInterval.getSampleCountAbove30().incrementAndGet();
                    }

                    if (samtoolsDepthInterval.getCoverage() >= 50) {
                        gatkDepthInterval.getSampleCountAbove50().incrementAndGet();
                    }
                }

            }

        });

        logger.info("writing output");
        try (FileWriter fw = new FileWriter(output); BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write("Target\ttotal_coverage\taverage_coverage");

            Arrays.asList(1, 2, 5, 8, 10, 15, 20, 30, 50).forEach(a -> {
                try {
                    bw.write(String.format("\tSample_%%_above_%d", a));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            bw.newLine();
            bw.flush();

            for (GATKDepthInterval gatkDepthInterval : allIntervalSet) {
                fw.write(gatkDepthInterval.toStringTrimmed());
                bw.flush();
            }

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        long endTime = System.currentTimeMillis();
        logger.info("duration {} seconds", (endTime - startTime) / 1000);

        return null;
    }

    public File getInput() {
        return input;
    }

    public void setInput(File input) {
        this.input = input;
    }

    public File getIntervals() {
        return intervals;
    }

    public void setIntervals(File intervals) {
        this.intervals = intervals;
    }

    public File getOutput() {
        return output;
    }

    public void setOutput(File output) {
        this.output = output;
    }

    @Override
    public String toString() {
        return String.format("SAMToolsDepthToGATKDepthOfCoverageFormatConverter [input=%s, intervals=%s, output=%s]", input, intervals,
                output);
    }

    public static void main(String[] args) {

        cliOptions.addOption(Option.builder("i").longOpt("input").desc("absolute path to SAMTools Depth file").required().hasArg().build());
        cliOptions.addOption(Option.builder("o").longOpt("output").desc("absolute path to Output file").required().hasArg().build());
        cliOptions.addOption(Option.builder("l").longOpt("intervals").desc("all intervals file").required().hasArg().build());
        cliOptions.addOption(Option.builder("h").longOpt("help").desc("print this help message").build());
        SAMToolsDepthToGATKDepthOfCoverageFormatConverter app = new SAMToolsDepthToGATKDepthOfCoverageFormatConverter();
        try {
            CommandLineParser commandLineParser = new DefaultParser();
            CommandLine commandLine = commandLineParser.parse(cliOptions, args);
            if (commandLine.hasOption("help")) {
                helpFormatter.printHelp("FilterVCF", cliOptions);
                return;
            }
            if (commandLine.hasOption("input")) {
                File input = new File(commandLine.getOptionValue("input"));
                if (!input.exists()) {
                    throw new ParseException("samtools depth file does not exist");
                }
                app.setInput(input);
            }
            if (commandLine.hasOption("intervals")) {
                File intervals = new File(commandLine.getOptionValue("intervals"));
                if (!intervals.exists()) {
                    throw new ParseException("intervals file does not exist");
                }
                app.setIntervals(intervals);
            }
            if (commandLine.hasOption("output")) {
                File output = new File(commandLine.getOptionValue("output"));
                app.setOutput(output);
            }
            app.call();
            System.exit(0);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            helpFormatter.printHelp(SAMToolsDepthToGATKDepthOfCoverageFormatConverter.class.getSimpleName(), cliOptions);
            System.exit(-1);
        }
    }

}
