package seedu.address.ui;

import org.apache.commons.lang.StringUtils;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import seedu.address.commons.util.FxViewUtil;

/**
 * A ui for the status bar that is displayed at the header of the application.
 */
public class ResultDisplay extends UiPart {

    private static final String NEWLINE_STRING = "\n";
    private static final String STRING_EMPTY = "";
    private static final int HEIGHT_PER_EXTRA_NEWLINE = 17;
    private static final int PREF_HEIGHT = 65;

    public static final String RESULT_DISPLAY_ID = "resultDisplay";
    private static final String STATUS_BAR_STYLE_SHEET = "result-display";
    private final StringProperty displayed = new SimpleStringProperty(STRING_EMPTY);

    private static final String FXML = "ResultDisplay.fxml";

    private TextArea resultDisplayArea;

    private AnchorPane placeHolder;

    private AnchorPane mainPane;

    public static ResultDisplay load(Stage primaryStage, AnchorPane placeHolder) {
        ResultDisplay statusBar = UiPartLoader.loadUiPart(primaryStage, placeHolder, new ResultDisplay());
        statusBar.configure();
        return statusBar;
    }

    public void configure() {
        resultDisplayArea = new TextArea();
        resultDisplayArea.setEditable(false);
        resultDisplayArea.setId(RESULT_DISPLAY_ID);
        resultDisplayArea.getStyleClass().removeAll();
        resultDisplayArea.getStyleClass().add(STATUS_BAR_STYLE_SHEET);
        resultDisplayArea.setText(STRING_EMPTY);
        resultDisplayArea.setWrapText(true);
        resultDisplayArea.textProperty().bind(displayed);
        resultDisplayArea.setPrefHeight(PREF_HEIGHT);

        // @@author A0093960X
        resultDisplayArea.textProperty().addListener(e -> {
            int newHeight = computeNewHeight();
            setNewHeight(newHeight);
        });

        // @@author
        FxViewUtil.applyAnchorBoundaryParameters(resultDisplayArea, 0.0, 0.0, 0.0, 0.0);
        mainPane.getChildren().add(resultDisplayArea);
        FxViewUtil.applyAnchorBoundaryParameters(mainPane, 0.0, 0.0, 0.0, 0.0);
        placeHolder.getChildren().add(mainPane);

    }

    // @@author A0093960X
    /**
     * Sets the height of the result display to the specified newHeight
     * 
     * @param newHeight The new height to set the result display area to
     */
    private void setNewHeight(int newHeight) {
        resultDisplayArea.setMinHeight(newHeight);
        resultDisplayArea.setPrefHeight(newHeight);
        resultDisplayArea.setMaxHeight(newHeight);
    }

    /**
     * Computes the new height of the result display area by considering the
     * number of newline characters present in the text of the result display
     * area.
     * 
     * @return The size of the new height
     */
    private int computeNewHeight() {
        return PREF_HEIGHT + computeExtraHeightFromNewLines();
    }

    /**
     * Computes the extra height of the display area due to the newline
     * characters in the text area.
     * 
     * @return The size of the extra height
     */
    private int computeExtraHeightFromNewLines() {
        return getNumberOfNewLines() * HEIGHT_PER_EXTRA_NEWLINE;
    }

    /**
     * Returns the number of newlines characters in the result display text.
     * 
     * @return The number of newline characters
     */
    private int getNumberOfNewLines() {
        String displayedText = resultDisplayArea.getText();
        return StringUtils.countMatches(displayedText, NEWLINE_STRING);
    }

    // @@author
    @Override
    public void setNode(Node node) {
        mainPane = (AnchorPane) node;
    }

    @Override
    public void setPlaceholder(AnchorPane placeholder) {
        this.placeHolder = placeholder;
    }

    @Override
    public String getFxmlPath() {
        return FXML;
    }

    public void postMessage(String message) {
        displayed.setValue(message);
    }

}
