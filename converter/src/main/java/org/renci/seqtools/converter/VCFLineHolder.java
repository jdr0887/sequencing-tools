package org.renci.seqtools.converter;

import java.util.ArrayList;
import java.util.List;

/**
 * A data structure for holding three VCF lines at a time.
 * <p>
 * VCF files can contain duplicate positions. Usually the duplicate position (second position) is an indel. We need a
 * look-back ability.
 * <p>
 * The goal is to keep a buffer of VCF lines so we can look-back at the previous line and see if the current position is
 * a duplicate.
 * <p>
 * When we determine an indel does or does not exist, we then write out the correct line, push a new previous line into
 * this object and roll on.
 * 
 * @author k47k4705
 * 
 */
public class VCFLineHolder {

    // Previous line list (before the first line).
    private List<String> tPreviousLineList;

    // First line list.
    private List<String> tCurrentLineList;

    // Second line list.
    private List<String> tNextLineList;

    // Last processed List.
    private List<String> tLastProcessedList;

    // Previous VCF line as a String.
    private String sPreviousLine;

    // First VCF line as a String.
    private String sCurrentLine;

    // Second VCF line as a String.
    private String sNextLine;

    // Last processed line.
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
    } // end getLastProcessedLine

    public void setLastProcessedLine(String sLastProcessedLineIn) {
        this.sLastProcessedLine = sLastProcessedLineIn;
    } // end setLastProcessedLine

    /**
     * VCFLineHolder private arg constructor
     */
    private VCFLineHolder(List<String> tPreviousLineListIn, List<String> tFirstLineListIn, List<String> tSecondLineListIn,
            String sPreviousLineIn, String sFirstLineIn, String sSecondLineIn) {
        this.tPreviousLineList = new ArrayList<String>(tPreviousLineListIn);
        this.tCurrentLineList = new ArrayList<String>(tFirstLineListIn);
        this.tNextLineList = new ArrayList<String>(tSecondLineListIn);
        this.sPreviousLine = sPreviousLineIn;
        this.sCurrentLine = sFirstLineIn;
        this.sNextLine = sSecondLineIn;
    } // end VCFLineHolder

    /**
     * VCFLineHolder private no-arg constructor
     */
    private VCFLineHolder() {
        this.tPreviousLineList = new ArrayList<String>();
        this.tCurrentLineList = new ArrayList<String>();
        this.tNextLineList = new ArrayList<String>();
        this.sPreviousLine = "";
        this.sCurrentLine = "";
        this.sNextLine = "";

    } // end VCFLineHolder

    /**
     * getInstance()
     * <p>
     * Return a VCFLineHolder instance.
     * 
     * @return VCFLineHolder
     */
    public static VCFLineHolder getInstance(List<String> tPreviousLineListIn, List<String> tFirstLineListIn, List<String> tSecondLineListIn,
            String sPreviousLineIn, String sFirstLineIn, String sSecondLineIn) {
        return new VCFLineHolder(tPreviousLineListIn, tFirstLineListIn, tSecondLineListIn, sPreviousLineIn, sFirstLineIn, sSecondLineIn);
    } // end getInstance()

    /**
     * getInstance()
     * <p>
     * Return a VCFLineHolder instance.
     * 
     * @return VCFLineHolder
     */
    public static VCFLineHolder getInstance() {
        return new VCFLineHolder();
    } // end getInstance()

    /**
     * arePreviousAndCurrentLinePositionsEqual()
     * <p>
     * Determines if the previous position equals the first position in the previous and first lists
     * 
     * @return boolean -- true, positions are equal, false, positions are not equal.
     */
    public boolean arePreviousAndCurrentLinePositionsEqual() {
        return this.tCurrentLineList.get(1).equalsIgnoreCase(this.tPreviousLineList.get(1));
    } // end arePreviousFirst

    /**
     * arePreviousAndNextLinePositionsEqual()
     */
    public boolean arePreviousAndNextLinePositionsEqual() {
        return this.tNextLineList.get(1).equalsIgnoreCase(this.tPreviousLineList.get(1));
    } // end arePreviousSecond

    /**
     * areCurrentNextPositionsEqual()
     */
    public boolean areCurrentAndNextPositionsEqual() {
        return this.tNextLineList.get(1).equalsIgnoreCase(this.tCurrentLineList.get(1));
    } // end areFirstSecond

    /**
     * setLines()
     * <p>
     * Set all the line lists at once.
     */
    public void setAll(List<String> tPreviousLineListIn, List<String> tFirstLineListIn, List<String> tSecondLineListIn,
            String sPreviousLineIn, String sFirstLineIn, String sSecondLineIn) {
        this.tPreviousLineList = new ArrayList<String>(tPreviousLineListIn);
        this.tCurrentLineList = new ArrayList<String>(tFirstLineListIn);
        this.tNextLineList = new ArrayList<String>(tSecondLineListIn);
        this.sPreviousLine = sPreviousLineIn;
        this.sCurrentLine = sFirstLineIn;
        this.sNextLine = sSecondLineIn;
    } // end VCFLineHolder

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

} // end VCFLineHolder
