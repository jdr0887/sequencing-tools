package org.renci.seqtools.converter.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.text.StrTokenizer;

public class Util {

    private static final long DETAIL_RECORD_STATIC_SIZE = 200000;

    private static final long KILOBYTE = 1024;

    private static final long MEGABYTE = 1048576;

    private static final long GIGABYTE = 1073741824;

    private Util() {
    }

    public static int MASTER_FILE_RECORD_LENGTH_GENOME = 23;

    public static int MASTER_FILE_RECORD_LENGTH_EXOME = 31;

    public static int INDEL_BASE_VALUE = 200;

    public static int DETAIL_RECORD_PADDING = 5;

    public static String getFirstGenomeCharacter(String sStringIn) {

        String sReturnChar = "Z";

        for (int ii = 0; ii < sStringIn.length(); ii++) {

            if ((Character.isLetter(sStringIn.charAt(ii))) || (sStringIn.charAt(ii) == '*')) {
                sReturnChar = String.valueOf(sStringIn.charAt(ii));
                return sReturnChar;
            }
        }

        return sReturnChar;

    }

    public static List<String> tabbedStringToList(String sTabbedStringIn) {

        List<String> tReturnList = new ArrayList<String>();

        StrTokenizer tTokenizer = new StrTokenizer();
        tTokenizer.reset(sTabbedStringIn);
        while (tTokenizer.hasNext()) {

            tReturnList.add(tTokenizer.nextToken());
        }

        return tReturnList;
    }

    public static List<String> removeFromFrontOfList(List<String> tListIn, int tNumElementsIn) {

        List<String> tReturnListCopy = new ArrayList<String>(tListIn);

        for (int ii = 0; ii < tNumElementsIn; ii++) {

            tReturnListCopy.remove(0);
        }

        return tReturnListCopy;
    }

    public static String byteToBits(byte b) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < 8; i++)
            buf.append((int) (b >> (8 - (i + 1)) & 0x0001));
        return buf.toString();
    }

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
    }

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
    }

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

        return tDetailP;
    }

}
