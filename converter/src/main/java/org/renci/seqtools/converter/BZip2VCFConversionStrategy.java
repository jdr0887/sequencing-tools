package org.renci.seqtools.converter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrTokenizer;

public class BZip2VCFConversionStrategy extends AbstractPileupConversionStrategy implements IConversionStrategy, Runnable {

    private String VERSION_STRING = "Converter 0.1";

    private File tInputVCFFile;

    private File tOutputDir;

    private File tMetricsFile;

    private IConversionFormat tConversionFormat;

    private String sName;

    private VCFLineDataProcessor tLDProcessor;

    private VCFBAMQueryTool tBAMQueryTool;

    private int iNumberOfFilePairs;

    private boolean bSeenVCFHeaders = false;

    private boolean bSeenDoublePoundHeader = false;

    private boolean bSeenSinglePoundHeader = false;

    private boolean bWrittenMasterHeader = false;

    private int iVCFHeaderColumnCount;

    private int iVCFElementsPerLine;

    private int iMetricsGenomeLocationsConvertedCount = 0;

    private int iMetricsSNPCount = 0;

    private int iMetricsInsertionCount = 0;

    private int iMetricsDeletionCount = 0;

    private File tErrorFile;

    private FileWriter tErrorWriter;

    private String sErrorFileName = "errorFile.txt";

    private File tPositionMapFile;

    private FileWriter tPositionMapFileWriter;

    private String sPositionMapFileName = "positionmap.txt";

    private File tVariantVCFOutFile;

    private String[] sColumnNamesArray;

    private Map<String, VCFMasterDetailFileStreamManager> tMapOfOutputFiles;

    private String DASH = "-";

    private VCFMasterDetailFileStreamManager tManager;

    private FileWriter tMetricsFileWriter;

    private Map<String, VCFMetricsManager> tMapOfMetricsData;

    private List<String> tPreviousPositionList;

    private VCFLineHolder tLineHolder;

    private TreeMap<Long, List<String>> tMapOfLeftoverLines = new TreeMap<Long, List<String>>();

    private BufferedWriter tBufferedWriterVariantsOnly;

