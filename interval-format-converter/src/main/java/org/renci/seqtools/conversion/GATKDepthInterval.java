package org.renci.seqtools.conversion;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;

public class GATKDepthInterval implements Serializable, Comparable<GATKDepthInterval> {

    private static final long serialVersionUID = 7645760450620588143L;

    private static final Pattern targetPattern = Pattern.compile("(?<contig>.+):(?<start>\\d+)-?(?<end>\\d+)?");

    private String contig;

    private Integer startPosition;

    private Integer endPosition;

    private AtomicInteger totalCoverage = new AtomicInteger(0);

    private Double averageCoverage;

    private Integer sampleTotalCoverage;

    private Double sampleMeanCoverage;

    private String sampleGranularQ1;

    private String sampleGranularMedian;

    private String sampleGranularQ3;

    private Double samplePercentAbove1 = 0D;

    private AtomicInteger sampleCountAbove1 = new AtomicInteger(0);

    private Double samplePercentAbove2 = 0D;

    private AtomicInteger sampleCountAbove2 = new AtomicInteger(0);

    private Double samplePercentAbove5 = 0D;

    private AtomicInteger sampleCountAbove5 = new AtomicInteger(0);

    private Double samplePercentAbove8 = 0D;

    private AtomicInteger sampleCountAbove8 = new AtomicInteger(0);

    private Double samplePercentAbove10 = 0D;

    private AtomicInteger sampleCountAbove10 = new AtomicInteger(0);

    private Double samplePercentAbove15 = 0D;

    private AtomicInteger sampleCountAbove15 = new AtomicInteger(0);

    private Double samplePercentAbove20 = 0D;

    private AtomicInteger sampleCountAbove20 = new AtomicInteger(0);

    private Double samplePercentAbove30 = 0D;

    private AtomicInteger sampleCountAbove30 = new AtomicInteger(0);

    private Double samplePercentAbove50 = 0D;

    private AtomicInteger sampleCountAbove50 = new AtomicInteger(0);

    public GATKDepthInterval() {
        super();
    }

    public GATKDepthInterval(String line) {
        super();
        String[] split = line.split("\t");

        if (split.length == 4) {
            this.contig = split[0];
            this.startPosition = Integer.valueOf(split[1]);
            this.endPosition = Integer.valueOf(split[2]);
        }

        if (split.length == 1) {
            Matcher m = targetPattern.matcher(line);
            if (m.matches()) {
                this.contig = m.group("contig");
                this.startPosition = Integer.valueOf(m.group("start"));
                this.endPosition = Integer.valueOf(m.group("end"));
            }
        }

        if (split.length == 17) {
            String target = split[0];
            Matcher m = targetPattern.matcher(target);
            if (m.matches()) {
                this.contig = m.group("contig");
                this.startPosition = Integer.valueOf(m.group("start"));
                this.endPosition = Integer.valueOf(m.group("end"));
            }
            this.totalCoverage = new AtomicInteger(Integer.valueOf(split[1]));
            this.averageCoverage = Double.valueOf(split[2]);
            this.sampleTotalCoverage = Integer.valueOf(split[3]);
            this.sampleMeanCoverage = Double.valueOf(split[4]);
            this.sampleGranularQ1 = split[5];
            this.sampleGranularMedian = split[6];
            this.sampleGranularQ3 = split[7];
            this.samplePercentAbove1 = Double.valueOf(split[8]);
            this.samplePercentAbove2 = Double.valueOf(split[9]);
            this.samplePercentAbove5 = Double.valueOf(split[10]);
            this.samplePercentAbove8 = Double.valueOf(split[11]);
            this.samplePercentAbove10 = Double.valueOf(split[12]);
            this.samplePercentAbove15 = Double.valueOf(split[13]);
            this.samplePercentAbove20 = Double.valueOf(split[14]);
            this.samplePercentAbove30 = Double.valueOf(split[15]);
            this.samplePercentAbove50 = Double.valueOf(split[16]);
        }
    }

    public String getContig() {
        return contig;
    }

    public void setContig(String contig) {
        this.contig = contig;
    }

    public Integer getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(Integer startPosition) {
        this.startPosition = startPosition;
    }

    public Integer getEndPosition() {
        return endPosition;
    }

    public void setEndPosition(Integer endPosition) {
        this.endPosition = endPosition;
    }

    public AtomicInteger getTotalCoverage() {
        return totalCoverage;
    }

    public Double getAverageCoverage() {
        return this.averageCoverage;
    }

    public void setAverageCoverage(Double averageCoverage) {
        this.averageCoverage = averageCoverage;
    }

    public Integer getSampleTotalCoverage() {
        return sampleTotalCoverage;
    }

    public void setSampleTotalCoverage(Integer sampleTotalCoverage) {
        this.sampleTotalCoverage = sampleTotalCoverage;
    }

    public Double getSampleMeanCoverage() {
        return sampleMeanCoverage;
    }

    public void setSampleMeanCoverage(Double sampleMeanCoverage) {
        this.sampleMeanCoverage = sampleMeanCoverage;
    }

    public String getSampleGranularQ1() {
        return sampleGranularQ1;
    }

    public void setSampleGranularQ1(String sampleGranularQ1) {
        this.sampleGranularQ1 = sampleGranularQ1;
    }

    public String getSampleGranularMedian() {
        return sampleGranularMedian;
    }

    public void setSampleGranularMedian(String sampleGranularMedian) {
        this.sampleGranularMedian = sampleGranularMedian;
    }

    public String getSampleGranularQ3() {
        return sampleGranularQ3;
    }

    public void setSampleGranularQ3(String sampleGranularQ3) {
        this.sampleGranularQ3 = sampleGranularQ3;
    }

    public Double getSamplePercentAbove1() {
        return this.samplePercentAbove1;
    }

