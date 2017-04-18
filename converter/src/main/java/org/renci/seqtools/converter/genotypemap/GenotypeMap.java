package org.renci.seqtools.converter.genotypemap;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class GenotypeMap {

    private static Map<String, Byte> tMapOfBytes = new TreeMap<String, Byte>();

    private GenotypeMap() {
        setUpGenotypeMap();
    }

    public static GenotypeMap getInstance() {
        return new GenotypeMap();
    }

    private void setUpGenotypeMap() {

        BigInteger tAValue = new BigInteger("00000001", 2);
        BigInteger tCValue = new BigInteger("00000010", 2);
        BigInteger tGValue = new BigInteger("00000011", 2);
        BigInteger tTValue = new BigInteger("00000100", 2);
        BigInteger tPlusValue = new BigInteger("00000101", 2);
        BigInteger tMinusValue = new BigInteger("00000110", 2);
        BigInteger tNullValue = new BigInteger("00000111", 2);
        BigInteger tNoCallValue = new BigInteger("00001000", 2);

        BigInteger tMValue = new BigInteger("00001001", 2);
        BigInteger tRValue = new BigInteger("00001010", 2);
        BigInteger tWValue = new BigInteger("00001011", 2);
        BigInteger tSValue = new BigInteger("00001100", 2);
        BigInteger tYValue = new BigInteger("00001101", 2);
        BigInteger tKValue = new BigInteger("00001110", 2);
        BigInteger tDeleteAValue = new BigInteger("00001111", 2);
        BigInteger tDeleteCValue = new BigInteger("00010000", 2);
        BigInteger tDeleteGValue = new BigInteger("00010001", 2);
        BigInteger tDeleteTValue = new BigInteger("00010010", 2);
        BigInteger tInsertAValue = new BigInteger("00010011", 2);
        BigInteger tInsertCValue = new BigInteger("00010100", 2);
        BigInteger tInsertGValue = new BigInteger("00010101", 2);
        BigInteger tInsertTValue = new BigInteger("00010110", 2);
        BigInteger tNValue = new BigInteger("00010111", 2);
        BigInteger tCAValue = new BigInteger("00011000", 2);
        BigInteger tGAValue = new BigInteger("00011001", 2);
        BigInteger tTAValue = new BigInteger("00011010", 2);
        BigInteger tGCValue = new BigInteger("00011011", 2);
        BigInteger tTCValue = new BigInteger("00011100", 2);
        BigInteger tTGValue = new BigInteger("00011101", 2);

        BigInteger tLittleAValue = new BigInteger("00011110", 2);
        BigInteger tLittleCValue = new BigInteger("00011111", 2);
        BigInteger tLittleGValue = new BigInteger("00100000", 2);
        BigInteger tLittleTValue = new BigInteger("00100001", 2);

        BigInteger tLittleNValue = new BigInteger("00100010", 2);
        BigInteger tLittleMValue = new BigInteger("00100011", 2);
        BigInteger tLittleRValue = new BigInteger("00100100", 2);
        BigInteger tLittleWValue = new BigInteger("00100101", 2);
        BigInteger tLittleSValue = new BigInteger("00100110", 2);
        BigInteger tLittleYValue = new BigInteger("00100111", 2);
        BigInteger tLittleKValue = new BigInteger("00101000", 2);
        BigInteger tDeleteLittleAValue = new BigInteger("00101001", 2);
        BigInteger tDeleteLittleCValue = new BigInteger("00101010", 2);
        BigInteger tDeleteLittleGValue = new BigInteger("00101011", 2);
        BigInteger tDeleteLittleTValue = new BigInteger("00101100", 2);
        BigInteger tInsertLittleAValue = new BigInteger("00101101", 2);
        BigInteger tInsertLittleCValue = new BigInteger("00101110", 2);
        BigInteger tInsertLittleGValue = new BigInteger("00101111", 2);
        BigInteger tInsertLittleTValue = new BigInteger("00110000", 2);
        BigInteger tLittleCAValue = new BigInteger("00110001", 2);
        BigInteger tLittleGAValue = new BigInteger("00110010", 2);
        BigInteger tLittleTAValue = new BigInteger("00110011", 2);
        BigInteger tLittleGCValue = new BigInteger("00110100", 2);
        BigInteger tLittleTCValue = new BigInteger("00110101", 2);
        BigInteger tLittleTGValue = new BigInteger("00110110", 2);

        tMapOfBytes.put("A", new Byte(tAValue.byteValue()));
        tMapOfBytes.put("C", new Byte(tCValue.byteValue()));
        tMapOfBytes.put("G", new Byte(tGValue.byteValue()));
        tMapOfBytes.put("T", new Byte(tTValue.byteValue()));
        tMapOfBytes.put("+", new Byte(tPlusValue.byteValue()));
        tMapOfBytes.put("-", new Byte(tMinusValue.byteValue()));
        tMapOfBytes.put("null", new Byte(tNullValue.byteValue()));
        tMapOfBytes.put("*", new Byte(tNoCallValue.byteValue()));

        tMapOfBytes.put("N", new Byte(tNValue.byteValue()));
        tMapOfBytes.put("M", new Byte(tMValue.byteValue()));
        tMapOfBytes.put("R", new Byte(tRValue.byteValue()));
        tMapOfBytes.put("W", new Byte(tWValue.byteValue()));
        tMapOfBytes.put("S", new Byte(tSValue.byteValue()));
        tMapOfBytes.put("Y", new Byte(tYValue.byteValue()));
        tMapOfBytes.put("K", new Byte(tKValue.byteValue()));

        tMapOfBytes.put("A/C", new Byte(tMValue.byteValue()));
        tMapOfBytes.put("C/A", new Byte(tCAValue.byteValue()));
        tMapOfBytes.put("A/G", new Byte(tRValue.byteValue()));
        tMapOfBytes.put("G/A", new Byte(tGAValue.byteValue()));
        tMapOfBytes.put("A/T", new Byte(tWValue.byteValue()));
        tMapOfBytes.put("T/A", new Byte(tTAValue.byteValue()));
        tMapOfBytes.put("C/G", new Byte(tSValue.byteValue()));
        tMapOfBytes.put("G/C", new Byte(tGCValue.byteValue()));
        tMapOfBytes.put("C/T", new Byte(tYValue.byteValue()));
        tMapOfBytes.put("T/C", new Byte(tTCValue.byteValue()));
        tMapOfBytes.put("G/T", new Byte(tKValue.byteValue()));
        tMapOfBytes.put("T/G", new Byte(tTGValue.byteValue()));

        tMapOfBytes.put("-/A", new Byte(tDeleteAValue.byteValue()));
        tMapOfBytes.put("-/C", new Byte(tDeleteCValue.byteValue()));
        tMapOfBytes.put("-/G", new Byte(tDeleteGValue.byteValue()));
        tMapOfBytes.put("-/T", new Byte(tDeleteTValue.byteValue()));
        tMapOfBytes.put("+/A", new Byte(tInsertAValue.byteValue()));
        tMapOfBytes.put("+/C", new Byte(tInsertCValue.byteValue()));
        tMapOfBytes.put("+/G", new Byte(tInsertGValue.byteValue()));
        tMapOfBytes.put("+/T", new Byte(tInsertTValue.byteValue()));

        tMapOfBytes.put("a", new Byte(tLittleAValue.byteValue()));
        tMapOfBytes.put("c", new Byte(tLittleCValue.byteValue()));
        tMapOfBytes.put("g", new Byte(tLittleGValue.byteValue()));
        tMapOfBytes.put("t", new Byte(tLittleTValue.byteValue()));

        tMapOfBytes.put("n", new Byte(tLittleNValue.byteValue()));
        tMapOfBytes.put("m", new Byte(tLittleMValue.byteValue()));
        tMapOfBytes.put("r", new Byte(tLittleRValue.byteValue()));
        tMapOfBytes.put("w", new Byte(tLittleWValue.byteValue()));
        tMapOfBytes.put("s", new Byte(tLittleSValue.byteValue()));
        tMapOfBytes.put("y", new Byte(tLittleYValue.byteValue()));
        tMapOfBytes.put("k", new Byte(tLittleKValue.byteValue()));
        tMapOfBytes.put("-/a", new Byte(tDeleteLittleAValue.byteValue()));
        tMapOfBytes.put("-/c", new Byte(tDeleteLittleCValue.byteValue()));
        tMapOfBytes.put("-/g", new Byte(tDeleteLittleGValue.byteValue()));
        tMapOfBytes.put("-/t", new Byte(tDeleteLittleTValue.byteValue()));
        tMapOfBytes.put("+/a", new Byte(tInsertLittleAValue.byteValue()));
        tMapOfBytes.put("+/c", new Byte(tInsertLittleCValue.byteValue()));
        tMapOfBytes.put("+/g", new Byte(tInsertLittleGValue.byteValue()));
        tMapOfBytes.put("+/t", new Byte(tInsertLittleTValue.byteValue()));
        tMapOfBytes.put("c/a", new Byte(tLittleCAValue.byteValue()));
        tMapOfBytes.put("g/a", new Byte(tLittleGAValue.byteValue()));
        tMapOfBytes.put("t/a", new Byte(tLittleTAValue.byteValue()));
        tMapOfBytes.put("g/c", new Byte(tLittleGCValue.byteValue()));
        tMapOfBytes.put("t/c", new Byte(tLittleTCValue.byteValue()));
        tMapOfBytes.put("t/g", new Byte(tLittleTGValue.byteValue()));

    }

    public boolean containsKey(String sKeyIn) {
        return tMapOfBytes.containsKey(sKeyIn);
    }

    public Byte get(String sKeyIn) {
        return (Byte) tMapOfBytes.get(sKeyIn);
    }

    public String getIndelKey(int iValueIn) {
        String sIndelKey = "";

        byte tMyByte = tMapOfBytes.get("I").byteValue();
        BigInteger tBI = new BigInteger(byteToBits(tMyByte), 2);

        Integer tInt = new Integer(iValueIn);
        BigInteger tBI2 = new BigInteger(byteToBits(tInt.byteValue()), 2);

        BigInteger tBI3 = tBI2.subtract(tBI);

        Iterator<Map.Entry<String, Byte>> iter = tMapOfBytes.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Byte> entry = iter.next();
            BigInteger tBLoop = new BigInteger(byteToBits(entry.getValue().byteValue()), 2);

            if (tBLoop.intValue() == tBI3.intValue()) {

                sIndelKey = (String) entry.getKey();
            }
        }

        if (sIndelKey == "") {

            sIndelKey = "Z";
        }

        return sIndelKey;
    }

    public String getIndelKey(byte tByteIn) {
        String sIndelKey = "";

        byte tMyByte = tMapOfBytes.get("I").byteValue();
        BigInteger tBI = new BigInteger(byteToBits(tMyByte), 2);

        BigInteger tBI2 = new BigInteger(byteToBits(tByteIn), 2);

        BigInteger tBI3 = tBI2.subtract(tBI);

        Iterator<Map.Entry<String, Byte>> iter = tMapOfBytes.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Byte> entry = iter.next();
            BigInteger tBLoop = new BigInteger(byteToBits(entry.getValue().byteValue()), 2);

            if (tBLoop.byteValue() == tBI3.byteValue()) {

                sIndelKey = (String) entry.getKey();
            }
        }

        if (sIndelKey == "") {

            sIndelKey = "Z";
        }

        return sIndelKey;

    }

    public String getNonIndelKey(byte tByteIn) {

        String sIndelKey = "";

        BigInteger tBI2 = new BigInteger(byteToBits(tByteIn), 2);

        Iterator<Map.Entry<String, Byte>> iter = tMapOfBytes.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Byte> entry = iter.next();
            BigInteger tBLoop = new BigInteger(byteToBits(entry.getValue().byteValue()), 2);
            if (tBLoop.byteValue() == tBI2.byteValue()) {

                sIndelKey = (String) entry.getKey();
                break;
            }
        }

        if (sIndelKey == "") {
            sIndelKey = "Z";
        }

        return sIndelKey;

    }

    public String getKeyForVCFBasedGenotype(byte tByteIn) {

        String sVCFBasedKey = "";

        BigInteger tBI2 = new BigInteger(byteToBits(tByteIn), 2);

        Iterator<Map.Entry<String, Byte>> iter = tMapOfBytes.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Byte> entry = iter.next();
            BigInteger tBLoop = new BigInteger(byteToBits(entry.getValue().byteValue()), 2);
            if (tBLoop.byteValue() == tBI2.byteValue()) {
                sVCFBasedKey = (String) entry.getKey();
                break;
            }
        }

        if (sVCFBasedKey == "") {
            sVCFBasedKey = "Z";
        }

        return sVCFBasedKey;

    }

    public boolean isGTBaseIndelValue(int iIndelValue) {

        boolean bHasIndel = false;

        byte tMyByte = tMapOfBytes.get("I").byteValue();
        BigInteger tBI = new BigInteger(byteToBits(tMyByte), 2);
        Integer tInt = new Integer(iIndelValue);
        BigInteger tBI2 = new BigInteger(byteToBits(tInt.byteValue()), 2);

        if (tBI2.intValue() > tBI.intValue()) {
            bHasIndel = true;
        }

        return bHasIndel;
    }

    public int addToBaseIndel(int iNumberToAdd) {
        int iNumberToReturn = 0;

        byte tMyByte = tMapOfBytes.get("I").byteValue();

        BigInteger tBI = new BigInteger(byteToBits(tMyByte), 2);

        Integer tIntToAdd = new Integer(iNumberToAdd);

        BigInteger tBIToAdd = new BigInteger(byteToBits(tIntToAdd.byteValue()), 2);

        BigInteger tBIToReturn = tBI.add(tBIToAdd);

        iNumberToReturn = tBIToReturn.intValue();

        return iNumberToReturn;

    }

    public byte addToBaseIndel(byte tByteIn) {

        byte tIndelByte = tMapOfBytes.get("I").byteValue();

        BigInteger tIndelBI = new BigInteger(byteToBits(tIndelByte), 2);

        BigInteger tInputBI = new BigInteger(byteToBits(tByteIn), 2);

        BigInteger tSumBI = tIndelBI.add(tInputBI);

        return tSumBI.byteValue();

    }

    private static String byteToBits(byte b) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < 8; i++)
            buf.append((int) (b >> (8 - (i + 1)) & 0x0001));
        return buf.toString();
    }

}
