package com.fisherprinting.invoicecommissionservice.fileUpload.controller;

import com.fisherprinting.invoicecommissionservice.fileUpload.DTOs.DTOs;
import com.fisherprinting.invoicecommissionservice.fileUpload.dao.FileUploadDao;
import com.fisherprinting.invoicecommissionservice.fileUpload.service.FileUploadService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    //localhost:1118/invoiceCommissionService/fileUpload/v1/excelFile/filterPaidInvoices
//    @PostMapping("/excelFile/filterPaidInvoices")
//    public ResponseEntity<?> filterPaidInvoicesFromFile(@RequestParam("empID") Integer empID, @RequestParam("file") MultipartFile file) {
//        if(fileUploadService.processPaidInvoiceExcelFile(empID, file)){
//            List<DTOs.PaidInvoiceInfo> shortPaid = fileUploadDao.getShortPaidInvoicesListFromBuffer(empID);
//            List<DTOs.PaidInvoiceInfo> fullyPaid = fileUploadDao.getFullyPaidInvoicesListFromBuffer(empID);
//            List<DTOs.PaidInvoiceInfo> overPaid = fileUploadDao.getOverPaidInvoicesListFromBuffer(empID);
//
//            fileUploadDao.deletePaidInvoiceDataFromBuffer(empID);
//
//            return ResponseEntity.ok().body(Map.of(
//                    "ShortPaidInvoices", shortPaid,
//                    "FullyPaidInvoices", fullyPaid,
//                    "OverPaidInvoices", overPaid
//            ));
//        }
//        return ResponseEntity.internalServerError().body(Map.of("Message", "Upload attempt failed."));
//    }

//    public record ToFilter(int empID, MultipartFile file){ }

    public static class ToFilter{
        int empID;
        MultipartFile file;
        public ToFilter( ){
            super();
            this.empID = 0;
            this.file = null;
        }
        public ToFilter(int empID, MultipartFile file){ }
    }
//    @PostMapping("/excelFile/filterPaidInvoices")
//    public ResponseEntity<?> filterPaidInvoicesFromFile(@RequestBody ToFilter toFilter) {
//        if(fileUploadService.processPaidInvoiceExcelFile(toFilter.empID, toFilter.file)){
//            List<DTOs.PaidInvoiceInfo> shortPaid = fileUploadDao.getShortPaidInvoicesListFromBuffer(toFilter.empID);
//            List<DTOs.PaidInvoiceInfo> fullyPaid = fileUploadDao.getFullyPaidInvoicesListFromBuffer(toFilter.empID);
//            List<DTOs.PaidInvoiceInfo> overPaid = fileUploadDao.getOverPaidInvoicesListFromBuffer(toFilter.empID);
//
//            fileUploadDao.deletePaidInvoiceDataFromBuffer(toFilter.empID);
//
//            return ResponseEntity.ok().body(Map.of(
//                    "ShortPaidInvoices", shortPaid,
//                    "FullyPaidInvoices", fullyPaid,
//                    "OverPaidInvoices", overPaid
//            ));
//        }
//        return ResponseEntity.internalServerError().body(Map.of("Message", "Upload attempt failed."));
//    }

    @PostMapping("/excelFile/filterPaidInvoices")
    public ResponseEntity<?> filterPaidInvoicesFromFile(@RequestParam("empIDStr") String empIDStr, @RequestParam("file") MultipartFile file) {
        int empID = Integer.parseInt(empIDStr);
        if(fileUploadService.processPaidInvoiceExcelFile(empID, file)){
            List<DTOs.PaidInvoiceInfo> shortPaid = fileUploadDao.getShortPaidInvoicesListFromBuffer(empID);
            List<DTOs.PaidInvoiceInfo> fullyPaid = fileUploadDao.getFullyPaidInvoicesListFromBuffer(empID);
            List<DTOs.PaidInvoiceInfo> overPaid = fileUploadDao.getOverPaidInvoicesListFromBuffer(empID);

            fileUploadDao.deletePaidInvoiceDataFromBuffer(empID);

            return ResponseEntity.ok().body(Map.of(
                    "ShortPaidInvoices", shortPaid,
                    "FullyPaidInvoices", fullyPaid,
                    "OverPaidInvoices", overPaid
            ));
        }
        return ResponseEntity.internalServerError().body(Map.of("Message", "Upload attempt failed."));
    }
}
