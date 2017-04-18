package org.renci.seqtools.converter;

import java.util.ArrayList;
import java.util.List;

public class VCFLineHolder {

    private List<String> tPreviousLineList;

    private List<String> tCurrentLineList;

    private List<String> tNextLineList;

    private List<String> tLastProcessedList;

    private String sPreviousLine;

    private String sCurrentLine;

    private String sNextLine;

    private String sLastProcessedLine;

    public String getPreviousLine() {
        return sPreviousLine;
    }

    public void setPreviousLine(String sPreviousLine) {
        this.sPreviousLine = sPreviousLine;
    }

    public String getCurrentLine() {
        return sCurrentLine;
    }

    public void setCurrentLine(String sCurrentLineIN) {
        this.sCurrentLine = sCurrentLineIN;
    }

    public String getNextLine() {
        return sNextLine;
    }

    public void setNextLine(String sNextLineIn) {
        this.sNextLine = sNextLineIn;
    }

    public String getLastProcessedLine() {
        return this.sLastProcessedLine;
    }

    public void setLastProcessedLine(String sLastProcessedLineIn) {
        this.sLastProcessedLine = sLastProcessedLineIn;
    }

    private VCFLineHolder(List<String> tPreviousLineListIn, List<String> tFirstLineListIn, List<String> tSecondLineListIn,
            String sPreviousLineIn, String sFirstLineIn, String sSecondLineIn) {
        this.tPreviousLineList = new ArrayList<String>(tPreviousLineListIn);
        this.tCurrentLineList = new ArrayList<String>(tFirstLineListIn);
        this.tNextLineList = new ArrayList<String>(tSecondLineListIn);
        this.sPreviousLine = sPreviousLineIn;
        this.sCurrentLine = sFirstLineIn;
        this.sNextLine = sSecondLineIn;
    }

    private VCFLineHolder() {
        this.tPreviousLineList = new ArrayList<String>();
        this.tCurrentLineList = new ArrayList<String>();
        this.tNextLineList = new ArrayList<String>();
        this.sPreviousLine = "";
        this.sCurrentLine = "";
        this.sNextLine = "";

    }

    public static VCFLineHolder getInstance(List<String> tPreviousLineListIn, List<String> tFirstLineListIn, List<String> tSecondLineListIn,
            String sPreviousLineIn, String sFirstLineIn, String sSecondLineIn) {
        return new VCFLineHolder(tPreviousLineListIn, tFirstLineListIn, tSecondLineListIn, sPreviousLineIn, sFirstLineIn, sSecondLineIn);
    }

    public static VCFLineHolder getInstance() {
        return new VCFLineHolder();
    }

    public boolean arePreviousAndCurrentLinePositionsEqual() {
        return this.tCurrentLineList.get(1).equalsIgnoreCase(this.tPreviousLineList.get(1));
    }

    public boolean arePreviousAndNextLinePositionsEqual() {
        return this.tNextLineList.get(1).equalsIgnoreCase(this.tPreviousLineList.get(1));
    }

    public boolean areCurrentAndNextPositionsEqual() {
        return this.tNextLineList.get(1).equalsIgnoreCase(this.tCurrentLineList.get(1));
    }

    public void setAll(List<String> tPreviousLineListIn, List<String> tFirstLineListIn, List<String> tSecondLineListIn,
            String sPreviousLineIn, String sFirstLineIn, String sSecondLineIn) {
        this.tPreviousLineList = new ArrayList<String>(tPreviousLineListIn);
        this.tCurrentLineList = new ArrayList<String>(tFirstLineListIn);
        this.tNextLineList = new ArrayList<String>(tSecondLineListIn);
        this.sPreviousLine = sPreviousLineIn;
        this.sCurrentLine = sFirstLineIn;
        this.sNextLine = sSecondLineIn;
    }

    public List<String> getPreviousLineList() {
        return tPreviousLineList;
    }

    public void setPreviousLineList(List<String> tPreviousLineList) {
        this.tPreviousLineList = new ArrayList<String>(tPreviousLineList);
    }

    public List<String> getCurrentLineList() {
        return tCurrentLineList;
    }

    public void setCurrentLineList(List<String> tFirstLineList) {
        this.tCurrentLineList = new ArrayList<String>(tFirstLineList);
    }

    public List<String> getNextLineList() {
        return tNextLineList;
    }

    public void setNextLineList(List<String> tSecondLineList) {
        this.tNextLineList = new ArrayList<String>(tSecondLineList);
    }

    public List<String> getLastProcessedLineList() {
        return this.tLastProcessedList;
    }

    public void setLastProcessedLineList(List<String> tLastProcessedLineListIn) {
        this.tLastProcessedList = new ArrayList<String>(tLastProcessedLineListIn);
    }

}
