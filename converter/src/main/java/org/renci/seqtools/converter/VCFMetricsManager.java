package org.renci.seqtools.converter;

/**
 * VCFMetricsManager
 * <p>
 * A metrics manager designed to collect statistics on VCF file elements: indels, snps, total positions processed.
 * 
 * @author k47k4705
 * 
 */
public class VCFMetricsManager {

    // Name of this manager (chromosome and sample name combined)
    private String sMetricsManagerName = null;

    // Total SNP count.
    private long lSNPCount = 0;

    // Total indel count.
    private long lIndelCount = 0;

    // Total positions count.
    private long lTotalPositionsCount = 0;

    // Internal read depth indicator
    private long lReadDepth = 0;;

    /**
     * VCFMetricsManager() no-arg constructor
     */
    private VCFMetricsManager(String sMetricsManagerNameIn) {
        this.sMetricsManagerName = sMetricsManagerNameIn;
    } // end constructor

    /**
     * getInstance()
     * <p>
     * Return an instance of this object.
     * 
     * @param sMetricsManagerNameIn
     *            -- the name of this object, a combination of chromosome and sample name combined.
     * @return VCFMetricsManager
     */
    public static VCFMetricsManager getInstance(String sMetricsManagerNameIn) {
        return new VCFMetricsManager(sMetricsManagerNameIn);
    } // end getInstance

    /**
     * incrementSNPCount()
     * <p>
     * Increment the number of SNPs.
     */
    public void incrementSNPCount() {
        this.lSNPCount++;
    } // end incrementSNPCount

    /**
     * getSNPCount()
     * <p>
     * Return the SNP total for this object.
     * 
     * @return int -- the snp total based on the internal snp long value.
     */
    public long getSNPCount() {
        return this.lSNPCount;
    } // end getSNPCount()

    /**
     * incrementIndelCount()
     * <p>
     * Increment the number of indels.
     */
    public void incrementIndelCount() {
        this.lIndelCount++;
    } // end incrementSNPCount

    /**
     * getIndelCount()
     * <p>
     * Return the indel total for this object.
     * 
     * @return long -- the indel total based on the internal indel long value.
     */
    public long getIndelCount() {
        return this.lIndelCount;
    } // end getIndelCount()

    /**
     * incrementTotalCount()
     * <p>
     * Increment the number of positions processed.
     */
    public void incrementTotalCount() {
        this.lTotalPositionsCount++;
    } // end incrementTotalCount

    /**
     * getTotalPositionsCount()
     * <p>
     * Return the positions total for this object.
     * 
     * @return int -- the positions total based on the internal positions long value.
     */
    public long getTotalPositionsCount() {
        return this.lTotalPositionsCount;
    } // end getTotalPositionsCount()

    /**
     * incrementReadDepth()
     * <p>
     * Add to the internal read depth for this chromosome marker.
     * 
     * @param lReadDepthCountIn
     */
    public void incrementReadDepth(long lReadDepthCountIn) {
        // Bumpt internal read depth tracker.
        this.lReadDepth += lReadDepthCountIn;

    } // end incrementReadDepth

    /**
     * getAverageReadDepth()
     * <p>
     * Return the average read depth contained by this object.
     * 
     * @return long -- average read depth for this chromosome marker.
     */
    public long getAverageReadDepth() {
        if (this.lTotalPositionsCount != 0) {
            return this.lReadDepth / this.lTotalPositionsCount;
        } else {
            return 0;
        }

    } // end getAverageReadDepth

} // end VCFMetricsManager
