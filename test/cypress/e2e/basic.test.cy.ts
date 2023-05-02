import {createSite, deleteSite, enableModule, publishAndWaitJobEnding} from "@jahia/cypress";
import {ContentEditor} from "@jahia/content-editor-cypress/dist/page-object/contentEditor"
import {JContent} from "@jahia/content-editor-cypress/dist/page-object/jcontent"
import {PageComposer} from "@jahia/content-editor-cypress/dist/page-object/pageComposer"
import {Field, SmallTextField} from "@jahia/content-editor-cypress/dist/page-object/fields"

describe('My first test', () => {
    const siteKey = 'testsite';
    const langEN = 'en';
    const langFR = 'fr';
    const languages = `${langEN},${langFR}`;
    const siteConfig = {
        languages,
        templateSet: 'basic-templateset',
        serverName: 'jahia-docker',
        locale: langEN
    };

    before(() => {
        createSite(siteKey, siteConfig);
        enableModule('jahia-training-developer', siteKey);
        publishAndWaitJobEnding(`/sites/${siteKey}/home`, ['fr', 'en']);
    });
    after(() => {
        deleteSite(siteKey);
    });

    it('Visit live homepage', () => {
        cy.visit(`/sites/${siteKey}/home.html`);
    });

    it('Create simple text', () => {
        cy.login();
        const pageComposer: PageComposer = PageComposer.visit(siteKey, 'en', 'home.html');
        const contentEditor: ContentEditor = pageComposer
            .openCreateContent()
            .getContentTypeSelector()
            .searchForContentType('Simple text')
            .selectContentType('Simple text')
            .create();
        contentEditor.getField(SmallTextField, 'jnt:text_text').addNewValue('toto');
        contentEditor.save();
        cy.logout();
    });

    it('Create an employee', () => {
        cy.login();
        cy.visit(`/cms/editframe/default/en/sites/${siteKey}.manageModules.html`).contains('jahia-training-developer');
        const contentEditor: ContentEditor = JContent.visit(siteKey, 'en', 'content-folders/contents').createContent('Employee');
        const field = contentEditor.getField(Field, 'foont:employee_jobTitle', false).get()
        field.click()
            .get('li.moonstone-menuItem[role="option"]')
            .should(elems => {
                expect(elems).to.have.length(4);
                const values = elems.get().map(e => e.getAttribute('data-value'));
                expect(values.sort()).to.deep.eq(['developer', 'it_administrator', 'it_consultant', 'manager']);
            });
        cy.logout();
    });
});
