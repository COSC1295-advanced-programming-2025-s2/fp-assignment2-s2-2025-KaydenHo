
package dao;

import model.CartItem;
import java.sql.SQLException;
import java.util.List;

public interface CartDao {
    void upsertCartItem(String username, int projectId, int slots, int hours) 
    		throws SQLException;
    void removeCartItem(String username, int projectId) 
    		throws SQLException;
    void clearCart(String username) 
    		throws SQLException;
    List<CartItem> listCart(String username) 
    		throws SQLException;
}
