package com.ci;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;


public class DbHandlerTest {
    private File tempDbFile;
    private String dbUrl;

    @BeforeEach
    void createTempDb() throws Exception {
        tempDbFile = Files.createTempFile("testdb", ".db").toFile();
        dbUrl = tempDbFile.getAbsolutePath();
    }

    @AfterEach
    void deleteTempDb() {
        if (tempDbFile.exists()) {
            tempDbFile.delete();
        }
    }
    /**
     * Contract:
     * The DbHandler shall be instantiate without problems and creating the build table shall not throw exceptions.
     * 
     * Expected behavior:
     * Creating the build table shall complete without throwing exceptions.
     */
    @Test
    void createTableShouldNotThrow() {
        DbHandler dbHandler = new DbHandler(dbUrl);
        assertNotNull(dbHandler);
        assertDoesNotThrow(() -> {
            dbHandler.createBuildTable();
        });
    }

    /**
     * Contract:
     * Inserting two entries with the same SHA shall throw a RuntimeException due to the UNIQUE constraint.
     * 
     * Expected behavior:
     * Inserting two entries with the same SHA shall throw a RuntimeException.
     */
    @Test
    void shaMustBeUnique() {
        DbHandler dbHandler = new DbHandler(dbUrl);
        assertThrows(RuntimeException.class, () -> {
            dbHandler.createBuildTable();
            dbHandler.addEntry("123", "test_branch", "error", "testing 'apostrophes' in description");
            dbHandler.addEntry("123", "test_branch", "error", "testing 'apostrophes' in description");
        });
    }

    /**
     * Contract:
     * The addEntry function shall insert a new entry without problems.
     * 
     * Expected behavior:
     * The function addEntry shall run without throwing RuntimeException.
     */
    @Test
    void insertValueShouldNotThrow() {
        DbHandler dbHandler = new DbHandler(dbUrl);
        assertDoesNotThrow(() -> {
            dbHandler.createBuildTable();
            dbHandler.addEntry("123", "test_branch", "pending");
        });
    }

    /**
     * Contract:
     * The funtion selectAllBuilds shall return all entries inside the dataset.
     * 
     * Expected behavior:
     * After inserting two different entries, selecting all entries shall return a list of length 2
     */
    @Test
    void insertTwoValuesAndGetAllShouldHaveLengthOf2() {
        DbHandler dbHandler = new DbHandler(dbUrl);
        assertDoesNotThrow(()-> {
            dbHandler.createBuildTable();
            dbHandler.addEntry("123", "test_branch", "pending", "first test entry");
            dbHandler.addEntry("456", "test_branch", "error");
        });
        List<BuildEntry> result = dbHandler.selectAllBuilds();
        assertTrue(result.size() == 2);
    }

    /**
     * Contract:
     * Function selectBySha shall return the entry with given SHA, if such entry exists.
     * 
     * Expected behavior:
     * Given a non-existent SHA, the function shall return null.
     */
    @Test
    void nonExistentShaReturnsNull() {
        DbHandler dbHandler = new DbHandler(dbUrl);
        dbHandler.createBuildTable();
        dbHandler.addEntry("123", "test_branch", "pending", "first test entry");
        BuildEntry result = dbHandler.selectBySha("456");
        assertNull(result);
    }

    /**
     * Contract:
     * Function selectBySha shall return the entry with given SHA, if such entry exists.
     * 
     * Expected behavior:
     * Given an existing SHA, the function shall return the correct entry.
     */
    @Test
    void selectByShaReturnsCorrectEntry() {
        DbHandler dbHandler = new DbHandler(dbUrl);
        dbHandler.createBuildTable();
        dbHandler.addEntry("12", "test_branch", "pending", "first test entry");
        dbHandler.addEntry("13", "test_branch", "pending", "second test entry");
        dbHandler.addEntry("14", "test_branch", "pending", "third test entry");
        BuildEntry result = dbHandler.selectBySha("13");
        assertNotNull(result);
        assertEquals("second test entry", result.buildDescription);
    }

