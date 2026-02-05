package com.ci;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class DbHandlerTest {
    /**
     * Contract:
     * The DbHandler shall be instantiate without problems and creating the build table shall not throw exceptions.
     * 
     * Expected behavior:
     * Creating the build table shall complete without throwing exceptions.
     */
    @Test
    void createTableShouldNotThrow() {
        DbHandler dbHandler = new DbHandler(true);
        assertNotNull(dbHandler);
        assertDoesNotThrow(() -> {
            dbHandler.createBuildTable();
            dbHandler.closeConnection();
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
        DbHandler dbHandler = new DbHandler(true);
        assertThrows(RuntimeException.class, () -> {
            dbHandler.createBuildTable();
            dbHandler.addEntry("123", "test_branch", "error", "testing 'apostrophes' in description");
            dbHandler.addEntry("123", "test_branch", "error", "testing 'apostrophes' in description");
            dbHandler.closeConnection();
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
        DbHandler dbHandler = new DbHandler(true);
        assertDoesNotThrow(() -> {
            dbHandler.createBuildTable();
            dbHandler.addEntry("123", "test_branch", "pending");
            dbHandler.closeConnection();
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
        DbHandler dbHandler = new DbHandler(true);
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
        DbHandler dbHandler = new DbHandler(true);
        dbHandler.createBuildTable();
        dbHandler.addEntry("123", "test_branch", "pending", "first test entry");
        BuildEntry result = dbHandler.selectBySha("456");
        assertNull(result);
        dbHandler.closeConnection();
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
        DbHandler dbHandler = new DbHandler(true);
        dbHandler.createBuildTable();
        dbHandler.addEntry("12", "test_branch", "pending", "first test entry");
        dbHandler.addEntry("13", "test_branch", "pending", "second test entry");
        dbHandler.addEntry("14", "test_branch", "pending", "third test entry");
        BuildEntry result = dbHandler.selectBySha("13");
        assertNotNull(result);
        assertEquals("second test entry", result.buildDescription);
        dbHandler.closeConnection();
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
        DbHandler dbHandler = new DbHandler(true);
        dbHandler.createBuildTable();
        dbHandler.addEntry("123", "branch1", "pending");
        dbHandler.addEntry("456", "branch1", "pending");
        dbHandler.addEntry("789", "branch2", "pending");
        List<BuildEntry> result = dbHandler.selectByBranch("branch3");
        assertEquals(0, result.size());
        dbHandler.closeConnection();
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
        DbHandler dbHandler = new DbHandler(true);
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
        dbHandler.closeConnection();
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
        DbHandler dbHandler = new DbHandler(true);
        dbHandler.createBuildTable();
        dbHandler.addEntry("123", "branch1", "pending");
        dbHandler.addEntry("456", "branch1", "pending");
        dbHandler.deleteEntry("123");
        BuildEntry result = dbHandler.selectBySha("123");
        assertNull(result);
        dbHandler.closeConnection();
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
        DbHandler dbHandler = new DbHandler(true);
        dbHandler.createBuildTable();
        dbHandler.addEntry("123", "branch1", "pending", "initial description");
        dbHandler.updateEntry("123", "branch2", "error", "updated description");
        BuildEntry result = dbHandler.selectBySha("123");
        assertNotNull(result);
        assertEquals("branch2", result.branch);
        assertEquals("error", result.buildResult);
        assertEquals("updated description", result.buildDescription);
        dbHandler.closeConnection();
    }
}