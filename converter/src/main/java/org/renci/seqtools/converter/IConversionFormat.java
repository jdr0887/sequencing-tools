package org.renci.seqtools.converter;

import java.io.BufferedWriter;
import java.io.DataOutputStream;

/**
 * IConversionFormat
 * 
 * @author k47k4705 IConversionFormat is an interface clients use to write data to a particular format.
 */
public interface IConversionFormat {

    /**
     * writeToGenomicFormatThree()
     * <p>
     * writeToFormat() accepts List<> data and writes it to the "complete genome" format. Complete Genome Format (CGF?
     * ;-) ) means we're not writing reference coordinate location information into our master file.
     * 
     * @param tListIn
     *            <String> -- the List containing data to format.
     * @param tDetailFileChannelIn
     *            -- the FileChannel for detail file data writing.
     * @param tMasterOutputStream
     *            -- the output stream for writing the master file.
     * @param tDetailOutputStream
     *            -- the output stream for writing the detail file.
     */
    // public void writeToGenomicFormatThree(List<String> tListIn,
    // DataOutputStream tDetailOutputStreamIn, DataOutputStream
    // tMasterOutputStream);

    // public void writeToGenomicFormatVCF(List<String> tListIn,
    // DataOutputStream tDetailOutputStreamIn, DataOutputStream
    // tMasterOutputStream, VCFRecord tRecordIn);
    public void writeToGenomicFormatVCF(DataOutputStream tDetailOutputStreamIn, DataOutputStream tMasterOutputStream,
            BufferedWriter tTestWriter, VCFRecord tRecordIn);

    /**
     * writeToIndelFormatThree()
     * <p>
     * 
     * @param tListIn
     * @param tMasterOutputStream
     * @param tDetailOutputStream
     */
    // public void writeToIndelFormatThree(List<String> tListIn,
    // DataOutputStream tDetailOutputStreamIn, DataOutputStream
    // tMasterOutputStream, List<String> tIndelDataListIn);

    /**
     * writeToIndelFormatVCF()
     * <p>
     * Write indel (insertion/deletion) data from VCF and BAM files to the parameter streams using the data contained in
     * the parameter VCFRecord.
     * 
     * @param tDetailOutputStreamIn
     *            -- the DataOutputStream used for writing consensus master data.
     * @param tMasterOutputStream
     *            -- the DataOutputStream used for writing consensus detail data.
     * @param tRecordIn
     *            -- the data to write contained in a VCFRecord.
     */
    public void writeToIndelFormatVCF(DataOutputStream tDetailOutputStreamIn, DataOutputStream tMasterOutputStream,
            BufferedWriter tTestWriter, VCFRecord tRecordIn);

    /**
     * writeToSNPFormatVCF()
     * <p>
     * Write SNP (single nucleotide polymorphic) data from VCF and BAM files to the parameter streams using the data
     * contained in the parameter VCFRecord.
     * 
     * @param tDetailOutputStreamIn
     *            -- the DataOutputStream used for writing consensus master data.
     * @param tMasterOutputStream
     *            -- the DataOutputStream used for writing consensus detail data.
     * @param tRecordIn
     *            -- the data to write contained in a VCFRecord.
     */
    public void writeToSNPFormatVCF(DataOutputStream tDetailOutputStreamIn, DataOutputStream tMasterOutputStream,
            BufferedWriter tTestWriter, VCFRecord tRecordIn);

    /**
     * writeToNoReferenceDataFormatVCF()
     * <p>
     * If the position in the VCF contains no reference data, (is a "." in the REF column), then write out to the
     * consensus file. This method formalizes and makes visible those writings. Instead of glomming everything into the
     * writeToGenomicFormat() method.
     */
    public void writeToNoReferenceDataFormatVCF(DataOutputStream tDetailOutputStreamIn, DataOutputStream tMasterOutputStream,
            BufferedWriter tTestWriter, VCFRecord tRecordIn);

    /**
     * getMasterPosition()
     * <p>
     * Return the current position in the master consensus file.
     * 
     * @return int -- the position as an int.
     */
    public int getMasterPosition();

    /**
     * getDetailPosition()
     * <p>
     * Returns the current position in the detail consensus file.
     * 
     * @return int -- the position as in int.
     * @return
     */
    public int getDetailPosition();

} // end IConversionFormat
