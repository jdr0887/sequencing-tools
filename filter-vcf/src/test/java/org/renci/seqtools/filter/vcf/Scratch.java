package org.renci.seqtools.filter.vcf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

public class Scratch {

    @Test
    public void scratch() {

        Pattern gatkPattern = Pattern.compile("(.+):(\\d+)-?(\\d+)?");
        File intervalList = new File("/tmp", "exons_pm_0_v39.interval_list");
        // File intervalList = new File("/tmp", "ic_snp_v2.list");
        // File intervalList = new File("/tmp", "exons_pm_0_v39.interval_list.gatk");
        try (FileReader fr = new FileReader(intervalList); BufferedReader br = new BufferedReader(fr)) {
            String line;
            String chromosome = null;
            Integer start, end;
            while ((line = br.readLine()) != null) {
                if (StringUtils.isEmpty(line.trim()) || line.startsWith("#") || line.startsWith("@")) {
                    continue;
                }
                Matcher gatkMatcher = gatkPattern.matcher(line);
                if (gatkMatcher.matches()) {
                    String[] lineArray = line.split(":");
                    chromosome = lineArray[0];
                    String position = lineArray[1];
                    if (position.contains("-")) {
                        String[] positionSplit = position.split("-");
                        start = Integer.valueOf(positionSplit[0]);
                        end = Integer.valueOf(positionSplit[1]);
                    } else {
                        start = Integer.valueOf(position);
                        end = start;
                    }
                } else {
                    String[] lineArray = line.split("\t");
                    chromosome = lineArray[0];
                    start = Integer.valueOf(lineArray[1]);
                    end = Integer.valueOf(lineArray[2]);
                }
                Range<Integer> range = Range.between(start, end);
                System.out.println(chromosome);
                System.out.println(range.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
