package cc.before30.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by before30 on 16/05/2017.
 */
@Component
public class AppInfoContributor implements InfoContributor {

    @Autowired AppInfoProperties appInfoProperties;

    @Override
    public void contribute(Info.Builder builder) {
        Map<String, String> map = new HashMap<>();
        map.put("name", appInfoProperties.getName());
        map.put("version", appInfoProperties.getVersion());

        builder
            .withDetail("app", map);
    }
}
