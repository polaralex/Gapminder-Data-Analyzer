package Model;

import java.sql.*;
import java.util.ArrayList;

public class DatabaseHandler {

    // Change these according to your database's credentials:
    private String url = "jdbc:mysql://localhost:3306/databasesProject?useLegacyDatetimeCode=false&serverTimezone=UTC";
    private String username = "databasesUser";
    private String password = "databasesUserPassword";

    private ResultSet resultSet = null;

    public ArrayList<DataValues> RunQueryForDataValues(String metric, String queryString) {

        System.out.println("Connecting database...");

        try (Connection connection = DriverManager.getConnection(url, username, password)) {

            Statement statement = connection.createStatement();
            resultSet = statement.executeQuery(queryString);

            return resultToArrayOfDataValues(metric, resultSet);

        } catch (SQLException e) {
            throw new IllegalStateException("Cannot connect the database!", e);
        }
    }

    private ArrayList<DataValues> resultToArrayOfDataValues(String metric, ResultSet resultSet) throws SQLException {

        ArrayList<DataValues> array = new ArrayList<>();

        while (resultSet.next()) {

            String country = resultSet.getString("country");
            int year = resultSet.getInt("Year");
            float value = resultSet.getFloat("Value");

            array.add(new DataValues(metric, country, year, value));
        }

        return array;
    }

    public ArrayList<String> RunQueryForListOfStrings(String queryString, String columnLabel) {

        System.out.println("Connecting database...");

        try (Connection connection = DriverManager.getConnection(url, username, password)) {

            Statement statement = connection.createStatement();
            resultSet = statement.executeQuery(queryString);

            return resultToArrayOfStrings(resultSet, columnLabel);

        } catch (SQLException e) {
            throw new IllegalStateException("Cannot connect the database!", e);
        }
    }

    private ArrayList<String> resultToArrayOfStrings(ResultSet resultSet, String columnLabel) throws SQLException {

        ResultSetMetaData rsmd = resultSet.getMetaData();
        ArrayList<String> stringArray = new ArrayList<>();

        while (resultSet.next()) {

            String string = resultSet.getString(columnLabel);
            stringArray.add(string);
        }

        return stringArray;
    }
}

