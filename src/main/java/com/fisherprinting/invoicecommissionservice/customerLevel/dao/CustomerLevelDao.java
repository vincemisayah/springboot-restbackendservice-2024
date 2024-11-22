package com.fisherprinting.invoicecommissionservice.customerLevel.dao;

import com.fisherprinting.invoicecommissionservice.customerLevel.model.CustomerInfo;
import com.fisherprinting.invoicecommissionservice.customerLevel.model.SalesPerson;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class CustomerLevelDao {
    private final NamedParameterJdbcTemplate template;

    public CustomerLevelDao(NamedParameterJdbcTemplate template) {
        this.template = template;
    }

    public CustomerInfo getSalesPersonListById(Integer customerID) {
        CustomerInfo ci = null;

        String sql = """
                DECLARE @customerId INT = :customerID
                
                SELECT
                	 [id] ,[ARnumber] ,[name]
                	,[salesman] s1Id, [salesperson2] s2Id, [salesperson3] s3Id, [salesperson4] s4Id
                	,(SELECT lastName + ', ' + firstName FROM employees where id = [salesman])     as s1Name
                	,(SELECT lastName + ', ' + firstName FROM employees where id = [salesperson2]) as s2Name
                	,(SELECT lastName + ', ' + firstName FROM employees where id = [salesperson3]) as s3Name
                	,(SELECT lastName + ', ' + firstName FROM employees where id = [salesperson4]) as s4Name
                FROM
                	[intrafisher].[dbo].[customers]
                WHERE
                	id = @customerId
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("customerID", customerID);
        List<Map<String, Object>> rows = template.queryForList(sql, parameters);

        for (Map<String, Object> row : rows) {
            int id = (int) row.get("id");
            String arNumber = (String)(row.get("ARnumber"));
            String name = (String)(row.get("name"));

            Integer s1 = (row.get("s1Id") != null)? (int)row.get("s1Id"):null;
            Integer s2 = (row.get("s2Id") != null)? (int)row.get("s2Id"):null;
            Integer s3 = (row.get("s3Id") != null)? (int)row.get("s3Id"):null;
            Integer s4 = (row.get("s4Id") != null)? (int)row.get("s4Id"):null;

            String s1Name = (row.get("s1Name") != null)? (String) row.get("s1Name"):null;
            String s2Name = (row.get("s2Name") != null)? (String) row.get("s2Name"):null;
            String s3Name = (row.get("s3Name") != null)? (String) row.get("s3Name"):null;
            String s4Name = (row.get("s4Name") != null)? (String) row.get("s4Name"):null;

            List<SalesPerson> li = new ArrayList<SalesPerson>( );

            if((s1 != null && s1 > 0) && s1Name != null)
                li.add(new SalesPerson(s1, s1Name));

            if((s2 != null && s2 > 0) && s2Name != null)
                li.add(new SalesPerson(s2, s2Name));

            if((s3 != null && s3 > 0) && s3Name != null)
                li.add(new SalesPerson(s3, s3Name));

            if((s4 != null && s4 > 0) && s4Name != null)
                li.add(new SalesPerson(s4, s4Name));

            ci = new CustomerInfo(id, name, arNumber, li);
        }
        return ci;
    }

    public List<CustomerInfo> getActiveCustomerListById(String arNumber) {
        List<CustomerInfo> list = new ArrayList<CustomerInfo>( );

        String sql = """
                DECLARE @AR_NUMBER VARCHAR(25) = :arNumber
                DECLARE @CUSTOMER_STATUS_ACTIVE INT = 10
                SELECT TOP(200)  [id] as customerId
                FROM customers
                WHERE
                		[ARnumber] LIKE TRIM(@AR_NUMBER) + '%'
                		AND [status] = @CUSTOMER_STATUS_ACTIVE
                ORDER BY [ARnumber] ASC""";

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("arNumber", arNumber);
        List<Map<String, Object>> rows = template.queryForList(sql, parameters);

        for (Map<String, Object> row : rows) {
            int cId = (int) row.get("customerId");
            CustomerInfo customerItem = this.getSalesPersonListById(cId);
            list.add(customerItem);
        }
        return list;
    }

    public record TaskDepartment(int id, String department) { }
    public List<TaskDepartment> getTaskDepartmentsList( ){
        List<TaskDepartment> list = new ArrayList<>( );

        String sql = """
                SELECT DISTINCT
                    T2.id as id,
                    T2.name as department
                FROM [intrafisher].[dbo].[invTasks] as T1
                    INNER JOIN [intrafisher].[dbo].[invDepts] as T2 on T1.dept = T2.id
                WHERE isActive <> 0
                ORDER BY T2.name ASC""";

        MapSqlParameterSource parameters = new MapSqlParameterSource();

        List<Map<String, Object>> rows = template.queryForList(sql, parameters);
        for (Map<String, Object> row : rows) {

            int deptId = (int) row.get("id");
            String departmentName = (String) row.get("department");
            TaskDepartment taskDepartment = new TaskDepartment(deptId, departmentName);
            list.add(taskDepartment);
        }
        return list;
    }

    public record InvoiceTaskItem(int id, int deptId, String department, String taskName, String description) { }
    public List<InvoiceTaskItem> getInvoiceTaskItemList( ){
        List<InvoiceTaskItem> list = new ArrayList<>( );

        String sql = """
                    SELECT 
                        T1.id as id,
                        T2.id as deptId,
                        T2.name as department,
                        T1.name as taskName,
                        CASE
                            WHEN LEN(T1.defaultDescription) < 1 OR T1.defaultDescription IS NULL THEN 'N/A'
                            ELSE T1.defaultDescription
                        END as description
                    FROM [intrafisher].[dbo].[invTasks] as T1
                        INNER JOIN [intrafisher].[dbo].[invDepts] as T2 on T1.dept = T2.id
                    WHERE isActive <> 0
                    ORDER BY T2.name, T1.name ASC
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource();

        List<Map<String, Object>> rows = template.queryForList(sql, parameters);
        for (Map<String, Object> row : rows) {

            int taskId = (int) row.get("id");
            int deptId = (int) row.get("deptId");
            String departmentName = (String) row.get("department");
            String taskName = (String) row.get("taskName");
            String description = (String) row.get("description");

            InvoiceTaskItem invoiceItem = new InvoiceTaskItem(taskId, deptId, departmentName, taskName, description);

            list.add(invoiceItem);
        }
        return list;
    }

    public record CustomerLevelTaskRates(String mapKey, int taskID, BigDecimal rate, String assignedBy, String notes) { }
    public List<CustomerLevelTaskRates> getCustomerLevelTaskRatesList(int customerID){
        List<CustomerLevelTaskRates> list = new ArrayList<>( );

        String sql = """
                    DECLARE @CUSOMTER_ID INT = :customerID
        
                    SELECT
                        'commRateTaskId#' + CONVERT(VARCHAR(10), [taskID]) as mapKey
                        ,[taskID]
                        ,[commRate]
                        ,employees.firstName + ' ' + employees.lastName as assignedBy
                        ,[notes]
                    FROM [intrafisher].[dbo].[InvComm_Config_CustomerLevel]
                        INNER JOIN employees on employees.id = assignedBy
                    WHERE [customerID] = @CUSOMTER_ID""";

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("customerID", customerID);

        List<Map<String, Object>> rows = template.queryForList(sql, parameters);
        for (Map<String, Object> row : rows) {
            String mapKey = (String) row.get("mapKey");
            int taskID = (int) row.get("taskID");
            BigDecimal commRate = (BigDecimal) row.get("commRate");
            String assignedBy = (String) row.get("assignedBy");
            String notes = (String) row.get("notes");

            CustomerLevelTaskRates rateItem = new CustomerLevelTaskRates(mapKey, taskID, commRate, assignedBy, notes);
            list.add(rateItem);
        }
        return list;
    }

    public record SalesPersonAssignedRateMapper(String mapKey, BigDecimal assignedRate, int assignedBy, String notes) { }
    public List<SalesPersonAssignedRateMapper> getSalesPersonAssignedRateMapperList(int customerID){
        List<SalesPersonAssignedRateMapper> list = new ArrayList<>( );

        String sql = """
                DECLARE @CUSTOMER_ID INT = :customerID
                SELECT
                    'taskId#' + CONVERT(varchar(10), [taskID]) + '#salesId#' + CONVERT(varchar(10), [empID]) as mapKey
                    ,[assignedRate]
                    ,[assignedBy]
                    ,[notes]
                FROM [intrafisher].[dbo].[InvComm_EmpAssignedRates_CustomerLevel]
                WHERE [customerID] = @CUSTOMER_ID
                """;

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("customerID", customerID);

        List<Map<String, Object>> rows = template.queryForList(sql, parameters);
        for (Map<String, Object> row : rows) {
            String mapKey = (String) row.get("mapKey");
            BigDecimal commRate = (BigDecimal) row.get("assignedRate");
            int assignedBy = (int) row.get("assignedBy");
            String notes = (String) row.get("notes");

            SalesPersonAssignedRateMapper item = new SalesPersonAssignedRateMapper(mapKey, commRate, assignedBy, notes);
            list.add(item);
        }
        return list;
    }

    public int updateInsertCustomerTaskConfig(int customerID, int taskID, int assignedBy, BigDecimal commRate, String notes) throws DataAccessException{
        int numberOfRowsAffected = 0;
        String sql = """
                    DECLARE @CUSTOMER_ID INT = :customerID
                    DECLARE @TASK_ID INT = :taskID
                    DECLARE @ASSIGNED_BY INT = :assignedBy
                    DECLARE @COMM_RATE DECIMAL(18,2) = :commRate
                    DECLARE @NOTES VARCHAR(250) = :notes
                    
                    DECLARE @CONFIG_EXIST INT = 0
                    SET @CONFIG_EXIST = (SELECT COUNT(*) FROM [intrafisher].[dbo].[InvComm_Config_CustomerLevel] WHERE [customerID] = @CUSTOMER_ID AND [taskID] = @TASK_ID)
                    
                    
                    IF(@CONFIG_EXIST > 0)
                        BEGIN
                            UPDATE [intrafisher].[dbo].[InvComm_Config_CustomerLevel]
                            SET [commRate] = @COMM_RATE,
                                [assignedBy] = @ASSIGNED_BY,
                                [notes] = @NOTES
                            WHERE [customerID] = @CUSTOMER_ID AND [taskID] = @TASK_ID
                        END
                    ELSE
                        BEGIN
                            INSERT INTO [intrafisher].[dbo].[InvComm_Config_CustomerLevel]
                                        ([customerID],[taskID],[commRate],[assignedBy],[notes])
                            VALUES(@CUSTOMER_ID, @TASK_ID, @COMM_RATE, @ASSIGNED_BY, @NOTES)
                        END
                    """;

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("customerID", customerID);
        parameters.addValue("taskID", taskID);
        parameters.addValue("assignedBy", assignedBy);
        parameters.addValue("commRate", commRate);
        parameters.addValue("notes", notes);

        numberOfRowsAffected = template.update(sql, parameters);

        return numberOfRowsAffected;
    }

    public int updateInsertSalespersonConfig(int customerID, int empID, int taskID, BigDecimal assignedRate, int assignedBy, String notes) throws DataAccessException {
        int numberOfRowsAffected = 0;
        String sql = """
                    DECLARE @CUSTOMER_ID INT = :customerID
                    DECLARE @EMP_ID INT = :empID
                    DECLARE @TASK_ID INT = :taskID
                    
                    DECLARE @ASSIGNED_RATE INT = :assignedRate
                    DECLARE @ASSIGNED_BY INT = :assignedBy
                    DECLARE @NOTES VARCHAR(150) = :notes
                    
                    DECLARE @CONFIG_EXIST INT = 0
                    SET @CONFIG_EXIST = (SELECT COUNT(*)\s
                        FROM [intrafisher].[dbo].[InvComm_EmpAssignedRates_CustomerLevel]\s
                        WHERE [customerID] = @CUSTOMER_ID
                        AND [empID] = @EMP_ID
                        AND [taskID] = @TASK_ID)
                    
                    IF(@CONFIG_EXIST > 0)
                        BEGIN
                            UPDATE [intrafisher].[dbo].[InvComm_EmpAssignedRates_CustomerLevel]\s
                            SET [assignedRate] = @ASSIGNED_RATE,
                                [assignedBy] = @ASSIGNED_BY,
                                [notes] = @NOTES
                            WHERE [customerID] = @CUSTOMER_ID
                                AND [empID] = @EMP_ID
                                AND [taskID] = @TASK_ID
                        END
                    ELSE
                        BEGIN
                            INSERT INTO [intrafisher].[dbo].[InvComm_EmpAssignedRates_CustomerLevel]\s
                                ([customerID],[empID],[taskID],[assignedRate],[assignedBy],[notes])
                            VALUES(@CUSTOMER_ID, @EMP_ID, @TASK_ID, @ASSIGNED_RATE, @ASSIGNED_BY, @NOTES)
                        END
                    """;

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("customerID", customerID);
        parameters.addValue("empID", empID);
        parameters.addValue("taskID", taskID);
        parameters.addValue("assignedRate", assignedRate);
        parameters.addValue("assignedBy", assignedBy);
        parameters.addValue("notes", notes);

        numberOfRowsAffected = template.update(sql, parameters);

        return numberOfRowsAffected;
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



    public record CustomerAndJobInfo(int customerID, String arNumber,String customerName, int jobID, String jobName, int invoiceID){ }
    public CustomerAndJobInfo getCustomerAndJobInfo(int invoiceID) {
        CustomerAndJobInfo customerAndJobInfo = null;

        String sql = """
                    DECLARE @invoiceId int = :invoiceID
                    
                    SELECT
                        customer as customerId,
                        t3.ARnumber,
                        t3.name as customerName,
                        job as jobId,
                        t2.name as jobName,
                        t1.id as invoiceId
                    FROM [intrafisher].[dbo].[invoices] as t1
                        INNER JOIN jobs as t2 on t1.job = t2.id
                        INNER JOIN customers as t3 on t1.customer = t3.id
                    WHERE t1.id = @invoiceId
                    """;

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("invoiceID", invoiceID);

        List<Map<String, Object>> rows = template.queryForList(sql, parameters);
        for (Map<String, Object> row : rows) {
            int customerId = (int) row.get("customerId");
            String arNumber = (String) row.get("arNumber");
            String customerName = (String) row.get("customerName");
            int jobId = (int) row.get("jobId");
            String jobName = (String) row.get("jobName");
            int invoiceId = (int) row.get("invoiceId");
            customerAndJobInfo = new CustomerAndJobInfo(customerId, arNumber, customerName, jobId, jobName, invoiceId);

        }
        return customerAndJobInfo;
    }

    public record EmployeeTaskRateInfo(BigDecimal commRate, String assignedBy, String notes){ }
    public EmployeeTaskRateInfo getEmployeeTaskRateInfo(int customerID, int empID, int taskID) {
        EmployeeTaskRateInfo taskRateInfo = null;

        String sql = """
                    DECLARE @CUSTOMER_ID INT = :customerID
                    DECLARE @EMP_ID INT = :empID
                    DECLARE @TASK_ID INT = :taskID
                    
                    SELECT [assignedRate]
                          ,employees.firstName + ' ' + employees.lastName as assignedBy
                          ,[notes]
                      FROM [intrafisher].[dbo].[InvComm_EmpAssignedRates_CustomerLevel]
                        INNER JOIN employees ON employees.id = assignedBy
                      WHERE [customerID] = @CUSTOMER_ID
                        AND [taskID] = @TASK_ID
                        AND [empID] = @EMP_ID
                    """;

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("customerID", customerID);
        parameters.addValue("empID", empID);
        parameters.addValue("taskID", taskID);

        List<Map<String, Object>> rows = template.queryForList(sql, parameters);
        for (Map<String, Object> row : rows) {
            BigDecimal assignedRate = (BigDecimal) row.get("assignedRate");
            String assignedBy = (String) row.get("assignedBy");
            String notes = (String) row.get("notes");
            taskRateInfo = new EmployeeTaskRateInfo(assignedRate, assignedBy, notes);
        }
        return taskRateInfo;
    }

    public record TaskRateInfo(BigDecimal commRate, String assignedBy, String notes){ }
    public TaskRateInfo getTaskRateInfo(int customerID, int empID, int taskID) {
        TaskRateInfo taskRateInfo = null;

        String sql = """
                    DECLARE @CUSTOMER_ID INT = :customerID
                    DECLARE @TASK_ID INT = :taskID
                    
                    
                    SELECT [commRate]
                          , employees.firstName + ' ' + employees.lastName as assignedBy
                          ,[notes]
                      FROM [intrafisher].[dbo].[InvComm_Config_CustomerLevel]
                        INNER JOIN employees ON employees.id = assignedBy
                      WHERE [customerID] = @CUSTOMER_ID
                        AND [taskID] = @TASK_ID
                    """;

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("customerID", customerID);
        parameters.addValue("taskID", taskID);

        List<Map<String, Object>> rows = template.queryForList(sql, parameters);
        for (Map<String, Object> row : rows) {
            BigDecimal commRate = (BigDecimal) row.get("commRate");
            String assignedBy = (String) row.get("assignedBy");
            String notes = (String) row.get("notes");
            taskRateInfo = new TaskRateInfo(commRate, assignedBy, notes);
        }
        return taskRateInfo;
    }
}