    /**
     * Contract:
     * Function selectByBranch shall return only the entries from the specified branch.
     * 
     * Expected behavior:
     * Given a non-existing branch name, the function shall return an empty list.
     */
    @Test
    void nonExistentBranchReturnsEmptyList() {
        DbHandler dbHandler = new DbHandler(dbUrl);
        dbHandler.createBuildTable();
        dbHandler.addEntry("123", "branch1", "pending");
        dbHandler.addEntry("456", "branch1", "pending");
        dbHandler.addEntry("789", "branch2", "pending");
        List<BuildEntry> result = dbHandler.selectByBranch("branch3");
        assertEquals(0, result.size());
    }

    /**
     * Contract:
     * Function selectByBranch shall return only the entries from the specified branch.
     * 
     * Expected behavior:
     * Given an existing branch name, the function shall return ALL entries from that branch.
     */
    @Test
    void selectByBranchReturnsEntriesFromGivenBranch() {
        DbHandler dbHandler = new DbHandler(dbUrl);
        dbHandler.createBuildTable();
        dbHandler.addEntry("123", "branch1", "pending");
        dbHandler.addEntry("456", "branch3", "pending");
        dbHandler.addEntry("789", "branch2", "pending");
        dbHandler.addEntry("145", "branch1", "pending");
        List<BuildEntry> result = dbHandler.selectByBranch("branch1");
        assertEquals(2, result.size());
        for (BuildEntry entry : result) {
            assertEquals("branch1", entry.branch);
        }
    }

    /**
     * Contract:
     * Function deleteEntry shall remove the entry with the specified SHA from the dataset.
     * 
     * Expected behavior:
     * After deleting an entry, selecting by the same SHA shall return null.
     */
    @Test
    void deleteEntryRemovesEntry() {
        DbHandler dbHandler = new DbHandler(dbUrl);
        dbHandler.createBuildTable();
        dbHandler.addEntry("123", "branch1", "pending");
        dbHandler.addEntry("456", "branch1", "pending");
        dbHandler.deleteEntry("123");
        BuildEntry result = dbHandler.selectBySha("123");
        assertNull(result);
    }

    /**
     * Contract:
     * Function updateEntry shall update the entry with the specified SHA with new values.
     * 
     * Expected behavior:
     * After updating an entry, selecting by the same SHA shall return the entry with updated values
     */
    @Test
    void updateEntryChangesValues() {
        DbHandler dbHandler = new DbHandler(dbUrl);
        dbHandler.createBuildTable();
        dbHandler.addEntry("123", "branch1", "pending", "initial description");
        dbHandler.updateEntry("123", "branch2", "error", "updated description");
        BuildEntry result = dbHandler.selectBySha("123");
        assertNotNull(result);
        assertEquals("branch2", result.branch);
        assertEquals("error", result.buildResult);
        assertEquals("updated description", result.buildDescription);
    }

    /**
     * Contract:
     * The toString method of BuildEntry shall return a string containing all fields of the entry.
     * 
     * Expected behavior:
     * The string returned by toString shall contain the SHA, branch, result, and description of the entry.
     */
    @Test
    void buildEntryToStringContainsAllFields() {
        DbHandler dbHandler = new DbHandler(dbUrl);
        dbHandler.createBuildTable();
        dbHandler.addEntry("123", "branch1", "pending", "test description");
        BuildEntry entry = dbHandler.selectBySha("123");
        String entryString = entry.toString();
        assertTrue(entryString.contains("123"));
        assertTrue(entryString.contains("branch1"));
        assertTrue(entryString.contains("pending"));
        assertTrue(entryString.contains("test description"));
    }

