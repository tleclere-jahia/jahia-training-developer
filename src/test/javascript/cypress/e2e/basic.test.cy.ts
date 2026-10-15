import {publishAndWaitJobEnding} from "@jahia/cypress";
import {gqlQuery, nodeProperties, propertyValue, setupTrainingSite, teardownTrainingSite} from "../support/training";

// UI tests in jContent and the Content Editor. The GraphQL specs cover the server side of the same features.
describe('Content editor UI with the training module', () => {
    const siteKey = 'trainingBasic';
    const contents = `/sites/${siteKey}/contents`;

    const field = (name: string) => cy.get(`[data-sel-content-editor-field="${name}"]`);

    // Opens the create form of one node type in the contents folder, through the API used by jContent itself.
    const openCreateForm = (nodeType: string) => {
        cy.visit(`/jahia/jcontent/${siteKey}/en/content-folders/contents`);
        cy.get('[data-sel-role="createContent"]').should('be.visible');
        gqlQuery(`query($path: String!) { jcr { nodeByPath(path: $path) { uuid } } }`, {path: contents}).then(result => {
            cy.window().then(win => win['CE_API'].create({
                uuid: result.data.jcr.nodeByPath.uuid,
                path: contents,
                lang: 'en',
                nodeTypes: [nodeType],
                isFullscreen: false
            }));
        });
        cy.get('[data-sel-role="createButton"]').should('be.visible');
    };

    const save = () => cy.get('[data-sel-role="createButton"]').click();

    // Children are read directly: a SQL2 query can miss a node that was saved a moment ago.
    const findCreated = (nodeType: string) => nodeProperties(contents, []).then(result =>
        result.data.jcr.nodeByPath.children.nodes
            .filter(child => child.primaryNodeType.name === nodeType)
            .map(child => `${contents}/${child.name}`));

    before(() => {
        setupTrainingSite(siteKey, ['en', 'fr']);
        publishAndWaitJobEnding(`/sites/${siteKey}/home`, ['en', 'fr']);
    });
    after(() => teardownTrainingSite(siteKey));

    beforeEach(() => cy.login());

    it('serves the live home page', () => {
        cy.request(`/sites/${siteKey}/home.html`).its('status').should('eq', 200);
    });

    it('lists the employee job titles and creates an employee', () => {
        openCreateForm('foont:employee');
        field('foont:employee_jobTitle').find('[role="dropdown"], div[class*="dropdown"]').first().click();
        cy.get('li[role="option"]').should(elems => {
            const values = elems.get().map(e => e.getAttribute('data-value'));
            expect(values.sort()).to.deep.eq(['developer', 'it_administrator', 'it_consultant', 'manager']);
        });
        cy.get('li[role="option"][data-value="developer"]').click();
        field('foont:employee_jcr:title').find('input').type('42');
        field('foont:employee_firstname').find('input').type('Ada');
        field('foont:employee_lastname').find('input').type('Lovelace');
        save();
        cy.waitUntil(() => findCreated('foont:employee').then(paths => paths.length === 1), {timeout: 10000, interval: 500});
        findCreated('foont:employee').then(([path]) => nodeProperties(path, ['firstname', 'lastname', 'jobTitle']).should(result => {
            const node = result.data.jcr.nodeByPath;
            expect(propertyValue(node, 'firstname')).to.eq('Ada');
            expect(propertyValue(node, 'lastname')).to.eq('Lovelace');
            expect(propertyValue(node, 'jobTitle')).to.eq('developer');
        }));
    });

    it('picks a color with the ColorPalette selector', () => {
        openCreateForm('foont:text');
        field('foomix:colorizable_color').find('button').click();
        // The module bundles its own Material-UI, whose class names are jss-generated, so the swatch is found by its color.
        cy.get('div[role="presentation"] div[style*="background-color: rgb(0, 255, 0)"]').should('be.visible').click();
        field('foomix:colorizable_color').find('button').should('contain.text', '#00FF00');
        cy.get('.cke_wysiwyg_frame, iframe.cke_wysiwyg_frame').should('exist').then($iframe => {
            const body = $iframe.contents().find('body');
            cy.wrap(body).click().type('colored text');
        });
        save();
        cy.waitUntil(() => findCreated('foont:text').then(paths => paths.length === 1), {timeout: 10000, interval: 500});
        findCreated('foont:text').then(([path]) => nodeProperties(path, ['color']).should(result =>
            expect(propertyValue(result.data.jcr.nodeByPath, 'color')).to.eq('#00FF00')));
    });

    it('switches a boolean with the ToggleBox selector', () => {
        openCreateForm('foont:choiceListOption');
        field('foont:choiceListOption_isDefaultValue').find('input[type="checkbox"]').click({force: true});
        field('foont:choiceListOption_value').find('input').type('option-value');
        save();
        cy.waitUntil(() => findCreated('foont:choiceListOption').then(paths => paths.length === 1), {timeout: 10000, interval: 500});
        findCreated('foont:choiceListOption').then(([path]) => nodeProperties(path, ['isDefaultValue', 'value']).should(result => {
            expect(propertyValue(result.data.jcr.nodeByPath, 'isDefaultValue')).to.eq('true');
            expect(propertyValue(result.data.jcr.nodeByPath, 'value')).to.eq('option-value');
        }));
    });
});
