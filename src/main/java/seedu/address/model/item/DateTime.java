package seedu.address.model.item;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;

public abstract class DateTime {
    
    private static final String DATE_FORMAT_TWO = "RELATIVE_DATE";
    private static final String DATE_FORMAT_ONE = "EXPLICIT_DATE";
    private static final int BASE_INDEX = 0;
    private static final int INTEGER_CONSTANT_ONE = 1;

    private static final int NUMBER_OF_DAYS_IN_A_WEEK = 7;
    
    public static final String TIME = "EXPLICIT_TIME";
    public static final String MESSAGE_VALUE_CONSTRAINTS = "DATE_TIME format: "
            + "DATE must be in one of the formats: "
            + "\"13th Sep 2015\", \"02-08-2015\" (mm/dd/yyyy) \n"
            + "TIME must be in one of the formats: "
            + "\"7:30am\", \"19:30\"";

    /**
     * Converts given String into a valid Date object
     * 
     * @return Date object converted from given String
     */
    public static Date convertStringToStartDate(String dateString) {
        Date date;
        List<DateGroup> dates = new Parser().parse(dateString);
        
        assert dates.get(BASE_INDEX) != null && dates.get(BASE_INDEX).getDates().get(BASE_INDEX) != null;
        
        date = dates.get(BASE_INDEX).getDates().get(BASE_INDEX);
        String syntaxTree = dates.get(BASE_INDEX).getSyntaxTree().toStringTree();
            
        if (!syntaxTree.contains(TIME)) {
            date = setTimeToStartOfDay(date);
        }
        return date;
    }
    
    /**
     * Converts given String into a valid Date object
     * 
     * @return Date object converted from given String
     */
    public static Date convertStringToEndDate(String endDateString, Date startDate) {
        Date endDate;
        List<DateGroup> dates = new Parser().parse(endDateString);
        
        assert dates.get(BASE_INDEX) != null && dates.get(BASE_INDEX).getDates().get(BASE_INDEX) != null;
        
        endDate = dates.get(BASE_INDEX).getDates().get(BASE_INDEX);
        String syntaxTree = dates.get(BASE_INDEX).getSyntaxTree().toStringTree();

        if (!syntaxTree.contains(TIME)) {
            endDate = setTimeToEndOfDay(endDate);
        }
        
        if (startDate != null && !syntaxTree.contains(DATE_FORMAT_ONE) && !syntaxTree.contains(DATE_FORMAT_TWO)) {
            endDate = setDateToStartDate(endDate, startDate);
        }
        
        return endDate;
    }
    
    private static Date setDateToStartDate(Date endDate, Date startDate) {
        Calendar calendarStartDate = Calendar.getInstance();
        calendarStartDate.setTime(startDate);
        int date = calendarStartDate.get(Calendar.DATE);
        int month = calendarStartDate.get(Calendar.MONTH);
        int year = calendarStartDate.get(Calendar.YEAR);
        
        Calendar calendarEndDate = Calendar.getInstance();
        calendarEndDate.setTime(endDate);
        calendarEndDate.set(Calendar.DATE, date);
        calendarEndDate.set(Calendar.MONTH, month);
        calendarEndDate.set(Calendar.YEAR, year);
        
        Date updatedDate = calendarEndDate.getTime();
        return updatedDate;
    }

    //TODO: ??
    /**
     * Verifies if given String conforms to what was specified in User Guide e.g 
     * "5pm tomorrow", "02/10/2016", "13 Sep"
     */
    public static boolean isValidDate(String dateString) {
        List<DateGroup> dates = new Parser().parse(dateString.trim());
        try {
            dates.get(BASE_INDEX).getDates().get(BASE_INDEX);
            int parsePosition = dates.get(BASE_INDEX).getPosition();
            String matchingValue = dates.get(BASE_INDEX).getText();
            
            if (parsePosition > INTEGER_CONSTANT_ONE || !matchingValue.equals(dateString)) {
                return false;
            }
        } catch (IndexOutOfBoundsException ioobe) {
            return false;
        }
        return true;
    }
    
    /**
     * Assigns start date to a specified weekday
     */
    public static Date assignStartDateToSpecifiedWeekday(String dateString) {
        assert dateString.toLowerCase().equals("monday") || dateString.toLowerCase().equals("tuesday") ||
        dateString.toLowerCase().equals("wednesday") || dateString.toLowerCase().equals("thursday") || 
        dateString.toLowerCase().equals("friday") || dateString.toLowerCase().equals("saturday") || 
        dateString.toLowerCase().equals("sunday");
        
        Date date;
        List<DateGroup> dates = new Parser().parse(dateString);
        
        date = dates.get(BASE_INDEX).getDates().get(BASE_INDEX);
        date = setTimeToStartOfDay(date);
        
        return date;
    }
    


