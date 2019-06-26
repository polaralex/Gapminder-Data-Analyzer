package Controller;

import Model.DatabaseHandler;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.controlsfx.control.CheckListView;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class MainCoordinator {

    DatabaseHandler databaseHandler = new DatabaseHandler();

    @FXML
    private WebView webview;
    WebEngine webengine;
    @FXML
    private TextField minDateRange;
    @FXML
    private TextField maxDateRange;

    @FXML
    public VBox checklistVbox;
    @FXML
    public VBox dropdownVbox;
    @FXML
    public VBox dropdownCountriesVbox;
    @FXML
    public Slider groupBySlider;
    @FXML
    public HBox metricsButtonsPanel;

    private boolean scatterplotMode = false;

    private ArrayList<ComboBox> comboBoxes = new ArrayList<>();
    private CheckListView<String> checkListView;

    private ComboBox scatterplotDropdown;

    private ObservableList<String> selectedCountriesMultipleChoice;

    // create the data to show in the CheckListView
    private final ObservableList<String> checklistViewStrings = FXCollections.observableArrayList();

    // Chart Type Objects:
    private TimelineChart timelineChart;
    private BarChart barChart;
    private ScatterPlot scatterPlot;

    private BaseChart selectedChart;

    public void initialize() throws MalformedURLException {
        InitializeWebview();
        createChecklistView();
        addMetricsSelector();
        addDropdownForScatterplot();

        timelineChart = new TimelineChart(this);
        barChart = new BarChart(this);
        scatterPlot = new ScatterPlot(this);

        // Show this as the default chart:
        showTrendlineChart();
    }

    private void showTwoMetricSelectors() {

        if (comboBoxes.size() < 2) {
            while (comboBoxes.size() < 2) {
                addMetricsSelector();
            }
        } else if (comboBoxes.size() > 2) {
            while (comboBoxes.size() > 2) {
                removeMetricsSelector();
            }
        }
    }

    public void addMetricsSelector() {

        ComboBox comboBox = new ComboBox();
        comboBoxes.add(comboBox);

        ObservableList observableList = comboBox.getItems();

        // Get all table names:
        String query = "SELECT table_name FROM information_schema.tables where table_schema='databasesProject';";

        ArrayList<String> tableNames = databaseHandler.RunQueryForListOfStrings(query, "TABLE_NAME");

        for (String s : tableNames) {
            observableList.add(s);
        }

        // Add the Dropdown to the layout:
        dropdownVbox.getChildren().add(comboBox);

        comboBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override public void changed(ObservableValue ov, String t, String t1) {

                System.out.println("Selected Metric: " + t1);

                checkListView.getCheckModel().clearChecks();
                populateCountriesListInCheckbox();
                populateCountriesInDropdownForScatterplot();
            }
        });
    }

    private void addDropdownForScatterplot() {

        scatterplotDropdown = new ComboBox();

        // Add the Dropdown to the layout:
        dropdownCountriesVbox.getChildren().add(scatterplotDropdown);
    }

    public void removeMetricsSelector() {

        if (dropdownVbox.getChildren().size() > 1) {
            dropdownVbox.getChildren().remove(dropdownVbox.getChildren().size()-1);
        }

        if (comboBoxes.size() > 1) {
            comboBoxes.remove(comboBoxes.size()-1);
        }

        // Refresh the available countries list:
        populateCountriesListInCheckbox();
    }

    private void createChecklistView() {

        // Create the CheckListView with the data
        checkListView = new CheckListView<>(checklistViewStrings);

        // and listen to the relevant events (e.g. when the selected indices or
        // selected items change).
        checkListView.getCheckModel().getCheckedItems().addListener((ListChangeListener<String>) c -> {

            selectedCountriesMultipleChoice = checkListView.getCheckModel().getCheckedItems();
            System.out.println(selectedCountriesMultipleChoice);
        });

        checklistVbox.getChildren().add(checkListView);
    }

    private void populateCountriesListInCheckbox() {

        ArrayList<String> countryNames = getCountriesOfMetricsList();

        checklistViewStrings.clear();
        checklistViewStrings.addAll(countryNames);
    }

    private void populateCountriesInDropdownForScatterplot() {

        ObservableList dropdownForScatterplotStrings = scatterplotDropdown.getItems();

        dropdownForScatterplotStrings.clear();
        dropdownForScatterplotStrings.addAll(getCountriesOfMetricsList());
    }

    private ArrayList<String> getCountriesOfMetricsList() {

        StringBuilder query = new StringBuilder("SELECT DISTINCT COUNTRY FROM (\n");

        for (int i = 0; i < comboBoxes.size(); i++) {

            query.append("SELECT COUNTRY FROM ").append(comboBoxes.get(i).getSelectionModel().getSelectedItem().toString()).append("\n");

            if (i != comboBoxes.size()-1) {
                query.append("UNION\n");
            }
        }

        query.append(") T");

        System.out.println(query);

        return databaseHandler.RunQueryForListOfStrings(query.toString(), "country");
    }

    private void InitializeWebview() {
        webengine = webview.getEngine();
        webengine.setJavaScriptEnabled(true);
    }

    public void runQueryFromSelection() {

        int minYear = Integer.parseInt(minDateRange.getCharacters().toString());
        int maxYear = Integer.parseInt(maxDateRange.getCharacters().toString());

        int groupBy = (int)groupBySlider.getValue();

        // Get Selected Metrics:
        ArrayList<String> selectedMetrics = new ArrayList<>();
        for (ComboBox c : comboBoxes) {
            selectedMetrics.add(c.getSelectionModel().getSelectedItem().toString());
        }

        // DEBUG:
        System.out.println("Selected Metrics:");
        for (String s : selectedMetrics) {
            System.out.println(s);
        }

        ArrayList<String> selectedCountriesNotObservable = new ArrayList<>();

        if (scatterplotMode) {

            selectedCountriesNotObservable.add(scatterplotDropdown.getSelectionModel().getSelectedItem().toString());

        } else {

            selectedCountriesNotObservable.addAll(selectedCountriesMultipleChoice);
        }

        if (!selectedCountriesNotObservable.isEmpty() && !selectedMetrics.isEmpty()) {
            ShowSelectedChart(minYear, maxYear, selectedMetrics, selectedCountriesNotObservable, groupBy);
        }
    }

    private void ShowSelectedChart(int minYear, int maxYear, ArrayList<String> selectedMetrics, ArrayList<String> selectedCountriesNotObservable, int groupBy) {

        selectedChart.ShowChart(selectedMetrics, selectedCountriesNotObservable, minYear, maxYear, groupBy);
    }

    public void showTrendlineChart() throws MalformedURLException {

        makeScatterPlotUiChanges(false);

        metricsButtonsPanel.setVisible(true);

        selectedChart = timelineChart;
        LoadPage(timelineChart.getHtmlUrl());
    }

    public void showBarChart() throws MalformedURLException {

        makeScatterPlotUiChanges(false);

        metricsButtonsPanel.setVisible(true);

        selectedChart = barChart;
        LoadPage(barChart.getHtmlUrl());
    }

    public void showScatterPlot() throws MalformedURLException {

        makeScatterPlotUiChanges(true);

        metricsButtonsPanel.setVisible(false);
        showTwoMetricSelectors();

        selectedChart = scatterPlot;
        LoadPage(scatterPlot.getHtmlUrl());
    }

    private void makeScatterPlotUiChanges(boolean state) {

        scatterplotMode = state;

        dropdownCountriesVbox.setVisible(state);
        checklistVbox.setVisible(!state);
    }

    private void LoadPage(String pathname) throws MalformedURLException {

        try {
            System.out.println(getClass().getResource(pathname).toURI().toURL().toString());
            webengine.load(getClass().getResource(pathname).toURI().toURL().toString());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

}