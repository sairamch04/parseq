package com.linkedin.parseq;

public interface TaskMonitor {

    long getStartNs();

    long getEndNs();

    void await();
}
