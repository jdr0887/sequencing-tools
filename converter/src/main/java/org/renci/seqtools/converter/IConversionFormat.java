package org.renci.seqtools.converter;

import java.io.BufferedWriter;
import java.io.DataOutputStream;

public interface IConversionFormat {

    public void writeToGenomicFormatVCF(DataOutputStream tDetailOutputStreamIn, DataOutputStream tMasterOutputStream,
            BufferedWriter tTestWriter, VCFRecord tRecordIn);

    public void writeToIndelFormatVCF(DataOutputStream tDetailOutputStreamIn, DataOutputStream tMasterOutputStream,
            BufferedWriter tTestWriter, VCFRecord tRecordIn);

    public void writeToSNPFormatVCF(DataOutputStream tDetailOutputStreamIn, DataOutputStream tMasterOutputStream,
            BufferedWriter tTestWriter, VCFRecord tRecordIn);

    public void writeToNoReferenceDataFormatVCF(DataOutputStream tDetailOutputStreamIn, DataOutputStream tMasterOutputStream,
            BufferedWriter tTestWriter, VCFRecord tRecordIn);

    public int getMasterPosition();

    public int getDetailPosition();

}
