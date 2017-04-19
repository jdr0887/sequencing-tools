package org.renci.seqtools.conversion;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SAMToolsDepthToGATKDepthOfCoverageFormatConverter implements Callable<Long> {

    private static final Logger logger = LoggerFactory.getLogger(SAMToolsDepthToGATKDepthOfCoverageFormatConverter.class);

    private static final HelpFormatter helpFormatter = new HelpFormatter();

    private static final Options cliOptions = new Options();

    private File input;

    private File intervals;

    private Integer threads;

    private File output;

    public SAMToolsDepthToGATKDepthOfCoverageFormatConverter() {
        super();
        this.threads = 4;
    }

    public SAMToolsDepthToGATKDepthOfCoverageFormatConverter(File input, File intervals, File output) {
        this(input, intervals, output, 4);
    }

    public SAMToolsDepthToGATKDepthOfCoverageFormatConverter(File input, File intervals, File output, Integer threads) {
        super();
        this.input = input;
        this.intervals = intervals;
        this.threads = threads;
        this.output = output;
    }

    @Override
    public Long call() throws Exception {
        logger.info(this.toString());

        long startTime = System.currentTimeMillis();

        logger.info("reading intervals file");
        List<String> allIntervals = FileUtils.readLines(intervals, "UTF-8");
        if (allIntervals.contains("Targets")) {
            allIntervals.remove("Targets");
        }
        SortedSet<GATKDepthInterval> allIntervalSet = new TreeSet<GATKDepthInterval>();
        allIntervals.forEach(a -> allIntervalSet.add(new GATKDepthInterval(a)));
        allIntervals = null;

        ExecutorService es = Executors.newFixedThreadPool(threads);

        logger.info("reading samtools depth file");
        try (FileReader fr = new FileReader(input); BufferedReader br = new BufferedReader(fr)) {
            String line;
            while ((line = br.readLine()) != null) {

                SAMToolsDepthInterval samtoolsDepthInterval = new SAMToolsDepthInterval(line);

                Optional<GATKDepthInterval> optionalGATKDepthInterval = allIntervalSet.parallelStream()
                        .filter(a -> a.getContig().equals(samtoolsDepthInterval.getContig())
                                && a.getPositionRange().contains(samtoolsDepthInterval.getPosition()))
                        .findFirst();

                if (optionalGATKDepthInterval.isPresent()) {

                    es.submit(() -> {

                        GATKDepthInterval gatkDepthInterval = optionalGATKDepthInterval.get();

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

                    });
                }

            }
            es.shutdown();
            if (!es.awaitTermination(90L, TimeUnit.MINUTES)) {
                es.shutdownNow();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

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
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();

        return endTime - startTime;
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

    public Integer getThreads() {
        return threads;
    }

    public void setThreads(Integer threads) {
        this.threads = threads;
    }

    public File getOutput() {
        return output;
    }

    public void setOutput(File output) {
        this.output = output;
    }

    @Override
    public String toString() {
        return String.format("SAMToolsDepthToGATKDepthOfCoverageFormatConverter [input=%s, intervals=%s, threads=%s, output=%s]", input,
                intervals, threads, output);
    }

    public static void main(String[] args) {

        cliOptions.addOption(Option.builder("i").longOpt("input").desc("absolute path to SAMTools Depth file").required().hasArg().build());
        cliOptions.addOption(Option.builder("o").longOpt("output").desc("absolute path to Output file").required().hasArg().build());
        cliOptions.addOption(Option.builder("l").longOpt("intervals").desc("all intervals file").required().hasArg().build());
        cliOptions.addOption(Option.builder("t").longOpt("threads").desc("threads").hasArg().build());
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
            if (commandLine.hasOption("threads")) {
                Integer threads = Integer.valueOf(commandLine.getOptionValue("threads"));
                app.setThreads(threads);
            }
            if (commandLine.hasOption("output")) {
                File output = new File(commandLine.getOptionValue("output"));
                app.setOutput(output);
            }
            Long duration = app.call();
            logger.info("Duration {} seconds", duration / 1000);
        } catch (Exception e) {
            logger.error(e.getMessage());
            helpFormatter.printHelp("FilterVCF", cliOptions);
            System.exit(-1);
        }
        System.exit(0);
    }

}
