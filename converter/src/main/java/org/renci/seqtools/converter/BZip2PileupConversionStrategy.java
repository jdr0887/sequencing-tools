package org.renci.seqtools.converter;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.lang3.text.StrTokenizer;

/**
 * BZip2PileupConversionStrategy
 * 
 * @author k47k4705
 *         <p>
 *         BZip2PileupConversionStrategy converts an input pileup file of a certain format into two binary files.
 *         <p>
 *         The first file contains essential pileup data and a pointer on every line. The pointer on every line in the
 *         first file is present on every line in the second binary file, the detail file.
 *         <p>
 *         The detail file contains the pointer, the read bases, and the read quality scores.
 */
public class BZip2PileupConversionStrategy extends AbstractPileupConversionStrategy implements IConversionStrategy, Runnable {

    // The input pileup file to convert.
    private File tInputPileupFile;

    // General binary file data stream.
    private DataOutputStream tMasterDataOutputStream;

    // Detail binary file data stream.
    private DataOutputStream tDetailDataOutputStream;

    // Conversion format.
    private IConversionFormat tConversionFormat;

    // Name of this Strategy (for threading purposes).
    private String sName;

    // Have we seen an indel?
    private boolean bSawAnIndel = false;

    /**
     * BZip2PileupConversionStrategy public constructor
     * 
     * @param tInputPileupFileIn
     *            -- the input pileup file to convert.
     */
    public BZip2PileupConversionStrategy(IConversionFormat tFormatIn, File tInputPileupFileIn, String sNameIn, GenomeType tGenomeTypeIn) {

        // Input pileup file.
        this.tInputPileupFile = tInputPileupFileIn;
        this.sName = sNameIn;
        this.tConversionFormat = tFormatIn;

    } // end BZip2PileupConversionStrategy

    /**
     * toString()
     * <p>
     * Mainly hangs around for Thread purposes.
     * <p>
     * Yes, we are threaded. Though the current file sizes make it unlikely we'll run over more than one pileup file at
     * a time.
     */
    public String toString() {
        return this.sName + " created.";
    } // end toString

    /*
     * run()
     */
    public void run() {
        System.out.println(Thread.currentThread().getName() + " executing " + this);
        this.convert();
    } // end run

    /*
     * (non-Javadoc)
     * 
     * @see org.renci.sequencing.converter.AbstractPileupConversionStrategy#convert()
     */
    public void convert() {

        // Get base (without extension) file name.
        String sFullFileName = this.tInputPileupFile.getName();
        String sBaseFileName = this.getBaseName(sFullFileName);

        // Ask parent class for name of master file to create.
        String sMasterFileName = getMasterFileName(sBaseFileName);

        // Ask parent class for name of detail file to create.
        String sDetailFileName = getDetailFileName(sBaseFileName);

        // Make master and detail files based on input pileup file.
        try {

            // Make a String containing the full path and file name of the
            // master and detail binary files we'll create.
            String sMasterPathAndFile = this.tInputPileupFile.getParent() + this.tInputPileupFile.separatorChar + sMasterFileName;
            String sDetailPathAndFile = this.tInputPileupFile.getParent() + this.tInputPileupFile.separatorChar + sDetailFileName;

            // Make the files.
            File tMasterFile = new File(sMasterPathAndFile);
            File tDetailFile = new File(sDetailPathAndFile);

            if (tMasterFile.exists() && tDetailFile.exists()) {
                // Delete the files if they already exist.
                tMasterFile.delete();
                tDetailFile.delete();
            } else if (tMasterFile.exists() && !tDetailFile.exists()) {
                tMasterFile.delete();
            } else if (!tMasterFile.exists() && tDetailFile.exists()) {
                tDetailFile.delete();
            }

            // Create the files.
            tMasterFile.createNewFile();
            tDetailFile.createNewFile();

            System.out.println("\tWriting to master file: " + tMasterFile.getAbsolutePath());
            System.out.println("\tWriting to detail file: " + tDetailFile.getAbsolutePath());

            // Create detail file channel.
            // this.tDetailFileChannel = new
            // FileOutputStream(tDetailFile).getChannel();

            // Set up compressed data output streams.
            this.tMasterDataOutputStream = new DataOutputStream(
                    new BufferedOutputStream(new BZip2CompressorOutputStream(new FileOutputStream(tMasterFile))));

            this.tDetailDataOutputStream = new DataOutputStream(
                    new BufferedOutputStream(new BZip2CompressorOutputStream(new FileOutputStream(tDetailFile))));

            // Read in the pileup file.
            File tPileupFileToRead = this.tInputPileupFile;

            // Read pileup file into a buffer.
            BufferedReader tBufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(tPileupFileToRead)));

            //
            // Main processing loop
            //

            // Line from pileup file.
            String tLine = null;
            // List of tokens from the pileup file line.
            List<String> tPileupList = new ArrayList<String>();
            // List of indel tokens from an indel line of data.
            List<String> tIndelLineList = new ArrayList<String>();
            // Apache Commons Lang tokenizer. With empty constructor. Love it.
            StrTokenizer tSTokenizer = new StrTokenizer();
            // Tab-delimited file.
            tSTokenizer.setDelimiterString("\t");
            // List of pileup line.
            List<String> tList;

