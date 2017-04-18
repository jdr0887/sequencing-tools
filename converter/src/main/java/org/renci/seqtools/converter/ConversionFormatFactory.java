package org.renci.seqtools.converter;

public class ConversionFormatFactory {

    private ConversionFormatFactory() {
    }

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
    }

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
    }

}
