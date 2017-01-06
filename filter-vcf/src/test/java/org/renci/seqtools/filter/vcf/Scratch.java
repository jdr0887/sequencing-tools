package org.renci.seqtools.filter.vcf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

public class Scratch {

    @Test
    public void scratch() {

        Pattern gatkPattern = Pattern.compile("(.+):(\\d+)-(\\d+)");
        Map<String, List<Range<Integer>>> map = new HashMap<String, List<Range<Integer>>>();

        File intervalList = new File("/tmp", "exons_pm_0_v39.interval_list");
        // File intervalList = new File("/tmp", "exons_pm_0_v39.interval_list.gatk");
        try (FileReader fr = new FileReader(intervalList); BufferedReader br = new BufferedReader(fr)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (StringUtils.isEmpty(line.trim()) || line.startsWith("#") || line.startsWith("@")) {
                    continue;
                }
                Matcher gatkMatcher = gatkPattern.matcher(line);
                if (gatkMatcher.matches()) {
                    String[] lineArray = line.split(":");
                    String chromosome = lineArray[0];
                    map.put(chromosome, new ArrayList<Range<Integer>>());
                } else {
                    String[] lineArray = line.split("\t");
                    String chromosome = lineArray[0];
                    map.put(chromosome, new ArrayList<Range<Integer>>());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
