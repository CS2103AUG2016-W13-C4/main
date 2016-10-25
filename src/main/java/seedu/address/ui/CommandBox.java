package seedu.address.ui;

import com.google.common.eventbus.Subscribe;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import seedu.address.commons.events.ui.IncorrectCommandAttemptedEvent;
import seedu.address.logic.Logic;
import seedu.address.logic.commands.*;
import seedu.address.commons.util.FxViewUtil;
import seedu.address.history.InputHistory;
import seedu.address.commons.core.LogsCenter;

import java.util.logging.Logger;

public class CommandBox extends UiPart {
    private final Logger logger = LogsCenter.getLogger(CommandBox.class);
    private static final String FXML = "CommandBox.fxml";

    private AnchorPane placeHolderPane;
    private AnchorPane commandPane;
    private ResultDisplay resultDisplay;
    String previousCommandTest;

    private Logic logic;
    private InputHistory history;
    
    private static final String BACKSPACE_UNICODE = "\u0008";
    private static final String SPACE_UNICODE = "\u0020";
    private static final String CARRIAGE_RETURN = "\r";

    @FXML
    private TextField commandTextField;
    private CommandResult mostRecentResult;

    public static CommandBox load(Stage primaryStage, AnchorPane commandBoxPlaceholder,
            ResultDisplay resultDisplay, Logic logic, InputHistory history) {
        CommandBox commandBox = UiPartLoader.loadUiPart(primaryStage, commandBoxPlaceholder, new CommandBox());
        commandBox.configure(resultDisplay, logic, history);
        commandBox.addToPlaceholder();
        return commandBox;
    }

    public void configure(ResultDisplay resultDisplay, Logic logic, InputHistory history) {
        this.resultDisplay = resultDisplay;
        this.logic = logic;
        this.history = history;
        registerAsAnEventHandler(this);
    }

    private void addToPlaceholder() {
        SplitPane.setResizableWithParent(placeHolderPane, false);
        placeHolderPane.getChildren().add(commandTextField);
        FxViewUtil.applyAnchorBoundaryParameters(commandPane, 0.0, 0.0, 0.0, 0.0);
        FxViewUtil.applyAnchorBoundaryParameters(commandTextField, 0.0, 0.0, 0.0, 0.0);
    }

    @Override
    public void setNode(Node node) {
        commandPane = (AnchorPane) node;
    }

    @Override
    public String getFxmlPath() {
        return FXML;
    }

    @Override
    public void setPlaceholder(AnchorPane pane) {
        this.placeHolderPane = pane;
    }
    
    /**
     * Handles the event where the user presses a key in the command box.
     */
    @FXML
    private void handleKeyInput(KeyEvent event) {       
        String keyInputAsString = event.getCharacter();
        
        boolean keyIsEnter = keyInputAsString.equals(CARRIAGE_RETURN);
        
        if (keyIsEnter) {
            return;
        }
        
        // reset incorrect command style on command box
        setStyleToIndicateCorrectCommand();
        
        String userInput = getUserInputAfterKeyPressed(keyInputAsString);
        updateTooltipForUser(userInput);        

    }
    
    @FXML
    private void handleKeyPressed(KeyEvent event) {
        KeyCode key = getKeyCodeFromEvent(event);
        
        boolean isNavigatingInputHistory = checkIfNavigatingInputHistory(key);
        boolean notNavigatingInputHistory = !isNavigatingInputHistory;
        
        if (notNavigatingInputHistory) {
            return;
        }
        
        setStyleToIndicateCorrectCommand();
        handleInputHistoryNavigation(key);
        /*
        String userInput = getUserInputAfterKeyPressed(key);
        */
        String userInput = commandTextField.getText();
        updateTooltipForUser(userInput);    
    }

    /**
     * Get the complete user input taking into account the current key pressed as key pressed
     * event is triggered before the command box text is updated.
     * key is either a backspace, space, letter or digit key.
     * 
     * @param keyAsString the key that was pressed as string
     * @return the full user input taking into account the key pressed
     */
    private String getUserInputAfterKeyPressed(String keyAsString) {
        String userInput = commandTextField.getText();
                
        switch (keyAsString) {
            case BACKSPACE_UNICODE:
                return applyBackspaceOnInputEnd(userInput);
            case SPACE_UNICODE:
                return applySpaceOnInputEnd(userInput);
            default:
                // is a normal letter/digit
                return applyKeyOnInputEnd(userInput, keyAsString);
        }
        
    }

