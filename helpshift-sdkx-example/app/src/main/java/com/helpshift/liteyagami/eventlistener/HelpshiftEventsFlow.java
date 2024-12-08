package com.helpshift.liteyagami.eventlistener;

import java.util.ArrayList;

public class HelpshiftEventsFlow {

    private static HelpshiftEventsFlow helpshiftEventsFlow;
    private static final ArrayList<HelpshiftEventData> helpshiftEvents = new ArrayList<>();
    private static HSEventsFlowListener hsEventsFlowListener;

    public static synchronized void initInstance() {
        if (helpshiftEventsFlow == null) {
            helpshiftEventsFlow = new HelpshiftEventsFlow();
        }
    }

    public static HelpshiftEventsFlow getInstance() {
        return helpshiftEventsFlow;
    }

    public void addHelpshiftEvent(HelpshiftEventData helpshiftEvent) {
        helpshiftEvents.add(helpshiftEvent);
        if (hsEventsFlowListener != null) {
            hsEventsFlowListener.onNewEvent(helpshiftEvent);
        }
    }

    public ArrayList<HelpshiftEventData> getHelpshiftEvents() {
        return helpshiftEvents;
    }

    public static void setHelpshiftEventFlowListener(HSEventsFlowListener helpshiftEventFlowListener) {
        hsEventsFlowListener = helpshiftEventFlowListener;
    }

    public static void deregisterHelpshiftEventFlowListener() {
        hsEventsFlowListener = null;
    }
}
