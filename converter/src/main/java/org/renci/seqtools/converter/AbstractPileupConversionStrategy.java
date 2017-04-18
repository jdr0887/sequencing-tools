package org.renci.seqtools.converter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public abstract class AbstractPileupConversionStrategy implements IConversionStrategy {

    private static final String DASH = "-";

    private static final Properties tProperties = new Properties();

    public AbstractPileupConversionStrategy() {
        this.loadProperties();
    }

    private void loadProperties() {
        try {
            ClassLoader cl = AbstractPileupConversionStrategy.class.getClassLoader();
            InputStream is = cl.getResourceAsStream("org/renci/sequencing/converter/config.properties");
            tProperties.load(is);
            is.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getMasterFileName(String sPileupFileName) {
        String sFileMasterFileName = tProperties.getProperty("mprefix") + DASH + sPileupFileName + tProperties.getProperty("msuffix");
        return sFileMasterFileName;
    }

    public String getDetailFileName(String sPileupFileName) {
        String sFileDetailFileName = tProperties.getProperty("dprefix") + DASH + sPileupFileName + tProperties.getProperty("dsuffix");
        return sFileDetailFileName;
    }

    @Override
    public void convert() throws Exception {

    }

}
