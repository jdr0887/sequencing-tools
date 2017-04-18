package org.renci.seqtools.converter;

import java.util.ArrayList;
import java.util.List;


public class VCFRecord {

    private int column;

    private String position;

    private String referenceGenotype;

    private String genotype;

    private String consensusquality;

    private String snpquality;

    private String mappingquality;

    private String readdepth;

    private String readbases;

    private String readqualityscores;

    private List<String> tListToReturn;

    private boolean bHasIndel;

    private boolean bHasSNP;

    private boolean bIsReverseStrand;

    private boolean bIsNoCall;

    private boolean bHasNoReferenceData;

    
    private VCFRecord(int iColumnIn, String sPositionIn, String sGenotypeIn, String sReferenceGenotypeIn, String sConsensusQualityIn,
            String sSNPQualityIn, String sMappingQualityIn, String sReadDepthIn, String sReadQualityScoresIn, String sReadBasesIn,
            boolean bHasIndelIn, boolean bHasSNPIn, boolean bIsReverseStrandIn, boolean bIsNoCallIn, boolean bHasNoReferenceDataIn) {
        this.column = iColumnIn;
        this.position = sPositionIn;
        this.genotype = sGenotypeIn;
        this.referenceGenotype = sReferenceGenotypeIn;
        this.consensusquality = sConsensusQualityIn;
        this.snpquality = sSNPQualityIn;
        this.mappingquality = sMappingQualityIn;
        this.readdepth = sReadDepthIn;
        this.readqualityscores = sReadQualityScoresIn;
        this.readbases = sReadBasesIn;
        this.bHasIndel = bHasIndelIn;
        this.bHasSNP = bHasSNPIn;
        this.bIsReverseStrand = bIsReverseStrandIn;
        this.bIsNoCall = bIsNoCallIn;
        this.bHasNoReferenceData = bHasNoReferenceDataIn;

        tListToReturn = new ArrayList<String>();

    } 

    
    public static VCFRecord getInstance(int iColumnIn, String sPositionIn, String sGenotypeIn, String sReferenceGenotypeIn,
            String sConsensusQualityIn, String sSNPQualityIn, String sMappingQualityIn, String sReadDepthIn, String sReadQualityScoresIn,
            String sReadBasesIn, boolean bHasIndelIn, boolean bHasSNPIn, boolean bIsReverseStrandIn, boolean bIsNoCallIn,
            boolean bHasNoReferenceDataIn) {
        return new VCFRecord(iColumnIn, sPositionIn, sGenotypeIn, sReferenceGenotypeIn, sConsensusQualityIn, sSNPQualityIn,
                sMappingQualityIn, sReadDepthIn, sReadQualityScoresIn, sReadBasesIn, bHasIndelIn, bHasSNPIn, bIsReverseStrandIn,
                bIsNoCallIn, bHasNoReferenceDataIn);
    } 

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public String getGenotype() {
        return genotype;
    }

    public void setGenotype(String genotype) {
        this.genotype = genotype;
    }

    public String getReferenceGenotype() {
        return this.referenceGenotype;
    }

    public void setReferenceGenotype(String refgenotype) {
        this.referenceGenotype = refgenotype;
    }

    public String getConsensusquality() {
        return consensusquality;
    }

    public void setConsensusquality(String consensusquality) {
        this.consensusquality = consensusquality;
    }

    public String getSnpquality() {
        return snpquality;
    }

    public void setSnpquality(String snpquality) {
        this.snpquality = snpquality;
    }

    public String getMappingquality() {
        return mappingquality;
    }

    public void setMappingquality(String mappingquality) {
        this.mappingquality = mappingquality;
    }

    public String getReaddepth() {
        return readdepth;
    }

    public void setReaddepth(String readdepth) {
        this.readdepth = readdepth;
    }

    public String getReadbases() {
        return readbases;
    }

    public void setReadbases(String readbases) {
        this.readbases = readbases;
    }

    public String getReadqualityScores() {
        return readqualityscores;
    }

    public void setReadqualityScores(String readquality) {
        this.readqualityscores = readquality;
    }

    public boolean isIndel() {
        return this.bHasIndel;
    } 

    public boolean isSNP() {
        return this.bHasSNP;
    } 

    public boolean isReverseStrand() {
        return this.bIsReverseStrand;
    } 

    public boolean isNoCall() {
        return this.bIsNoCall;
    } 

    public boolean hasNoReferenceData() {
        return this.bHasNoReferenceData;
    } 

    public List<String> getVCFRecordAsList() {
        this.tListToReturn.clear();
        tListToReturn.add(this.getPosition());
        tListToReturn.add(this.getGenotype());
        tListToReturn.add(this.getConsensusquality());
        tListToReturn.add(this.getSnpquality());
        tListToReturn.add(this.getMappingquality());
        tListToReturn.add(this.getReaddepth());
        tListToReturn.add(this.getReadbases());
        tListToReturn.add(this.getReadqualityScores());
        return tListToReturn;
    } 

} 
