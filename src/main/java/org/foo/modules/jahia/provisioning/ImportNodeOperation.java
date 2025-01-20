package org.foo.modules.jahia.provisioning;

import org.jahia.api.content.JCRTemplate;
import org.jahia.services.importexport.DocumentViewImportHandler;
import org.jahia.services.importexport.ImportExportService;
import org.jahia.services.provisioning.ExecutionContext;
import org.jahia.services.provisioning.Operation;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import javax.jcr.RepositoryException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Component(service = Operation.class, property = "type=" + ImportNodeOperation.TYPE)
public class ImportNodeOperation implements Operation {
    private static final Logger logger = LoggerFactory.getLogger(ImportNodeOperation.class);

    public static final String TYPE = "importNode";
    private static final String ROOT_PATH = "rootPath";

    @Reference
    private JCRTemplate jcrTemplate;
    @Reference
    private ImportExportService importExportService;

    private static final Pattern PATTERN = Pattern.compile("^[a-z][a-z0-9+.-]+:.*");

    /**
     * Get resource
     *
     * @param key              key
     * @param executionContext executioncontext
     * @return resource
     * @throws IOException exception
     */
    private static Resource getResource(String key, ExecutionContext executionContext) throws IOException {
        if (PATTERN.matcher(key).matches()) {
            return new UrlResource(key);
        } else {
            Map<String, Resource> resources = (Map<String, Resource>) executionContext.getContext().get("resources");
            if (resources != null) {
                return resources.get(key);
            }
        }
        throw new IOException(MessageFormat.format("Resource not found, {0}", key));
    }

    /**
     * Convert an entry with a list of string values on one main field, to a list of entries, one entry per value.
     * Generated entries will contain all other fields from the main entry.
     * If a value is itself an object, every fields here will be copied to the generated entry.
     * <p>
     * Example :
     * { key: [ 'a', 'b', 'c' ], option: 'v1' } -> [ { key: 'a', option: 'v1' }, { key: 'b', option: 'v1' }, { key: 'c', option: 'v1' }]
     * { key: [ 'a', { subkey: 'b', option: 'v2' } ], option: v1 } -> [ { key: 'a', option: 'v1' }, { key: 'b', option: 'v2' } ]
     *
     * @param entry The full entry
     * @return list of entries
     */
    private static List<Map<String, Object>> convertToList(Map<String, Object> entry) {
        Object value = entry.get(ImportNodeOperation.TYPE);
        if (value == null) {
            return Collections.emptyList();
        }
        if (value instanceof String) {
            // Simple case - value is a single-value, just return it as singleton entry
            return Collections.singletonList(entry);
        } else if (value instanceof List) {
            // Value is a list - items can be string, or sub-entries with different options
            List<Map<String, Object>> result = new ArrayList<>();
            List<?> l = (List<?>) value;
            for (Object item : l) {
                Map<String, Object> m = new HashMap<>(entry);
                if (item instanceof String) {
                    // This item is a string - build an entry based on root entry, with this value as the main key
                    m.put(ImportNodeOperation.TYPE, item);
                } else if (item instanceof Map) {
                    // This item is a sub-entry - build an entry based on root entry, override with sub entry, and use the sub-key field for main key
                    Map<String, Object> itemMap = (Map<String, Object>) item;
                    m.put(ImportNodeOperation.TYPE, itemMap.remove("url"));
                    m.putAll(itemMap);
                }
                result.add(m);
            }
            return result;
        }
        throw new IllegalArgumentException(ImportNodeOperation.TYPE + " parameter must be a list or a single-value");
    }

    @Override
    public boolean canHandle(Map<String, Object> map) {
        return map.containsKey(TYPE);
    }

    @Override
    public void perform(Map<String, Object> map, ExecutionContext executionContext) {
        try {
            jcrTemplate.doExecuteWithSystemSession(session -> {
                List<Map<String, Object>> entries = convertToList(map);
                for (Map<String, Object> subEntry : entries) {
                    try {
                        String url = (String) subEntry.get(TYPE);
                        String rootPath = (String) subEntry.get(ROOT_PATH);
                        importExportService.importZip(rootPath != null ? rootPath : "/", getResource(url, executionContext), DocumentViewImportHandler.ROOT_BEHAVIOUR_REPLACE, session);
                    } catch (IOException e) {
                        throw new RepositoryException(e);
                    }
                }
                return null;
            });
        } catch (RepositoryException e) {
            logger.error("Cannot import node", e);
        }
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
