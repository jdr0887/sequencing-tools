package org.renci.seqtools.converter;

/**
 * ConversionFormatFactory
 * <p>
 * A Factory object used to make conversion formats for consensus files.
 * BZip2VCFConversionExomeFormat/BZip2VCFConversionGenomeFormat, etc.
 * 
 * @author k47k4705
 * 
 */
public class ConversionFormatFactory {

    /**
     * ConversionFormatFactory private constructor
     */
    private ConversionFormatFactory() {
    }

    /**
     * makePileupFormat()
     * <p>
     * Makes the appropriate format object given a conversion type to make.
     * 
     * @param tConversionType
     *            -- the type of conversion strategy to make.
     * @return tConversionStrategy -- an interface fronting for the "right" conversion strategy.
     */
    public static IConversionFormat makePileupFormat(GenomeType tGenomeTypeIn) {
        IConversionFormat tFormat = null;

        if (tGenomeTypeIn == GenomeType.WHOLE_GENOME) {
            tFormat = new BZip2PileupConversionFormat();
        } else if (tGenomeTypeIn == GenomeType.EXOMIC_GENOME) {
            tFormat = new BZip2PileupConversionFormat();
        } else {
            tFormat = new BZip2PileupConversionFormat();
        }

        return tFormat;
    } // end makePileupFormat

    /**
     * makeVCFFormat()
     * 
     * @param tGenomeTypeIn
     * @return
     */
    public static IConversionFormat makeVCFFormat(GenomeType tGenomeTypeIn, boolean bIsInTestMode) {
        IConversionFormat tFormat = null;

        if (tGenomeTypeIn == GenomeType.WHOLE_GENOME) {
            System.out.println("Making VCF whole genome format.");
            tFormat = new BZip2VCFConversionFormat(bIsInTestMode);
        } else if (tGenomeTypeIn == GenomeType.EXOMIC_GENOME) {
            System.out.println("Making VCF exome format.");
            tFormat = new BZip2VCFExomeConversionFormat();
        } else {
            tFormat = new BZip2VCFConversionFormat(bIsInTestMode);
        }

        return tFormat;
    } // end makeVCFFormat

} // end ConversionFormatFactory
