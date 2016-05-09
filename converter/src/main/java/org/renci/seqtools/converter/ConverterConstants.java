package org.renci.seqtools.converter;

import java.util.regex.Pattern;

/**
 * ConverterConstants
 * <p>
 * A uninstantiable constants object.
 * 
 * @author k47k4705
 * 
 */
public class ConverterConstants {

    // Header info.
    // What is the version of this software?
    public static final int THIS_SOFTWARE_VERSION_NUMBER = 1;

    // What version of the VCF standard do we build to?
    public static final int VCF_STANDARD_VERSION = 4;

    // How far (in bytes) is the end position from the start of the master file?
    public static final long HEADER_DISTANCE_TO_END_POSITION = 24;

    // Maximum length of VCF and BAM file names as a byte array. (Yes, we're
    // truncating these names. Sorry.
    public static final int CONVERTED_FILES_BYTE_ARRAY_LENGTH = 50;

    // Location in line of VCF data for the POS column.
    public static final int LOCATION_VCF_POS_COLUMN = 0;

    // Positions of VCF data in a List<String>
    public static int REFERENCE_COORDINATE_POSITION = 0;

    public static int GENOTYPE_POSITION = 1;

    public static int CONSENSUS_QUALITY_POSITION = 2;

    public static int SNP_QUALITY_POSITION = 3;

    public static int MAPPING_QUALITY_POSITION = 4;

    public static int READ_DEPTH_POSITION = 5;

    public static int READ_BASES_POSITION = 6;

    public static int READ_QUALITY_POSITION = 7;

    // public static int REFERENCE_COORDINATE_POSITION = -1;
    //
    // public static int GENOTYPE_POSITION = 0;
    //
    // public static int CONSENSUS_QUALITY_POSITION = 1;
    //
    // public static int SNP_QUALITY_POSITION = 2;
    //
    // public static int MAPPING_QUALITY_POSITION = 3;
    //
    // public static int READ_DEPTH_POSITION = 4;
    //
    // public static int READ_BASES_POSITION = 5;
    //
    // public static int READ_QUALITY_POSITION = 6;
    //
    // public static int INDEL_POSITION = 8;

    // Matches against a "+1C" or "+2ag"
    public static String MARKER_INSERTION = ".*\\+\\d[atcgATCG].*";

    // Matches against a "-1c" or "-3aac"
    public static String MARKER_DELETION = ".*\\-\\d[atcgATCG].*";

    public static Pattern PATTERN_INSERT = Pattern.compile(".*\\+(\\d)[atcgATCG].*");

    public static Pattern PATTERN_DELETE = Pattern.compile(".*\\-(\\d)[atcgATCG].*");

    private ConverterConstants() {
    }

} // end ConverterConstants
