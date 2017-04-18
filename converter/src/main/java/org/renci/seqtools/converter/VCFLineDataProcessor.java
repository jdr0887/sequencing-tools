package org.renci.seqtools.converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VCFLineDataProcessor {

    public static final int INT_ALT_COLUMN_POSITION = 4;

    public static final int INT_REF_COLUMN_POSITION = 3;

    public static final int INT_FORMAT_COLUMN_POSITION = 8;

    public static final int INT_POSITION_COLUMN_POSITION = 1;

    public static final int INT_QUAL_COLUMN_POSITION = 5;

    public static final int INT_VCF_DATA_COLUMN_POSITION = 9;

    public static boolean bHasGenotype = false;

    public static boolean bHasSNPQualityScore = false;

    public static boolean bHasReadDepth = false;

    public static boolean bHasConsensusQuailty = false;

    private static boolean bHasDotSlashDotSampleData = false;

    public static int iGenotypeIndex = 0;

    public static int iSNPQualityScoreIndex = 0;

    public static int iReadDepthIndex = 0;

    public static int iConsensusQualityIndex = 0;

    private static final String FORMAT_GT = "GT";

    private static final String FORMAT_QUAL = "QUAL";

    private static final String FORMAT_READ_DEPTH = "DP";

    private static final String FORMAT_CONSENSUS_QUALITY = "GQ";

    private static final int INDEX_REFERENCE = 0;

    private static final String INSERTION_FLAG = "+/";

    private static final String DELETION_FLAG = "-/";

    private static final String TAB = "\t";

    private static String GENOTYPE = "Z";

    private List<String> tBaseDataList = null;

    private int iFilePairNumber = 0;

    private VCFBAMQueryTool tBAMTool;

    private VCFDataManager tDataManager;

    private String[] sArrayOfRefAndAltData;

    private VCFLineDataProcessor() {
    }

    private VCFLineDataProcessor(List<String> tOutputList, int iNumberOfFilePairs, VCFBAMQueryTool tBAMTool, String sStartPosition,
            String sEndPosition) throws Exception {
        this.tBaseDataList = tOutputList;
        this.iFilePairNumber = iNumberOfFilePairs;
        this.tBAMTool = tBAMTool;
        this.sArrayOfRefAndAltData = this.makeRefAndAltStringArray(this.tBaseDataList);
        this.tDataManager = VCFDataManager.getInstance();
    }

    public static VCFLineDataProcessor getInstance(List<String> tOutputList, int iNumberOfFilePairs, VCFBAMQueryTool tBAMTool,
            String sStartPosition, String sEndPosition) throws Exception {
        return new VCFLineDataProcessor(tOutputList, iNumberOfFilePairs, tBAMTool, sStartPosition, sEndPosition);
    }

    public static VCFLineDataProcessor getInstance() {
        return new VCFLineDataProcessor();
    }

    public void setInputs(List<String> tOutputList, int iNumberOfFilePairs, VCFBAMQueryTool tBAMTool, String sStartPosition,
            String sEndPosition) throws Exception {

        this.tBaseDataList = null;
        this.iFilePairNumber = 0;

        this.tBAMTool = null;

        this.tDataManager = null;

        this.sArrayOfRefAndAltData = null;

        VCFLineDataProcessor.bHasGenotype = false;
        VCFLineDataProcessor.bHasSNPQualityScore = false;
        VCFLineDataProcessor.bHasReadDepth = false;
        VCFLineDataProcessor.bHasConsensusQuailty = false;

        VCFLineDataProcessor.bHasDotSlashDotSampleData = false;

        VCFLineDataProcessor.iGenotypeIndex = 0;
        VCFLineDataProcessor.iSNPQualityScoreIndex = 0;
        VCFLineDataProcessor.iReadDepthIndex = 0;
        VCFLineDataProcessor.iConsensusQualityIndex = 0;

        this.tBaseDataList = tOutputList;
        this.iFilePairNumber = iNumberOfFilePairs;
        this.tBAMTool = tBAMTool;
        this.sArrayOfRefAndAltData = this.makeRefAndAltStringArray(this.tBaseDataList);
        this.tDataManager = VCFDataManager.getInstance();

    }

    public void loadParsedVCFDataIntoMap() throws Exception {

        this.tBAMTool.getRecord();

        for (int ii = 0; ii < this.iFilePairNumber; ii++) {

            this.parseFormatColumn(this.tBaseDataList, ii);

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

                this.makeConsensusDataList(ii, this.tBaseDataList);
            }
        }

    }

    private void processNoReferenceData(List<String> tBaseDataList2, int iColumnIdIn) {

        String sPosition = tBaseDataList2.get(VCFLineDataProcessor.INT_POSITION_COLUMN_POSITION);

        boolean bHasIndel = false;
        boolean bHasSNP = false;
        boolean bIsReverseStrand = this.tBAMTool.getRecord().getReadNegativeStrandFlag();
        boolean bIsNoCall = false;
        boolean bHasNoReferenceData = true;

        String sTotalGenotype = "null";

        String sRefGenotype = tBaseDataList2.get(INT_REF_COLUMN_POSITION);

        VCFRecord tRecord = VCFRecord.getInstance(iColumnIdIn, sPosition, sTotalGenotype, sRefGenotype,
                this.getConsensusQualityScore(iColumnIdIn), this.getSNPQualityScore(iColumnIdIn),
                Short.toString(this.tBAMTool.getMappingQuality()), this.getReadDepth(iColumnIdIn), this.tBAMTool.getReadQualityScores(),
                this.tBAMTool.getReadBases(), bHasIndel, bHasSNP, bIsReverseStrand, bIsNoCall, bHasNoReferenceData);

        this.tDataManager.addRecord(tRecord, iColumnIdIn);

    }

    public boolean hasNoReferenceData(List<String> tBaseDataList2, int iSampleColumnIn) throws Exception {
        boolean bHasNoReferenceData = false;

        String sRefValue = tBaseDataList2.get(this.INT_REF_COLUMN_POSITION);

        if (sRefValue.equalsIgnoreCase(".")) {

            bHasNoReferenceData = true;
        }

        return bHasNoReferenceData;
    }

    public boolean hasWeirdData(List<String> tBaseDataList2, int iSampleColumnIn) throws Exception {
        boolean bHasWeirdData = false;

        int iColumnToGet = VCFLineDataProcessor.INT_FORMAT_COLUMN_POSITION + iSampleColumnIn + 1;

        int[] iByPosition = this.getZygosityArray(tBaseDataList2.get(iColumnToGet));

        return bHasWeirdData;
    }

    public void processMonomorphicReference(List<String> tBaseDataList2, int iSampleColumnIn) throws Exception {

        String sPosition = tBaseDataList2.get(VCFLineDataProcessor.INT_POSITION_COLUMN_POSITION);

        String sTotalGenotype = tBaseDataList2.get(INT_REF_COLUMN_POSITION);
        boolean bHasIndel = false;
        boolean bHasSNP = false;
        boolean bIsNoCall = false;
        boolean bHasNoReferenceData = false;

        boolean bIsReverseStrand = this.tBAMTool.getRecord().getReadNegativeStrandFlag();

        VCFRecord tRecord = VCFRecord.getInstance(iSampleColumnIn, sPosition, sTotalGenotype, sTotalGenotype,
                this.getConsensusQualityScore(iSampleColumnIn), this.getSNPQualityScore(iSampleColumnIn),
                Short.toString(this.tBAMTool.getMappingQuality()), this.getReadDepth(iSampleColumnIn), this.tBAMTool.getReadQualityScores(),
                this.tBAMTool.getReadBases(), bHasIndel, bHasSNP, bIsReverseStrand, bIsNoCall, bHasNoReferenceData);

        this.tDataManager.addRecord(tRecord, iSampleColumnIn);

    }

    public boolean hasMonomorphicReference(List<String> tBaseDataList2, int iSampleColumnIn) throws Exception {

        boolean bHasMonomorphicRef = false;

        int iColumnToGet = VCFLineDataProcessor.INT_FORMAT_COLUMN_POSITION + iSampleColumnIn + 1;

        int[] iByPosition = this.getZygosityArray(tBaseDataList2.get(iColumnToGet));

        if ((iByPosition[0] == iByPosition[1])
                && (this.sArrayOfRefAndAltData[iByPosition[0]].length() == this.sArrayOfRefAndAltData[iByPosition[1]].length())) {

            String sAltValue = tBaseDataList2.get(INT_ALT_COLUMN_POSITION);

            if (sAltValue.equalsIgnoreCase(".") && (iByPosition[0] == 0)) {

                bHasMonomorphicRef = true;
            }

        }

        return bHasMonomorphicRef;
    }

    public void processLargeStructuralVariant(List<String> tBaseDataListIn, int iSampleColumnIn) throws Exception {
    }

    public void processSingleHeterozygousSNP(List<String> tBaseDataList2, int iColumnIdIn) throws Exception {

        String sPosition = tBaseDataList2.get(VCFLineDataProcessor.INT_POSITION_COLUMN_POSITION);

        int iColumnToGet = VCFLineDataProcessor.INT_FORMAT_COLUMN_POSITION + iColumnIdIn + 1;
        int[] iByPosition = this.getZygosityArray(tBaseDataList2.get(iColumnToGet));
        boolean bHasIndel = false;
        boolean bHasSNP = true;
        boolean bIsReverseStrand = this.tBAMTool.getRecord().getReadNegativeStrandFlag();
        boolean bIsNoCall = false;
        boolean bHasNoReferenceData = false;

        String sRefGenotype = tBaseDataList2.get(INT_REF_COLUMN_POSITION);
        String sAltGenotype = tBaseDataList2.get(INT_ALT_COLUMN_POSITION);
        String sTotalGenotype = sAltGenotype;

        VCFRecord tRecord = VCFRecord.getInstance(iColumnIdIn, sPosition, sTotalGenotype, sRefGenotype + " " + sAltGenotype,
                this.getConsensusQualityScore(iColumnIdIn), this.getSNPQualityScore(iColumnIdIn),
                Short.toString(this.tBAMTool.getMappingQuality()), this.getReadDepth(iColumnIdIn), this.tBAMTool.getReadQualityScores(),
                this.tBAMTool.getReadBases(), bHasIndel, bHasSNP, bIsReverseStrand, bIsNoCall, bHasNoReferenceData);

        this.tDataManager.addRecord(tRecord, iColumnIdIn);

    }

    public void processSingleHomozygousAltSNP(List<String> tBaseDataList2, int iColumnIdIn) throws Exception {

        String sPosition = tBaseDataList2.get(VCFLineDataProcessor.INT_POSITION_COLUMN_POSITION);

        int iColumnToGet = VCFLineDataProcessor.INT_FORMAT_COLUMN_POSITION + iColumnIdIn + 1;
        int[] iByPosition = this.getZygosityArray(tBaseDataList2.get(iColumnToGet));
        boolean bHasIndel = false;
        boolean bHasSNP = true;
        boolean bIsReverseStrand = this.tBAMTool.getRecord().getReadNegativeStrandFlag();
        boolean bIsNoCall = false;
        boolean bHasNoReferenceData = false;

        String sRefGenotype = tBaseDataList2.get(INT_REF_COLUMN_POSITION);
        String sAltGenotype = tBaseDataList2.get(INT_ALT_COLUMN_POSITION);
        String sTotalGenotype = sAltGenotype;

        VCFRecord tRecord = VCFRecord.getInstance(iColumnIdIn, sPosition, sTotalGenotype, sRefGenotype + " " + sAltGenotype,
                this.getConsensusQualityScore(iColumnIdIn), this.getSNPQualityScore(iColumnIdIn),
                Short.toString(this.tBAMTool.getMappingQuality()), this.getReadDepth(iColumnIdIn), this.tBAMTool.getReadQualityScores(),
                this.tBAMTool.getReadBases(), bHasIndel, bHasSNP, bIsReverseStrand, bIsNoCall, bHasNoReferenceData);

        this.tDataManager.addRecord(tRecord, iColumnIdIn);

    }

    public boolean hasNoCall(List<String> tBaseDataList2, int iSampleColumnIn) throws Exception {

        boolean bHasNoCall = false;

        int iColumnToGet = VCFLineDataProcessor.INT_FORMAT_COLUMN_POSITION + iSampleColumnIn + 1;

        int[] iByPosition = this.getZygosityArray(tBaseDataList2.get(iColumnToGet));

        if ((this.sArrayOfRefAndAltData[0].equalsIgnoreCase(".") && this.sArrayOfRefAndAltData[1].equalsIgnoreCase("."))) {
            bHasNoCall = true;
        } else if (this.hasDotSlashDotSampleData()) {
            bHasNoCall = true;
        }

        return bHasNoCall;
    }

    public void processNoCall(List<String> tBaseDataList2, int iColumnIdIn) throws Exception {

        String sPosition = tBaseDataList2.get(VCFLineDataProcessor.INT_POSITION_COLUMN_POSITION);

        boolean bHasIndel = false;
        boolean bHasSNP = false;
        boolean bIsReverseStrand = this.tBAMTool.getRecord().getReadNegativeStrandFlag();
        boolean bIsNoCall = true;
        boolean bHasNoReferenceData = false;

        String sTotalGenotype = "*";

        String sRefGenotype = tBaseDataList2.get(INT_REF_COLUMN_POSITION);

        VCFRecord tRecord = VCFRecord.getInstance(iColumnIdIn, sPosition, sTotalGenotype, sRefGenotype,
                this.getConsensusQualityScore(iColumnIdIn), this.getSNPQualityScore(iColumnIdIn),
                Short.toString(this.tBAMTool.getMappingQuality()), this.getReadDepth(iColumnIdIn), this.tBAMTool.getReadQualityScores(),
                this.tBAMTool.getReadBases(), bHasIndel, bHasSNP, bIsReverseStrand, bIsNoCall, bHasNoReferenceData);

        this.tDataManager.addRecord(tRecord, iColumnIdIn);

    }

    public List<VCFRecord> getVCFRecordListForColumn(int iSampleNumberIn) {
        return this.tDataManager.getRecordList(iSampleNumberIn);
    }

    public void makeConsensusDataList(int iPairNumberIn, List<String> tBaseDataList2) {
        String sPosition = tBaseDataList2.get(VCFLineDataProcessor.INT_POSITION_COLUMN_POSITION);

        boolean bHasIndel = false;
        boolean bHasSNP = false;
        boolean bIsReverseStrand = this.tBAMTool.getRecord().getReadNegativeStrandFlag();
        boolean bIsNoCall = false;
        boolean bHasNoReferenceData = false;

        String sTotalGenotype = tBaseDataList2.get(INT_REF_COLUMN_POSITION);

        VCFRecord tRecord = VCFRecord.getInstance(iPairNumberIn, sPosition, this.getGenotype(), sTotalGenotype,
                this.getConsensusQualityScore(iPairNumberIn), this.getSNPQualityScore(iPairNumberIn),
                Short.toString(this.tBAMTool.getMappingQuality()), this.getReadDepth(iPairNumberIn), this.tBAMTool.getReadQualityScores(),
                this.tBAMTool.getReadBases(), bHasIndel, bHasSNP, bIsReverseStrand, bIsNoCall, bHasNoReferenceData);

        this.tDataManager.addRecord(tRecord, iPairNumberIn);

    }

    public String[] makeRefAndAltStringArray(List<String> tListIn) throws Exception {

        List<String> tTotalList = new ArrayList<String>();
        tTotalList.add((String) tListIn.get(INT_REF_COLUMN_POSITION));
        String[] sAltArray = tListIn.get(INT_ALT_COLUMN_POSITION).split(",");
        for (int ii = 0; ii < sAltArray.length; ii++) {
            tTotalList.add(sAltArray[ii]);
        }

        return (String[]) tTotalList.toArray(new String[tTotalList.size()]);
    }

    public boolean hasInsertion(List<String> tVCFLineIn, int iSampleColumnIn) throws Exception {

        boolean bIsInsertion = false;

        int iColumnToGet = VCFLineDataProcessor.INT_FORMAT_COLUMN_POSITION + iSampleColumnIn + 1;
        int[] iByPosition = this.getZygosityArray(tVCFLineIn.get(iColumnToGet));

        int iDiploidOneSlotLength = this.sArrayOfRefAndAltData[iByPosition[0]].length();
        int iDiploidTwoSlotLength = this.sArrayOfRefAndAltData[iByPosition[1]].length();
        int iRefAlleleLength = this.sArrayOfRefAndAltData[0].length();
        if ((iRefAlleleLength < iDiploidOneSlotLength) || (iRefAlleleLength < iDiploidTwoSlotLength)) {
            bIsInsertion = true;
        }
        return bIsInsertion;
    }

    public boolean hasDeletion(List<String> tVCFLineIn, int iSampleColumnIn) throws Exception {

        boolean bIsDeletion = false;

        int iColumnToGet = VCFLineDataProcessor.INT_FORMAT_COLUMN_POSITION + iSampleColumnIn + 1;

        int[] iByPosition = this.getZygosityArray(tVCFLineIn.get(iColumnToGet));

        int iDiploidOneSlotLength = this.sArrayOfRefAndAltData[iByPosition[0]].length();
        int iDiploidTwoSlotLength = this.sArrayOfRefAndAltData[iByPosition[1]].length();
        int iRefAlleleLength = this.sArrayOfRefAndAltData[0].length();
        if ((iRefAlleleLength > iDiploidOneSlotLength) || (iRefAlleleLength > iDiploidTwoSlotLength)) {
            bIsDeletion = true;
        }
        return bIsDeletion;
    }

    public boolean hasLargeStructuralVariant(List<String> tVCFLineIn, int iSampleColumnIn) throws Exception {
        boolean bHasLSV = false;
        return bHasLSV;
    }

    public boolean hasSingleHeterozygousSNP(List<String> tVCFLineIn, int iSampleColumnIn) throws Exception {
        boolean bValueToReturn = false;

        int iColumnToGet = VCFLineDataProcessor.INT_FORMAT_COLUMN_POSITION + iSampleColumnIn + 1;
        int[] iByPosition = this.getZygosityArray(tVCFLineIn.get(iColumnToGet));

        if ((iByPosition[0] != iByPosition[1])
                && (this.sArrayOfRefAndAltData[iByPosition[0]].length() == this.sArrayOfRefAndAltData[iByPosition[1]].length())) {
            bValueToReturn = true;
        }

        return bValueToReturn;
    }

    public boolean hasSingleHomozygousAltSNP(List<String> tVCFLineIn, int iSampleColumnIn) throws Exception {
        boolean bValueToReturn = false;

        int iColumnToGet = VCFLineDataProcessor.INT_FORMAT_COLUMN_POSITION + iSampleColumnIn + 1;
        int[] iByPosition = this.getZygosityArray(tVCFLineIn.get(iColumnToGet));

        if ((iByPosition[0] == iByPosition[1])
                && (this.sArrayOfRefAndAltData[iByPosition[0]].length() == this.sArrayOfRefAndAltData[iByPosition[1]].length())
                && (iByPosition[0] == 1)) {
            bValueToReturn = true;
        }

        return bValueToReturn;
    }

    public void processSameAltAndRef(List<String> tVCFLine, int iSampleColumnIn) throws Exception {

        int iColumnToGet = VCFLineDataProcessor.INT_FORMAT_COLUMN_POSITION + iSampleColumnIn + 1;
        int[] iByPosition = this.getZygosityArray(tVCFLine.get(iColumnToGet));

        if ((iByPosition[0] == 0 && iByPosition[1] == 0) && (this.sArrayOfRefAndAltData[INDEX_REFERENCE].length() == 1)) {

            VCFLineDataProcessor.GENOTYPE = this.sArrayOfRefAndAltData[INDEX_REFERENCE];

        } else if ((iByPosition[0] == iByPosition[1])
                && (this.sArrayOfRefAndAltData[iByPosition[0]].length() == this.sArrayOfRefAndAltData[iByPosition[1]].length())) {

            VCFLineDataProcessor.GENOTYPE = this.sArrayOfRefAndAltData[iByPosition[0]];
        } else {
            throw new Exception("Don't know how to handle this line: " + Arrays.toString(tVCFLine.toArray()));
        }

    }

    public void parseFormatColumn(List<String> tVCFLine, int iSampleColumnIn) throws Exception {

        String sFormatString = tVCFLine.get(INT_FORMAT_COLUMN_POSITION);

        String[] sFormatElements = sFormatString.split(":");

        String sSNPQualScore = tVCFLine.get(INT_QUAL_COLUMN_POSITION);

        if (sSNPQualScore.equalsIgnoreCase(".")) {
            VCFLineDataProcessor.bHasSNPQualityScore = false;
        } else if (VCFLineDataProcessor.isNumber(sSNPQualScore)) {
            VCFLineDataProcessor.bHasSNPQualityScore = false;

        } else {
            VCFLineDataProcessor.bHasSNPQualityScore = true;
            VCFLineDataProcessor.iSNPQualityScoreIndex = INT_QUAL_COLUMN_POSITION;
        }

        if (sFormatElements.length == 0) {
            throw new Exception("Format column has a zero length for line: " + Arrays.toString(tVCFLine.toArray()));
        }
        if (sFormatElements.length != tVCFLine.get(INT_VCF_DATA_COLUMN_POSITION + iSampleColumnIn).split(":").length) {
            throw new Exception("Format column length does not match : format array: " + Arrays.toString(sFormatElements) + " vcf line: "
                    + Arrays.toString(tVCFLine.toArray()));
        }

        for (int ii = 0; ii < sFormatElements.length; ii++) {

            if (sFormatElements[ii].equals(FORMAT_GT)) {
                VCFLineDataProcessor.bHasGenotype = true;
                VCFLineDataProcessor.iGenotypeIndex = ii;

            } else if (sFormatElements[ii].equals(FORMAT_READ_DEPTH)) {

                VCFLineDataProcessor.bHasReadDepth = true;
                VCFLineDataProcessor.iReadDepthIndex = ii;
            } else if (sFormatElements[ii].equals(FORMAT_CONSENSUS_QUALITY)) {

                VCFLineDataProcessor.bHasConsensusQuailty = true;
                VCFLineDataProcessor.iConsensusQualityIndex = ii;
            }
        }

    }

    public static boolean isNumber(String sValueIn) {
        boolean bIsNumber = false;
        try {
            Long.parseLong(sValueIn);
        } catch (NumberFormatException e) {
            bIsNumber = false;
        }
        return bIsNumber;

    }

    public void processInsertion(List<String> tListIn, int iSampleColumnIn) throws Exception {

        int[] iByPosition = this.getZygosityArray(this.getColumnDataForIndividual(iSampleColumnIn));

        int iDiploidSlotOneLength = this.sArrayOfRefAndAltData[iByPosition[0]].length();
        int iDiploidSlotTwoLength = this.sArrayOfRefAndAltData[iByPosition[1]].length();

        String sInsertionValue = null;
        if (iDiploidSlotOneLength == iDiploidSlotTwoLength) {
            sInsertionValue = this.sArrayOfRefAndAltData[iByPosition[0]];
        } else if (iDiploidSlotOneLength > iDiploidSlotTwoLength) {
            sInsertionValue = this.sArrayOfRefAndAltData[iByPosition[0]];
        } else {
            sInsertionValue = this.sArrayOfRefAndAltData[iByPosition[1]];
        }

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

    }

    public void processDeletion(List<String> tListIn, int iSampleColumnIn) throws Exception {

        int[] iByPosition = this.getZygosityArray(this.getColumnDataForIndividual(iSampleColumnIn));

        int iDiploidSlotOneLength = this.sArrayOfRefAndAltData[iByPosition[0]].length();
        int iDiploidSlotTwoLength = this.sArrayOfRefAndAltData[iByPosition[1]].length();

        String sDeletionValue = null;
        if (iDiploidSlotOneLength == iDiploidSlotTwoLength) {
            sDeletionValue = this.sArrayOfRefAndAltData[iByPosition[0]];
        } else if (iDiploidSlotOneLength > iDiploidSlotTwoLength) {
            sDeletionValue = this.sArrayOfRefAndAltData[iByPosition[0]];
        } else {
            sDeletionValue = this.sArrayOfRefAndAltData[iByPosition[1]];
        }

        String sRefGenotype = tListIn.get(INT_REF_COLUMN_POSITION);

        String sAltGenotype = tListIn.get(INT_ALT_COLUMN_POSITION);

        VCFRecord tRecord;
        boolean bHasIndel = true;
        boolean bHasSNP = false;
        boolean bIsReverseStrand = this.tBAMTool.getRecord().getReadNegativeStrandFlag();
        boolean bIsNoCall = false;
        boolean bHasNoReferenceData = false;

        tRecord = VCFRecord.getInstance(iSampleColumnIn, tListIn.get(INT_POSITION_COLUMN_POSITION),
                DELETION_FLAG + Character.toString(sDeletionValue.charAt(0)), sRefGenotype, this.getConsensusQualityScore(iSampleColumnIn),
                this.getSNPQualityScore(iSampleColumnIn), Short.toString(this.tBAMTool.getMappingQuality()),

                this.getReadDepth(iSampleColumnIn), this.tBAMTool.getReadQualityScores(), sRefGenotype + " " + sAltGenotype, bHasIndel,
                bHasSNP, bIsReverseStrand, bIsNoCall, bHasNoReferenceData);

        this.tDataManager.addRecord(tRecord, iSampleColumnIn);

    }

    public String getColumnDataForIndividual(int iPairIdIn) {
        String sReturn = "";
        sReturn = this.tBaseDataList.get(iPairIdIn + VCFLineDataProcessor.INT_FORMAT_COLUMN_POSITION + 1);
        return sReturn;
    }

    public String getGenotype() {
        return VCFLineDataProcessor.GENOTYPE;
    }

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

        return Long.toString(Math.round(Double.valueOf(sReturnVal)));
    }

    public String getSNPQualityScore(int iSampleColumnIn) {
        try {
            return Long.toString(Math.round(Double.valueOf(this.tBaseDataList.get(INT_QUAL_COLUMN_POSITION))));

        } catch (NumberFormatException e) {
            return "0";
        }
    }

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
    }

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
    }

    public int[] getZygosityArray(String sProcessStringIn) throws Exception {

        VCFLineDataProcessor.bHasDotSlashDotSampleData = false;
        int[] iByPipe;

        if (sProcessStringIn.equalsIgnoreCase("./.") || sProcessStringIn.equalsIgnoreCase(".")) {
            iByPipe = new int[] { 0, 0 };
            VCFLineDataProcessor.bHasDotSlashDotSampleData = true;
        } else {
            String[] sByColon = sProcessStringIn.split(":");

            String[] sByPipe = sByColon[VCFLineDataProcessor.iGenotypeIndex].split("[|]|[/]");

            iByPipe = new int[sByPipe.length];
            iByPipe[0] = Integer.parseInt(sByPipe[0]);
            iByPipe[1] = Integer.parseInt(sByPipe[1]);
        }
        return iByPipe;
    }

    public boolean hasDotSlashDotSampleData() {
        return VCFLineDataProcessor.bHasDotSlashDotSampleData;
    }

}
