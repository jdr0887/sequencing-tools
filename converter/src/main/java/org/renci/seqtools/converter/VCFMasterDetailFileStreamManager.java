package org.renci.seqtools.converter;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

/**
 * VCFMasterDetailFileStreamManager holds pairs of Files and Streams for master and detail objects.
 * <p>
 * Let's see if we can put some more functionality, file creation, dataoutputstream creation, format writing into this
 * object.
 * <p>
 * Let's loop this object and call methods in the loop.
 */
public class VCFMasterDetailFileStreamManager {

    // Master name, detail name. Various file creation superflua.
    private String sMaster = "master-";

    private String sDetail = "detail-";

    private String sDatBZ2 = ".dat.bz2";

    // Base consensus filename for master and detail consensus files.
    // We'll wrap a master- and a .dat.bz2 onto this name.
    private String sBaseFileName;

    // File abstractions.
    private File tVCFFile;

    // Output dir.
    private File tOutputDir;

    // Master and Detail File objects.
    private File tMasterFile2;

    private File tDetailFile2;

    // Text-mode test consensus file output file.
    private File tTextModeTestFile;

    // Master and detail output streams to write consensus data to.
    private DataOutputStream tMasterStream2;

    private DataOutputStream tDetailStream2;

    // Text-mode consensus output buffered writer.
    private BufferedWriter tTextModeWriter;

    // The contained format object, a delegate that does the actual writing.
    private IConversionFormat tFormat;

    // The number of an individual (a column number for an individual in a VCF
    // file).
    private int iSampleColumnNumber;

    // Empty list of Strings passed to and filled by contained format object.
    private List<String> tIndelList;

    // Have we written to this stream yet?
    private boolean bFirstWriteToStream = false;

    // The first position for this file.
    private String sFirstPosition = null;

    // The last position for this file.
    private String sLastPosition = null;

    /**
     * VCFMasterDetailFileStreamManager public constructor
     * 
     * @param sFileName
     *            -- base filename
     * @param iSlotNumberIn
     *            -- sample column number
     * @param sVCFFileIn
     *            -- the VCF file to parse
     * @param tFormtIn
     *            -- the IConversionFormat (format object to write with)
     * 
     */
    private VCFMasterDetailFileStreamManager(String sFileName, int iSlotNumberIn, File sVCFFileIn, File tOutputDirIn,
            IConversionFormat tFormatIn) {
        this.sBaseFileName = sFileName;
        this.tVCFFile = sVCFFileIn;
        this.tOutputDir = tOutputDirIn;
        this.tFormat = tFormatIn;
        this.iSampleColumnNumber = iSlotNumberIn;
        this.tIndelList = new ArrayList<String>();
        this.bFirstWriteToStream = true;

    } // end VCFMasterDetailFileStreamManager

    /**
     * getInstance()
     * 
     * @param sFileNameIn
     *            -- name of base consensus filename to construct for both master and detail files.
     * @param iSlotNumberIn
     *            -- the column number for this individual found in the consensus file (used to associate files and data
     *            to this individual).
     * @param sVCFFileIn
     *            -- the VCF file.
     * @param tFormatIn
     *            -- the IConversionFormat object -- a delegate that does the actual writing using DataOutputStreams for
     *            master and detail consensus files.
     * @return VCFMasterDetailFileStreamManager -- an instance of a MasterDetailContainer.
     */
    public static VCFMasterDetailFileStreamManager getInstance(String sFileNameIn, int iSlotNumberIn, File tVCFFileIn, File tOutputDirIn,
            IConversionFormat tFormatIn) {
        return new VCFMasterDetailFileStreamManager(sFileNameIn, iSlotNumberIn, tVCFFileIn, tOutputDirIn, tFormatIn);
    } // end getInstance

