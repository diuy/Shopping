package com.example.shopping.task;

public class TaskFactory {
    public static final String NAME_YPTask = "YPTask";
    public static final String NAME_JDTask = "JDTask";
    public static final String NAME_JDTestTask = "JDTestTask";
    public static final String NAME_NotifyTask = "NotifyTask";

    public static Task createTask(String name, TaskHelper helper) {
        if (NAME_YPTask.contentEquals(name)) {
            return new YPTask(helper);
        }
        if (NAME_JDTask.contentEquals(name)) {
            return new JDTask(helper);
        }
        if (NAME_JDTestTask.contentEquals(name)) {
            return new JDTestTask(helper);
        }
        if (NAME_NotifyTask.contentEquals(name)) {
            return new NotifyTask(helper);
        }
        return null;
    }

    public static Task createNotifyTask(TaskHelper helper) {
        return createTask(NAME_NotifyTask, helper);
    }
}
