package transactions;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages the PostgreSQL JDBC connection.
 * Edit the constants below to match your environment.
 */
public class DBConnection {

    private static final String HOST     = "localhost";
    private static final String PORT     = "5432";
    private static final String DATABASE = "projectdb";
    private static final String USER     = "postgres";
    private static final String PASSWORD = "postgres";

    private static final String URL =
            "jdbc:postgresql://" + HOST + ":" + PORT + "/" + DATABASE;

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
        conn.setAutoCommit(false); // all callers manage transactions explicitly
        return conn;
    }
}
