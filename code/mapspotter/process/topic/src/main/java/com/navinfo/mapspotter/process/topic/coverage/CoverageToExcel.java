package com.navinfo.mapspotter.process.topic.coverage;

import java.io.*;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.poi.xssf.usermodel.*;

/**
 * Created by huanghai on 2016/3/7.
 */
public class CoverageToExcel {
//    static String dirName = "20160101-20160131_1_0.75";
//    String outpath = "E:\\Road_Fusion_Data\\coverage\\result\\" + dirName;
//    String templateXls = "E:\\Road_Fusion_Data\\coverage\\TOP60城市列表20141104.xlsx";


    public void azkabanRun(String coverageMileagePath, String templateXlsPath, String outpath) throws IOException {
        CoverageToExcel toExcel = new CoverageToExcel();
        toExcel.exportExcel(coverageMileagePath, templateXlsPath, outpath);
    }

    public static void main(String[] args) throws SQLException, IOException {
        String outputPath = "/coverage/mileage/" + "20160101-20160131_1_0.75";
        String templateXls = "E:\\Road_Fusion_Data\\coverage\\TOP60城市列表20141104.xlsx";
        String outpath = "";
        CoverageToExcel toExcel = new CoverageToExcel();
        toExcel.exportExcel(outputPath, templateXls, outpath);
    }

