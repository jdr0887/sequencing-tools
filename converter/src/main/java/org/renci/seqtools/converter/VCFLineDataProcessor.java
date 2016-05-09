package org.renci.seqtools.converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * VCFLineDataProcessor
 * <p>
 * Contains a line of data for each individual in a VCF file.
 * <p>
 * VCFLineDataProcessor maintains a Map of key/value pairs for a line of data for each individual in the VCF file. Each
 * individual in a VCF file is represented by an Integer in the map, the key. The value in the Map is the line of
 * consensus data derived from the VCF file data for that individual in that line of VCF data.
 * <p>
 * VCFLineDataProcessor teases out the VCF individual data and associates it with one individual.
 * <p>
 * So, if a VCF file contains five individuals, the Map wrapped by VCFLineDataProcessor will contain five entries. Each
 * entry will contain data for that individual. Each data entry will be written to a consensus file pair for that
 * individual (master/detail bzip2 files):
 * <p>
 * These five Map entries would generate writes to five separate pairs of master/detail files for each individual.
 * <p>
 * File names are based on the column names for each individual in the VCF file.
 * <p>
 * master-NA**0.dat.bz2, detail-NA**0.dat.bz2, master-NA**1.dat.bz2, detail-NA**1.dat.bz2, master-NA**2.dat.bz2,
 * detail-NA**2.dat.bz2, master-NA**3.dat.bz2, detail-NA**3.dat.bz2, master-NA**4.dat.bz2, detail-NA**4.dat.bz2.
 * <p>
 * 
 * @author k47k4705
 * 
 */
public class VCFLineDataProcessor {

    // The ALT and REF and FORMAT and POSITION column positions in a line of VCF
    // data.
    public static final int INT_ALT_COLUMN_POSITION = 4;

    public static final int INT_REF_COLUMN_POSITION = 3;

    public static final int INT_FORMAT_COLUMN_POSITION = 8;

    public static final int INT_POSITION_COLUMN_POSITION = 1;

    public static final int INT_QUAL_COLUMN_POSITION = 5;

    public static final int INT_VCF_DATA_COLUMN_POSITION = 9;

    // The various format elements regarded as "present" by a boolean.
    public static boolean bHasGenotype = false; // genotype

    public static boolean bHasSNPQualityScore = false; // snp quality

    public static boolean bHasReadDepth = false; // read depth

    public static boolean bHasConsensusQuailty = false; // consensus quality

    // Do we have a ./. as our sample column?
    private static boolean bHasDotSlashDotSampleData = false;

    // Indices into the FORMAT column to find various elements.
    public static int iGenotypeIndex = 0;

    public static int iSNPQualityScoreIndex = 0;

    public static int iReadDepthIndex = 0;

    public static int iConsensusQualityIndex = 0;

    // Various elements of the FORMAT column as strings.
    private static final String FORMAT_GT = "GT";

    private static final String FORMAT_QUAL = "QUAL";

    private static final String FORMAT_READ_DEPTH = "DP";

    private static final String FORMAT_CONSENSUS_QUALITY = "GQ";

    private static final int INDEX_REFERENCE = 0;

    // Strings for marking discovered indel alleles.
    private static final String INSERTION_FLAG = "+/";

    private static final String DELETION_FLAG = "-/";

    private static final String TAB = "\t";

    private static String GENOTYPE = "Z";

    private List<String> tBaseDataList = null;

    private int iFilePairNumber = 0;

    // VCFBamQueryTool.
    private VCFBAMQueryTool tBAMTool;

    // VCF data manager.
    private VCFDataManager tDataManager;

    // An array containing the ref and alt values for this line of data.
    private String[] sArrayOfRefAndAltData;

    /**
     * VCFLineDataProcessor no-arg constructor()
     */
    private VCFLineDataProcessor() {
    }

    /**
     * VCFLineDataProcessor()
     * <p>
     * VCFLineDataProcessor public arg constructor
     * 
     * @param sPathToBAMFile2
     * @throws Exception
     */
    private VCFLineDataProcessor(List<String> tOutputList, int iNumberOfFilePairs, VCFBAMQueryTool tBAMTool, String sStartPosition,
            String sEndPosition) throws Exception {
        this.tBaseDataList = tOutputList;
        this.iFilePairNumber = iNumberOfFilePairs;
        this.tBAMTool = tBAMTool;
        this.sArrayOfRefAndAltData = this.makeRefAndAltStringArray(this.tBaseDataList);
        this.tDataManager = VCFDataManager.getInstance();
    } // end VCFLineDataProcessor

    /**
     * getInstance()
     * <p>
     * Returns an instance of a VCFLineDataProcessor
     * 
     * @param tOutputList
     * @param iNumberOfFilePairs
     * @param sPathToBAMFile2
     * @return VCFLineDataProcessor
     * @throws Exception
     */
    public static VCFLineDataProcessor getInstance(List<String> tOutputList, int iNumberOfFilePairs, VCFBAMQueryTool tBAMTool,
            String sStartPosition, String sEndPosition) throws Exception {
        return new VCFLineDataProcessor(tOutputList, iNumberOfFilePairs, tBAMTool, sStartPosition, sEndPosition);
    } // end getInstance

    /**
     * getInstance() no arg constructor
     * <p>
     * Returns a plain vanilla instance of a VCFLineDataProcessor.
     */
    public static VCFLineDataProcessor getInstance() {
        return new VCFLineDataProcessor();
    } // end

    /**
     * setInputs()
     * <p>
     * Set internal state of this object with the given inputs.
     */
    public void setInputs(List<String> tOutputList, int iNumberOfFilePairs, VCFBAMQueryTool tBAMTool, String sStartPosition,
            String sEndPosition) throws Exception {

        this.tBaseDataList = null;
        this.iFilePairNumber = 0;
        // VCFBamQueryTool.
        this.tBAMTool = null;
        // VCF data manager.
        this.tDataManager = null;

        // An array containing the ref and alt values for this line of data.
        this.sArrayOfRefAndAltData = null;

        VCFLineDataProcessor.bHasGenotype = false; // genotype
        VCFLineDataProcessor.bHasSNPQualityScore = false; // snp quality
        VCFLineDataProcessor.bHasReadDepth = false; // read depth
        VCFLineDataProcessor.bHasConsensusQuailty = false; // consensus quality

        // Do we have a ./. as our sample column?
        VCFLineDataProcessor.bHasDotSlashDotSampleData = false;

        // Indices into the FORMAT column to find various elements.
        VCFLineDataProcessor.iGenotypeIndex = 0;
        VCFLineDataProcessor.iSNPQualityScoreIndex = 0;
        VCFLineDataProcessor.iReadDepthIndex = 0;
        VCFLineDataProcessor.iConsensusQualityIndex = 0;

        this.tBaseDataList = tOutputList;
        this.iFilePairNumber = iNumberOfFilePairs;
        this.tBAMTool = tBAMTool;
        this.sArrayOfRefAndAltData = this.makeRefAndAltStringArray(this.tBaseDataList);
        this.tDataManager = VCFDataManager.getInstance();

    } // end setInputs

