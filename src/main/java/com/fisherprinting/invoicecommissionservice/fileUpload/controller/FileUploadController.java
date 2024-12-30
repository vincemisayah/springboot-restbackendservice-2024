package com.fisherprinting.invoicecommissionservice.fileUpload.controller;

import com.fisherprinting.invoicecommissionservice.fileUpload.DTOs.DTOs;
import com.fisherprinting.invoicecommissionservice.fileUpload.dao.FileUploadDao;
import com.fisherprinting.invoicecommissionservice.fileUpload.service.FileUploadService;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/fileUpload/v1")
public class FileUploadController {
    private final FileUploadService fileUploadService;
    private final FileUploadDao fileUploadDao;

//    @PostMapping("/excelFile/filterPaidInvoices")
//    public ResponseEntity<?> excelFile(@RequestParam("empID") Integer empID, @RequestParam("file") MultipartFile file) {
//        if(fileUploadService.processPaidInvoiceExcelFile(empID, file))
//            return ResponseEntity.ok().body(Map.of("Message", "Upload success."));
//        return ResponseEntity.internalServerError().body(Map.of("Message", "Upload attempt failed."));
//    }

//    @PostMapping("/excelFile/filterPaidInvoices")
//    public List<DTOs.PaidInvoiceInfo> filterPaidInvoicesFromFile(@RequestParam("empID") Integer empID, @RequestParam("file") MultipartFile file) {
//        return fileUploadService.filterPaidInvoicesFromFile(empID, file);
//    }

    //localhost:1118/invoiceCommissionService/fileUpload/v1/excelFile/filterPaidInvoicesb
    @PostMapping("/excelFile/filterPaidInvoices")
    public ResponseEntity<?> filterPaidInvoicesFromFile(@RequestParam("empIDStr") String empIDStr, @RequestParam("file") MultipartFile file) throws ParseException {
        int empID = Integer.parseInt(empIDStr);
        if(fileUploadService.processPaidInvoiceExcelFile(empID, file)){
            List<DTOs.PaidInvoiceInfo> shortPaid = fileUploadService.removeDuplicates(fileUploadDao.getShortPaidInvoicesListFromBuffer(empID));
            List<DTOs.PaidInvoiceInfo> fullyPaid = fileUploadService.removeDuplicates(fileUploadDao.getFullyPaidInvoicesListFromBuffer(empID));
            List<DTOs.PaidInvoiceInfo> overPaid = fileUploadService.removeDuplicates(fileUploadDao.getOverPaidInvoicesListFromBuffer(empID));

            List<DTOs.ViewableFilteredInvoiceData> viewableFullyPaidInvoices = fileUploadService.viewableFilteredInvoiceData(fullyPaid);
            List<DTOs.ViewableFilteredInvoiceData> viewableOverPaidInvoices = fileUploadService.viewableFilteredInvoiceData(overPaid);
            List<DTOs.ViewableFilteredInvoiceData> viewableShortPaidInvoices = fileUploadService.viewableFilteredInvoiceData(shortPaid);

            List<DTOs.InvoiceDup> invoiceDups = fileUploadDao.invoiceDupListFromBuffer(empID);

            fileUploadDao.deletePaidInvoiceDataFromBuffer(empID);

            return ResponseEntity.ok().body(Map.of(
                    "ViewableFullyPaidInvoices", viewableFullyPaidInvoices,
                    "ViewableOverPaidInvoices", viewableOverPaidInvoices,
                    "ViewableShortPaidInvoices", viewableShortPaidInvoices,
                    "ShortPaidInvoices", shortPaid,
                    "FullyPaidInvoices", fullyPaid,
                    "OverPaidInvoices", overPaid,
                    "InvoiceDupsFound", invoiceDups
            ));
        }
        return ResponseEntity.internalServerError().body(Map.of("Message", "Upload attempt failed."));
    }


//    invoiceID: '',
//    invoiceDate: '',
//    datePaid: '',
//    total: '',
//    amountPaid: '',


    // TODO-------------------------------------
    @PostMapping("/excelFile/parseRow")
    public ResponseEntity<?> filterPaidInvoicesFromFile(@RequestParam("empID") int empID, @RequestBody List<DTOs.InvoiceRowData> invoiceRowData) throws ParseException {
//        int empID = 3667;
        if(fileUploadService.insertToBeFilteredPaidInvoiceData(empID, invoiceRowData)){
            List<DTOs.PaidInvoiceInfo> shortPaid = fileUploadService.removeDuplicates(fileUploadDao.getShortPaidInvoicesListFromBuffer(empID));
            List<DTOs.PaidInvoiceInfo> fullyPaid = fileUploadService.removeDuplicates(fileUploadDao.getFullyPaidInvoicesListFromBuffer(empID));
            List<DTOs.PaidInvoiceInfo> overPaid = fileUploadService.removeDuplicates(fileUploadDao.getOverPaidInvoicesListFromBuffer(empID));

            List<DTOs.ViewableFilteredInvoiceData> viewableFullyPaidInvoices = fileUploadService.viewableFilteredInvoiceData(fullyPaid);
            List<DTOs.ViewableFilteredInvoiceData> viewableOverPaidInvoices = fileUploadService.viewableFilteredInvoiceData(overPaid);
            List<DTOs.ViewableFilteredInvoiceData> viewableShortPaidInvoices = fileUploadService.viewableFilteredInvoiceData(shortPaid);

            List<DTOs.InvoiceDup> invoiceDups = fileUploadDao.invoiceDupListFromBuffer(empID);

            fileUploadDao.deletePaidInvoiceDataFromBuffer(empID);

            return ResponseEntity.ok().body(Map.of(
                    "ViewableFullyPaidInvoices", viewableFullyPaidInvoices,
                    "ViewableOverPaidInvoices", viewableOverPaidInvoices,
                    "ViewableShortPaidInvoices", viewableShortPaidInvoices,
                    "ShortPaidInvoices", shortPaid,
                    "FullyPaidInvoices", fullyPaid,
                    "OverPaidInvoices", overPaid,
                    "InvoiceDupsFound", invoiceDups
            ));
        }

        return ResponseEntity.internalServerError().body(Map.of("Message", "Upload attempt failed."));
    }

    @PostMapping("/excelFile/saveInvoiceData")
    public ResponseEntity<?> saveInvoiceData(@RequestBody List<DTOs.PaidInvoiceInfo> invoiceData){
        List<Integer> unsavedInvoiceIDs = new ArrayList<>();
        int count = 0;
        try {
            for (DTOs.PaidInvoiceInfo paidInvoiceInfo : invoiceData) {
                if(fileUploadDao.invoiceFound(paidInvoiceInfo.invoiceID())){
                    fileUploadDao.saveInvoiceData(paidInvoiceInfo);
                    ++count;
                }else{
                    unsavedInvoiceIDs.add(paidInvoiceInfo.invoiceID());
                }
            }
            return ResponseEntity.ok().body(Map.of(
                    "SavedInvoicesCount", count,
                    "UnsavedInvoices", unsavedInvoiceIDs));
        }catch (DataAccessException e){
            return ResponseEntity.internalServerError().body(Map.of("SavedInvoicesCount", count));
        }
    }
}
