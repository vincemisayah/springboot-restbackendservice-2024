package com.fisherprinting.invoicecommissionservice.fileUpload.service;

import com.fisherprinting.invoicecommissionservice.fileUpload.DTOs.DTOs;
import com.fisherprinting.invoicecommissionservice.fileUpload.dao.FileUploadDao;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.DateUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Objects;

@AllArgsConstructor
@Service
public class FileUploadService {
    private final FileUploadDao fileUploadDao;

    public static boolean isValidExcelFile(MultipartFile file) {
        return Objects.equals(file.getContentType(), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    public boolean processPaidInvoiceExcelFile(int uploadedBy, MultipartFile file) {
        boolean processAttemptSuccess = true;

        // Confirm that file type is valid format.
        if(!isValidExcelFile(file))
            return false;

        try{
            InputStream inputStream = file.getInputStream();
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = workbook.getSheetAt(0);

            int rowIndex = 0;
            for(Row row: sheet){
                if(rowIndex == 0){
                    ++rowIndex;
                    continue;
                }

                Iterator<Cell> cellIterator = row.iterator();
                int cellIndex = 0;

                Integer invoiceID = null;
                Date invoiceDate = null;
                Date datePaid = null;
                BigDecimal invoiceTotal = null;
                BigDecimal amountPaid = null;
                while (cellIterator.hasNext()){
                    Cell cell = cellIterator.next();
                    switch (cellIndex){
                        case 0 -> invoiceID = (parseInvoiceId(cell.getStringCellValue()) != null)? parseInvoiceId(cell.getStringCellValue()): null;
                        case 1 -> invoiceDate = convertToSqlDate(cell.getNumericCellValue());
                        case 2 -> datePaid = convertToSqlDate(cell.getNumericCellValue());
                        case 3 -> invoiceTotal = new BigDecimal(cell.getNumericCellValue());
                        case 4 -> amountPaid = new BigDecimal(cell.getNumericCellValue());
                        default -> { }
                    }
                    if(invoiceID == null)
                        break;
                    cellIndex++;
                }

                if(invoiceID != null){
                    DTOs.PaidInvoiceInfo paidInvoiceInfo = new DTOs.PaidInvoiceInfo(uploadedBy, new Timestamp(System.currentTimeMillis()), invoiceID, invoiceDate, datePaid, invoiceTotal, amountPaid);
                    this.fileUploadDao.insertPaidInvoiceData(paidInvoiceInfo);
                }
            }
        }catch (Exception e){
            processAttemptSuccess = false;
        }
        return processAttemptSuccess;
    }

    public static java.util.Date convertExcelDateToDouble(double excelDate) {
        return DateUtil.getJavaDate(excelDate);
    }

    public static Date convertToSqlDate(Double dateDouble){
        java.util.Date utilDate = convertExcelDateToDouble(dateDouble);
        return new Date(utilDate.getTime());
    }

    public static Integer parseInvoiceId(String columnInput){
        String[] parsedDate = columnInput.split("-");
        if(parsedDate.length < 2){
            return null;
        }

        String invoiceID = parsedDate[0];
        String IN = parsedDate[1];
        return Integer.parseInt(invoiceID);
    }
}
