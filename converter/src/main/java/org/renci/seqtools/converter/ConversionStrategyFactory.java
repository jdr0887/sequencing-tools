package org.renci.seqtools.converter;

import java.io.File;

public class ConversionStrategyFactory {

    private ConversionStrategyFactory() {
    }

    public static IConversionStrategy makeStrategy(IConversionFormat tFormatIn, ConversionType tConversionTypeIn, GenomeType tGenomeTypeIn,
            int iFileNumberIn, File tVCFOrPileupFileIn, File tBAMFileIn, File tOutputDirIn, File tMetricsFileIn, File tVariantOutVCFFile) {
        IConversionStrategy tStrategy = null;

        if (tConversionTypeIn == ConversionType.PILEUP) {
            tStrategy = new BZip2PileupConversionStrategy(tFormatIn, tVCFOrPileupFileIn, String.valueOf(iFileNumberIn), tGenomeTypeIn);
        } else if (tConversionTypeIn == ConversionType.VCF) {
            if (tMetricsFileIn == null) {
                File tBlankMetricsFile = null;
                tStrategy = new BZip2VCFConversionStrategy(tFormatIn, tVCFOrPileupFileIn, tBAMFileIn, tOutputDirIn,
                        String.valueOf(iFileNumberIn), tGenomeTypeIn, tBlankMetricsFile, tVariantOutVCFFile);
            } else {
                tStrategy = new BZip2VCFConversionStrategy(tFormatIn, tVCFOrPileupFileIn, tBAMFileIn, tOutputDirIn,
                        String.valueOf(iFileNumberIn), tGenomeTypeIn, tMetricsFileIn, tVariantOutVCFFile);
            }
        } else {
            tStrategy = new BZip2PileupConversionStrategy(tFormatIn, tVCFOrPileupFileIn, String.valueOf(iFileNumberIn), tGenomeTypeIn);
        }

        return tStrategy;
    }

}
