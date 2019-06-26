package Controller;

import Model.DataValues;

import java.util.ArrayList;

public class TimelineChart extends BaseChart {

    private String htmlUrl = "/timeline.html";

    public TimelineChart(MainCoordinator mainCoordinator) {
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
                "    bindto: '#chart',\n" +
                "    data: {\n" +
                "        x: 'x',\n" +
                "        columns: [\n" + arrayForJavascript +
                "        ]\n" +
                "    },\n" +
                "    axis: {\n" +
                "        x: {\n" +
                "            type: 'timeseries',\n" +
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
