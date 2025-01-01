package com.fisherprinting.invoicecommissionservice.userlogin.dao;

import com.fisherprinting.invoicecommissionservice.invoiceLevel.dao.InvoiceLevelDao;
import com.fisherprinting.invoicecommissionservice.userlogin.model.User;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Repository
public class UserRepository {
    private final NamedParameterJdbcTemplate template;

    public User findByUserName(String userName) {
        List<InvoiceLevelDao.InvoiceSearchResult> list = new ArrayList<InvoiceLevelDao.InvoiceSearchResult>( );

        String sql = """
                    SELECT e.id
                      ,[username]
                      ,[InvComm_Users].[password]
                      , e.firstName + ' ' + e.lastName as fullname
                    FROM [intrafisher].[dbo].[InvComm_Users]
                        INNER JOIN employees as e on e.login = [username]
                    WHERE [username] = :userName
                    """;

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("userName", userName);

        List<Map<String, Object>> rows = template.queryForList(sql, parameters);
        User user = null;
        for (Map<String, Object> row : rows) {
            int empID = (int) row.get("id");
            String username = (String) row.get("username");
            String password = (String) row.get("password");
            String fullname = (String) row.get("fullname");
            user = new User(empID, fullname, username, password);
        }

        // https://bcrypt-generator.com/
        // 123456
        return user;
    }
}
