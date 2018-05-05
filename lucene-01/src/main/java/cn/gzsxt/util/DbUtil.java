package cn.gzsxt.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbUtil {

    private static Connection connection;


    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql:///lucene", "root", "root");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection(){
        return connection;
    }

    public static void close(){
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
