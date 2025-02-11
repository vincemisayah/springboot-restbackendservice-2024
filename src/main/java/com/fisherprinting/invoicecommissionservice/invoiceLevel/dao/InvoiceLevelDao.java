package com.fisherprinting.invoicecommissionservice.invoiceLevel.dao;

import com.fisherprinting.invoicecommissionservice.invoiceLevel.controller.InvoiceLevelController;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Repository
public class InvoiceLevelDao {
    private final NamedParameterJdbcTemplate template;

    public InvoiceLevelDao(NamedParameterJdbcTemplate template) {
        this.template = template;
    }

    public record InvoiceSearchResult(
            int invoiceID,
            String customerName,
            Date invoiceDate
    ) { }
    public List<InvoiceSearchResult> getInvoiceById(int invoiceID) {
        List<InvoiceSearchResult> list = new ArrayList<InvoiceSearchResult>( );

        String sql = """

                    """;

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("invoiceID", invoiceID);

        List<Map<String, Object>> rows = template.queryForList(sql, parameters);
        for (Map<String, Object> row : rows) {
            int invoiceId = (int) row.get("invoiceID");
            String customerName = (String) row.get("customerName");
            Date invoiceDate = (Date) row.get("invoiceDate");
            InvoiceSearchResult invoiceItem = new InvoiceSearchResult(invoiceId, customerName, invoiceDate);
            list.add(invoiceItem);
        }
        return list;
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

                    """;

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("invoiceID", invoiceID);
        parameters.addValue("taskID", taskID);
        parameters.addValue("empID", empID);

        List<Map<String, Object>> rows = template.queryForList(sql, parameters);
        if(rows.isEmpty( )){
            BigDecimal assignedRate = new BigDecimal(0);
            String notes = "";
            taskRateInfo = new EmployeeTaskRateInfo(assignedRate, notes);
        }else{
            for (Map<String, Object> row : rows) {
                BigDecimal assignedRate = (BigDecimal) row.get("assignedRate");
                String notes = (String) row.get("notes");
                taskRateInfo = new EmployeeTaskRateInfo(assignedRate, notes);
            }
        }
        return taskRateInfo;
    }

    public record TaskRateInfo(BigDecimal commRate, String assignedBy, Boolean active, String notes){ }
    public TaskRateInfo getTaskRateInfo(int invoiceID, int taskID){
        TaskRateInfo taskRateInfo = null;

        String sql = """

                    """;

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("invoiceID", invoiceID);
        parameters.addValue("taskID", taskID);

        List<Map<String, Object>> rows = template.queryForList(sql, parameters);

        if(rows.isEmpty()){
            BigDecimal taskRate = new BigDecimal(0);
            String assignedBy = "";
            Boolean active = false;
            String taskNote = "";
            taskRateInfo = new TaskRateInfo(taskRate, assignedBy, active, taskNote);
        }else{
            for (Map<String, Object> row : rows) {
                BigDecimal taskRate = (BigDecimal) row.get("taskRate");
                String assignedBy = (String) row.get("assignedBy");
                Boolean active = (Boolean) row.get("active");
                String taskNote = (String) row.get("taskNote");
                taskRateInfo = new TaskRateInfo(taskRate, assignedBy, active, taskNote);
            }
        }
        return taskRateInfo;
    }

    public record InvoiceTaskItem(int taskID, String taskName, int deptID, String deptName, String description){ }
    public List<InvoiceTaskItem> getDistinctChargedInvoiceTaskItems(int invoiceID) {
        List<InvoiceTaskItem> list = new ArrayList<>();

        String sql = """

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

    public record InvoiceInfo(
            int invoiceID,
            String invoiceTitle,
            Date invoiceDate,
            Date invoiceCreatedDate,
            int jobID,
            String jobName,
            String customerName){}
    public List<InvoiceInfo> getInvoiceInfoList(int invoiceID) {
        List<InvoiceInfo> list = new ArrayList<>();

        String sql = """

                    """;

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("invoiceID", invoiceID);

        List<Map<String, Object>> rows = template.queryForList(sql, parameters);
        for (Map<String, Object> row : rows) {
            int invoiceId = (int) row.get("invoiceID");
            String invoiceTitle = (String) row.get("invoiceTitle");
            Date invoiceDate = (Date) row.get("invoiceDate");
            Date invoiceCreatedDate = (Date) row.get("invoiceCreatedDate");
            int jobID = (int) row.get("jobID");
            String jobName = (String) row.get("jobName");
            String customerName = (String) row.get("customerName");
            InvoiceInfo invoiceInfo = new InvoiceInfo(invoiceId, invoiceTitle, invoiceDate, invoiceCreatedDate, jobID, jobName, customerName);

            list.add(invoiceInfo);
        }
        return list;
    }
}
