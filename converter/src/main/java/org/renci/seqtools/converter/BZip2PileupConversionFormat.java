package org.renci.seqtools.converter;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.util.List;

import org.renci.seqtools.converter.genotypemap.GenotypeMap;

/**
 * BZip2PileupConversionFormat
 * 
 * @author k47k4705 BZip2PileupConversionFormat is an object which knows how to write data into a particular consensus
 *         binary format.
 *         <p>
 *         It handles the output format of the conversion process. It deals with a line of data. And it writes that data
 *         out to the master file (writeToGenomicFormatThree) or detail file (writeToIndelFormatThree).
 *         <p>
 *         The BZip2 compression stream objects live above this object but the Format object knows how and deals with
 *         DataOutputStreams for writing data.
 *         <p>
 *         Thanks to the genotype indel "additive" base byte concept, getGenotype() and getIndelGenotype() here converts
 *         Strings to bytes.
 */
public class BZip2PileupConversionFormat implements IConversionFormat {

    // List<String> of a line of a pileup text file.
    private List<String> tPileupLineList;

    // DataOutputStream (for master output file)
    private DataOutputStream tMasterOutputStream;

    // DataOutputStream (for detail output file)
    private DataOutputStream tDetailOutputStream;

    // Text-mode BufferedWriter
    private BufferedWriter tTextModeWriter;

    // Lists and StringBuffers for handling indel data.
    private List<String> tIndelDataList;

    private StringBuffer tIndelBuffer = new StringBuffer();

    // Map of genotypes in pileup file to binary/byte/bit representation.
    private GenotypeMap tGMap;

    /**
     * BZip2PileupConversionFormat public constructor
     * <p>
     * Makes a GenotypeMap util object contained by this object.
     */
    BZip2PileupConversionFormat() {

        // Get instance of genotype map: genotype <=> byte/bit representation.
        this.tGMap = GenotypeMap.getInstance();

    } // end BZip2PileupConversionFormat

    /*
     * (non-Javadoc)
     * 
     * @see org.renci.sequencing.converter.IConversionFormat#getGenotype(java.lang .String)
     */
    public byte getGenotype() throws Exception {
        // byte to return.
        byte tByteToReturn = 00000000;
        String sGenotype = this.tPileupLineList.get(ConverterConstants.GENOTYPE_POSITION);

        // Try it with the genotype map class.
        if (this.tGMap.containsKey(sGenotype)) {
            Byte tByte = (Byte) this.tGMap.get(sGenotype);
            tByteToReturn = tByte.byteValue();
        } // end if

        return tByteToReturn;
    } // end getGenotype

    /**
     * getConsensusQuality()
     */
    public int getConsensusQuality() {
        String sCQ = this.tPileupLineList.get(ConverterConstants.CONSENSUS_QUALITY_POSITION);

        return Integer.parseInt(sCQ);
    } // end getConsensusQuality

    /**
     * getSNPQuality()
     */
    public int getSNPQuality() {
        String sSNPQ = this.tPileupLineList.get(ConverterConstants.SNP_QUALITY_POSITION);

        return Integer.parseInt(sSNPQ);
    } // end getSNPQuality

    /**
     * getMappingQuality()
     */
    public short getMappingQuality() {
        String sMQ = this.tPileupLineList.get(ConverterConstants.MAPPING_QUALITY_POSITION);

        return Short.parseShort(sMQ);
    } // end getMappingQuality

    /**
     * getReadDepth()
     */
    public int getReadDepth() {
        String sRD = this.tPileupLineList.get(ConverterConstants.READ_DEPTH_POSITION);

        return Integer.parseInt(sRD);

    } // end getReadDepth

    /**
     * getReadBases()
     */
    public String getReadBases() {
        String sRB = this.tPileupLineList.get(ConverterConstants.READ_BASES_POSITION);
        return sRB;
    } // end getReadBases

    /**
     * getReadQualityScores()
     */
    public String getReadQualityScores() {
        String sRQS = this.tPileupLineList.get(ConverterConstants.READ_QUALITY_POSITION);
        return sRQS;
    } // end getReadQualityScores