    /**
     * closeStreams()
     * <p>
     * House-keeping for streams.
     */
    public void closeStreams() {
        try {
            this.tMasterStream2.flush();
            this.tDetailStream2.flush();
            this.tMasterStream2.close();
            this.tDetailStream2.close();
            this.tTextModeWriter.flush();
            this.tTextModeWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    } // end closeStreams

    /**
     * getMasterName()
     * 
     * @return String -- name of master thingy.
     */
    public String getMasterName() {
        return this.sMaster + this.sBaseFileName + this.sDatBZ2;
    } // end getMasterName

    /**
     * getDetailName()
     * 
     * @return String -- name of detail thingy.
     */
    public String getDetailName() {
        return this.sDetail + this.sBaseFileName + this.sDatBZ2;
    } // end getDetailName

    /**
     * isSampleColumnNumber()
     * <p>
     * Returns whether an input sample column number corresponds to the contained sample column number for this object.
     * 
     * @param iSampleColumnNumberIn
     *            -- the sample column number at issue.
     * @return boolean -- true, the sample column numbers are the same, false, the sample column numbers are not the
     *         same.
     */
    public boolean isSampleColumnNumber(int iSampleColumnNumberIn) {
        return this.iSampleColumnNumber == iSampleColumnNumberIn;
    } // end isSlotNumber

    /**
     * getSampleColumnNumber()
     * <p>
     * The sample column number of an individual in the VCF file. Starts from 0.
     * 
     * @return int -- the column number. Starts from 0.
     */
    public int getSampleColumnNumber() {
        return this.iSampleColumnNumber;
    } // end getSampleColumnNumber

    /**
     * createFiles2()
     * <p>
     * Create master and detail files and streams for this manager object.
     * 
     * @param sChromosomeNameInDashSampleColumnIn
     *            -- the sample column string used to name the master and detail consensus file.
     * 
     */
    public void createFilesAndStreams2(String sChromosomeNameInDashSampleColumnIn) {
        // Loop over the column names.

        // Make a String containing the full path and file name of the master
        // and detail binary files we'll create.
        // String sMasterPathAndFile = this.tVCFFile.getParent() +
        // File.separatorChar + "master-" + this.tVCFFile.getName() + "-" +
        // sChromosomeNameInDashSampleColumnIn + ".dat.bz2";
        // String sDetailPathAndFile = this.tVCFFile.getParent() +
        // File.separatorChar + "detail-" + this.tVCFFile.getName() + "-" +
        // sChromosomeNameInDashSampleColumnIn + ".dat.bz2";

        try {
            String sMasterPathAndFile = this.tOutputDir.getCanonicalPath() + File.separatorChar + "master-" + this.tVCFFile.getName() + "-"
                    + sChromosomeNameInDashSampleColumnIn + ".dat.bz2";
            String sDetailPathAndFile = this.tOutputDir.getCanonicalPath() + File.separatorChar + "detail-" + this.tVCFFile.getName() + "-"
                    + sChromosomeNameInDashSampleColumnIn + ".dat.bz2";

            // Make the files.
            this.tMasterFile2 = new File(sMasterPathAndFile);
            this.tDetailFile2 = new File(sDetailPathAndFile);

            if (this.tMasterFile2.exists() && this.tDetailFile2.exists()) {
                // Delete the file if it already exists.
                tMasterFile2.delete();
                tDetailFile2.delete();
            }

            this.tMasterFile2.createNewFile();
            this.tDetailFile2.createNewFile();
            this.tMasterStream2 = new DataOutputStream(
                    new BufferedOutputStream(new BZip2CompressorOutputStream(new FileOutputStream(this.tMasterFile2))));
            this.tDetailStream2 = new DataOutputStream(
                    new BufferedOutputStream(new BZip2CompressorOutputStream(new FileOutputStream(this.tDetailFile2))));

            // Create text-mode test consensus file output.
            String sTextConsensusFilePathAndFile = this.tOutputDir.getCanonicalPath() + File.separatorChar + "text-mode-"
                    + this.tVCFFile.getName() + "-" + sChromosomeNameInDashSampleColumnIn + ".txt";
            this.tTextModeTestFile = new File(sTextConsensusFilePathAndFile);
            this.tTextModeTestFile.createNewFile();
            this.tTextModeWriter = new BufferedWriter(new FileWriter(this.tTextModeTestFile));

        } catch (IOException e1) {

            e1.printStackTrace();
        }

    } // end createFiles2()

    /**
     * writeGenomicData()
     * <p>
     * Delegates genomic consensus data writing to the contained IConversionFormat object.
     * <p>
     * 
     * @param tListIn
     *            -- the List<String> to write to the master and detail streams.
     * @param tRecordIn
     *            -- the input VCFRecord to write.
     */

    public void writeGenomicData(VCFRecord tRecordIn) {

        // Get the first position of this file / last position of this file.
        if (this.bFirstWriteToStream) {
            this.sFirstPosition = tRecordIn.getPosition();
            this.bFirstWriteToStream = false;
        } // end if
        this.sLastPosition = tRecordIn.getPosition();
        // Write out the VCF data as consensus data.
        this.tFormat.writeToGenomicFormatVCF(this.tDetailStream2, this.tMasterStream2, this.tTextModeWriter, tRecordIn);
    } // end writeData

    /**
     * writeIndelData
     * 
     * @param tDataList
     *            -- the List<String> to write to the master and detail streams.
     * @param tRecordIn
     *            -- the VCFRecord to write to the master and detail streams.
     */

    public void writeIndelData(VCFRecord tRecordIn) {

        tIndelList.clear();
        // Get the first position of this file / last position of this file.
        if (this.bFirstWriteToStream) {
            this.sFirstPosition = tRecordIn.getPosition();
            this.bFirstWriteToStream = false;
        } // end if

        this.sLastPosition = tRecordIn.getPosition();
        // Write out the VCF data as indel consensus data.
        this.tFormat.writeToIndelFormatVCF(this.tDetailStream2, this.tMasterStream2, this.tTextModeWriter, tRecordIn);
    } // end writeIndelData

    /**
     * writeSNPData
     * 
     * @param tDataList
     *            -- the List<String> to write to the master and detail streams.
     * @param tRecordIn
     *            -- the VCFRecord to write to the master and detail streams.
     */

    public void writeSNPData(VCFRecord tRecordIn) {

        tIndelList.clear();
        // Get the first position of this file / last position of this file.
        if (this.bFirstWriteToStream) {
            this.sFirstPosition = tRecordIn.getPosition();
            this.bFirstWriteToStream = false;
        } // end if

        this.sLastPosition = tRecordIn.getPosition();
        // Write out the VCF data as indel consensus data.
        this.tFormat.writeToSNPFormatVCF(this.tDetailStream2, this.tMasterStream2, this.tTextModeWriter, tRecordIn);
    } // end writeSNPData

    /**
     * writeNoReferenceData()
     * <p>
     * If we don't have any reference data at a given position, write out to consensus files.
     * 
     * @param tRecord
     */
    public void writeNoReferenceData(VCFRecord tRecordIn) {
        // Get the first position of this file / last position of this file.
        if (this.bFirstWriteToStream) {
            this.sFirstPosition = tRecordIn.getPosition();
            this.bFirstWriteToStream = false;
        } // end if
        this.sLastPosition = tRecordIn.getPosition();
        // Write out the VCF data as consensus data.
        this.tFormat.writeToGenomicFormatVCF(this.tDetailStream2, this.tMasterStream2, this.tTextModeWriter, tRecordIn);

    } // end writeNoReferenceData()

    public int getMasterPosition() {
        return this.tFormat.getMasterPosition();
    }

    public int getDetailPosition() {
        return this.tFormat.getDetailPosition();
    }

    /**
     * getMasterFile()
     * <p>
     * Return the master consensus file.
     * 
     * @return File -- the master consensus file.
     */
    public File getMasterFile() {
        return this.tMasterFile2;
    } // end getMasterFile

    /**
     * getDetailFile()
     * <p>
     * Return the detail consensus file.
     * 
     * @return File -- the detail consensus file.
     */
    public File getDetailFile() {
        return this.tDetailFile2;
    } // end getDetailFile

    /**
     * getFirstPositionInFile()
     * <p>
     * Return the first position written to a master consensus file.
     * 
     * @return String -- the first position as a String.
     */
    public String getFirstPositionInFile() {
        return this.sFirstPosition;
    } // end getFirstPositionInFile()

    /**
     * getLastPositionInFile()
     * <p>
     * Returh the last position written to a master consensus file.
     * 
     * @return String -- the last position as a String.
     */
    public String getLastPositionInFile() {
        return this.sLastPosition;
    } // end getLastPositionInFile()

} // end VCFMasterDetailFileStreamManager
