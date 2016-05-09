package org.renci.seqtools.converter;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import org.renci.seqtools.converter.genotypemap.GenotypeMap;

/**
 * BZip2VCFExomeConversionFormat
 * <p>
 * The goal of the BZip2VCFExomeConversionFormat is to write out exomic data gleaned from VCF and BAM files into
 * consensus files.
 * <p>
 * The conversion format handles both "regular" and indel (insertion/deletion) data.
 * 
 * @author k47k4705
 * 
 */
public class BZip2VCFExomeConversionFormat implements IConversionFormat {
    // Master consensus file output stream.
    private DataOutputStream tMasterOutputStream;

    // Detail consensus file output stream.
    private DataOutputStream tDetailOutputStream;

    // Text-mode buffered writer for text consensus file for testing purposes.
    private BufferedWriter tTextModeWriter;

    private List<String> tVCFLineList;

    // Map of genotypes in vcf file to binary/byte/bit representation.
    private GenotypeMap tGMap;

    /**
     * BZip2PileupConversionFormat public constructor
     * <p>
     * Makes a GenotypeMap util object contained by this object.
     */
    public BZip2VCFExomeConversionFormat() {

        // Get instance of genotype map: genotype <=> byte/bit representation.
        this.tGMap = GenotypeMap.getInstance();

    } // end BZip2PileupConversionFormat

    @Override
    public void writeToGenomicFormatVCF(DataOutputStream tDetailOutputStreamIn, DataOutputStream tMasterOutputStreamIn,
            BufferedWriter tTextWriterIn, VCFRecord tRecordIn) {

        // try {
        // System.out.println("hey from BZip2VCFExomeConversionFormat.writeToGenomicFormatThree() ... "
        // + tRecordIn.getGenotype() + " " +
        // this.getGenotype(tRecordIn.getGenotype()));
        // Thread.currentThread().sleep(1000);
        // } catch (Exception e1) {
        // // TODO Auto-generated catch block
        // e1.printStackTrace();
        // }

        this.tDetailOutputStream = tDetailOutputStreamIn;
        this.tMasterOutputStream = tMasterOutputStreamIn;
        this.tTextModeWriter = tTextWriterIn;

        try {
            this.tMasterOutputStream.flush();

            // System.out.println("genotype: " + sGenotype + " " +
            // this.getGenotype() + " consensus quality: " +
            // this.getConsensusQuality() + " snp quality: " +
            // this.getSNPQuality() + " mapping quailty: " +
            // this.getMappingQuality() + " read depth: " +
            // this.getReadDepth());

            this.tMasterOutputStream.writeLong(Long.parseLong(tRecordIn.getPosition()));
            this.tMasterOutputStream.writeByte(this.getGenotype(tRecordIn.getGenotype()));
            this.tMasterOutputStream.writeInt(Integer.parseInt(tRecordIn.getConsensusquality()));
            this.tMasterOutputStream.writeInt(Integer.parseInt(tRecordIn.getSnpquality()));
            this.tMasterOutputStream.writeShort(Short.parseShort(tRecordIn.getMappingquality()));
            this.tMasterOutputStream.writeInt(Integer.parseInt(tRecordIn.getReaddepth()));
            // Write long file pointer to data in the data file. In other words,
            // no indel data associated with this position.
            this.tMasterOutputStream.writeLong(-1);

            this.tMasterOutputStream.flush();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } // end catch

    } // end writeToGenomicFormatThree