    /**
     * getReferenceCoordinate()
     */
    public long getCoordinatePosition() {
        String sRC = this.tPileupLineList.get(ConverterConstants.REFERENCE_COORDINATE_POSITION);

        return Long.parseLong(sRC);
    }

    // @Override
    // public String getIndelData() {
    // // Empty the buffer.
    // this.tIndelBuffer.setLength(0);
    // // Write the indel data separated by tabs into the buffer.
    // for (String x : this.tIndelDataList) {
    // this.tIndelBuffer.append("\t");
    // this.tIndelBuffer.append(x);
    // }
    // return this.tIndelBuffer.toString();
    // } // end getIndelData
    //
    // @Override
    // public byte getIndelGenotype() {
    //
    // String sGenotype =
    // Util.getFirstGenomeCharacter(this.tPileupLineList.get(ConverterConstants.GENOTYPE_POSITION));
    //
    // byte tMyByte = 00000000;
    // if (this.tGMap.containsKey(sGenotype)) {
    // // Add the indel genotype to the indel base. Get it as a byte.
    // tMyByte =
    // this.tGMap.addToBaseIndel(this.tGMap.get(sGenotype).byteValue());
    // } // end if
    //
    // return tMyByte;
    //
    // } // end getIndelGenotype

    // @Override
    // public void writeToGenomicFormatThree(List<String> tListIn,
    // DataOutputStream tDetailOutputStreamIn,
    // DataOutputStream tMasterOutputStreamIn) {
    //
    // //System.out.println("hey from BZip2PileupConversionFormat.writeToGenomicFormatThree() ... "
    // + tListIn.toString());
    //
    // this.tPileupLineList = tListIn;
    // this.tDetailOutputStream = tDetailOutputStreamIn;
    // this.tMasterOutputStream = (DataOutputStream)tMasterOutputStreamIn;
    //
    //
    // try {
    //
    // // Write data to master file. Not including reference coordinate.
    // this.tMasterOutputStream.writeByte(this.getGenotype());
    // this.tMasterOutputStream.writeInt(this.getConsensusQuality());
    // this.tMasterOutputStream.writeInt(this.getSNPQuality());
    // this.tMasterOutputStream.writeShort(this.getMappingQuality());
    // this.tMasterOutputStream.writeInt(this.getReadDepth());
    // // Write long file pointer to data in the data file.
    // this.tMasterOutputStream.writeLong(0);
    // this.tMasterOutputStream.flush();
    //
    // } catch (IOException e) {
    // e.printStackTrace();
    // } catch (Exception e) {
    // e.printStackTrace();
    // System.out.println("Exception: " + " this.tPileupLineList.size(): " +
    // this.tPileupLineList.size());
    // Iterator tIter = this.tPileupLineList.iterator();
    // while (tIter.hasNext()) {
    // System.out.println("tPileupLineList element: " + (String)tIter.next());
    // } // end while
    //
    // }
    //
    // } // end writeToGenomicFormatThree
    //
    // @Override
    // public void writeToIndelFormatThree(List<String> tListIn,
    // DataOutputStream tDetailOutputStreamIn,
    // DataOutputStream tMasterOutputStream, List<String> tIndelDataListIn) {
    //
    // this.tPileupLineList = tListIn;
    // this.tIndelDataList = tIndelDataListIn;
    // this.tDetailOutputStream = tDetailOutputStreamIn;
    // this.tMasterOutputStream = (DataOutputStream)tMasterOutputStream;
    //
    // try {
    // this.tMasterOutputStream.flush();
    // this.tDetailOutputStream.flush();
    //
    // // Get current master stream position, detail stream position.
    // int iCurrentMasterPosition = this.tMasterOutputStream.size();
    // int iCurrentDetailPosition = this.tDetailOutputStream.size();
    //
    // // Write detail data in to "sparse" detail file at the same position as
    // the master file.
    // // So, look ups of the data in the master file will translate well into
    // lookups in the detail file.
    // String tBasesAndQualityScoresAndIndelData = this.getReadBases() + "\t" +
    // this.getReadQualityScores() + "|";
    // ByteBuffer tBuffer = null;
    // tBuffer = ByteBuffer.wrap(tBasesAndQualityScoresAndIndelData.getBytes());
    // byte[] tBArray = new byte[tBuffer.remaining()];
    // //System.out.println("tBArray.length: " + tBArray.length);
    // tBuffer.get(tBArray);
    //
    // // Write data to master file. Not including reference coordinate.
    // // This is an indel line. Add the indel genotype byte to the actual
    // genotype byte of this pileup line.
    // // We do this so we can identify this record later (on read) as having an
    // indel line associated with it
    // // in the detail file.
    // this.tMasterOutputStream.writeByte(this.getGenotype());
    // this.tMasterOutputStream.writeInt(this.getConsensusQuality());
    // this.tMasterOutputStream.writeInt(this.getSNPQuality());
    // this.tMasterOutputStream.writeShort(this.getMappingQuality());
    // this.tMasterOutputStream.writeInt(this.getReadDepth());
    // this.tMasterOutputStream.writeLong(iCurrentDetailPosition);
    //
    // this.tDetailOutputStream.write(tBArray);
    // //System.out.println("BZCS writeToIndelFormatTwo: wrote to master stream last act before catch "
    // + this.getIndelGenotype() + " " + this.getConsensusQuality() + " " +
    // this.getSNPQuality() + " " + this.getMappingQuality() + " " +
    // this.getReadDepth() + " " + sIndelData);
    //
    // this.tDetailOutputStream.flush();
    // this.tMasterOutputStream.flush();
    //
    // } catch (Exception e) {
    // e.printStackTrace();
    // System.out.println("Exception: " + " this.tPileupLineList.size(): " +
    // this.tPileupLineList.size());
    // Iterator tIter = this.tPileupLineList.iterator();
    // while (tIter.hasNext()) {
    // System.out.println("tPileupLineList element: " + (String)tIter.next());
    // } // end while
    //
    // } // end catch
    //
    //
    // } // end writeToIndelFormatThree

