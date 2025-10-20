package dao;

import model.CartItem;
import model.Registration;
import model.RegistrationDetail;
import java.sql.SQLException;
import java.util.List;

public interface RegistrationDao {
    void confirm(String username, List<CartItem> items) throws SQLException;
    
    List<model.Registration> listByUser(String username) throws SQLException;
    
    List<model.RegistrationDetail> listDetailsByUser(String username) throws SQLException;
}
