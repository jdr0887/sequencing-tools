package org.renci.seqtools.converter;

import java.io.File;

import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMFileReader;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.ValidationStringency;

public class VCFBAMQueryTool {

    private SAMFileReader tInputBAMReader;

    private String sChromosome;

    private String sStartPos;

    private String sEndPos;

    private SAMRecord tSAMRecord;

    private SAMRecordIterator tIterator;

    private SAMFileHeader tHeader;

    private SAMSequenceDictionary tDict;

    private SAMSequenceRecord tARecord;

    private boolean bKeepCurrentRecord = false;

    private int iMatched = 0;

    private int iUnmatched = 0;

    private VCFBAMQueryTool(File tInputBAMFileIn, String sChromosomeIn, String sStartPosIn, String sEndPosIn) {
        this.tInputBAMReader = new SAMFileReader(tInputBAMFileIn, true);
        this.tInputBAMReader.setValidationStringency(ValidationStringency.SILENT);
        this.sChromosome = sChromosomeIn;
        this.sStartPos = sStartPosIn;
        this.sEndPos = sEndPosIn;
        this.tHeader = this.tInputBAMReader.getFileHeader();

        this.tDict = this.tHeader.getSequenceDictionary();
        this.tARecord = this.tDict.getSequence(this.sChromosome);

        this.tIterator = this.tInputBAMReader.iterator();

    }

    private VCFBAMQueryTool(File tInputBAMFileIn) {
        this.tInputBAMReader = new SAMFileReader(tInputBAMFileIn, true);
        this.tInputBAMReader.setValidationStringency(ValidationStringency.SILENT);
        this.tIterator = this.tInputBAMReader.iterator();
    }

    public String getMatchUnmatchCount() {
        return "Matched: " + this.iMatched + " Unmatched: " + this.iUnmatched + System.getProperty("line.separator");
    }

    public static VCFBAMQueryTool getInstance(File tInputBAMFileIn) {
        return new VCFBAMQueryTool(tInputBAMFileIn);
    }

    public static VCFBAMQueryTool getInstance(File tInputBAMFileIn, String sChromosomeIn, String sStartPosIn, String sEndPosIn) {
        return new VCFBAMQueryTool(tInputBAMFileIn, sChromosomeIn, sStartPosIn, sEndPosIn);
    }

    public void setInputs(String sChromosomeIn, String sStartPosIn) {
        this.sChromosome = sChromosomeIn;
        this.sStartPos = sStartPosIn;
        this.tHeader = this.tInputBAMReader.getFileHeader();
        this.tDict = this.tHeader.getSequenceDictionary();
        this.tARecord = this.tDict.getSequence(this.sChromosome);

    }

    public SAMRecord getRecord() {

        if (this.tSAMRecord == null) {
            if (this.tIterator.hasNext()) {
                this.tSAMRecord = this.tIterator.next();
            } else {
                this.tIterator.close();
            }
        }

        int iStartPos = Integer.parseInt(this.sStartPos);

        if ((this.isSameChromosome(this.sChromosome, this.tSAMRecord.getReferenceName()))
                && (this.isVCFPosInSAMRecord(this.tSAMRecord, iStartPos))) {

            this.bKeepCurrentRecord = true;
            this.iMatched++;

        } else if ((this.isSameChromosome(this.sChromosome, this.tSAMRecord.getReferenceName()))
                && ((this.isVCFPosGTSAMRecord(this.tSAMRecord, iStartPos)))) {
            this.bKeepCurrentRecord = false;
            this.iUnmatched++;

        } else if ((this.isSameChromosome(this.sChromosome, this.tSAMRecord.getReferenceName()))
                && ((this.isVCFPosLTSAMRecord(this.tSAMRecord, iStartPos)))) {
            this.bKeepCurrentRecord = true;
            this.iUnmatched++;

        } else if (!this.isSameChromosome(this.sChromosome, this.tSAMRecord.getReferenceName())) {
            this.bKeepCurrentRecord = false;
            this.iUnmatched++;
        }

        if (this.keepCurrentRecord() && this.tSAMRecord != null) {
            return this.tSAMRecord;
        } else {
            if (this.tIterator.hasNext()) {
                this.tSAMRecord = this.tIterator.next();
                return this.tSAMRecord;
            } else {
                this.tIterator.close();

                return this.tSAMRecord;
            }
        }

    }

    public String getReadBases() {
        String sReadBases = null;
        if (this.tSAMRecord != null) {
            sReadBases = new String(this.getRecord().getReadBases());
        } else {
            sReadBases = null;
        }

        return sReadBases != null ? sReadBases : "0";
    }

    public String getReadQualityScores() {

        String sRQScore = null;
        if (this.tSAMRecord != null) {
            try {

                sRQScore = this.getRecord().getBaseQualityString();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {

            sRQScore = null;
        }

        return sRQScore != null ? sRQScore : "0";
    }

    public short getMappingQuality() {
        short tMappingQuality = 0;

        if (this.tSAMRecord != null) {
            tMappingQuality = (short) this.getRecord().getMappingQuality();

        } else {

            tMappingQuality = 0;
        }

        return tMappingQuality != 0 ? tMappingQuality : 0;
    }

    public VCFRecord getVCFRecord() {
        VCFRecord tRecord = null;
        return tRecord;
    }

    public boolean isVCFPosInSAMRecord(SAMRecord tRecordIn, int iVCFPositionIn) {
        boolean bIsInRange = false;

        if ((tRecordIn.getAlignmentStart() <= iVCFPositionIn) && (iVCFPositionIn <= tRecordIn.getAlignmentEnd())) {
            bIsInRange = true;
        }

        return bIsInRange;
    }

    public boolean isVCFPosLTSAMRecord(SAMRecord tRecordIn, int iVCFPositionIn) {
        boolean bIsLTRecord = false;

        if (iVCFPositionIn < tRecordIn.getAlignmentStart()) {

            bIsLTRecord = true;
        }

        return bIsLTRecord;

    }

    public boolean isVCFPosGTSAMRecord(SAMRecord tRecordIn, int iVCFPositionIn) {
        boolean bIsGTRecord = false;

        if (iVCFPositionIn > tRecordIn.getAlignmentEnd()) {

            bIsGTRecord = true;
        }

        return bIsGTRecord;

    }

    public boolean isSameChromosome(String sVCFChromIn, String sBAMChromIn) {
        boolean bIsSameChrom = false;
        if (sVCFChromIn.equalsIgnoreCase(sBAMChromIn)) {
            bIsSameChrom = true;
        }
        return bIsSameChrom;
    }

    public boolean keepCurrentRecord() {
        return this.bKeepCurrentRecord;
    }

}