    /**
     * loadParsedVCFDataIntoMap()
     * <p>
     * Loads a HashMap with a value (a line of constructed consensus data) for each file pair. Keys are the number of
     * the individual column of data in the VCF file (NA**0)\t(NA**1)\t(NA**2) etc. Values are the data to write to the
     * consensus file for this individual.
     * 
     * @param tOutputList
     * @param iNumberOfFilePairs
     */
    public void loadParsedVCFDataIntoMap() throws Exception {
        // Get Record for BAM. Lets the BAMTool get the next SAMRecord off the
        // pile.
        this.tBAMTool.getRecord();
        // Loop through number of file pairs: the master/detail consensus file
        // pairs for every individual or sample column of data in the VCF file.
        for (int ii = 0; ii < this.iFilePairNumber; ii++) {

            // Parse format column. Set global vars for this line of data
            // (VCFLineDataProcessor) to match format column for this line.
            this.parseFormatColumn(this.tBaseDataList, ii);

            // Figure out and process the line: deletions, insertions, no
            // reference data, large structural variants, snps.
            if (this.hasWeirdData(this.tBaseDataList, ii)) {
                throw new Exception("Weird, mismatched, or nonsensical data (maybe monomorphic no-alt allele yet zygosity of 0/1 or 1/0?): "
                        + Arrays.toString(this.tBaseDataList.toArray()));
            } else if (this.hasNoReferenceData(this.tBaseDataList, ii)) {
                this.processNoReferenceData(this.tBaseDataList, ii);
            } else if (this.hasDeletion(this.tBaseDataList, ii)) {
                this.processDeletion(this.tBaseDataList, ii);
            } else if (this.hasNoCall(this.tBaseDataList, ii)) {
                this.processNoCall(this.tBaseDataList, ii);
            } else if (this.hasInsertion(this.tBaseDataList, ii)) {
                this.processInsertion(this.tBaseDataList, ii);
            } else if (this.hasMonomorphicReference(this.tBaseDataList, ii)) {
                this.processMonomorphicReference(this.tBaseDataList, ii);
            } else if (this.hasLargeStructuralVariant(this.tBaseDataList, ii)) {
                this.processLargeStructuralVariant(this.tBaseDataList, ii);
            } else if (this.hasSingleHeterozygousSNP(this.tBaseDataList, ii)) {
                this.processSingleHeterozygousSNP(this.tBaseDataList, ii);
            } else if (this.hasSingleHomozygousAltSNP(this.tBaseDataList, ii)) {
                this.processSingleHomozygousAltSNP(this.tBaseDataList, ii);
            } else {
                this.processSameAltAndRef(this.tBaseDataList, ii);
                // Make VCFRecord data for each individual in a VCF file. Add
                // record to the data manager.
                this.makeConsensusDataList(ii, this.tBaseDataList);
            }
        } // end for

    } // end loadParsedVCFDataIntoMap

    /**
     * processNoReferenceData()
     * <p>
     * Process the case where we have no reference data. The reference data is missing and is represented by a ".".
     * 
     * @param tBaseDataList2
     * @param ii
     */
    private void processNoReferenceData(List<String> tBaseDataList2, int iColumnIdIn) {
        // Position information.
        String sPosition = tBaseDataList2.get(VCFLineDataProcessor.INT_POSITION_COLUMN_POSITION);

        boolean bHasIndel = false;
        boolean bHasSNP = false;
        boolean bIsReverseStrand = this.tBAMTool.getRecord().getReadNegativeStrandFlag();
        boolean bIsNoCall = false;
        boolean bHasNoReferenceData = true;

        String sTotalGenotype = "null";
        // Get reference genotype.
        String sRefGenotype = tBaseDataList2.get(INT_REF_COLUMN_POSITION);

        VCFRecord tRecord = VCFRecord.getInstance(iColumnIdIn, sPosition, sTotalGenotype, sRefGenotype,
                this.getConsensusQualityScore(iColumnIdIn), this.getSNPQualityScore(iColumnIdIn),
                Short.toString(this.tBAMTool.getMappingQuality()), this.getReadDepth(iColumnIdIn), this.tBAMTool.getReadQualityScores(),
                this.tBAMTool.getReadBases(), bHasIndel, bHasSNP, bIsReverseStrand, bIsNoCall, bHasNoReferenceData);

        this.tDataManager.addRecord(tRecord, iColumnIdIn);

    } // end processNoReferenceData

    /**
     * hasNoReferenceData()
     * <p>
     * If the reference data is a ".", (we have no reference data), we want to write it out.
     */
    public boolean hasNoReferenceData(List<String> tBaseDataList2, int iSampleColumnIn) throws Exception {
        boolean bHasNoReferenceData = false;

        // Are the reference and alt values in the sample column the same?
        // Get the ALT column from the input list.
        String sRefValue = tBaseDataList2.get(this.INT_REF_COLUMN_POSITION);

        if (sRefValue.equalsIgnoreCase(".")) {

            bHasNoReferenceData = true;
        } // end if

        return bHasNoReferenceData;
    } // end hasNoReferenceData

    /**
     * hasWeirdData()
     * <p>
     * Try to weed out weird data so we don't waste time processing it.
     * <p>
     * Weird data here can be REF:ALT:ZYG of A:.:0/1 or .:.:0/1 What does that mean? Not much. Treat it as bad data.
     * 
     * @param tBaseDataList2
     * @param ii
     * @return boolean, true -- has weird, bad data, false -- has not weird data.
     */
    public boolean hasWeirdData(List<String> tBaseDataList2, int iSampleColumnIn) throws Exception {
        boolean bHasWeirdData = false;

        int iColumnToGet = VCFLineDataProcessor.INT_FORMAT_COLUMN_POSITION + iSampleColumnIn + 1;

        int[] iByPosition = this.getZygosityArray(tBaseDataList2.get(iColumnToGet));
        // Are the ref and alt values the same in the sample column? :: 0|0,
        // 0/0? Are they the same length?

        return bHasWeirdData;
    } // end hasWeirdData()

