package org.renci.seqtools.converter;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.commons.lang3.text.StrTokenizer;
import org.renci.seqtools.converter.util.Util;

/**
 * IndelHandler handles insertion and deletion detections of the pileup read bases.
 * <p>
 * It takes in a List of a line of pileup data (as well as a list of indel data, a subset of the pileup line). It writes
 * this data to a detail file and a master file.
 * <p>
 * When a line of master data is written, the position of the write in the master file is used as a starting point for
 * where to write data in the detail file.
 * <p>
 * We use the master position and an offset value to determine where to read and write data in the detail file.
 * <p>
 * We do this because the indel data can be of variable length. So we need some padding around each indel data write.
 * The padding we use is the position of the master file write times 100. Fortuntately, because we're using a
 * FileChannel and because we can write sparse data to a FileChannel, the space constraints will be minimal when we get
 * to the compression phase (bzip2 in this case) of the detail file.
 */
public class IndelHandler {

    private DataOutputStream tMasterStream;

    private DataOutputStream tDetailStream;

    private IConversionFormat tFormat;

    private StrTokenizer tTokenizer;

    private List<String> tIndelList;

    private FileChannel tDetailFileChannel;

    /**
     * IndelHandler
     * <p>
     * Private constructor
     * 
     * @param tDetailFileChannelIn
     * @param tMasterDataOutputStream
     * @param tDetailDataOutputStream
     */
    // private IndelHandler(FileChannel tDetailFileChannelIn, DataOutputStream
    // tMasterDataOutputStream, DataOutputStream tDetailDataOutputStream,
    // IConversionFormat tFormatIn, List<String> tIndelListIn, StrTokenizer
    // tTokenizerIn) {
    private IndelHandler(FileChannel tDetailFileChannelIn, DataOutputStream tMasterDataOutputStream, IConversionFormat tFormatIn,
            List<String> tIndelListIn, StrTokenizer tTokenizerIn) {
        this.tDetailFileChannel = tDetailFileChannelIn;
        this.tMasterStream = tMasterDataOutputStream;
        this.tFormat = tFormatIn;
        this.tTokenizer = tTokenizerIn;
        this.tIndelList = tIndelListIn;
    } // end IndelHandler

    /**
     * IndelHandler
     * <p>
     * Private constructor
     * 
     * @param tDetailFileChannelIn
     * @param tMasterDataOutputStream
     * @param tDetailDataOutputStream
     */
    // private IndelHandler(FileChannel tDetailFileChannelIn, DataOutputStream
    // tMasterDataOutputStream, DataOutputStream tDetailDataOutputStream,
    // IConversionFormat tFormatIn, List<String> tIndelListIn, StrTokenizer
    // tTokenizerIn) {
    private IndelHandler(DataOutputStream tDetailOutputStreamIn, DataOutputStream tMasterDataOutputStream, IConversionFormat tFormatIn,
            List<String> tIndelListIn, StrTokenizer tTokenizerIn) {
        this.tDetailStream = tDetailOutputStreamIn;
        this.tMasterStream = tMasterDataOutputStream;
        this.tFormat = tFormatIn;
        this.tTokenizer = tTokenizerIn;
        this.tIndelList = tIndelListIn;
    } // end IndelHandler

    /**
     * getInstance()
     * <p>
     * Returns an instance of this object.
     * 
     * @param tDetailDataOutputStream
     * @param tMasterDataOutputStream
     * @param tRAccessFile
     * @return
     */
    public static IndelHandler getInstance(FileChannel tDetailFileChannel, DataOutputStream tMasterDataOutputStream,
            IConversionFormat tFormatIn, List<String> tIndelList, StrTokenizer tTokenizer) {
        return new IndelHandler(tDetailFileChannel, tMasterDataOutputStream, tFormatIn, tIndelList, tTokenizer);
    } // end getInstance

    /**
     * getInstance()
     * <p>
     * Returns an instance of this object.
     * 
     * @param tDetailDataOutputStream
     * @param tMasterDataOutputStream
     * @param tRAccessFile
     * @return
     */
    public static IndelHandler getInstance(DataOutputStream tDetailOutputStream, DataOutputStream tMasterDataOutputStream,
            IConversionFormat tFormatIn, List<String> tIndelList, StrTokenizer tTokenizer) {
        return new IndelHandler(tDetailOutputStream, tMasterDataOutputStream, tFormatIn, tIndelList, tTokenizer);
    } // end getInstance

