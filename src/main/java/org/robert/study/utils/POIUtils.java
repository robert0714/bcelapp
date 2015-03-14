package org.robert.study.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class POIUtils {
	public static List<String[]> processFromSheet(final Sheet sheet) {        
        List<String[]> result = new ArrayList<String[]>();
        int physicalNumberOfRows = sheet.getPhysicalNumberOfRows();// 得到總行數
        int headerNumberOfCells = 0;
        if (physicalNumberOfRows > 0) {
            headerNumberOfCells = sheet.getRow(0).getPhysicalNumberOfCells();
            for (int i = 0; i < physicalNumberOfRows; ++i) {
                Row row = sheet.getRow(i);
                if(row==null){
                	continue;
                }
                int physicalNumberOfCells = row.getPhysicalNumberOfCells();
//                if (physicalNumberOfCells <= headerNumberOfCells) {
                if (true) {
                    String[] contants = new String[headerNumberOfCells];
                    for (int j = 0; j < headerNumberOfCells; j++) {
                        Cell cell = row.getCell(j);
                        if (cell != null) {
                            int cellStyle = cell.getCellType();
                            switch (cellStyle) {
                            case Cell.CELL_TYPE_BLANK:
                                // System.out.println("Cell.CELL_TYPE_BLANK");
                                contants[j] = "";
                                break;
                            case Cell.CELL_TYPE_BOOLEAN:
                                // System.out.println("Cell.CELL_TYPE_BOOLEAN");
                                boolean booleanCell = cell.getBooleanCellValue();
                                contants[j] = StringUtils.trim(Boolean.toString(booleanCell));
                                break;
                            case Cell.CELL_TYPE_ERROR:
                                // System.out.println("Cell.CELL_TYPE_ERROR");
                                byte error = cell.getErrorCellValue();
                                contants[j] = "";
                                break;
                            case Cell.CELL_TYPE_FORMULA:
                                // System.out.print("Cell.CELL_TYPE_FORMULA");
                                String cellFormula =StringUtils.trim( cell.getCellFormula());
                                // System.out.println("cellFormula: " +
                                // cellFormula);
                                contants[j] = "";
                                break;
                            case Cell.CELL_TYPE_NUMERIC:
                                // System.out.println("Cell.CELL_TYPE_NUMERIC");
                            	double tmp = cell.getNumericCellValue();                               
                                contants[j] =StringUtils.trim(new BigDecimal(tmp).toString());
                                break;
                            case Cell.CELL_TYPE_STRING:
                                // System.out.println("Cell.CELL_TYPE_STRING");
                                contants[j] = StringUtils.trim(cell.getStringCellValue());
                                break;
                            }
                        } else {
                            System.out.print("第" + i + "行第" + j + "列Cell為null值");
                        }
                    }
                    boolean qualified =false;
                    for(String tmp :contants){
                    	if(StringUtils.isNotBlank(tmp)){
                    		qualified=true;
                    	}                    	
                    }
                    if(qualified){
                    	result.add(contants);
                    }
                    
                }
            }
        }
        return result;
    }
    /**
     * 調整字型/畫線
     * 
     * @param sheet
     * @param font
     *            字型大小
     * @param columnStyle
     *            {@see CellStyle.ALIGN_CENTER}
     * @param line
     *            畫線(預設值:黑色)
     * @param bold
     *            粗體字
     * @param color
     *            底色
     */
    public static CellStyle buildCellStyle(Sheet sheet, Integer fontSize, short columnStyle, boolean line,
            boolean bold, Integer color) {
        return buildCellStyle(sheet, fontSize, columnStyle, line, bold, color, CellStyle.VERTICAL_CENTER);
    }

    private static CellStyle buildCellStyle(Sheet sheet, Integer fontSize, short columnStyle, boolean line,
            boolean bold, Integer color, short aligment) {
        HSSFWorkbook wb = (HSSFWorkbook) sheet.getWorkbook();
        HSSFCellStyle cellStyle = (HSSFCellStyle) wb.createCellStyle();
        // HSSFCellStyle cellStyle = new
        // TestHSSFCellStyle(wb).getHSSFCellStyle();
        cellStyle.setAlignment(columnStyle); // 置中對齊
        cellStyle.setVerticalAlignment(aligment); // 水平對齊
        cellStyle.setWrapText(true);

        Font titleFont = wb.createFont();
        if (bold) {
            titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD); // 粗體
        }
        titleFont.setFontName("微軟正黑體");
        if (fontSize != null) {
            short fontHeightInPoints = fontSize.shortValue();
            titleFont.setFontHeightInPoints(fontHeightInPoints);
        }
        cellStyle.setFont(titleFont);
        if (color != null) {
            cellStyle.setFillForegroundColor((short) (int) color);
            cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
        }
        // cellStyle.setDataFormat(sheet.getWorkbook().createDataFormat().getFormat("#,##0"));
        // // 千分位數學符號(ex. 1,000)

        if (line) {
            cellStyle.setBorderBottom(CellStyle.BORDER_THIN);
            cellStyle.setBorderLeft(CellStyle.BORDER_THIN);
            cellStyle.setBorderRight(CellStyle.BORDER_THIN);
            cellStyle.setBorderTop(CellStyle.BORDER_THIN);
        }

        return cellStyle;
    }

    public static HSSFCellStyle buildCellStyle(Sheet sheet, Integer fontSize, short columnStyle, String fontName,
            boolean bold) {
        HSSFWorkbook wb = (HSSFWorkbook) sheet.getWorkbook();
        HSSFCellStyle cellStyle = (HSSFCellStyle) wb.createCellStyle();
        // HSSFCellStyle cellStyle = new
        // TestHSSFCellStyle(wb).getHSSFCellStyle();
        cellStyle.setAlignment(columnStyle); // 置中對齊

        cellStyle.setWrapText(true);

        Font titleFont = wb.createFont();
        if (bold) {
            titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD); // 粗體
        }
        if (fontName != null)
            titleFont.setFontName(fontName);
        else
            titleFont.setFontName("微軟正黑體");
        if (fontSize != null) {
            short fontHeightInPoints = fontSize.shortValue();
            titleFont.setFontHeightInPoints(fontHeightInPoints);
        }
        cellStyle.setFont(titleFont);

        return cellStyle;
    }

    public static void writeWorkbookOut(File output, Workbook document) throws IOException {
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        FileOutputStream out = new FileOutputStream(output);
        try {
            document.write(ostream);
            out.write(ostream.toByteArray());
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                ostream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void writePOIXMLDocumentPartOut(File output, POIXMLDocument document) throws IOException {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(output);
            document.write(out);
        } finally {
            try {
                if (out != null)
                    out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param x_position
     *            第x攔 Columns are 0 based.
     * @param y_position
     *            第x行 Rows are 0 based.
     * @param cellStyle
     *            {@see CellStyle.ALIGN_CENTER}
     * 
     */
    public static void setSheetCellPosValue(Sheet sheet, int x_position, int y_position, Object o, CellStyle cellStyle,
            float rowHeight) {
        // Create a row and put some cells in it. Rows are 0 based.
        Row row = sheet.getRow(y_position);
        if (row == null)
            row = sheet.createRow(y_position); // Create a cell and put a value
                                               // in it.
        row.setHeightInPoints(rowHeight); // row 高度

        Cell cell = row.getCell(x_position);
        if (cell == null)
            cell = row.createCell(x_position);
        if (o instanceof Number) {
            cell.setCellValue(((Number) o).doubleValue());
        } else if (o instanceof RichTextString) {
            cell.setCellValue((RichTextString) o);
        } else if (o instanceof String) {
            cell.setCellValue((String) o);
        } else if (o instanceof Boolean) {
            cell.setCellValue((Boolean) o);
        }
        cell.setCellStyle(cellStyle);
    }

    public static void setSheetCellPosFormulaValue(Sheet sheet, int x_position, int y_position, Object o,
            CellStyle cellStyle, float rowHeight, String formula) {
        // Create a row and put some cells in it. Rows are 0 based.
        Row row = sheet.getRow(y_position);

        if (row == null)
            row = sheet.createRow(y_position); // Create a cell and put a value
                                               // in it.
        row.setHeightInPoints(rowHeight); // row 高度

        Cell cell = row.getCell(x_position);
        if (cell == null)
            cell = row.createCell(x_position);

        cell.setCellFormula(formula);

        if (o instanceof Number) {
            cell.setCellValue(((Number) o).doubleValue());
        } else if (o instanceof RichTextString) {
            cell.setCellValue((RichTextString) o);
        } else if (o instanceof String) {
            cell.setCellValue((String) o);

        } else if (o instanceof Boolean) {
            cell.setCellValue((Boolean) o);
        } else if (o instanceof Calendar) {
            cell.setCellValue((Calendar) o);
        }

        cell.setCellStyle(cellStyle);
    }
}
