package transactions;

import java.sql.*;
import java.util.Scanner;

/**
 * Implements six ACID transactions against the Product / Depot / Stock schema.
 *
 * Reactive constraints (ON DELETE CASCADE, ON UPDATE CASCADE) are declared in
 * the DDL so that a single DML statement on a parent table automatically
 * propagates the change to Stock, satisfying the "in Product AND Stock" /
 * "in Depot AND Stock" requirement.  Each transaction wraps its work inside an
 * explicit BEGIN / COMMIT block (autoCommit = false) and rolls back on any
 * error, ensuring Atomicity, Consistency, Isolation, and Durability.
 */
public class Main {

    // -----------------------------------------------------------------------
    // Entry point
    // -----------------------------------------------------------------------

    public static void main(String[] args) {
        System.out.println("=== SQL Transactions Demo ===\n");
        printTables("Current state");

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Select a transaction to run:");
            System.out.println("  1 – Delete product p1 from Product (cascades to Stock)");
            System.out.println("  2 – Delete depot d1 from Depot (cascades to Stock)");
            System.out.println("  3 – Rename product p1 → pp1 in Product (cascades to Stock)");
            System.out.println("  4 – Rename depot d1 → dd1 in Depot (cascades to Stock)");
            System.out.println("  5 – Add product (p100, cd, 5) and stock (p100, d2, 50)");
            System.out.println("  6 – Add depot (d100, Chicago, 100) and stock (p1, d100, 100)");
            System.out.println("  0 – Reset tables to original seed data");
            System.out.println("  q – Quit");
            System.out.print("\nEnter choice: ");

            String input = scanner.nextLine().trim();
            System.out.println();

            switch (input) {
                case "1": runTransaction1_DeleteProduct(); break;
                case "2": runTransaction2_DeleteDepot();   break;
                case "3": runTransaction3_RenameProduct(); break;
                case "4": runTransaction4_RenameDepot();   break;
                case "5": runTransaction5_AddProduct();    break;
                case "6": runTransaction6_AddDepot();      break;
                case "0":
                    resetData();
                    printTables("After reset");
                    break;
                case "q": case "Q":
                    System.out.println("Goodbye.");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid choice. Please enter 0–6 or q.\n");
            }
        }
    }

    // -----------------------------------------------------------------------
    // Transaction 1 – Delete product p1 from Product and Stock
    // -----------------------------------------------------------------------

    private static void runTransaction1_DeleteProduct() {
        System.out.println(">>> Transaction 1: Delete product p1 from Product (cascades to Stock)");
        String sql = "DELETE FROM Product WHERE prodid = 'p1'";

        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int rows = ps.executeUpdate();
                conn.commit();
                System.out.println("    Rows deleted from Product: " + rows
                        + "  (Stock rows removed automatically via CASCADE)");
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("    ROLLBACK – " + e.getMessage());
            }
        } catch (SQLException e) {
            System.err.println("    Connection error – " + e.getMessage());
        }

        printTables("After Transaction 1");
    }

    // -----------------------------------------------------------------------
    // Transaction 2 – Delete depot d1 from Depot and Stock
    // -----------------------------------------------------------------------

    private static void runTransaction2_DeleteDepot() {
        System.out.println(">>> Transaction 2: Delete depot d1 from Depot (cascades to Stock)");
        String sql = "DELETE FROM Depot WHERE depid = 'd1'";

        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int rows = ps.executeUpdate();
                conn.commit();
                System.out.println("    Rows deleted from Depot: " + rows
                        + "  (Stock rows removed automatically via CASCADE)");
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("    ROLLBACK – " + e.getMessage());
            }
        } catch (SQLException e) {
            System.err.println("    Connection error – " + e.getMessage());
        }

        printTables("After Transaction 2");
    }

    // -----------------------------------------------------------------------
    // Transaction 3 – Rename product p1 → pp1 in Product and Stock
    // The prodid column is the primary key; ON UPDATE CASCADE propagates
    // the new key value to every matching Stock row automatically.
    // -----------------------------------------------------------------------

    private static void runTransaction3_RenameProduct() {
        System.out.println(">>> Transaction 3: Rename product p1 → pp1 in Product (cascades to Stock)");
        String sql = "UPDATE Product SET prodid = 'pp1' WHERE prodid = 'p1'";

        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int rows = ps.executeUpdate();
                conn.commit();
                System.out.println("    Rows updated in Product: " + rows
                        + "  (Stock.prodid updated automatically via CASCADE)");
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("    ROLLBACK – " + e.getMessage());
            }
        } catch (SQLException e) {
            System.err.println("    Connection error – " + e.getMessage());
        }

        printTables("After Transaction 3");
    }

    // -----------------------------------------------------------------------
    // Transaction 4 – Rename depot d1 → dd1 in Depot and Stock
    // ON UPDATE CASCADE propagates the new depid to every Stock row.
    // -----------------------------------------------------------------------

    private static void runTransaction4_RenameDepot() {
        System.out.println(">>> Transaction 4: Rename depot d1 → dd1 in Depot (cascades to Stock)");
        String sql = "UPDATE Depot SET depid = 'dd1' WHERE depid = 'd1'";

        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int rows = ps.executeUpdate();
                conn.commit();
                System.out.println("    Rows updated in Depot: " + rows
                        + "  (Stock.depid updated automatically via CASCADE)");
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("    ROLLBACK – " + e.getMessage());
            }
        } catch (SQLException e) {
            System.err.println("    Connection error – " + e.getMessage());
        }

        printTables("After Transaction 4");
    }

    // -----------------------------------------------------------------------
    // Transaction 5 – Add (p100, cd, 5) to Product and (p100, d2, 50) to Stock
    // Both inserts must succeed together; if either fails the whole transaction
    // is rolled back (Atomicity).
    // -----------------------------------------------------------------------

    private static void runTransaction5_AddProduct() {
        System.out.println(">>> Transaction 5: Add product (p100, cd, 5) and stock (p100, d2, 50)");
        String insertProduct = "INSERT INTO Product (prodid, pname, price) VALUES ('p100', 'cd', 5.00)";
        String insertStock   = "INSERT INTO Stock   (prodid, depid, quantity) VALUES ('p100', 'd2', 50)";

        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement ps1 = conn.prepareStatement(insertProduct);
                 PreparedStatement ps2 = conn.prepareStatement(insertStock)) {

                ps1.executeUpdate();
                ps2.executeUpdate();
                conn.commit();
                System.out.println("    Product and Stock rows inserted successfully.");
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("    ROLLBACK – " + e.getMessage());
            }
        } catch (SQLException e) {
            System.err.println("    Connection error – " + e.getMessage());
        }

        printTables("After Transaction 5");
    }

    // -----------------------------------------------------------------------
    // Transaction 6 – Add (d100, Chicago, 100) to Depot and (p1, d100, 100) to Stock
    // Both inserts are atomic; a failure on either rolls back both.
    // -----------------------------------------------------------------------

    private static void runTransaction6_AddDepot() {
        System.out.println(">>> Transaction 6: Add depot (d100, Chicago, 100) and stock (p1, d100, 100)");
        String insertDepot = "INSERT INTO Depot (depid, addr, volume) VALUES ('d100', 'Chicago', 100)";
        String insertStock = "INSERT INTO Stock (prodid, depid, quantity) VALUES ('p1', 'd100', 100)";

        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement ps1 = conn.prepareStatement(insertDepot);
                 PreparedStatement ps2 = conn.prepareStatement(insertStock)) {

                ps1.executeUpdate();
                ps2.executeUpdate();
                conn.commit();
                System.out.println("    Depot and Stock rows inserted successfully.");
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("    ROLLBACK – " + e.getMessage());
            }
        } catch (SQLException e) {
            System.err.println("    Connection error – " + e.getMessage());
        }

        printTables("After Transaction 6");
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /**
     * Clears all tables and reloads the original seed data so each
     * transaction demo starts from a known, consistent state.
     */
    private static void resetData() {
        System.out.println("    [Resetting tables to original seed data]");
        try (Connection conn = DBConnection.getConnection()) {
            try (Statement st = conn.createStatement()) {
                st.execute("DELETE FROM Stock");
                st.execute("DELETE FROM Product");
                st.execute("DELETE FROM Depot");

                // Products
                st.execute("INSERT INTO Product VALUES ('p1','tape',2.50)");
                st.execute("INSERT INTO Product VALUES ('p2','tv',250.00)");
                st.execute("INSERT INTO Product VALUES ('p3','vcr',80.00)");

                // Depots
                st.execute("INSERT INTO Depot VALUES ('d1','New York',9000)");
                st.execute("INSERT INTO Depot VALUES ('d2','Syracuse',6000)");
                st.execute("INSERT INTO Depot VALUES ('d4','New York',2000)");

                // Stock
                st.execute("INSERT INTO Stock VALUES ('p1','d1', 1000)");
                st.execute("INSERT INTO Stock VALUES ('p1','d2',-1000)");
                st.execute("INSERT INTO Stock VALUES ('p1','d4', 1200)");
                st.execute("INSERT INTO Stock VALUES ('p3','d1', 3000)");
                st.execute("INSERT INTO Stock VALUES ('p3','d4', 2000)");
                st.execute("INSERT INTO Stock VALUES ('p2','d4', 1500)");
                st.execute("INSERT INTO Stock VALUES ('p2','d1', -400)");
                st.execute("INSERT INTO Stock VALUES ('p2','d2', 2000)");

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("    Reset ROLLBACK – " + e.getMessage());
            }
        } catch (SQLException e) {
            System.err.println("    Reset connection error – " + e.getMessage());
        }
    }

    /** Prints the current contents of all three tables. */
    private static void printTables(String label) {
        System.out.println("\n--- " + label + " ---");
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(true);
            printTable(conn, "Product", "prodid, pname, price");
            printTable(conn, "Depot",   "depid, addr, volume");
            printTable(conn, "Stock",   "prodid, depid, quantity");
        } catch (SQLException e) {
            System.err.println("    printTables error – " + e.getMessage());
        }
        System.out.println();
    }

    private static void printTable(Connection conn, String table, String cols)
            throws SQLException {
        System.out.println("  " + table + ":");
        try (Statement st  = conn.createStatement();
             ResultSet rs  = st.executeQuery(
                     "SELECT " + cols + " FROM " + table + " ORDER BY 1, 2")) {
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();

            // Header
            StringBuilder header = new StringBuilder("    ");
            for (int i = 1; i <= colCount; i++) {
                header.append(String.format("%-15s", meta.getColumnName(i)));
            }
            System.out.println(header);
            System.out.println("    " + "-".repeat(15 * colCount));

            // Rows
            boolean any = false;
            while (rs.next()) {
                any = true;
                StringBuilder row = new StringBuilder("    ");
                for (int i = 1; i <= colCount; i++) {
                    row.append(String.format("%-15s", rs.getString(i)));
                }
                System.out.println(row);
            }
            if (!any) System.out.println("    (empty)");
        }
    }
}