    @Override
    public int getMasterPosition() {
        return this.tMasterOutputStream.size();
    } // end getMasterPosition

    @Override
    public int getDetailPosition() {
        return this.tDetailOutputStream.size();
    } // end getDetailPosition

    // @Override
    // public void writeToGenomicFormatVCF(List<String> tListIn,
    // DataOutputStream tDetailOutputStreamIn,
    // DataOutputStream tMasterOutputStream, VCFRecord tRecordIn) {
    // // TODO Auto-generated method stub
    //
    // }
    //
    // @Override
    // public void writeToIndelFormatVCF(List<String> tListIn,
    // DataOutputStream tDetailOutputStreamIn,
    // DataOutputStream tMasterOutputStream,
    // List<String> tIndelDataListIn, VCFRecord tRecordIn) {
    // // TODO Auto-generated method stub
    //
    // }

    @Override
    public void writeToGenomicFormatVCF(DataOutputStream tDetailOutputStreamIn, DataOutputStream tMasterOutputStream,
            BufferedWriter tBufferedWriterIn, VCFRecord tRecordIn) {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeToIndelFormatVCF(DataOutputStream tDetailOutputStreamIn, DataOutputStream tMasterOutputStream,
            BufferedWriter tBufferedWriterIn, VCFRecord tRecordIn) {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeToSNPFormatVCF(DataOutputStream tDetailOutputStreamIn, DataOutputStream tMasterOutputStream,
            BufferedWriter tBufferedWriterIn, VCFRecord tRecordIn) {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeToNoReferenceDataFormatVCF(DataOutputStream tDetailOutputStreamIn, DataOutputStream tMasterOutputStream,
            BufferedWriter tTestWriter, VCFRecord tRecordIn) {
        // TODO Auto-generated method stub

    }

    // @Override
    // public void writeToGenomicFormatThree(List<String> tListIn,
    // DataOutputStream tDetailOutputStreamIn,
    // DataOutputStream tMasterOutputStream) {
    // // TODO Auto-generated method stub
    //
    // }

} // end GZipBinaryPileupConversionFormat
