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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.text.NumberFormat;

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
                        case 3 -> invoiceTotal = BigDecimal.valueOf(cell.getNumericCellValue());
                        case 4 -> amountPaid = BigDecimal.valueOf(cell.getNumericCellValue());
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
        return Integer.parseInt(invoiceID);
    }

//    public List<DTOs.PaidInvoiceInfo> filterPaidInvoicesFromFile(Integer empID, MultipartFile file){
//        List<DTOs.PaidInvoiceInfo> list = new ArrayList<>();
//        if(processPaidInvoiceExcelFile(empID, file))
//            list = fileUploadDao.getFullyPaidInvoicesListFromBuffer(empID);
//        fileUploadDao.deletePaidInvoiceDataFromBuffer(empID);
//        return list;
//    }
    public List<DTOs.ViewableFilteredInvoiceData> viewableFilteredInvoiceData(List<DTOs.PaidInvoiceInfo> list) throws ParseException {
        List<DTOs.ViewableFilteredInvoiceData> toReturnList = new ArrayList<>();

        TreeSet<Integer> setInvoiceID = new TreeSet<>();
        for(DTOs.PaidInvoiceInfo paidInvoiceInfo: list){
            setInvoiceID.add(paidInvoiceInfo.invoiceID());
        }



        List<DTOs.PaidInvoiceInfo> removedDups = new ArrayList<>();
        for(Integer invoiceID: setInvoiceID){
            list.stream().filter(n->n.invoiceID() == invoiceID).findFirst().ifPresent(n->removedDups.add(n));
        }




        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US);
        SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy");

        int counter = 0;
        for(DTOs.PaidInvoiceInfo paidInvoiceInfo: removedDups){
            int rowNumber = (++counter);
            int invoiceID = paidInvoiceInfo.invoiceID();
            String invoiceDate = dateFormatter.format(paidInvoiceInfo.invoiceDate());
            String datePaid = dateFormatter.format(paidInvoiceInfo.datePaid());
            String invoiceTotal = currencyFormatter.format(paidInvoiceInfo.invoiceTotal());
            String amountPaid = currencyFormatter.format(paidInvoiceInfo.amountPaid());

            DTOs.ViewableFilteredInvoiceData data = new DTOs.ViewableFilteredInvoiceData(rowNumber, invoiceID, invoiceDate, datePaid, invoiceTotal, amountPaid);
            toReturnList.add(data);
        }
        return toReturnList;
    }

}
