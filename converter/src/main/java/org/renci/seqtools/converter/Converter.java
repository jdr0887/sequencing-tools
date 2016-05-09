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

/**
 * Converter
 * 
 * @author k47k4705
 * 
 *         Converter wraps the main program and instantiates Readers and Writers for reading, converting, and writing
 *         out the cleaned-up pileup data.
 *         <p>
 *         The purpose of the Converter is to move data from the text-based, variable-length format of the pileup file,
 *         and its variants, into a fixed-length binary file.
 *         <p>
 *         From Sarah Grimm's pileup file our input looks like:
 *         <p>
 * 
 *         1 1 T T 7 0 0 2 ^!.^!. @A 1 2 A A 15 0 0 7 ..^!.^!.^!.^!.^!. ?>AA>AA 1 3 A A 16 0 0 9 .......^!.^!. @9@6>?@BA
 *         1 4 C C 16 0 0 11 .........^!.^!. <9=7;=<=4AA 1 5 C C 15 0 0 14 ...........^!.^!.^!. <?>5=?>>>>>AA= 1 6 C C
 *         15 0 0 19 ..............^!,^!.^!.^!.^!. =??>>?>>>>>>>9?@?AB 1 7 T T 16 0 0 21 ..............,....^!,^!.
 *         A=B8AAAA@@A@@>-?@@@$A 1 8 A A 18 0 0 25 ..............,....,.^!,^!,^!.^!. @?@@4?>???7>>=?>=?>:>??AB
 *         <p>
 *         Column One: Chromosome name. Column Two: Position Column Three: Reference base Column Four: Test base Column
 *         Five: Read number Column Six: ? Column Seven: ? Column Eight: Read bases Column Nine: Read qualities
 *         <p>
 *         The goal is to strip out redundant data and push the remainder into two binary files. The first file contains
 *         the essential data and a pointer for each line. The second file contains the pointer from each line of the
 *         first file and the detail data containing read bases and read quality scores.
 *         <p>
 *         We wind up with:
 *         <p>
 *         First essential data binary file:
 *         <p>
 *         T T 7 0 0 2 11222 A A 15 0 0 7 11223 A A 16 0 0 9 11224 C C 16 0 0 11 11225 C C 15 0 0 14 11226 C C 15 0 0 19
 *         11227 T T 16 0 0 21 11228 A A 18 0 0 25 11229
 *         <p>
 *         Second detail data binary file:
 *         <p>
 *         11222 ^!.^!. @A 11223 ..^!.^!.^!.^!.^!. ?>AA>AA 11224 .......^!.^!.
 * @9@6>?@BA 11225 .........^!.^!. <9=7;=<=4AA 11226 ...........^!.^!.^!. <?>5=?>>>>>AA= 11227
 *           ..............^!,^!.^!.^!.^!. =??>>?>>>>>>>9?@?AB 11228 ..............,....^!,^!. A=B8AAAA@@A@@>-?@@@$A
 *           11229 ..............,....,.^!,^!,^!.^!. @?@@4?>???7>>=?>=?>:>??AB
 *           <p>
 *           Hadoop (or some parallel program) will work its magic on the stripped-down data and put the results into a
 *           variant (or some re-named) database.
 *           <p>
 *           Since the data files are in the XX gigabyte range, we will benefit from some threading. But we'll focus on
 *           getting a basic reader off the ground first.
 */

public class Converter {

    private static final String ERROR_MESSAGE_NO_FILES_FOUND = "Could not find the input files: ";

    // Input VCF file to convert.
    private static File tVCFFile;

    // Input BAM file to convert.
    private static File tBAMFile;

    // Input pileup file to convert.
    private static File tPileupFile;

    // Metrics file.
    private static File tMetricsFile;

    // Short-form just-the-variants output VCF file.
    private static File tVariantsOnlyVCFOutputFile;

    // Output dir.
    private static File tOutputDir;

    // Is the user-supplied output directory any good?
    private static boolean bIsGoodOutputDir = false;

    /**
     * Converter
     * <p>
     * Public constructor for a converter instance.
     */
    public Converter() {
    } // end constructor

