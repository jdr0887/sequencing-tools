package org.renci.seqtools.converter;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.renci.seqtools.converter.genotypemap.GenotypeMap;

public class BZip2VCFConversionFormat implements IConversionFormat {

    private DataOutputStream tMasterOutputStream;

    private DataOutputStream tDetailOutputStream;

    private BufferedWriter tTestModeWriter;

    private boolean bIsTestMode;

    private static final String FORWARD_STRAND_LABEL = "F";

    private static final String REVERSE_STRAND_LABEL = "R";

    private GenotypeMap tGMap;

    public BZip2VCFConversionFormat(boolean bIsTestModeIn) {

        this.tGMap = GenotypeMap.getInstance();
        this.bIsTestMode = bIsTestModeIn;

    }

    @Override
    public void writeToGenomicFormatVCF(DataOutputStream tDetailOutputStreamIn, DataOutputStream tMasterOutputStreamIn,
            BufferedWriter tTestModeWriterIn, VCFRecord tRecordIn) {

        this.tDetailOutputStream = tDetailOutputStreamIn;
        this.tMasterOutputStream = tMasterOutputStreamIn;
        this.tTestModeWriter = tTestModeWriterIn;

        try {

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

                this.tMasterOutputStream.flush();
                this.tMasterOutputStream.writeByte(this.getGenotype(tRecordIn.getGenotype()));
                this.tMasterOutputStream.writeInt(Integer.parseInt(tRecordIn.getConsensusquality()));
                this.tMasterOutputStream.writeInt(Integer.parseInt(tRecordIn.getSnpquality()));
                this.tMasterOutputStream.writeShort(Short.parseShort(tRecordIn.getMappingquality()));
                this.tMasterOutputStream.writeInt(Integer.parseInt(tRecordIn.getReaddepth()));

                this.tMasterOutputStream.writeLong(-1);

                this.tMasterOutputStream.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    @Override
    public void writeToIndelFormatVCF(DataOutputStream tDetailOutputStreamIn, DataOutputStream tMasterOutputStream,
            BufferedWriter tTestModeWriterIn, VCFRecord tRecordIn) {

        this.tDetailOutputStream = tDetailOutputStreamIn;
        this.tMasterOutputStream = (DataOutputStream) tMasterOutputStream;
        this.tTestModeWriter = tTestModeWriterIn;

        try {
            this.tMasterOutputStream.flush();
            this.tDetailOutputStream.flush();

            String sStrand = tRecordIn.isReverseStrand() ? BZip2VCFConversionFormat.REVERSE_STRAND_LABEL
                    : BZip2VCFConversionFormat.FORWARD_STRAND_LABEL;

            int iCurrentDetailPosition = this.tDetailOutputStream.size();

            String tBasesAndQualityScoresAndIndelData = sStrand + "\t" + tRecordIn.getReadbases() + "\t" + tRecordIn.getReadqualityScores()
                    + "|";

            ByteBuffer tBuffer = null;
            tBuffer = ByteBuffer.wrap(tBasesAndQualityScoresAndIndelData.getBytes());
            byte[] tBArray = new byte[tBuffer.remaining()];

            tBuffer.get(tBArray);

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
        }

    }

    @Override
    public void writeToSNPFormatVCF(DataOutputStream tDetailOutputStreamIn, DataOutputStream tMasterOutputStream,
            BufferedWriter tTestModeWriterIn, VCFRecord tRecordIn) {

        this.tDetailOutputStream = tDetailOutputStreamIn;
        this.tMasterOutputStream = (DataOutputStream) tMasterOutputStream;
        this.tTestModeWriter = tTestModeWriterIn;

        try {
            this.tMasterOutputStream.flush();
            this.tDetailOutputStream.flush();

            String sStrand = tRecordIn.isReverseStrand() ? BZip2VCFConversionFormat.REVERSE_STRAND_LABEL
                    : BZip2VCFConversionFormat.FORWARD_STRAND_LABEL;

            int iCurrentDetailPosition = this.tDetailOutputStream.size();

            String tBasesAndQualityScoresAndIndelData = sStrand + "\t" + tRecordIn.getReferenceGenotype() + "\t" + tRecordIn.getReadbases()
                    + "\t" + tRecordIn.getReadqualityScores() + "|";

            ByteBuffer tBuffer = null;
            tBuffer = ByteBuffer.wrap(tBasesAndQualityScoresAndIndelData.getBytes());
            byte[] tBArray = new byte[tBuffer.remaining()];

            tBuffer.get(tBArray);

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
        }

    }

    @Override
    public int getMasterPosition() {
        return this.tMasterOutputStream.size();
    }

    @Override
    public int getDetailPosition() {
        return this.tDetailOutputStream.size();
    }

    public byte getGenotype(String sGenotypeIn) throws Exception {

        byte tByteToReturn = 00000000;

        if (this.tGMap.containsKey(sGenotypeIn)) {
            Byte tByte = (Byte) this.tGMap.get(sGenotypeIn);
            tByteToReturn = tByte.byteValue();
        }

        return tByteToReturn;
    }

    @Override
    public void writeToNoReferenceDataFormatVCF(DataOutputStream tDetailOutputStreamIn, DataOutputStream tMasterOutputStreamIn,
            BufferedWriter tTestModeWriterIn, VCFRecord tRecordIn) {
        this.tDetailOutputStream = tDetailOutputStreamIn;
        this.tMasterOutputStream = tMasterOutputStreamIn;
        this.tTestModeWriter = tTestModeWriterIn;

        try {

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

                this.tMasterOutputStream.flush();
                this.tMasterOutputStream.writeByte(this.getGenotype(tRecordIn.getGenotype()));
                this.tMasterOutputStream.writeInt(Integer.parseInt(tRecordIn.getConsensusquality()));
                this.tMasterOutputStream.writeInt(Integer.parseInt(tRecordIn.getSnpquality()));
                this.tMasterOutputStream.writeShort(Short.parseShort(tRecordIn.getMappingquality()));
                this.tMasterOutputStream.writeInt(Integer.parseInt(tRecordIn.getReaddepth()));

                this.tMasterOutputStream.writeLong(-1);

                this.tMasterOutputStream.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();

        }

    }

}
