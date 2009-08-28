/**
 * Copyright 2009 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.application.editor.util;

import java.util.Calendar;
import java.util.Date;

/**
 * TODO comment it.
 *
 * @author Catherine Leroy (cleroy@ebi.ac.uk)
 * @version $Id$
 */
public class DateToolbox {


        public static String getMonth(int monthNumber){
            String monthName = "";
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

