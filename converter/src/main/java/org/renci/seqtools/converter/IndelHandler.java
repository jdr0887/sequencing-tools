package org.renci.seqtools.converter;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.commons.lang3.text.StrTokenizer;
import org.renci.seqtools.converter.util.Util;

public class IndelHandler {

    private DataOutputStream tMasterStream;

    private DataOutputStream tDetailStream;

    private IConversionFormat tFormat;

    private StrTokenizer tTokenizer;

    private List<String> tIndelList;

    private FileChannel tDetailFileChannel;

    private IndelHandler(FileChannel tDetailFileChannelIn, DataOutputStream tMasterDataOutputStream, IConversionFormat tFormatIn,
            List<String> tIndelListIn, StrTokenizer tTokenizerIn) {
        this.tDetailFileChannel = tDetailFileChannelIn;
        this.tMasterStream = tMasterDataOutputStream;
        this.tFormat = tFormatIn;
        this.tTokenizer = tTokenizerIn;
        this.tIndelList = tIndelListIn;
    }

    private IndelHandler(DataOutputStream tDetailOutputStreamIn, DataOutputStream tMasterDataOutputStream, IConversionFormat tFormatIn,
            List<String> tIndelListIn, StrTokenizer tTokenizerIn) {
        this.tDetailStream = tDetailOutputStreamIn;
        this.tMasterStream = tMasterDataOutputStream;
        this.tFormat = tFormatIn;
        this.tTokenizer = tTokenizerIn;
        this.tIndelList = tIndelListIn;
    }

    public static IndelHandler getInstance(FileChannel tDetailFileChannel, DataOutputStream tMasterDataOutputStream,
            IConversionFormat tFormatIn, List<String> tIndelList, StrTokenizer tTokenizer) {
        return new IndelHandler(tDetailFileChannel, tMasterDataOutputStream, tFormatIn, tIndelList, tTokenizer);
    }

    public static IndelHandler getInstance(DataOutputStream tDetailOutputStream, DataOutputStream tMasterDataOutputStream,
            IConversionFormat tFormatIn, List<String> tIndelList, StrTokenizer tTokenizer) {
        return new IndelHandler(tDetailOutputStream, tMasterDataOutputStream, tFormatIn, tIndelList, tTokenizer);
    }

    public void handleIndel(List<String> tList, BufferedReader tBufReaderIn) throws IOException {

        String sIndel = tList.get(ConverterConstants.READ_BASES_POSITION);

        Matcher tInsertMatcher = ConverterConstants.PATTERN_INSERT.matcher(sIndel);
        if (tInsertMatcher.find()) {

            int iNumberToRead = Integer.parseInt(tInsertMatcher.group(1));

            int iLoopVar = 0;

            tBufReaderIn.readLine();

            for (iLoopVar = 0; iLoopVar < iNumberToRead; iLoopVar++) {

                List<String> tNewList = Util.tabbedStringToList(tBufReaderIn.readLine());

                if (tNewList.size() == 0) {
                    break;
                }

                List<String> tPileupList = Util.removeFromFrontOfList(tNewList, 2);
                List<String> tMyIndelList = Util.removeFromFrontOfList(tNewList, 8);
            }

        }

        Matcher tDeleteMatcher = ConverterConstants.PATTERN_DELETE.matcher(sIndel);
        if (tDeleteMatcher.find()) {
            int iNumberToRead = Integer.parseInt(tDeleteMatcher.group(1));
            int iLoopVar = 0;
            tBufReaderIn.readLine();

            for (iLoopVar = 0; iLoopVar < iNumberToRead; iLoopVar++) {
                List<String> tNewList = Util.tabbedStringToList(tBufReaderIn.readLine());
                if (tNewList.size() == 0) {
                    break;
                }

                List<String> tPileupList = Util.removeFromFrontOfList(tNewList, 2);
                List<String> tMyIndelList = Util.removeFromFrontOfList(tNewList, 8);

            }
        }

    }

    public boolean hasIndel(String sReadBases) {
        boolean bHasIndel = false;
        if (this.hasInsertion(sReadBases) || this.hasDeletion(sReadBases)) {
            bHasIndel = true;
        }
        return bHasIndel;
    }

    private boolean hasInsertion(String sReadBases) {
        boolean bHasInsertion = false;
        if (sReadBases.matches(ConverterConstants.MARKER_INSERTION)) {
            bHasInsertion = true;

        }
        return bHasInsertion;
    }

    private boolean hasDeletion(String sReadBases) {
        boolean bHasDeletion = false;
        if (sReadBases.matches(ConverterConstants.MARKER_DELETION)) {
            bHasDeletion = true;

        }
        return bHasDeletion;
    }

}
