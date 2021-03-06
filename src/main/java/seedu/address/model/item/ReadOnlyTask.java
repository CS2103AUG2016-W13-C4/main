package seedu.address.model.item;

import java.util.Date;
import java.util.Optional;

//@@author A0139498J
public interface ReadOnlyTask {

    Name getName();
    Priority getPriorityValue();
    Optional<Date> getStartDate();
    Optional<Date> getEndDate();
    Optional<RecurrenceRate> getRecurrenceRate();
    
    /**
     * Returns true if both have the same state. (interfaces cannot override
     * .equals) All fields must be equal.
     */
    default boolean isSameStateAs(ReadOnlyTask other) {
        return other == this // short circuit if same object
                || (other != null // this is first to avoid NPE below
                && other.getName().equals(this.getName()) // state checks here onwards
                && other.getPriorityValue().equals(this.getPriorityValue())
                && other.getStartDate().equals(this.getStartDate())
                && other.getEndDate().equals(this.getEndDate())
                && other.getRecurrenceRate().equals(this.getRecurrenceRate()));
    }
       
    //@@author A0139498J
    /**
     * Formats the task as text, showing all present details.
     */
    default String getAsText() {
        final StringBuilder builder = new StringBuilder();
        builder.append(getName().getTaskName())
                .append(", Priority: ")
                .append(getPriorityValue());
        if (getStartDate().isPresent()) {
            builder.append(", StartDate: ").append(getStartDate().get());
        }
        if (getEndDate().isPresent()) {
            builder.append(", EndDate: ").append(getEndDate().get());
        }
        if (getRecurrenceRate().isPresent()) {
            builder.append(", Repeats: ").append(getRecurrenceRate().get());
        }
        return builder.toString();
    }
    
}
