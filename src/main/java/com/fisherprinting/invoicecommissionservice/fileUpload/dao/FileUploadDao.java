package com.fisherprinting.invoicecommissionservice.fileUpload.dao;

import com.fisherprinting.invoicecommissionservice.customerLevel.dao.CustomerLevelDao;
import com.fisherprinting.invoicecommissionservice.fileUpload.DTOs.DTOs;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Repository
public class FileUploadDao {
    private final NamedParameterJdbcTemplate template;

    public int insertPaidInvoiceData(DTOs.PaidInvoiceInfo paidInvoiceInfo) {
        String sql = """
                    INSERT INTO [intrafisher].[dbo].[InvComm_toFilterPaidInvoicesBuffer]
                          ([uploadedBy]
                          ,[uploadDatetime]
                          ,[invoiceID]
                          ,[invoiceDate]
                          ,[datePaid]
                          ,[invoiceTotal]
                          ,[amountPaid])
                    VALUES(:uploadedBy, :uploadDatetime, :invoiceID,:invoiceDate, :datePaid, :invoiceTotal, :amountPaid)
                    """;

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("uploadedBy", paidInvoiceInfo.uploadedBy());
        parameters.addValue("uploadDatetime", paidInvoiceInfo.uploadDatetime());
        parameters.addValue("invoiceID", paidInvoiceInfo.invoiceID());
        parameters.addValue("invoiceDate", paidInvoiceInfo.invoiceDate());
        parameters.addValue("datePaid", paidInvoiceInfo.datePaid());
        parameters.addValue("invoiceTotal", paidInvoiceInfo.invoiceTotal());
        parameters.addValue("amountPaid", paidInvoiceInfo.amountPaid());
        return template.update(sql, parameters);
    }

