package com.udg.monitorgea;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DatePointer
{
    static int BYDAY = 0;
    static int BYWEEK = 1;
    static int BYMONTH = 2;

    private Date firstDayOfCurrentPeriod;
    private Date lastDayOfCurrentPeriod;
    private Date firstPurchaseDate;

    private Calendar calendar;

    private int dateRangeType;

    public DatePointer()
    {
        dateRangeType = BYDAY;
        calendar = Calendar.getInstance();
        setFirstDayOfCurrentPeriod();
        setLastDayOfCurrentPeriod();
    }

    public void setFirstDayOfCurrentPeriod()
    {
        Date dateToReturn;

        if (dateRangeType == BYDAY)
        {
            //Obtener primera hora del día
            Calendar date = calendar;
            date.set(Calendar.HOUR_OF_DAY, 0);
            date.set(Calendar.MINUTE, 0);
            date.set(Calendar.SECOND, 0);
            dateToReturn = date.getTime();
        }
        else if (dateRangeType == BYWEEK)
        {
            if (calendar.get(Calendar.DAY_OF_WEEK) == 2) //Si es Lunes...
            {
                dateToReturn = calendar.getTime();
            }
            else
            {
                Calendar date = calendar;

                do
                {
                    date.add(Calendar.DAY_OF_WEEK, -1);
                } while (date.get(Calendar.DAY_OF_WEEK) != 2);

                dateToReturn = date.getTime();
            }
        }
        else
        {
            //Obtener primer día del mes
            Calendar date = Calendar.getInstance();
            date.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
            date.set(Calendar.DAY_OF_MONTH, 1);

            dateToReturn = date.getTime();
        }

        firstDayOfCurrentPeriod = dateToReturn;
    }

    public void setLastDayOfCurrentPeriod()
    {
        if (dateRangeType == BYDAY)
        {
            Calendar date = Calendar.getInstance();
            //Definir primera hora del día
            date.setTime(calendar.getTime());
            //Agregar un día
            date.add(Calendar.DAY_OF_MONTH, 1);
            //Regresar última hora del día
            lastDayOfCurrentPeriod = date.getTime();
        }
        else if (dateRangeType == BYWEEK)
        {
            Calendar date;

            date = calendar;
            date.add(Calendar.DAY_OF_WEEK, 6);

            lastDayOfCurrentPeriod = date.getTime();
        }
        else
        {
            //Obtener ultimo día del mes
            Calendar date = Calendar.getInstance();
            date.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
            date.set(Calendar.DAY_OF_MONTH, date.getActualMaximum(Calendar.DAY_OF_MONTH));

            lastDayOfCurrentPeriod = date.getTime();
        }
    }

    public Date getFirstDateOfCurrentPeriod()
    {
        return firstDayOfCurrentPeriod;
    }

    public Date getLastDayOfCurrentPeriod()
    {
        return lastDayOfCurrentPeriod;
    }

    public long getFirstDateOfCurrentPeriodEpoch()
    {
        return firstDayOfCurrentPeriod.getTime() / 1000;
    }

    public long getLastDateOfCurrentPeriodEpoch()
    {
        return lastDayOfCurrentPeriod.getTime() / 1000;
    }

    public String getFirstDateOfCurrentPeriodString()
    {
        return abbreviateDate(firstDayOfCurrentPeriod);
    }

    public String getLastDateOfCurrentPeriodString()
    {
        return abbreviateDate(lastDayOfCurrentPeriod);
    }

    public String getCurrentMonth()
    {
        String month = "";

        month += (calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()));
        month += " ";
        month += (String.valueOf(calendar.get(Calendar.YEAR)));

        return month.toUpperCase();
    }

    public String abbreviateDate(Date date)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM");

        String dateString = sdf.format(date);
        dateString = dateString.toUpperCase();
        return dateString;
    }

    public void setOnePeriodBefore()
    {
        if (dateRangeType == BYDAY)
        {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
        }
        else if (dateRangeType == BYWEEK)
        {
            calendar.add(Calendar.WEEK_OF_YEAR, -1);
        }
        else
        {
            calendar.add(Calendar.MONTH, -1);
        }
        setFirstDayOfCurrentPeriod();
        setLastDayOfCurrentPeriod();
    }

    public void setOnePeriodAfter()
    {
        if (dateRangeType == BYDAY)
        {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        else if (dateRangeType == BYWEEK)
        {
            calendar.add(Calendar.WEEK_OF_YEAR, 1);
        }
        else
        {
            calendar.add(Calendar.MONTH, 1);
        }
        setFirstDayOfCurrentPeriod();
        setLastDayOfCurrentPeriod();
    }

    public void setDateRangeType(int dateRangeType)
    {
        //Si va de mensual a semanal, analizar...
        if (this.dateRangeType == DatePointer.BYMONTH && dateRangeType == BYWEEK)
        {
            if (firstPurchaseDate != null)
            {
                if (firstDayOfCurrentPeriod.before(firstPurchaseDate))
                {
                    calendar.setTime(firstPurchaseDate);
                }
            }
        }
        this.dateRangeType = dateRangeType;
        setFirstDayOfCurrentPeriod();
        setLastDayOfCurrentPeriod();
    }

    public int getDateRangeType()
    {
        return dateRangeType;
    }

    public boolean isSameDay(Date dateToCompare)
    {
        int dayNumber;
        int objectWeekNumber;

        Calendar dayToCompare = Calendar.getInstance();
        dayToCompare.setTime(dateToCompare);
        dayNumber = dayToCompare.get(Calendar.DAY_OF_YEAR);

        Calendar firstHourDay = Calendar.getInstance();
        firstHourDay.setTime(firstDayOfCurrentPeriod);
        objectWeekNumber = firstHourDay.get(Calendar.DAY_OF_YEAR);

        if (dayNumber == objectWeekNumber)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean isSameWeekNumber(Date dateToCompare)
    {
        int dateWeekNumber;
        int objectWeekNumber;

        Calendar dayToCompare = Calendar.getInstance();
        dayToCompare.setTime(dateToCompare);
        dateWeekNumber = dayToCompare.get(Calendar.WEEK_OF_YEAR);

        Calendar firsDateOfWeeek = Calendar.getInstance();
        firsDateOfWeeek.setTime(firstDayOfCurrentPeriod);
        objectWeekNumber = firsDateOfWeeek.get(Calendar.WEEK_OF_YEAR);

        if (dayToCompare.get(Calendar.DAY_OF_WEEK) == 1) //Si es Domingo
        {
            //Ajustar el numero de semana
            dateWeekNumber -= 1;
        }

        if (dateWeekNumber == objectWeekNumber)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean isSameMonth(Date dateToCompare)
    {
        int monthNumber;
        int objectMonthNumber;

        Calendar monthToCompare = Calendar.getInstance();
        monthToCompare.setTime(dateToCompare);
        monthNumber = monthToCompare.get(Calendar.MONTH);

        Calendar focusedMonth = Calendar.getInstance();
        focusedMonth.setTime(firstDayOfCurrentPeriod);
        objectMonthNumber = focusedMonth.get(Calendar.MONTH);

        if (monthNumber == objectMonthNumber)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public void setFirstPurchaseDate(Date date)
    {
        this.firstPurchaseDate = date;
    }

    public Date getFirstPurchaseDate()
    {
        return firstPurchaseDate;
    }

    public String[] getHoursOfDay()
    {
        String[] hoursOfTheDay = new String[24];
        for (int i = 0; i < 24; i++)
        {
            hoursOfTheDay[i] = String.valueOf(i);
        }

        return hoursOfTheDay;
    }

    public String[] getDaysOfTheWeek()
    {
        String[] daysOfTheWeek = new String[]{
                "L ",
                "M ",
                "Mi ",
                "J ",
                "V ",
                "S ",
                "D "
        };

        for (int i = 0; i < 7; i++)
        {
            Calendar day = Calendar.getInstance();
            day.setTime(firstDayOfCurrentPeriod);
            day.add(Calendar.DAY_OF_MONTH, i);
            daysOfTheWeek[i] += String.valueOf(day.get(Calendar.DAY_OF_MONTH));
        }

        return daysOfTheWeek;
    }

    public String[] getDaysOfTheMonth()
    {
        String[] daysOfTheMonth = new String[31];
        for (int i = 0; i < 31; i++)
        {
            daysOfTheMonth[i] = String.valueOf(i);
        }

        return daysOfTheMonth;
    }

    public int getDateRangeLength()
    {
        if (dateRangeType == BYWEEK)
        {
            return 7;
        }
        else
        {
            int max = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            return max;
        }
    }
}
