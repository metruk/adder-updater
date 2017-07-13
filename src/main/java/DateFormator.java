import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public final class DateFormator {
	final static String FullDateTimeFormat = "yyyy-MM-dd HH:mm:ss";
	final static String ShortDateFormat = "dd.MM";
	final static String MonthStringFormat = "MM";
	final static String DayStringFormat = "dd";
	final static String HoursStringFormat = "HH";
	final static String MinutesStringFormat = "mm";

	static String currentDateGrinvichTime() throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat(FullDateTimeFormat);
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		return dateFormat.format(new Date());
	}

	static String currentDateDAO() throws ParseException {
		return formatDate(new Date(), FullDateTimeFormat);
	}

	static Date substractDaysFromToday(int days){
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -days);
		return cal.getTime();
	}

	static String formatDate(Date date, String stringFormat){
		SimpleDateFormat dateFormat = new SimpleDateFormat(stringFormat);
		return dateFormat.format(date);
	}
}