package org.renci.seqtools.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VCFDataManager {

    private Map<Integer, List<VCFRecord>> tMapOfRecords;

    private VCFDataManager() {
        this.tMapOfRecords = new HashMap<Integer, List<VCFRecord>>();
    }

    public static VCFDataManager getInstance() {
        return new VCFDataManager();
    }

    public void addRecord(VCFRecord tRecordIn, int iColumnIn) {

        if (!this.tMapOfRecords.containsKey(Integer.valueOf(iColumnIn))) {
            List<VCFRecord> tNewList = new ArrayList<VCFRecord>();
            tNewList.add(tRecordIn);
            this.tMapOfRecords.put(Integer.valueOf(iColumnIn), tNewList);
        } else {
            List<VCFRecord> tList = new ArrayList<VCFRecord>(this.tMapOfRecords.get(Integer.valueOf(iColumnIn)));
            tList.add(tRecordIn);
            this.tMapOfRecords.remove(Integer.valueOf(iColumnIn));
            this.tMapOfRecords.put(Integer.valueOf(iColumnIn), tList);
        }
    }

    public List<VCFRecord> getRecordList(int iColumnIn) {
        return this.tMapOfRecords.get(iColumnIn);
    }

}
