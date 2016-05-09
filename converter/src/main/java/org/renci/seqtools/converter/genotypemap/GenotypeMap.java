package org.renci.seqtools.converter.genotypemap;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * GenotypeMap is a common wrapper around a Map of genotypes.
 * <p>
 * The genotypes are the "A", "T", "G", "C", etc., of pileup files and VCF files.
 * <p>
 * The idea is to use this object as common code between the consensus file-making code and the consensus file-querying
 * code.
 * <p>
 * Some of the implications of this file are in heterozygous representation. The IUB codes essentially say "C or G" or
 * "C/G". However, in pileup or VCF representation we need to expect "C/G" will come through as "G/C". When we find a
 * heterozygote in the VCF or pileup file, we want the C/G or G/C to point to the same IUB code.
 * <p>
 * This object provides a handy way of accessing these common bits for both the converter and reader projects.
 * 
 * @author k47k4705
 *
 */
public class GenotypeMap {

    // Map of genotype key to byte storage value.
    // private static Map<String, Byte> tMapOfBytes = new HashMap<String, Byte>();
    private static Map<String, Byte> tMapOfBytes = new TreeMap<String, Byte>();

    /**
     * GenotypeMap private constructor
     */
    private GenotypeMap() {
        // Set up the map.
        setUpGenotypeMap();
    } // end GenotypeMap

    /**
     * getInstance()
     * <p>
     * Static private instance constructor.
     * <p>
     * 
     * @return GenotypeMap -- a single instance of this object.
     */
    public static GenotypeMap getInstance() {
        return new GenotypeMap();
    } // end getInstance

