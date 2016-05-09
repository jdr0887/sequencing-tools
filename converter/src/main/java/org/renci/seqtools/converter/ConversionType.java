package org.renci.seqtools.converter;

/**
 * ConversionType
 * <p>
 * Type-safe enum. Distinguishes between the types of conversion files. Useful as a parameter so clients can determine
 * the conversion type without resorting to Strings as param args.
 * 
 * @author k47k4705
 * 
 */
public class ConversionType {

    private String sName;

    /**
     * ConversionType private arg constructor
     * 
     * @param sNameIn
     */
    private ConversionType(String sNameIn) {
        this.sName = sNameIn;
    } // end sNameIn

    public String toString() {
        return this.sName;
    } // end toString()

    // Process vcfs.
    public static ConversionType VCF = new ConversionType("BZIP2VCF");

    // Process pileups.
    public static ConversionType PILEUP = new ConversionType("BZIP2PILEUP");

} // end ConversionType
