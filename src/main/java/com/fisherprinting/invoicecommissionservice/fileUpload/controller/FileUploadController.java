package com.fisherprinting.invoicecommissionservice.fileUpload.controller;

import com.fisherprinting.invoicecommissionservice.fileUpload.service.FileUploadService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@AllArgsConstructor
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/fileUpload/v1")
public class FileUploadController {
    private final FileUploadService fileUploadService;

    @PostMapping("/excelFile/filterPaidInvoices")
    public ResponseEntity<?> excelFile(@RequestParam("empID") Integer empID, @RequestParam("file") MultipartFile file) {
        if(fileUploadService.processPaidInvoiceExcelFile(empID, file))
            return ResponseEntity.ok().body(Map.of("Message", "Upload success."));
        return ResponseEntity.internalServerError().body(Map.of("Message", "Upload attempt failed."));
    }
}
