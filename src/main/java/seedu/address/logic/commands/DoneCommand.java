package seedu.address.logic.commands;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.Messages;
import seedu.address.commons.core.UnmodifiableObservableList;
import seedu.address.commons.exceptions.TaskNotRecurringException;
import seedu.address.commons.util.StringUtil;
import seedu.address.model.item.ReadOnlyTask;
import seedu.address.model.item.Task;
import seedu.address.model.item.UniqueTaskList.TaskNotFoundException;

//@@author A0139498J
/**
 * Archives a task identified using its last displayed index from the task manager.
 */
public class DoneCommand extends UndoableCommand {
    
    private static final Logger logger = LogsCenter.getLogger(DoneCommand.class);

    public static final String COMMAND_WORD = "done";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Archives the task identified by the index number used in the last task listing.\n"
            + "Parameters: INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1";
    
    //@author A0093960X
    public static final String TOOL_TIP = "done INDEX [ANOTHER_INDEX ...]";

    public static final String MESSAGE_DONE_ITEM_SUCCESS = "Archived Task: %1$s";
    public static final String MESSAGE_DONE_ITEMS_SUCCESS = "Archived Tasks: %1$s";
    public static final String MESSAGE_DONE_UNDO_SUCCESS = "Undid archive tasks! Tasks restored to undone list!";
    public static final String MESSAGE_DONE_UNDO_SOME_FAILURE = "The following tasks were unable to be undone: %1$s";
    
    private List<Task> readdedRecurringTasks;
    private List<Task> doneTasksUndoFail;
    
    //@@author A0139498J
    private final List<Integer> targetIndexes;
    private List<Task> doneTasks;
    private int adjustmentForRemovedTask;
    private boolean isViewingDoneList;

    
    public DoneCommand(List<Integer> targetIndexes) {
        assert targetIndexes != null;
        this.targetIndexes = targetIndexes;
    }

    @Override
    public CommandResult execute() {
        
        assert model != null;
        prepareToArchiveTasks(); 

        if (isViewingDoneList) {
            logger.warning("Invalid command, cannot do done command on done list.");
            indicateAttemptToExecuteIncorrectCommand();
            return new CommandResult(String.format(Messages.MESSAGE_DONE_LIST_RESTRICTION));
        }
        
        //TODO: Lin Han to do.
        try {
            archiveTasksFromGivenTargetIndexes();
        } catch (TaskNotRecurringException e) {
            return new CommandResult(String.format(Messages.MESSAGE_DONE_LIST_RESTRICTION));
        }
        updateHistory();
        
        if (doneTasks.isEmpty()) {
            logger.warning("No tasks archived. None of the given task indexes are valid.");
            indicateAttemptToExecuteIncorrectCommand();
            return new CommandResult(Messages.MESSAGE_INVALID_TASK_DISPLAYED_INDEX);
        }
        String toDisplay = StringUtil.removeArrayBrackets(doneTasks.toString());
        return (doneTasks.size() == 1)? 
                new CommandResult(String.format(MESSAGE_DONE_ITEM_SUCCESS, toDisplay)):
                new CommandResult(String.format(MESSAGE_DONE_ITEMS_SUCCESS, toDisplay));
    }

    /**
     * Archives the tasks denoted by the list of target indexes.
     * Invalid target indexes in the list will be ignored.
     */
    private void archiveTasksFromGivenTargetIndexes() throws TaskNotRecurringException {
        for (int targetIndex: targetIndexes) {
            UnmodifiableObservableList<ReadOnlyTask> lastShownList = model.getFilteredUndoneTaskList();
            
            boolean isTaskTargetIndexOutOfBounds = (lastShownList.size() < targetIndex - adjustmentForRemovedTask);
            if (isTaskTargetIndexOutOfBounds) {
                logger.warning("Task target index out of bounds detected. Skipping task with current target index.");
                continue;
            }
            
            int adjustedTaskIndex = targetIndex - adjustmentForRemovedTask - 1;
            Task taskToArchive = new Task(lastShownList.get(adjustedTaskIndex));
            assert isViewingDoneList == false;
            
            try {
                model.deleteUndoneTask(taskToArchive);
            } catch (TaskNotFoundException pnfe) {
                assert false : "The target task cannot be missing";
            }
            
            doneTasks.add(taskToArchive);
            
            if (taskToArchive.getRecurrenceRate().isPresent()) {
                updateRecurrenceAndReAddTask(taskToArchive);
            } else {
                adjustmentForRemovedTask++;
            }
            
            model.addDoneTask(taskToArchive);
        }
    }

