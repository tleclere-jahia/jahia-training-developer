package org.foo.modules.jahia.graphql;

import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.annotationTypes.GraphQLNonNull;
import graphql.annotations.annotationTypes.GraphQLTypeExtension;
import org.jahia.api.content.JCRTemplate;
import org.jahia.modules.graphql.provider.dxm.DXGraphQLProvider;
import org.jahia.modules.graphql.provider.dxm.node.GqlJcrNode;
import org.jahia.modules.graphql.provider.dxm.node.GqlJcrNodeImpl;
import org.jahia.osgi.BundleUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.Query;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@GraphQLTypeExtension(DXGraphQLProvider.Mutation.class)
public final class CategorySubscriptionExtension {
    private static final Logger logger = LoggerFactory.getLogger(CategorySubscriptionExtension.class);

    private static final String MIXIN = "foomix:categorySubscription";

    private CategorySubscriptionExtension() {
        // Nothing to do
    }

    @GraphQLField
    public static Result subscribeCategories(@GraphQLName("email") @GraphQLNonNull String email, @GraphQLName("categoriesSubscribed") List<String> categoriesSubscribed, @GraphQLName("categoriesUnsubscribed") List<String> categoriesUnsubscribed) {
        JCRTemplate jcrTemplate = BundleUtils.getOsgiService(JCRTemplate.class, null);
        Result result = new Result(email);

        if (categoriesUnsubscribed != null && !categoriesUnsubscribed.isEmpty()) {
            categoriesUnsubscribed.forEach(categoryIdentifier -> {
                try {
                    jcrTemplate.doExecuteWithSystemSession(systemSession -> {
                        systemSession.getWorkspace().getQueryManager().createQuery(
                                        "SELECT * FROM [" + MIXIN + "]" +
                                                "WHERE ISDESCENDANTNODE('/sites/systemsite/categories')" +
                                                "AND ([emails] = '" + email + "' OR [confirmedEmails] = '" + email + "')" +
                                                "AND [jcr:uuid] = '" + categoryIdentifier + "'", Query.JCR_SQL2)
                                .execute().getNodes().forEach(categoryNode -> {
                                    removeEmail(categoryNode, "emails", email);
                                    removeEmail(categoryNode, "confirmedEmails", email);
                                    result.categoriesUnsubscribed.add(new GqlJcrNodeImpl(categoryNode));
                                });
                        return null;
                    });
                } catch (RepositoryException e) {
                    logger.error("", e);
                }
            });
        }

        if (categoriesSubscribed != null && !categoriesSubscribed.isEmpty()) {
            categoriesSubscribed.forEach(categoryIdentifier -> {
                try {
                    jcrTemplate.doExecuteWithSystemSession(systemSession -> {
                        systemSession.getWorkspace().getQueryManager().createQuery(
                                        "SELECT * FROM [jnt:category]" +
                                                "WHERE ISDESCENDANTNODE('/sites/systemsite/categories')" +
                                                "AND [jcr:uuid] = '" + categoryIdentifier + "'", Query.JCR_SQL2)
                                .execute().getNodes().forEach(categoryNode -> {
                                    addEmail(categoryNode, email);
                                    result.categoriesSubscribed.add(new GqlJcrNodeImpl(categoryNode));
                                });
                        return null;
                    });
                } catch (RepositoryException e) {
                    logger.error("", e);
                }
            });
        }

        return result;
    }

    private static void removeEmail(JCRNodeWrapper categoryNode, String property, String email) {
        try {
            List<Value> values = new ArrayList<>(Arrays.asList(categoryNode.getProperty(property).getValues()));
            values.removeIf(value -> {
                try {
                    return email.equals(value.getString());
                } catch (RepositoryException e) {
                    logger.error("", e);
                    return false;
                }
            });
            categoryNode.getProperty(property).setValue(values.toArray(new Value[0]));
            categoryNode.saveSession();
        } catch (RepositoryException e) {
            logger.error("", e);
        }
    }

    private static void addEmail(JCRNodeWrapper categoryNode, String email) {
        try {
            if (!categoryNode.isNodeType(MIXIN)) {
                categoryNode.addMixin(MIXIN);
            }
            List<Value> newValues = new ArrayList<>();
            if (categoryNode.hasProperty("emails")) {
                Value[] values = categoryNode.getProperty("emails").getValues();
                if (Arrays.stream(values).noneMatch(value -> {
                    try {
                        return email.equals(value.getString());
                    } catch (RepositoryException e) {
                        logger.error("", e);
                        return false;
                    }
                })) {
                    newValues.addAll(Arrays.asList(values));
                }
            }
            newValues.add(new ValueImpl(email, PropertyType.STRING, false));
            categoryNode.setProperty("emails", newValues.toArray(new Value[0]));
            categoryNode.saveSession();
        } catch (RepositoryException e) {
            logger.error("", e);
        }
    }

    @GraphQLField
    private static class Result {
        private final String email;
        private final List<GqlJcrNode> categoriesSubscribed;
        private final List<GqlJcrNode> categoriesUnsubscribed;

        public Result(String email) {
            this.email = email;
            this.categoriesSubscribed = new ArrayList<>();
            this.categoriesUnsubscribed = new ArrayList<>();
        }

        @GraphQLField
        public String getEmail() {
            return email;
        }

        @GraphQLField
        public List<GqlJcrNode> getCategoriesSubscribed() {
            return categoriesSubscribed;
        }

        @GraphQLField
        public List<GqlJcrNode> getCategoriesUnsubscribed() {
            return categoriesUnsubscribed;
        }
    }
}