    /**
     * Contract:
     * The addEntry function shall be able to handle date strings.
     * 
     * Expected behavior:
     * Inserting an entry with a date string shall not throw exceptions.
     */
    @Test
    public void addEntryWithDate() {
        DbHandler dbHandler = new DbHandler(dbUrl);
        dbHandler.createBuildTable();
        assertDoesNotThrow(() -> {
            dbHandler.addEntry("abc123", "main", "pending", "Initial build", "2026-02-11_12:00:00");
        });
    }

    /**
     * Contract:
     * The updateEntry function shall be able to handle date strings.
     * 
     * Expected behavior:
     * Updating an entry with a date string shall not throw exceptions.
     */
    @Test
    public void updateEntryWithDate() {
        DbHandler dbHandler = new DbHandler(dbUrl);
        dbHandler.createBuildTable();
        dbHandler.addEntry("abc123", "main", "pending", "Initial build", "2026-02-11_12:00:00");
        assertDoesNotThrow(() -> {
            dbHandler.updateEntry("abc123", "main", "success", "Build completed", "2026-02-11_12:30:00");
        });
    }

    /* #region Runtime Exceptions */

    /**
     * Contract:
     * If the database connection fails, all DbHandler methods shall throw a RuntimeException.
     * 
     * Expected behavior:
     * If the database connection fails, all DbHandler methods shall throw a RuntimeException.
     */
    @Test
    void createTableThrowsRuntime() {
        DbHandler db = brokenDb();
        assertThrows(RuntimeException.class, db::createBuildTable);
    }

    @Test
    void addEntryThrowsRuntime_fullParams() {
        DbHandler db = brokenDb();
        assertThrows(RuntimeException.class,
            () -> db.addEntry("sha","main","success","desc","date"));
    }

    @Test
    void addEntryThrowsRuntime_autoDate() {
        DbHandler db = brokenDb();
        assertThrows(RuntimeException.class,
            () -> db.addEntry("sha","main","success","desc"));
    }

    @Test
    void selectAllBuildsThrowsRuntime() {
        DbHandler db = brokenDb();
        assertThrows(RuntimeException.class, db::selectAllBuilds);
    }

    @Test
    void selectByShaThrowsRuntime() {
        DbHandler db = brokenDb();
        assertThrows(RuntimeException.class,
            () -> db.selectBySha("abc"));
    }

    @Test
    void selectByBranchThrowsRuntime() {
        DbHandler db = brokenDb();
        assertThrows(RuntimeException.class,
            () -> db.selectByBranch("main"));
    }

    @Test
    void deleteEntryThrowsRuntime() {
        DbHandler db = brokenDb();
        assertThrows(RuntimeException.class,
            () -> db.deleteEntry("abc"));
    }

    @Test
    void updateEntryThrowsRuntime_fullParams() {
        DbHandler db = brokenDb();
        assertThrows(RuntimeException.class,
            () -> db.updateEntry("sha","main","fail","desc","date"));
    }

    @Test
    void updateEntryThrowsRuntime_shortParams() {
        DbHandler db = brokenDb();
        assertThrows(RuntimeException.class,
            () -> db.updateEntry("sha","main","fail","desc"));
    }

    @Test
    void constructor_directoryCreationFails_throwsRuntimeException() throws Exception {

        // create temp file (NOT directory)
        Path file = Files.createTempFile("dbhandler-test", ".tmp");

        // attempt to create directory under file -> impossible
        Path invalid = file.resolve("subdir").resolve("db.db");

        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> new DbHandler(invalid.toString()));

        assertTrue(ex.getMessage().contains("Failed to create directories"));
    }



    private DbHandler brokenDb() {
        return new BrokenDbHandler();
    }

    /**
     * A subclass of DbHandler that simulates a broken database connection 
     * by throwing SQLException on getConnection.
     */
    static class BrokenDbHandler extends DbHandler {
        @Override
        protected Connection getConnection() throws SQLException {
            throw new SQLException("forced failure");
        }
    }
    /* #endregion */

}