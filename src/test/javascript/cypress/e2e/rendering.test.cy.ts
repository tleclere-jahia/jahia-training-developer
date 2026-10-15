import {publishAndWaitJobEnding} from "@jahia/cypress";
import {addChildOrFail, gqlQuery, setupTrainingSite, teardownTrainingSite} from "../support/training";

describe('Views and render filters of the training module', () => {
    const siteKey = 'trainingRendering';
    const sitePath = `/sites/${siteKey}`;
    const contents = `${sitePath}/contents`;
    const rootAuth = () => ({Authorization: `Basic ${btoa(`root:${Cypress.env('SUPER_USER_PASSWORD')}`)}`});

    const render = (path: string, view = 'default', templateType = 'html') => gqlQuery(`query($path: String!, $view: String, $templateType: String) {
        jcr { nodeByPath(path: $path) {
            renderedContent(view: $view, templateType: $templateType, contextConfiguration: "module", language: "en") { output }
        } }
    }`, {path, view, templateType}).then(result => {
        expect(result.errors, JSON.stringify(result.errors)).to.be.undefined;
        return result.data.jcr.nodeByPath.renderedContent.output as string;
    });

    const normalize = (html: string) => html.replace(/\s+/g, ' ');

    before(() => {
        setupTrainingSite(siteKey);
        addChildOrFail(contents, 'acme', 'foont:firm', [{name: 'jcr:title', language: 'en', value: 'ACME'}]).then(firm => {
            addChildOrFail(contents, 'company', 'foont:company', [{name: 'jcr:title', language: 'en', value: 'Training Company'}]);
            addChildOrFail(`${contents}/company`, 'ada', 'foont:employee', [
                {name: 'jcr:title', value: '42'},
                {name: 'firstname', value: 'Ada'},
                {name: 'lastname', value: 'Lovelace'},
                {name: 'jobTitle', value: 'developer'},
                {name: 'email', value: 'ada@example.org'},
                {name: 'phone', value: '+33102030405'},
                {name: 'biography', language: 'en', value: '<p>First programmer</p>'},
                {name: 'firm', value: firm.uuid}
            ]);
            addChildOrFail(`${contents}/company`, 'grace', 'foont:employee', [
                {name: 'jcr:title', value: '43'},
                {name: 'firstname', value: 'Grace'},
                {name: 'lastname', value: 'Hopper'},
                {name: 'firm', value: firm.uuid}
            ]);
        });
        addChildOrFail(contents, 'table', 'foont:table');
        addChildOrFail(`${contents}/table/header`, 'name', 'foont:tcell', [{name: 'type', value: 'foomix:tCellTextType'}], ['foomix:tCellTextType']);
        addChildOrFail(`${contents}/table/header`, 'age', 'foont:tcell', [{name: 'type', value: 'foomix:tCellLongType'}], ['foomix:tCellLongType']);
        addChildOrFail(`${contents}/table`, 'row', 'foont:trow');
        addChildOrFail(contents, 'dates', 'foont:simpleList', [
            {name: 'itemType', value: 'foomix:dateType'},
            {name: 'pattern', value: 'yyyy/MM/dd'}
        ], ['foomix:dateType']);
        addChildOrFail(`${contents}/dates`, 'birthday', 'foont:date', [{name: 'date', value: '1815-12-10T12:00:00.000Z'}]);
        addChildOrFail(contents, 'numbers', 'foont:simpleList', [{name: 'itemType', value: 'foomix:longType'}], ['foomix:longType']);
        addChildOrFail(`${contents}/numbers`, 'answer', 'foont:long', [{name: 'long', value: '42'}]);
        publishAndWaitJobEnding(sitePath, ['en']);
    });
    after(() => teardownTrainingSite(siteKey));

    describe('foont:employee', () => {
        const ada = `${contents}/company/ada`;

        it('renders the default view with the full name set by EmployeeNameFilter', () => {
            render(ada).then(output => {
                expect(normalize(output)).to.contain('Ada Lovelace (#42)');
                expect(normalize(output)).to.contain('Fullname: Ada Lovelace');
            });
        });

        it('renders the details view', () => {
            render(ada, 'details').then(output => {
                expect(output).to.contain('<dd>42</dd>');
                expect(output).to.contain('<dd>Ada</dd>');
                expect(output).to.contain('<dd>Lovelace</dd>');
                expect(output).to.contain('<p>First programmer</p>');
            });
        });

        // The /cms/render/...json URL answers 404 on Jahia 8.2.3, so the json template type is rendered through GraphQL.
        it('renders the json view', () => {
            render(ada, 'default', 'json').then(output => {
                const body = JSON.parse(output);
                expect(body).to.include({title: '42', firstname: 'Ada', lastname: 'Lovelace'});
                expect(body.properties).to.include({email: 'ada@example.org', jobTitle: 'developer'});
            });
        });

        it('renders the vcf view', () => {
            cy.request({url: `/cms/render/live/en${ada}.vcf`}).then(response => {
                const vcard = normalize(response.body);
                expect(vcard).to.contain('BEGIN:VCARD');
                expect(vcard).to.contain('FN:Ada Lovelace (42)');
                expect(vcard).to.contain('N:Lovelace;Ada');
                expect(vcard).to.contain('TITLE:Developer');
                expect(vcard).to.contain('EMAIL:ada@example.org');
                expect(vcard).to.contain('END:VCARD');
            });
        });
    });

    it('lists the employees of a company', () => {
        render(`${contents}/company`).then(output => {
            expect(output).to.contain('<h2>Training Company</h2>');
            expect(normalize(output)).to.contain('Ada Lovelace (#42)');
            expect(normalize(output)).to.contain('Grace Hopper (#43)');
        });
    });

    it('lists the employees referencing a firm', () => {
        render(`${contents}/acme`).then(output => {
            expect(output).to.contain('<h2>ACME</h2>');
            expect(normalize(output)).to.contain('Ada Lovelace (#42)');
            expect(normalize(output)).to.contain('Grace Hopper (#43)');
        });
    });

    it('renders a table with header and row cells', () => {
        render(`${contents}/table`).then(output => {
            const html = normalize(output);
            expect(html).to.match(/<thead>\s*<tr>\s*<th>name<\/th>\s*<th>age<\/th>\s*<\/tr>\s*<\/thead>/);
            expect(html).to.match(/<tr>\s*<td>cell-1<\/td>\s*<td>cell-2<\/td>\s*<\/tr>/);
        });
    });

    describe('foont:simpleList', () => {
        it('formats the dates with the pattern of the list', () => {
            render(`${contents}/dates`).then(output => expect(output).to.contain('1815/12/10'));
        });

        it('renders its items with the view of the item type', () => {
            render(`${contents}/numbers`).then(output => expect(output).to.match(/^\s*42\s*</));
        });
    });

    describe('HtmlGenerationInfoFilter', () => {
        it('adds the generation time before </body> on a site using the module', () => {
            cy.request({url: `/cms/render/default/en${sitePath}/home.html?ec=${Date.now()}`, headers: rootAuth()}).then(response => {
                expect(response.body).to.match(/<!-- Time ellapsed: \d+[.,]\d{4}s --><\/body>/);
            });
        });

        it('does not touch a page of a site without the module', () => {
            cy.request({url: `/cms/render/default/en/sites/systemsite/home.html?ec=${Date.now()}`, headers: rootAuth(), failOnStatusCode: false})
                .then(response => expect(response.body).not.to.contain('Time ellapsed'));
        });
    });
});