    public BZip2VCFConversionStrategy(IConversionFormat tFormatIn, File tVCFFileIn, File tBAMFileIn, File tOutputDirIn, String valueOf,
            GenomeType tGenomeTypeIn, File tMetricsFileIn, File tVariantVCFFileOutIn) {

        this.tInputVCFFile = tVCFFileIn;
        this.tOutputDir = tOutputDirIn;
        this.tMetricsFile = tMetricsFileIn;
        this.tVariantVCFOutFile = tVariantVCFFileOutIn;
        this.sName = valueOf;
        this.tConversionFormat = tFormatIn;
        this.tBAMQueryTool = VCFBAMQueryTool.getInstance(tBAMFileIn);

        this.tMapOfOutputFiles = new HashMap<String, VCFMasterDetailFileStreamManager>();
        this.tLDProcessor = VCFLineDataProcessor.getInstance();

        this.tMapOfMetricsData = new HashMap<String, VCFMetricsManager>();

        this.tPreviousPositionList = new ArrayList<String>();
        this.tPreviousPositionList.add("mychrom");
        this.tPreviousPositionList.add("0");

        this.tLineHolder = VCFLineHolder.getInstance(this.tPreviousPositionList, new ArrayList<String>(), new ArrayList<String>(), "", "",
                "");

    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + " executing " + this);
        try {
            this.convert();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void convert() throws Exception {

        try {

            System.out.println("\tConverter: Writing master file(s) ...");
            System.out.println("\tConverter: Writing detail file(s) ...");

            if (this.tMetricsFile != null) {
                this.makeMetricsFile(this.tMetricsFile);
            }

            if (this.tVariantVCFOutFile != null) {
                this.makeVariantsVCFOutFile(this.tVariantVCFOutFile);
            }

            this.sErrorFileName = Thread.currentThread().getName() + "errorFile.txt";
            this.makeErrorFile(this.sErrorFileName);

            this.makePositionMapFile(this.sPositionMapFileName);

            this.writePositionMapHeader();

            File tVCFFileToRead = this.tInputVCFFile;

            BufferedReader tBufferedReader = null;
            if (tVCFFileToRead.getName().toLowerCase().endsWith(".gz")) {
                tBufferedReader = new BufferedReader(
                        new InputStreamReader(new GzipCompressorInputStream(new FileInputStream(tVCFFileToRead))));
            } else {
                tBufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(tVCFFileToRead)));
            }

            java.util.Date tMyDate = new java.util.Date();
            this.tErrorWriter.write("# Error File for " + this.tInputVCFFile.getAbsolutePath() + " run at: " + tMyDate.toString()
                    + System.getProperty("line.separator"));
            this.tErrorWriter.flush();

            this.loopOverData(tBufferedReader);

            this.writePositionMap(this.tMapOfOutputFiles);

            if (this.tMetricsFile != null) {

                this.writeMetricsData(this.tMapOfMetricsData, this.tInputVCFFile.getName());
            }

            tBufferedReader.close();
            this.tErrorWriter.close();
            this.tPositionMapFileWriter.close();
            this.tErrorWriter.close();

            this.tBufferedWriterVariantsOnly.flush();
            this.tBufferedWriterVariantsOnly.close();

            System.out.println("\tConverter: Finished writing master file(s). ");
            System.out.println("\tConverter: Finished writing detail file(s). ");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void makeVariantsVCFOutFile(File tVariantVCFOutFile2) {
        try {
            this.tVariantVCFOutFile = tVariantVCFOutFile2;
            if (this.tVariantVCFOutFile.exists()) {
                this.tVariantVCFOutFile.delete();
            }
            this.tVariantVCFOutFile.createNewFile();

            this.tBufferedWriterVariantsOnly = new BufferedWriter(
                    new OutputStreamWriter(new GzipCompressorOutputStream(new FileOutputStream(this.tVariantVCFOutFile))));

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void makeMetricsFile(File tMetricsFileIn) {
        try {
            this.tMetricsFile = tMetricsFileIn;
            if (this.tMetricsFile.exists()) {
                this.tMetricsFile.delete();
            }
            this.tMetricsFile.createNewFile();
            this.tMetricsFileWriter = new FileWriter(this.tMetricsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void loopOverData(BufferedReader tReaderIn) {

        try {

            String tCurrentLine = null;

            String tNextLine = null;

            List<String> tDupFirstList = new ArrayList<String>();
            List<String> tDupSecondList = new ArrayList<String>();

            List<String> tVCFLineList = new ArrayList<String>();

            StrTokenizer tSTokenizer = new StrTokenizer();

            tSTokenizer.setDelimiterString("\t");

            while ((tCurrentLine = tReaderIn.readLine()) != null && (tNextLine = tReaderIn.readLine()) != null) {

                handleTwoLines(tCurrentLine, tNextLine, this.tInputVCFFile, tSTokenizer, tVCFLineList, tDupFirstList, tDupSecondList);

                tVCFLineList.clear();
                tDupFirstList.clear();
                tDupSecondList.clear();

            }

            tVCFLineList.clear();
            tDupFirstList.clear();
            tDupSecondList.clear();

            List<String> tTempList = new ArrayList<String>();
            Long tPosition;
            tVCFLineList.clear();
            this.tMapOfLeftoverLines.clear();

            tSTokenizer.reset(tCurrentLine);

            if (tCurrentLine != null) {
                tTempList = this.parseLine(tCurrentLine, tTempList, tSTokenizer);
                tPosition = new Long(this.getPositionFromList(tTempList));
                this.tMapOfLeftoverLines.put(tPosition, tTempList);
            }

            tVCFLineList.clear();
            tSTokenizer.reset(tNextLine);
            if (tNextLine != null) {
                tVCFLineList = this.parseLine(tNextLine, tVCFLineList, tSTokenizer);
                this.tMapOfLeftoverLines.put(new Long(this.getPositionFromList(tVCFLineList)), tVCFLineList);
            }

            if (this.getPositionFromList(this.tLineHolder.getPreviousLineList()) > this
                    .getPositionFromList(this.tLineHolder.getLastProcessedLineList())) {
                this.tMapOfLeftoverLines.put(new Long(this.getPositionFromList(this.tLineHolder.getPreviousLineList())),
                        this.tLineHolder.getPreviousLineList());
            }
            if (this.getPositionFromList(this.tLineHolder.getCurrentLineList()) > this
                    .getPositionFromList(this.tLineHolder.getLastProcessedLineList())) {
                this.tMapOfLeftoverLines.put(new Long(this.getPositionFromList(this.tLineHolder.getCurrentLineList())),
                        this.tLineHolder.getCurrentLineList());
            }
            if (this.getPositionFromList(this.tLineHolder.getNextLineList()) > this
                    .getPositionFromList(this.tLineHolder.getLastProcessedLineList())) {
                this.tMapOfLeftoverLines.put(new Long(this.getPositionFromList(this.tLineHolder.getNextLineList())),
                        this.tLineHolder.getNextLineList());
            }

            for (Map.Entry<Long, List<String>> tLoopEntry : this.tMapOfLeftoverLines.entrySet()) {
                this.processLineOfVCFData(tLoopEntry.getValue(), false);
            }

            for (Map.Entry<String, VCFMasterDetailFileStreamManager> tEntry : this.tMapOfOutputFiles.entrySet()) {
                VCFMasterDetailFileStreamManager tManager = tEntry.getValue();
                tManager.closeStreams();
            }

        } catch (IOException e) {
            e.printStackTrace();

            System.exit(1);
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void handleTwoLines(String tCurrentLineOfDataIn, String tNextLineOfDataIn, File tInputVCFFile, StrTokenizer tSTokenizer,
            List<String> tHeaderVCFListToFill, List<String> tCurrentLineListIn, List<String> tNextLineListIn) {

        boolean bArePositionsEqual = false;

        List<String> tParsedHeaderVCFLineList = tHeaderVCFListToFill;

        this.iNumberOfFilePairs = 0;

        if (!this.bSeenVCFHeaders) {

            try {
                tSTokenizer.reset(tCurrentLineOfDataIn);
                this.processHeaders(tCurrentLineOfDataIn, tSTokenizer, tParsedHeaderVCFLineList);
                tSTokenizer.reset(tNextLineOfDataIn);
                this.processHeaders(tNextLineOfDataIn, tSTokenizer, tParsedHeaderVCFLineList);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }

        } else {

            if (((tCurrentLineOfDataIn == null) || (tCurrentLineOfDataIn.isEmpty()))
                    || ((tNextLineOfDataIn == null) || (tNextLineOfDataIn.isEmpty()))) {

            } else {

                try {

                    tSTokenizer.reset(tCurrentLineOfDataIn);
                    tCurrentLineListIn = parseLine(tCurrentLineOfDataIn, tCurrentLineListIn, tSTokenizer);
                    tSTokenizer.reset(tNextLineOfDataIn);
                    tNextLineListIn = parseLine(tNextLineOfDataIn, tNextLineListIn, tSTokenizer);

                    this.tLineHolder.setCurrentLineList(tCurrentLineListIn);
                    this.tLineHolder.setCurrentLine(tCurrentLineOfDataIn);
                    this.tLineHolder.setNextLineList(tNextLineListIn);
                    this.tLineHolder.setNextLine(tNextLineOfDataIn);

                    this.tPreviousPositionList = tCurrentLineListIn;
                    if (this.tLineHolder.areCurrentAndNextPositionsEqual()) {
                        bArePositionsEqual = true;
                        this.processLineOfVCFData(this.tLineHolder.getNextLineList(), bArePositionsEqual);
                        this.tLineHolder.setLastProcessedLine(this.tLineHolder.getNextLine());
                        this.tLineHolder.setLastProcessedLineList(this.tLineHolder.getNextLineList());
                    } else if (this.tLineHolder.arePreviousAndCurrentLinePositionsEqual()) {
                        bArePositionsEqual = true;
                        this.processLineOfVCFData(this.tLineHolder.getCurrentLineList(), bArePositionsEqual);
                        this.tLineHolder.setLastProcessedLine(this.tLineHolder.getCurrentLine());
                        this.tLineHolder.setLastProcessedLineList(this.tLineHolder.getCurrentLineList());

                    } else if (this.tLineHolder.arePreviousAndNextLinePositionsEqual()) {
                        bArePositionsEqual = true;
                        this.processLineOfVCFData(this.tLineHolder.getNextLineList(), bArePositionsEqual);
                        this.tLineHolder.setLastProcessedLine(this.tLineHolder.getNextLine());
                        this.tLineHolder.setLastProcessedLineList(this.tLineHolder.getNextLineList());
                    } else {
                        bArePositionsEqual = false;
                        this.processLineOfVCFData(this.tLineHolder.getPreviousLineList(), bArePositionsEqual);
                        this.processLineOfVCFData(this.tLineHolder.getCurrentLineList(), bArePositionsEqual);

                        this.tLineHolder.setLastProcessedLine(this.tLineHolder.getCurrentLine());
                        this.tLineHolder.setLastProcessedLineList(this.tLineHolder.getCurrentLineList());

                    }

                    this.tLineHolder.setPreviousLineList(tNextLineListIn);
                    this.tLineHolder.setPreviousLine(tNextLineListIn.toString());

                } catch (Exception e) {
                    e.printStackTrace();
                    this.writeError("Could not process this line: ", tCurrentLineOfDataIn + " " + e.getMessage());
                }
            }
        }

    }

    private void processHeaders(String tLineOfDataIn, StrTokenizer tSTokenizer, List<String> tVCFListToFill) throws Exception {

        if (tLineOfDataIn.startsWith("##")) {

            this.bSeenDoublePoundHeader = true;

            this.tBufferedWriterVariantsOnly.write(tLineOfDataIn);
            this.tBufferedWriterVariantsOnly.write(System.getProperty("line.separator"));

        } else if (tLineOfDataIn.startsWith("#")) {

            this.tBufferedWriterVariantsOnly.write(tLineOfDataIn);
            this.tBufferedWriterVariantsOnly.write(System.getProperty("line.separator"));

            List<String> tHeaderList = parseLine(tLineOfDataIn, tVCFListToFill, tSTokenizer);

            this.iVCFHeaderColumnCount = getVCFHeaderColumnCount(tHeaderList);

            this.sColumnNamesArray = this.getSampleColumnNames(tLineOfDataIn, this.tInputVCFFile);

            this.iNumberOfFilePairs = this.sColumnNamesArray.length;
            if (this.sColumnNamesArray.length == 0) {
                this.writeError("Found no individual or sample data column names in the VCF file", tLineOfDataIn);
                System.out.println("Found no individual data in the VCF file.");
                this.writeError(
                        "BZip2VCFConversionStrategy: processHeaders: this.sColumnNamesArray.length == 0; no individual or sample data in the VCF file",
                        tLineOfDataIn);
                System.exit(1);
            }

            this.bSeenSinglePoundHeader = true;
        }

        if (this.bSeenDoublePoundHeader && this.bSeenSinglePoundHeader) {
            this.bSeenVCFHeaders = true;
        }

    }

    private void processLineOfVCFData(List<String> tParsedVCFLineList, boolean bArePositionsEqualIn) throws Exception {

        boolean bSNPError = false;
        try {

            if ((tParsedVCFLineList != null) && (!tParsedVCFLineList.isEmpty()) && (!tParsedVCFLineList.get(1).equalsIgnoreCase("0"))) {

                iNumberOfFilePairs = this.sColumnNamesArray.length;

                String sChromosomeName = tParsedVCFLineList.get(0);
                String sStartPosition = tParsedVCFLineList.get(1);

                this.iVCFElementsPerLine = this.getElementsPerLineCount(tParsedVCFLineList);

                if (this.iVCFElementsPerLine == this.iVCFHeaderColumnCount) {

                    this.tBAMQueryTool.setInputs(sChromosomeName, sStartPosition);

                    this.tLDProcessor = this.getLineDataProcessor();
                    this.tLDProcessor.setInputs(tParsedVCFLineList, iNumberOfFilePairs, this.tBAMQueryTool,
                            tParsedVCFLineList.get(VCFLineDataProcessor.INT_POSITION_COLUMN_POSITION),
                            tParsedVCFLineList.get(VCFLineDataProcessor.INT_POSITION_COLUMN_POSITION));

                    this.tLDProcessor.loadParsedVCFDataIntoMap();

                    Iterator<VCFRecord> tRecordIter;

                    for (int ii = 0; ii < sColumnNamesArray.length; ii++) {

                        List<VCFRecord> tRecordList = this.tLDProcessor.getVCFRecordListForColumn(ii);

                        tRecordIter = tRecordList.iterator();

                        this.tManager = this.checkOrCreateFiles(sChromosomeName, this.sColumnNamesArray[ii], this.tInputVCFFile);

                        while (tRecordIter.hasNext()) {

                            VCFRecord tRecord = (VCFRecord) tRecordIter.next();

                            if (tRecord.isIndel()) {
                                this.tManager.writeIndelData(tRecord);

                                this.tBufferedWriterVariantsOnly.write(StringUtils.join(tParsedVCFLineList, "\t"));
                                this.tBufferedWriterVariantsOnly.write(System.getProperty("line.separator"));

                            } else if (tRecord.isSNP() && bArePositionsEqualIn) {
                                bSNPError = true;
                                throw new Exception("SNP duplicate positions detected: " + StringUtils.join(tParsedVCFLineList, "\t"));

                            } else if (tRecord.isNoCall()) {

                                this.tManager.writeIndelData(tRecord);

                                this.tBufferedWriterVariantsOnly.write(StringUtils.join(tParsedVCFLineList, "\t"));
                                this.tBufferedWriterVariantsOnly.write(System.getProperty("line.separator"));
                            }

                            else if (tRecord.isSNP() && !bArePositionsEqualIn) {

                                this.tManager.writeSNPData(tRecord);

                                this.tBufferedWriterVariantsOnly.write(StringUtils.join(tParsedVCFLineList, "\t"));
                                this.tBufferedWriterVariantsOnly.write(System.getProperty("line.separator"));

                            } else if (tRecord.hasNoReferenceData()) {
                                this.tManager.writeNoReferenceData(tRecord);
                            } else {
                                this.tManager.writeGenomicData(tRecord);
                            }

                            this.collectMetrics(sChromosomeName + DASH + sColumnNamesArray[ii], tRecord.isSNP(), tRecord.isIndel(), true,
                                    Long.parseLong(tRecord.getReaddepth()));

                        }
                    }

                    this.iMetricsGenomeLocationsConvertedCount++;

                } else {

                    this.writeError("Wrong number of data columns: header columns: " + this.iVCFHeaderColumnCount + ", data columns: "
                            + this.iVCFElementsPerLine, tParsedVCFLineList);
                }
            } else {
                this.writeError("VCF line was null or empty", tParsedVCFLineList);
            }
        } catch (Exception e) {

            this.writeError(e.getMessage(), tParsedVCFLineList);
            e.printStackTrace();
            if (bSNPError) {
                System.out.println("Converter exiting from caught SNP duplicate position exception");
                System.exit(1);
            }
        }
    }

    private int getElementsPerLineCount(List<String> tOutputList) {
        int iElementsPerLine = tOutputList.size();
        return iElementsPerLine;
    }

    private int getVCFHeaderColumnCount(List<String> tHeaderList) {
        int iHeaderColumns = tHeaderList.size();
        return iHeaderColumns;
    }

    private VCFLineDataProcessor getLineDataPerIndividual(List<String> tOutputList, int iNumberOfFilePairs, VCFBAMQueryTool tBAMQueryTool,
            String sStartPosition, String sEndPosition) throws Exception {
        return VCFLineDataProcessor.getInstance(tOutputList, iNumberOfFilePairs, tBAMQueryTool, sStartPosition, sEndPosition);
    }

    private VCFLineDataProcessor getLineDataProcessor() {
        return this.tLDProcessor;
    }

    private VCFMasterDetailFileStreamManager checkOrCreateFiles(String sChromosome, String sColumnName, File tVCFFileIn) {

        VCFMasterDetailFileStreamManager tManager = null;

        String sProspectName = sChromosome + DASH + sColumnName;

        sProspectName = BZip2VCFConversionStrategy.sanitizeFilename(sProspectName);

        if (!this.tMapOfOutputFiles.containsKey(sProspectName)) {

            tManager = VCFMasterDetailFileStreamManager.getInstance(sProspectName, 0, tVCFFileIn, this.tOutputDir, this.tConversionFormat);
            tManager.createFilesAndStreams2(sProspectName);
            this.tMapOfOutputFiles.put(sProspectName, tManager);
        } else {
            tManager = this.tMapOfOutputFiles.get(sProspectName);
        }
        return tManager;

    }

    private void collectMetrics(String sMapKeyNameIn, boolean bHasNewSNP, boolean bHasNewIndel, boolean bHasNewTotal,
            long lReadDepthCountIn) {

        VCFMetricsManager tMetricsManager = null;
        if (!this.tMapOfMetricsData.containsKey(sMapKeyNameIn)) {

            tMetricsManager = VCFMetricsManager.getInstance(sMapKeyNameIn);
            if (bHasNewSNP) {
                tMetricsManager.incrementSNPCount();
            }
            if (bHasNewIndel) {
                tMetricsManager.incrementIndelCount();
            }
            if (bHasNewTotal) {
                tMetricsManager.incrementTotalCount();
            }

            tMetricsManager.incrementReadDepth(lReadDepthCountIn);

            this.tMapOfMetricsData.put(sMapKeyNameIn, tMetricsManager);
        } else {
            tMetricsManager = this.tMapOfMetricsData.get(sMapKeyNameIn);
            if (bHasNewSNP) {
                tMetricsManager.incrementSNPCount();
            }
            if (bHasNewIndel) {
                tMetricsManager.incrementIndelCount();
            }
            if (bHasNewTotal) {
                tMetricsManager.incrementTotalCount();
            }

            tMetricsManager.incrementReadDepth(lReadDepthCountIn);

            this.tMapOfMetricsData.put(sMapKeyNameIn, tMetricsManager);

        }

    }

    public static String sanitizeFilename(String name) {
        return name.replaceAll("[:\\\\/*?|<>]", "_");
    }

    private String[] getSampleColumnNames(String tLine, File tVCFFileIn) {

        String[] sArray = tLine.split("FORMAT");
        String[] sFiles = sArray[1].trim().split("\t");

        return sFiles;
    }

    private List<String> parseLine(String sLineOfDataIn, List<String> tLineOfVCFDataIn, StrTokenizer tSTokenizerIn) throws Exception {

        List<String> tList = tLineOfVCFDataIn;
        tList.clear();
        if (sLineOfDataIn == null) {
            System.out.println("BZip2VCFConversionStrategy: parseLine, sLineOfData is null");

            throw new Exception("BZip2VCFConversionStrategy: parseLine: sLineOfData is null");

        } else {

            StrTokenizer lineScanner = tSTokenizerIn;

            while (lineScanner.hasNext()) {
                tList.add(lineScanner.nextToken());
            }
        }

        return tList;
    }

    private void makeErrorFile(String sErrorFile) {

        try {
            String sErrorPathAndFile = this.tOutputDir.getCanonicalPath() + File.separatorChar + sErrorFile;

            this.tErrorFile = new File(sErrorPathAndFile);

            if (this.tErrorFile.exists()) {
                this.tErrorFile.delete();
            }
            this.tErrorFile.createNewFile();
            this.tErrorWriter = new FileWriter(tErrorFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeError(String sMessageIn, List<String> tListIn) {
        try {
            this.tErrorWriter.write(sMessageIn + " " + tListIn.toString() + System.getProperty("line.separator"));
            this.tErrorWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeError(String sMessageIn, String sErrorLineIn) {
        try {
            this.tErrorWriter.write(sMessageIn + " " + sErrorLineIn + System.getProperty("line.separator"));
            this.tErrorWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void makePositionMapFile(String sPositionMapFile) {

        try {

            String sPositionMapFileName = this.tOutputDir.getCanonicalPath() + File.separatorChar + this.tInputVCFFile.getName() + this.DASH
                    + sPositionMapFile;
            this.tPositionMapFile = new File(sPositionMapFileName);

            if (this.tPositionMapFile.exists()) {
                this.tPositionMapFile.delete();
            }
            this.tPositionMapFile.createNewFile();
            this.tPositionMapFileWriter = new FileWriter(this.tPositionMapFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writePositionMap(Map<String, VCFMasterDetailFileStreamManager> tMapIn) {
        try {

            Iterator tIter = tMapIn.entrySet().iterator();
            VCFMasterDetailFileStreamManager tManager = null;
            String sFirstPosition = null, sLastPosition = null;
            File tFileName = null;
            while (tIter.hasNext()) {
                Map.Entry<String, VCFMasterDetailFileStreamManager> tEntry = (Map.Entry) tIter.next();
                tManager = tEntry.getValue();
                if (tManager != null) {
                    sFirstPosition = tManager.getFirstPositionInFile();
                    sLastPosition = tManager.getLastPositionInFile();
                    tFileName = tManager.getMasterFile();
                    this.tPositionMapFileWriter.write(tFileName.getName() + ":" + tEntry.getKey() + ":" + sFirstPosition + ":"
                            + sLastPosition + System.getProperty("line.separator"));
                }
            }

            this.tPositionMapFileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writePositionMapHeader() {
        try {
            this.tPositionMapFileWriter.write(this.VERSION_STRING + System.getProperty("line.separator"));
            this.tPositionMapFileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeMetricsData(Map<String, VCFMetricsManager> tMapIn, String sVCFFileNameIn) {
        try {

            long iTotalFilePosition = 0;
            long iTotalSNPFile = 0;
            long iTotalIndelFile = 0;
            long lTotalReadDepth = 0;
            long lAvgTotalReadDepth = 0;
            long lCounter = 0;
            Iterator tIter = tMapIn.entrySet().iterator();
            VCFMetricsManager tManager = null;

            while (tIter.hasNext()) {
                lCounter++;
                Map.Entry<String, VCFMetricsManager> tEntry = (Map.Entry) tIter.next();
                tManager = tEntry.getValue();
                if (tManager != null) {
                    this.tMetricsFileWriter.write(tEntry.getKey() + ":\tTotal Indels:" + tManager.getIndelCount() + "\tTotal SNPs: "
                            + tManager.getSNPCount() + "\t\tTotal Positions: " + tManager.getTotalPositionsCount()
                            + "\t\tAverage Read Depth:" + tManager.getAverageReadDepth() + System.getProperty("line.separator"));
                    iTotalFilePosition = iTotalFilePosition + tManager.getTotalPositionsCount();
                    iTotalSNPFile = iTotalSNPFile + tManager.getSNPCount();
                    iTotalIndelFile = iTotalIndelFile + tManager.getIndelCount();
                    lTotalReadDepth += tManager.getAverageReadDepth();
                }
            }

            if (lCounter > 0) {
                lAvgTotalReadDepth = lTotalReadDepth / lCounter;
            } else {
                lAvgTotalReadDepth = 0;
            }
            this.tMetricsFileWriter.write(System.getProperty("line.separator"));
            this.tMetricsFileWriter.write("File: " + sVCFFileNameIn + "\tTotal Indels:" + iTotalIndelFile + "\tTotal SNPs: " + iTotalSNPFile
                    + "\t\tTotal Positions: " + iTotalFilePosition + "\t\tTotal Average Read Depth: " + lAvgTotalReadDepth
                    + System.getProperty("line.separator"));

            this.tMetricsFileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public long getPositionFromList(List<String> tListOfVCFElementsIn) {
        String sPosition = tListOfVCFElementsIn.get(1);
        return Long.valueOf(sPosition).longValue();
    }

}
