package Controller;

import Model.DataValues;

import java.util.ArrayList;

public class ScatterPlot extends BaseChart {

    private String htmlUrl = "/scatterPlot.html";

    public ScatterPlot(MainCoordinator mainCoordinator) {
        mainCoordinatorInstance = mainCoordinator;
    }

    public void ShowChart(ArrayList<String> metrics, ArrayList<String> countries, int minYear, int maxYear, int groupBy) {

        if (metrics.size() != 2) {
            return;
        }

        ArrayList<DataValues> dataValues = new ArrayList<>();

        // Special case: Use TWO metrics for a Scatter Plot:
        for (int i=0; i<2; i++) {
            dataValues.addAll(GetDataForOneMetric(metrics.get(i), countries, minYear, maxYear, groupBy));
        }

        ArrayList<ArrayList<DataValues>> countriesWithTheirData = FilterDataValuesByCountries(dataValues);

        String arrayForJavascript = createArrayForChart(countriesWithTheirData, minYear, maxYear, groupBy);

        String axisData = metrics.get(0) + ": \"" + metrics.get(1) + "\",\n";

        String javascriptToShowScatterPlot = "var chart = c3.generate({\n" +
                "    data: {\n" +
                "        xs: {\n" + axisData +
                "        },\n" +
                "        columns: [\n" + arrayForJavascript +
                "        ],\n" +
                "        type: 'scatter'\n" +
                "    },\n" +
                "    axis: {\n" +
                "        x: {\n" +
                "            label: \"" + metrics.get(1) + "\",\n" +
                "            tick: {\n" +
                "                fit: false\n" +
                "            }\n" +
                "        },\n" +
                "        y: {\n" +
                "            label: \"" + metrics.get(0) + "\"\n" +
                "        }\n" +
                "    },\n" +
                "legend: {\n" +
                "        show: false\n" +
                "    }"+
                "});";

        System.out.println(" ---- JAVASCRIPT FOR SCATTERPLOT ---- ");
        System.out.println(javascriptToShowScatterPlot);

        mainCoordinatorInstance.webengine.executeScript(javascriptToShowScatterPlot);
    }

    @Override
    protected String createArrayForChart(ArrayList<ArrayList<DataValues>> countriesWithTheirData, int minYear, int maxYear, int groupBy) {

        System.out.println("-- Create Array For Scatter Plot Chart --");

        StringBuilder array = new StringBuilder();

        for (int i=0; i < countriesWithTheirData.size(); i++) {

            int firstYearOfCurrentData = countriesWithTheirData.get(i).get(0).year;

            String metric = "";
            if (countriesWithTheirData.get(i).get(0).metric != "") {
                metric = countriesWithTheirData.get(i).get(0).metric;
            }

            System.out.println("Metric: " + metric);

            array.append("[\"").append(metric).append("\"");

            // First, fill the years without data, with empty spaces:
            for (int year = minYear; year < firstYearOfCurrentData; year += groupBy) {
                System.out.println("Year: " + year + ", Value: " + " ");
                array.append(", " + "null");
            }

            for(DataValues d : countriesWithTheirData.get(i)) {
                System.out.println("Year: " + d.year + ", Value: " + d.value);
                array.append(", ").append(d.value);
            }

            array.append("]");

            if (i < countriesWithTheirData.size()-1) {
                array.append(",\n");
            }
        }

        System.out.println("-- ScatterPlot Javascript Array --");
        System.out.println(array);

        return array.toString();
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }
}