    public List<DTOs.PaidInvoiceInfo> getShortPaidInvoicesListFromBuffer(int uploaderEmpID) {
        List<DTOs.PaidInvoiceInfo> list = new ArrayList<>();
        String sql = """
                    SELECT [uploadedBy]
                          ,[uploadDatetime]
                          ,[invoiceID]
                          ,[invoiceDate]
                          ,[datePaid]
                          ,[invoiceTotal]
                          ,[amountPaid]
                      FROM [intrafisher].[dbo].[InvComm_toFilterPaidInvoicesBuffer]
                      WHERE ABS([invoiceTotal]) > ABS([amountPaid])
                        AND [uploadedBy] = :uploaderEmpID
                      ORDER BY invoiceDate
                    """;

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("uploaderEmpID", uploaderEmpID);

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

    public List<DTOs.PaidInvoiceInfo> getFullyPaidInvoicesListFromBuffer(int uploaderEmpID) {
        List<DTOs.PaidInvoiceInfo> list = new ArrayList<>();
        String sql = """
                    SELECT [uploadedBy]
                          ,[uploadDatetime]
                          ,[invoiceID]
                          ,[invoiceDate]
                          ,[datePaid]
                          ,[invoiceTotal]
                          ,[amountPaid]
                      FROM [intrafisher].[dbo].[InvComm_toFilterPaidInvoicesBuffer]
                      WHERE ABS([invoiceTotal]) = ABS([amountPaid])
                        AND [uploadedBy] = :uploaderEmpID
                      ORDER BY invoiceDate
                    """;

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("uploaderEmpID", uploaderEmpID);

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

    public List<DTOs.PaidInvoiceInfo> getOverPaidInvoicesListFromBuffer(int uploaderEmpID) {
        List<DTOs.PaidInvoiceInfo> list = new ArrayList<>();
        String sql = """
                    SELECT [uploadedBy]
                          ,[uploadDatetime]
                          ,[invoiceID]
                          ,[invoiceDate]
                          ,[datePaid]
                          ,[invoiceTotal]
                          ,[amountPaid]
                      FROM [intrafisher].[dbo].[InvComm_toFilterPaidInvoicesBuffer]
                      WHERE ABS([invoiceTotal]) < ABS([amountPaid])
                        AND [uploadedBy] = :uploaderEmpID
                      ORDER BY invoiceDate
                    """;

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("uploaderEmpID", uploaderEmpID);

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

    public int deletePaidInvoiceDataFromBuffer(int empID) {
        String sql = """
                    DELETE FROM [intrafisher].[dbo].[InvComm_toFilterPaidInvoicesBuffer]
                    WHERE uploadedBy = :empID
                    """;

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("empID", empID);
        return template.update(sql, parameters);
    }

    public List<DTOs.InvoiceDup> invoiceDupListFromBuffer(int uploaderEmpID) {
        List<DTOs.InvoiceDup> list = new ArrayList<>();
        String sql = """
                    SELECT [invoiceID], invoiceCount
                    FROM (
                        SELECT
                            [invoiceID],
                            COUNT([invoiceID]) as invoiceCount
                        FROM [intrafisher].[dbo].[InvComm_toFilterPaidInvoicesBuffer]
                        WHERE ABS([invoiceTotal]) = ABS([amountPaid])
                            and uploadedBy = :uploaderEmpID
                        GROUP BY [invoiceID]
                    ) AS T1
                    WHERE invoiceCount > 1
                    ORDER BY [invoiceID]
                    """;

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("uploaderEmpID", uploaderEmpID);

        List<Map<String, Object>> rows = template.queryForList(sql, parameters);
        for (Map<String, Object> row : rows) {
            int invoiceID = (int) row.get("invoiceID");
            int count = (int) row.get("invoiceCount");

            DTOs.InvoiceDup data = new DTOs.InvoiceDup(invoiceID, count);
            list.add(data);
        }
        return list;
    }

    public int saveInvoiceData(DTOs.PaidInvoiceInfo invoiceData) throws DataAccessException {
        int rowsAffected = 0;
        String sql = """
                    DECLARE @invoiceID INT = :invoiceID
                    DECLARE @invoiceDate DATE = :invoiceDate
                    DECLARE @datePaid DATE = :datePaid
                    DECLARE @invoiceTotal DECIMAL(18,2) = :invoiceTotal
                    DECLARE @amountPaid DECIMAL(18,2) = :amountPaid
                    DECLARE @uploadDatetime DATETIME = :uploadDatetime
                    DECLARE @uploadedBy INT = :uploadedBy
                    
                    
                    DECLARE @INVOICE_EXIST INT = 0
                    SET @INVOICE_EXIST = (SELECT COUNT(*)
                                            FROM [intrafisher].[dbo].[InvComm_RecordsFullyPaidInvoices]
                                            WHERE [invoiceID] = @invoiceID)
                    
                    IF(@INVOICE_EXIST > 0)
                        BEGIN
                            UPDATE [intrafisher].[dbo].[InvComm_RecordsFullyPaidInvoices]
                            SET   [invoiceDate] = @invoiceDate,
                                  [datePaid] = @datePaid,
                                  [invoiceTotal] = @invoiceTotal,
                                  [amountPaid] = @amountPaid,
                                  [uploadDatetime] = @uploadDatetime,
                                  [uploadedBy] = @uploadedBy
                            WHERE [invoiceID] = @invoiceID
                        END
                    ELSE
                        BEGIN
                            INSERT INTO [intrafisher].[dbo].[InvComm_RecordsFullyPaidInvoices]
                                      ([invoiceID]
                                      ,[invoiceDate]
                                      ,[datePaid]
                                      ,[invoiceTotal]
                                      ,[amountPaid]
                                      ,[uploadDatetime]
                                      ,[uploadedBy])
                            VALUES(@invoiceID, @invoiceDate, @datePaid, @invoiceTotal,
                                   @amountPaid, @uploadDatetime,@uploadedBy)
                        END
                    """;

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("invoiceID", invoiceData.invoiceID());
        parameters.addValue("invoiceDate", invoiceData.invoiceDate());
        parameters.addValue("datePaid", invoiceData.datePaid());
        parameters.addValue("invoiceTotal", invoiceData.invoiceTotal());
        parameters.addValue("amountPaid", invoiceData.amountPaid());
        parameters.addValue("uploadDatetime", invoiceData.uploadDatetime());
        parameters.addValue("uploadedBy", invoiceData.uploadedBy());

        rowsAffected = template.update(sql, parameters);
        return rowsAffected;
    }
}