    /**
     * processMonomorphicReference()
     * <p>
     * In the case that there are no alternate alleles (i.e., we find a "." in the ALT column, process that case. And a
     * "0/0" in the GT sample column.
     * 
     * @param tBaseDataList2
     * @param iSampleColumnIn
     */
    public void processMonomorphicReference(List<String> tBaseDataList2, int iSampleColumnIn) throws Exception {
        // Position information.
        String sPosition = tBaseDataList2.get(VCFLineDataProcessor.INT_POSITION_COLUMN_POSITION);
        // Get reference genotype.
        String sTotalGenotype = tBaseDataList2.get(INT_REF_COLUMN_POSITION);
        boolean bHasIndel = false;
        boolean bHasSNP = false;
        boolean bIsNoCall = false;
        boolean bHasNoReferenceData = false;
        // Is this a forward or reverse strand?
        boolean bIsReverseStrand = this.tBAMTool.getRecord().getReadNegativeStrandFlag();
        // Make a VCFRecord with the reference genotype as our genotype value.
        VCFRecord tRecord = VCFRecord.getInstance(iSampleColumnIn, sPosition, sTotalGenotype, sTotalGenotype,
                this.getConsensusQualityScore(iSampleColumnIn), this.getSNPQualityScore(iSampleColumnIn),
                Short.toString(this.tBAMTool.getMappingQuality()), this.getReadDepth(iSampleColumnIn), this.tBAMTool.getReadQualityScores(),
                this.tBAMTool.getReadBases(), bHasIndel, bHasSNP, bIsReverseStrand, bIsNoCall, bHasNoReferenceData);

        this.tDataManager.addRecord(tRecord, iSampleColumnIn);

    } // end processMonomorphicReference

    /**
     * hasMonomorphicReference() Is the ALT column a "."? This means there are no alternative alleles at this position.
     * Is there a
     * 
     * @param tBaseDataList2
     * @param ii
     * @return boolean -- true, we have no alt alleles, false, we do have alt alleles.
     */
    public boolean hasMonomorphicReference(List<String> tBaseDataList2, int iSampleColumnIn) throws Exception {

        // Return value.
        boolean bHasMonomorphicRef = false;
        // Get the correct sample column.
        int iColumnToGet = VCFLineDataProcessor.INT_FORMAT_COLUMN_POSITION + iSampleColumnIn + 1;

        int[] iByPosition = this.getZygosityArray(tBaseDataList2.get(iColumnToGet));
        // Are the ref and alt values the same in the sample column? :: 0|0,
        // 0/0? Are they the same length?

        if ((iByPosition[0] == iByPosition[1])
                && (this.sArrayOfRefAndAltData[iByPosition[0]].length() == this.sArrayOfRefAndAltData[iByPosition[1]].length())) {

            // Are the reference and alt values in the sample column the same?
            // Get the ALT column from the input list.
            String sAltValue = tBaseDataList2.get(INT_ALT_COLUMN_POSITION);

            // Is the ALT column a period (meaning a monomorphic reference --
            // there is no alternate allele)?
            if (sAltValue.equalsIgnoreCase(".") && (iByPosition[0] == 0)) {

                bHasMonomorphicRef = true;
            } // end if

        } // end if

        return bHasMonomorphicRef;
    } // end hasMonomorphicReference

    /**
     * processLargeStructuralVariant()
     * <p>
     * Handles making a VCFRecord for a large structural variant: <DEL>.
     * 
     * @param tBaseDataListIn
     * @param iSampleColumnIn
     */
    public void processLargeStructuralVariant(List<String> tBaseDataListIn, int iSampleColumnIn) throws Exception {
    } // end processLargeStructuralVariant

    /**
     * processSingleHeterozygousSNP()
     * <p>
     * If this is a 1/1 in the variant data, make a record.
     * 
     * @param tBaseDataList2
     * @param ii
     */
    public void processSingleHeterozygousSNP(List<String> tBaseDataList2, int iColumnIdIn) throws Exception {
        // System.out.println("DataProcessor: has SNP.");
        // Position information.
        String sPosition = tBaseDataList2.get(VCFLineDataProcessor.INT_POSITION_COLUMN_POSITION);
        // Get the heterozygous value to insert.
        int iColumnToGet = VCFLineDataProcessor.INT_FORMAT_COLUMN_POSITION + iColumnIdIn + 1;
        int[] iByPosition = this.getZygosityArray(tBaseDataList2.get(iColumnToGet));
        boolean bHasIndel = false;
        boolean bHasSNP = true;
        boolean bIsReverseStrand = this.tBAMTool.getRecord().getReadNegativeStrandFlag();
        boolean bIsNoCall = false;
        boolean bHasNoReferenceData = false;

        // String sTotalGenotype = this.sArrayOfRefAndAltData[iByPosition[0]] +
        // "/" + this.sArrayOfRefAndAltData[iByPosition[1]];
        // Get reference genotype.
        String sRefGenotype = tBaseDataList2.get(INT_REF_COLUMN_POSITION);
        String sAltGenotype = tBaseDataList2.get(INT_ALT_COLUMN_POSITION);
        String sTotalGenotype = sAltGenotype;
        // System.out.println("processSingleHeterozygousSNP: " +
        // sTotalGenotype);

        VCFRecord tRecord = VCFRecord.getInstance(iColumnIdIn, sPosition, sTotalGenotype, sRefGenotype + " " + sAltGenotype,
                this.getConsensusQualityScore(iColumnIdIn), this.getSNPQualityScore(iColumnIdIn),
                Short.toString(this.tBAMTool.getMappingQuality()), this.getReadDepth(iColumnIdIn), this.tBAMTool.getReadQualityScores(),
                this.tBAMTool.getReadBases(), bHasIndel, bHasSNP, bIsReverseStrand, bIsNoCall, bHasNoReferenceData);

        this.tDataManager.addRecord(tRecord, iColumnIdIn);

    } // end processHeterozygousSNP