    public void setSamplePercentAbove1(Double samplePercentAbove1) {
        this.samplePercentAbove1 = samplePercentAbove1;
    }

    public Double getSamplePercentAbove2() {
        return this.samplePercentAbove2;
    }

    public void setSamplePercentAbove2(Double samplePercentAbove2) {
        this.samplePercentAbove2 = samplePercentAbove2;
    }

    public Double getSamplePercentAbove5() {
        return this.samplePercentAbove5;
    }

    public void setSamplePercentAbove5(Double samplePercentAbove5) {
        this.samplePercentAbove5 = samplePercentAbove5;
    }

    public Double getSamplePercentAbove8() {
        return this.samplePercentAbove8;
    }

    public void setSamplePercentAbove8(Double samplePercentAbove8) {
        this.samplePercentAbove8 = samplePercentAbove8;
    }

    public Double getSamplePercentAbove10() {
        return this.samplePercentAbove10;
    }

    public void setSamplePercentAbove10(Double samplePercentAbove10) {
        this.samplePercentAbove10 = samplePercentAbove10;
    }

    public Double getSamplePercentAbove15() {
        return this.samplePercentAbove15;
    }

    public void setSamplePercentAbove15(Double samplePercentAbove15) {
        this.samplePercentAbove15 = samplePercentAbove15;
    }

    public Double getSamplePercentAbove20() {
        return this.samplePercentAbove20;
    }

    public void setSamplePercentAbove20(Double samplePercentAbove20) {
        this.samplePercentAbove20 = samplePercentAbove20;
    }

    public Double getSamplePercentAbove30() {
        return this.samplePercentAbove30;
    }

    public void setSamplePercentAbove30(Double samplePercentAbove30) {
        this.samplePercentAbove30 = samplePercentAbove30;
    }

    public Double getSamplePercentAbove50() {
        return this.samplePercentAbove50;
    }

    public void setSamplePercentAbove50(Double samplePercentAbove50) {
        this.samplePercentAbove50 = samplePercentAbove50;
    }

    public Range<Integer> getPositionRange() {
        return Range.between(this.startPosition, this.endPosition);
    }

    public Integer getLength() {
        return this.endPosition - this.startPosition + 1;
    }

    public AtomicInteger getSampleCountAbove1() {
        return sampleCountAbove1;
    }

    public AtomicInteger getSampleCountAbove2() {
        return sampleCountAbove2;
    }

    public AtomicInteger getSampleCountAbove5() {
        return sampleCountAbove5;
    }

    public AtomicInteger getSampleCountAbove8() {
        return sampleCountAbove8;
    }

    public AtomicInteger getSampleCountAbove10() {
        return sampleCountAbove10;
    }

    public AtomicInteger getSampleCountAbove15() {
        return sampleCountAbove15;
    }

    public AtomicInteger getSampleCountAbove20() {
        return sampleCountAbove20;
    }

    public AtomicInteger getSampleCountAbove30() {
        return sampleCountAbove30;
    }

    public AtomicInteger getSampleCountAbove50() {
        return sampleCountAbove50;
    }

    @Override
    public int compareTo(GATKDepthInterval o) {
        int ret = 0;
        if (StringUtils.isNotEmpty(this.contig)) {
            ret = this.contig.compareTo(o.getContig());
            if (ret == 0) {
                ret = this.startPosition.compareTo(o.getStartPosition());
            }
        }
        return ret;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((contig == null) ? 0 : contig.hashCode());
        result = prime * result + ((endPosition == null) ? 0 : endPosition.hashCode());
        result = prime * result + ((startPosition == null) ? 0 : startPosition.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GATKDepthInterval other = (GATKDepthInterval) obj;
        if (contig == null) {
            if (other.contig != null)
                return false;
        } else if (!contig.equals(other.contig))
            return false;
        if (endPosition == null) {
            if (other.endPosition != null)
                return false;
        } else if (!endPosition.equals(other.endPosition))
            return false;
        if (startPosition == null) {
            if (other.startPosition != null)
                return false;
        } else if (!startPosition.equals(other.startPosition))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return String.format("%s\t%s\t%.2f\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s%n",
                String.format("%s:%d-%d", contig, startPosition, endPosition), totalCoverage, averageCoverage, sampleTotalCoverage,
                sampleMeanCoverage, sampleGranularQ1, sampleGranularMedian, sampleGranularQ3, samplePercentAbove1, samplePercentAbove2,
                samplePercentAbove5, samplePercentAbove8, samplePercentAbove10, samplePercentAbove15, samplePercentAbove20,
                samplePercentAbove30, samplePercentAbove50);
    }

    public String toStringTrimmed() {
        return String.format("%s\t%d\t%.2f\t%.2f\t%.2f\t%.2f\t%.2f\t%.2f\t%.2f\t%.2f\t%.2f\t%.2f%n",
                String.format("%s:%d-%d", contig, startPosition, endPosition), totalCoverage.get(),
                Double.valueOf(1D * getTotalCoverage().get() / getLength()),
                Double.valueOf(100D * getSampleCountAbove1().get() / getLength()),
                Double.valueOf(100D * getSampleCountAbove2().get() / getLength()),
                Double.valueOf(100D * getSampleCountAbove5().get() / getLength()),
                Double.valueOf(100D * getSampleCountAbove8().get() / getLength()),
                Double.valueOf(100D * getSampleCountAbove10().get() / getLength()),
                Double.valueOf(100D * getSampleCountAbove15().get() / getLength()),
                Double.valueOf(100D * getSampleCountAbove20().get() / getLength()),
                Double.valueOf(100D * getSampleCountAbove30().get() / getLength()),
                Double.valueOf(100D * getSampleCountAbove50().get() / getLength()));
    }

}
