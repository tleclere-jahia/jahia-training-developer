import {createSite, deleteSite, enableModule} from "@jahia/cypress";
import gql from "graphql-tag";

export const MODULE = 'jahia-training-developer';

// The template set must be deployed on the target Jahia. Override with CYPRESS_TEMPLATE_SET.
export const templateSet = (): string => Cypress.env('TEMPLATE_SET') || 'dx-base-demo-templates';

export const serverName = (siteKey: string): string => `${siteKey.toLowerCase()}.training.test`;

export const setupTrainingSite = (siteKey: string, languages: string[] = ['en']) => {
    createSite(siteKey, {
        languages: languages.join(','),
        templateSet: templateSet(),
        serverName: serverName(siteKey),
        locale: languages[0]
    });
    enableModule(MODULE, siteKey);
};

export const teardownTrainingSite = (siteKey: string) => deleteSite(siteKey);

// Runs a GraphQL query as root and keeps the errors in the result instead of failing.
export const gqlQuery = (query: string, variables: Record<string, unknown> = {}) => cy.apollo({
    query: gql(query),
    variables,
    errorPolicy: 'all',
    fetchPolicy: 'no-cache'
});

// Runs a GraphQL mutation as root and keeps the errors in the result instead of failing.
export const gqlMutation = (mutation: string, variables: Record<string, unknown> = {}) => cy.apollo({
    mutation: gql(mutation),
    variables,
    errorPolicy: 'all'
});

export type Property = { name: string, value?: string, values?: string[], language?: string };

export const addChild = (parentPath: string, name: string, primaryNodeType: string, properties: Property[] = [], mixins: string[] = []) =>
    gqlMutation(`mutation($parentPath: String!, $name: String!, $type: String!, $properties: [InputJCRProperty], $mixins: [String]) {
        jcr {
            addNode(parentPathOrId: $parentPath, name: $name, primaryNodeType: $type, properties: $properties, mixins: $mixins) {
                uuid
                node { path }
            }
        }
    }`, {parentPath, name, type: primaryNodeType, properties, mixins});

// Fails the test when the node could not be created, so that a later assertion does not run on nothing.
export const addChildOrFail = (parentPath: string, name: string, primaryNodeType: string, properties: Property[] = [], mixins: string[] = []) =>
    addChild(parentPath, name, primaryNodeType, properties, mixins).then(result => {
        expect(result?.errors, `errors when creating ${parentPath}/${name}`).to.be.undefined;
        return result.data.jcr.addNode;
    });

// A rejected write must fail on the server side (constraint, validator, permission), never on a malformed query.
export const expectServerRejection = (result, ...messageParts: string[]) => {
    expect(result?.errors, 'errors').to.have.length.greaterThan(0);
    result.errors.forEach(error => expect(error?.extensions?.classification, error.message).not.to.eq('ValidationError'));
    const messages = JSON.stringify(result.errors);
    messageParts.forEach(part => expect(messages).to.contain(part));
};

export const publish = (path: string, languages: string[] = ['en']) =>
    gqlMutation(`mutation($path: String!, $languages: [String]) {
        jcr { mutateNode(pathOrId: $path) { publish(languages: $languages, publishSubNodes: true) } }
    }`, {path, languages});

export const nodeProperties = (path: string, properties: string[], language = 'en', workspace: 'EDIT' | 'LIVE' = 'EDIT') =>
    gqlQuery(`query($path: String!, $properties: [String], $language: String, $workspace: Workspace) {
        jcr(workspace: $workspace) {
            nodeByPath(path: $path) {
                uuid
                mixinTypes { name }
                properties(names: $properties, language: $language) { name value values }
                children { nodes { name primaryNodeType { name } mixinTypes { name } } }
            }
        }
    }`, {path, properties, language, workspace});

export const propertyValue = (node: { properties: { name: string, value: string }[] }, name: string): string | undefined =>
    node?.properties?.find(p => p.name === name)?.value;