    /**
     * Sets time of Date object to start of the day i.e "00:00:00"
     */
    private static Date setTimeToStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        
        Date updatedDate = calendar.getTime();
        return updatedDate;
    }
    
    /**
     * Sets time of Date object to end of the day i.e "23:59:59"
     */
    private static Date setTimeToEndOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        
        Date updatedDate = calendar.getTime();
        return updatedDate;
    }
    
    //TODO: Comments
    public static void updateDateByRecurrenceRate(Calendar calendar, RecurrenceRate recurrenceRate) {
        switch (recurrenceRate.timePeriod) {
        case HOUR:
            calendar.add(Calendar.HOUR_OF_DAY, recurrenceRate.rate);
            break;
        case DAY:
            calendar.add(Calendar.DAY_OF_YEAR, recurrenceRate.rate);
            break;
        case WEEK:
            calendar.add(Calendar.WEEK_OF_YEAR, recurrenceRate.rate);
            break;
        case MONTH:
            calendar.add(Calendar.MONTH, recurrenceRate.rate);
            break;
        case YEAR:
            calendar.add(Calendar.YEAR, recurrenceRate.rate);
            break;
        case MONDAY:
            DateTime.updateDateToNextMonday(calendar, recurrenceRate.rate);
            break;
        case TUESDAY:
            DateTime.updateDateToNextTuesday(calendar, recurrenceRate.rate);
            break;
        case WEDNESDAY:
            DateTime.updateDateToNextWednesday(calendar, recurrenceRate.rate);
            break;
        case THURSDAY:
            DateTime.updateDateToNextThursday(calendar, recurrenceRate.rate);
            break;
        case FRIDAY:
            DateTime.updateDateToNextFriday(calendar, recurrenceRate.rate);
            break;
        case SATURDAY:
            DateTime.updateDateToNextSaturday(calendar, recurrenceRate.rate);
            break;
        case SUNDAY:
            DateTime.updateDateToNextSunday(calendar, recurrenceRate.rate);
            break;
        }
    }
    
    //TODO: Comments
    private static void updateDateToNextMonday(Calendar calendar, int rate) {
        updateDateByRate(calendar, rate);

        calendar.add(Calendar.DATE, INTEGER_CONSTANT_ONE);
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            calendar.add(Calendar.DATE, INTEGER_CONSTANT_ONE);
        }
    }

    private static void updateDateToNextTuesday(Calendar calendar, int rate) {
        updateDateByRate(calendar, rate);
        
        calendar.add(Calendar.DATE, INTEGER_CONSTANT_ONE);
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.TUESDAY) {
            calendar.add(Calendar.DATE, INTEGER_CONSTANT_ONE);
        }
    }

    private static void updateDateToNextWednesday(Calendar calendar, int rate) {
        updateDateByRate(calendar, rate);
        
        calendar.add(Calendar.DATE, INTEGER_CONSTANT_ONE);
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.WEDNESDAY) {
            calendar.add(Calendar.DATE, INTEGER_CONSTANT_ONE);
        }
    }

    private static void updateDateToNextThursday(Calendar calendar, int rate) {
        updateDateByRate(calendar, rate);
        
        calendar.add(Calendar.DATE, INTEGER_CONSTANT_ONE);
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.THURSDAY) {
            calendar.add(Calendar.DATE, INTEGER_CONSTANT_ONE);
        }
    }

    private static void updateDateToNextFriday(Calendar calendar, int rate) {
        updateDateByRate(calendar, rate);
        
        calendar.add(Calendar.DATE, INTEGER_CONSTANT_ONE);
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY) {
            calendar.add(Calendar.DATE, INTEGER_CONSTANT_ONE);
        }
    }

    private static void updateDateToNextSaturday(Calendar calendar, int rate) {
        updateDateByRate(calendar, rate);
        
        calendar.add(Calendar.DATE, INTEGER_CONSTANT_ONE);
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY) {
            calendar.add(Calendar.DATE, INTEGER_CONSTANT_ONE);
        }
    }

    private static void updateDateToNextSunday(Calendar calendar, int rate) {
        updateDateByRate(calendar, rate);
        
        calendar.add(Calendar.DATE, INTEGER_CONSTANT_ONE);
        while (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            calendar.add(Calendar.DATE, INTEGER_CONSTANT_ONE);
        }
    }
    
    private static void updateDateByRate(Calendar calendar, int rate) {
        if (rate > INTEGER_CONSTANT_ONE) {
            calendar.add(Calendar.DATE, (rate - INTEGER_CONSTANT_ONE) * NUMBER_OF_DAYS_IN_A_WEEK);
        }
    }
}