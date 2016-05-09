package org.renci.seqtools.converter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrTokenizer;

/**
 * BZip2VCFConversionStrategy
 * <p>
 * Convert VCF files into consensus files.
 * <p>
 * In the case of VCF files, multiple consensus files can be represented in one VCF file. That is, one VCF file contains
 * genomics data for multiple individuals.
 * <p>
 * So, this Strategy object creates multiple consensus files for one or multiple individuals in every VCF file.
 * <p>
 * Data points per Chris's email:
 * <p>
 * For single-sample VCFs, consensus quality is the GQ column, snp quality is the QUAL column.
 * <p>
 * From the email itself: Feb. 14th 2011, "vcfs, pileups, ... and database"
 * <p>
 * These two are equivalent, for single sample VCFs, to the consensus quality (GQ) and snp quality (QUAL) from pileup
 * files.
 * 
 * For multiple sample VCFs, the QUAL is a bit funny. It's no longer describing the chance that a single sample is
 * homozygous reference, but that all the samples are. I'm not aware of a way to get a per-sample version of this
 * number. And in fact, it might not make sense to ask for such a thing given that these are all called together.
 * Probably using QUAL for all the sample snps is the thing to do here.
 * 
 * In BAM files, there are two more qualties: 1) a mapping score (per-read): probability that this read is badly mapped
 * 2) a base quality (per-base-per read). probability that this base in this read is incorrectly called.
 * 
 * 2) can be gotten from SamRecord.getBaseQualities()
 * 
 * The disposition of these elements ( IMO ) is:
 * 
 * BAM.mapping -> ignore. BAM.basequality -> goes into extended consensus file VCF.QUAL -> Goes into database table as
 * snpqual VCF.GQ -> Goes into basic consensus file as consensus quality.
 * 
 * A note on quality scores: These scores are all phred scaled, which means that it is: Q = -10 Log_10(p)
 * 
 * So, low p -> high Q. So if p is, for instance, the probability that a genotype is wrong, then a good genotype would
 * have a low probability, which would translate to a high Q. So good values are high in this scale and bad values are
 * low.
 * 
 * @author k47k4705
 * 
 */
public class BZip2VCFConversionStrategy extends AbstractPileupConversionStrategy implements IConversionStrategy, Runnable {

    // Version string for position map file.
    private String VERSION_STRING = "Converter 0.1";

    // The input VCF file to convert.
    private File tInputVCFFile;

    // Output directory to write to.
    private File tOutputDir;

    // Metrics file.
    private File tMetricsFile;

    // Conversion format for writing VCF data.
    private IConversionFormat tConversionFormat;

    // Name of this Strategy (for threading purposes).
    private String sName;

    // Container of Line Data info.
    private VCFLineDataProcessor tLDProcessor;

    // BAMQueryTool pulled into a larger context.
    private VCFBAMQueryTool tBAMQueryTool;

    // How many consensus file pairs to create (master/detail).
    private int iNumberOfFilePairs;

    // Have we seen the VCF header lines?
    private boolean bSeenVCFHeaders = false;

    // Have we seen the VCF's double pound header (##)?
    private boolean bSeenDoublePoundHeader = false;

    // Have we seen the VCF's single pound header (#)?
    private boolean bSeenSinglePoundHeader = false;

    // Have we written out the master consensus file header?
    private boolean bWrittenMasterHeader = false;

    // Count of header columns in a VCF file.
    private int iVCFHeaderColumnCount;

    // Count of elements in a line of VCF data.
    private int iVCFElementsPerLine;

    // Metrics counts of SNPs, insertions, deletions.
    private int iMetricsGenomeLocationsConvertedCount = 0;

    private int iMetricsSNPCount = 0;

    private int iMetricsInsertionCount = 0;

    private int iMetricsDeletionCount = 0;

    // Error file stuff.
    private File tErrorFile;

    private FileWriter tErrorWriter;

    private String sErrorFileName = "errorFile.txt";

    // Map to positions file.
    private File tPositionMapFile;

    private FileWriter tPositionMapFileWriter;

    private String sPositionMapFileName = "positionmap.txt";

    // Variant-only VCF output file.
    private File tVariantVCFOutFile;

    // Array of column names after the FORMAT column header.
    private String[] sColumnNamesArray;

    // A Map to track names of output files (consensus and index)
    private Map<String, VCFMasterDetailFileStreamManager> tMapOfOutputFiles;

    private String DASH = "-";

    // Local VCFMasterDetailFileStreamManager.
    private VCFMasterDetailFileStreamManager tManager;

    // Metrics file writer.
    private FileWriter tMetricsFileWriter;

    // Manager of metrics data per chromsome and sample column.
    private Map<String, VCFMetricsManager> tMapOfMetricsData;

    // Hold on to the previous position of the last line read.
    // Because of indel trickiness, we need to compare it to the current
    // position line.
    // Then we can determine if we can write the previous line of data and the
    // current line of data.
    private List<String> tPreviousPositionList;

    // A VCFLineHolder for looking back at the previous lines for duplicate
    // positions.
    private VCFLineHolder tLineHolder;

    // A Map to track un-processed lines of data. Since we are using a
    // two-at-a-time and previous line look-back to detect
    // duplicate positions in the VCF file, we run into cases where we have
    // unprocessed data after the while (BufferedReader) loop
    // that reads the VCF file line by line. This Map keeps track of those
    // unprocessed lines. Since it is a sorted map by key, we
    // can loop over the map and print out the values.
    private TreeMap<Long, List<String>> tMapOfLeftoverLines = new TreeMap<Long, List<String>>();

    // FileWriter for VCFVariantOutFile.
    // private FileWriter tVariantVCFFileWriter;
    private BufferedWriter tBufferedWriterVariantsOnly;

