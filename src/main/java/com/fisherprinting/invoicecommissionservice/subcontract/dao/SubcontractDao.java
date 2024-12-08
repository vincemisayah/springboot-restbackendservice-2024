package com.fisherprinting.invoicecommissionservice.subcontract.dao;

import com.fisherprinting.invoicecommissionservice.invoiceLevel.dao.InvoiceLevelDao;
import com.fisherprinting.invoicecommissionservice.subcontract.controller.SubcontractController;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class SubcontractDao {
    private final NamedParameterJdbcTemplate template;

    public SubcontractDao(NamedParameterJdbcTemplate template) {
        this.template = template;
    }

    public record InvoiceChargedTaskItem(
            int invoice,
            int order,
            int deptId,
            String deptName,
            int taskId,
            String taskName,
            String description,
            Double qty,
            BigDecimal cost,
            BigDecimal amount
    ) { }
    public List<InvoiceChargedTaskItem> getInvoiceTaskItemsByJobId(int jobID) {
        List<InvoiceChargedTaskItem> list = new ArrayList<InvoiceChargedTaskItem>( );

        String sql = """
                    DECLARE @JOB_ID as int = :jobID
    
                    SELECT\s
                        [invoice], [order],
                        t2.id as deptId,
                        t2.name as deptName,
                        [task] as taskId,
                        t1.name as taskName,
                        [desc] as description,\s
                        [quantity] as qty,\s
                        CONVERT(decimal(18,2),[cost], 2) as cost,\s
                        -- Determines the amount based on 'per thousand' or '%'
                        CASE\s
                            WHEN t1.unitName = '%'\s
                                THEN CONVERT(decimal(18,2),([quantity] * cost)/100, 2)\s
                            WHEN t1.chargePerM > 0 OR t1.unitName like '%PM%'\s
                                THEN CONVERT(decimal(18,2),([quantity] * cost)/1000, 2)\s
                                ELSE CONVERT(decimal(18,2),([quantity] * cost), 2)\s
                        END as amount
                    FROM [intrafisher].[dbo].[invoiceItems]\s
                        INNER JOIN [intrafisher].[dbo].[invTasks] as t1 on [task] = t1.id\s
                        INNER JOIN [intrafisher].[dbo].[invDepts] as t2 on t1.dept = t2.id\s
    
                    WHERE [invoice] IN (SELECT id from invoices WHERE job = @JOB_ID)
                    ORDER BY [invoice], [order] ASC
                    """;

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("jobID", jobID);

        List<Map<String, Object>> rows = template.queryForList(sql, parameters);
        for (Map<String, Object> row : rows) {
            int invoice = (int) row.get("invoice");
            int order = (int) row.get("order");
            int deptId = (int) row.get("deptId");
            String deptName = (String) row.get("deptName");
            int taskId = (int) row.get("taskId");
            String taskName = (String) row.get("taskName");
            String description = (String) row.get("description");
            Double qty = (Double) row.get("qty");
            BigDecimal cost = (BigDecimal) row.get("cost");
            BigDecimal amount = (BigDecimal) row.get("amount");

            InvoiceChargedTaskItem invoiceItem = new InvoiceChargedTaskItem(invoice, order, deptId, deptName, taskId, taskName, description, qty, cost, amount);

            list.add(invoiceItem);
        }
        return list;
    }

    public int updatePurchaseOrderItem(SubcontractController.PoItem poItem) throws DataAccessException {
        int rowsAffected = 0;

        String sql = """
                    DECLARE @PO_ITEM_ID INT = :poItemID
                    DECLARE @JOB_ID INT = :jobID
                    DECLARE @INVOICE_ID INT = :invoiceID
                    DECLARE @TASK_ID INT = :taskID
                    
                    UPDATE [intrafisher].[dbo].[PURCH_purchaseOrderItems]
                    SET [jobId] = @JOB_ID,
                        invoiceID = @INVOICE_ID,\s
                        [taskID] = @TASK_ID
                    WHERE [id] = @PO_ITEM_ID
                    """;

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("poItemID", poItem.poItemID());
        parameters.addValue("jobID", poItem.jobID());
        parameters.addValue("invoiceID", poItem.invoiceID());
        parameters.addValue("taskID", poItem.taskID());

        rowsAffected = template.update(sql, parameters);

        return rowsAffected;
    }

    public SubcontractController.PoItem getLinkedPurchaseOrderItem(int purchaseOrderItemID) {
        List<SubcontractController.PoItem> list = new ArrayList<SubcontractController.PoItem>( );

        String sql = """
                    DECLARE @PO_ITEM_ID INT = :purchaseOrderItemID
                    
                    SELECT [poId]
                          ,[jobId]
                          ,[invoiceID]
                          ,[taskID]
                    FROM [intrafisher].[dbo].[PURCH_purchaseOrderItems]
                    WHERE [id] = @PO_ITEM_ID
                    """;

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("purchaseOrderItemID", purchaseOrderItemID);

        List<Map<String, Object>> rows = template.queryForList(sql, parameters);
        for (Map<String, Object> row : rows) {
            int jobId = (int) row.get("jobId");
            int invoiceID = (int) row.get("invoiceID");
            int taskID = (int) row.get("taskID");

            SubcontractController.PoItem poItem = new SubcontractController.PoItem(purchaseOrderItemID, jobId, invoiceID, taskID);
            list.add(poItem);
        }
        return list.getFirst();
    }
}
