package org.duoduochild.magic.magiccrawler.dataflow.datainput;

import org.duoduochild.magic.magiccrawler.dataflow.datainput.model.Type;
import org.duoduochild.magic.magiccrawler.dataflow.datainput.processor.ResultEntryProcessor;

/**
 * Created by levinliu on 2017/10/11
 * GitHub: https://github.com/levinliu
 * (Change file header on Settings -> Editor -> File and Code Templates)
 */
public class ResultEntryProcessorFactory {
    public static ResultEntryProcessor newProcessor(Type type) {
        String className = ResultEntryProcessor.class.getPackage().getName() + ".";
        className += type.getProcessorPrefix() + ResultEntryProcessor.class.getSimpleName();
        try {
            return (ResultEntryProcessor) Class.forName(className).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new ProcessingException("Fail to instance of class " + className + " for type=" + type, e);
        }
    }
}