    @Override
    public void writeToIndelFormatVCF(DataOutputStream tDetailOutputStreamIn, DataOutputStream tMasterOutputStream,
            BufferedWriter tTextModeWriterIn, VCFRecord tRecordIn) {

        this.tDetailOutputStream = tDetailOutputStreamIn;
        this.tMasterOutputStream = (DataOutputStream) tMasterOutputStream;
        this.tTextModeWriter = tTextModeWriterIn;

        try {
            this.tMasterOutputStream.flush();
            this.tDetailOutputStream.flush();

            // Get current master stream position, detail stream position.
            // int iCurrentMasterPosition = this.tMasterOutputStream.size();
            int iCurrentDetailPosition = this.tDetailOutputStream.size();

            String tBasesAndQualityScoresAndIndelData = tRecordIn.getReadbases() + "\t" + tRecordIn.getReadqualityScores() + "|";
            // System.out.println("writeToIndelFormatVCF: " +
            // tBasesAndQualityScoresAndIndelData);
            // System.out.println("writeToIndelFormatThree: " +
            // tBasesAndQualityScoresAndIndelData);
            ByteBuffer tBuffer = null;
            tBuffer = ByteBuffer.wrap(tBasesAndQualityScoresAndIndelData.getBytes());
            byte[] tBArray = new byte[tBuffer.remaining()];
            // System.out.println("tBArray.length: " + tBArray.length);
            tBuffer.get(tBArray);

            // Write data to master file. Not including reference coordinate.
            // This is an indel line. Add the indel genotype byte to the actual
            // genotype byte of this pileup line.
            // We do this so we can identify this record later (on read) as
            // having an indel line associated with it
            // in the detail file.
            this.tMasterOutputStream.writeLong(Long.parseLong(tRecordIn.getPosition()));
            // this.tMasterOutputStream.writeByte(this.getIndelGenotype());
            this.tMasterOutputStream.writeByte(this.getGenotype(tRecordIn.getGenotype()));
            this.tMasterOutputStream.writeInt(Integer.parseInt(tRecordIn.getConsensusquality()));
            this.tMasterOutputStream.writeInt(Integer.parseInt(tRecordIn.getSnpquality()));
            this.tMasterOutputStream.writeShort(Short.parseShort(tRecordIn.getMappingquality()));
            this.tMasterOutputStream.writeInt(Integer.parseInt(tRecordIn.getReaddepth()));
            this.tMasterOutputStream.writeLong(iCurrentDetailPosition);

            this.tDetailOutputStream.write(tBArray);

            this.tDetailOutputStream.flush();
            this.tMasterOutputStream.flush();

        } catch (Exception e) {
            e.printStackTrace();
        } // end catch

    } // end writeToIndelFormatThree

    @Override
    public void writeToSNPFormatVCF(DataOutputStream tDetailOutputStreamIn, DataOutputStream tMasterOutputStream,
            BufferedWriter tTextModeWriterIn, VCFRecord tRecordIn) {

        this.tDetailOutputStream = tDetailOutputStreamIn;
        this.tMasterOutputStream = (DataOutputStream) tMasterOutputStream;
        this.tTextModeWriter = tTextModeWriterIn;

        try {
            this.tMasterOutputStream.flush();
            this.tDetailOutputStream.flush();

            // Get current master stream position, detail stream position.
            // int iCurrentMasterPosition = this.tMasterOutputStream.size();
            int iCurrentDetailPosition = this.tDetailOutputStream.size();

            String tBasesAndQualityScoresAndIndelData = tRecordIn.getReferenceGenotype() + "\t" + tRecordIn.getReadbases() + "\t"
                    + tRecordIn.getReadqualityScores() + "|";
            // System.out.println("writeToIndelFormatVCF: " +
            // tBasesAndQualityScoresAndIndelData);
            // System.out.println("writeToIndelFormatThree: " +
            // tBasesAndQualityScoresAndIndelData);
            ByteBuffer tBuffer = null;
            tBuffer = ByteBuffer.wrap(tBasesAndQualityScoresAndIndelData.getBytes());
            byte[] tBArray = new byte[tBuffer.remaining()];
            // System.out.println("tBArray.length: " + tBArray.length);
            tBuffer.get(tBArray);

            // Write data to master file. Not including reference coordinate.
            // This is an indel line. Add the indel genotype byte to the actual
            // genotype byte of this pileup line.
            // We do this so we can identify this record later (on read) as
            // having an indel line associated with it
            // in the detail file.
            this.tMasterOutputStream.writeLong(Long.parseLong(tRecordIn.getPosition()));
            // this.tMasterOutputStream.writeByte(this.getIndelGenotype());
            this.tMasterOutputStream.writeByte(this.getGenotype(tRecordIn.getGenotype()));
            this.tMasterOutputStream.writeInt(Integer.parseInt(tRecordIn.getConsensusquality()));
            this.tMasterOutputStream.writeInt(Integer.parseInt(tRecordIn.getSnpquality()));
            this.tMasterOutputStream.writeShort(Short.parseShort(tRecordIn.getMappingquality()));
            this.tMasterOutputStream.writeInt(Integer.parseInt(tRecordIn.getReaddepth()));
            this.tMasterOutputStream.writeLong(iCurrentDetailPosition);

            this.tDetailOutputStream.write(tBArray);

            this.tDetailOutputStream.flush();
            this.tMasterOutputStream.flush();

        } catch (Exception e) {
            e.printStackTrace();
        } // end catch

    } // end writeToSNPFormatVCF