    /**
     * processSingleHomozygousAltSNP()
     * <p>
     * If this is a homozygous alt snp (1/1).
     * 
     * @param tBaseDataList2
     * @param ii
     */
    public void processSingleHomozygousAltSNP(List<String> tBaseDataList2, int iColumnIdIn) throws Exception {
        // System.out.println("DataProcessor: has SNP.");
        // Position information.
        String sPosition = tBaseDataList2.get(VCFLineDataProcessor.INT_POSITION_COLUMN_POSITION);
        // Get the heterozygous value to insert.
        int iColumnToGet = VCFLineDataProcessor.INT_FORMAT_COLUMN_POSITION + iColumnIdIn + 1;
        int[] iByPosition = this.getZygosityArray(tBaseDataList2.get(iColumnToGet));
        boolean bHasIndel = false;
        boolean bHasSNP = true;
        boolean bIsReverseStrand = this.tBAMTool.getRecord().getReadNegativeStrandFlag();
        boolean bIsNoCall = false;
        boolean bHasNoReferenceData = false;

        // String sTotalGenotype = this.sArrayOfRefAndAltData[iByPosition[0]] +
        // "/" + this.sArrayOfRefAndAltData[iByPosition[1]];
        // Get reference genotype.
        String sRefGenotype = tBaseDataList2.get(INT_REF_COLUMN_POSITION);
        String sAltGenotype = tBaseDataList2.get(INT_ALT_COLUMN_POSITION);
        String sTotalGenotype = sAltGenotype;
        // System.out.println("processSingleHeterozygousSNP: " +
        // sTotalGenotype);

        VCFRecord tRecord = VCFRecord.getInstance(iColumnIdIn, sPosition, sTotalGenotype, sRefGenotype + " " + sAltGenotype,
                this.getConsensusQualityScore(iColumnIdIn), this.getSNPQualityScore(iColumnIdIn),
                Short.toString(this.tBAMTool.getMappingQuality()), this.getReadDepth(iColumnIdIn), this.tBAMTool.getReadQualityScores(),
                this.tBAMTool.getReadBases(), bHasIndel, bHasSNP, bIsReverseStrand, bIsNoCall, bHasNoReferenceData);

        this.tDataManager.addRecord(tRecord, iColumnIdIn);

    } // end processHomozygousAltSNP

    /**
     * hasNoCall() Is the ALT column a "." and the REF column a "."? This means this is a no-call, a "*" genotype.
     * 
     * @param tBaseDataList2
     * @param iSampleColumnIn
     * @return boolean -- true, no-call, false, not no-call.
     */
    public boolean hasNoCall(List<String> tBaseDataList2, int iSampleColumnIn) throws Exception {
        // Return value.
        boolean bHasNoCall = false;
        // Get the correct sample column.
        int iColumnToGet = VCFLineDataProcessor.INT_FORMAT_COLUMN_POSITION + iSampleColumnIn + 1;

        int[] iByPosition = this.getZygosityArray(tBaseDataList2.get(iColumnToGet));

        // Are the ref and alt values the same in the sample column? :: 0|0,
        // 0/0? Are they the same length?
        if ((this.sArrayOfRefAndAltData[0].equalsIgnoreCase(".") && this.sArrayOfRefAndAltData[1].equalsIgnoreCase("."))) {
            bHasNoCall = true;
        } else if (this.hasDotSlashDotSampleData()) {
            bHasNoCall = true;
        }

        return bHasNoCall;
    } // end hasNoCall

    /**
     * processNoCall()
     * <p>
     * If this is a no-call, a "." in the REF and ALT column, use a "*" as the genotype.
     * 
     * @param tBaseDataList2
     * @param ii
     */
    public void processNoCall(List<String> tBaseDataList2, int iColumnIdIn) throws Exception {
        // Position information.
        String sPosition = tBaseDataList2.get(VCFLineDataProcessor.INT_POSITION_COLUMN_POSITION);

        boolean bHasIndel = false;
        boolean bHasSNP = false;
        boolean bIsReverseStrand = this.tBAMTool.getRecord().getReadNegativeStrandFlag();
        boolean bIsNoCall = true;
        boolean bHasNoReferenceData = false;

        String sTotalGenotype = "*";
        // Get reference genotype.
        String sRefGenotype = tBaseDataList2.get(INT_REF_COLUMN_POSITION);

        VCFRecord tRecord = VCFRecord.getInstance(iColumnIdIn, sPosition, sTotalGenotype, sRefGenotype,
                this.getConsensusQualityScore(iColumnIdIn), this.getSNPQualityScore(iColumnIdIn),
                Short.toString(this.tBAMTool.getMappingQuality()), this.getReadDepth(iColumnIdIn), this.tBAMTool.getReadQualityScores(),
                this.tBAMTool.getReadBases(), bHasIndel, bHasSNP, bIsReverseStrand, bIsNoCall, bHasNoReferenceData);

        this.tDataManager.addRecord(tRecord, iColumnIdIn);

    } // end processNoCall

    /**
     * getVCFRecordListForColumn()
     * <p>
     * Return a List<VCFRecord> for the input sample column number.
     * 
     * @param iSampleNumberIn
     *            -- the sample column number (zero-based)
     * @return List<VCFRecord> -- a List of VCFRecord objects for a given column number.
     */
    public List<VCFRecord> getVCFRecordListForColumn(int iSampleNumberIn) {
        return this.tDataManager.getRecordList(iSampleNumberIn);
    } // end getVCFRecordListForColumn

    /**
     * makeConsensusDataList()
     * <p>
     * Make VCFRecord data for each individual in a VCF file. Add record to the data manager.
     * 
     * @param iPairNumberIn
     *            -- the "pair" number of the consensus file(s) to make. Pair number refers to the consensus files
     *            generated for each individual in the VCF file. One or more individuals can live in one VCF file.
     * @param tBaseDataList2
     */
    public void makeConsensusDataList(int iPairNumberIn, List<String> tBaseDataList2) {
        String sPosition = tBaseDataList2.get(VCFLineDataProcessor.INT_POSITION_COLUMN_POSITION);

        boolean bHasIndel = false;
        boolean bHasSNP = false;
        boolean bIsReverseStrand = this.tBAMTool.getRecord().getReadNegativeStrandFlag();
        boolean bIsNoCall = false;
        boolean bHasNoReferenceData = false;

        // Get reference genotype.
        String sTotalGenotype = tBaseDataList2.get(INT_REF_COLUMN_POSITION);

        VCFRecord tRecord = VCFRecord.getInstance(iPairNumberIn, sPosition, this.getGenotype(), sTotalGenotype,
                this.getConsensusQualityScore(iPairNumberIn), this.getSNPQualityScore(iPairNumberIn),
                Short.toString(this.tBAMTool.getMappingQuality()), this.getReadDepth(iPairNumberIn), this.tBAMTool.getReadQualityScores(),
                this.tBAMTool.getReadBases(), bHasIndel, bHasSNP, bIsReverseStrand, bIsNoCall, bHasNoReferenceData);

        this.tDataManager.addRecord(tRecord, iPairNumberIn);

        // return tReturnList;

    } // end makeConsensusDataList()