    private String applyKeyOnInputEnd(String userInput, String keyString) {
        return userInput + keyString;
    }

    private KeyCode getKeyCodeFromEvent(KeyEvent event) {
        return event.getCode();
    }

    /**
     * Returns if the key press corresponds to an up or down arrow key used to navigate input history.
     * @param key the key to check
     * @return boolean representing if the key is an up or down arrow key
     */
    private boolean checkIfNavigatingInputHistory(KeyCode key) {
        return key == KeyCode.UP || key == KeyCode.DOWN;
    }

    /**
     * Updates the tooltip on the GUI for the user to see.
     */
    private void updateTooltipForUser(String userInput) {
        String toDisplay = logic.generateToolTip(userInput);
        resultDisplay.postMessage(toDisplay);
    }
    
    private String applySpaceOnInputEnd(String expectedUserInput) {
        return expectedUserInput + " ";
    }

    private String applyBackspaceOnInputEnd(String userInput) {
        if (userInput.isEmpty()) {
            return "";
        }
        else {
            return userInput.substring(0, userInput.length()-1);
        }
    }

    
    private void handleInputHistoryNavigation(KeyCode keyCode) {
        boolean wantPrev = checkIfWantPrevInput(keyCode);
        boolean wantNext = !wantPrev;
        
        // if attempt to get next command while at latest command input or prev while at earliest, return
        if ((history.isLatestInput() && wantNext) || (history.isEarliestInput() && wantPrev)) {
            return;
        }
                
        // handle differently depending on up arrow
        if (wantPrev){
            // store the current input into the next first
            if (history.isLatestInput()) {
                history.pushNextInput(commandTextField.getText());
            }
                
            else {
                history.pushNextInput(history.getStoredCurrentShownInput());
            }
                
            // get a previous command input and replace current input
            commandTextField.setText(history.popPrevInput());
        }
        
        // or down arrow
        else {
            // store the current input into the prev first
            history.pushPrevInput(history.getStoredCurrentShownInput());
            
            // get a next command input and replace current input
            commandTextField.setText(history.popNextInput());
        }
        
        String currentInputShown = commandTextField.getText();
        // positions the caret at the end of the string for easy edit
        commandTextField.positionCaret(currentInputShown.length());
    }

    private boolean checkIfWantPrevInput(KeyCode keyCode) {
        return keyCode == KeyCode.UP;
    }

    @FXML
    private void handleCommandInputEntered() {
        //Take a copy of the command text
        previousCommandTest = commandTextField.getText();
        
        // first push back all 'next' commands into 'prev' command       
        // immediately add it to the history of command inputs
        history.updateInputHistory(previousCommandTest); 

        /* We assume the command is correct. If it is incorrect, the command box will be changed accordingly
         * in the event handling code {@link #handleIncorrectCommandAttempted}
         */
        setStyleToIndicateCorrectCommand();
        mostRecentResult = logic.execute(previousCommandTest);
        commandTextField.setText("");
        resultDisplay.postMessage(mostRecentResult.feedbackToUser);
        logger.info("Result: " + mostRecentResult.feedbackToUser);
    }


    /**
     * Sets the command box style to indicate a correct command.
     */
    private void setStyleToIndicateCorrectCommand() {
        commandTextField.getStyleClass().remove("error");
    }

    @Subscribe
    private void handleIncorrectCommandAttempted(IncorrectCommandAttemptedEvent event){
        logger.info(LogsCenter.getEventHandlingLogMessage(event,"Invalid command: " + previousCommandTest));
        setStyleToIndicateIncorrectCommand();
        restoreCommandText();
    }

    /**
     * Restores the command box text to the previously entered command
     */
    private void restoreCommandText() {
        commandTextField.setText(previousCommandTest);
    }

    /**
     * Sets the command box style to indicate an error
     */
    private void setStyleToIndicateIncorrectCommand() {
        commandTextField.getStyleClass().add("error");
    }

}
