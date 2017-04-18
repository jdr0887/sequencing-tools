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

public class VCFMasterDetailFileStreamManager {

    private String sMaster = "master-";

    private String sDetail = "detail-";

    private String sDatBZ2 = ".dat.bz2";

    private String sBaseFileName;

    private File tVCFFile;

    private File tOutputDir;

    private File tMasterFile2;

    private File tDetailFile2;

    private File tTextModeTestFile;

    private DataOutputStream tMasterStream2;

    private DataOutputStream tDetailStream2;

    private BufferedWriter tTextModeWriter;

    private IConversionFormat tFormat;

    private int iSampleColumnNumber;

    private List<String> tIndelList;

    private boolean bFirstWriteToStream = false;

    private String sFirstPosition = null;

    private String sLastPosition = null;

    private VCFMasterDetailFileStreamManager(String sFileName, int iSlotNumberIn, File sVCFFileIn, File tOutputDirIn,
            IConversionFormat tFormatIn) {
        this.sBaseFileName = sFileName;
        this.tVCFFile = sVCFFileIn;
        this.tOutputDir = tOutputDirIn;
        this.tFormat = tFormatIn;
        this.iSampleColumnNumber = iSlotNumberIn;
        this.tIndelList = new ArrayList<String>();
        this.bFirstWriteToStream = true;

    }

    public static VCFMasterDetailFileStreamManager getInstance(String sFileNameIn, int iSlotNumberIn, File tVCFFileIn, File tOutputDirIn,
            IConversionFormat tFormatIn) {
        return new VCFMasterDetailFileStreamManager(sFileNameIn, iSlotNumberIn, tVCFFileIn, tOutputDirIn, tFormatIn);
    }

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
    }

    public String getMasterName() {
        return this.sMaster + this.sBaseFileName + this.sDatBZ2;
    }

    public String getDetailName() {
        return this.sDetail + this.sBaseFileName + this.sDatBZ2;
    }

    public boolean isSampleColumnNumber(int iSampleColumnNumberIn) {
        return this.iSampleColumnNumber == iSampleColumnNumberIn;
    }

    public int getSampleColumnNumber() {
        return this.iSampleColumnNumber;
    }

    public void createFilesAndStreams2(String sChromosomeNameInDashSampleColumnIn) {

        try {
            String sMasterPathAndFile = this.tOutputDir.getCanonicalPath() + File.separatorChar + "master-" + this.tVCFFile.getName() + "-"
                    + sChromosomeNameInDashSampleColumnIn + ".dat.bz2";
            String sDetailPathAndFile = this.tOutputDir.getCanonicalPath() + File.separatorChar + "detail-" + this.tVCFFile.getName() + "-"
                    + sChromosomeNameInDashSampleColumnIn + ".dat.bz2";

            this.tMasterFile2 = new File(sMasterPathAndFile);
            this.tDetailFile2 = new File(sDetailPathAndFile);

            if (this.tMasterFile2.exists() && this.tDetailFile2.exists()) {

                tMasterFile2.delete();
                tDetailFile2.delete();
            }

            this.tMasterFile2.createNewFile();
            this.tDetailFile2.createNewFile();
            this.tMasterStream2 = new DataOutputStream(
                    new BufferedOutputStream(new BZip2CompressorOutputStream(new FileOutputStream(this.tMasterFile2))));
            this.tDetailStream2 = new DataOutputStream(
                    new BufferedOutputStream(new BZip2CompressorOutputStream(new FileOutputStream(this.tDetailFile2))));

            String sTextConsensusFilePathAndFile = this.tOutputDir.getCanonicalPath() + File.separatorChar + "text-mode-"
                    + this.tVCFFile.getName() + "-" + sChromosomeNameInDashSampleColumnIn + ".txt";
            this.tTextModeTestFile = new File(sTextConsensusFilePathAndFile);
            this.tTextModeTestFile.createNewFile();
            this.tTextModeWriter = new BufferedWriter(new FileWriter(this.tTextModeTestFile));

        } catch (IOException e1) {

            e1.printStackTrace();
        }

    }

    public void writeGenomicData(VCFRecord tRecordIn) {

        if (this.bFirstWriteToStream) {
            this.sFirstPosition = tRecordIn.getPosition();
            this.bFirstWriteToStream = false;
        }
        this.sLastPosition = tRecordIn.getPosition();

        this.tFormat.writeToGenomicFormatVCF(this.tDetailStream2, this.tMasterStream2, this.tTextModeWriter, tRecordIn);
    }

    public void writeIndelData(VCFRecord tRecordIn) {

        tIndelList.clear();

        if (this.bFirstWriteToStream) {
            this.sFirstPosition = tRecordIn.getPosition();
            this.bFirstWriteToStream = false;
        }

        this.sLastPosition = tRecordIn.getPosition();

        this.tFormat.writeToIndelFormatVCF(this.tDetailStream2, this.tMasterStream2, this.tTextModeWriter, tRecordIn);
    }

    public void writeSNPData(VCFRecord tRecordIn) {

        tIndelList.clear();

        if (this.bFirstWriteToStream) {
            this.sFirstPosition = tRecordIn.getPosition();
            this.bFirstWriteToStream = false;
        }

        this.sLastPosition = tRecordIn.getPosition();

        this.tFormat.writeToSNPFormatVCF(this.tDetailStream2, this.tMasterStream2, this.tTextModeWriter, tRecordIn);
    }

    public void writeNoReferenceData(VCFRecord tRecordIn) {

        if (this.bFirstWriteToStream) {
            this.sFirstPosition = tRecordIn.getPosition();
            this.bFirstWriteToStream = false;
        }
        this.sLastPosition = tRecordIn.getPosition();

        this.tFormat.writeToGenomicFormatVCF(this.tDetailStream2, this.tMasterStream2, this.tTextModeWriter, tRecordIn);

    }

    public int getMasterPosition() {
        return this.tFormat.getMasterPosition();
    }

    public int getDetailPosition() {
        return this.tFormat.getDetailPosition();
    }

    public File getMasterFile() {
        return this.tMasterFile2;
    }

    public File getDetailFile() {
        return this.tDetailFile2;
    }

    public String getFirstPositionInFile() {
        return this.sFirstPosition;
    }

    public String getLastPositionInFile() {
        return this.sLastPosition;
    }

}
