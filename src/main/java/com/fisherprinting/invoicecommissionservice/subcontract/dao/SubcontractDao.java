package com.fisherprinting.invoicecommissionservice.subcontract.dao;

import com.fisherprinting.invoicecommissionservice.invoiceLevel.dao.InvoiceLevelDao;
import com.fisherprinting.invoicecommissionservice.subcontract.controller.SubcontractController;
import com.fisherprinting.invoicecommissionservice.subcontract.dtos.DTOs;
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


    public List<DTOs.InvoiceChargedTaskItem> getInvoiceTaskItemsByJobId(int jobID) {
        List<DTOs.InvoiceChargedTaskItem> list = new ArrayList<DTOs.InvoiceChargedTaskItem>( );

        String sql = """

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

            DTOs.InvoiceChargedTaskItem invoiceItem = new DTOs.InvoiceChargedTaskItem(invoice, order, deptId, deptName, taskId, taskName, description, qty, cost, amount);

            list.add(invoiceItem);
        }
        return list;
    }

    public int updatePurchaseOrderItem(DTOs.PoItem poItem) throws DataAccessException {
        int rowsAffected = 0;

        String sql = """

                    """;

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("poItemID", poItem.poItemID());
        parameters.addValue("jobID", poItem.jobID());
        parameters.addValue("invoiceID", poItem.invoiceID());
        parameters.addValue("taskID", poItem.taskID());

        rowsAffected = template.update(sql, parameters);

        return rowsAffected;
    }

    public DTOs.PoItem getLinkedPurchaseOrderItem(int purchaseOrderItemID) {
        List<DTOs.PoItem> list = new ArrayList<DTOs.PoItem>( );

        String sql = """

                    """;

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("purchaseOrderItemID", purchaseOrderItemID);

        List<Map<String, Object>> rows = template.queryForList(sql, parameters);
        for (Map<String, Object> row : rows) {
            int jobId = (int) row.get("jobId");
            int invoiceID = (int) row.get("invoiceID");
            int taskID = (int) row.get("taskID");

            DTOs.PoItem poItem = new DTOs.PoItem(purchaseOrderItemID, jobId, invoiceID, taskID);
            list.add(poItem);
        }
        return list.getFirst();
    }

    public Boolean invoiceIsLinkedToPO(int invoiceID, int taskID) throws DataAccessException {
        String sql = """

                    """;

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("invoiceID", invoiceID);
        parameters.addValue("taskID", taskID);

        try{
            return template.queryForObject(sql, parameters, Boolean.class);
        }catch(DataAccessException e){
            throw e;
        }
    }

    public BigDecimal invoiceTaskIdPurchaseOrderItemPrice(int invoiceID, int taskID) throws DataAccessException {
        String sql = """

                    """;

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("invoiceID", invoiceID);
        parameters.addValue("taskID", taskID);

        try{
            return template.queryForObject(sql, parameters, BigDecimal.class);
        }catch(DataAccessException e){
            throw e;
        }
    }

    public BigDecimal getSummedInvoiceTaskIdsAmount(int invoiceID, int taskID) throws DataAccessException {
        String sql = """

                    """;

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("invoiceID", invoiceID);
        parameters.addValue("taskID", taskID);

        try{
            return template.queryForObject(sql, parameters, BigDecimal.class);
        }catch(DataAccessException e){
            throw e;
        }
    }
}
