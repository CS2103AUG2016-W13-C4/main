package seedu.address.model;

import javafx.collections.ObservableList;
import seedu.address.model.item.FloatingTask;
import seedu.address.model.item.ReadOnlyFloatingTask;
import seedu.address.model.person.Person;
import seedu.address.model.person.ReadOnlyPerson;
import seedu.address.model.person.UniqueFloatingTaskList;
import seedu.address.model.tag.Tag;
import seedu.address.model.tag.UniqueTagList;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Wraps all data at the address-book level
 * Duplicates are not allowed (by .equals comparison)
 */
public class AddressBook implements ReadOnlyAddressBook {

    private final UniqueFloatingTaskList floatingTasks;
    private final UniqueTagList tags;

    {
        floatingTasks = new UniqueFloatingTaskList();
        tags = new UniqueTagList();
    }

    public AddressBook() {}

    /**
     * FloatingTasks and Tags are copied into this addressbook
     */
    public AddressBook(ReadOnlyAddressBook toBeCopied) {
        this(toBeCopied.getUniqueFloatingTaskList(), toBeCopied.getUniqueTagList());
    }

    /**
     * Persons and Tags are copied into this addressbook
     */
    public AddressBook(UniqueFloatingTaskList persons, UniqueTagList tags) {
        resetData(persons.getInternalList(), tags.getInternalList());
    }

    public static ReadOnlyAddressBook getEmptyAddressBook() {
        return new AddressBook();
    }

//// list overwrite operations

    public ObservableList<FloatingTask> getFloatingTasks() {
        return floatingTasks.getInternalList();
    }

    public void setFloatingTasks(List<FloatingTask> floatingTasks) {
        this.floatingTasks.getInternalList().setAll(floatingTasks);
    }

    public void setTags(Collection<Tag> tags) {
        this.tags.getInternalList().setAll(tags);
    }

    public void resetData(Collection<? extends ReadOnlyFloatingTask> newFloatingTasks, Collection<Tag> newTags) {
        //setFloatingTasks(newFloatingTasks.stream().map(FloatingTask::new).collect(Collectors.toList()));
        setTags(newTags);
    }

    public void resetData(ReadOnlyAddressBook newData) {
        resetData(newData.getFloatingTaskList(), newData.getTagList());
    }

//// person-level operations

    /**
     * Adds a person to the address book.
     * Also checks the new person's tags and updates {@link #tags} with any new tags found,
     * and updates the Tag objects in the person to point to those in {@link #tags}.
     *
     * @throws UniquePersonList.DuplicatePersonException if an equivalent person already exists.
     */
    public void addFloatingTask(FloatingTask f) throws UniqueFloatingTaskList.DuplicateFloatingTaskException {
        //syncTagsWithMasterList(f);
        floatingTasks.add(f);
    }

    /**
     * Ensures that every tag in this person:
     *  - exists in the master list {@link #tags}
     *  - points to a Tag object in the master list
     */
    private void syncTagsWithMasterList(Person person) {
        final UniqueTagList personTags = person.getTags();
        tags.mergeFrom(personTags);

        // Create map with values = tag object references in the master list
        final Map<Tag, Tag> masterTagObjects = new HashMap<>();
        for (Tag tag : tags) {
            masterTagObjects.put(tag, tag);
        }

        // Rebuild the list of person tags using references from the master list
        final Set<Tag> commonTagReferences = new HashSet<>();
        for (Tag tag : personTags) {
            commonTagReferences.add(masterTagObjects.get(tag));
        }
        person.setTags(new UniqueTagList(commonTagReferences));
    }

    public boolean removeFloatingTask(ReadOnlyFloatingTask key) throws UniqueFloatingTaskList.FloatingTaskNotFoundException {
        if (floatingTasks.remove(key)) {
            return true;
        } else {
            throw new UniqueFloatingTaskList.FloatingTaskNotFoundException();
        }
    }

//// tag-level operations

    public void addTag(Tag t) throws UniqueTagList.DuplicateTagException {
        tags.add(t);
    }

//// util methods

    @Override
    public String toString() {
        return floatingTasks.getInternalList().size() + " floating tasks, " + tags.getInternalList().size() +  " tags";
        // TODO: refine later
    }

    @Override
    public List<ReadOnlyFloatingTask> getFloatingTaskList() {
        return Collections.unmodifiableList(floatingTasks.getInternalList());
    }

    @Override
    public List<Tag> getTagList() {
        return Collections.unmodifiableList(tags.getInternalList());
    }

    @Override
    public UniqueFloatingTaskList getUniqueFloatingTaskList() {
        return this.floatingTasks;
    }

    @Override
    public UniqueTagList getUniqueTagList() {
        return this.tags;
    }


    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof AddressBook // instanceof handles nulls
                && this.floatingTasks.equals(((AddressBook) other).floatingTasks)
                && this.tags.equals(((AddressBook) other).tags));
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(floatingTasks, tags);
    }
}
