package com.helpshift.liteyagami.proactive;

import static com.helpshift.liteyagami.config.SampleAppConfig.getSDKConfig;

import com.helpshift.proactive.HelpshiftProactiveAPIConfigCollector;

import java.util.Map;

/**
 * Provides local config when handling proactive outbound notification or deeplinks.
 * For eaxmple, this can be used to set user specific config in Helpshift SDK when dealing with proactive outbound
 * links and notifications.
 * https://developers.helpshift.com/sdkx_android/outbound-support/#user-specific-config
 */
public class ProactiveLocalAPIConfigProvider implements HelpshiftProactiveAPIConfigCollector {

    @Override
    public Map<String, Object> getAPIConfig() {
        Map<String, Object> localConfig = getSDKConfig();
        localConfig.put("tags", new String[]{"localConfig", "paid", "renewal", "vip", "level"});
        return localConfig;
    }

}