    public void exportExcel(String coverageMileagePath, String templateXlsPath, String outpath) throws IOException {
        DecimalFormat df = new DecimalFormat("###.##");
        Map<String, Double> allLMap = new HashMap<>();
        Map<String, Double> covLMap = new HashMap<>();

        Map<String, Double> allFcLMap = new HashMap<>();
        Map<String, Double> covFcLMap = new HashMap<>();

        Map<String, Double> allNMap = new HashMap<>();
        Map<String, Double> covNMap = new HashMap<>();

        Map<String, Double> allFcNMap = new HashMap<>();
        Map<String, Double> covFcNMap = new HashMap<>();

        Configuration conf = new Configuration();
        conf.addDefaultResource("core-site.xml");
        conf.addDefaultResource("hdfs-site.xml");


        BufferedReader br = null;
        try {
            FileSystem fileSystem = FileSystem.get(conf);
            Path filePath = new Path(coverageMileagePath + File.separator + "part-r-00000");
            if (!fileSystem.exists(filePath)) {
                throw new RuntimeException("file no exists.");
            }
            br = new BufferedReader(new InputStreamReader(fileSystem.open(filePath)));
            String tmp;
            while ((tmp = br.readLine()) != null) {
                String[] keyVals = tmp.split("\t");
                String[] split = keyVals[0].split("\\|");
                if (split.length == 3) {
                    if ("COV".equals(split[0])) {
                        covFcLMap.put(split[1] + "-" + split[2], Double.parseDouble(keyVals[1]));
                    } else if ("ALL".equals(split[0])) {
                        allFcLMap.put(split[1] + "-" + split[2], Double.parseDouble(keyVals[1]));
                    } else if ("COVFCN".equals(split[0])) {
                        covFcNMap.put(split[1] + "-" + split[2], Double.parseDouble(keyVals[1]));
                    } else if ("ALLFCN".equals(split[0])) {
                        allFcNMap.put(split[1] + "-" + split[2], Double.parseDouble(keyVals[1]));
                    } else {
                        throw new RuntimeException("error fc data.");
                    }
                } else if (split.length == 2) {
                    if ("COV".equals(split[0])) {
                        covLMap.put(split[1], Double.parseDouble(keyVals[1]));
                    } else if ("ALL".equals(split[0])) {
                        allLMap.put(split[1], Double.parseDouble(keyVals[1]));
                    } else if ("ALLN".equals(split[0])) {
                        allNMap.put(split[1], Double.parseDouble(keyVals[1]));
                    } else if ("COVN".equals(split[0])) {
                        covNMap.put(split[1], Double.parseDouble(keyVals[1]));
                    } else {
                        throw new RuntimeException("error data.");
                    }
                }
            }
        } catch (IOException e) {
            throw e;
        } finally {
            if (br != null) {
                br.close();
            }
        }

        // 构建 XSSFWorkbook 对象，strPath 传入文件路径
        XSSFWorkbook xwb = new XSSFWorkbook(templateXlsPath);
        XSSFCellStyle cellStyle = xwb.createCellStyle();
        cellStyle.setWrapText(true);


        XSSFSheet sheet1 = xwb.getSheet("Sheet1");
        sheet1.setColumnWidth(4, 1000 * 4);
        XSSFRow row = sheet1.getRow(0);
        XSSFCell cell = row.createCell(3);
        cell.setCellValue("总里程覆盖率");
        cell = row.createCell(4);
        cell.setCellValue("里程覆盖率");
        cell = row.createCell(5);
        cell.setCellValue("总link数覆盖率");
        cell = row.createCell(6);
        cell.setCellValue("link数覆盖率");
        int lastRowNum = sheet1.getLastRowNum();

        for (int i = 1; i <= lastRowNum; i++) {
            row = sheet1.getRow(i);
            String provnm = row.getCell(1).getStringCellValue();
            String citynm = row.getCell(2).getStringCellValue();

            double allLength = allLMap.get(provnm + "-" + citynm);
            Double covLength = covLMap.get(provnm + "-" + citynm);
            // 里程覆盖率
            double lengthCR = covLength == null ? 0 : covLength
                    / allLength;
            cell = row.createCell(3);
            cell.setCellValue(df.format(lengthCR * 100) + "%");

            // 功能等级

            double fc_all_1 = allFcLMap.get(provnm + "-" + citynm + "-1");
            Double fc_cov_1 = covFcLMap.get(provnm + "-" + citynm + "-1");
            double fc1CR = fc_cov_1 == null ? 0 : fc_cov_1 / fc_all_1;

            double fc_all_2 = allFcLMap.get(provnm + "-" + citynm + "-2");
            Double fc_cov_2 = covFcLMap.get(provnm + "-" + citynm + "-2");
            double fc2CR = fc_cov_2 == null ? 0 : fc_cov_2 / fc_all_2;

            double fc_all_3 = allFcLMap.get(provnm + "-" + citynm + "-3");
            Double fc_cov_3 = covFcLMap.get(provnm + "-" + citynm + "-3");
            double fc3CR = fc_cov_3 == null ? 0 : fc_cov_3 / fc_all_3;

            double fc_all_4 = allFcLMap.get(provnm + "-" + citynm + "-4");
            Double fc_cov_4 = covFcLMap.get(provnm + "-" + citynm + "-4");
            double fc4CR = fc_cov_4 == null ? 0 : fc_cov_4 / fc_all_4;

            double fc_all_5 = allFcLMap.get(provnm + "-" + citynm + "-5");
            Double fc_cov_5 = covFcLMap.get(provnm + "-" + citynm + "-5");
            double fc5CR = fc_cov_5 == null ? 0 : fc_cov_5 / fc_all_5;


            cell = row.createCell(4);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(new XSSFRichTextString("1：" + df.format(fc1CR * 100) + "%" + "\r\n" + "2：" + df.format(fc2CR * 100) + "%" + "\r\n" + "3：" + df.format(fc3CR * 100) + "%" + "\r\n" + "4：" + df.format(fc4CR * 100) + "%" + "\r\n" + "5：" + df.format(fc5CR * 100) + "%" + "\r\n"));

            // link数覆盖率
            double allNum = allNMap.get(provnm + "-" + citynm);
            Double covNum = covNMap.get(provnm + "-" + citynm);
            double numCR = covNum == null ? 0 : covNum
                    / allNum;
            cell = row.createCell(5);
            cell.setCellValue(df.format(numCR * 100) + "%");

            // link覆盖率按功能等级

            double fcn_all_1 = allFcNMap.get(provnm + "-" + citynm + "-1");
            Double fcn_cov_1 = covFcNMap.get(provnm + "-" + citynm + "-1");
            double fcn1CR = fcn_cov_1 == null ? 0 : fcn_cov_1 / fcn_all_1;

            double fcn_all_2 = allFcNMap.get(provnm + "-" + citynm + "-2");
            Double fcn_cov_2 = covFcNMap.get(provnm + "-" + citynm + "-2");
            double fcn2CR = fcn_cov_2 == null ? 0 : fcn_cov_2 / fcn_all_2;

            double fcn_all_3 = allFcNMap.get(provnm + "-" + citynm + "-3");
            Double fcn_cov_3 = covFcNMap.get(provnm + "-" + citynm + "-3");
            double fcn3CR = fcn_cov_3 == null ? 0 : fcn_cov_3 / fcn_all_3;

            double fcn_all_4 = allFcNMap.get(provnm + "-" + citynm + "-4");
            Double fcn_cov_4 = covFcNMap.get(provnm + "-" + citynm + "-4");
            double fcn4CR = fcn_cov_4 == null ? 0 : fcn_cov_4 / fcn_all_4;

            double fcn_all_5 = allFcNMap.get(provnm + "-" + citynm + "-5");
            Double fcn_cov_5 = covFcNMap.get(provnm + "-" + citynm + "-5");
            double fcn5CR = fcn_cov_5 == null ? 0 : fcn_cov_5 / fcn_all_5;


            cell = row.createCell(6);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(new XSSFRichTextString("1：" + df.format(fcn1CR * 100) + "%" + "\r\n" + "2：" + df.format(fcn2CR * 100) + "%" + "\r\n" + "3：" + df.format(fcn3CR * 100) + "%" + "\r\n" + "4：" + df.format(fcn4CR * 100) + "%" + "\r\n" + "5：" + df.format(fcn5CR * 100) + "%" + "\r\n"));
        }

        // 总link覆盖率
        File outPath = new File(outpath);
        outPath.mkdirs();
        FileOutputStream fos = new FileOutputStream(new File(outpath + File.separator
                + "statistic_coverage.xlsx"));
        xwb.write(fos);
    }
}
