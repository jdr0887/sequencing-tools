package org.renci.seqtools.converter;

/**
 * IConversionStrategy
 * 
 * @author k47k4705
 *         <p>
 *         IConversionStrategy is an interface for objects that want to provide a way of converting pileup files from
 *         one format to another.
 *         <p>
 *         It extends Runnable so threading is possible.
 * 
 */
public interface IConversionStrategy extends Runnable {

    /**
     * convert()
     * <p>
     * Default method for conversion of pileup files to another format.
     */
    public void convert() throws Exception;

} // end IConversionStrategy
