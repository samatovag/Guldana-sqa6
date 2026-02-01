package aitu.sqa;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ExcelUtils {

    public static Object[][] readSheetFromResources(String resourcePath, String sheetName) {
        List<Object[]> rows = new ArrayList<>();

        try (InputStream is = ExcelUtils.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new RuntimeException("Excel not found in resources: " + resourcePath);
            }

            Workbook workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                StringBuilder names = new StringBuilder();
                for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                    names.append("[").append(workbook.getSheetName(i)).append("] ");
                }
                throw new RuntimeException("Sheet not found: " + sheetName + ". Available sheets: " + names);
            }


            int lastRow = sheet.getLastRowNum(); // row 0 is header
            for (int i = 1; i <= lastRow; i++) {
                Row r = sheet.getRow(i);
                if (r == null) continue;

                String testName = getCellAsString(r.getCell(0));
                String fullName = getCellAsString(r.getCell(1));
                String email = getCellAsString(r.getCell(2));
                String currentAddress = getCellAsString(r.getCell(3));
                String permanentAddress = getCellAsString(r.getCell(4));
                String expected = getCellAsString(r.getCell(5));

                rows.add(new Object[]{testName, fullName, email, currentAddress, permanentAddress, expected});
            }

            workbook.close();

        } catch (Exception e) {
            throw new RuntimeException("Failed to read Excel from resources: " + e.getMessage(), e);
        }

        return rows.toArray(new Object[0][0]);
    }

    private static String getCellAsString(Cell cell) {
        if (cell == null) return "";
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }
}
