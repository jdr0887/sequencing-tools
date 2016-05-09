package org.renci.seqtools.converter;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.renci.seqtools.converter.genotypemap.GenotypeMap;

/**
 * BZip2VCFConversionFormat
 * <p>
 * The conversion format for VCF files / BAM files writing genomic data (as opposed to exomic data) to consensus master
 * and detail files.
 * <p>
 * One key difference between this object and the exome object is this: genomic data in consensus files does not contain
 * the reference position or coordinate.
 * <p>
 * The exome format object does contain reference position or coordinate data in the consensus file output.
 * <p>
 * Handles the output format for VCF<->Consensus files. Deals with the actual dataoutputstreams for both master and
 * detail consensus files.
 * <p>
 * Should probably be identical to other conversion formats but there may be some trickiness here. TBD.
 * 
 * @author k47k4705
 * 
 */
public class BZip2VCFConversionFormat implements IConversionFormat {
    // Master consensus file output stream.
    private DataOutputStream tMasterOutputStream;

    // Detail consensus file output stream.
    private DataOutputStream tDetailOutputStream;

    // Test-mode textual output consensus writer.
    private BufferedWriter tTestModeWriter;

    // Are we in test-mode?
    private boolean bIsTestMode;

    // String constants for forward strand, reverse strand.
    private static final String FORWARD_STRAND_LABEL = "F";

    private static final String REVERSE_STRAND_LABEL = "R";

    // Map of genotypes/bases in vcf/bam file to binary/byte/bit representation.
    private GenotypeMap tGMap;

    /**
     * BZip2PileupConversionFormat public constructor
     * <p>
     * Makes a GenotypeMap util object contained by this object.
     */
    public BZip2VCFConversionFormat(boolean bIsTestModeIn) {
        // Get instance of genotype map: genotype <=> byte/bit representation.
        this.tGMap = GenotypeMap.getInstance();
        this.bIsTestMode = bIsTestModeIn;

    } // end BZip2PileupConversionFormat