    /**
     * makeRefAndAltStringArray()
     * <p>
     * Return a composite array of Strings containing the reference column and the alt column. Of the form:
     * sArrayOfRefAndAltData: [A, G]
     */
    public String[] makeRefAndAltStringArray(List<String> tListIn) throws Exception {

        List<String> tTotalList = new ArrayList<String>();
        tTotalList.add((String) tListIn.get(INT_REF_COLUMN_POSITION));
        String[] sAltArray = tListIn.get(INT_ALT_COLUMN_POSITION).split(",");
        for (int ii = 0; ii < sAltArray.length; ii++) {
            tTotalList.add(sAltArray[ii]);
        } // end for

        return (String[]) tTotalList.toArray(new String[tTotalList.size()]);
    } // end makeRefAndAltStringArray

    /**
     * hasInsertion()
     * <p>
     * Determines whether a given input string is an insertion compared to a reference string.
     * <p>
     * An insertion is represented in the genotype column by two non-zero values. A zero value stands for the reference
     * allele.
     * <p>
     * A non-zero value in the genotype column stands for an alt allele. So, two equal but non-zero values in the
     * genotype column could mean an insertion. If the length of the reference value is smaller than the length of the
     * alt column, then we can say we have an insertion.
     * 
     * @param sReference
     *            -- the reference String
     * @param sAlt
     *            -- the alt String
     * @return boolean -- true, the alt string is an insertion, false, the alt string is not an insertion.
     */
    public boolean hasInsertion(List<String> tVCFLineIn, int iSampleColumnIn) throws Exception {
        // Return boolean
        boolean bIsInsertion = false;

        int iColumnToGet = VCFLineDataProcessor.INT_FORMAT_COLUMN_POSITION + iSampleColumnIn + 1;
        int[] iByPosition = this.getZygosityArray(tVCFLineIn.get(iColumnToGet));

        // Insertion: one of the diploid alleles (0/1) is longer than the
        // reference allele (0).
        int iDiploidOneSlotLength = this.sArrayOfRefAndAltData[iByPosition[0]].length();
        int iDiploidTwoSlotLength = this.sArrayOfRefAndAltData[iByPosition[1]].length();
        int iRefAlleleLength = this.sArrayOfRefAndAltData[0].length();
        if ((iRefAlleleLength < iDiploidOneSlotLength) || (iRefAlleleLength < iDiploidTwoSlotLength)) {
            bIsInsertion = true;
        }
        return bIsInsertion;
    } // end hasInsertion

    /**
     * hasDeletion()
     * <p>
     * Determines whether a given input string is a deletion compared to a reference string.
     * <p>
     * A deletion is represented in the genotype column by two non-zero values. A zero value stands for the reference
     * allele.
     * <p>
     * A non-zero value in the genotype column stands for an alt allele. So, two equal but non-zero values in the
     * genotype column could mean a deletion. If the length of the reference value is bigger than the length of the alt
     * column, then we can say we have a deletion.
     * 
     * @param sReference
     *            -- the reference String
     * @param sAlt
     *            -- the alt String
     * @return boolean -- true, the alt string is a deletion, false, the alt string is not a deletion.
     */
    public boolean hasDeletion(List<String> tVCFLineIn, int iSampleColumnIn) throws Exception {
        // Return boolean
        boolean bIsDeletion = false;

        int iColumnToGet = VCFLineDataProcessor.INT_FORMAT_COLUMN_POSITION + iSampleColumnIn + 1;

        int[] iByPosition = this.getZygosityArray(tVCFLineIn.get(iColumnToGet));
        // Deletion: the reference allele (0) is longer than one of the diploid
        // alleles (1/0).
        int iDiploidOneSlotLength = this.sArrayOfRefAndAltData[iByPosition[0]].length();
        int iDiploidTwoSlotLength = this.sArrayOfRefAndAltData[iByPosition[1]].length();
        int iRefAlleleLength = this.sArrayOfRefAndAltData[0].length();
        if ((iRefAlleleLength > iDiploidOneSlotLength) || (iRefAlleleLength > iDiploidTwoSlotLength)) {
            bIsDeletion = true;
        }
        return bIsDeletion;
    } // end hasDeletion

    /**
     * hasLargeStructuralVariant
     * <p>
     * Does the ALT column say "<DEL>"? "<DEL>" means a column contains a Large Structural Variant.
     * 
     * @param tVCFLineIn
     *            -- the List<String> of a line of VCF data.
     * @param iSampleColumn
     *            -- the number of the sample column (zero based).
     */
    public boolean hasLargeStructuralVariant(List<String> tVCFLineIn, int iSampleColumnIn) throws Exception {
        boolean bHasLSV = false;
        return bHasLSV;
    } // end hasLargeStructuralVariant

    /**
     * hasSingleHeterozygousSNP()
     * <p>
     * Returns whether this genotype for a column of data is a heterozygous single nucleotide polymorphism.
     * <p>
     * That is, whether the value read by the machine was ambiguous.
     * <p>
     * It could have been a "C". Or it could have been a "G". So, it's a C/G. Meaning it's a IUB code.
     * 
     * @param tVCFLine
     *            -- a List<String> of a line of VCF data.
     * @param iSampleColumnIn
     *            -- the column number for a sample.
     * @return boolean -- true, the value is ambiguous, "C/G" -> "C or G", false, the value is not ambiguous not
     *         "C or G".
     */
    public boolean hasSingleHeterozygousSNP(List<String> tVCFLineIn, int iSampleColumnIn) throws Exception {
        boolean bValueToReturn = false;

        int iColumnToGet = VCFLineDataProcessor.INT_FORMAT_COLUMN_POSITION + iSampleColumnIn + 1;
        int[] iByPosition = this.getZygosityArray(tVCFLineIn.get(iColumnToGet));

        // Are the numbers in the zygosity array different? And are they the
        // same length? Meaning not an insertion or deletion?
        if ((iByPosition[0] != iByPosition[1])
                && (this.sArrayOfRefAndAltData[iByPosition[0]].length() == this.sArrayOfRefAndAltData[iByPosition[1]].length())) {
            bValueToReturn = true;
        } // end if

        return bValueToReturn;
    } // end hasSingleHeterozygousSNP