    /**
     * handleIndel()
     * <p>
     * handleIndel() figures out the insertion/deletions and writes out the right data. in the pileup data.
     * <p>
     * 
     * @param tList
     *            -- a String List of a line pulled from a pileup file.
     * @param tBufReaderIn
     *            -- the BufferedReader reading the pileup file ... in case we need to read more lines from the pileup
     *            file.
     * @throws IOException
     */
    public void handleIndel(List<String> tList, BufferedReader tBufReaderIn) throws IOException {
        // Get the read bases position data from the List.
        // System.out.println("IH handleIndel tList: " + tList.size() + " " +
        // tList.toString());
        String sIndel = tList.get(ConverterConstants.READ_BASES_POSITION);

        // Match insertions in the bases data.
        Matcher tInsertMatcher = ConverterConstants.PATTERN_INSERT.matcher(sIndel);
        if (tInsertMatcher.find()) {
            // System.out.println("Found an insert match");
            // We want to read the indel line and the number of
            // Integer.parseInt(tInsertMatcher.group(1)) lines below it.
            // So we add one to the number of insertions/deletions mentioned in
            // the pileup line.
            int iNumberToRead = Integer.parseInt(tInsertMatcher.group(1));
            // Add one/two?. The indel deletions start one line past the initial
            // indel "indicator" line.
            // iNumberToRead++; //iNumberToRead++;

            int iLoopVar = 0;
            // System.out.println("Found this insertion number: " +
            // tInsertMatcher.group(1));

            // this.tFormat.writeToIndelFormatTwo(tList,
            // this.tDetailFileChannel, this.tMasterStream,
            // Util.removeFromFrontOfList(tList, 8));
            // this.tFormat.writeToIndelFormatThree(tList, this.tDetailStream,
            // this.tMasterStream, Util.removeFromFrontOfList(tList, 8));

            tBufReaderIn.readLine();

            for (iLoopVar = 0; iLoopVar < iNumberToRead; iLoopVar++) {

                List<String> tNewList = Util.tabbedStringToList(tBufReaderIn.readLine());
                // System.out.println("tNewList.size() : " + tNewList.size() +
                // " " + tNewList.toString());
                // We may run out of real-estate here. At least, in tests we
                // read past the end of the file.
                if (tNewList.size() == 0) {
                    break;
                }

                List<String> tPileupList = Util.removeFromFrontOfList(tNewList, 2);
                List<String> tMyIndelList = Util.removeFromFrontOfList(tNewList, 8);
            }

        }

        Matcher tDeleteMatcher = ConverterConstants.PATTERN_DELETE.matcher(sIndel);
        if (tDeleteMatcher.find()) {
            int iNumberToRead = Integer.parseInt(tDeleteMatcher.group(1));
            int iLoopVar = 0;
            tBufReaderIn.readLine();

            for (iLoopVar = 0; iLoopVar < iNumberToRead; iLoopVar++) {
                List<String> tNewList = Util.tabbedStringToList(tBufReaderIn.readLine());
                if (tNewList.size() == 0) {
                    break;
                }

                List<String> tPileupList = Util.removeFromFrontOfList(tNewList, 2);
                List<String> tMyIndelList = Util.removeFromFrontOfList(tNewList, 8);

            }
        }

    }

    /**
     * hasIndel()
     * <p>
     * Determines whether the input line of pileup data as a string contains an insertion or deletion.
     * 
     * @param sReadBases
     *            -- String with the read bases suspected of having an insertion or deletion.
     * @return boolean, true: has indel, false: does not have indel
     */
    public boolean hasIndel(String sReadBases) {
        boolean bHasIndel = false;
        if (this.hasInsertion(sReadBases) || this.hasDeletion(sReadBases)) {
            bHasIndel = true;
        }
        return bHasIndel;
    } // end hasIndel

    private boolean hasInsertion(String sReadBases) {
        boolean bHasInsertion = false;
        if (sReadBases.matches(ConverterConstants.MARKER_INSERTION)) {
            bHasInsertion = true;
            // System.out.println("IndelHandler: hasInsertion: " + sReadBases);
        }
        return bHasInsertion;
    } // end

    private boolean hasDeletion(String sReadBases) {
        boolean bHasDeletion = false;
        if (sReadBases.matches(ConverterConstants.MARKER_DELETION)) {
            bHasDeletion = true;
            // System.out.println("IndelHandler: hasDeletion: " + sReadBases);
        }
        return bHasDeletion;
    } // end

} // end IndelHandler