    @Override
    public int getMasterPosition() {
        return this.tMasterOutputStream.size();
    } // end getMasterPosition

    @Override
    public int getDetailPosition() {
        return this.tDetailOutputStream.size();
    } // end getDetailPosition

    /*
     * (non-Javadoc)
     * 
     * @see org.renci.sequencing.converter.IConversionFormat#getGenotype(java.lang .String)
     */
    public byte getGenotype(String sGenotypeIn) throws Exception {
        // byte to return.
        byte tByteToReturn = 00000000;

        // Try it with the genotype map class.
        if (this.tGMap.containsKey(sGenotypeIn)) {
            Byte tByte = (Byte) this.tGMap.get(sGenotypeIn);
            tByteToReturn = tByte.byteValue();
        } // end if

        return tByteToReturn;
    } // end getGenotype

    @Override
    public void writeToNoReferenceDataFormatVCF(DataOutputStream tDetailOutputStreamIn, DataOutputStream tMasterOutputStreamIn,
            BufferedWriter tTextWriterIn, VCFRecord tRecordIn) {
        // try {
        // System.out.println("hey from BZip2VCFExomeConversionFormat.writeToGenomicFormatThree() ... "
        // + tRecordIn.getGenotype() + " " +
        // this.getGenotype(tRecordIn.getGenotype()));
        // Thread.currentThread().sleep(1000);
        // } catch (Exception e1) {
        // // TODO Auto-generated catch block
        // e1.printStackTrace();
        // }

        this.tDetailOutputStream = tDetailOutputStreamIn;
        this.tMasterOutputStream = tMasterOutputStreamIn;
        this.tTextModeWriter = tTextWriterIn;

        try {
            this.tMasterOutputStream.flush();

            // System.out.println("genotype: " + sGenotype + " " +
            // this.getGenotype() + " consensus quality: " +
            // this.getConsensusQuality() + " snp quality: " +
            // this.getSNPQuality() + " mapping quailty: " +
            // this.getMappingQuality() + " read depth: " +
            // this.getReadDepth());

            this.tMasterOutputStream.writeLong(Long.parseLong(tRecordIn.getPosition()));
            this.tMasterOutputStream.writeByte(this.getGenotype(tRecordIn.getGenotype()));
            this.tMasterOutputStream.writeInt(Integer.parseInt(tRecordIn.getConsensusquality()));
            this.tMasterOutputStream.writeInt(Integer.parseInt(tRecordIn.getSnpquality()));
            this.tMasterOutputStream.writeShort(Short.parseShort(tRecordIn.getMappingquality()));
            this.tMasterOutputStream.writeInt(Integer.parseInt(tRecordIn.getReaddepth()));
            // Write long file pointer to data in the data file. In other words,
            // no indel data associated with this position.
            this.tMasterOutputStream.writeLong(-1);

            this.tMasterOutputStream.flush();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } // end catch

    } // end writeToNoReferenceDataFormatVCF

} // end BZip2VCFExomeConversionFormat
