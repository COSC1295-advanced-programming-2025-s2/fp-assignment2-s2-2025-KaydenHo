package dao;

import model.CartItem;
import java.sql.SQLException;
import java.util.List;

public interface RegistrationDao {
    void confirm(String username, List<CartItem> items) throws SQLException;
}
