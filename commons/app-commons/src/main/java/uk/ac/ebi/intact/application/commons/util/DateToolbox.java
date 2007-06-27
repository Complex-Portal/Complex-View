/*
Copyright (c) 2002 The European Bioinformatics Institute, and others.
All rights reserved. Please see the file LICENSE
in the root directory of this distribution.
*/
package uk.ac.ebi.intact.application.commons.util;

import java.util.Date;
import java.util.Calendar;

/**
 * TODO comment it.
 *
 * @author Catherine Leroy (cleroy@ebi.ac.uk)
 * @version $Id$
 */
public class DateToolbox {


        public static String getMonth(int monthNumber){
            String monthName = new String();
            switch (monthNumber) {
                case 0:  monthName = "JAN"; break;
                case 1:  monthName = "FEB"; break;
                case 2:  monthName = "MAR"; break;
                case 3:  monthName = "APR"; break;
                case 4:  monthName = "MAY"; break;
                case 5:  monthName = "JUN"; break;
                case 6:  monthName = "JUL"; break;
                case 7:  monthName = "AUG"; break;
                case 8:  monthName = "SEP"; break;
                case 9: monthName = "OCT"; break;
                case 10: monthName = "NOV"; break;
                case 11: monthName = "DEC"; break;
                default: monthName = "Not a month!";break;
            }

            return monthName;
        }

        public static String formatDate(Date date){
            if(date != null){
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                String day = Integer.toString(calendar.get(Calendar.DAY_OF_MONTH));
                String monthName = getMonth(calendar.get(Calendar.MONTH));
                String year = Integer.toString(calendar.get(Calendar.YEAR));
                String newDate = year + "-" + monthName + "-" + day;
                newDate = newDate.trim();
                return newDate;
            }else{
                return null;
            }
        }


}