    /**
     * hasSingleHomozygousAltSNP()
     * <p>
     * Returns whether this genotype for a column of data is a homozygous single nucleotide polymorphism.
     * <p>
     * That is, whether the value read by the machine was ambiguous.
     * <p>
     * It could have been a "C". Or it could have been a "G". So, it's a C/G. Meaning it's a IUB code.
     * 
     * @param tVCFLine
     *            -- a List<String> of a line of VCF data.
     * @param iSampleColumnIn
     *            -- the column number for a sample.
     * @return boolean -- true, the value is ambiguous, "C/G" -> "C or G", false, the value is not ambiguous not
     *         "C or G".
     */
    public boolean hasSingleHomozygousAltSNP(List<String> tVCFLineIn, int iSampleColumnIn) throws Exception {
        boolean bValueToReturn = false;

        int iColumnToGet = VCFLineDataProcessor.INT_FORMAT_COLUMN_POSITION + iSampleColumnIn + 1;
        int[] iByPosition = this.getZygosityArray(tVCFLineIn.get(iColumnToGet));

        // Are the numbers in the zygosity array different? And are they the
        // same length? Meaning not an insertion or deletion?
        if ((iByPosition[0] == iByPosition[1])
                && (this.sArrayOfRefAndAltData[iByPosition[0]].length() == this.sArrayOfRefAndAltData[iByPosition[1]].length())
                && (iByPosition[0] == 1)) {
            bValueToReturn = true;
        } // end if

        return bValueToReturn;
    } // end hasSingleHomozygousAltSNP

    /**
     * processGenotype()
     * <p>
     * Writes genotype data for ref and alt columns for this individual given a line of VCF data as a List<String>.
     * <p>
     * We are likely looking at a zygosity array of the same numberL: 0/0, 1/1, etc.
     * 
     * @param sRefColumn
     *            -- the reference column value.
     * @param sAltColumn
     *            -- the alt column value.
     * @param iSampleColumnIn
     *            -- the individual to retrieve.
     * @return List<String> -- a two item List<String> containing the order of colums, ref in the first slot and alt in
     *         the second.
     */
    public void processSameAltAndRef(List<String> tVCFLine, int iSampleColumnIn) throws Exception {

        // Pair number starts at 0. So add one to get the column after the
        // FORMAT column.
        int iColumnToGet = VCFLineDataProcessor.INT_FORMAT_COLUMN_POSITION + iSampleColumnIn + 1;
        int[] iByPosition = this.getZygosityArray(tVCFLine.get(iColumnToGet));

        // Are the numbers in the zygosity array the same? Are they both zero,
        // the reference value?
        if ((iByPosition[0] == 0 && iByPosition[1] == 0) && (this.sArrayOfRefAndAltData[INDEX_REFERENCE].length() == 1)) {

            VCFLineDataProcessor.GENOTYPE = this.sArrayOfRefAndAltData[INDEX_REFERENCE];
            // Are they both 1 or 2 or ..., the alt value?
        } else if ((iByPosition[0] == iByPosition[1])
                && (this.sArrayOfRefAndAltData[iByPosition[0]].length() == this.sArrayOfRefAndAltData[iByPosition[1]].length())) {

            VCFLineDataProcessor.GENOTYPE = this.sArrayOfRefAndAltData[iByPosition[0]];
        } else {
            throw new Exception("Don't know how to handle this line: " + Arrays.toString(tVCFLine.toArray()));
        } // end else

    } // end processGenotype

    /**
     * parseFormatColumn()
     * <p>
     * Okay, an attempt to change direction. Let's start by processing the FORMAT column.
     * <p>
     * Then set various globals to tell everybody else the layout of the column.
     * 
     * @param tVCFLine
     *            -- a List<String>, the VCF line of data as a List.
     * @param iSampleColumnIn
     *            -- the sample data column.
     */
    public void parseFormatColumn(List<String> tVCFLine, int iSampleColumnIn) throws Exception {
        // Get format for this line.
        String sFormatString = tVCFLine.get(INT_FORMAT_COLUMN_POSITION);
        // Split format string by colons.
        String[] sFormatElements = sFormatString.split(":");
        // SNP quality score from the QUAL column of the input VCF line.
        String sSNPQualScore = tVCFLine.get(INT_QUAL_COLUMN_POSITION);
        // Is the QUAL column SNP quality score a number?
        if (sSNPQualScore.equalsIgnoreCase(".")) {
            VCFLineDataProcessor.bHasSNPQualityScore = false;
        } else if (VCFLineDataProcessor.isNumber(sSNPQualScore)) {
            VCFLineDataProcessor.bHasSNPQualityScore = false;
            // throw new
            // Exception("SNP Quality Score is not a number for line: " +
            // Arrays.toString(tVCFLine.toArray()));
        } else {
            VCFLineDataProcessor.bHasSNPQualityScore = true;
            VCFLineDataProcessor.iSNPQualityScoreIndex = INT_QUAL_COLUMN_POSITION;
        } // end else

        // Zero length format column?
        if (sFormatElements.length == 0) {
            throw new Exception("Format column has a zero length for line: " + Arrays.toString(tVCFLine.toArray()));
        }
        if (sFormatElements.length != tVCFLine.get(INT_VCF_DATA_COLUMN_POSITION + iSampleColumnIn).split(":").length) {
            throw new Exception("Format column length does not match : format array: " + Arrays.toString(sFormatElements) + " vcf line: "
                    + Arrays.toString(tVCFLine.toArray()));
        }
        // Does the format column not contain a GT or DP? Genotype or Read Depth
        // column?

        // Loop over and pass FORMAT column elements to various handlers.
        for (int ii = 0; ii < sFormatElements.length; ii++) {

            if (sFormatElements[ii].equals(FORMAT_GT)) { // genotype
                VCFLineDataProcessor.bHasGenotype = true;
                VCFLineDataProcessor.iGenotypeIndex = ii;

            } else if (sFormatElements[ii].equals(FORMAT_READ_DEPTH)) { // read
                                                                        // depth
                VCFLineDataProcessor.bHasReadDepth = true;
                VCFLineDataProcessor.iReadDepthIndex = ii;
            } else if (sFormatElements[ii].equals(FORMAT_CONSENSUS_QUALITY)) { // consensus
                                                                               // quality
                VCFLineDataProcessor.bHasConsensusQuailty = true;
                VCFLineDataProcessor.iConsensusQualityIndex = ii;
            }
        } // end for

    } // end parseFormatColumn

