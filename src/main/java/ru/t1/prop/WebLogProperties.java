package ru.t1.prop;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.logging.web")
public class WebLogProperties {
    private boolean enabled = true;
    private LevelsDetail detailing = LevelsDetail.DEFAULT;

    private enum LevelsDetail {SHORT, DEFAULT, FULL}
}
