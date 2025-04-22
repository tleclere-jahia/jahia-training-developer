package org.foo.modules.jahia.interceptors;

import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkInterceptorTest {

    @Test
    public void testPattern() {
        String value = "##cms-context##/{mode}/{lang}/##ref:link1##.html";
        Matcher matcher = Pattern.compile("##cms-context##/\\{mode}/\\{lang}/##ref:link([0-9]+)##.html").matcher(value);
        Assert.assertTrue(matcher.matches());
        Assert.assertEquals("1", matcher.group(1));
    }
}
