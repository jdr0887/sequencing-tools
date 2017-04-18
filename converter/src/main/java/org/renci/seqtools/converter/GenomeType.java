package org.renci.seqtools.converter;

public class GenomeType {

    public static GenomeType WHOLE_GENOME = new GenomeType("WHOLE_GENOME");

    public static GenomeType EXOMIC_GENOME = new GenomeType("EXOMIC_GENOME");

    private String sName;

    private GenomeType(String sNameIn) {
        this.sName = sNameIn;
    }

    public String toString() {
        return this.sName;
    }

}