    /**
     * isNumber()
     * <p>
     * Is the input string actually a number?
     * 
     * @param sValueIn
     *            -- the input String to test for numberness.
     * @return boolean -- true, is a number, false, is not a number.
     */
    public static boolean isNumber(String sValueIn) {
        boolean bIsNumber = false;
        try {
            Long.parseLong(sValueIn);
        } catch (NumberFormatException e) {
            bIsNumber = false;
        }
        return bIsNumber;

    } // end isNumber

    /**
     * processInsertion()
     * <p>
     * processInsertion() handles insertions. It loops over the supposed insertion and puts the assembled list of
     * Strings to insert into the map for this sample column.
     * <p>
     * Determines the insertion alleles, loops over the longest allele string. Makes VCFRecords for each insertion. Adds
     * the records to the data manager.
     * 
     * @param tListIn
     *            -- a List of Strings representing the VCF line of data to process.
     * @param iSampleColumnIn
     *            -- the column number of a sample genome (or exome) to process.
     */
    public void processInsertion(List<String> tListIn, int iSampleColumnIn) throws Exception {
        // Get the diploid numbers for this sample column.
        int[] iByPosition = this.getZygosityArray(this.getColumnDataForIndividual(iSampleColumnIn));
        // Since we're processing an insertion, either one is larger than the
        // size of the reference allele string.
        int iDiploidSlotOneLength = this.sArrayOfRefAndAltData[iByPosition[0]].length();
        int iDiploidSlotTwoLength = this.sArrayOfRefAndAltData[iByPosition[1]].length();
        // Get the longest alt allele.
        String sInsertionValue = null;
        if (iDiploidSlotOneLength == iDiploidSlotTwoLength) {
            sInsertionValue = this.sArrayOfRefAndAltData[iByPosition[0]];
        } else if (iDiploidSlotOneLength > iDiploidSlotTwoLength) {
            sInsertionValue = this.sArrayOfRefAndAltData[iByPosition[0]];
        } else {
            sInsertionValue = this.sArrayOfRefAndAltData[iByPosition[1]];
        }

        // Get reference genotype.
        String sRefGenotype = tListIn.get(INT_REF_COLUMN_POSITION);
        String sAltGenotype = tListIn.get(INT_ALT_COLUMN_POSITION);

        boolean bHasIndel = true;
        boolean bHasSNP = false;
        boolean bIsReverseStrand = this.tBAMTool.getRecord().getReadNegativeStrandFlag();
        boolean bIsNoCall = false;
        boolean bHasNoReferenceData = false;

        VCFRecord tRecord = VCFRecord.getInstance(iSampleColumnIn, tListIn.get(INT_POSITION_COLUMN_POSITION),
                INSERTION_FLAG + Character.toString(sInsertionValue.charAt(0)), sRefGenotype,
                this.getConsensusQualityScore(iSampleColumnIn), this.getSNPQualityScore(iSampleColumnIn),
                Short.toString(this.tBAMTool.getMappingQuality()), this.getReadDepth(iSampleColumnIn), this.tBAMTool.getReadQualityScores(),
                sRefGenotype + " " + sAltGenotype, bHasIndel, bHasSNP, bIsReverseStrand, bIsNoCall, bHasNoReferenceData);
        this.tDataManager.addRecord(tRecord, iSampleColumnIn);

    } // end processInsertion()

    /**
     * processDeletion()
     * <p>
     * processDeletion handles deletions discovered in a line of VCF data.
     * 
     * @param tListIn
     *            -- a List of Strings containing a line of VCF data.
     * @param iSampleColumnIn
     *            -- the column number of a sample in a VCF file (zero-based).
     */
    public void processDeletion(List<String> tListIn, int iSampleColumnIn) throws Exception {
        // Get the diploid numbers for this sample column.
        int[] iByPosition = this.getZygosityArray(this.getColumnDataForIndividual(iSampleColumnIn));
        // Since we're processing a deletion, either one is smaller than the
        // size of the reference allele string.
        int iDiploidSlotOneLength = this.sArrayOfRefAndAltData[iByPosition[0]].length();
        int iDiploidSlotTwoLength = this.sArrayOfRefAndAltData[iByPosition[1]].length();
        // Get the smallest alt allele.
        String sDeletionValue = null;
        if (iDiploidSlotOneLength == iDiploidSlotTwoLength) {
            sDeletionValue = this.sArrayOfRefAndAltData[iByPosition[0]];
        } else if (iDiploidSlotOneLength > iDiploidSlotTwoLength) {
            sDeletionValue = this.sArrayOfRefAndAltData[iByPosition[0]];
        } else {
            sDeletionValue = this.sArrayOfRefAndAltData[iByPosition[1]];
        }
        // System.out.println("processDeletion: " +
        // tListIn.get(INT_REF_COLUMN_POSITION) + " " +
        // tListIn.get(INT_ALT_COLUMN_POSITION));

        // Get reference genotype.
        String sRefGenotype = tListIn.get(INT_REF_COLUMN_POSITION);
        // Get alt genotype.
        String sAltGenotype = tListIn.get(INT_ALT_COLUMN_POSITION);

        VCFRecord tRecord;
        boolean bHasIndel = true;
        boolean bHasSNP = false;
        boolean bIsReverseStrand = this.tBAMTool.getRecord().getReadNegativeStrandFlag();
        boolean bIsNoCall = false;
        boolean bHasNoReferenceData = false;

        // Insert a '-/' + Allele character to represent the deletion.
        tRecord = VCFRecord.getInstance(iSampleColumnIn, tListIn.get(INT_POSITION_COLUMN_POSITION),
                DELETION_FLAG + Character.toString(sDeletionValue.charAt(0)), sRefGenotype, this.getConsensusQualityScore(iSampleColumnIn),
                this.getSNPQualityScore(iSampleColumnIn), Short.toString(this.tBAMTool.getMappingQuality()),
                // this.getReadDepth(iSampleColumnIn),
                // this.tBAMTool.getReadQualityScores(), sDeletionValue,
                // bHasIndel, bHasSNP);
                this.getReadDepth(iSampleColumnIn), this.tBAMTool.getReadQualityScores(), sRefGenotype + " " + sAltGenotype, bHasIndel,
                bHasSNP, bIsReverseStrand, bIsNoCall, bHasNoReferenceData);

        this.tDataManager.addRecord(tRecord, iSampleColumnIn);

    } // end processDeletion

