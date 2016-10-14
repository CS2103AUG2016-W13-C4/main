package seedu.address.logic.commands;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import seedu.address.history.ReversibleEffect;
import seedu.address.model.item.Name;
import seedu.address.model.item.Priority;
import seedu.address.model.item.ReadOnlyTask;
import seedu.address.model.item.RecurrenceRate;
import seedu.address.model.item.Task;
import seedu.address.model.item.UniqueTaskList.TaskNotFoundException;

/**
 * Selects a person identified using it's last displayed index from the address book.
 */
public class UndoCommand extends Command {

    public static final String COMMAND_WORD = "undo";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Undoes the last reversible command, reversing the effect on the task manager.\n"
            + "Example: " + COMMAND_WORD;
    
    public static final String TOOL_TIP = "undo";
    

    public UndoCommand() {
    }

    @Override
    public CommandResult execute() {
        assert history != null;
        
        // if we are at the earliest state where there is no earlier reversible command to undo, return nothing to undo
        if (history.isEarliest()){
            return new CommandResult("Nothing to undo.");
        }
        
        ReversibleEffect reversibleEffect = history.undoStep();
        String commandName = reversibleEffect.getCommandName();
        List<Task> tasksAffected = reversibleEffect.getTasksAffected();
        List<ReadOnlyTask> readOnlyTasksAffected = convertTaskListToReadOnlyTaskList(tasksAffected);
        
        assert tasksAffected.size() > 0 && readOnlyTasksAffected.size() > 0;
        Task firstAffectedTask = getFirstTaskInList(tasksAffected);
        
        switch(commandName){
            case "add":
                try {
                    model.deleteTask(firstAffectedTask);
                } catch (TaskNotFoundException e) {
                    // TODO Auto-generated catch block
                    return new CommandResult("Unable to undo last add command.");
                }
                return new CommandResult("Undid last command:\n\t" + commandName + " " + firstAffectedTask);
                
            case "delete":
                model.addTask(firstAffectedTask);
                return new CommandResult("Undid last command:\n\t" + commandName + " " + firstAffectedTask);
            
            
            case "edit":
                assert tasksAffected.size() == 2;
                
                // this is the updated task
                Task editedTaskToRevert = getSecondTaskInList(tasksAffected);
                
                // keep a deep copy for printing since the task will be changed
                Task copyOfEditedTask = new Task(editedTaskToRevert);
                
                Task prevStateOfEditedTask = firstAffectedTask;
                                
                undoEditCommand(prevStateOfEditedTask, editedTaskToRevert);
                return new CommandResult("Undid last command:\n\t" + commandName + " " + copyOfEditedTask + " reverted back to " + prevStateOfEditedTask);    
            
            case "clear":
                model.addTasks(tasksAffected);
                return new CommandResult("Undid last command:\n\t" + commandName);
            
            /*
            case "done":
                break;
            */
                
            default:
                return new CommandResult("Nothing to undo.");
        }
        
        
    }
    
    private void undoEditCommand(Task prevStateOfEditedTask, Task editedTaskToRevert) {
        // temporary method of undoing edit by editing back all fields 
        // until there is a single unified edit method
        
        Name oldTaskName = prevStateOfEditedTask.getName();
        Optional<Date> oldStartDate = prevStateOfEditedTask.getStartDate();
        Optional<Date> oldEndDate = prevStateOfEditedTask.getEndDate();
        Priority oldPriority = prevStateOfEditedTask.getPriorityValue();
        Optional<RecurrenceRate> oldReccurence = prevStateOfEditedTask.getRecurrenceRate();
        
        Task taskInListToRevert = model.getTaskManager().getUniqueTaskList().getTask(editedTaskToRevert);
      
        model.editName(taskInListToRevert, oldTaskName);
        
        model.editPriority(taskInListToRevert, oldPriority);
        
        
        // edit back the start date
        if (oldStartDate.isPresent()){
            model.editStartDate(taskInListToRevert, oldStartDate.get());
        }
        else {
            model.editStartDate(taskInListToRevert, null);
        }
        
        // edit back the end date
        if (oldEndDate.isPresent()){
            model.editEndDate(taskInListToRevert, oldEndDate.get());
        }
        else {
            model.editEndDate(taskInListToRevert, null);
        }
        
        // edit back the recurrence rate
        if (oldReccurence.isPresent()){
            model.editRecurrence(taskInListToRevert, oldReccurence.get());
        }
        else{
            model.editRecurrence(taskInListToRevert, null);
        }
        
        
    }

    private List<ReadOnlyTask> convertTaskListToReadOnlyTaskList(List<Task> tasks){
        List<ReadOnlyTask> readOnlyTaskList = new ArrayList<ReadOnlyTask>();
        for (Task task: tasks){
            readOnlyTaskList.add(task);
        }
        return readOnlyTaskList;
    }
    
    private Task getFirstTaskInList(List<Task> tasks){
        assert tasks.size() >= 1;
        return tasks.get(0);
    }
    
    private Task getSecondTaskInList(List<Task> tasks){
        assert tasks.size() >= 2;
        return tasks.get(1);
    }
    
}
