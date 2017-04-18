package org.renci.seqtools.converter;

import java.util.regex.Pattern;

public class ConverterConstants {

    public static final int THIS_SOFTWARE_VERSION_NUMBER = 1;

    public static final int VCF_STANDARD_VERSION = 4;

    public static final long HEADER_DISTANCE_TO_END_POSITION = 24;

    public static final int CONVERTED_FILES_BYTE_ARRAY_LENGTH = 50;

    public static final int LOCATION_VCF_POS_COLUMN = 0;

    public static int REFERENCE_COORDINATE_POSITION = 0;

    public static int GENOTYPE_POSITION = 1;

    public static int CONSENSUS_QUALITY_POSITION = 2;

    public static int SNP_QUALITY_POSITION = 3;

    public static int MAPPING_QUALITY_POSITION = 4;

    public static int READ_DEPTH_POSITION = 5;

    public static int READ_BASES_POSITION = 6;

    public static int READ_QUALITY_POSITION = 7;

    public static String MARKER_INSERTION = ".*\\+\\d[atcgATCG].*";

    public static String MARKER_DELETION = ".*\\-\\d[atcgATCG].*";

    public static Pattern PATTERN_INSERT = Pattern.compile(".*\\+(\\d)[atcgATCG].*");

    public static Pattern PATTERN_DELETE = Pattern.compile(".*\\-(\\d)[atcgATCG].*");

    private ConverterConstants() {
    }

}
