package org.duoduochild.magic.magiccrawler.dataflow.datainput.model;

public enum Type {
    AD("Ad"), PUBLIC("Public"), UNKOWN("Unknown"), IMAGE("Image");

    private String processorPrefix;

    private Type(String processorPrefix) {
        this.processorPrefix = processorPrefix;
    }

    public String getProcessorPrefix() {
        return processorPrefix;
    }
}
