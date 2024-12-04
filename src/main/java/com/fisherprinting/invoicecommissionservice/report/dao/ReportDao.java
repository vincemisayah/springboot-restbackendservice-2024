package com.fisherprinting.invoicecommissionservice.report.dao;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public class ReportDao {
    private final NamedParameterJdbcTemplate template;

    public ReportDao(NamedParameterJdbcTemplate template) {
        this.template = template;
    }

    public int updateInsertCustomerTaskConfig(int customerID, int taskID, int assignedBy, BigDecimal commRate, String notes) throws DataAccessException {
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
}
