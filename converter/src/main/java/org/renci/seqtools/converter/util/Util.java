package org.renci.seqtools.converter.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.text.StrTokenizer;

/**
 * Util
 * <p>
 * A utility class containing a few methods for string-parsing, character-finding, etc.
 * 
 * @author k47k4705
 *
 */
public class Util {

    private static final long DETAIL_RECORD_STATIC_SIZE = 200000;

    private static final long KILOBYTE = 1024;

    private static final long MEGABYTE = 1048576;

    private static final long GIGABYTE = 1073741824;

    /**
     * Util private constructor.
     */
    private Util() {
    } // end util

    /**
     * MASTER_FILE_RECORD_LENGTH constant -- the length of a record in the master file in bytes.
     */
    // public static int MASTER_FILE_RECORD_LENGTH = 15;
    // public static int MASTER_FILE_RECORD_LENGTH = 23;

    // Master record size for a genome file.
    public static int MASTER_FILE_RECORD_LENGTH_GENOME = 23;

    // Master record size for an exome file. (Exomes contain an extra long with genomic position information.)
    public static int MASTER_FILE_RECORD_LENGTH_EXOME = 31;

    /**
     * INDEL_BASE_VALUE constant -- the "starting point" for indel values. If you are an indel value, you start and add
     * to this base.
     */
    public static int INDEL_BASE_VALUE = 200;

    /**
     * DETAIL_RECORD_PADDING -- the padding for detail records. The detail record file is sparse. However, because
     * master records are small, writing detail records into the detail file based on the current file pointer position
     * of the master file means we can potentially overwrite data in the detail file if the pileup file we are
     * converting has indel records early in the file.
     */
    public static int DETAIL_RECORD_PADDING = 5;

    /**
     * getFirstGenomeCharacter()
     * <p>
     * Get the first character from a genome String from a pileup file that is an alphabetic character or a "*"
     * character.
     * <p>
     * Return "Z" (a non-sense genome) if no character is found in the String.
     * 
     * @param tStringIn
     *            -- the String to search for an alphabetic character.
     * @return String -- the alpabetic character.
     */
    public static String getFirstGenomeCharacter(String sStringIn) {
        // String to return. Nonsense "Z" if we can't find an alphabetic character or a "*" in the input string.
        String sReturnChar = "Z";

        for (int ii = 0; ii < sStringIn.length(); ii++) {
            // Is this a character or a "*"?
            if ((Character.isLetter(sStringIn.charAt(ii))) || (sStringIn.charAt(ii) == '*')) {
                sReturnChar = String.valueOf(sStringIn.charAt(ii));
                return sReturnChar;
            }
        } // end for

        return sReturnChar;

    } // end getFirstGenomeCharacter()

    /**
     * tabbedStringToList()
     * <p>
     * Takes a tabbed String and returns a List.
     * 
     * @param sTabbedStringIn
     *            -- the tabbed String to transform into a List.
     * @return List -- a List with elements made from a tabbed String.
     */
    public static List<String> tabbedStringToList(String sTabbedStringIn) {
        // List to return.
        List<String> tReturnList = new ArrayList<String>();

        // StringTokenizer.
        StrTokenizer tTokenizer = new StrTokenizer();
        tTokenizer.reset(sTabbedStringIn);
        while (tTokenizer.hasNext()) {
            // System.out.println("next token: " + tTokenizer.nextToken());
            tReturnList.add(tTokenizer.nextToken());
        } // end while

        return tReturnList;
    } // end tabbedStringToList

    /**
     * removeFromFrontOfList()
     * <p>
     * Removes a number of elements from the front of a list.
     * 
     * @param tInputList
     *            -- the List in need of element removal.
     * @param tNumElements
     *            -- the number of elements to remove.
     * @return List -- a copy of the input list with elements removed.
     */
    public static List<String> removeFromFrontOfList(List<String> tListIn, int tNumElementsIn) {
        // Copy the input list.
        List<String> tReturnListCopy = new ArrayList<String>(tListIn);

        // Loop through list copy.
        for (int ii = 0; ii < tNumElementsIn; ii++) {
            // Remove an element from the front of the list.
            tReturnListCopy.remove(0);
        } // end for

        return tReturnListCopy;
    } // end removeFromFrontOfList

