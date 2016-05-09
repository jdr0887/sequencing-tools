package org.renci.seqtools.converter;

import java.io.File;

import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMFileReader;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMSequenceRecord;
import htsjdk.samtools.ValidationStringency;

/**
 * VCFBAMQueryTool
 * <p>
 * A class that collects consensus file info (master file) from BAM files.
 * <p>
 * It looks in BAM files based on a line from a VCF file. It uses this VCF line (chromosome, start position, end
 * position, etc) to get consensus file data (SNP quality, Mapping quality, Read Quality, etc.)
 * 
 * @author k47k4705
 * 
 */
public class VCFBAMQueryTool {

    // Wrapped SAMFileReader/BAMFileReader
    private SAMFileReader tInputBAMReader;

    private String sChromosome;

    private String sStartPos;

    private String sEndPos;

    private SAMRecord tSAMRecord;

    private SAMRecordIterator tIterator;

    private SAMFileHeader tHeader;

    private SAMSequenceDictionary tDict;

    private SAMSequenceRecord tARecord;

    // Is the current BAM record sufficient to answer the VCF record's needs?
    private boolean bKeepCurrentRecord = false;

    // temp match/unmatch variables for counting matches.
    private int iMatched = 0;

    private int iUnmatched = 0;

    /**
     * VCFBAMQueryTool 4-arg private constructor
     */
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

    } // end VCFBAMQueryTool

    /**
     * VCFBAMQueryTool no-arg private constructor.
     */
    private VCFBAMQueryTool(File tInputBAMFileIn) {
        this.tInputBAMReader = new SAMFileReader(tInputBAMFileIn, true);
        this.tInputBAMReader.setValidationStringency(ValidationStringency.SILENT);
        this.tIterator = this.tInputBAMReader.iterator();
    }

    /**
     * getMatchUnmatchCount()
     * <p>
     * Return the matches/unmatches between VCF and BAM positions.
     */
    public String getMatchUnmatchCount() {
        return "Matched: " + this.iMatched + " Unmatched: " + this.iUnmatched + System.getProperty("line.separator");
    } // end getMatchUnmatchCount()

    /**
     * getInstance()
     * <p>
     * Returns an instance of this object.
     * <p>
     * No arg.
     * 
     * @return VCFBAMQueryTool -- an instance of this object.
     */
    public static VCFBAMQueryTool getInstance(File tInputBAMFileIn) {
        return new VCFBAMQueryTool(tInputBAMFileIn);
    } // end

    /**
     * getInstance()
     * <p>
     * Return an instance of this object.
     * 
     * @return VCFBAMQueryTool -- an instance of this object.
     */
    public static VCFBAMQueryTool getInstance(File tInputBAMFileIn, String sChromosomeIn, String sStartPosIn, String sEndPosIn) {
        return new VCFBAMQueryTool(tInputBAMFileIn, sChromosomeIn, sStartPosIn, sEndPosIn);
    } // end getInstance

    /**
     * setInputs()
     * <p>
     * Set up the internal fields of this object.
     */
    public void setInputs(String sChromosomeIn, String sStartPosIn) {
        this.sChromosome = sChromosomeIn;
        this.sStartPos = sStartPosIn;
        this.tHeader = this.tInputBAMReader.getFileHeader();
        this.tDict = this.tHeader.getSequenceDictionary();
        this.tARecord = this.tDict.getSequence(this.sChromosome);

    } // end setInput

    /**
     * getRecord()
     * <p>
     * Return a SAMRecord for the internal Iterator object.
     * 
     * @return SAMRecord -- the Record for the contained BAM reader iterator.
     */
    public SAMRecord getRecord() {

        if (this.tSAMRecord == null) {
            if (this.tIterator.hasNext()) {
                this.tSAMRecord = this.tIterator.next();
            } else {
                this.tIterator.close();
            }
        }

        int iStartPos = Integer.parseInt(this.sStartPos);
        // Are the VCF and BAM file records in the same chromosome and the VCF
        // position is between the BAM alignment start and end?
        if ((this.isSameChromosome(this.sChromosome, this.tSAMRecord.getReferenceName()))
                && (this.isVCFPosInSAMRecord(this.tSAMRecord, iStartPos))) {
            // Keep current record.
            this.bKeepCurrentRecord = true;
            this.iMatched++;
            // Same chrom and VCF pos is ahead of the SAM/BAM record?
        } else if ((this.isSameChromosome(this.sChromosome, this.tSAMRecord.getReferenceName()))
                && ((this.isVCFPosGTSAMRecord(this.tSAMRecord, iStartPos)))) {
            this.bKeepCurrentRecord = false;
            this.iUnmatched++;
            // Same chrom and VCF pos is behind the SAM/BAM record?
        } else if ((this.isSameChromosome(this.sChromosome, this.tSAMRecord.getReferenceName()))
                && ((this.isVCFPosLTSAMRecord(this.tSAMRecord, iStartPos)))) {
            this.bKeepCurrentRecord = true;
            this.iUnmatched++;
            // The VCF and BAM records are not even in the same chromosome.
        } else if (!this.isSameChromosome(this.sChromosome, this.tSAMRecord.getReferenceName())) {
            this.bKeepCurrentRecord = false;
            this.iUnmatched++;
        } // end else

        if (this.keepCurrentRecord() && this.tSAMRecord != null) {
            return this.tSAMRecord;
        } else {
            if (this.tIterator.hasNext()) {
                this.tSAMRecord = this.tIterator.next();
                return this.tSAMRecord;
            } else {
                this.tIterator.close();
                // this.tSAMRecord = null;
                return this.tSAMRecord;
            }
        }

    } // end getRecord

    /**
     * getReadBases()
     * <p>
     * Return read bases of the contained current SAMRecord.
     * 
     * @return String -- the read bases (byte array) as a String.
     */
    public String getReadBases() {
        String sReadBases = null;
        if (this.tSAMRecord != null) {
            sReadBases = new String(this.getRecord().getReadBases());
        } else {
            sReadBases = null;
        }

        return sReadBases != null ? sReadBases : "0";
    } // end getReadBases()

    /**
     * getReadQualityScores()
     * <p>
     * Return read quality scores of contained current SAMRecord.
     * 
     * @return String -- the base quality scores (byte array) as a String.
     */
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
    } // end getReadQualityScores

    /**
     * getMappingQuality()
     * <p>
     * Return mapping quality for the contained current SAMRecord.
     * 
     * @return short -- the mapping quality for this SAMRecord as a short.
     */
    public short getMappingQuality() {
        short tMappingQuality = 0;

        if (this.tSAMRecord != null) {
            tMappingQuality = (short) this.getRecord().getMappingQuality();

        } else {

            tMappingQuality = 0;
        }

        return tMappingQuality != 0 ? tMappingQuality : 0;
    } // end getMappingQuality

    /**
     * getVCFRecord()
     * <p>
     * Return a VCFRecord using the BAM file data at this object's reference position.
     * 
     * @return
     */
    public VCFRecord getVCFRecord() {
        VCFRecord tRecord = null;
        return tRecord;
    } // end getVCFRecord

    /**
     * isVCFPosInSAMRecord()
     * <p>
     * Is the input iVCFPositionIn between the SAMRecord's alignment start and end?
     * 
     * @param tRecordIn
     *            -- the SAMRecord to test.
     * @param iVCFPositionIn
     *            -- the int VCF position in a line of VCF data.
     * @return boolean -- true, within SAMRecord alignment, false, not in SAMRecord alignment.
     */
    public boolean isVCFPosInSAMRecord(SAMRecord tRecordIn, int iVCFPositionIn) {
        boolean bIsInRange = false;

        // Is the VCFPosition between the SAMRecords alignment start and end?
        if ((tRecordIn.getAlignmentStart() <= iVCFPositionIn) && (iVCFPositionIn <= tRecordIn.getAlignmentEnd())) {
            bIsInRange = true;
        }

        return bIsInRange;
    } // end isVCFPosInSAMRecord()

    /**
     * isVCFPosLTSAMRecord()
     * <p>
     * Is the VCF position less than the SAMRecord alignment start or end?
     * 
     * @return boolean, true is <, false is not <
     */
    public boolean isVCFPosLTSAMRecord(SAMRecord tRecordIn, int iVCFPositionIn) {
        boolean bIsLTRecord = false;

        if (iVCFPositionIn < tRecordIn.getAlignmentStart()) { // Need new
                                                              // SAMRecord.
            bIsLTRecord = true;
        } // end else

        return bIsLTRecord;

    } // end isVCFPosLTSAMRecord()

    /**
     * isVCFPosGTSAMRecord()
     * <p>
     * Is the VCF position greater than the SAMRecord alignment start or end?
     * 
     * @return boolean, true is >, false is not >
     */
    public boolean isVCFPosGTSAMRecord(SAMRecord tRecordIn, int iVCFPositionIn) {
        boolean bIsGTRecord = false;

        if (iVCFPositionIn > tRecordIn.getAlignmentEnd()) { // Need new
                                                            // SAMRecord.
            bIsGTRecord = true;
        } // end else

        return bIsGTRecord;

    } // end isVCFPosGTSAMRecord()

    /**
     * isSameChromosome()
     * <p>
     * Do the VCF and BAM records have the same chromosome?
     */
    public boolean isSameChromosome(String sVCFChromIn, String sBAMChromIn) {
        boolean bIsSameChrom = false;
        if (sVCFChromIn.equalsIgnoreCase(sBAMChromIn)) {
            bIsSameChrom = true;
        }
        return bIsSameChrom;
    } // end isSameChromosome

    /**
     * keepCurrentRecord()
     * <p>
     * Do we need to use the current record?
     */
    public boolean keepCurrentRecord() {
        return this.bKeepCurrentRecord;
    } // end keepCurrentRecord

} // end VCFBAMQueryTool
