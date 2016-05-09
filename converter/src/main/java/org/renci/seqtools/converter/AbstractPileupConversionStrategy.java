package org.renci.seqtools.converter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * AbstractPileupConversionStrategy
 * 
 * @author k47k4705
 *         <p>
 *         AbstractPileupConversionStrategy is an implementation of a pileup conversion strategy.
 *         <p>
 *         This object is a house-keeping object and meta object. It sets up and reads a configuration Property file.
 *         And, thanks to the Properties file, it sets the name of the output master and detail consensus files.
 *         <p>
 *         Thinking about putting Properties file methods here so the underlying Strategy objects can get back the
 *         Master binary filename and detail binary filename
 * 
 */
public abstract class AbstractPileupConversionStrategy implements IConversionStrategy {

    // A dash between file parts.
    private static final String DASH = "-";

    // Properties file describing master/detail file prefixes and suffixes.
    private static final Properties tProperties = new Properties();

    /**
     * AbstractPileupConversionStrategy private constructor
     */
    public AbstractPileupConversionStrategy() {
        // Load properties file.
        this.loadProperties();
    } // end AbstractPileupConversionStrategy

    /**
     * loadProperties()
     * <p>
     * loadProperties() loads a Properties file.
     */
    private void loadProperties() {

        // Setup properties file.
        try {

            ClassLoader cl = AbstractPileupConversionStrategy.class.getClassLoader();
            InputStream is = cl.getResourceAsStream("org/renci/sequencing/converter/config.properties");
            tProperties.load(is);
            is.close();

            // tProperties.load(new FileInputStream("config.properties"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * getMasterFileName()
     * <p>
     * getMasterFileName() returns the name of the Master file to create.
     * 
     * @param sPileupFileName
     *            -- name of the pileup file
     * @return sMasterFileName -- name of the Master file.
     */
    public String getMasterFileName(String sPileupFileName) {
        String sFileMasterFileName = tProperties.getProperty("mprefix") + DASH + sPileupFileName + tProperties.getProperty("msuffix");
        return sFileMasterFileName;
    } // end getMasterFileName()

    /**
     * getDetailFileName()
     * <p>
     * getDetailFileName() returns the name of the detail file.
     * 
     * @param sPileupFileName
     *            -- name of the pileup file
     * @return sDetailFileName -- name of the detail file.
     */
    public String getDetailFileName(String sPileupFileName) {
        String sFileDetailFileName = tProperties.getProperty("dprefix") + DASH + sPileupFileName + tProperties.getProperty("dsuffix");
        return sFileDetailFileName;
    } // end getDetailFileName

    @Override
    public void convert() throws Exception {

    } // end convert

} // end AbstractPileupConversionStrategy
