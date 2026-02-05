package com.ci;

import java.lang.Thread.State;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DbHandler {
    private static Connection connection;
    private String dbUrl = "jdbc:sqlite:data/builds.db";

    public DbHandler() {
        try {
            connection = DriverManager.getConnection(dbUrl);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public DbHandler(Boolean test) {
        if (test) {
            dbUrl = "jdbc:sqlite:data/test.db";
        }
        try {
            connection = DriverManager.getConnection(dbUrl);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Craetes a table in the database.
     * @throws SQLException
     */
    public void createBuildTable() throws SQLException {
        Statement stm = connection.createStatement();
        String sqlCreate = "CREATE TABLE IF NOT EXISTS builds (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT,"+
            "sha TEXT UNIQUE NOT NULL,"+
            "branch TEXT NOT NULL,"+                
            "build_result TEXT NOT NULL,"+                
            "build_description TEXT,"+  
            "build_date TEXT"+
            ")";
        stm.execute(sqlCreate);
        stm.close();
    }



    /**
     * Inserts a value into the dataset.
     * 
     * @param sha sha of the commit/pull request
     * @param branch related branch
     * @param result the result of the build/test
     * @param description additional description
     * @param date the date and time
     * @throws SQLException
     */
    public void addEntry(String sha, String branch, String result, String description, String date) throws SQLException {
        Statement stm = connection.createStatement();
        String sqlInsert = "INSERT INTO builds (sha, branch, build_result, build_description, build_date) VALUES ('"+
            sha +"','"+ branch +"','"+ result +"','"+ description +"','"+ date +"')";
        stm.executeUpdate(sqlInsert);
        stm.close();
    }
    /**
     * Inserts a value into the dataset. Automatically sets the date and time to current time.
     * 
     * @param sha sha of the commit/pull request
     * @param branch related branch
     * @param result the result of the build/test
     * @param description additional description
     * @throws SQLException
     */
    public void addEntry(String sha, String branch, String result, String description) throws SQLException {
        Statement stm = connection.createStatement();
        String sqlInsert = "INSERT INTO builds (sha, branch, build_result, build_description, build_date) VALUES ('"+
            sha +"','"+ branch +"','"+ result +"','"+ description +"',CURRENT_TIMESTAMP)";
        stm.executeUpdate(sqlInsert);
        stm.close();
    }
    /**
     * Inserts a value into the dataset. Automatically sets the date and time to current time.
     * 
     * @param sha sha of the commit/pull request
     * @param branch related branch
     * @param result the result of the build/test
     * @throws SQLException
     */
    public void addEntry(String sha, String branch, String result) throws SQLException {
        addEntry(sha, branch, result, ""); 
    }

    /**
     * Applies the SQL SELECT Statement
     * @param sqlSelect
     * @return a list of entries satisfying the given statement
     */
    public List<BuildEntry> selectBuild(String sqlSelect) {
        List<BuildEntry> builds = new ArrayList<BuildEntry>();
        try (Statement stm = connection.createStatement();) {
            ResultSet rs = stm.executeQuery(sqlSelect);
            while (rs.next()) {
                builds.add(new BuildEntry(rs.getString("sha"),
                                          rs.getString("branch"),
                                          rs.getString("build_result"),
                                          rs.getString("build_description"),
                                          rs.getString("build_date")
                                        ));
            }
        } catch (SQLException e) {
            System.out.println("Error querying the database.");
        }
        return builds;
    }

    /**
     * Deletes entry based on the commit SHA.
     * @param sha
     */
    public void deleteEntry(String sha) {
        try (Statement stm = connection.createStatement();) {
            String sqlDelete = "DELETE FROM builds WHERE sha='"+sha+"'";
            stm.executeUpdate(sqlDelete);
        } catch (SQLException e) {
            System.out.println("Error deleting entry from the database.");
        }
    }

    /**
     * Updates an existing entry based on the commit SHA.
     * @param sha commit SHA
     * @param branch branch name
     * @param result build result
     * @param description build description
     * @param date build date
     * @throws SQLException
     */
    public void updateEntry(String sha, String branch, String result, String description, String date) throws SQLException {
        Statement stm = connection.createStatement();
        String sqlUpdate = "UPDATE builds SET branch = '" + branch 
                            + "', build_result = '" + result 
                            + "', build_description = '" + description
                            +"', build_date = '"+date+"' WHERE sha = '"+sha+"'";
        stm.executeUpdate(sqlUpdate);
        stm.close();
    }

    /**
     * Updates an existing entry based on the commit SHA.
     * @param sha commit SHA
     * @param branch branch name
     * @param result build result
     * @param description build description
     * @throws SQLException
     */
    public void updateEntry(String sha, String branch, String result, String description) throws SQLException {
        Statement stm = connection.createStatement();
        String sqlUpdate = "UPDATE builds SET branch = '" + branch 
                            + "', build_result = '" + result 
                            + "', build_description = '" + description
                            +"' WHERE sha = '"+sha+"'";
        stm.executeUpdate(sqlUpdate);
        stm.close();
    }

    public void closeConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