    /**
     * setUpGenotypeMap()
     * <p>
     * setUpGenotypeMap() populates the map with the correct genotype/byte mappings.
     * <p>
     * The idea here is to identify indel values given a base indel starting point. Here the starting point is "200" or
     * tIndelValue = new BigInteger("11001000", 2);
     * <p>
     * So, if you are an indel value in a pileup file, you'll be identified starting from the "200" base. If you are
     * above "200", you are an indel.
     * <p>
     * If you are below "200", you are not an indel.
     */
    private void setUpGenotypeMap() {

        // Genotype mapping. Capital letters.
        BigInteger tAValue = new BigInteger("00000001", 2); // 1
        BigInteger tCValue = new BigInteger("00000010", 2); // 2
        BigInteger tGValue = new BigInteger("00000011", 2); // 3
        BigInteger tTValue = new BigInteger("00000100", 2); // 4
        BigInteger tPlusValue = new BigInteger("00000101", 2); // 5
        BigInteger tMinusValue = new BigInteger("00000110", 2); // 6
        BigInteger tNullValue = new BigInteger("00000111", 2); // 7
        BigInteger tNoCallValue = new BigInteger("00001000", 2); // 8

        // Heterozygous Genotype mapping.
        BigInteger tMValue = new BigInteger("00001001", 2); // M = A/C // 9
        BigInteger tRValue = new BigInteger("00001010", 2); // R = A/G // 10
        BigInteger tWValue = new BigInteger("00001011", 2); // W = A/T // 11
        BigInteger tSValue = new BigInteger("00001100", 2); // S = C/G // 12
        BigInteger tYValue = new BigInteger("00001101", 2); // Y = C/T // 13
        BigInteger tKValue = new BigInteger("00001110", 2); // K = G/T // 14
        BigInteger tDeleteAValue = new BigInteger("00001111", 2); // -/A // 15
        BigInteger tDeleteCValue = new BigInteger("00010000", 2); // -/C // 16
        BigInteger tDeleteGValue = new BigInteger("00010001", 2); // -/G // 17
        BigInteger tDeleteTValue = new BigInteger("00010010", 2); // -/T // 18
        BigInteger tInsertAValue = new BigInteger("00010011", 2); // +/A // 19
        BigInteger tInsertCValue = new BigInteger("00010100", 2); // +/C // 20
        BigInteger tInsertGValue = new BigInteger("00010101", 2); // +/G // 21
        BigInteger tInsertTValue = new BigInteger("00010110", 2); // +/T // 22
        BigInteger tNValue = new BigInteger("00010111", 2); // +/T // 23
        BigInteger tCAValue = new BigInteger("00011000", 2); // C/A // 24
        BigInteger tGAValue = new BigInteger("00011001", 2); // G/A // 25
        BigInteger tTAValue = new BigInteger("00011010", 2); // T/A // 26
        BigInteger tGCValue = new BigInteger("00011011", 2); // G/C // 27
        BigInteger tTCValue = new BigInteger("00011100", 2); // T/C // 28
        BigInteger tTGValue = new BigInteger("00011101", 2); // T/G // 29

        // Genotype mapping. Little letters.
        BigInteger tLittleAValue = new BigInteger("00011110", 2); // 30
        BigInteger tLittleCValue = new BigInteger("00011111", 2); // 31
        BigInteger tLittleGValue = new BigInteger("00100000", 2); // 32
        BigInteger tLittleTValue = new BigInteger("00100001", 2); // 33

        // Lowercase heterozygous
        BigInteger tLittleNValue = new BigInteger("00100010", 2); // 34
        BigInteger tLittleMValue = new BigInteger("00100011", 2); // m = a/c 35
        BigInteger tLittleRValue = new BigInteger("00100100", 2); // r = a/g 36
        BigInteger tLittleWValue = new BigInteger("00100101", 2); // w = a/t 37
        BigInteger tLittleSValue = new BigInteger("00100110", 2); // s = c/g 38
        BigInteger tLittleYValue = new BigInteger("00100111", 2); // y = c/t 39
        BigInteger tLittleKValue = new BigInteger("00101000", 2); // k = g/t 40
        BigInteger tDeleteLittleAValue = new BigInteger("00101001", 2); // -/a 41
        BigInteger tDeleteLittleCValue = new BigInteger("00101010", 2); // -/c 42
        BigInteger tDeleteLittleGValue = new BigInteger("00101011", 2); // -/g 43
        BigInteger tDeleteLittleTValue = new BigInteger("00101100", 2); // -/t 44
        BigInteger tInsertLittleAValue = new BigInteger("00101101", 2); // +/a 45
        BigInteger tInsertLittleCValue = new BigInteger("00101110", 2); // +/c 46
        BigInteger tInsertLittleGValue = new BigInteger("00101111", 2); // +/g 47
        BigInteger tInsertLittleTValue = new BigInteger("00110000", 2); // +/t 48
        BigInteger tLittleCAValue = new BigInteger("00110001", 2); // c/a 49
        BigInteger tLittleGAValue = new BigInteger("00110010", 2); // g/a 50
        BigInteger tLittleTAValue = new BigInteger("00110011", 2); // t/a 51
        BigInteger tLittleGCValue = new BigInteger("00110100", 2); // g/c 52
        BigInteger tLittleTCValue = new BigInteger("00110101", 2); // t/c 53
        BigInteger tLittleTGValue = new BigInteger("00110110", 2); // t/g 54

        // BigInteger tIndelValue = new BigInteger("11001000", 2); // Indel flag // 128 + 64 + 8 = 200

        // Capital letters.
        // Regular map key/values
        tMapOfBytes.put("A", new Byte(tAValue.byteValue()));
        tMapOfBytes.put("C", new Byte(tCValue.byteValue()));
        tMapOfBytes.put("G", new Byte(tGValue.byteValue()));
        tMapOfBytes.put("T", new Byte(tTValue.byteValue()));
        tMapOfBytes.put("+", new Byte(tPlusValue.byteValue())); // insertion
        tMapOfBytes.put("-", new Byte(tMinusValue.byteValue())); // deletion
        tMapOfBytes.put("null", new Byte(tNullValue.byteValue())); // null (no reads at this location)
        tMapOfBytes.put("*", new Byte(tNoCallValue.byteValue())); // no call

        // Heterozygous map key/values
        tMapOfBytes.put("N", new Byte(tNValue.byteValue())); // A/C
        tMapOfBytes.put("M", new Byte(tMValue.byteValue())); // A/C
        tMapOfBytes.put("R", new Byte(tRValue.byteValue())); // A/G
        tMapOfBytes.put("W", new Byte(tWValue.byteValue())); // A/T
        tMapOfBytes.put("S", new Byte(tSValue.byteValue())); // C/G
        tMapOfBytes.put("Y", new Byte(tYValue.byteValue())); // C/T
        tMapOfBytes.put("K", new Byte(tKValue.byteValue())); // G/T

        // Good idea to represent the C/G as "C or G" and "G or C" which all point to the "M" key.
        // We can't count on VCF files to contain C/G. We might find G/C but still want to count
        // that as heterozygous and point it to our "M" value.
        tMapOfBytes.put("A/C", new Byte(tMValue.byteValue())); // A/C => M
        tMapOfBytes.put("C/A", new Byte(tCAValue.byteValue())); // C/A => M
        tMapOfBytes.put("A/G", new Byte(tRValue.byteValue())); // A/G => R
        tMapOfBytes.put("G/A", new Byte(tGAValue.byteValue())); // G/A => R
        tMapOfBytes.put("A/T", new Byte(tWValue.byteValue())); // A/T => W
        tMapOfBytes.put("T/A", new Byte(tTAValue.byteValue())); // T/A => W
        tMapOfBytes.put("C/G", new Byte(tSValue.byteValue())); // C/G => S
        tMapOfBytes.put("G/C", new Byte(tGCValue.byteValue())); // G/C => S
        tMapOfBytes.put("C/T", new Byte(tYValue.byteValue())); // C/T => Y
        tMapOfBytes.put("T/C", new Byte(tTCValue.byteValue())); // T/C => Y
        tMapOfBytes.put("G/T", new Byte(tKValue.byteValue())); // G/T => K
        tMapOfBytes.put("T/G", new Byte(tTGValue.byteValue())); // T/G => K

        // Inserts/Deletes starting with A, C, G, T.
        tMapOfBytes.put("-/A", new Byte(tDeleteAValue.byteValue())); // -/A
        tMapOfBytes.put("-/C", new Byte(tDeleteCValue.byteValue())); // -/C
        tMapOfBytes.put("-/G", new Byte(tDeleteGValue.byteValue())); // -/G
        tMapOfBytes.put("-/T", new Byte(tDeleteTValue.byteValue())); // -/T
        tMapOfBytes.put("+/A", new Byte(tInsertAValue.byteValue())); // +/A
        tMapOfBytes.put("+/C", new Byte(tInsertCValue.byteValue())); // +/C
        tMapOfBytes.put("+/G", new Byte(tInsertGValue.byteValue())); // +/G
        tMapOfBytes.put("+/T", new Byte(tInsertTValue.byteValue())); // +/T

        // Lowercase letters.
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

        // Indel value -- a base flag for identifying indel records.
        // We're using this flag as a base value to be able to tell at
        // a glance whether a record in the master file contains an indel
        // record in the detail file.
        //
        // Genotypes that contain indels are additive on top of this base
        // indel value: "11000000" + "00000001" is indel value "A".
        // indel value: "11000000" + "00000010" is indel value "C".
        // tMapOfBytes.put("I", new Byte(tIndelValue.byteValue()));

    } // end setUpGenotypeMap

