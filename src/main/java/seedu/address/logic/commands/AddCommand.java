package seedu.address.logic.commands;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.item.FloatingTask;
import seedu.address.model.item.Name;
import seedu.address.model.item.Priority;
import seedu.address.model.item.UniqueFloatingTaskList.DuplicateFloatingTaskException;

/**
 * Adds a person to the address book.
 */
public class AddCommand extends Command {

    public static final String COMMAND_WORD = "add";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds an item to To-Do List. "
            + "Parameters: NAME [rank PRIORITY_VALUE]\n"
            + "Example: " + COMMAND_WORD
            + " read Harry Potter and the Akshay rank 1";
    
    public static final String TOOL_TIP = "add NAME [rank PRIORITY]";
    
    public static final String MESSAGE_DUPLICATE_FLOATING_TASK = "This task already exists in the task manager";

    public static final String MESSAGE_SUCCESS = "New item added: %1$s";

    private final FloatingTask toAdd;

    /**
     * Convenience constructor using raw values.
     *
     * @throws IllegalValueException if any of the raw values are invalid
     */
    public AddCommand(String name) throws IllegalValueException {
        this.toAdd = new FloatingTask(new Name(name));
    }
    
    public AddCommand(String name, String priorityValue) throws IllegalValueException {
       this.toAdd = new FloatingTask(new Name(name), new Priority(priorityValue));
    }

    @Override
    public CommandResult execute() {
        assert model != null;
        try {
            model.addFloatingTask(toAdd);
        } catch (DuplicateFloatingTaskException e) {
            return new CommandResult(MESSAGE_DUPLICATE_FLOATING_TASK);
        }
        return new CommandResult(String.format(MESSAGE_SUCCESS, toAdd));
    }

}