    /**
     * byteToBits()
     * <p>
     * Turn a byte into a String of bits: "10000001"
     * 
     * @param b
     *            -- the byte to convert to a String
     * @return String -- the String of bits underlying that byte: "10000001"
     */
    public static String byteToBits(byte b) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < 8; i++)
            buf.append((int) (b >> (8 - (i + 1)) & 0x0001));
        return buf.toString();
    } // end byteToBits

    /**
     * isStringChar()
     * <p>
     * Gets around String encoding problems. Lets client test individual chars against a switch/case block.
     * <p>
     * Likely, the switch/case block can expand or contract as we discover characters in the pileup files.
     * 
     * @param ch
     * @return
     */
    public static boolean isStringChar(char ch) {
        if (ch >= 'a' && ch <= 'z')
            return true;
        if (ch >= 'A' && ch <= 'Z')
            return true;
        if (ch >= '0' && ch <= '9')
            return true;
        switch (ch) {
            case '/':
            case '-':
            case ':':
            case ';':
            case '.':
            case ',':
            case '_':
            case '$':
            case '%':
            case '\'':
            case '(':
            case ')':
            case '[':
            case ']':
            case '<':
            case '>':
            case '|':
            case '!':
            case '@':
            case '=':
            case '*':
            case '#':
            case '^':
            case '+':
            case '?':
            case ' ':
            case '\t':
                return true;
        }
        return false;
    } // end isStringChar

    /**
     * calculateDetailFilePointerPosition()
     * <p>
     * Given the current file pointer position of the master file, calculates a file pointer position in the detail file
     * where a detail record can be inserted.
     * <p>
     * Calculating a position for the detail file pointer is necessary. It is necessary because indels may occur early
     * in the pileup file. And master records are small by design. So the master file pointer won't move much (15 bytes
     * in the current design) record by record. However, indels in the detail file are larger than the master record.
     * So, early indels in a pileup without a sufficiently padded detail file pointer means we overwrite records in the
     * detail file.
     * <p>
     * The calculation of a detail file pointer position given the current position of the master file pointer means we
     * have enough padding in the detail file so overwriting detail records is not a problem.
     * <p>
     * 
     * @param tMasterFilePointerPositionIn
     *            -- the long value of the current position in the master file.
     * @param tDetailFilePointerPositionIn
     *            -- the long value of the current position in the detail file.
     * @return long -- the calculated position of the detail file pointer (the start of the next detail record).
     */
    public static long calculateDetailFilePointerPosition(long tMasterFilePointerPositionIn, long tDetailFileCurrentPointerPositionIn) {

        System.out.println("Util: master pos: " + tMasterFilePointerPositionIn + " detail pos:" + tDetailFileCurrentPointerPositionIn);
        if (tMasterFilePointerPositionIn < 1000) {
            System.out.println("tMasterFilePointerPositionIn < 1000: " + tMasterFilePointerPositionIn);
        }
        if (tDetailFileCurrentPointerPositionIn < 1000) {
            System.out.println("tDetailFilePointerPositionIn < 1000: " + tDetailFileCurrentPointerPositionIn);
        }

        long tReturnLong = Math
                .round((long) (tMasterFilePointerPositionIn + tDetailFileCurrentPointerPositionIn + Util.DETAIL_RECORD_STATIC_SIZE)
                        * Util.DETAIL_RECORD_PADDING / 150);
        System.out.println("Util: calc detail pos: " + tReturnLong);
        return tReturnLong;
    } // end calculateDetailFilePointerPosition

    public static int calcDFP(long tMasterPosition) {
        int tDetailP = 0;
        if (tMasterPosition < Util.KILOBYTE && tMasterPosition > 0) {
            tDetailP = Math.round(Util.KILOBYTE * tMasterPosition);
        } else if (tMasterPosition > Util.KILOBYTE && tMasterPosition < Util.MEGABYTE) {
            tDetailP = Math.round(tMasterPosition / 10) + 100000;
        } else if (tMasterPosition > Util.MEGABYTE && tMasterPosition < Util.GIGABYTE) {
            tDetailP = Math.round(tMasterPosition / 10000) + 100000;
        } else if (tMasterPosition > Util.GIGABYTE) {
            tDetailP = Math.round(tMasterPosition / 100000) + 100000;
        }

        // tDetailP = Math.round(tMasterPosition / 10) + 100000;
        return tDetailP;
    } // end calcDFP

} // end Util
