/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
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
 *  limitations under the License.
 */
package uk.ac.ebi.intact.application.hierarchview.struts.view.utils;

/**
 * Helper class for confidence filter.
 *
 * @author Irina Armean (iarmean@ebi.ac.uk)
 * @version $Id$
 * @since 1.6.0-SNAPSHOT
 */
public class Compare {
    private static String lowerEqual = "<=";
    private static String higherEqual = ">=";
    private static String lower = "<";
    private static String higher = ">";
    private static String equal = "="; 

    public static boolean compare (Double value1, Double value2, String relation){
       if(lowerEqual.equals( relation )){
               return value1 <= value2;
       } else if (higherEqual.equals( relation )){
           return value1 >= value2;
       } else if (lower.equals( relation )){
           return value1 < value2;
       } else if (higher.equals( relation )){
           return value1 > value2;
       } else if (equal.equals( relation )){
           return value1.doubleValue() == value2.doubleValue();
       }
       return false;
    }
}
