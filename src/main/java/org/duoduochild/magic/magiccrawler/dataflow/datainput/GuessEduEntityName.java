package org.duoduochild.magic.magiccrawler.dataflow.datainput;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * Created by levinliu on 2017/10/14
 * GitHub: https://github.com/levinliu
 * (Change file header on Settings -> Editor -> File and Code Templates)
 */
public class GuessEduEntityName {
    private final Logger log = Logger.getLogger(GuessEduEntityName.class);

    private static final String[] SEPARATOR = {"-", ",", "|", "_", " ", "..."};
    private String title;
    private String description;

    public GuessEduEntityName(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public Optional<String> guessName() {
        Set<String> keywords = splitTitle(title, SEPARATOR);
        if (!keywords.isEmpty()) {
            //log.debug("keywords=" + keywords);
            for (String name : keywords) {
                if (isNotBlank(name) && description.startsWith(name)) {
                    log.debug("get name[" + name + "] from title[" + title + "] desc[" + description + "]");
                    return Optional.of(name);
                }
            }
            for (String name : keywords) {
                if (isNotBlank(name) && description.contains(name) && name.length() >= 4) {
                    log.debug("get potential name[" + name + "] from title[" + title + "] desc[" + description + "]");
                    return Optional.of(name);
                }
            }
        }
        return Optional.empty();
    }

    private Set<String> splitTitle(String title, String[] separators) {
        String separator = separators[0];
        Iterable<String> texts = Splitter.on(separator).split(title);
        Set<String> keywords = Sets.newHashSet();
        for (String text : texts) {
            if (isNotBlank(text)) {
                if (separators.length == 1) {
                    keywords.add(text);
                } else {
                    int decreasedLength = separators.length - 1;
                    String[] subArray = new String[decreasedLength];
                    System.arraycopy(separators, 1, subArray, 0, decreasedLength);
                    //log.debug("subArray=" + Arrays.toString(subArray));
                    keywords.addAll(splitTitle(text, subArray));
                }
            }
        }
        return keywords;
    }

}
