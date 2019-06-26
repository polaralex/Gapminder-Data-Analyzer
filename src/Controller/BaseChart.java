package Controller;

import Model.DataValues;
import java.util.ArrayList;

public abstract class BaseChart {

    protected MainCoordinator mainCoordinatorInstance;

    public abstract void ShowChart(ArrayList<String> metrics, ArrayList<String> countries, int minYear, int maxYear, int groupBy);
    public abstract String getHtmlUrl();

    protected String createArrayForChart(ArrayList<ArrayList<DataValues>> countriesWithTheirData, int minYear, int maxYear, int groupBy) {

        System.out.println("-- Create Array For Chart --");

        StringBuilder array = new StringBuilder("['x'");

        for (int year = minYear; year < maxYear; year+=groupBy) {
            array.append(", '").append(year).append("-00-00").append("'");
        }

        array.append("],\n");

        for (int i=0; i < countriesWithTheirData.size(); i++) {

            int firstYearOfCurrentData = countriesWithTheirData.get(i).get(0).year;

            String metric = "";

            System.out.println("= Metric: " + metric);

            if (countriesWithTheirData.get(i).get(0).metric != "") {
                metric = countriesWithTheirData.get(i).get(0).metric + ": ";
            }

            array.append("['").append(metric).append(countriesWithTheirData.get(i).get(0).country).append("'");

            // First, fill the years without data, with empty spaces:
            for (int year = minYear; year < firstYearOfCurrentData; year += groupBy) {
                System.out.println("Year: " + year + ", Value: " + " ");
                array.append(", " + "null");
            }

            // Then add the valid, existing data:
            for(DataValues d : countriesWithTheirData.get(i)) {
                System.out.println("Year: " + d.year + ", Value: " + d.value);
                array.append(", ").append(d.value);
            }

            array.append("]");

            if (i < countriesWithTheirData.size()-1) {
                array.append(",\n");
            }
        }

        System.out.println(array);

        return array.toString();
    }

    protected ArrayList<DataValues> GetDataForOneMetric(String metricTable, ArrayList<String> countries, int minYear, int maxYear, int groupBy) {

        // 1. Create the Query string using the countries selected and the years period:

        StringBuilder query1 = new StringBuilder("SELECT country, min(Year) as Year, avg(Value) as Value FROM databasesProject." + metricTable +
                " WHERE (Year >= " + minYear + ") AND (Year <= " + maxYear + ") AND (");

        for (int i=0; i<countries.size(); i++) {

            if (i>0) {
                query1.append(" OR ");
            }

            query1.append(" (country = \'").append(countries.get(i)).append("\') ");
        }

        query1.append(") GROUP BY country, FLOOR(Year /").append(groupBy).append(");");

        System.out.println(query1);

        return mainCoordinatorInstance.databaseHandler.RunQueryForDataValues(metricTable, query1.toString());
    }

    protected ArrayList<ArrayList<DataValues>> FilterDataValuesByCountries(ArrayList<DataValues> dataValues) {

        ArrayList<ArrayList<DataValues>> countriesAndData = new ArrayList<>();

        String currentCountry = "";
        String currentMetric = "";
        int i=0;
        int y=0;

        while (i < dataValues.size()) {

            if (!currentCountry.equals(dataValues.get(i).country) || !currentMetric.equals(dataValues.get(i).metric)) {
                currentCountry = dataValues.get(i).country;
                currentMetric = dataValues.get(i).metric;
                countriesAndData.add(new ArrayList<>());
                y = countriesAndData.size()-1;
            }

            countriesAndData.get(y).add(new DataValues(dataValues.get(i).metric, dataValues.get(i).country, dataValues.get(i).year, dataValues.get(i).value));

            i++;
        }

        // Debug Print:
        //TestArrays(countriesAndData);

        return countriesAndData;
    }
}
