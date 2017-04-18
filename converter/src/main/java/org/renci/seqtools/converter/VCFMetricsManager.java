package org.renci.seqtools.converter;

public class VCFMetricsManager {

    private String sMetricsManagerName = null;

    private long lSNPCount = 0;

    private long lIndelCount = 0;

    private long lTotalPositionsCount = 0;

    private long lReadDepth = 0;;

    private VCFMetricsManager(String sMetricsManagerNameIn) {
        this.sMetricsManagerName = sMetricsManagerNameIn;
    }

    public static VCFMetricsManager getInstance(String sMetricsManagerNameIn) {
        return new VCFMetricsManager(sMetricsManagerNameIn);
    }

    public void incrementSNPCount() {
        this.lSNPCount++;
    }

    public long getSNPCount() {
        return this.lSNPCount;
    }

    public void incrementIndelCount() {
        this.lIndelCount++;
    }

    public long getIndelCount() {
        return this.lIndelCount;
    }

    public void incrementTotalCount() {
        this.lTotalPositionsCount++;
    }

    public long getTotalPositionsCount() {
        return this.lTotalPositionsCount;
    }

    public void incrementReadDepth(long lReadDepthCountIn) {
        this.lReadDepth += lReadDepthCountIn;
    }

    public long getAverageReadDepth() {
        if (this.lTotalPositionsCount != 0) {
            return this.lReadDepth / this.lTotalPositionsCount;
        } else {
            return 0;
        }

    }

}
