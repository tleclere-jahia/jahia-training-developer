import {createSite, deleteSite, enableModule, publishAndWaitJobEnding} from "@jahia/cypress";
import gql from "graphql-tag";

describe('Html JCR Validator Test', () => {
    const siteKey = 'testsite';
    const langEN = 'en';
    const languages = langEN;
    const siteConfig = {
        languages,
        templateSet: 'dx-base-demo-templates',
        serverName: 'jahia-docker',
        locale: langEN
    };

    before(() => {
        createSite(siteKey, siteConfig);
        enableModule('jahia-training-developer', siteKey);
        publishAndWaitJobEnding(`/sites/${siteKey}/home`, [langEN]);
    });
    after(() => deleteSite(siteKey));

    beforeEach(() => cy.login());
    afterEach(() => cy.logout());

    it('Test Html valid', () => {
        cy.apollo({
            query: gql`
            mutation {
              jcr {
                mutateNode(pathOrId: "/sites/${siteKey}/contents") {
                  addChild(
                    name: "blabla"
                    primaryNodeType: "foont:text"
                    properties: [{name: "text", language: "en", value: "<span>blabla</span>"}]
                  ) {
                    uuid
                  }
                }
              }
            }`,
            errorPolicy: 'all'
        }).should(result => {
            expect(result?.errors).not.to.exist;
            expect(result?.errors).to.be.undefined;
            expect(result?.data?.jcr?.mutateNode?.addChild?.uuid).not.to.be.undefined;
        });
    });
    it('Test Html not valid', () => {
        cy.apollo({
            query: gql`
            mutation {
              jcr {
                mutateNode(pathOrId: "/sites/${siteKey}/contents") {
                  addChild(
                    name: "blabla2"
                    primaryNodeType: "foont:text"
                    properties: [{name: "text", language: "en", value: "<span>blabla"}]
                  ) {
                    uuid
                  }
                }
              }
            }`,
            errorPolicy: 'all'
        }).should(result => {
            expect(result?.errors).to.exist;
        });
    });
});
