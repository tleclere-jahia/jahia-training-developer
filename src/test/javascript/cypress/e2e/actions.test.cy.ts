import {createUser, deleteUser, grantRoles, publishAndWaitJobEnding} from "@jahia/cypress";
import {addChildOrFail, setupTrainingSite, teardownTrainingSite} from "../support/training";

describe('Render actions of the training module', () => {
    const siteKey = 'trainingActions';
    const contents = `/sites/${siteKey}/contents`;
    const editor = {name: 'trainingActionsEditor', password: 'trainingActions-editor-1'};
    const reader = {name: 'trainingActionsReader', password: 'trainingActions-reader-1'};

    const basicAuth = (user: { name: string, password: string }) => ({Authorization: `Basic ${btoa(`${user.name}:${user.password}`)}`});
    const root = {name: 'root', password: Cypress.env('SUPER_USER_PASSWORD')};

    const sayHi = (workspace: 'default' | 'live', user?: { name: string, password: string }, ajax = true) => cy.request({
        method: 'POST',
        url: `/cms/render/${workspace}/en${contents}/ada.hi.do`,
        form: true,
        body: ajax ? {ajax: 'true'} : {},
        headers: {Accept: 'application/json', ...(user ? basicAuth(user) : {})},
        followRedirect: false,
        failOnStatusCode: false
    });

    before(() => {
        setupTrainingSite(siteKey);
        addChildOrFail(contents, 'ada', 'foont:employee', [
            {name: 'jcr:title', value: '42'},
            {name: 'firstname', value: 'Ada'},
            {name: 'lastname', value: 'Lovelace'}
        ]);
        addChildOrFail(contents, 'form', 'foont:ajaxForm');
        publishAndWaitJobEnding(contents, ['en']);
        createUser(editor.name, editor.password);
        createUser(reader.name, reader.password);
        grantRoles(`/sites/${siteKey}`, ['editor'], editor.name, 'USER');
        grantRoles(`/sites/${siteKey}`, ['reader'], reader.name, 'USER');
    });
    after(() => {
        teardownTrainingSite(siteKey);
        deleteUser(editor.name);
        deleteUser(reader.name);
    });

    describe('hi.do (requires the sayHi permission)', () => {
        it('answers the employee names in JSON to root', () => {
            sayHi('default', root).then(response => {
                expect(response.status).to.eq(200);
                expect(response.body).to.deep.eq({firstname: 'Ada', lastname: 'Lovelace'});
            });
        });

        it('redirects to the site home without the ajax parameter', () => {
            sayHi('default', root, false).then(response => {
                expect(response.status).to.be.oneOf([301, 302, 303]);
                expect(response.headers.location).to.contain(`/sites/${siteKey}/home`);
            });
        });

        it('is granted to an editor through the module roles', () => {
            sayHi('live', editor).then(response => {
                expect(response.status).to.eq(200);
                expect(response.body.firstname).to.eq('Ada');
            });
        });

        it('is refused to a reader', () => {
            sayHi('live', reader).then(response => {
                expect(response.status).to.be.oneOf([401, 403]);
                expect(response.body).not.to.have.property('firstname');
            });
        });

        it('is refused to guest', () => {
            sayHi('live').then(response => {
                expect(response.status).to.be.oneOf([401, 403]);
                expect(response.body).not.to.have.property('firstname');
            });
        });
    });

    describe('ajaxForm.do', () => {
        it('accepts a guest submission', () => {
            cy.request({
                method: 'POST',
                url: `/cms/render/live/en${contents}/form.ajaxForm.do`,
                form: true,
                body: {message: 'hello'},
                headers: {Accept: 'application/json'},
                failOnStatusCode: false
            }).then(response => {
                expect(response.status).to.eq(200);
                expect(response.headers['content-type']).to.contain('application/json');
            });
        });
    });
});