    /**
     * containsKey()
     * <p>
     * Returns a true/false on whether the wrapped Map contains a Key.
     * 
     * @param sKeyIn
     *            -- the key to check.
     * @return boolean -- true: contains the key, false: does not contain the key.
     */
    public boolean containsKey(String sKeyIn) {
        return tMapOfBytes.containsKey(sKeyIn);
    } // end containsKey

    /**
     * get()
     * <p>
     * 
     * @param sKeyIn
     *            -- the key to check.
     * @return Byte -- the Byte value contained in the Map for the input key.
     */
    public Byte get(String sKeyIn) {
        return (Byte) tMapOfBytes.get(sKeyIn);
    } // end get

    /**
     * getIndelKey()
     * <p>
     * Indel genotype values start from a base indel value: 192.
     * <p>
     * This is useful because we need to tell at a glance whether a record in the master file has an indel value in the
     * detail file.
     * <p>
     * So, we take in an additive int value derived from the master file. We look up the master value and s
     * 
     * @param iValueIn
     *            -- the additive (base indel value + genotype) indel value to retrieve.
     * @return String -- the implicit key of this additive indel value.
     */
    public String getIndelKey(int iValueIn) {
        String sIndelKey = "";

        // Get "indel" "I" as a byte.
        byte tMyByte = tMapOfBytes.get("I").byteValue();
        BigInteger tBI = new BigInteger(byteToBits(tMyByte), 2);
        // Get int input into a BigInteger.
        Integer tInt = new Integer(iValueIn);
        BigInteger tBI2 = new BigInteger(byteToBits(tInt.byteValue()), 2);
        // System.out.println("tBI: " + tBI.intValue() + " " + tBI2.intValue() + " " + byteToBits(tInt.byteValue()));
        // Get difference between larger (should be larger) input and "base" indel value.
        BigInteger tBI3 = tBI2.subtract(tBI);
        // System.out.println("tBI3: " + tBI3.intValue());

        // System.out.println("indel byte: " + byteToBits(tMyByte));

        Iterator<Map.Entry<String, Byte>> iter = tMapOfBytes.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Byte> entry = iter.next();
            BigInteger tBLoop = new BigInteger(byteToBits(entry.getValue().byteValue()), 2);
            // System.out.println("entry.value: " + new BigInteger(byteToBits(entry.getValue().byteValue()),
            // 2).intValue());
            if (tBLoop.intValue() == tBI3.intValue()) {
                // System.out.println("Equal: " + tBLoop.intValue() + " " + tBI3.intValue());
                sIndelKey = (String) entry.getKey();
            } // end if
        } // end while