    /**
     * BZip2VCFConversionStrategy public constructor
     * 
     * @param tVCFFileIn
     * @param valueOf
     * @param tGenomeTypeIn
     */
    public BZip2VCFConversionStrategy(IConversionFormat tFormatIn, File tVCFFileIn, File tBAMFileIn, File tOutputDirIn, String valueOf,
            GenomeType tGenomeTypeIn, File tMetricsFileIn, File tVariantVCFFileOutIn) {
        // Input vcf file.
        this.tInputVCFFile = tVCFFileIn;
        this.tOutputDir = tOutputDirIn;
        this.tMetricsFile = tMetricsFileIn;
        this.tVariantVCFOutFile = tVariantVCFFileOutIn;
        this.sName = valueOf;
        this.tConversionFormat = tFormatIn;
        this.tBAMQueryTool = VCFBAMQueryTool.getInstance(tBAMFileIn);

        this.tMapOfOutputFiles = new HashMap<String, VCFMasterDetailFileStreamManager>();
        this.tLDProcessor = VCFLineDataProcessor.getInstance();

        this.tMapOfMetricsData = new HashMap<String, VCFMetricsManager>();

        this.tPreviousPositionList = new ArrayList<String>();
        this.tPreviousPositionList.add("mychrom");
        this.tPreviousPositionList.add("0");

        this.tLineHolder = VCFLineHolder.getInstance(this.tPreviousPositionList, new ArrayList<String>(), new ArrayList<String>(), "", "",
                "");

    } // BZip2VCFConversionStrategy

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + " executing " + this);
        try {
            this.convert();
        } catch (Exception e) {
            e.printStackTrace();
        }
    } // end run

    @Override
    public void convert() throws Exception {

        // Make master and detail files based on input VCF file.
        try {

            System.out.println("\tConverter: Writing master file(s) ...");
            System.out.println("\tConverter: Writing detail file(s) ...");

            // Make metrics file.
            if (this.tMetricsFile != null) {
                this.makeMetricsFile(this.tMetricsFile);
            } // end if

            // Make variants-only out file.
            if (this.tVariantVCFOutFile != null) {
                this.makeVariantsVCFOutFile(this.tVariantVCFOutFile);
            } // end if

            // Make an error file for writing errant VCF data lines to.
            this.sErrorFileName = Thread.currentThread().getName() + "errorFile.txt";
            this.makeErrorFile(this.sErrorFileName);
            // Make a position map file.
            this.makePositionMapFile(this.sPositionMapFileName);
            // Write out header.
            this.writePositionMapHeader();

            // Read in the VCF file.
            File tVCFFileToRead = this.tInputVCFFile;

            // Read VCF file into a buffer.
            // Is this a gzipped VCF file?
            BufferedReader tBufferedReader = null;
            if (tVCFFileToRead.getName().toLowerCase().endsWith(".gz")) {
                tBufferedReader = new BufferedReader(
                        new InputStreamReader(new GzipCompressorInputStream(new FileInputStream(tVCFFileToRead))));
            } else {
                tBufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(tVCFFileToRead)));
            }
            // BufferedReader tBufferedReader = new BufferedReader(new
            // InputStreamReader(new FileInputStream(tVCFFileToRead)));
            // BufferedReader tBufferedReader = new BufferedReader(new
            // InputStreamReader(new GzipCompressorInputStream(new
            // FileInputStream(tVCFFileToRead))));
            // Date for error file writing.
            java.util.Date tMyDate = new java.util.Date();
            this.tErrorWriter.write("# Error File for " + this.tInputVCFFile.getAbsolutePath() + " run at: " + tMyDate.toString()
                    + System.getProperty("line.separator"));
            this.tErrorWriter.flush();

            // Loop over the data, line by line.
            this.loopOverData(tBufferedReader);

            // Write out the ranges of positional data for each file into a
            // positional map file.
            this.writePositionMap(this.tMapOfOutputFiles);

            if (this.tMetricsFile != null) {
                // Write out metrics file.
                this.writeMetricsData(this.tMapOfMetricsData, this.tInputVCFFile.getName());
            }

            // Close up shop.
            tBufferedReader.close();
            this.tErrorWriter.close();
            this.tPositionMapFileWriter.close();
            this.tErrorWriter.close();
            // this.tVariantVCFFileWriter.flush();
            // this.tVariantVCFFileWriter.close();

            // Close gzipped variants-only file.
            this.tBufferedWriterVariantsOnly.flush();
            this.tBufferedWriterVariantsOnly.close();

            // print out matched/unmatched in VCF vs BAM records.
            // System.out.println(this.tBAMQueryTool.getMatchUnmatchCount());

            System.out.println("\tConverter: Finished writing master file(s). ");
            System.out.println("\tConverter: Finished writing detail file(s). ");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    } // end convert

    /**
     * makeVariantsVCFOutFile
     * 
     * @param tVariantVCFOutFile2
     */
    private void makeVariantsVCFOutFile(File tVariantVCFOutFile2) {
        try {
            this.tVariantVCFOutFile = tVariantVCFOutFile2;
            if (this.tVariantVCFOutFile.exists()) {
                this.tVariantVCFOutFile.delete();
            }
            this.tVariantVCFOutFile.createNewFile();
            // this.tVariantVCFFileWriter = new
            // FileWriter(this.tVariantVCFOutFile);
            this.tBufferedWriterVariantsOnly = new BufferedWriter(
                    new OutputStreamWriter(new GzipCompressorOutputStream(new FileOutputStream(this.tVariantVCFOutFile))));

        } catch (IOException e) {
            e.printStackTrace();
        }

    } // end makeVariantsVCFOutFile()

    /**
     * If the user passed in a metrics file, make it.
     * 
     * @param tMetricsFile2
     */
    private void makeMetricsFile(File tMetricsFileIn) {
        try {
            this.tMetricsFile = tMetricsFileIn;
            if (this.tMetricsFile.exists()) {
                this.tMetricsFile.delete();
            }
            this.tMetricsFile.createNewFile();
            this.tMetricsFileWriter = new FileWriter(this.tMetricsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

    } // end makeMetricsFile

    /**
     * loopOverData()
     * <p>
     * Loops over a line of input data. Parses the line. Writes out the data per individual in the VCF file.
     * 
     * @param tReaderIn
     *            -- the BufferedReader containing the VCF file to read.
     */
    private void loopOverData(BufferedReader tReaderIn) {

        try {
            // Line from VCF file.
            String tCurrentLine = null;
            // Second line from VCF line.
            String tNextLine = null;
            // Check for duplicate positions.
            List<String> tDupFirstList = new ArrayList<String>();
            List<String> tDupSecondList = new ArrayList<String>();

            // List of tokens to be made from the vcf file line.
            List<String> tVCFLineList = new ArrayList<String>();
            // Apache Commons Lang tokenizer. With empty constructor. Love it.
            StrTokenizer tSTokenizer = new StrTokenizer();
            // Tab-delimited file.
            tSTokenizer.setDelimiterString("\t");

            // Loop over lines in the VCF file. Two at a time. We want to look
            // back so we have some control
            // over writing duplicate positions. VCF files can contain duplicate
            // positions.
            while ((tCurrentLine = tReaderIn.readLine()) != null && (tNextLine = tReaderIn.readLine()) != null) {

                // Handle two lines at a time. As long as neither one is null.
                handleTwoLines(tCurrentLine, tNextLine, this.tInputVCFFile, tSTokenizer, tVCFLineList, tDupFirstList, tDupSecondList);

                // Clear out vcf line, indel line list for next line from pileup
                // file.
                tVCFLineList.clear();
                tDupFirstList.clear();
                tDupSecondList.clear();

            } // end while

            // Clear out vcf line, indel line list for next line from pileup
            // file.
            tVCFLineList.clear();
            tDupFirstList.clear();
            tDupSecondList.clear();

            List<String> tTempList = new ArrayList<String>();
            Long tPosition;
            tVCFLineList.clear();
            this.tMapOfLeftoverLines.clear();

            tSTokenizer.reset(tCurrentLine);

            if (tCurrentLine != null) {
                tTempList = this.parseLine(tCurrentLine, tTempList, tSTokenizer);
                tPosition = new Long(this.getPositionFromList(tTempList));
                this.tMapOfLeftoverLines.put(tPosition, tTempList);
            }

            tVCFLineList.clear();
            tSTokenizer.reset(tNextLine);
            if (tNextLine != null) {
                tVCFLineList = this.parseLine(tNextLine, tVCFLineList, tSTokenizer);
                this.tMapOfLeftoverLines.put(new Long(this.getPositionFromList(tVCFLineList)), tVCFLineList);
            }

            if (this.getPositionFromList(this.tLineHolder.getPreviousLineList()) > this
                    .getPositionFromList(this.tLineHolder.getLastProcessedLineList())) {
                this.tMapOfLeftoverLines.put(new Long(this.getPositionFromList(this.tLineHolder.getPreviousLineList())),
                        this.tLineHolder.getPreviousLineList());
            }
            if (this.getPositionFromList(this.tLineHolder.getCurrentLineList()) > this
                    .getPositionFromList(this.tLineHolder.getLastProcessedLineList())) {
                this.tMapOfLeftoverLines.put(new Long(this.getPositionFromList(this.tLineHolder.getCurrentLineList())),
                        this.tLineHolder.getCurrentLineList());
            }
            if (this.getPositionFromList(this.tLineHolder.getNextLineList()) > this
                    .getPositionFromList(this.tLineHolder.getLastProcessedLineList())) {
                this.tMapOfLeftoverLines.put(new Long(this.getPositionFromList(this.tLineHolder.getNextLineList())),
                        this.tLineHolder.getNextLineList());
            }

            // Loop over Map loaded with "leftover" lines missed by our loop.
            for (Map.Entry<Long, List<String>> tLoopEntry : this.tMapOfLeftoverLines.entrySet()) {
                this.processLineOfVCFData(tLoopEntry.getValue(), false);
            } // end for.

            // Close out streams.
            for (Map.Entry<String, VCFMasterDetailFileStreamManager> tEntry : this.tMapOfOutputFiles.entrySet()) {
                VCFMasterDetailFileStreamManager tManager = tEntry.getValue();
                tManager.closeStreams();
            } // end for

        } catch (IOException e) {
            e.printStackTrace();

            System.exit(1);
        } catch (Exception e) {

            e.printStackTrace();
        }
    } // end loopOverLine

    /**
     * handleEachLine()
     * <p>
     * A convenience method for each line of data while data is being looped over.
     * <p>
     * Writes data to the streams.
     * 
     * @param tLineOfDataIn
     *            -- a String with the line of data from the VCF file.
     * @param tInputVCFFile
     *            -- the VCF file as a File.
     * @param tSTokenizer
     *            -- the StrTokenizer for taking apart the line.
     * @param tVCFListToFill
     *            -- an empty array list we'll fill with parsed VCF data.
     */
    // private void handleEachLine(String tLineOfDataIn, File tInputVCFFile,
    // StrTokenizer tSTokenizer, List<String> tVCFListToFill, int
    // iLineCounterIn) {
    // System.out.println("handleEachLine: " + tLineOfDataIn);
    // // List of VCF line.
    // List<String> tParsedVCFLineList = tVCFListToFill;
    // // Number of file pairs.
    // this.iNumberOfFilePairs = 0;
    //
    // // Reset the tokenizer for next pass.
    // tSTokenizer.reset(tLineOfDataIn);
    //
    //
    // // Have we seen both types of headers yet? Double pound (##) and single
    // pound (#)?
    // if ( ! this.bSeenVCFHeaders) {
    // // Get data from the VCF headers. If we find no headers, (we include the
    // #COL line as a header, a crucial, life-altering, world-ending header),
    // // we bail in a most heinous manner and stand down the entire army.
    // try {
    // this.processHeaders(tLineOfDataIn, tSTokenizer, tParsedVCFLineList);
    // } catch (Exception e) {
    // e.printStackTrace();
    // System.exit(1);
    // }
    // // Parse a line of data. Now we're in the heart of the VCF file.
    // } else {
    //
    // if ( (tLineOfDataIn == null) || (tLineOfDataIn.isEmpty())) {
    // // Ignore null lines? Probably want to.
    // } else {
    // try {
    // // Finally. Process the line of VCF data.
    //
    // tSTokenizer.reset(tLineOfDataIn);
    // tParsedVCFLineList = parseLine(tLineOfDataIn, tParsedVCFLineList,
    // tSTokenizer);
    //
    // this.processLineOfVCFData(tParsedVCFLineList);
    // } catch (Exception e) {
    //
    // this.writeError("Could not process this line: ", tLineOfDataIn + " " +
    // e.getMessage());
    // }
    // }
    // } // end else
    // } // end handleEachLine

    /**
     * handleTwoLines()
     * <p>
     * A convenience method for each line of data while data is being looped over.
     * <p>
     * Writes data to the streams.
     * <p>
     * Handles two lines because of VCF files can contain duplicate positions.
     * <p>
     * Duplicate positions indicate indel lines and we need a way to look-back before we know we can write data.
     * <p>
     * The line we want to write may be an indel and a duplicate position to the previous line. We need to delay writing
     * until we know for sure we're good to write.
     * 
     * @param tLineOfDataIn
     *            -- a String with the line of data from the VCF file.
     * @param tInputVCFFile
     *            -- the VCF file as a File.
     * @param tSTokenizer
     *            -- the StrTokenizer for taking apart the line.
     * @param tHeaderVCFListToFill
     *            -- an empty array list we'll fill with parsed VCF data.
     */
    private void handleTwoLines(String tCurrentLineOfDataIn, String tNextLineOfDataIn, File tInputVCFFile, StrTokenizer tSTokenizer,
            List<String> tHeaderVCFListToFill, List<String> tCurrentLineListIn, List<String> tNextLineListIn) {

        // Are previous/current/next line VCF positions equal?
        boolean bArePositionsEqual = false;

        // List of VCF line.
        List<String> tParsedHeaderVCFLineList = tHeaderVCFListToFill;
        // Number of file pairs.
        this.iNumberOfFilePairs = 0;

        // Have we seen both types of headers yet? Double pound (##) and single
        // pound (#)?
        if (!this.bSeenVCFHeaders) {
            // Get data from the VCF headers. If we find no headers, (we include
            // the #COL line as a header, a crucial, life-altering, world-ending
            // header),
            // we bail in a most heinous manner and stand down the entire army.
            try {
                tSTokenizer.reset(tCurrentLineOfDataIn);
                this.processHeaders(tCurrentLineOfDataIn, tSTokenizer, tParsedHeaderVCFLineList);
                tSTokenizer.reset(tNextLineOfDataIn);
                this.processHeaders(tNextLineOfDataIn, tSTokenizer, tParsedHeaderVCFLineList);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
            // Parse a line of data. Now we're in the heart of the VCF file.
        } else {

            if (((tCurrentLineOfDataIn == null) || (tCurrentLineOfDataIn.isEmpty()))
                    || ((tNextLineOfDataIn == null) || (tNextLineOfDataIn.isEmpty()))) {
                // Ignore null lines? Probably want to.
            } else {
                // Parse current and next VCF lines.
                try {

                    tSTokenizer.reset(tCurrentLineOfDataIn);
                    tCurrentLineListIn = parseLine(tCurrentLineOfDataIn, tCurrentLineListIn, tSTokenizer);
                    tSTokenizer.reset(tNextLineOfDataIn);
                    tNextLineListIn = parseLine(tNextLineOfDataIn, tNextLineListIn, tSTokenizer);

                    this.tLineHolder.setCurrentLineList(tCurrentLineListIn);
                    this.tLineHolder.setCurrentLine(tCurrentLineOfDataIn);
                    this.tLineHolder.setNextLineList(tNextLineListIn);
                    this.tLineHolder.setNextLine(tNextLineOfDataIn);

                    this.tPreviousPositionList = tCurrentLineListIn;
                    if (this.tLineHolder.areCurrentAndNextPositionsEqual()) {
                        bArePositionsEqual = true;
                        this.processLineOfVCFData(this.tLineHolder.getNextLineList(), bArePositionsEqual);
                        this.tLineHolder.setLastProcessedLine(this.tLineHolder.getNextLine());
                        this.tLineHolder.setLastProcessedLineList(this.tLineHolder.getNextLineList());
                    } else if (this.tLineHolder.arePreviousAndCurrentLinePositionsEqual()) {
                        bArePositionsEqual = true;
                        this.processLineOfVCFData(this.tLineHolder.getCurrentLineList(), bArePositionsEqual);
                        this.tLineHolder.setLastProcessedLine(this.tLineHolder.getCurrentLine());
                        this.tLineHolder.setLastProcessedLineList(this.tLineHolder.getCurrentLineList());

                    } else if (this.tLineHolder.arePreviousAndNextLinePositionsEqual()) {
                        bArePositionsEqual = true;
                        this.processLineOfVCFData(this.tLineHolder.getNextLineList(), bArePositionsEqual);
                        this.tLineHolder.setLastProcessedLine(this.tLineHolder.getNextLine());
                        this.tLineHolder.setLastProcessedLineList(this.tLineHolder.getNextLineList());
                    } else {
                        bArePositionsEqual = false;
                        this.processLineOfVCFData(this.tLineHolder.getPreviousLineList(), bArePositionsEqual);
                        this.processLineOfVCFData(this.tLineHolder.getCurrentLineList(), bArePositionsEqual);

                        this.tLineHolder.setLastProcessedLine(this.tLineHolder.getCurrentLine());
                        this.tLineHolder.setLastProcessedLineList(this.tLineHolder.getCurrentLineList());

                    }

                    this.tLineHolder.setPreviousLineList(tNextLineListIn);
                    this.tLineHolder.setPreviousLine(tNextLineListIn.toString());

                } catch (Exception e) {
                    e.printStackTrace();
                    this.writeError("Could not process this line: ", tCurrentLineOfDataIn + " " + e.getMessage());
                }
            }
        } // end else

    } // end handleTwoLines

    /**
     * processHeaders()
     * <p>
     * Handle the VCF headers. Extract the information they contain. Rule the world.
     * 
     * @param tLineOfDataIn
     *            -- the VCF line of data as a String.
     * @param tSTokenizer
     *            -- a String Tokenizer to pull apart the VCF line of data.
     * @param tVCFListToFill
     *            -- an temporary, empty list used for making a VCFRecord.
     */
    private void processHeaders(String tLineOfDataIn, StrTokenizer tSTokenizer, List<String> tVCFListToFill) throws Exception {

        // Parse line of text into a List<String>.
        // Is this a "pound", a comment or header element?
        if (tLineOfDataIn.startsWith("##")) {
            // Ignore this line. It's a comment.
            this.bSeenDoublePoundHeader = true;
            // Write line to Variant-only VCF out file.
            // this.tVariantVCFFileWriter.write(tLineOfDataIn);
            // this.tVariantVCFFileWriter.write(System.getProperty("line.separator"));

            // Write to GZipped variant-only file.
            this.tBufferedWriterVariantsOnly.write(tLineOfDataIn);
            this.tBufferedWriterVariantsOnly.write(System.getProperty("line.separator"));

        } else if (tLineOfDataIn.startsWith("#")) {
            // Write line to Variant-only VCF out file.
            // this.tVariantVCFFileWriter.write(tLineOfDataIn);
            // this.tVariantVCFFileWriter.write(System.getProperty("line.separator"));

            // Write to GZipped variant-only file.
            this.tBufferedWriterVariantsOnly.write(tLineOfDataIn);
            this.tBufferedWriterVariantsOnly.write(System.getProperty("line.separator"));

            // Get a list of header column elements on this line.
            List<String> tHeaderList = parseLine(tLineOfDataIn, tVCFListToFill, tSTokenizer);

            // Get column count. We'll use this for sanity checks against
            // corrupt lines in the VCF file.
            this.iVCFHeaderColumnCount = getVCFHeaderColumnCount(tHeaderList);

            // Get the names of the samples (the sample columns after the FORMAT
            // column in the header).
            this.sColumnNamesArray = this.getSampleColumnNames(tLineOfDataIn, this.tInputVCFFile);

            this.iNumberOfFilePairs = this.sColumnNamesArray.length;
            if (this.sColumnNamesArray.length == 0) {
                this.writeError("Found no individual or sample data column names in the VCF file", tLineOfDataIn);
                System.out.println("Found no individual data in the VCF file.");
                this.writeError(
                        "BZip2VCFConversionStrategy: processHeaders: this.sColumnNamesArray.length == 0; no individual or sample data in the VCF file",
                        tLineOfDataIn);
                System.exit(1);
            } // end if

            // Just saw a line that started with a single "#", a header line.
            this.bSeenSinglePoundHeader = true;
        }
        // Have we seen both headers?
        if (this.bSeenDoublePoundHeader && this.bSeenSinglePoundHeader) {
            this.bSeenVCFHeaders = true;
        }

    } // end processHeaders

    /**
     * processLineOfVCFData()
     * <p>
     * Wraps up the processing block for an actual line of VCF data, not a header.
     * 
     * @param tParsedVCFLineList
     *            -- an empty List to be filled by the tokens from the parseLine() method, an object reused for every
     *            VCF line.
     * @param tLineOfDataIn
     *            -- the line of VCF data as a String.
     */

    private void processLineOfVCFData(List<String> tParsedVCFLineList, boolean bArePositionsEqualIn) throws Exception {
        // Do we have a duplicate SNP positions? That's a fatal error.
        boolean bSNPError = false;
        try {

            // Null line, empty line?
            if ((tParsedVCFLineList != null) && (!tParsedVCFLineList.isEmpty()) && (!tParsedVCFLineList.get(1).equalsIgnoreCase("0"))) {

                // How many individual columns of data does the VCF file
                // contain? This number only represents the NA***0 and NA****1
                // columns.
                iNumberOfFilePairs = this.sColumnNamesArray.length;

                // Parse the line into tokens.
                String sChromosomeName = tParsedVCFLineList.get(0);
                String sStartPosition = tParsedVCFLineList.get(1);

                // Get the number of elements in this line of data.
                this.iVCFElementsPerLine = this.getElementsPerLineCount(tParsedVCFLineList);

                // If the number of header columns match the number of elements
                // in this line, we've got a good line of data.
                if (this.iVCFElementsPerLine == this.iVCFHeaderColumnCount) {

                    // Set the BAMQueryTool, a wrapper allowing query of
                    // chromosome id and start position.
                    // We pick off the data associated with the start position.
                    // Maybe not performant?

                    this.tBAMQueryTool.setInputs(sChromosomeName, sStartPosition);

                    this.tLDProcessor = this.getLineDataProcessor();
                    this.tLDProcessor.setInputs(tParsedVCFLineList, iNumberOfFilePairs, this.tBAMQueryTool,
                            tParsedVCFLineList.get(VCFLineDataProcessor.INT_POSITION_COLUMN_POSITION),
                            tParsedVCFLineList.get(VCFLineDataProcessor.INT_POSITION_COLUMN_POSITION));

                    // Load VCF line of data into a Map.
                    this.tLDProcessor.loadParsedVCFDataIntoMap();

                    // Loop over the files, create Files, DataOutputStreams.
                    // Write data to them.
                    Iterator<VCFRecord> tRecordIter;

                    // Loop over individual sample data columns.
                    for (int ii = 0; ii < sColumnNamesArray.length; ii++) {

                        // Get a List of VCFRecords for an individual sample
                        // column.
                        List<VCFRecord> tRecordList = this.tLDProcessor.getVCFRecordListForColumn(ii);

                        tRecordIter = tRecordList.iterator();
                        // Get or create and then get a StreamManager for
                        // writing consensus data to.
                        this.tManager = this.checkOrCreateFiles(sChromosomeName, this.sColumnNamesArray[ii], this.tInputVCFFile);

                        // Loop over records.
                        while (tRecordIter.hasNext()) {

                            VCFRecord tRecord = (VCFRecord) tRecordIter.next();

                            // Write out record to master/detail files.
                            if (tRecord.isIndel()) {
                                this.tManager.writeIndelData(tRecord);
                                // Write variant to variants-only VCF out file.
                                // this.tVariantVCFFileWriter.write(StringUtils.join(tParsedVCFLineList,
                                // "\t"));
                                // this.tVariantVCFFileWriter.write(System.getProperty("line.separator"));

                                // Write to gzipped variants-only file.
                                this.tBufferedWriterVariantsOnly.write(StringUtils.join(tParsedVCFLineList, "\t"));
                                this.tBufferedWriterVariantsOnly.write(System.getProperty("line.separator"));

                                // Line containing SNP.
                            } else if (tRecord.isSNP() && bArePositionsEqualIn) {
                                bSNPError = true;
                                throw new Exception("SNP duplicate positions detected: " + StringUtils.join(tParsedVCFLineList, "\t"));
                                // Line with a no-call.
                            } else if (tRecord.isNoCall()) {
                                // this.tManager.writeGenomicData(tRecord);
                                this.tManager.writeIndelData(tRecord);
                                // Write variant to variants-only VCF out file.
                                // this.tVariantVCFFileWriter.write(StringUtils.join(tParsedVCFLineList,
                                // "\t"));
                                // this.tVariantVCFFileWriter.write(System.getProperty("line.separator"));

                                // Write to gzipped variants-only file.
                                this.tBufferedWriterVariantsOnly.write(StringUtils.join(tParsedVCFLineList, "\t"));
                                this.tBufferedWriterVariantsOnly.write(System.getProperty("line.separator"));
                            }
                            // Line with an SNP and not the same position.
                            else if (tRecord.isSNP() && !bArePositionsEqualIn) {

                                this.tManager.writeSNPData(tRecord);
                                // Write variant to variants-only VCF out file.
                                // this.tVariantVCFFileWriter.write(StringUtils.join(tParsedVCFLineList,
                                // "\t"));
                                // this.tVariantVCFFileWriter.write(System.getProperty("line.separator"));

                                // Write to gzipped variants-only file.
                                this.tBufferedWriterVariantsOnly.write(StringUtils.join(tParsedVCFLineList, "\t"));
                                this.tBufferedWriterVariantsOnly.write(System.getProperty("line.separator"));

                                // Other. Write to consensus files.
                            } else if (tRecord.hasNoReferenceData()) {
                                this.tManager.writeNoReferenceData(tRecord);
                            } else {
                                this.tManager.writeGenomicData(tRecord);
                            }

                            // Collect statistics on this conversion run. //
                            // name of chromosome marker and sample, number of
                            // snps, indels, and total positions
                            this.collectMetrics(sChromosomeName + DASH + sColumnNamesArray[ii], tRecord.isSNP(), tRecord.isIndel(), true,
                                    Long.parseLong(tRecord.getReaddepth()));

                        } // end while
                    } // end for

                    // Collect statistics for the metrics file.
                    this.iMetricsGenomeLocationsConvertedCount++;

                } else {
                    // Skip this line. Header count does not match element
                    // count.
                    this.writeError("Wrong number of data columns: header columns: " + this.iVCFHeaderColumnCount + ", data columns: "
                            + this.iVCFElementsPerLine, tParsedVCFLineList);
                }
            } else {
                this.writeError("VCF line was null or empty", tParsedVCFLineList);
            }
        } catch (Exception e) {

            // An exception from somewhere above.
            this.writeError(e.getMessage(), tParsedVCFLineList);
            e.printStackTrace();
            if (bSNPError) {
                System.out.println("Converter exiting from caught SNP duplicate position exception");
                System.exit(1);
            }
        }
    } // end processLineOfVCFData

    /**
     * getElementsPerLineCount()
     * <p>
     * Return a count of elements in a line of VCF data.
     * <p>
     * Used for comparing
     * 
     * @param tOutputList
     *            -- a String and line of VCF data.
     * @return int -- the count of elements in a line of VCF data.
     */
    private int getElementsPerLineCount(List<String> tOutputList) {
        int iElementsPerLine = tOutputList.size();
        return iElementsPerLine;
    } // end getElementsPerLineCount

    /**
     * getVCFColumnCount()
     * <p>
     * Return a count of the number of columns in the VCF header.
     * <p>
     * The column count can be useful in identifying corrupt VCF lines in a file.
     * 
     * @param tHeaderList
     *            -- String a line of VCF data.
     * @return int -- the count of VCF header columns.
     */
    private int getVCFHeaderColumnCount(List<String> tHeaderList) {
        int iHeaderColumns = tHeaderList.size();
        return iHeaderColumns;
    } // end getVCFColumnCount()

    /**
     * getLineDataPerIndividual()
     * <p>
     * Returns a line of data for each input individual.
     * 
     * @param tOutputList
     * @param iNumberOfFilePairs
     * @param sPathToBAMFile2
     * @return
     * @throws Exception
     */
    private VCFLineDataProcessor getLineDataPerIndividual(List<String> tOutputList, int iNumberOfFilePairs, VCFBAMQueryTool tBAMQueryTool,
            String sStartPosition, String sEndPosition) throws Exception {
        return VCFLineDataProcessor.getInstance(tOutputList, iNumberOfFilePairs, tBAMQueryTool, sStartPosition, sEndPosition);
    } // end getLineDataPerIndividual

    private VCFLineDataProcessor getLineDataProcessor() {
        return this.tLDProcessor;
    } // end

    /**
     * checkOrCreateFiles()
     * <p>
     * Take in the initial VCF comment line -- the line that starts with one pound sign.
     * <p>
     * Parse the line and figure out how many elements come after the FORMAT delimiter.
     * 
     * @param tLine
     *            -- the header line of a VCF file.
     * @return VCFMasterDetailFileStreamManager -- a stream manager containing files and streams for this chromosome and
     *         sample column.
     */
    private VCFMasterDetailFileStreamManager checkOrCreateFiles(String sChromosome, String sColumnName, File tVCFFileIn) {

        // VCFMasterDetailFileStreamManager to return.
        VCFMasterDetailFileStreamManager tManager = null;
        // Column names -- the samples or sample in the VCF file.
        String sProspectName = sChromosome + DASH + sColumnName;
        // Check for invalid filename characters in the chromosome string,
        // column name: "/ | : . " etc.
        sProspectName = BZip2VCFConversionStrategy.sanitizeFilename(sProspectName);
        // Make an output filename.
        if (!this.tMapOfOutputFiles.containsKey(sProspectName)) {
            // System.out.println("Strategy makeLocalSetOfFileNamesToTrack: adding: "
            // + sProspectName);
            tManager = VCFMasterDetailFileStreamManager.getInstance(sProspectName, 0, tVCFFileIn, this.tOutputDir, this.tConversionFormat);
            tManager.createFilesAndStreams2(sProspectName);
            this.tMapOfOutputFiles.put(sProspectName, tManager);
        } else {
            tManager = this.tMapOfOutputFiles.get(sProspectName);
        }
        return tManager;

    } // end checkOrCreateFiles()

    /**
     * collectMetrics()
     * <p>
     * Collect statistics (metrics) on this file conversion. SNPs, indels, total positions converted.
     * 
     * @param name
     * 
     */
    private void collectMetrics(String sMapKeyNameIn, boolean bHasNewSNP, boolean bHasNewIndel, boolean bHasNewTotal,
            long lReadDepthCountIn) {
        // Make an output filename.
        VCFMetricsManager tMetricsManager = null;
        if (!this.tMapOfMetricsData.containsKey(sMapKeyNameIn)) {
            // Make a new metrics manager.
            tMetricsManager = VCFMetricsManager.getInstance(sMapKeyNameIn);
            if (bHasNewSNP) {
                tMetricsManager.incrementSNPCount();
            }
            if (bHasNewIndel) {
                tMetricsManager.incrementIndelCount();
            }
            if (bHasNewTotal) {
                tMetricsManager.incrementTotalCount();
            }
            // Bump the metrics read depth counter.
            tMetricsManager.incrementReadDepth(lReadDepthCountIn);
            // Put key and metrics manager for this key (chromsome marker) in
            // the map.
            this.tMapOfMetricsData.put(sMapKeyNameIn, tMetricsManager);
        } else {
            tMetricsManager = this.tMapOfMetricsData.get(sMapKeyNameIn);
            if (bHasNewSNP) {
                tMetricsManager.incrementSNPCount();
            }
            if (bHasNewIndel) {
                tMetricsManager.incrementIndelCount();
            }
            if (bHasNewTotal) {
                tMetricsManager.incrementTotalCount();
            }

            // Bump the metrics read depth counter.
            tMetricsManager.incrementReadDepth(lReadDepthCountIn);
            // Put key and metrics manager for this key (chromsome marker) in
            // the map.
            this.tMapOfMetricsData.put(sMapKeyNameIn, tMetricsManager);

        } // end else

    } // end collectMetrics

    public static String sanitizeFilename(String name) {
        return name.replaceAll("[:\\\\/*?|<>]", "_");
    } // end sanitizeFilename

    /**
     * getSampleColumnNames()
     * <p>
     * Return an array of Strings containing the sample format column names.
     * 
     * @param tLine
     * @param tVCFFileIn
     * @return String[] array containing the sample column names.
     */
    private String[] getSampleColumnNames(String tLine, File tVCFFileIn) {
        // Split the comment string on the "FORMAT" word.
        // Everything after that should be the number of files to write. ???
        // Right?
        String[] sArray = tLine.split("FORMAT");
        String[] sFiles = sArray[1].trim().split("\t");

        return sFiles;
    } // end getSampleColumnNames()

    /**
     * parseLine()
     * <p>
     * parseLine() parses an input line from a VCF file.
     * 
     * @param sLineOfDataIn
     *            -- the input line from a VCF file as a String.
     * @return tList -- a List<String> containing data read from the input VCF file.
     */
    private List<String> parseLine(String sLineOfDataIn, List<String> tLineOfVCFDataIn, StrTokenizer tSTokenizerIn) throws Exception {

        // List of Strings to return.
        List<String> tList = tLineOfVCFDataIn;
        tList.clear();
        if (sLineOfDataIn == null) {
            System.out.println("BZip2VCFConversionStrategy: parseLine, sLineOfData is null");
            // System.exit(1);
            throw new Exception("BZip2VCFConversionStrategy: parseLine: sLineOfData is null");

        } else {
            // Read line via Scanner.
            StrTokenizer lineScanner = tSTokenizerIn;

            // Loop over line.
            while (lineScanner.hasNext()) {
                tList.add(lineScanner.nextToken());
            } // end while
        } // end else
          // System.out.println("parseLine: size: " + tList.size());
        return tList;
    } // end parseLine()

    /**
     * makeErrorFile()
     * <p>
     * Make an error file to write errant VCF lines to.
     */
    private void makeErrorFile(String sErrorFile) {

        try {
            String sErrorPathAndFile = this.tOutputDir.getCanonicalPath() + File.separatorChar + sErrorFile;
            // System.out.println("Making error file");
            this.tErrorFile = new File(sErrorPathAndFile);

            if (this.tErrorFile.exists()) {
                this.tErrorFile.delete();
            }
            this.tErrorFile.createNewFile();
            this.tErrorWriter = new FileWriter(tErrorFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    } // end makeErrorFile

    /**
     * writeError()
     * <p>
     * Write an error to the error file.
     * 
     * @param tListIn
     *            -- the List of Strings of a line of VCF data.
     * @param sMessageIn
     *            -- the error message to write.
     */
    private void writeError(String sMessageIn, List<String> tListIn) {
        try {
            this.tErrorWriter.write(sMessageIn + " " + tListIn.toString() + System.getProperty("line.separator"));
            this.tErrorWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    } // end writeError

    /**
     * writeError()
     * <p>
     * Write an error to the error file.
     * 
     * @param tListIn
     *            -- the List of Strings of a line of VCF data.
     * @param sMessageIn
     *            -- the error message to write.
     */
    private void writeError(String sMessageIn, String sErrorLineIn) {
        try {
            this.tErrorWriter.write(sMessageIn + " " + sErrorLineIn + System.getProperty("line.separator"));
            this.tErrorWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    } // end writeError

    /**
     * makePositionMapFile()
     * <p>
     * Make a position map file to keep track of which files have which positional data ranges.
     */
    private void makePositionMapFile(String sPositionMapFile) {

        try {

            String sPositionMapFileName = this.tOutputDir.getCanonicalPath() + File.separatorChar + this.tInputVCFFile.getName() + this.DASH
                    + sPositionMapFile;
            this.tPositionMapFile = new File(sPositionMapFileName);

            if (this.tPositionMapFile.exists()) {
                this.tPositionMapFile.delete();
            }
            this.tPositionMapFile.createNewFile();
            this.tPositionMapFileWriter = new FileWriter(this.tPositionMapFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    } // end makeErrorFile

    /**
     * writePositionMap()
     * <p>
     * Write out data for the position map file.
     * 
     * @param tListIn
     *            -- the List of Strings of a line of VCF data.
     * @param sMessageIn
     *            -- the error message to write.
     */
    private void writePositionMap(Map<String, VCFMasterDetailFileStreamManager> tMapIn) {
        try {
            // Loop over the map.
            Iterator tIter = tMapIn.entrySet().iterator();
            VCFMasterDetailFileStreamManager tManager = null;
            String sFirstPosition = null, sLastPosition = null;
            File tFileName = null;
            while (tIter.hasNext()) {
                Map.Entry<String, VCFMasterDetailFileStreamManager> tEntry = (Map.Entry) tIter.next();
                tManager = tEntry.getValue();
                if (tManager != null) {
                    sFirstPosition = tManager.getFirstPositionInFile();
                    sLastPosition = tManager.getLastPositionInFile();
                    tFileName = tManager.getMasterFile();
                    this.tPositionMapFileWriter.write(tFileName.getName() + ":" + tEntry.getKey() + ":" + sFirstPosition + ":"
                            + sLastPosition + System.getProperty("line.separator"));
                } // end if
            } // end while

            this.tPositionMapFileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    } // end writePositionMap

    /**
     * writePositionMapHeader()
     * <p>
     * Write out header data for the position map file.
     * 
     * @param tListIn
     *            -- the List of Strings of a line of VCF data.
     * @param sMessageIn
     *            -- the error message to write.
     */
    private void writePositionMapHeader() {
        try {
            this.tPositionMapFileWriter.write(this.VERSION_STRING + System.getProperty("line.separator"));
            this.tPositionMapFileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    } // end writePositionMap

    /**
     * writeMetricsData()
     * <p>
     * Write out data for the metrics data.
     * 
     * @param tMapIn
     *            -- the map of metrics data containing all the objects chromosome markers and sample columns along with
     *            snp, indel, and chromosome marker totals.
     * @param sVCFFileNameIn
     *            -- the name of the VCF file to convert.
     */
    private void writeMetricsData(Map<String, VCFMetricsManager> tMapIn, String sVCFFileNameIn) {
        try {
            // Loop over the map.
            long iTotalFilePosition = 0;
            long iTotalSNPFile = 0;
            long iTotalIndelFile = 0;
            long lTotalReadDepth = 0;
            long lAvgTotalReadDepth = 0;
            long lCounter = 0;
            Iterator tIter = tMapIn.entrySet().iterator();
            VCFMetricsManager tManager = null;
            // Write metrics per chromsome marker/sample column.
            while (tIter.hasNext()) {
                lCounter++;
                Map.Entry<String, VCFMetricsManager> tEntry = (Map.Entry) tIter.next();
                tManager = tEntry.getValue();
                if (tManager != null) {
                    this.tMetricsFileWriter.write(tEntry.getKey() + ":\tTotal Indels:" + tManager.getIndelCount() + "\tTotal SNPs: "
                            + tManager.getSNPCount() + "\t\tTotal Positions: " + tManager.getTotalPositionsCount()
                            + "\t\tAverage Read Depth:" + tManager.getAverageReadDepth() + System.getProperty("line.separator"));
                    iTotalFilePosition = iTotalFilePosition + tManager.getTotalPositionsCount();
                    iTotalSNPFile = iTotalSNPFile + tManager.getSNPCount();
                    iTotalIndelFile = iTotalIndelFile + tManager.getIndelCount();
                    lTotalReadDepth += tManager.getAverageReadDepth();
                } // end if
            } // end while
              // Write out file totals.
            if (lCounter > 0) {
                lAvgTotalReadDepth = lTotalReadDepth / lCounter;
            } else {
                lAvgTotalReadDepth = 0;
            }
            this.tMetricsFileWriter.write(System.getProperty("line.separator"));
            this.tMetricsFileWriter.write("File: " + sVCFFileNameIn + "\tTotal Indels:" + iTotalIndelFile + "\tTotal SNPs: " + iTotalSNPFile
                    + "\t\tTotal Positions: " + iTotalFilePosition + "\t\tTotal Average Read Depth: " + lAvgTotalReadDepth
                    + System.getProperty("line.separator"));

            this.tMetricsFileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    } // end writeMetricsData

    /**
     * getPositionFromList()
     * <p>
     * Return the position given a List<String> representing a line of VCF data.
     * 
     * @param tListOfVCFLineElements
     *            -- List<String> of VCF line elements.
     * @return long -- the position of this VCF line as a long value.
     */
    public long getPositionFromList(List<String> tListOfVCFElementsIn) {
        String sPosition = tListOfVCFElementsIn.get(1);
        return Long.valueOf(sPosition).longValue();
    } // end getPositionFromList

} // end BZip2VCFConversionStrategy
