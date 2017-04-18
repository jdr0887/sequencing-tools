package org.renci.seqtools.converter;

public class ConversionType {

    private String sName;

    private ConversionType(String sNameIn) {
        this.sName = sNameIn;
    }

    public String toString() {
        return this.sName;
    }

    public static ConversionType VCF = new ConversionType("BZIP2VCF");

    public static ConversionType PILEUP = new ConversionType("BZIP2PILEUP");

}