    /**
     * Adds the recurring task back into the undone task list
     * with their start and end dates updated, if present.
     */
    private void updateRecurrenceAndReAddTask(Task taskToArchive) throws TaskNotRecurringException {
        logger.fine("In updateRecurrenceAndReAddTask(). Task is recurring. Updating task details.");
        Task recurringTaskToReAdd = new Task(taskToArchive);
        recurringTaskToReAdd.updateRecurringTask();
        readdedRecurringTasks.add(recurringTaskToReAdd);
        model.addTask(recurringTaskToReAdd);
    }

    /**
     * Sets the boolean attribute isViewingDoneList to reflect if the current
     * list view is done. This attribute is used to ensure that the done operation
     * will not occur while viewing the done list.
     */
    private void setCurrentViewingList() {
        logger.fine("In setCurrentViewingList(), updating boolean isViewingDoneList.");
        isViewingDoneList = model.isCurrentListDoneList();
    }
    
    /**
     * Prepares for the archiving of tasks
     * Initialises the attributes of this done command class
     * Sorts the indexes to ensure the proper offset is used
     */
    private void prepareToArchiveTasks() {
        logger.fine("In prepareToArchiveTasks(). Setting up.");
        if (!isRedoAction) {
            setCurrentViewingList();
        }
        doneTasks = new ArrayList<Task>();
        readdedRecurringTasks = new ArrayList<Task>();
        Collections.sort(targetIndexes);
        adjustmentForRemovedTask = 0;
    }

    //@@author A0093960X
    @Override
    public CommandResult undo() {
        doneTasksUndoFail = new ArrayList<Task>();
        
        for (Task doneTask : doneTasks){
            attemptToDeleteDoneTaskFromDoneList(doneTask);
        }
        
        for (Task readdedRecurTask : readdedRecurringTasks) { 
            attemptToDeleteReaddedRecurTaskFromUndoneList(readdedRecurTask);
        }
        
        readdSuccessfullyUndoneTasks();
        
        if (isSuccessfulInUndoingAllDoneTasks()) {
            return new CommandResult(MESSAGE_DONE_UNDO_SUCCESS);
        }
        else {
            return new CommandResult(String.format(MESSAGE_DONE_UNDO_SOME_FAILURE, doneTasksUndoFail));
        }
    }

    private void readdSuccessfullyUndoneTasks() {
        model.addTasks(doneTasks);
    }

    private void attemptToDeleteReaddedRecurTaskFromUndoneList(Task readdedRecurTask) {
        try {
            model.deleteUndoneTask(readdedRecurTask);
        } catch (TaskNotFoundException e) {
            logger.info("Cannot find task: " + readdedRecurTask + "; adding to list of done task failures to inform user.");
            doneTasksUndoFail.add(readdedRecurTask);
        }
    }

    private void attemptToDeleteDoneTaskFromDoneList(Task doneTask) {
        try {
            model.deleteDoneTask(doneTask);
        } catch (TaskNotFoundException e) {
            logger.info("Cannot find task: " + doneTask + "; adding to list of done task failures to inform user.");
            doneTasksUndoFail.add(doneTask);
        }
    }
    
    private boolean isSuccessfulInUndoingAllDoneTasks() {
        return doneTasksUndoFail.isEmpty();
    }

}
