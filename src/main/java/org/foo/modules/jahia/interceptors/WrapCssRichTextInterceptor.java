package org.foo.modules.jahia.interceptors;

import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.content.interceptor.BaseInterceptor;
import org.jahia.services.content.interceptor.RichTextInterceptor;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.jcr.RepositoryException;
import javax.jcr.Value;

@Component(service = BaseInterceptor.class, immediate = true)
public class WrapCssRichTextInterceptor extends RichTextInterceptor {
    private JCRStoreService jcrStoreService;

    @Reference
    private void setJcrStoreService(JCRStoreService jcrStoreService) {
        this.jcrStoreService = jcrStoreService;
    }

    @Activate
    private void start() {
        jcrStoreService.addInterceptor(this);
    }

    @Deactivate
    private void stop() {
        jcrStoreService.removeInterceptor(this);
    }

    @Override
    public Value afterGetValue(JCRPropertyWrapper property, Value storedValue) throws RepositoryException {
        return property.getSession().getValueFactory().createValue("<div class=\"richtext\">" + storedValue.getString() + "</div>");
    }

    @Override
    public Value[] afterGetValues(JCRPropertyWrapper property, Value[] storedValues) throws RepositoryException {
        Value[] res = new Value[storedValues.length];
        for (int i = 0; i < storedValues.length; i++) {
            Value storedValue = storedValues[i];
            res[i] = afterGetValue(property, storedValue);
        }
        return res;
    }
}
