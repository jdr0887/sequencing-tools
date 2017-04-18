package org.renci.seqtools.conversion;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SAMToolsDepthInterval {

    private static final Pattern p = Pattern.compile("(?<contig>\\S+)\t(?<position>\\S+)\t(?<coverage>\\S+)");

    private String contig;

    private Integer position;

    private Integer coverage;

    public SAMToolsDepthInterval() {
        super();
    }

    public SAMToolsDepthInterval(String contig, Integer position, Integer coverage) {
        super();
        this.contig = contig;
        this.position = position;
        this.coverage = coverage;
    }

    public SAMToolsDepthInterval(String line) {
        super();
        Matcher m = p.matcher(line);
        if (m.find()) {
            contig = m.group("contig");
            position = Integer.valueOf(m.group("position"));
            coverage = Integer.valueOf(m.group("coverage"));
        }
        // String[] split = line.split("\t");
        // if (split.length == 3) {
        // this.contig = split[0];
        // this.position = Integer.valueOf(split[1]);
        // this.coverage = Integer.valueOf(split[2]);
        // }
    }

    public String getContig() {
        return contig;
    }

    public void setContig(String contig) {
        this.contig = contig;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Integer getCoverage() {
        return coverage;
    }

    public void setCoverage(Integer coverage) {
        this.coverage = coverage;
    }

    @Override
    public String toString() {
        return String.format("SAMToolsDepthInterval [contig=%s, position=%s, coverage=%s]", contig, position, coverage);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((contig == null) ? 0 : contig.hashCode());
        result = prime * result + ((coverage == null) ? 0 : coverage.hashCode());
        result = prime * result + ((position == null) ? 0 : position.hashCode());
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
        SAMToolsDepthInterval other = (SAMToolsDepthInterval) obj;
        if (contig == null) {
            if (other.contig != null)
                return false;
        } else if (!contig.equals(other.contig))
            return false;
        if (coverage == null) {
            if (other.coverage != null)
                return false;
        } else if (!coverage.equals(other.coverage))
            return false;
        if (position == null) {
            if (other.position != null)
                return false;
        } else if (!position.equals(other.position))
            return false;
        return true;
    }

}
