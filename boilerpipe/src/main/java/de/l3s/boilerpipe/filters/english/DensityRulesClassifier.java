/**
 * boilerpipe
 *
 * Copyright (c) 2009 Christian Kohlschütter
 *
 * The author licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.l3s.boilerpipe.filters.english;

import java.util.List;
import java.util.ListIterator;

import de.l3s.boilerpipe.BoilerpipeFilter;
import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.document.TextBlock;
import de.l3s.boilerpipe.document.TextDocument;
import de.l3s.boilerpipe.labels.DefaultLabels;

/**
 * Classifies {@link TextBlock}s as content/not-content through rules that have
 * been determined using the C4.8 machine learning algorithm, as described in the
 * paper "Boilerplate Detection using Shallow Text Features", particularly using
 * text densities and link densities.
 *
 * @author Christian Kohlschütter
 */
public class DensityRulesClassifier implements
        BoilerpipeFilter {
    public static final DensityRulesClassifier INSTANCE = new DensityRulesClassifier();

    /**
     * Returns the singleton instance for RulebasedBoilerpipeClassifier.
     */
    public static DensityRulesClassifier getInstance() {
        return INSTANCE;
    }

    public boolean process(TextDocument doc)
            throws BoilerpipeProcessingException {
        List<TextBlock> textBlocks = doc.getTextBlocks();
        boolean hasChanges = false;

        ListIterator<TextBlock> it = textBlocks.listIterator();
        if (!it.hasNext()) {
            return false;
        }
        TextBlock prevBlock = TextBlock.EMPTY_START;
        TextBlock currentBlock = it.next();
        TextBlock nextBlock = it.hasNext() ? it.next() : TextBlock.EMPTY_START;

        hasChanges = classify(prevBlock, currentBlock, nextBlock) | hasChanges;

        if (nextBlock != TextBlock.EMPTY_START) {
            while (it.hasNext()) {
                prevBlock = currentBlock;
                currentBlock = nextBlock;
                nextBlock = it.next();
                String trimmedText = nextBlock.getText().trim();
                while (trimmedText.length() == 0 || trimmedText.length() == 1 && trimmedText.charAt(0) == 160) {
                    if (!it.hasNext())
                        break;
                    nextBlock = it.next();
                    trimmedText = nextBlock.getText().trim();
                }
                hasChanges = classify(prevBlock, currentBlock, nextBlock)
                        | hasChanges;
            }
            prevBlock = currentBlock;
            currentBlock = nextBlock;
            nextBlock = TextBlock.EMPTY_START;
            hasChanges = classify(prevBlock, currentBlock, nextBlock)
                    | hasChanges;
        }

        return hasChanges;
    }

    protected boolean classify(final TextBlock prev, final TextBlock curr,
                               final TextBlock next) {
        final boolean isContent;

        if (curr.getLinkDensity() <= 0.333333) {
            if (prev.getLinkDensity() <= 0.555556) {
                if (curr.getTextDensity() <= 7) {
                    if (next.getTextDensity() <= 8) {
                        if (prev.getTextDensity() <= 4) {
                            if (prev.isImage() && curr.isImage()) {
                                isContent = true;
                            } else {
                                isContent = false;
                            }
                        } else {
                            isContent = true;
                        }
                    } else {
                        isContent = true;
                    }
                } else {
                    if (next.getTextDensity() == 0) {
                        isContent = false;
                    } else {
                        isContent = true;
                    }
                }
            } else {
                if (next.getTextDensity() <= 11) {
                    if (curr.getNumWords() > 128) {
                        isContent = true;
                    } else {
                        if (prev.getLinkDensity() == 1.0f && prev.isImage()) {
                            if (next.getLinkDensity() == 1.0f && next.isImage()) {
                                isContent = true;
                            } else {
                                if (curr.getTextDensity() >= 18) {
                                    isContent = true;
                                } else
                                    isContent = false;
                            }
                        } else {
                            isContent = false;
                        }
                    }
                } else {
                    isContent = true;
                }
            }
        } else {
            if (curr.getLinkDensity() == 1.0f && curr.isImage()) {
                isContent = prev.isContent();
            } else
                isContent = false;
        }

        return curr.setIsContent(isContent && !curr.hasLabel(DefaultLabels.INDICATES_END_OF_TEXT));
    }

}
