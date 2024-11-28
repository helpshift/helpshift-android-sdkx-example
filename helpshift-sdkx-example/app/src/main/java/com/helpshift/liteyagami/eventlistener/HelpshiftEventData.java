package com.helpshift.liteyagami.eventlistener;

import java.util.Map;

public class HelpshiftEventData {
    private final String eventName;
    private final Map<String, Object> data;

    public HelpshiftEventData(String eventName, Map<String, Object> data) {
        this.eventName = eventName;
        this.data = data;
    }

    public String getEventName() {
        return eventName;
    }

    public Map<String, Object> getData() {
        return data;
    }
}
