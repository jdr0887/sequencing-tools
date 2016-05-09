package org.renci.seqtools.converter;

import java.io.File;

/**
 * ConversionStrategyFactory
 * <p>
 * A Factory object used to make conversion strategies for consensus files.
 * BZip2VCFConversionStrategy/BZip2PileupConversionStrategy, etc.
 * 
 * @author k47k4705
 * 
 */
public class ConversionStrategyFactory {

    /**
     * ConversionStrategyFactory private constructor
     */
    private ConversionStrategyFactory() {
    }

    /**
     * makeStrategy()
     * <p>
     * Makes the appropriate strategy object given a convesion type to make.
     * 
     * @param tConversionType
     *            -- the type of conversion strategy to make.
     * @return tConversionStrategy -- an interface fronting for the "right" conversion strategy.
     */
    public static IConversionStrategy makeStrategy(IConversionFormat tFormatIn, ConversionType tConversionTypeIn, GenomeType tGenomeTypeIn,
            int iFileNumberIn, File tVCFOrPileupFileIn, File tBAMFileIn, File tOutputDirIn, File tMetricsFileIn, File tVariantOutVCFFile) {
        IConversionStrategy tStrategy = null;

        // Which type do we have?
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
    } // end makeStrategy

} // end ConversionStrategyFactory
