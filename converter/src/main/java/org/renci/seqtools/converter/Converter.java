package org.renci.seqtools.converter;

import java.io.File;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

public class Converter {

    private static final String ERROR_MESSAGE_NO_FILES_FOUND = "Could not find the input files: ";

    private static File tVCFFile;

    private static File tBAMFile;

    private static File tPileupFile;

    private static File tMetricsFile;

    private static File tVariantsOnlyVCFOutputFile;

    private static File tOutputDir;

    private static boolean bIsGoodOutputDir = false;

    public Converter() {
    }

    public static void main(String[] args) {

        HelpFormatter tHelpFormatter = new HelpFormatter();

        Options tOptions = new Options();

        OptionGroup tOGroup = new OptionGroup();

        tOptions.addOption("t", "test", false, "output a text-based consensus file for verifying output");
        tOptions.addOption("v", "vcf", true, "vcf file name and path");
        tOptions.addOption("b", "bam", true, "bam file name and path");

        Option tOutputOption = new Option("o", true, "output directory");
        tOutputOption.setRequired(true);
        tOptions.addOption(tOutputOption);

        Option tGenomeOption = new Option("g", false, "convert genome");

        Option tExomeOption = new Option("x", false, "convert exome");

        tOGroup.addOption(tGenomeOption);
        tOGroup.addOption(tExomeOption);

        Option tMetricsFileOption = new Option("m", "metrics", false, "generate metrics file");
        tOptions.addOption(tMetricsFileOption);

        tOGroup.setRequired(true);
        tOptions.addOptionGroup(tOGroup);

        try {

            CommandLineParser tParser = new PosixParser();
            CommandLine tCommandLine = tParser.parse(tOptions, args);

            if (tCommandLine.hasOption("o")) {
                String sOutputDirectory = tCommandLine.getOptionValue("o");
                if (!Converter.isValidOutputDirectory(sOutputDirectory)) {
                    System.err.println("Converter: Output directory " + sOutputDirectory
                            + " is not a valid directory (either it doesn't exist or isn't writable, etc.)");
                    System.exit(1);
                }
            }

            if (tCommandLine.getOptions().length == 0) {
                tHelpFormatter.printHelp("converter", getHelpHeader(), tOptions, getHelpFooter());
            } else if ((tCommandLine.hasOption("v") && tCommandLine.hasOption("b") && tCommandLine.hasOption("x"))
                    || (tCommandLine.hasOption("v") && tCommandLine.hasOption("b") && tCommandLine.hasOption("g"))) {

                System.out.println("starting program...");

                startProgram(tCommandLine);

            } else {
                tHelpFormatter.printHelp("converter", getHelpHeader(), tOptions, getHelpFooter());
            }

        } catch (ParseException e) {

            e.printStackTrace();
            tHelpFormatter.printHelp("converter", tOptions);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.exit(0);

    }

    private static boolean isValidOutputDirectory(String sOutputDirectory) {

        try {
            Converter.tOutputDir = new File(sOutputDirectory);

            if ((Converter.tOutputDir.exists()) && (Converter.tOutputDir.isDirectory()) && (Converter.tOutputDir.canWrite())) {
                Converter.bIsGoodOutputDir = true;
            } else {
                Converter.bIsGoodOutputDir = Converter.tOutputDir.mkdirs();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return Converter.bIsGoodOutputDir;
    }

    private static void startProgram(CommandLine tCommandLine) throws Exception {

        boolean bIsGenomeOption = tCommandLine.hasOption("g");
        boolean bHasTestOption = tCommandLine.hasOption("t");

        String sVCFFilePath = tCommandLine.getOptionValue("v").trim();
        String sBAMFilePath = tCommandLine.getOptionValue("b").trim();

        ThreadPoolExecutor tThreadPoolExecutor = getExecutor();

        ConversionType tCType = getConversionTypeForOptions(tCommandLine);

        if (Converter.doFilesExist(sVCFFilePath, sBAMFilePath)) {

            Converter.tVCFFile = Converter.getVCFFile(sVCFFilePath);
            Converter.tBAMFile = Converter.getBAMFile(sBAMFilePath);

            if (tCommandLine.hasOption("m")) {

                Converter.tMetricsFile = Converter.getMetricsFile(Converter.tOutputDir.getCanonicalPath(), Converter.tVCFFile.getName());
            }

            Converter.tVariantsOnlyVCFOutputFile = Converter.getVariantsVCFOutputFile(Converter.tOutputDir.getCanonicalPath(),
                    Converter.tVCFFile.getName());

            Converter.runLoop(Converter.tVCFFile, Converter.tBAMFile, Converter.tOutputDir, bIsGenomeOption, bHasTestOption, tCType,
                    tThreadPoolExecutor, Converter.tMetricsFile, Converter.tVariantsOnlyVCFOutputFile);
        } else {
            String msg = ERROR_MESSAGE_NO_FILES_FOUND + " " + sVCFFilePath + " " + sBAMFilePath;
            System.err.println(msg);
            errorAndDie(msg);
        }

    }

    private static File getMetricsFile(String sOutputDir, String sVCFFileNameIn) throws Exception {
        return new File(sOutputDir, sVCFFileNameIn + ".metrics");
    }

    private static File getVariantsVCFOutputFile(String sOutputDir, String sVCFFileNameIn) throws Exception {
        return new File(sOutputDir, sVCFFileNameIn + ".variants-only.gz");
    }

    private static void runLoop(File tVCFFileIn, File tBAMFileIn, File tOutputDirIn, boolean bIsGenomeOption, boolean bHasTestOption,
            ConversionType tCTypeIn, ThreadPoolExecutor tExecutor, File tMetricsFileIn, File tVariantsVCFOutFileIn) {

        IConversionFormat tFormat = null;
        if (tCTypeIn == ConversionType.PILEUP && bIsGenomeOption) {
            tFormat = ConversionFormatFactory.makePileupFormat(GenomeType.WHOLE_GENOME);
        } else if (tCTypeIn == ConversionType.PILEUP && !bIsGenomeOption) {
            tFormat = ConversionFormatFactory.makePileupFormat(GenomeType.EXOMIC_GENOME);
        } else if (tCTypeIn == ConversionType.VCF && bIsGenomeOption) {
            tFormat = ConversionFormatFactory.makeVCFFormat(GenomeType.WHOLE_GENOME, bHasTestOption);
        } else if (tCTypeIn == ConversionType.VCF && !bIsGenomeOption) {
            tFormat = ConversionFormatFactory.makeVCFFormat(GenomeType.EXOMIC_GENOME, bHasTestOption);
        }

        IConversionStrategy tStrategy = null;

        if (bIsGenomeOption) {
            tStrategy = ConversionStrategyFactory.makeStrategy(tFormat, tCTypeIn, GenomeType.WHOLE_GENOME, 1, tVCFFileIn, tBAMFileIn,
                    tOutputDirIn, tMetricsFileIn, tVariantsVCFOutFileIn);
        } else {
            tStrategy = ConversionStrategyFactory.makeStrategy(tFormat, tCTypeIn, GenomeType.EXOMIC_GENOME, 1, tVCFFileIn, tBAMFileIn,
                    tOutputDirIn, tMetricsFileIn, tVariantsVCFOutFileIn);
        }

        try {

            tStrategy.run();

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    private static ThreadPoolExecutor getExecutor() {

        BlockingQueue<Runnable> tQueue = new ArrayBlockingQueue<Runnable>(100, true);

        ThreadPoolExecutor tExecutor = new ThreadPoolExecutor(10, 20, 1, TimeUnit.MINUTES, tQueue);

        return tExecutor;
    }

    private static File getVCFFile(String sPathToVCFFileIn) {
        return new File(sPathToVCFFileIn);
    }

    private static File getBAMFile(String sPathToBAMFileIn) {
        return new File(sPathToBAMFileIn);
    }

    private static boolean doFilesExist(String tVCFFileIn, String tBAMFileIn) {
        boolean bFilesExist = false;

        File tVCFFileInFile = new File(tVCFFileIn);
        File tBAMFileInFile = new File(tBAMFileIn);
        if (tVCFFileInFile.exists() && tBAMFileInFile.exists()) {
            bFilesExist = true;
        }

        return bFilesExist;
    }

    private static ConversionType getConversionTypeForOptions(CommandLine tCommandLine) {
        ConversionType tCType = null;

        if (tCommandLine.hasOption("v")) {
            tCType = ConversionType.VCF;
        } else if (tCommandLine.hasOption("p")) {
            tCType = ConversionType.PILEUP;
        }

        return tCType;
    }

    private static void errorAndDie(String sErrorMessage) {

        System.exit(1);

    }

    private static void printFooter() {
        System.out.println();
        System.out.println("-----------");
    }

    private static void printHeader() {
        System.out.println("Converter");
        System.out.println("-----------");
        System.out.println();

    }

    private static String getHelpHeader() {
        StringBuilder tBuilder = new StringBuilder();
        String sEnder = System.getProperty("line.separator");

        tBuilder.append(sEnder);
        tBuilder.append("converter" + sEnder);
        tBuilder.append("---------");
        tBuilder.append(sEnder);
        tBuilder.append("NAME" + sEnder);
        tBuilder.append(sEnder);
        tBuilder.append("converter - convert vcf and pileup files into consensus files" + sEnder);
        tBuilder.append("SYNOPSIS" + sEnder);
        tBuilder.append(sEnder);
        tBuilder.append("java -jar converter.jar [ options ] [ directory ... ]" + sEnder);
        tBuilder.append("DESCRIPTION" + sEnder);
        tBuilder.append(sEnder);
        tBuilder.append(
                "Given a directory argument, converter transforms pileup and vcf files into master and detail bzip2ed consensus files."
                        + sEnder);
        tBuilder.append(sEnder);
        tBuilder.append("Options specify the directory to look in and the type of conversion to perform." + sEnder);
        tBuilder.append(sEnder);
        return tBuilder.toString();
    }

    private static String getHelpFooter() {
        StringBuilder tBuilder = new StringBuilder();
        String sEnder = System.getProperty("line.separator");
        tBuilder.append(sEnder);
        return tBuilder.toString();
    }

}
