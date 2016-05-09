package org.renci.seqtools.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * VCFDataManager
 * <p>
 * VCFDataManager is a convenience object for putting and getting columns and records and lines of VCF sample data.
 * <p>
 * We need an abstraction for getting one or many records per line of data, per column in that line.
 * <p>
 * Depending on insertions and deletions a column can produce one line of data (one record) or many lines of data (many
 * records).
 * <p>
 * The data manager has the task of making, housing, and allowing access to the contained data.
 * 
 * @author k47k4705
 * 
 */
public class VCFDataManager {

    // A Map of VCFRecords by column
    private Map<Integer, List<VCFRecord>> tMapOfRecords;

    /**
     * VCFDataManager private constructor
     */
    private VCFDataManager() {
        this.tMapOfRecords = new HashMap<Integer, List<VCFRecord>>();
    } // end VCFDataManager

    /**
     * getInstance()
     * <p>
     * Return an instance of this object.
     * 
     * @return VCFDataManager
     */
    public static VCFDataManager getInstance() {
        return new VCFDataManager();
    } // end getInstance

    /**
     * addRecord()
     * <p>
     * Add and store a record of VCF data by column.
     */
    public void addRecord(VCFRecord tRecordIn, int iColumnIn) {

        // Does this column have any records?
        if (!this.tMapOfRecords.containsKey(Integer.valueOf(iColumnIn))) {
            List<VCFRecord> tNewList = new ArrayList<VCFRecord>();
            tNewList.add(tRecordIn);
            this.tMapOfRecords.put(Integer.valueOf(iColumnIn), tNewList);
        } else {
            List<VCFRecord> tList = new ArrayList<VCFRecord>(this.tMapOfRecords.get(Integer.valueOf(iColumnIn)));
            tList.add(tRecordIn);
            this.tMapOfRecords.remove(Integer.valueOf(iColumnIn));
            this.tMapOfRecords.put(Integer.valueOf(iColumnIn), tList);
        } // end else
    } // end addRecord

    /**
     * getRecordList()
     * <P>
     * Return a list of records for this line of data by column.
     */
    public List<VCFRecord> getRecordList(int iColumnIn) {
        return this.tMapOfRecords.get(iColumnIn);
    } // end getRecordList

} // end VCFDataManager
