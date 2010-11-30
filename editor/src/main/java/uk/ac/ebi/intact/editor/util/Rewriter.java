/**
 * Copyright 2010 The European Bioinformatics Institute, and others.
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
package uk.ac.ebi.intact.editor.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Replaces parts of a string and captures groups that can be used in the output string.</p>
 *
 * <p> For example:
 * <code>
 *
 * String original = "lalalala thing:12345 lololo";
 * Rewriter rewriter = new Rewriter("thing:(\\d+)", "The thing number is {1}";
 * String replaced = rewriter.rewrite(original);
 *
 * replaced would be "lalalala The thing number is 12345 lololo".
 *
 *
 * </p>
 *
 * @author Bruno Aranda (baranda@ebi.ac.uk)
 * @version $Id$
 */
public class Rewriter {

    private Pattern pattern;
    private Matcher matcher;

    private String replacement;

    public Rewriter(String regex, String replacement) {
        this.pattern = Pattern.compile(regex);
        this.replacement = replacement;
    }

    private String replacement(int groupCount) {
        String replaced = replacement;

        for (int i=0; i<=groupCount; i++) {
            replaced =  replaced.replaceAll("\\{"+i+"\\}", matcher.group(i));
        }

        return replaced;
    }

    public String rewrite(CharSequence original) {
        this.matcher = pattern.matcher(original);
        StringBuffer result = new StringBuffer(original.length());
        while (matcher.find()) {
            matcher.appendReplacement(result, "");
            result.append(replacement(matcher.groupCount()));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    public static void main(String[] args) {
        String original = "lalalala publication:EBI-12345 lololo";
        Rewriter rewriter = new Rewriter("(\\w+):(EBI-\\d+)", "<a href=\"/editor/{1}/{2}\">{1}</a>");
        String replaced = rewriter.rewrite(original);

        System.out.println(replaced);
    }
}
