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
package uk.ac.ebi.intact.view.webapp.controller.config;

import org.springframework.stereotype.Controller;

/**
 * Colour Palette.
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
@Controller
public class ColourPalette {

    private final String[] GREEN_HEX = new String[] {
            "#73b360", "#84bc73", "#96c688", "#a7cf9b", "#cae2c3", "#dcecd7", "#edf5ea", "#f6faf4"
    };

    private final String[] RED_HEX = new String[] {
            "#e33e3e", "#e65555", "#ea6e6e", "#ed8585", "#f4b6b6", "#f8cfcf", "#fbe6e6", "#fdf2f2"
    };

    private final String[] GREYISH_HEX = new String[] {
            "#006666", "#1f7979", "#408c8c", "#5e9e9e", "#a1c7c7", "#bdd7d7", "#deebeb", "#eef5f5"
    };

    private int greenIndex;
    private int redIndex;
    private int greyIndex;

    public int lenght() {
        return GREEN_HEX.length;
    }

    public String getGreenHex(int position) {
        return GREEN_HEX[position];
    }

    public String getRedHex(int position) {
        return RED_HEX[position];
    }

    public String getGreyishHex(int position) {
        return GREYISH_HEX[position];
    }

    public String getNextGreen() {
        if (greenIndex == lenght()) {
            greenIndex = 0;
        }
        String colour = getGreenHex(greenIndex);
        greenIndex++;

        return colour;
    }

    public String getNextGrey() {
        if (greyIndex == lenght()) {
            greyIndex = 0;
        }
        String colour = getGreyishHex(greyIndex);
        greyIndex++;

        return colour;
    }

    public String getNextRed() {
        if (redIndex == lenght()) {
            redIndex = 0;
        }
        String colour = getRedHex(redIndex);
        redIndex++;

        return colour;
    }
}