            // Loop over lines in the pileup file.
            while ((tLine = tBufferedReader.readLine()) != null) {

                // Reset the tokenizer for next pass.
                tSTokenizer.reset(tLine);

                // Parse line of text into a List<String>.
                tList = parseLine(tLine, tPileupList, tSTokenizer, tBufferedReader, tIndelLineList);

                // Write line to master and detail binary files.
                if (this.bSawAnIndel) {
                    this.bSawAnIndel = false;
                    tList.clear();
                    continue;
                } else {
                    this.writeLine(tList);
                }
                // Clear out pileup line, indel line list for next line from
                // pileup file.
                tPileupList.clear();
                tIndelLineList.clear();
                // tList = null;

            } // end while
              // System.out.println("End of buffered reader lines");
            tBufferedReader.close();

            // Close out the streams/files.
            this.tMasterDataOutputStream.close();
            this.tDetailDataOutputStream.close();

            System.out.println("\tFinished writing to master file: " + tMasterFile.getAbsolutePath());
            System.out.println("\tFinished writing to detail file: " + tDetailFile.getAbsolutePath());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    } // end convert

    /**
     * getBaseName()
     * <p>
     * getBaseName() returns the non-suffix, non-extension part of an input filename/String.
     * <p>
     * 
     * @param sFullFileName
     *            -- the full, suffixed version of a file name (as a String).
     * @return sBaseName -- the non-suffix, non-extension part of an input filename/String
     */
    private String getBaseName(String sFullFileName) {

        String sBaseFileName = null;
        int pos = sFullFileName.lastIndexOf('.');
        if (pos > 0 && pos < sFullFileName.length() - 1) {
            sBaseFileName = sFullFileName.substring(0, pos);
        } else {
            sBaseFileName = sFullFileName;
        }

        return sBaseFileName;

    } // end getBaseName()

    /**
     * parseLine()
     * <p>
     * parseLine() parses an input line from a pileup file.
     * 
     * @param sLineOfDataIn
     *            -- the input line from a pileup file as a String.
     * @return tList -- a List<String> containing data read from the input pileup file.
     */
    private List<String> parseLine(String sLineOfDataIn, List<String> tLineOfPileupDataIn, StrTokenizer tSTokenizerIn,
            BufferedReader tBufReaderIn, List<String> tIndelLineList) {

        // List of Strings to return.
        List<String> tList = tLineOfPileupDataIn;

        try {

            if (sLineOfDataIn == null) {
                System.out.println("sLineOfData is null");
            } else {
                // Read line via Scanner.
                StrTokenizer lineScanner = tSTokenizerIn;

                // Loop over line.
                while (lineScanner.hasNext()) {
                    tList.add(lineScanner.nextToken());
                } // end while
            } // end else
            System.out.println(tList.toString());

            // Handle insertions or deletions.
            IndelHandler tIndelHandler = IndelHandler.getInstance(this.tDetailDataOutputStream, this.tMasterDataOutputStream,
                    this.tConversionFormat, tIndelLineList, tSTokenizerIn);

            // Remove elements from line of pileup data.
            if (tList.size() > 3) {

                // Remove elements from the pileup line.
                this.removePileupElements(tList);

                // Check readbases for insertions or deletions.
                String sReadBases = tList.get(ConverterConstants.READ_BASES_POSITION);

                // Deletion? Insertion?
                if (tIndelHandler.hasIndel(sReadBases)) {
                    this.bSawAnIndel = true;
                    tIndelHandler.handleIndel(tList, tBufReaderIn);
                    // System.out.println("BZPCS parseLine: past tIndelHandler.handleIndel");
                } else {
                    // System.out.println("BZCS: not indel");
                }

            } else {
                System.out.println("Got a List with < 3 elements " + tList.size());
            } // end else

        } catch (Exception e) {
            e.printStackTrace();
        } // end catch
        return tList;
    } // end parseLine

    /**
     * removePileupElements() removes selected elements from the assembled List<String> of pileup elements.
     */
    private List<String> removePileupElements(List<String> tListIn) {
        tListIn.remove(0); // Remove chromosome
        tListIn.remove(1); // Remove reference base.
        // So we go from a pileup line like:
        // [1, 399, C, C, 13, 0, 3, 94,
        // ,$T$..,.G......,.T,T,,...,.,,..,....,,,,,ta,,a.a,,,,..,,,,.,,,,,,..,,,,,,.,,,,,....^!,^!,^!,^!,^!,^!,^!,^!,^!,^!,^!,^!.^!.,
        // >%5%6.%%$:%&4:%';'==66%=,9-7>,:<<>76'-)*&;*%>$60:6;>)-/9:8$*780<<8/(=%0<,+8:49;=;3/3788509-8GE]
        // to a line like:
        // [399, C, 13, 0, 3, 94,
        // ,$T$..,.G......,.T,T,,...,.,,..,....,,,,,ta,,a.a,,,,..,,,,.,,,,,,..,,,,,,.,,,,,....^!,^!,^!,^!,^!,^!,^!,^!,^!,^!,^!,^!.^!.,
        // >%5%6.%%$:%&4:%';'==66%=,9-7>,:<<>76'-)*&;*%>$60:6;>)-/9:8$*780<<8/(=%0<,+8:49;=;3/3788509-8GE]
        return tListIn;
    } // end removePileupElements

    /**
     * writeLine()
     * <p>
     * writeLine() writes a line of data to several binary files.
     * 
     * @param tListIn
     *            -- a List<String> of input data.
     */
    private void writeLine(List<String> tListIn) {
        // Use a format object to convert and write the data.
        // this.tConversionFormat.writeToGenomicFormatThree(tListIn,
        // this.tDetailDataOutputStream, this.tMasterDataOutputStream);
    } // end writeLine

} // end GZipBinaryPileupConversionStrategy
