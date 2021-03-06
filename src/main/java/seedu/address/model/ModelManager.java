package seedu.address.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.UnmodifiableObservableList;
import seedu.address.commons.util.StringUtil;
import seedu.address.commons.events.model.TaskManagerChangedEvent;
import seedu.address.commons.events.ui.ChangeToListDoneViewEvent;
import seedu.address.commons.events.ui.ChangeToListUndoneViewEvent;
import seedu.address.commons.events.ui.SwapTaskListEvent;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.events.ui.JumpToListRequestEvent;
import seedu.address.commons.core.ComponentManager;
import seedu.address.commons.core.EventsCenter;
import seedu.address.model.item.Task;
import seedu.address.model.item.DateTime;
import seedu.address.model.item.Name;
import seedu.address.model.item.Priority;
import seedu.address.model.item.ReadOnlyTask;
import seedu.address.model.item.RecurrenceRate;
import seedu.address.model.item.UniqueTaskList.TaskNotFoundException;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Represents the in-memory model of the address book data.
 * All changes to any model should be synchronized.
 */
public class ModelManager extends ComponentManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final TaskManager taskManager;
    private FilteredList<Task> filteredUndoneTasks;
    private FilteredList<Task> filteredDoneTasks;
    private Boolean isDoneList = false;
    
    /**
     * Initializes a ModelManager with the given AddressBook
     * AddressBook and its variables should not be null
     */
    public ModelManager(TaskManager src, UserPrefs userPrefs) {
        super();
        assert src != null;
        assert userPrefs != null;

        logger.fine("Initializing with task manager: " + src + " and user prefs " + userPrefs);

        taskManager = new TaskManager(src);
        filteredUndoneTasks = new FilteredList<>(taskManager.getUndoneTasks());
        filteredDoneTasks = new FilteredList<>(taskManager.getDoneTasks());
    }

    public ModelManager() {
        this(new TaskManager(), new UserPrefs());
    }

    public ModelManager(ReadOnlyTaskManager initialData, UserPrefs userPrefs) {
        taskManager = new TaskManager(initialData);
        filteredUndoneTasks = new FilteredList<>(taskManager.getUndoneTasks());
        filteredDoneTasks = new FilteredList<>(taskManager.getDoneTasks());
        
    }
    
    @Override
    public void resetData(ReadOnlyTaskManager newData) {
        taskManager.resetDoneData(newData);
        taskManager.resetUndoneData(newData);
        indicateTaskManagerChanged();
    }
    
    @Override
    public void resetUndoneData(ReadOnlyTaskManager newData) {
        taskManager.resetUndoneData(newData);
        indicateTaskManagerChanged();
    }
    
    @Override
    public void resetDoneData(ReadOnlyTaskManager newData) {
        taskManager.resetDoneData(newData);
        indicateTaskManagerChanged();
    }
    
    @Override
    public ObservableList<Task> getTaskManagerUndoneList() {
        return taskManager.getUndoneTasks();
    }
    
    @Override
    public ObservableList<Task> getTaskManagerDoneList() {
        return taskManager.getDoneTasks();
    }
    
    @Override
    public void setTaskManagerUndoneList(ObservableList<Task> list) {
        taskManager.getUniqueUndoneTaskList().setInternalList(list);
        filteredUndoneTasks = new FilteredList<>(taskManager.getUndoneTasks());
        indicateTaskManagerChanged();
        EventsCenter.getInstance().post(new SwapTaskListEvent(false));
    }
    
    @Override
    public void setTaskManagerDoneList(ObservableList<Task> list) {
        taskManager.getUniqueDoneTaskList().setInternalList(list);
        filteredDoneTasks = new FilteredList<>(taskManager.getDoneTasks());
        indicateTaskManagerChanged();
        EventsCenter.getInstance().post(new SwapTaskListEvent(true));
    }
    
    @Override
    public void clearTaskManagerUndoneList() {
        ObservableList <Task> emptyList = FXCollections.observableArrayList();
        setTaskManagerUndoneList(emptyList);
    }

    @Override
    public void clearTaskManagerDoneList() {
        ObservableList <Task> emptyList = FXCollections.observableArrayList();
        setTaskManagerDoneList(emptyList);
    }

    @Override
    public ReadOnlyTaskManager getTaskManager() {
        return taskManager;
    }

    /** Raises an event to indicate the model has changed */
    private void indicateTaskManagerChanged() {
        raise(new TaskManagerChangedEvent(taskManager));
    }

    @Override
    public synchronized void deleteUndoneTask(ReadOnlyTask undoneTask) throws TaskNotFoundException {
        taskManager.deleteUndoneTask(undoneTask);
        indicateTaskManagerChanged();
    }
    
    //@@author A0139498J
    @Override
    public synchronized void addTask(Task task) {
        taskManager.addTask(task);
        updateFilteredListsToShowAll();
        indicateTaskManagerChanged();
        EventsCenter.getInstance().post(new JumpToListRequestEvent(getFilteredUndoneTaskList().indexOf(task)));
    }
    

    @Override
    public synchronized void addDoneTask(Task task) {
        taskManager.addDoneTask(task);
        indicateTaskManagerChanged();
    }
    
    @Override
    public synchronized void deleteDoneTask(ReadOnlyTask doneTask) throws TaskNotFoundException {
        taskManager.deleteDoneTask(doneTask);
        indicateTaskManagerChanged();
    }
    
    //@@author 
    @Override
    public void addTasks(List<Task> tasks) {
        for (Task task: tasks){
            addTask(task);
        }
    }
    
    @Override
    public void addDoneTasks(List<Task> tasks) {
        for (Task task: tasks) {
            addDoneTask(task);
        }
        
    }

    //@@author A0139498J
    @Override
    public Boolean isCurrentListDoneList() {
        return isDoneList;
    }

    @Override
    public void setCurrentListToBeDoneList() {
        EventsCenter.getInstance().post(new ChangeToListDoneViewEvent());
        isDoneList = true;
    }
  
    @Override
    public void setCurrentListToBeUndoneList() {
        EventsCenter.getInstance().post(new ChangeToListUndoneViewEvent());
        isDoneList = false;
    }
    
    //@@author A0139552B
    public synchronized void editTask(ReadOnlyTask task, Name name, Date startDate,
            Date endDate, Priority priority, RecurrenceRate recurrenceRate) {
        taskManager.editFloatingTask(task, name, startDate, endDate, priority, recurrenceRate);
        updateFilteredListsToShowAll();
        indicateTaskManagerChanged();
        jumpToCurrentEditedTask(task);
    }
    
    /*
     * Show the user the most recently edited item
     */
    private void jumpToCurrentEditedTask(ReadOnlyTask task) {
        EventsCenter.getInstance().post(new JumpToListRequestEvent(getFilteredUndoneTaskList().indexOf(task)));
    }
    //@@author
    
    //=========== Filtered Person List Accessors ===============================================================

    @Override
    public UnmodifiableObservableList<ReadOnlyTask> getFilteredUndoneTaskList() {
        return new UnmodifiableObservableList<>(filteredUndoneTasks);
    }
    
    @Override
    public UnmodifiableObservableList<ReadOnlyTask> getFilteredDoneTaskList() {
        return new UnmodifiableObservableList<>(filteredDoneTasks);
    }

    public void TaskManager() {
        filteredUndoneTasks.setPredicate(null);
        filteredDoneTasks.setPredicate(null);
    }

    @Override
    public void updateFilteredUndoneTaskListNamePred(Set<String> keywords){
        updateFilteredUndoneTaskList(new PredicateExpression(new NameQualifier(keywords)));
    }
    
    @Override
    public void updateFilteredUndoneTaskListDatePred(String keyword) throws IllegalValueException {
        updateFilteredUndoneTaskList(new PredicateExpression(new DateQualifier(keyword)));
    }

    private void updateFilteredUndoneTaskList(Expression expression) {
        filteredUndoneTasks.setPredicate(expression::satisfies);
    }
    
    @Override
    public void updateFilteredDoneTaskListNamePred(Set<String> keywords){
        updateFilteredDoneTaskList(new PredicateExpression(new NameQualifier(keywords)));
    }
    
    @Override
    public void updateFilteredDoneTaskListDatePred(String keyword) throws IllegalValueException {
        updateFilteredDoneTaskList(new PredicateExpression(new DateQualifier(keyword)));
    }

    private void updateFilteredDoneTaskList(Expression expression) {
        filteredDoneTasks.setPredicate(expression::satisfies);
    }

    //========== Inner classes/interfaces used for filtering ==================================================

    interface Expression {
        boolean satisfies(ReadOnlyTask person);
        String toString();
    }

    private class PredicateExpression implements Expression {

        private final Qualifier qualifier;

        PredicateExpression(Qualifier qualifier) {
            this.qualifier = qualifier;
        }

        @Override
        public boolean satisfies(ReadOnlyTask person) {
            return qualifier.run(person);
        }

        @Override
        public String toString() {
            return qualifier.toString();
        }
    }

    interface Qualifier {
        boolean run(ReadOnlyTask person);
        String toString();
    }

    private class NameQualifier implements Qualifier {
        private Set<String> nameKeyWords;

        NameQualifier(Set<String> nameKeyWords) {
            this.nameKeyWords = nameKeyWords;
        }

        @Override
        public boolean run(ReadOnlyTask person) {
            return nameKeyWords.stream()
                    .filter(keyword -> StringUtil.containsIgnoreCase(person.getName().getTaskName(), keyword))
                    .findAny()
                    .isPresent();
        }

        @Override
        public String toString() {
            return "name=" + String.join(", ", nameKeyWords);
        }
    }
    
    private class DateQualifier implements Qualifier {
        private Set<Date> dates;

        DateQualifier(String dateKeyWord) throws IllegalValueException {
            dates = new HashSet<Date>();
            dates.add(DateTime.convertStringToDate(dateKeyWord));
        }

        @Override
        public boolean run(ReadOnlyTask task) {
            return dates.stream()
                    .filter(currentDate -> { 

                        int date = currentDate.getDate();

                        if (!task.getStartDate().isPresent() && !task.getEndDate().isPresent()) {
                            return false;
                        }

                        if (task.getStartDate().isPresent()) {
                            if (task.getStartDate().get().getDate() == date) {
                                return true;
                            }
                        }

                        if (task.getEndDate().isPresent()) {
                            if (task.getEndDate().get().getDate() == date) {
                                return true;
                            }
                        }

                        return false;
                        

                    })
                    .findAny()
                    .isPresent();
        }

        @Override
        public String toString() {
            return "dates=" + String.join(", ", dates.toString());
        }
    }
    
    //@@author A0139498J
    @Override
    public void updateFilteredListsToShowAll() {
        filteredUndoneTasks.setPredicate(null);
        filteredDoneTasks.setPredicate(null);
    }
    
}
