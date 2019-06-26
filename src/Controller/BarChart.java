package Controller;

import Model.DataValues;

import java.util.ArrayList;

public class BarChart extends BaseChart {

    private String htmlUrl = "/barChart.html";

    public BarChart(MainCoordinator mainCoordinator) {
        mainCoordinatorInstance = mainCoordinator;
    }

    public void ShowChart(ArrayList<String> metrics, ArrayList<String> countries, int minYear, int maxYear, int groupBy) {

        ArrayList<DataValues> dataValues = new ArrayList<>();

        for (String metric : metrics) {
            dataValues.addAll(GetDataForOneMetric(metric, countries, minYear, maxYear, groupBy));
        }

        ArrayList<ArrayList<DataValues>> countriesWithTheirData = FilterDataValuesByCountries(dataValues);

        String arrayForJavascript = createArrayForChart(countriesWithTheirData, minYear, maxYear, groupBy);

        String javascriptToShowTimeline = "var chart = c3.generate({\n" +
                "    data: {\n" +
                "        x: 'x',\n" +
                "        columns: [\n" + arrayForJavascript +
                "        ],\n" +
                "        type: 'bar'\n" +
                "    },\n" +
                "    bar: {\n" +
                "        width: {\n" +
                "            ratio: 0.5 // this makes bar width 50% of length between ticks\n" +
                "        }\n" +
                "        // or\n" +
                "        //width: 100 // this makes bar width 100px\n" +
                "    },\n" +
                "    axis : {\n" +
                "        x : {\n" +
                "            type : 'timeseries',\n" +
                "            tick: {\n" +
                "                format: '%Y'\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "});";

        mainCoordinatorInstance.webengine.executeScript(javascriptToShowTimeline);
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }
}