    /**
     * getColumnDataForIndividual()
     * <p>
     * Return a String of data for an individual in a VCF file.
     * 
     * @param iPairId
     *            -- the id of the individual
     * @return String -- the String of data for this column.
     */
    public String getColumnDataForIndividual(int iPairIdIn) {
        String sReturn = "";
        sReturn = this.tBaseDataList.get(iPairIdIn + VCFLineDataProcessor.INT_FORMAT_COLUMN_POSITION + 1);
        return sReturn;
    } // end getColumnDataForIndividual()

    /**
     * getGenotype()
     * <p>
     * Placeholder to return genotype value.
     */
    public String getGenotype() {
        return VCFLineDataProcessor.GENOTYPE;
    } // end getGenotype

    /**
     * getConsensusQualityScore()
     * <p>
     * Placeholder method to return consensus quality score, "GQ" in the format column of vcf files.
     * 
     * @return
     */
    public String getConsensusQualityScore(int iFilePairNumberIn) {
        String sReturnVal = null;
        if (this.hasDotSlashDotSampleData()) {
            sReturnVal = "0";
        } else {
            if (VCFLineDataProcessor.bHasConsensusQuailty) {
                String sConsensusQualityScore = this.getValueInIndividualColumnData(iFilePairNumberIn,
                        VCFLineDataProcessor.iConsensusQualityIndex);
                sReturnVal = sConsensusQualityScore;
            } else {
                sReturnVal = "0";
            }
        }
        // Consensus Quality is the GQ column. The "1" position (zero-based).
        // return Integer.toString(Double.valueOf(sReturnVal).intValue());
        return Long.toString(Math.round(Double.valueOf(sReturnVal)));
    } // end getConsensusQualityScore

    /**
     * getSNPQualityScore()
     * <p>
     * Placeholder method to return snp quality score.
     * 
     * @return int
     */
    public String getSNPQualityScore(int iSampleColumnIn) {
        try {
            return Long.toString(Math.round(Double.valueOf(this.tBaseDataList.get(INT_QUAL_COLUMN_POSITION))));
            // return
            // Integer.toString(Double.valueOf(this.tBaseDataList.get(INT_QUAL_COLUMN_POSITION)).intValue());
        } catch (NumberFormatException e) {
            return "0";
        }
    } // end getSNPQualityScore

    /**
     * getReadDepth()
     * <p>
     * Placeholder method to return read depth value
     * 
     * @return int
     */
    public String getReadDepth(int iSampleColumnIn) {
        String sReturnVal = null;
        if (this.hasDotSlashDotSampleData()) {
            sReturnVal = "0";
        } else {
            if (VCFLineDataProcessor.bHasReadDepth) {
                String sReadDepth = this.getValueInIndividualColumnData(iSampleColumnIn, VCFLineDataProcessor.iReadDepthIndex);
                sReturnVal = sReadDepth;
            } else {
                sReturnVal = "0";

            }
        }
        return Integer.toString(Double.valueOf(sReturnVal).intValue());
    } // end getReadDepth

    /**
     * getValueInIndividualColumnData()
     * <p>
     * Get a bit of data from an individual sample.
     */
    public String getValueInIndividualColumnData(int iSampleColumnIn, int iIndexIn) {
        String sReturn = null;
        if (this.hasDotSlashDotSampleData()) {
            sReturn = "0";
        } else {
            String sDataString = this.getColumnDataForIndividual(iSampleColumnIn);
            String[] sSplitArray = sDataString.split(":");
            sReturn = sSplitArray[iIndexIn];
        }
        return sReturn;
    } // end getValueInIndividualColumnData

    /**
     * getZygosityArray
     * <p>
     * Return an int array with the writable zygous elements for a individual. 0/0 as two elements of an int[] array
     * {0,0} or 1|0 as {1,0}, etc.
     * 
     * @param sProcessStringIn
     * @return int[] -- the write position indicating the ref or alt (columns separated by comma) to write
     */
    public int[] getZygosityArray(String sProcessStringIn) throws Exception {
        // Array to store the zygosity setting whose whereabouts are indicated
        // by the format column: 0/0 or 1/1 or whatever.
        // This data bit (0/0) might live anywhere in the format column.
        // parseFormatColumn() sets up the indices for where
        // the genotype (zygosity) is found via the
        // VCFLineDataProcessor.iGenotypeIndex static value.
        VCFLineDataProcessor.bHasDotSlashDotSampleData = false;
        int[] iByPipe;

        if (sProcessStringIn.equalsIgnoreCase("./.") || sProcessStringIn.equalsIgnoreCase(".")) {
            iByPipe = new int[] { 0, 0 };
            VCFLineDataProcessor.bHasDotSlashDotSampleData = true;
        } else {
            String[] sByColon = sProcessStringIn.split(":");

            // This line might be of the form: [42, 0/0, -3.56,-0.30,-0.00,
            // 1.76] the zygosity array 0/0 is in the second slot.
            // Use the parseFormatColumn() method's iGenotypeIndex to tell us
            // where in the format column the zygosity array lives.
            // We wind up with Strings of the form ["0", "0"] or ["1", "0"].
            String[] sByPipe = sByColon[VCFLineDataProcessor.iGenotypeIndex].split("[|]|[/]");
            // Divide up this zygosity marker 0/0, 0/1, 1/1 or whatever, into
            // two bits of an int array.
            // Return the int[] array.
            iByPipe = new int[sByPipe.length];
            iByPipe[0] = Integer.parseInt(sByPipe[0]);
            iByPipe[1] = Integer.parseInt(sByPipe[1]);
        }
        return iByPipe;
    } // end getZygosityArray

    /**
     * hasDotSlashDotSampleData()
     * <p>
     * Returns a boolean for the case of a "./." in the sample column.
     * 
     * @return true -- has a ./. in the sample column, false -- does not have a ./. in the sample column.
     */
    public boolean hasDotSlashDotSampleData() {
        return VCFLineDataProcessor.bHasDotSlashDotSampleData;
    } // end hasDotSlashDotSampleData

} // end VCFLineDataProcessor
