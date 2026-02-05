package com.ci;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class DbHandlerTest {
    /**
     * Contract:
     * The DbHandler class and createBuildTable() should execute without exceptions.
     */
    @Test
    void createTableShouldNotThrow() {
        DbHandler dbHandler = new DbHandler(true);
        assertDoesNotThrow(() -> {
            dbHandler.createBuildTable();
            dbHandler.closeConnection();
        });
    }

    /**
     * Contract:
     * The database contains entries about the build jobs of the different commits. As such, one commit can be saved only once inside the database.
     * 
     * Expected behavior:
     * Trying to add an entry with the same SHA twice shall throw SQLException.
     */
    @Test
    void shaMustBeUnique() {
        DbHandler dbHandler = new DbHandler(true);
        assertThrows(SQLException.class, () -> {
            dbHandler.createBuildTable();
            dbHandler.addEntry("123", "test_branch", "error");
            dbHandler.addEntry("123", "test_branch", "error");
        });
    }

    @Test
    void insertValueShouldNotThrow() {
        DbHandler dbHandler = new DbHandler(true);
        assertDoesNotThrow(() -> {
            dbHandler.createBuildTable();
            dbHandler.deleteEntry("123");
            dbHandler.addEntry("123", "test_branch", "pending");
            dbHandler.closeConnection();
        });
    }

    @Test
    void insertTwoValuesAndGetAllShouldHaveLengthOf2() {
        DbHandler dbHandler = new DbHandler(true);
        assertDoesNotThrow(()-> {
            dbHandler.createBuildTable();
            // delete entries just to be sure
            dbHandler.deleteEntry("123");
            dbHandler.deleteEntry("456");
            dbHandler.addEntry("123", "test_branch", "pending", "first test entry");
            dbHandler.addEntry("456", "test_branch", "error");
        });
        List<BuildEntry> result = dbHandler.selectBuild("SELECT * FROM builds");
        System.out.println(result.get(0));
        assertTrue(result.size() == 2);
    }

    @Test
    void updateDoesNotThrow() {
        DbHandler dbHandler = new DbHandler(true);
        assertDoesNotThrow(()-> {
            dbHandler.createBuildTable();
            // delete entry just to be sure
            dbHandler.deleteEntry("123");
            dbHandler.addEntry("123", "test_branch", "pending", "first test entry");
            dbHandler.updateEntry("123", "updated_branch", "success", "updated description");
            dbHandler.closeConnection();
        });
    }
}
