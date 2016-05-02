package org.renci.seqtools.filter.vcf;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilterVCF implements Callable<Void> {

    private static final Logger logger = LoggerFactory.getLogger(FilterVCF.class);

    private static final HelpFormatter helpFormatter = new HelpFormatter();

    private static final Options cliOptions = new Options();

    private File input;

    private File output;

    private File intervalList;

    private Boolean withMissing = Boolean.FALSE;

    public FilterVCF() {
        super();
    }

    public FilterVCF(File input, File output, File intervalList) {
        super();
        this.input = input;
        this.output = output;
        this.intervalList = intervalList;
    }

    @Override
    public Void call() throws FilterVCFException {
        logger.info(this.toString());

        Map<String, List<Range<Integer>>> map = new HashMap<String, List<Range<Integer>>>();
        try (FileReader fr = new FileReader(intervalList); BufferedReader br = new BufferedReader(fr)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (StringUtils.isEmpty(line.trim()) || line.startsWith("#")) {
                    continue;
                }
                String[] lineArray = line.split(":");
                String chromosome = lineArray[0];
                map.put(chromosome, new ArrayList<Range<Integer>>());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (FileReader fr = new FileReader(intervalList); BufferedReader br = new BufferedReader(fr)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (StringUtils.isEmpty(line.trim()) || line.startsWith("#")) {
                    continue;
                }
                String[] lineArray = line.split(":");
                String chromosome = lineArray[0];
                String position = lineArray[1];
                Integer start, end;

                if (position.contains("-")) {
                    String[] positionSplit = position.split("-");
                    start = Integer.valueOf(positionSplit[0]);
                    end = Integer.valueOf(positionSplit[1]);
                } else {
                    start = Integer.valueOf(position);
                    end = start;
                }

                Range<Integer> range = Range.between(start, end);
                map.get(chromosome).add(range);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (FileWriter fw = new FileWriter(output);
                BufferedWriter bw = new BufferedWriter(fw);
                FileReader fr = new FileReader(input);
                BufferedReader br = new BufferedReader(fr)) {
            
            String line;
            line: while ((line = br.readLine()) != null) {

                if (line.startsWith("#")) {
                    bw.write(line);
                    bw.newLine();
                } else {
                    String[] lineSplit = line.split("\t");
                    String chromosome = lineSplit[0];
                    String position = lineSplit[1];

                    List<Range<Integer>> rangeList = map.get(chromosome);
                    if (rangeList != null) {
                        for (Range<Integer> range : rangeList) {
                            if (range.contains(Integer.valueOf(position.trim()))) {
                                bw.write(line);
                                bw.newLine();
                                continue line;
                            }
                        }
                    }

                    if (withMissing && lineSplit.length > 3) {

                        String alternateAllele = lineSplit[4];

                        List<String> formatKeyList = Arrays.asList(lineSplit[8].split(":"));
                        List<String> formatValueList = Arrays.asList(lineSplit[9].split(":"));

                        if (!".".equals(alternateAllele.trim())) {
                            bw.write(line);
                            bw.newLine();
                        } else if (formatValueList.get(formatKeyList.indexOf("GT")).contains(".")) {
                            bw.write(line);
                            bw.newLine();
                        }

                    }

                }

                bw.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public File getIntervalList() {
        return intervalList;
    }

    public void setIntervalList(File intervalList) {
        this.intervalList = intervalList;
    }

    public File getInput() {
        return input;
    }

    public void setInput(File input) {
        this.input = input;
    }

    public File getOutput() {
        return output;
    }

    public void setOutput(File output) {
        this.output = output;
    }

    public Boolean getWithMissing() {
        return withMissing;
    }

    public void setWithMissing(Boolean withMissing) {
        this.withMissing = withMissing;
    }

    @Override
    public String toString() {
        return String.format("FilterVCF [intervalList=%s, input=%s, output=%s, withMissing=%s]", intervalList, input, output, withMissing);
    }

    public static void main(String[] args) {
        cliOptions.addOption(Option.builder("i").longOpt("input").desc("Absolute path to VCF").required().hasArg().build());
        cliOptions.addOption(Option.builder("o").longOpt("output").desc("Absolute path to Output File").required().hasArg().build());
        cliOptions.addOption(Option.builder("l").longOpt("interval-list").desc("Interval List File").required().hasArg().build());
        cliOptions.addOption(Option.builder("m").longOpt("missing").desc("Include Missing Alleles").build());
        cliOptions.addOption(Option.builder("h").longOpt("help").desc("print this help message").build());
        FilterVCF app = new FilterVCF();
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
                    throw new FilterVCFException("input vcf does not exist");
                }
                app.setInput(input);
            }
            if (commandLine.hasOption("interval-list")) {
                File intervalList = new File(commandLine.getOptionValue("interval-list"));
                if (!intervalList.exists()) {
                    throw new FilterVCFException("intervalList does not exist");
                }
                app.setIntervalList(intervalList);
            }
            if (commandLine.hasOption("output")) {
                File output = new File(commandLine.getOptionValue("output"));
                app.setOutput(output);
            }
            if (commandLine.hasOption("missing")) {
                app.setWithMissing(Boolean.TRUE);
            }
            app.call();
        } catch (ParseException | FilterVCFException e) {
            logger.error(e.getMessage());
            helpFormatter.printHelp("FilterVCF", cliOptions);
            System.exit(-1);
        }

    }
}
