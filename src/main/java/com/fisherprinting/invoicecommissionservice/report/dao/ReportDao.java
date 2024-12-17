package com.fisherprinting.invoicecommissionservice.report.dao;

import com.fisherprinting.invoicecommissionservice.fileUpload.DTOs.DTOs;
import com.fisherprinting.invoicecommissionservice.report.dtos.DataTransferObjectsContainer;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class ReportDao {
    private final NamedParameterJdbcTemplate template;

    public ReportDao(NamedParameterJdbcTemplate template) {
        this.template = template;
    }

    public String getEmployeeName(int empID) throws DataAccessException {
        String sql = """
                    SELECT firstName + ' ' + lastName
                    FROM employees
                    WHERE id = :empID
                    """;

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("empID", empID);

        try{
            return template.queryForObject(sql, parameters, String.class);
        }catch(DataAccessException e){
            throw e;
        }
    }

    public BigDecimal getInvoiceTotal(int invoiceID) throws DataAccessException {
        String sql = """
                    DECLARE @invoiceId as int = :invoiceID
                      \s
                    SELECT
                        SUM(
                            CASE
                                WHEN t1.unitName = '%'
                                    THEN CONVERT(decimal(18,2),([quantity] * cost)/100, 2)
                                WHEN t1.chargePerM > 0 OR t1.unitName like '%PM%'
                                    THEN CONVERT(decimal(18,2),([quantity] * cost)/1000, 2)
                                    ELSE CONVERT(decimal(18,2),([quantity] * cost), 2)
                            END
                        ) as invoiceTotal
                    FROM [intrafisher].[dbo].[invoiceItems]
                        INNER JOIN [intrafisher].[dbo].[invTasks] as t1 on [task] = t1.id
                        INNER JOIN [intrafisher].[dbo].[invDepts] as t2 on t1.dept = t2.id
                                       \s
                    WHERE [invoice] = @invoiceId
                    """;

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("invoiceID", invoiceID);

        try{
            return template.queryForObject(sql, parameters, BigDecimal.class);
        }catch(DataAccessException e){
            throw e;
        }
    }


    public DataTransferObjectsContainer.InvoiceInfo getInvoiceInfo(int invoiceID) throws DataAccessException {
        List<DataTransferObjectsContainer.InvoiceInfo> list = new ArrayList<>();
        String sql = """
                    DECLARE @INVOICE_ID INT = :invoiceID
                    
                    SELECT customers.ARnumber as customerAR,
                           customers.id as customerId,
                           customers.name as customerName,
                           CONVERT(date, [date]) as invoiceDate,
                           CONVERT(date, [paymentDueDate]) as paymentDueDate
                    FROM [intrafisher].[dbo].[invoices]
                        INNER JOIN customers on invoices.customer = customers.id
                    WHERE [invoices].[id] = @INVOICE_ID
                    """;

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("invoiceID", invoiceID);

        try{
            List<Map<String, Object>> rows =  template.queryForList(sql, parameters);
            for(Map<String, Object> row : rows){
                String customerAR = (String)row.get("customerAR");
                int customerId = (int)row.get("customerId");
                String customerName = (String)row.get("customerName");
                Date invoiceDate = (Date)row.get("invoiceDate");
                Date paymentDueDate = (Date)row.get("paymentDueDate");

                DataTransferObjectsContainer.InvoiceInfo invoiceInfo = new DataTransferObjectsContainer.InvoiceInfo(customerAR, customerId, customerName, invoiceDate, paymentDueDate);
                list.add(invoiceInfo);
            }

        }catch(DataAccessException e){
            throw e;
        }

        return list.getFirst();
    }

    public List<DTOs.PaidInvoiceInfo> getPaidInvoicesFromRecords(LocalDate startDate, LocalDate endDate) throws DataAccessException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedStartDate = startDate.format(formatter);
        String formattedEndDate = endDate.format(formatter);

        List<DTOs.PaidInvoiceInfo> list = new ArrayList<>();
        String sql = """
                    DECLARE @START_DATE DATE = :formattedStartDate
                    DECLARE @END_DATE DATE = :formattedEndDate
                    
                    SELECT [id]
                          ,[invoiceID]
                          ,[invoiceDate]
                          ,[datePaid]
                          ,[invoiceTotal]
                          ,[amountPaid]
                          ,[uploadDatetime]
                          ,[uploadedBy]
                      FROM [intrafisher].[dbo].[InvComm_RecordsFullyPaidInvoices]
                      WHERE @START_DATE <= [invoiceDate] AND [invoiceDate] <= @END_DATE
                      ORDER BY [invoiceDate]
                    """;

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("formattedStartDate", formattedStartDate);
        parameters.addValue("formattedEndDate", formattedEndDate);

        List<Map<String, Object>> rows = template.queryForList(sql, parameters);
        for (Map<String, Object> row : rows) {
            int uploadedBy = (int) row.get("uploadedBy");
            Timestamp uploadDatetime = (Timestamp) row.get("uploadDatetime");
            int invoiceID = (int) row.get("invoiceID");
            Date invoiceDate = (Date) row.get("invoiceDate");
            Date datePaid = (Date) row.get("datePaid");
            BigDecimal invoiceTotal = (BigDecimal) row.get("invoiceTotal");
            BigDecimal amountPaid = (BigDecimal) row.get("amountPaid");

            DTOs.PaidInvoiceInfo data = new DTOs.PaidInvoiceInfo(uploadedBy, uploadDatetime, invoiceID, invoiceDate, datePaid, invoiceTotal, amountPaid);
            list.add(data);
        }
        return list;
    }

    public List<DTOs.PaidInvoiceInfo> getPaidInvoicesFromRecordsByEmpID(Integer empID,LocalDate startDate, LocalDate endDate) throws DataAccessException {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedStartDate = startDate.format(formatter);
        String formattedEndDate = endDate.format(formatter);

        List<DTOs.PaidInvoiceInfo> list = new ArrayList<>();
        String sql = """
                    DECLARE @SALES_EMP_ID INT = :empID
                    DECLARE @START_DATE DATE = :formattedStartDate
                    DECLARE @END_DATE DATE = :formattedEndDate
                    
                    SELECT [invoiceID]
                          ,[invoiceDate]
                          ,[datePaid]
                          ,[invoiceTotal]
                          ,[amountPaid]
                          ,[uploadDatetime]
                          ,[uploadedBy]
                    FROM [intrafisher].[dbo].[InvComm_RecordsFullyPaidInvoices] as T1
                        INNER JOIN invoices T2 on T1.invoiceID = T2.id
                        INNER JOIN jobs T3 on T2.job = T3.id
                        INNER JOIN customers T4 on T4.id = T3.customerID
                    WHERE @START_DATE <= [invoiceDate] AND [invoiceDate] <= @END_DATE
                        AND  ([salesman] = @SALES_EMP_ID
                           OR [salesperson2] = @SALES_EMP_ID
                           OR [salesperson3] = @SALES_EMP_ID
                           OR [salesperson4] = @SALES_EMP_ID)
                    """;

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("empID", empID);
        parameters.addValue("formattedStartDate", formattedStartDate);
        parameters.addValue("formattedEndDate", formattedEndDate);

        List<Map<String, Object>> rows = template.queryForList(sql, parameters);
        for (Map<String, Object> row : rows) {
            int uploadedBy = (int) row.get("uploadedBy");
            Timestamp uploadDatetime = (Timestamp) row.get("uploadDatetime");
            int invoiceID = (int) row.get("invoiceID");
            Date invoiceDate = (Date) row.get("invoiceDate");
            Date datePaid = (Date) row.get("datePaid");
            BigDecimal invoiceTotal = (BigDecimal) row.get("invoiceTotal");
            BigDecimal amountPaid = (BigDecimal) row.get("amountPaid");

            DTOs.PaidInvoiceInfo data = new DTOs.PaidInvoiceInfo(uploadedBy, uploadDatetime, invoiceID, invoiceDate, datePaid, invoiceTotal, amountPaid);
            list.add(data);
        }
        return list;
    }

    public String getEmployeeNameByID(int empID) throws DataAccessException {
        List<DataTransferObjectsContainer.InvoiceInfo> list = new ArrayList<>();
        String sql = """
                    SELECT firstName + ' ' + lastName
                    FROM [intrafisher].[dbo].[employees]
                    WHERE [employees].[id] = :empID
                    """;

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("empID", empID);

        try{
            return template.queryForObject(sql, parameters, String.class);
        }catch(DataAccessException e){
            throw e;
        }
    }
}
