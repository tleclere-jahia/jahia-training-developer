import {createSite, deleteSite, enableModule, publishAndWaitJobEnding} from "@jahia/cypress";
import {ContentEditor} from "@jahia/content-editor-cypress/dist/page-object/contentEditor"
import {PageComposer} from "@jahia/content-editor-cypress/dist/page-object/pageComposer"
import {Field} from "@jahia/content-editor-cypress/dist/page-object/fields";
import gql from "graphql-tag";

describe('Jahia Properties Taglib Test', () => {
    const siteKey = 'testsite';
    const langEN = 'en';
    const langFR = 'fr';
    const languages = `${langEN},${langFR}`;
    const siteConfig = {
        languages,
        templateSet: 'dx-base-demo-templates',
        serverName: 'jahia-docker',
        locale: langEN
    };

    before(() => {
        createSite(siteKey, siteConfig);
        enableModule('jahia-training-developer', siteKey);
        publishAndWaitJobEnding(`/sites/${siteKey}/home`, [langFR, langEN]);
    });
    after(() => deleteSite(siteKey));

    beforeEach(() => cy.login());
    afterEach(() => cy.logout())

    it('Create a simple list', () => {
        const contentEditor: ContentEditor = PageComposer.visit(siteKey, langEN, 'home.html')
            .createContent('Simple list');
        contentEditor.getField(Field, 'foont:simpleList_itemType').get()
            .find('div[role="dropdown"]').should('exist').click();
        contentEditor.getField(Field, 'foont:simpleList_itemType').get()
            .find('menu[role="listbox"]').should('exist')
            .find('li[data-value="foomix:textType"]').should('exist').click();
        contentEditor.save();
        cy.visit(`/cms/render/default/en/sites/${siteKey}/home.html`);
    });
    it('Check the jahia properties in the html rendered', () => {
        cy.visit(`/cms/render/default/en/sites/${siteKey}/home.html`);
        // cy.get(':contains("<!-- cluster.node.serverId: standalone -->")').should('exist');
        cy.apollo({
            query: gql`
            query($path: String!) {
                jcr {
                    nodeByPath(path: $path) {
                        renderedContent { output }
                    }
                }
            }`, variables: {path: `/sites/${siteKey}/home/area-main/simple-list`}
        }).should(result => {
            expect(result?.data?.jcr?.nodeByPath?.renderedContent?.output).to.equals('<!-- cluster.node.serverId: standalone -->');
        });
    });
});
