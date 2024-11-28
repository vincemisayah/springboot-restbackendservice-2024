package com.fisherprinting.invoicecommissionservice.invoiceLevel.dao;

import com.fisherprinting.invoicecommissionservice.invoiceLevel.controller.InvoiceLevelController;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Repository
public class InvoiceLevelDao {
    private final NamedParameterJdbcTemplate template;

    public InvoiceLevelDao(NamedParameterJdbcTemplate template) {
        this.template = template;
    }


    public record InvoiceChargedTaskItem(
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
    public List<InvoiceChargedTaskItem> getInvoiceChargedItems(int invoiceId) {
        List<InvoiceChargedTaskItem> list = new ArrayList<InvoiceChargedTaskItem>( );

        String sql = """
                    DECLARE @invoiceId as int = :invoiceId
                    
                    SELECT
                        [order],
                        t2.id as deptId,
                        t2.name as deptName,
                        [task] as taskId,
                        t1.name as taskName,
                        [desc] as description,
                        [quantity] as qty,
                        CONVERT(decimal(18,2),[cost], 2) as cost,
                        -- Determines the amount based on 'per thousand' or '%'
                        CASE
                            WHEN t1.unitName = '%'
                                THEN CONVERT(decimal(18,2),([quantity] * cost)/100, 2)
                            WHEN t1.chargePerM > 0 OR t1.unitName like '%PM%'
                                THEN CONVERT(decimal(18,2),([quantity] * cost)/1000, 2)
                                ELSE CONVERT(decimal(18,2),([quantity] * cost), 2)
                        END as amount
                    FROM [intrafisher].[dbo].[invoiceItems]
                        INNER JOIN [intrafisher].[dbo].[invTasks] as t1 on [task] = t1.id
                        INNER JOIN [intrafisher].[dbo].[invDepts] as t2 on t1.dept = t2.id
                    
                    WHERE [invoice] = @invoiceId
                    ORDER BY [order] ASC
                    """;

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("invoiceId", invoiceId);

        List<Map<String, Object>> rows = template.queryForList(sql, parameters);
        for (Map<String, Object> row : rows) {

            int order = (int) row.get("order");
            int deptId = (int) row.get("deptId");
            String deptName = (String) row.get("deptName");
            int taskId = (int) row.get("taskId");
            String taskName = (String) row.get("taskName");
            String description = (String) row.get("description");
            Double qty = (Double) row.get("qty");
            BigDecimal cost = (BigDecimal) row.get("cost");
            BigDecimal amount = (BigDecimal) row.get("amount");

            InvoiceChargedTaskItem invoiceItem = new InvoiceChargedTaskItem(order, deptId, deptName, taskId, taskName, description, qty, cost, amount);

            list.add(invoiceItem);
        }
        return list;
    }

    public int saveEmployeeConfig(int invoiceId, int taskId, int empId, BigDecimal assignedRate, String note) throws DataAccessException{
        int rowsAffected = 0;

        String sql = """
                    DECLARE @INVOICE_ID INT = :invoiceID
                    DECLARE @TASK_ID INT = :taskID
                    DECLARE @EMP_ID INT = :empID
                    DECLARE @ASSIGNED_RATE DECIMAL(18,2) = :assignedRate
                    DECLARE @NOTE VARCHAR(110) = :note
                    
                                       \s
                    DECLARE @CONFIG_EXIST INT = 0
                    SET @CONFIG_EXIST = (SELECT COUNT(*)
                                         FROM [intrafisher].[dbo].[InvComm_EmpAssignedRates_InvoiceLevel]
                                         WHERE [invoiceID] = @INVOICE_ID
                                            AND [taskID] = @TASK_ID
                                            AND [empID] = @EMP_ID)
                    
                    IF(@CONFIG_EXIST > 0)
                        BEGIN
                            UPDATE [intrafisher].[dbo].[InvComm_EmpAssignedRates_InvoiceLevel]
                            SET  [assignedRate] = @ASSIGNED_RATE
                                ,[notes] = @NOTE
                             WHERE [invoiceID] = @INVOICE_ID
                                    AND [taskID] = @TASK_ID
                                    AND [empID] = @EMP_ID
                        END
                    ELSE
                        BEGIN
                            INSERT INTO [intrafisher].[dbo].[InvComm_EmpAssignedRates_InvoiceLevel]
                                  ([invoiceID]
                                  ,[taskID]
                                  ,[empID]
                                  ,[assignedRate]
                                  ,[notes])
                    
                            VALUES(@INVOICE_ID
                                  ,@TASK_ID
                                  ,@EMP_ID
                                  ,@ASSIGNED_RATE
                                  ,@NOTE)
                        END
                    """;

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("invoiceID", invoiceId);
        parameters.addValue("taskID", taskId);
        parameters.addValue("empID", empId);
        parameters.addValue("assignedRate", assignedRate);
        parameters.addValue("note", note);

        rowsAffected = template.update(sql, parameters);

        return rowsAffected;
    }

    public record EmployeeTaskRateInfo(BigDecimal commRate, String notes){ }
    public EmployeeTaskRateInfo getEmployeeTaskRateInfo(int invoiceID, int empID, int taskID) {
        EmployeeTaskRateInfo taskRateInfo = null;

        String sql = """
                    DECLARE @INVOICE_ID INT = :invoiceID
                    DECLARE @TASK_ID INT = :taskID
                    DECLARE @EMP_ID INT = :empID
                    
                    
                    
                    SELECT [assignedRate]
                          ,[notes]
                    FROM [intrafisher].[dbo].[InvComm_EmpAssignedRates_InvoiceLevel]
                    WHERE [invoiceID] = @INVOICE_ID\s
                        AND [taskID] = @TASK_ID
                        AND [empID] = @EMP_ID
                    """;

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("invoiceID", invoiceID);
        parameters.addValue("taskID", taskID);
        parameters.addValue("empID", empID);

        List<Map<String, Object>> rows = template.queryForList(sql, parameters);
        for (Map<String, Object> row : rows) {
            BigDecimal assignedRate = (BigDecimal) row.get("assignedRate");
            String notes = (String) row.get("notes");
            taskRateInfo = new EmployeeTaskRateInfo(assignedRate, notes);
        }
        return taskRateInfo;
    }

    public record TaskRateInfo(BigDecimal commRate, String assignedBy, Boolean active, String notes){ }
    public TaskRateInfo getTaskRateInfo(int invoiceID, int taskID){
        TaskRateInfo taskRateInfo = null;

        String sql = """
                    DECLARE @INVOICE_ID INT = :invoiceID
                    DECLARE @TASK_ID INT = :taskID
                    
                    SELECT [taskRate],
                           employees.firstName + ' ' + employees.lastName as assignedBy,
                           [InvComm_Config_InvoiceLevel].[active],
                           [taskNote]
                    FROM [intrafisher].[dbo].[InvComm_Config_InvoiceLevel]
                        INNER JOIN employees ON employees.id = assignedBy
                    WHERE [invoiceID] = @INVOICE_ID
                        AND [taskID] = @TASK_ID
                    """;

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("invoiceID", invoiceID);
        parameters.addValue("taskID", taskID);

        List<Map<String, Object>> rows = template.queryForList(sql, parameters);
        for (Map<String, Object> row : rows) {
            BigDecimal taskRate = (BigDecimal) row.get("taskRate");
            String assignedBy = (String) row.get("assignedBy");
            Boolean active = (Boolean) row.get("active");
            String taskNote = (String) row.get("taskNote");
            taskRateInfo = new TaskRateInfo(taskRate, assignedBy, active, taskNote);
        }
        return taskRateInfo;
    }

    public record InvoiceTaskItem(int taskID, String taskName, int deptID, String deptName, String description){ }
    public List<InvoiceTaskItem> getDistinctChargedInvoiceTaskItems(int invoiceID) {
        List<InvoiceTaskItem> list = new ArrayList<>();

        String sql = """
                    DECLARE @invoiceId as int = :invoiceID
                    
                    SELECT DISTINCT
                        [task] as taskId,
                        t1.name as taskName,
                        t2.id as deptId,
                        t2.name as deptName,
                        [desc] as description
                    
                    FROM [intrafisher].[dbo].[invoiceItems]
                        INNER JOIN [intrafisher].[dbo].[invTasks] as t1 on [task] = t1.id
                        INNER JOIN [intrafisher].[dbo].[invDepts] as t2 on t1.dept = t2.id
                    WHERE [invoice] = @invoiceId
                    """;

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("invoiceID", invoiceID);

        List<Map<String, Object>> rows = template.queryForList(sql, parameters);
        for (Map<String, Object> row : rows) {
            int taskId = (int) row.get("taskId");
            String taskName = (String) row.get("taskName");
            int deptId = (int) row.get("deptId");
            String deptName = (String) row.get("deptName");
            String description = (String) row.get("description");

            InvoiceTaskItem invoiceTaskItem = new InvoiceTaskItem(taskId, taskName, deptId, deptName, description);
            list.add(invoiceTaskItem);
        }
        return list;
    }

    public int saveTaskConfiguration(InvoiceLevelController.InvoiceTaskConfig config) throws DataAccessException{
        int rowsAffected = 0;

        String sql = """
                DECLARE @LAST_EDITED_BY INT = :lastEditedBy
                DECLARE @CUSTOMER_ID INT = :customerID
                DECLARE @INVOICE_ID INT = :invoiceID
                DECLARE @TASK_ID INT = :taskID
                DECLARE @TASK_RATE DECIMAL(18,2) = :taskRate
                DECLARE @ACTIVE BIT = :active
                DECLARE @NOTE VARCHAR(110) = :notes
                
                                   \s
                DECLARE @CONFIG_EXIST INT = 0
                SET @CONFIG_EXIST = (SELECT COUNT(*)
                                     FROM [intrafisher].[dbo].[InvComm_Config_InvoiceLevel]
                                     WHERE [invoiceID] = @INVOICE_ID
                                        AND [taskID] = @TASK_ID)
                
                           \s
                
                IF(@CONFIG_EXIST > 0)
                    BEGIN
                        UPDATE [intrafisher].[dbo].[InvComm_Config_InvoiceLevel]
                        SET [taskRate] = @TASK_RATE
                           ,[taskNote] = @NOTE
                           ,[assignedBy] = @LAST_EDITED_BY
                           ,[lastEdit] = GETDATE( )
                           ,[active] = @ACTIVE
                        WHERE [invoiceID] = @INVOICE_ID
                            AND [taskID] = @TASK_ID
                    END
                ELSE
                    BEGIN
                        INSERT INTO [intrafisher].[dbo].[InvComm_Config_InvoiceLevel]
                              ([invoiceID]
                              ,[taskID]
                              ,[taskRate]
                              ,[taskNote]
                              ,[assignedBy]
                              ,[lastEdit]
                              ,[active])
                
                        VALUES(@INVOICE_ID
                              ,@TASK_ID
                              ,@TASK_RATE
                              ,@NOTE
                              ,@LAST_EDITED_BY
                              ,GETDATE( )
                              ,@ACTIVE)
                    END
                """;
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("lastEditedBy", config.lastEditedBy());
        parameters.addValue("customerID", config.customerID());
        parameters.addValue("invoiceID", config.invoiceID());
        parameters.addValue("taskID", config.taskID());
        parameters.addValue("taskRate", config.taskRate());
        parameters.addValue("active", config.active());
        parameters.addValue("notes", config.notes());

        rowsAffected = template.update(sql, parameters);

        return rowsAffected;
    }
}