        // Check if anything happened: if a value was set.
        if (sIndelKey == "") {
            // sIndelKey = Integer.toString(iValueIn);
            sIndelKey = "Z";
        } // end if

        return sIndelKey;
    } // end getIndelKey

    /**
     * getIndelKey
     * <p>
     * Overloaded to accept a byte as the input value to retrieve.
     * 
     * @param tByteIn
     *            -- the byte value associated with the key to return.
     * @return String -- the key associated with the input value.
     */
    public String getIndelKey(byte tByteIn) {
        String sIndelKey = "";

        // Get "indel" "I" as a byte.
        byte tMyByte = tMapOfBytes.get("I").byteValue();
        BigInteger tBI = new BigInteger(byteToBits(tMyByte), 2);

        BigInteger tBI2 = new BigInteger(byteToBits(tByteIn), 2);
        // System.out.println("tBI: " + tBI.intValue() + " " + tBI2.intValue() + " " + byteToBits(tInt.byteValue()));
        // Get difference between larger (should be larger) input and "base" indel value.
        BigInteger tBI3 = tBI2.subtract(tBI);
        // System.out.println("tBI3: " + tBI3.intValue());

        // System.out.println("indel byte: " + byteToBits(tMyByte));

        Iterator<Map.Entry<String, Byte>> iter = tMapOfBytes.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Byte> entry = iter.next();
            BigInteger tBLoop = new BigInteger(byteToBits(entry.getValue().byteValue()), 2);
            // System.out.println("entry.value: " + new BigInteger(byteToBits(entry.getValue().byteValue()),
            // 2).intValue());
            if (tBLoop.byteValue() == tBI3.byteValue()) {
                // System.out.println("Equal: " + tBLoop.intValue() + " " + tBI3.intValue());
                sIndelKey = (String) entry.getKey();
            } // end if
        } // end while

        // Check if anything happened: if a value was set.
        if (sIndelKey == "") {
            // sIndelKey = Integer.toString(iValueIn);
            sIndelKey = "Z";
        } // end if

        return sIndelKey;

    } // end getIndelKey

    /**
     * getNonIndelKey
     * <p>
     * Gets a non-indel key for a given byte input.
     * 
     * @param tByteIn
     *            -- the byte value associated with the key to return.
     * @return String -- the key associated with the input value.
     */
    public String getNonIndelKey(byte tByteIn) {
        // Return key.
        String sIndelKey = "";
        // BigInteger to catch and convert key using the input byte.
        BigInteger tBI2 = new BigInteger(byteToBits(tByteIn), 2);
        // Loop over the map, extract key for this byte.
        Iterator<Map.Entry<String, Byte>> iter = tMapOfBytes.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Byte> entry = iter.next();
            BigInteger tBLoop = new BigInteger(byteToBits(entry.getValue().byteValue()), 2);
            if (tBLoop.byteValue() == tBI2.byteValue()) {
                // System.out.println("Equal: " + tBLoop.intValue() + " " + tBI3.intValue());
                sIndelKey = (String) entry.getKey();
                break;
            } // end if
        } // end while

        // Check if anything happened: if a value was set. If not, default to a known "non-sense" value: "Z".
        if (sIndelKey == "") {
            sIndelKey = "Z";
        } // end if

        return sIndelKey;

    } // end getNonIndelKey

    /**
     * getKeyForVCFBasedGenotype
     * <p>
     * Gets a genotype key for consensus files made from VCF files given a byte input.
     * 
     * @param tByteIn
     *            -- the byte value associated with the key to return.
     * @return String -- the key associated with the input value.
     */
    public String getKeyForVCFBasedGenotype(byte tByteIn) {
        // Return key.
        String sVCFBasedKey = "";
        // BigInteger to catch and convert key using the input byte.
        BigInteger tBI2 = new BigInteger(byteToBits(tByteIn), 2);
        // Loop over map, extract key for this byte.
        Iterator<Map.Entry<String, Byte>> iter = tMapOfBytes.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Byte> entry = iter.next();
            BigInteger tBLoop = new BigInteger(byteToBits(entry.getValue().byteValue()), 2);
            if (tBLoop.byteValue() == tBI2.byteValue()) {
                sVCFBasedKey = (String) entry.getKey();
                break;
            } // end if
        } // end while

        // Check if anything happened: if a value was set. If not, default to a known "non-sense" value: "Z".
        if (sVCFBasedKey == "") {
            sVCFBasedKey = "Z";
        } // end if

        return sVCFBasedKey;

    } // end getKeyForVCFBasedGenotype

    /**
     * isGTBaseIndelValue()
     * <p>
     * Tells whether a possible input indel value (the genotype value pulled from a master file) is actually considered
     * an indel value (is greater than the 200 base indel value).
     * 
     * @param iIndelValue
     *            -- the candidate indel value used to determine if the master record has a detail record (if the master
     *            record is an indel record).
     * @return boolean -- true: is an indel value (has an indel detail record), false: is not an indel value (does not
     *         have an indel detail record).
     */
    public boolean isGTBaseIndelValue(int iIndelValue) {
        // Return boolean.
        boolean bHasIndel = false;

        // Get "indel" "I" as a byte.
        byte tMyByte = tMapOfBytes.get("I").byteValue();
        BigInteger tBI = new BigInteger(byteToBits(tMyByte), 2);
        Integer tInt = new Integer(iIndelValue);
        BigInteger tBI2 = new BigInteger(byteToBits(tInt.byteValue()), 2);

        // Is the input indel value > the "base" indel value?
        if (tBI2.intValue() > tBI.intValue()) {
            bHasIndel = true;
        } // end if

        return bHasIndel;
    } // end isGTBaseIndelValue

    /**
     * addToBaseIndel()
     * <p>
     * Adds a number to the base indel value.
     * 
     * @param tNumberToAdd
     *            -- the number to add to the base indel value.
     * @return int -- the resultant number of the base indel value addition.
     */
    public int addToBaseIndel(int iNumberToAdd) {
        int iNumberToReturn = 0;
        // Get "indel" "I" as a byte.
        byte tMyByte = tMapOfBytes.get("I").byteValue();
        // Convert to BigInteger.
        BigInteger tBI = new BigInteger(byteToBits(tMyByte), 2);

        // Make an Integer from the int input.
        Integer tIntToAdd = new Integer(iNumberToAdd);

        // Make a BigInteger from this Integer.
        BigInteger tBIToAdd = new BigInteger(byteToBits(tIntToAdd.byteValue()), 2);

        // Add to BigIntegers together into third BigInteger.
        BigInteger tBIToReturn = tBI.add(tBIToAdd);

        // Set int to return.
        iNumberToReturn = tBIToReturn.intValue();

        return iNumberToReturn;

    } // end addToBaseIndel

    /**
     * addToBaseIndel()
     * <p>
     * Overloaded method to return the value of a base indel byte added to the input byte.
     * 
     * @param tByteIn
     *            -- the input byte to add an indel byte to.
     * @return byte -- the value of an indel byte added to the input byte.
     */
    public byte addToBaseIndel(byte tByteIn) {
        // Get indel "I" as a byte.
        byte tIndelByte = tMapOfBytes.get("I").byteValue();
        // Convert to BigInteger.
        BigInteger tIndelBI = new BigInteger(byteToBits(tIndelByte), 2);

        // Make an input BigInteger.
        BigInteger tInputBI = new BigInteger(byteToBits(tByteIn), 2);

        // Add to BigIntegers together to make third BigInteger.
        BigInteger tSumBI = tIndelBI.add(tInputBI);
        // System.out.println("addToBaseIndel: " + tSumBI.intValue() + " indel byte value: " + tIndelBI.byteValue() + "
        // " + tSumBI.byteValue());
        return tSumBI.byteValue();

    } // end addToBaseIndel

    /**
     * byteToBits()
     * <p>
     * Turn a byte into a String of bits: "10000001"
     * 
     * @param b
     *            -- the byte to convert to a String
     * @return String -- the String of bits underlying that byte: "10000001"
     */
    private static String byteToBits(byte b) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < 8; i++)
            buf.append((int) (b >> (8 - (i + 1)) & 0x0001));
        return buf.toString();
    }

} // end GenotypeMap