    @Override
    public void writeToGenomicFormatVCF(DataOutputStream tDetailOutputStreamIn, DataOutputStream tMasterOutputStreamIn,
            BufferedWriter tTestModeWriterIn, VCFRecord tRecordIn) {
        // System.out.println("hey from BZip2VCFConversionFormat.writeToGenomicFormatThree() ... "
        // + tListIn.toString());

        this.tDetailOutputStream = tDetailOutputStreamIn;
        this.tMasterOutputStream = tMasterOutputStreamIn;
        this.tTestModeWriter = tTestModeWriterIn;

        try {

            // Write data to streams or writers.
            // Do we have a not-null Writer? Write to text file.
            if (this.tTestModeWriter != null && this.bIsTestMode) {

                this.tTestModeWriter.write(tRecordIn.getPosition());
                this.tTestModeWriter.write("\t");
                this.tTestModeWriter.write(tRecordIn.getGenotype());
                this.tTestModeWriter.write("\t");
                this.tTestModeWriter.write(tRecordIn.getConsensusquality());
                this.tTestModeWriter.write("\t");
                this.tTestModeWriter.write(tRecordIn.getSnpquality());
                this.tTestModeWriter.write("\t");
                this.tTestModeWriter.write(tRecordIn.getMappingquality());
                this.tTestModeWriter.write("\t");
                this.tTestModeWriter.write(tRecordIn.getReaddepth());
                this.tTestModeWriter.write("\t");
                this.tTestModeWriter.write("-1");
                this.tTestModeWriter.write(System.getProperty("line.separator"));

            } else {
                // Write to stream.
                this.tMasterOutputStream.flush();
                this.tMasterOutputStream.writeByte(this.getGenotype(tRecordIn.getGenotype()));
                this.tMasterOutputStream.writeInt(Integer.parseInt(tRecordIn.getConsensusquality()));
                this.tMasterOutputStream.writeInt(Integer.parseInt(tRecordIn.getSnpquality()));
                this.tMasterOutputStream.writeShort(Short.parseShort(tRecordIn.getMappingquality()));
                this.tMasterOutputStream.writeInt(Integer.parseInt(tRecordIn.getReaddepth()));
                // Write long file pointer to data in the data file. In other
                // words, no indel data associated with this position.
                this.tMasterOutputStream.writeLong(-1);

                this.tMasterOutputStream.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();

        } // end catch

    } // end writeToGenomicFormatThree

    @Override
    public void writeToIndelFormatVCF(DataOutputStream tDetailOutputStreamIn, DataOutputStream tMasterOutputStream,
            BufferedWriter tTestModeWriterIn, VCFRecord tRecordIn) {

        this.tDetailOutputStream = tDetailOutputStreamIn;
        this.tMasterOutputStream = (DataOutputStream) tMasterOutputStream;
        this.tTestModeWriter = tTestModeWriterIn;

        try {
            this.tMasterOutputStream.flush();
            this.tDetailOutputStream.flush();

            // Figure out the strand direction.
            String sStrand = tRecordIn.isReverseStrand() ? BZip2VCFConversionFormat.REVERSE_STRAND_LABEL
                    : BZip2VCFConversionFormat.FORWARD_STRAND_LABEL;

            // Get current master stream position, detail stream position.
            // int iCurrentMasterPosition = this.tMasterOutputStream.size();
            int iCurrentDetailPosition = this.tDetailOutputStream.size();

            // Write detail data in to "sparse" detail file at the same position
            // as the master file.
            // So, look ups of the data in the master file will translate well
            // into lookups in the detail file.
            String tBasesAndQualityScoresAndIndelData = sStrand + "\t" + tRecordIn.getReadbases() + "\t" + tRecordIn.getReadqualityScores()
                    + "|";
            // System.out.println("writeToIndelFormatVCF: " +
            // tBasesAndQualityScoresAndIndelData);
            // System.out.println("writeToIndelFormatThree: " +
            // tBasesAndQualityScoresAndIndelData);
            ByteBuffer tBuffer = null;
            tBuffer = ByteBuffer.wrap(tBasesAndQualityScoresAndIndelData.getBytes());
            byte[] tBArray = new byte[tBuffer.remaining()];
            // System.out.println("tBArray.length: " + tBArray.length);
            tBuffer.get(tBArray);

            // Do we have a

            // Write data to master file. Not including reference coordinate.
            // This is an indel line. Add the indel genotype byte to the actual
            // genotype byte of this pileup line.
            // We do this so we can identify this record later (on read) as
            // having an indel line associated with it
            // in the detail file.
            // this.tMasterOutputStream.writeLong(Long.parseLong(tRecordIn.getPosition()));
            // this.tMasterOutputStream.writeByte(this.getIndelGenotype());

            // Is the test writer not-null? That means we are in "test" mode and
            // we write out a text-based consensus file.
            if (tTestModeWriterIn != null && this.bIsTestMode) {

                this.tTestModeWriter.write(tRecordIn.getPosition());
                this.tTestModeWriter.write("\t");
                this.tTestModeWriter.write(tRecordIn.getGenotype());
                this.tTestModeWriter.write("\t");
                this.tTestModeWriter.write(tRecordIn.getConsensusquality());
                this.tTestModeWriter.write("\t");
                this.tTestModeWriter.write(tRecordIn.getSnpquality());
                this.tTestModeWriter.write("\t");
                this.tTestModeWriter.write(tRecordIn.getMappingquality());
                this.tTestModeWriter.write("\t");
                this.tTestModeWriter.write(tRecordIn.getReaddepth());
                this.tTestModeWriter.write("\t");
                this.tTestModeWriter.write(new String(tBArray));
                this.tTestModeWriter.write(System.getProperty("line.separator"));

            } else {

                this.tMasterOutputStream.writeByte(this.getGenotype(tRecordIn.getGenotype()));
                this.tMasterOutputStream.writeInt(Integer.parseInt(tRecordIn.getConsensusquality()));
                this.tMasterOutputStream.writeInt(Integer.parseInt(tRecordIn.getSnpquality()));
                this.tMasterOutputStream.writeShort(Short.parseShort(tRecordIn.getMappingquality()));
                this.tMasterOutputStream.writeInt(Integer.parseInt(tRecordIn.getReaddepth()));
                this.tMasterOutputStream.writeLong(iCurrentDetailPosition);

                this.tDetailOutputStream.write(tBArray);

                this.tDetailOutputStream.flush();
                this.tMasterOutputStream.flush();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } // end catch

    } // end writeToIndelFormatThree

    @Override
    public void writeToSNPFormatVCF(DataOutputStream tDetailOutputStreamIn, DataOutputStream tMasterOutputStream,
            BufferedWriter tTestModeWriterIn, VCFRecord tRecordIn) {

        this.tDetailOutputStream = tDetailOutputStreamIn;
        this.tMasterOutputStream = (DataOutputStream) tMasterOutputStream;
        this.tTestModeWriter = tTestModeWriterIn;

        try {
            this.tMasterOutputStream.flush();
            this.tDetailOutputStream.flush();

            // Figure out the strand direction.
            String sStrand = tRecordIn.isReverseStrand() ? BZip2VCFConversionFormat.REVERSE_STRAND_LABEL
                    : BZip2VCFConversionFormat.FORWARD_STRAND_LABEL;

            // Get current master stream position, detail stream position.
            // int iCurrentMasterPosition = this.tMasterOutputStream.size();
            int iCurrentDetailPosition = this.tDetailOutputStream.size();

            // Write detail data in to "sparse" detail file at the same position
            // as the master file.
            // So, look ups of the data in the master file will translate well
            // into lookups in the detail file.
            String tBasesAndQualityScoresAndIndelData = sStrand + "\t" + tRecordIn.getReferenceGenotype() + "\t" + tRecordIn.getReadbases()
                    + "\t" + tRecordIn.getReadqualityScores() + "|";

            // String tBasesAndQualityScoresAndIndelData =
            // tRecordIn.getReadbases() + "\t" +
            // tRecordIn.getReadqualityScores() + "|";
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
            // this.tMasterOutputStream.writeLong(Long.parseLong(tRecordIn.getPosition()));
            // this.tMasterOutputStream.writeByte(this.getIndelGenotype());

            // Is the Writer not-null? This means we output a text-based
            // consensus file for testing purposes.
            if (this.tTestModeWriter != null && this.bIsTestMode) {

                this.tTestModeWriter.write(tRecordIn.getPosition());
                this.tTestModeWriter.write("\t");
                this.tTestModeWriter.write(tRecordIn.getGenotype());
                this.tTestModeWriter.write("\t");
                this.tTestModeWriter.write(tRecordIn.getConsensusquality());
                this.tTestModeWriter.write("\t");
                this.tTestModeWriter.write(tRecordIn.getSnpquality());
                this.tTestModeWriter.write("\t");
                this.tTestModeWriter.write(tRecordIn.getMappingquality());
                this.tTestModeWriter.write("\t");
                this.tTestModeWriter.write(tRecordIn.getReaddepth());
                this.tTestModeWriter.write("\t");
                this.tTestModeWriter.write(new String(tBArray));
                this.tTestModeWriter.write(System.getProperty("line.separator"));

            } else {
                this.tMasterOutputStream.writeByte(this.getGenotype(tRecordIn.getGenotype()));
                this.tMasterOutputStream.writeInt(Integer.parseInt(tRecordIn.getConsensusquality()));
                this.tMasterOutputStream.writeInt(Integer.parseInt(tRecordIn.getSnpquality()));
                this.tMasterOutputStream.writeShort(Short.parseShort(tRecordIn.getMappingquality()));
                this.tMasterOutputStream.writeInt(Integer.parseInt(tRecordIn.getReaddepth()));
                this.tMasterOutputStream.writeLong(iCurrentDetailPosition);

                this.tDetailOutputStream.write(tBArray);

                this.tDetailOutputStream.flush();
                this.tMasterOutputStream.flush();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } // end catch

    } // end writeToSNPVCFFormat

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
            BufferedWriter tTestModeWriterIn, VCFRecord tRecordIn) {
        this.tDetailOutputStream = tDetailOutputStreamIn;
        this.tMasterOutputStream = tMasterOutputStreamIn;
        this.tTestModeWriter = tTestModeWriterIn;

        try {

            // Write data to streams or writers.
            // Do we have a not-null Writer? Write to text file.
            if (this.tTestModeWriter != null && this.bIsTestMode) {

                this.tTestModeWriter.write(tRecordIn.getPosition());
                this.tTestModeWriter.write("\t");
                this.tTestModeWriter.write(tRecordIn.getGenotype());
                this.tTestModeWriter.write("\t");
                this.tTestModeWriter.write(tRecordIn.getConsensusquality());
                this.tTestModeWriter.write("\t");
                this.tTestModeWriter.write(tRecordIn.getSnpquality());
                this.tTestModeWriter.write("\t");
                this.tTestModeWriter.write(tRecordIn.getMappingquality());
                this.tTestModeWriter.write("\t");
                this.tTestModeWriter.write(tRecordIn.getReaddepth());
                this.tTestModeWriter.write("\t");
                this.tTestModeWriter.write("-1");
                this.tTestModeWriter.write(System.getProperty("line.separator"));

            } else {
                // Write to stream.
                this.tMasterOutputStream.flush();
                this.tMasterOutputStream.writeByte(this.getGenotype(tRecordIn.getGenotype()));
                this.tMasterOutputStream.writeInt(Integer.parseInt(tRecordIn.getConsensusquality()));
                this.tMasterOutputStream.writeInt(Integer.parseInt(tRecordIn.getSnpquality()));
                this.tMasterOutputStream.writeShort(Short.parseShort(tRecordIn.getMappingquality()));
                this.tMasterOutputStream.writeInt(Integer.parseInt(tRecordIn.getReaddepth()));
                // Write long file pointer to data in the data file. In other
                // words, no indel data associated with this position.
                this.tMasterOutputStream.writeLong(-1);

                this.tMasterOutputStream.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();

        } // end catch

    } // end writeToNoReferenceDataFormatVCF()

} // end BZip2ConversionFormat
