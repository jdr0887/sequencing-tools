package org.renci.seqtools.converter;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import org.renci.seqtools.converter.genotypemap.GenotypeMap;

public class BZip2VCFExomeConversionFormat implements IConversionFormat {

    private DataOutputStream tMasterOutputStream;

    private DataOutputStream tDetailOutputStream;

    private BufferedWriter tTextModeWriter;

    private List<String> tVCFLineList;

    private GenotypeMap tGMap;

    public BZip2VCFExomeConversionFormat() {
        this.tGMap = GenotypeMap.getInstance();
    }

    @Override
    public void writeToGenomicFormatVCF(DataOutputStream tDetailOutputStreamIn, DataOutputStream tMasterOutputStreamIn,
            BufferedWriter tTextWriterIn, VCFRecord tRecordIn) {

        this.tDetailOutputStream = tDetailOutputStreamIn;
        this.tMasterOutputStream = tMasterOutputStreamIn;
        this.tTextModeWriter = tTextWriterIn;

        try {
            this.tMasterOutputStream.flush();

            this.tMasterOutputStream.writeLong(Long.parseLong(tRecordIn.getPosition()));
            this.tMasterOutputStream.writeByte(this.getGenotype(tRecordIn.getGenotype()));
            this.tMasterOutputStream.writeInt(Integer.parseInt(tRecordIn.getConsensusquality()));
            this.tMasterOutputStream.writeInt(Integer.parseInt(tRecordIn.getSnpquality()));
            this.tMasterOutputStream.writeShort(Short.parseShort(tRecordIn.getMappingquality()));
            this.tMasterOutputStream.writeInt(Integer.parseInt(tRecordIn.getReaddepth()));

            this.tMasterOutputStream.writeLong(-1);

            this.tMasterOutputStream.flush();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void writeToIndelFormatVCF(DataOutputStream tDetailOutputStreamIn, DataOutputStream tMasterOutputStream,
            BufferedWriter tTextModeWriterIn, VCFRecord tRecordIn) {

        this.tDetailOutputStream = tDetailOutputStreamIn;
        this.tMasterOutputStream = (DataOutputStream) tMasterOutputStream;
        this.tTextModeWriter = tTextModeWriterIn;

        try {
            this.tMasterOutputStream.flush();
            this.tDetailOutputStream.flush();

            int iCurrentDetailPosition = this.tDetailOutputStream.size();

            String tBasesAndQualityScoresAndIndelData = tRecordIn.getReadbases() + "\t" + tRecordIn.getReadqualityScores() + "|";

            ByteBuffer tBuffer = null;
            tBuffer = ByteBuffer.wrap(tBasesAndQualityScoresAndIndelData.getBytes());
            byte[] tBArray = new byte[tBuffer.remaining()];

            tBuffer.get(tBArray);

            this.tMasterOutputStream.writeLong(Long.parseLong(tRecordIn.getPosition()));

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
        }

    }

    @Override
    public void writeToSNPFormatVCF(DataOutputStream tDetailOutputStreamIn, DataOutputStream tMasterOutputStream,
            BufferedWriter tTextModeWriterIn, VCFRecord tRecordIn) {

        this.tDetailOutputStream = tDetailOutputStreamIn;
        this.tMasterOutputStream = (DataOutputStream) tMasterOutputStream;
        this.tTextModeWriter = tTextModeWriterIn;

        try {
            this.tMasterOutputStream.flush();
            this.tDetailOutputStream.flush();

            int iCurrentDetailPosition = this.tDetailOutputStream.size();

            String tBasesAndQualityScoresAndIndelData = tRecordIn.getReferenceGenotype() + "\t" + tRecordIn.getReadbases() + "\t"
                    + tRecordIn.getReadqualityScores() + "|";

            ByteBuffer tBuffer = null;
            tBuffer = ByteBuffer.wrap(tBasesAndQualityScoresAndIndelData.getBytes());
            byte[] tBArray = new byte[tBuffer.remaining()];

            tBuffer.get(tBArray);

            this.tMasterOutputStream.writeLong(Long.parseLong(tRecordIn.getPosition()));

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
            BufferedWriter tTextWriterIn, VCFRecord tRecordIn) {

        this.tDetailOutputStream = tDetailOutputStreamIn;
        this.tMasterOutputStream = tMasterOutputStreamIn;
        this.tTextModeWriter = tTextWriterIn;

        try {
            this.tMasterOutputStream.flush();

            this.tMasterOutputStream.writeLong(Long.parseLong(tRecordIn.getPosition()));
            this.tMasterOutputStream.writeByte(this.getGenotype(tRecordIn.getGenotype()));
            this.tMasterOutputStream.writeInt(Integer.parseInt(tRecordIn.getConsensusquality()));
            this.tMasterOutputStream.writeInt(Integer.parseInt(tRecordIn.getSnpquality()));
            this.tMasterOutputStream.writeShort(Short.parseShort(tRecordIn.getMappingquality()));
            this.tMasterOutputStream.writeInt(Integer.parseInt(tRecordIn.getReaddepth()));

            this.tMasterOutputStream.writeLong(-1);

            this.tMasterOutputStream.flush();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
