package org.renci.seqtools.converter;

/**
 * GenomeType is a type-safe enum pattern class.
 * <p>
 * Clients use this object to determine which type of genome and data they want to process.
 * <p>
 * Like: GenomeType.WHOLE_GENOME and GenomeType.EXOMIC_GENOME
 * <p>
 * It serves as a simple enum or type indicator object.
 * 
 * @author k47k4705
 * 
 */
public class GenomeType {

    // Process whole genome pileups.
    public static GenomeType WHOLE_GENOME = new GenomeType("WHOLE_GENOME");

    // Process exomic pileups.
    public static GenomeType EXOMIC_GENOME = new GenomeType("EXOMIC_GENOME");

    private String sName;

    /**
     * GenomeType private arg constructor
     * 
     * @param sNameIn
     */
    private GenomeType(String sNameIn) {
        this.sName = sNameIn;
    } // end sNameIn

    public String toString() {
        return this.sName;
    }

}
