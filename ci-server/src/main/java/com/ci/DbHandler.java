package com.ci;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * The DbHandler class is responsible for managing the database operations related to build entries.
 * It provides methods to create the builds table, add new entries, retrieve entries, update existing entries, and delete entries based on commit SHA.
 * The class uses SQLite as the underlying database and ensures that the necessary directories are created if they do not exist.
 */
public class DbHandler {
    private String dbUrl = "jdbc:sqlite:builds.db";

    public DbHandler() {
        this("data/builds.db");
    }

    /**
     * Constructor that allows specifying a custom database URL. If the specified path does not exist, it will be created.
     * @param dbUrl the URL of the database to connect to.
    */
    public DbHandler(String dbUrl) {
        Path path = Paths.get(dbUrl);
        Path parentDir = path.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            try {
                Files.createDirectories(parentDir);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create directories for database path: " + dbUrl, e);
            }
        }
        this.dbUrl = "jdbc:sqlite:"+dbUrl;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl);
    }

    /**
     * Creates a table in the database.
     */
    public void createBuildTable() {
        String sqlCreate = "CREATE TABLE IF NOT EXISTS builds (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT,"+
            "sha TEXT UNIQUE NOT NULL,"+
            "branch TEXT NOT NULL,"+                
            "build_result TEXT NOT NULL,"+                
            "build_description TEXT,"+  
            "build_date TEXT"+
            ")";
        try (Connection connection = getConnection();
            Statement stm = connection.createStatement();) {
            stm.execute(sqlCreate);
        }
        catch (SQLException e) {
            throw new RuntimeException("Failed to create table in database: " + dbUrl, e);
        }
    }



    /**
     * Inserts a value into the dataset.
     * 
     * @param sha sha of the commit/pull request
     * @param branch related branch
     * @param result the result of the build/test
     * @param description additional description
     * @param date the date and time
     * @throws RuntimeException if the database operation fails
     */
    public void addEntry(String sha, String branch, String result, String description, String date) {
        String sqlInsert = "INSERT INTO builds " +
        "(sha, branch, build_result, build_description, build_date) " +
        "VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = getConnection();
            PreparedStatement stm = connection.prepareStatement(sqlInsert);) {
            stm.setString(1, sha);
            stm.setString(2, branch);
            stm.setString(3, result);
            stm.setString(4, description);
            stm.setString(5, date);
            stm.executeUpdate();
        }
        catch (SQLException e) {
            throw new RuntimeException("Failed to insert entry into database: " + dbUrl, e);
        }
    }

    /**
     * Inserts a value into the dataset. Automatically sets the date and time to current time.
     * 
     * @param sha sha of the commit/pull request
     * @param branch related branch
     * @param result the result of the build/test
     * @param description additional description
     * @throws RuntimeException if the database operation fails
     */
    public void addEntry(String sha, String branch, String result, String description) {
        String sqlInsert = "INSERT INTO builds " +
        "(sha, branch, build_result, build_description, build_date) " +
        "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (Connection connection = getConnection();
            PreparedStatement stm = connection.prepareStatement(sqlInsert);) {
            stm.setString(1, sha);
            stm.setString(2, branch);
            stm.setString(3, result);
            stm.setString(4, description);
            stm.executeUpdate();
        }
        catch (SQLException e) {
            throw new RuntimeException("Failed to insert entry into database.", e);
        }
    }
    /**
     * Inserts a value into the dataset. Automatically sets the date and time to current time.
     * 
     * @param sha sha of the commit/pull request
     * @param branch related branch
     * @param result the result of the build/test
     */
    public void addEntry(String sha, String branch, String result) {
        addEntry(sha, branch, result, ""); 
    }


    /**
     * Selects all build entries from the database.
     * @return List of BuildEntry objects
     */
    public List<BuildEntry> selectAllBuilds() {
        String sqlSelect = "SELECT * FROM builds";
        List<BuildEntry> entries = new ArrayList<>();
        try (Connection connection = getConnection();
            PreparedStatement stm = connection.prepareStatement(sqlSelect);
            ResultSet rs = stm.executeQuery();) {
            while (rs.next()) {
                BuildEntry entry = new BuildEntry(
                    rs.getInt("id"),
                    rs.getString("sha"),
                    rs.getString("branch"),
                    rs.getString("build_result"),
                    rs.getString("build_description"),
                    rs.getString("build_date")
                );
                entries.add(entry);
            }
        }
        catch (SQLException e) {
            throw new RuntimeException("Failed to select builds from database: " + dbUrl, e);
        }
        return entries;
    }
    /**
     * Selects build entry by the commit SHA.
     * @param sha commit SHA
     * @return BuildEntry object or null if not found
     */
    public BuildEntry selectBySha(String sha) {
        String sqlSelect = "SELECT * FROM builds WHERE sha = ?";
        BuildEntry build = null;
        try (Connection connection = getConnection();
            PreparedStatement stm = connection.prepareStatement(sqlSelect);) {
            stm.setString(1, sha);
            ResultSet rs = stm.executeQuery();
            if (rs.next()) {  
                BuildEntry entry = new BuildEntry(
                    rs.getInt("id"),
                    rs.getString("sha"),
                    rs.getString("branch"),
                    rs.getString("build_result"),
                    rs.getString("build_description"),
                    rs.getString("build_date")
                );
                build = entry;
            }
        }
        catch (SQLException e) {
            throw new RuntimeException("Failed to select build with sha: " + sha, e);
        }
        return build;
    }

    /**
     * Selects all build entries corresponding to a specific branch.
     * @param branch branch name
     * @return List of BuildEntry objects
     */
    public List<BuildEntry> selectByBranch(String branch) {
        String sqlSelect = "SELECT * FROM builds WHERE branch = ?";
        List<BuildEntry> builds = new ArrayList<>();
        try (Connection connection = getConnection();
            PreparedStatement stm = connection.prepareStatement(sqlSelect);) {
            stm.setString(1, branch);
            ResultSet rs = stm.executeQuery();
            while (rs.next()) {  
                BuildEntry entry = new BuildEntry(
                    rs.getInt("id"),
                    rs.getString("sha"),
                    rs.getString("branch"),
                    rs.getString("build_result"),
                    rs.getString("build_description"),
                    rs.getString("build_date")
                );
                builds.add(entry);
            }
        }
        catch (SQLException e) {
            throw new RuntimeException("Failed to select builds with branch: " + branch, e);
        }
        return builds;
    }

    /**
     * Deletes entry based on the commit SHA.
     * @param sha commit SHA
     */
    public void deleteEntry(String sha) {
        String sqlDelete = "DELETE FROM builds WHERE sha = ?";
        try (Connection connection = getConnection();
            PreparedStatement stm = connection.prepareStatement(sqlDelete);) {
            stm.setString(1, sha);
            stm.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete entry with sha: " + sha, e);
        }
    }

    /**
     * Updates an existing entry based on the commit SHA.
     * @param sha commit SHA
     * @param branch branch name
     * @param result build result
     * @param description build description
     * @param date build date
     */
    public void updateEntry(String sha, String branch, String result, String description, String date) {
        String sqlUpdate = "UPDATE builds SET branch = ?, build_result = ?, build_description = ?, build_date = ? WHERE sha = ?";
        try (Connection connection = getConnection();
            PreparedStatement stm = connection.prepareStatement(sqlUpdate);) {
            stm.setString(1, branch);
            stm.setString(2, result);
            stm.setString(3, description);
            stm.setString(4, date);
            stm.setString(5, sha);
            stm.executeUpdate();
        }
        catch (SQLException e) {
            throw new RuntimeException("Failed to update entry with sha: " + sha, e);
        }
    }

    /**
     * Updates an existing entry based on the commit SHA.
     * @param sha commit SHA
     * @param branch branch name
     * @param result build result
     * @param description build description
     */
    public void updateEntry(String sha, String branch, String result, String description) {
        String sqlUpdate = "UPDATE builds SET branch = ?, build_result = ?, build_description = ? WHERE sha = ?";
        try (Connection connection = getConnection();
            PreparedStatement stm = connection.prepareStatement(sqlUpdate);) {
            stm.setString(1, branch);
            stm.setString(2, result);
            stm.setString(3, description);
            stm.setString(4, sha);
            stm.executeUpdate();
        }
        catch (SQLException e) {
            throw new RuntimeException("Failed to update entry with sha: " + sha, e);
        }
    }
}
