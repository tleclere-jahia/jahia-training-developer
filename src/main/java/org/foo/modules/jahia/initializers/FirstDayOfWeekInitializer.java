package org.foo.modules.jahia.initializers;

import org.apache.jackrabbit.util.ISO8601;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.content.nodetypes.initializers.ValueInitializer;
import org.osgi.service.component.annotations.Component;

import javax.jcr.PropertyType;
import javax.jcr.Value;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.util.GregorianCalendar;
import java.util.List;

@Component(service = ValueInitializer.class)
public class FirstDayOfWeekInitializer implements ValueInitializer {
    @Override
    public Value[] getValues(ExtendedPropertyDefinition extendedPropertyDefinition, List<String> params) {
        return new Value[]{new ValueImpl(
                ISO8601.format(GregorianCalendar.from(
                        LocalDateTime.now(ZoneOffset.UTC)
                                .with(ChronoField.DAY_OF_WEEK, 1)
                                .toLocalDate()
                                .atStartOfDay()
                                .atZone(ZoneOffset.UTC))),
                PropertyType.DATE, false)};
    }
}
