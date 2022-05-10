package org.foo.modules.jahia.taglibs;

import org.jahia.taglibs.ValueJahiaTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

public final class TimeAgoTaglib extends ValueJahiaTag {
    private static final Logger logger = LoggerFactory.getLogger(TimeAgoTaglib.class);

    private static final String DAY = "jour";
    private static final String HOUR = "heure";
    private static final String MINUTE = "minute";
    private static final String SECONDE = "seconde";
    private static final String NOW = "maintenant";

    private Date date;

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public int doStartTag() {
        Duration between = Duration.between(LocalDateTime.ofInstant(date.toInstant(), ZoneOffset.UTC), LocalDateTime.now(ZoneOffset.UTC));
        long days = between.toDays();
        between = between.minusDays(days);
        long hours = between.toHours();
        between = between.minusHours(hours);
        long minutes = between.toMinutes();
        between = between.minusMinutes(minutes);
        long seconds = between.getSeconds();

        String valueToSet = NOW;
        if (days > 0) {
            valueToSet = format(days, DAY);
        } else if (hours > 0) {
            valueToSet = format(hours, HOUR);
        } else if (minutes > 0) {
            valueToSet = format(minutes, MINUTE);
        } else if (seconds > 0) {
            valueToSet = format(seconds, SECONDE);
        }
        if (getVar() != null) {
            pageContext.setAttribute(getVar(), valueToSet);
        } else {
            try {
                pageContext.getOut().print(valueToSet);
            } catch (IOException e) {
                logger.error("", e);
            }
        }
        return SKIP_BODY;
    }

    private static String format(long value, String unit) {
        StringBuilder sb = new StringBuilder();
        sb.append(value).append(" ").append(unit);
        if (value > 1) {
            sb.append("s");
        }
        return sb.toString();
    }

    public int doEndTag() {
        resetState();
        return EVAL_PAGE;
    }

    @Override
    protected void resetState() {
        super.resetState();
        date = null;
    }
}
