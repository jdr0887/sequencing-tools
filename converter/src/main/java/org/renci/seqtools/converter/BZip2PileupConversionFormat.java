package org.renci.seqtools.converter;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.util.List;

import org.renci.seqtools.converter.genotypemap.GenotypeMap;

public class BZip2PileupConversionFormat implements IConversionFormat {

    private List<String> tPileupLineList;

    private DataOutputStream tMasterOutputStream;

    private DataOutputStream tDetailOutputStream;

    private BufferedWriter tTextModeWriter;

    private List<String> tIndelDataList;

    private StringBuffer tIndelBuffer = new StringBuffer();

    private GenotypeMap tGMap;

    BZip2PileupConversionFormat() {
        this.tGMap = GenotypeMap.getInstance();

    }

    public byte getGenotype() throws Exception {

        byte tByteToReturn = 00000000;
        String sGenotype = this.tPileupLineList.get(ConverterConstants.GENOTYPE_POSITION);

        if (this.tGMap.containsKey(sGenotype)) {
            Byte tByte = (Byte) this.tGMap.get(sGenotype);
            tByteToReturn = tByte.byteValue();
        }

        return tByteToReturn;
    }

    public int getConsensusQuality() {
        String sCQ = this.tPileupLineList.get(ConverterConstants.CONSENSUS_QUALITY_POSITION);
        return Integer.parseInt(sCQ);
    }

    public int getSNPQuality() {
        String sSNPQ = this.tPileupLineList.get(ConverterConstants.SNP_QUALITY_POSITION);
        return Integer.parseInt(sSNPQ);
    }

    public short getMappingQuality() {
        String sMQ = this.tPileupLineList.get(ConverterConstants.MAPPING_QUALITY_POSITION);
        return Short.parseShort(sMQ);
    }

    public int getReadDepth() {
        String sRD = this.tPileupLineList.get(ConverterConstants.READ_DEPTH_POSITION);

        return Integer.parseInt(sRD);

    }

    public String getReadBases() {
        String sRB = this.tPileupLineList.get(ConverterConstants.READ_BASES_POSITION);
        return sRB;
    }

    public String getReadQualityScores() {
        String sRQS = this.tPileupLineList.get(ConverterConstants.READ_QUALITY_POSITION);
        return sRQS;
    }

    public long getCoordinatePosition() {
        String sRC = this.tPileupLineList.get(ConverterConstants.REFERENCE_COORDINATE_POSITION);
        return Long.parseLong(sRC);
    }

    @Override
    public int getMasterPosition() {
        return this.tMasterOutputStream.size();
    }

    @Override
    public int getDetailPosition() {
        return this.tDetailOutputStream.size();
    }

    @Override
    public void writeToGenomicFormatVCF(DataOutputStream tDetailOutputStreamIn, DataOutputStream tMasterOutputStream,
            BufferedWriter tBufferedWriterIn, VCFRecord tRecordIn) {

    }

    @Override
    public void writeToIndelFormatVCF(DataOutputStream tDetailOutputStreamIn, DataOutputStream tMasterOutputStream,
            BufferedWriter tBufferedWriterIn, VCFRecord tRecordIn) {

    }

    @Override
    public void writeToSNPFormatVCF(DataOutputStream tDetailOutputStreamIn, DataOutputStream tMasterOutputStream,
            BufferedWriter tBufferedWriterIn, VCFRecord tRecordIn) {

    }

    @Override
    public void writeToNoReferenceDataFormatVCF(DataOutputStream tDetailOutputStreamIn, DataOutputStream tMasterOutputStream,
            BufferedWriter tTestWriter, VCFRecord tRecordIn) {

    }

}
