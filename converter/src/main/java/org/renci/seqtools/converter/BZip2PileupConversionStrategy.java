package org.renci.seqtools.converter;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.lang3.text.StrTokenizer;

public class BZip2PileupConversionStrategy extends AbstractPileupConversionStrategy implements IConversionStrategy, Runnable {

    private File tInputPileupFile;

    private DataOutputStream tMasterDataOutputStream;

    private DataOutputStream tDetailDataOutputStream;

    private IConversionFormat tConversionFormat;

    private String sName;

    private boolean bSawAnIndel = false;

    public BZip2PileupConversionStrategy(IConversionFormat tFormatIn, File tInputPileupFileIn, String sNameIn, GenomeType tGenomeTypeIn) {

        this.tInputPileupFile = tInputPileupFileIn;
        this.sName = sNameIn;
        this.tConversionFormat = tFormatIn;

    }

    public String toString() {
        return this.sName + " created.";
    }

    public void run() {
        System.out.println(Thread.currentThread().getName() + " executing " + this);
        this.convert();
    }

    public void convert() {

        String sFullFileName = this.tInputPileupFile.getName();
        String sBaseFileName = this.getBaseName(sFullFileName);

        String sMasterFileName = getMasterFileName(sBaseFileName);

        String sDetailFileName = getDetailFileName(sBaseFileName);

        try {

            String sMasterPathAndFile = this.tInputPileupFile.getParent() + this.tInputPileupFile.separatorChar + sMasterFileName;
            String sDetailPathAndFile = this.tInputPileupFile.getParent() + this.tInputPileupFile.separatorChar + sDetailFileName;

            File tMasterFile = new File(sMasterPathAndFile);
            File tDetailFile = new File(sDetailPathAndFile);

            if (tMasterFile.exists() && tDetailFile.exists()) {

                tMasterFile.delete();
                tDetailFile.delete();
            } else if (tMasterFile.exists() && !tDetailFile.exists()) {
                tMasterFile.delete();
            } else if (!tMasterFile.exists() && tDetailFile.exists()) {
                tDetailFile.delete();
            }

            tMasterFile.createNewFile();
            tDetailFile.createNewFile();

            System.out.println("\tWriting to master file: " + tMasterFile.getAbsolutePath());
            System.out.println("\tWriting to detail file: " + tDetailFile.getAbsolutePath());

            this.tMasterDataOutputStream = new DataOutputStream(
                    new BufferedOutputStream(new BZip2CompressorOutputStream(new FileOutputStream(tMasterFile))));

            this.tDetailDataOutputStream = new DataOutputStream(
                    new BufferedOutputStream(new BZip2CompressorOutputStream(new FileOutputStream(tDetailFile))));

            File tPileupFileToRead = this.tInputPileupFile;

            BufferedReader tBufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(tPileupFileToRead)));

            String tLine = null;

            List<String> tPileupList = new ArrayList<String>();

            List<String> tIndelLineList = new ArrayList<String>();

            StrTokenizer tSTokenizer = new StrTokenizer();

            tSTokenizer.setDelimiterString("\t");

            List<String> tList;

            while ((tLine = tBufferedReader.readLine()) != null) {

                tSTokenizer.reset(tLine);

                tList = parseLine(tLine, tPileupList, tSTokenizer, tBufferedReader, tIndelLineList);

                if (this.bSawAnIndel) {
                    this.bSawAnIndel = false;
                    tList.clear();
                    continue;
                } else {
                    this.writeLine(tList);
                }

                tPileupList.clear();
                tIndelLineList.clear();

            }

            tBufferedReader.close();

            this.tMasterDataOutputStream.close();
            this.tDetailDataOutputStream.close();

            System.out.println("\tFinished writing to master file: " + tMasterFile.getAbsolutePath());
            System.out.println("\tFinished writing to detail file: " + tDetailFile.getAbsolutePath());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String getBaseName(String sFullFileName) {

        String sBaseFileName = null;
        int pos = sFullFileName.lastIndexOf('.');
        if (pos > 0 && pos < sFullFileName.length() - 1) {
            sBaseFileName = sFullFileName.substring(0, pos);
        } else {
            sBaseFileName = sFullFileName;
        }

        return sBaseFileName;

    }

    private List<String> parseLine(String sLineOfDataIn, List<String> tLineOfPileupDataIn, StrTokenizer tSTokenizerIn,
            BufferedReader tBufReaderIn, List<String> tIndelLineList) {

        List<String> tList = tLineOfPileupDataIn;

        try {

            if (sLineOfDataIn == null) {
                System.out.println("sLineOfData is null");
            } else {

                StrTokenizer lineScanner = tSTokenizerIn;

                while (lineScanner.hasNext()) {
                    tList.add(lineScanner.nextToken());
                }
            }
            System.out.println(tList.toString());

            IndelHandler tIndelHandler = IndelHandler.getInstance(this.tDetailDataOutputStream, this.tMasterDataOutputStream,
                    this.tConversionFormat, tIndelLineList, tSTokenizerIn);

            if (tList.size() > 3) {

                this.removePileupElements(tList);

                String sReadBases = tList.get(ConverterConstants.READ_BASES_POSITION);

                if (tIndelHandler.hasIndel(sReadBases)) {
                    this.bSawAnIndel = true;
                    tIndelHandler.handleIndel(tList, tBufReaderIn);

                } else {

                }

            } else {
                System.out.println("Got a List with < 3 elements " + tList.size());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return tList;
    }

    private List<String> removePileupElements(List<String> tListIn) {
        tListIn.remove(0);
        tListIn.remove(1);

        return tListIn;
    }

    private void writeLine(List<String> tListIn) {

    }

}
