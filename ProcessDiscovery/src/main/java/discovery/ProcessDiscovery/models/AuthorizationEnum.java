package discovery.ProcessDiscovery.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public enum AuthorizationEnum implements Serializable {

    PRIVATE,
    PUBLIC,
    MINIMUM;

    private static Map<String, AuthorizationEnum> namesMap = new HashMap<String, AuthorizationEnum>(3);

    static {
        namesMap.put("private", PRIVATE);
        namesMap.put("public", PUBLIC);
        namesMap.put("minimum", MINIMUM);
    }

    @JsonCreator
    public static AuthorizationEnum forValue(String value) {
        return namesMap.get(StringUtils.lowerCase(value));
    }

    @JsonValue
    public String toValue() {
        for (Map.Entry<String, AuthorizationEnum> entry : namesMap.entrySet()) {
            if (entry.getValue() == this)
                return entry.getKey();
        }

        return null;
    }

}