    /**
     * main()
     * <p>
     * main() launches the Converter app.
     * <p>
     * The invocation is:
     * <p>
     * java -jar Converter.jar my-dir-to-search -p or -v
     * <p>
     * where my-dir-to-search contains *.pileup files or vcf files. And -p or -v specifies the type of file to convert
     * to consensus files.
     * <p>
     * From there the application takes off and grabs pileup files or vcf files via the multi-threaded javaext library,
     * reads them in, converts them to a condensed binary format and writes the results to two files. One file contains
     * essential data and a pointer on every line to data in the second file. The second file contains the line pointer
     * for every line in the first file along with the extra read base and read quality data.
     * <p>
     * 
     * @param args
     *            -- command line options.
     */
    public static void main(String[] args) {
        // Command-line help.
        HelpFormatter tHelpFormatter = new HelpFormatter();

        // Create Options object
        Options tOptions = new Options();
        // OptionGroup for mutuallly exclusive exomic or genomic command-line
        // options.
        OptionGroup tOGroup = new OptionGroup();

        // Add options. ;-)
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
        // Add an option for a verification tool: a metrics file that gathers
        // statistics on VCF data processed.
        Option tMetricsFileOption = new Option("m", "metrics", false, "generate metrics file");
        tOptions.addOption(tMetricsFileOption);

        tOGroup.setRequired(true);
        tOptions.addOptionGroup(tOGroup);

        try {
            // Parse command-line options.
            CommandLineParser tParser = new PosixParser();
            CommandLine tCommandLine = tParser.parse(tOptions, args);

            // Check out the output directory for permissions, writability, etc.
            if (tCommandLine.hasOption("o")) {
                String sOutputDirectory = tCommandLine.getOptionValue("o");
                if (!Converter.isValidOutputDirectory(sOutputDirectory)) {
                    System.err.println("Converter: Output directory " + sOutputDirectory
                            + " is not a valid directory (either it doesn't exist or isn't writable, etc.)");
                    System.exit(1);
                }
            } // end if

            // Check out all the options.
            if (tCommandLine.getOptions().length == 0) {
                tHelpFormatter.printHelp("converter", getHelpHeader(), tOptions, getHelpFooter());
            } else if ((tCommandLine.hasOption("v") && tCommandLine.hasOption("b") && tCommandLine.hasOption("x"))
                    || (tCommandLine.hasOption("v") && tCommandLine.hasOption("b") && tCommandLine.hasOption("g"))) {

                System.out.println("starting program...");
                // The arguments are good.
                // Start the program.
                startProgram(tCommandLine);

            } else {
                tHelpFormatter.printHelp("converter", getHelpHeader(), tOptions, getHelpFooter());
            }

        } catch (ParseException e) {
            // Help!
            e.printStackTrace();
            tHelpFormatter.printHelp("converter", tOptions);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.exit(0);

    } // end main

    /**
     * checkOutputDirectory()
     * <p>
     * Does the output directory exist? Is it writable?
     * 
     * @param sOutputDirectory
     *            -- the prospective output directory.
     */
    private static boolean isValidOutputDirectory(String sOutputDirectory) {

        try {
            Converter.tOutputDir = new File(sOutputDirectory);
            // Test if dir contains bad characters for the underlying OS. Throws
            // IOException.
            // Does this directory already exist? If not, create it.
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
    } // end isValidOutputDirectory

    /**
     * startProgram()
     * <p>
     * Starts the program. ")
     * <p>
     * More specifics: it parses the command-line options. Makes a filter to find the right files to parse, makes a
     * thread pool, an execution context for threading, and launches a conversion strategy.
     * 
     * @param tCommandLine
     */
    private static void startProgram(CommandLine tCommandLine) throws Exception {
        // Get command-line options
        boolean bIsGenomeOption = tCommandLine.hasOption("g");
        boolean bHasTestOption = tCommandLine.hasOption("t");

        // File paths from command line.
        String sVCFFilePath = tCommandLine.getOptionValue("v").trim();
        String sBAMFilePath = tCommandLine.getOptionValue("b").trim();

        // Thread pool for concurrent multiple file conversion.
        // fixed pool fixed queue
        ThreadPoolExecutor tThreadPoolExecutor = getExecutor();

        // Conversion type. Used to get the right conversion strategy.
        ConversionType tCType = getConversionTypeForOptions(tCommandLine);

        // Do these files exist? And have the proper extension?
        if (Converter.doFilesExist(sVCFFilePath, sBAMFilePath)) {

            // Make VCF and BAM files from input paths.
            Converter.tVCFFile = Converter.getVCFFile(sVCFFilePath);
            Converter.tBAMFile = Converter.getBAMFile(sBAMFilePath);

            // Do we need to make a metrics file?
            if (tCommandLine.hasOption("m")) {
                // System.out.println(Converter.tOutputDir.getName() +
                // File.separator + Converter.tVCFFile.getName());
                Converter.tMetricsFile = Converter.getMetricsFile(Converter.tOutputDir.getCanonicalPath(), Converter.tVCFFile.getName());
            } // end if

            // Make a variants-only vcf output file.
            Converter.tVariantsOnlyVCFOutputFile = Converter.getVariantsVCFOutputFile(Converter.tOutputDir.getCanonicalPath(),
                    Converter.tVCFFile.getName());

            // Run the converter.
            Converter.runLoop(Converter.tVCFFile, Converter.tBAMFile, Converter.tOutputDir, bIsGenomeOption, bHasTestOption, tCType,
                    tThreadPoolExecutor, Converter.tMetricsFile, Converter.tVariantsOnlyVCFOutputFile);
        } else {
            String msg = ERROR_MESSAGE_NO_FILES_FOUND + " " + sVCFFilePath + " " + sBAMFilePath;
            System.err.println(msg);
            errorAndDie(msg);
        } // end else

    } // end startProgram

    /**
     * getMetricsFile()
     * <p>
     * Make a metrics file for later verification and run-checking of VCF files processed.
     * 
     * @param tOutputDir2
     * @return File -- the File backing this metrics file.
     */
    private static File getMetricsFile(String sOutputDir, String sVCFFileNameIn) throws Exception {
        return new File(sOutputDir, sVCFFileNameIn + ".metrics");
    } // end getMetricsFile

    /**
     * getVariantsVCFOutputFile()
     * <p>
     * Make a metrics file for later verification and run-checking of VCF files processed.
     * 
     * @param tOutputDir2
     * @return File -- the File backing this metrics file.
     */
    private static File getVariantsVCFOutputFile(String sOutputDir, String sVCFFileNameIn) throws Exception {
        return new File(sOutputDir, sVCFFileNameIn + ".variants-only.gz");
    } // end getMetricsFile

    /**
     * runLoop
     * <p>
     * Runs the converter over a number of files. Make consensus files.
     * 
     * @param sDirectory
     *            -- the directory where pileup or vcf files live.
     * @param tCType
     *            -- the ConversionType, an enum class specifying which strategy needed to convert the input files.
     * @param tStrategy
     *            -- the filter to apply, used to find pileup or vcf files.
     * @param tExecutor
     *            -- the thread executor.
     */
    private static void runLoop(File tVCFFileIn, File tBAMFileIn, File tOutputDirIn, boolean bIsGenomeOption, boolean bHasTestOption,
            ConversionType tCTypeIn, ThreadPoolExecutor tExecutor, File tMetricsFileIn, File tVariantsVCFOutFileIn) {

        // ConversionFormat to make by genome/exome option.
        IConversionFormat tFormat = null;
        if (tCTypeIn == ConversionType.PILEUP && bIsGenomeOption) {
            tFormat = ConversionFormatFactory.makePileupFormat(GenomeType.WHOLE_GENOME);
        } else if (tCTypeIn == ConversionType.PILEUP && !bIsGenomeOption) {
            tFormat = ConversionFormatFactory.makePileupFormat(GenomeType.EXOMIC_GENOME);
        } else if (tCTypeIn == ConversionType.VCF && bIsGenomeOption) {
            tFormat = ConversionFormatFactory.makeVCFFormat(GenomeType.WHOLE_GENOME, bHasTestOption);
        } else if (tCTypeIn == ConversionType.VCF && !bIsGenomeOption) {
            tFormat = ConversionFormatFactory.makeVCFFormat(GenomeType.EXOMIC_GENOME, bHasTestOption);
        } // end else

        IConversionStrategy tStrategy = null;
        // Does the user want to convert an exome or genome?
        if (bIsGenomeOption) {
            tStrategy = ConversionStrategyFactory.makeStrategy(tFormat, tCTypeIn, GenomeType.WHOLE_GENOME, 1, tVCFFileIn, tBAMFileIn,
                    tOutputDirIn, tMetricsFileIn, tVariantsVCFOutFileIn);
        } else {
            tStrategy = ConversionStrategyFactory.makeStrategy(tFormat, tCTypeIn, GenomeType.EXOMIC_GENOME, 1, tVCFFileIn, tBAMFileIn,
                    tOutputDirIn, tMetricsFileIn, tVariantsVCFOutFileIn);
        }

        // Future<?> tFuture = tExecutor.submit(tStrategy);

        try {
            // Convert the file.
            tStrategy.run();
            // tExecutor.execute(tStrategy);

            // tFuture.get();

            // Shutdown the thread pool.
            // tExecutor.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } // end catch

    } // end runLoop

    /**
     * getExecutor()
     * <p>
     * Returns a ThreadPoolExecutor. Sets up the thread queue.
     * 
     * @return ThreadPoolExecutor
     */
    private static ThreadPoolExecutor getExecutor() {

        // Thread pool for concurrent multiple file conversion.
        // fixed pool fixed queue
        BlockingQueue<Runnable> tQueue = new ArrayBlockingQueue<Runnable>(100, true);

        ThreadPoolExecutor tExecutor = new ThreadPoolExecutor(10, // core size
                20, // max size
                1, // keep alive time
                TimeUnit.MINUTES, // keep alive time units
                tQueue // the queue to use
        );

        return tExecutor;
    } // end getExecutor()

    /**
     * getVCFFile()
     * <p>
     * Returns a VCF file based on the input string -- a path to the VCF file.
     * 
     * @param sPathToVCFFile
     *            -- the VCF file as a String system path.
     * @return File tVCFFile -- the VCF file as a File.
     */
    private static File getVCFFile(String sPathToVCFFileIn) {
        return new File(sPathToVCFFileIn);
    } // end getVCFFile()

    /**
     * getBAMFile()
     * <p>
     * Returns a BAM file based on the input string -- a path to the BAM file.
     * 
     * @param sPathToBAMFile
     *            -- the BAM file as a String system path.
     * @return javaxt.io.File tBAMFile -- the BAM file as a javaxt.io.File.
     */
    private static File getBAMFile(String sPathToBAMFileIn) {
        return new File(sPathToBAMFileIn);
    } // end getBAMile()

    /**
     * doFilesExist()
     * <p>
     * Returns a boolean if both BAM and VCF files exist.
     * 
     * @param tVCFFileIn
     *            -- the javaxt.io.File fronting a VCF file.
     * @param tBAMFileIn
     *            -- the javaxt.io.File fronting a BAM file.
     * @return boolean -- true, the files exist, false, the files don't exist.
     */
    private static boolean doFilesExist(String tVCFFileIn, String tBAMFileIn) {
        boolean bFilesExist = false;

        File tVCFFileInFile = new File(tVCFFileIn);
        File tBAMFileInFile = new File(tBAMFileIn);
        if (tVCFFileInFile.exists() && tBAMFileInFile.exists()) {
            bFilesExist = true;
        }

        return bFilesExist;
    } // end doFilesExist()

    /**
     * getConversionTypeForOptions()
     * <p>
     * Returns a ConversionType for the conversion we want to run.
     * 
     * @return
     */
    private static ConversionType getConversionTypeForOptions(CommandLine tCommandLine) {
        ConversionType tCType = null;

        // Get the file type we're searching for.
        if (tCommandLine.hasOption("v")) {
            tCType = ConversionType.VCF;
        } else if (tCommandLine.hasOption("p")) {
            tCType = ConversionType.PILEUP;
        } // end if

        return tCType;
    } // end getConversionTypeForOptions

    /**
     * errorAndDie
     * <p>
     * Errors and dies. ")
     * <p>
     * Prints a message to the screen and dies.
     */
    private static void errorAndDie(String sErrorMessage) {

        // printHeader();
        // System.out.println(sErrorMessage);
        // printFooter();
        System.exit(1);
        // throw new
        // NullPointerException("Converter: end of errorAndDie method and exit point");

    } // end errorAndDie

    private static void printFooter() {
        System.out.println();
        System.out.println("-----------");
    } // end printHeader

    private static void printHeader() {
        System.out.println("Converter");
        System.out.println("-----------");
        System.out.println();

    } // end printHeader

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
    } // end getHelpHeader

    private static String getHelpFooter() { // end getHelpFooter
        StringBuilder tBuilder = new StringBuilder();
        String sEnder = System.getProperty("line.separator");
        tBuilder.append(sEnder);
        return tBuilder.toString();
    } // end getHelpFooter

} // end Converter
