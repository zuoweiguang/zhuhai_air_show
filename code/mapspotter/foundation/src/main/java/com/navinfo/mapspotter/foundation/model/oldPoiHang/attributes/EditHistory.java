package com.navinfo.mapspotter.foundation.model.oldPoiHang.attributes;

import java.util.List;

public class EditHistory {

    private String sourceName;

    private String sourceTask;

    private String sourceProject;

    public String getSourceProject() {
        return sourceProject;
    }

    public void setSourceProject(String sourceProject) {
        this.sourceProject = sourceProject;
    }

    private List<Operator> operator;

    private String operation;

    private String mergeDate;

    private List<MergeContent> mergeContents;

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }


    public String getSourceTask() {
        return sourceTask;
    }

    public void setSourceTask(String sourceTask) {
        this.sourceTask = sourceTask;
    }

    public List<Operator> getOperator() {
        return operator;
    }

    public void setOperator(List<Operator> operator) {
        this.operator = operator;
    }


    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getMergeDate() {
        return mergeDate;
    }

    public void setMergeDate(String mergeDate) {
        this.mergeDate = mergeDate;
    }

    public List<MergeContent> getMergeContents() {
        return mergeContents;
    }

    public void setMergeContents(List<MergeContent> mergeContents) {
        this.mergeContents = mergeContents;
    }
}
