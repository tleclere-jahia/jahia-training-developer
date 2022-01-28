package org.foo.modules.jahia.jaxrs;

public class RequestData {
    private String pathjahia;
    private String typedecontenu;
    private String champdetrie;
    private String langue;
    private Integer offset;
    private Integer limit;

    public String getPathjahia() {
        return pathjahia;
    }

    public String getTypedecontenu() {
        return typedecontenu;
    }

    public String getChampdetrie() {
        return champdetrie;
    }

    public String getLangue() {
        return langue;
    }

    public Integer getOffset() {
        return offset;
    }

    public Integer getLimit() {
        return limit;
    }
}
