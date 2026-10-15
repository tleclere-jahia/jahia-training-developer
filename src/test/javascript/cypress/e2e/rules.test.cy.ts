import {publishAndWaitJobEnding} from "@jahia/cypress";
import {
    addChildOrFail,
    gqlMutation,
    nodeProperties,
    propertyValue,
    setupTrainingSite,
    teardownTrainingSite
} from "../support/training";

describe('Drools rules and publication listener', () => {
    const siteKey = 'trainingRules';
    const sitePath = `/sites/${siteKey}`;
    const contents = `${sitePath}/contents`;

    before(() => setupTrainingSite(siteKey));
    after(() => teardownTrainingSite(siteKey));

    describe('foont:table', () => {
        it('creates nbRows rows when a table is created', () => {
            addChildOrFail(contents, 'table-with-rows', 'foont:table', [{name: 'nbRows', value: '3'}]);
            nodeProperties(`${contents}/table-with-rows`, []).should(result => {
                const children = result.data.jcr.nodeByPath.children.nodes;
                expect(children.filter(c => c.primaryNodeType.name === 'foont:trow').map(c => c.name))
                    .to.deep.eq(['trow-1', 'trow-2', 'trow-3']);
                expect(children.filter(c => c.primaryNodeType.name === 'foont:thead').map(c => c.name)).to.deep.eq(['header']);
            });
        });

        it('creates one text cell per header cell when a row is added', () => {
            const table = `${contents}/table-with-header`;
            addChildOrFail(contents, 'table-with-header', 'foont:table');
            addChildOrFail(`${table}/header`, 'head-1', 'foont:tcell', [{name: 'type', value: 'foomix:tCellLongType'}], ['foomix:tCellLongType']);
            addChildOrFail(`${table}/header`, 'head-2', 'foont:tcell', [{name: 'type', value: 'foomix:tCellBooleanType'}], ['foomix:tCellBooleanType']);
            addChildOrFail(table, 'row', 'foont:trow');
            nodeProperties(`${table}/row`, []).should(result => {
                const cells = result.data.jcr.nodeByPath.children.nodes;
                expect(cells.map(c => c.name)).to.deep.eq(['cell-1', 'cell-2']);
                cells.forEach(cell => {
                    expect(cell.primaryNodeType.name).to.eq('foont:tcell');
                    expect(cell.mixinTypes.map(m => m.name)).to.include('foomix:tCellTextType');
                });
            });
            nodeProperties(`${table}/row/cell-1`, ['type']).should(result =>
                expect(propertyValue(result.data.jcr.nodeByPath, 'type')).to.eq('foomix:tCellTextType'));
        });
    });

    describe('foont:card', () => {
        const card = `${contents}/card`;

        it('sets j:view and adds jmix:renderable when the view property is set', () => {
            addChildOrFail(contents, 'card', 'foont:card', [{name: 'view', value: 'image'}]);
            nodeProperties(card, ['view', 'j:view']).should(result => {
                const node = result.data.jcr.nodeByPath;
                expect(node.mixinTypes.map(m => m.name)).to.include('jmix:renderable');
                expect(propertyValue(node, 'j:view')).to.eq('image');
            });
        });

        it('removes jmix:renderable when the view property is removed', () => {
            gqlMutation(`mutation($path: String!) {
                jcr { mutateNode(pathOrId: $path) { mutateProperty(name: "view") { delete } } }
            }`, {path: card}).should(result => expect(result.errors).to.be.undefined);
            nodeProperties(card, ['j:view']).should(result => {
                const node = result.data.jcr.nodeByPath;
                expect(node.mixinTypes.map(m => m.name)).not.to.include('jmix:renderable');
                expect(propertyValue(node, 'j:view')).to.be.undefined;
            });
        });
    });

    describe('Publication of a foomix:simpleItem', () => {
        const list = `${contents}/numbered-list`;

        before(() => {
            addChildOrFail(contents, 'numbered-list', 'foont:simpleList', [{name: 'itemType', value: 'foomix:textType'}], ['foomix:textType']);
            addChildOrFail(list, 'first', 'foont:text', [{name: 'text', language: 'en', value: '<p>first</p>'}]);
            addChildOrFail(list, 'second', 'foont:text', [{name: 'text', language: 'en', value: '<p>second</p>'}]);
            publishAndWaitJobEnding(list, ['en']);
        });

        it('increments the site counter once per published item', () => {
            nodeProperties(`${sitePath}/counter`, ['count']).should(result => {
                expect(result.errors).to.be.undefined;
                expect(Number(propertyValue(result.data.jcr.nodeByPath, 'count'))).to.be.at.least(2);
            });
        });

        it('stores a distinct item number on every published item', () => {
            const itemNumbers = [];
            ['first', 'second'].forEach(name => nodeProperties(`${list}/${name}`, ['itemNumber'], 'en', 'LIVE').then(result => {
                const itemNumber = propertyValue(result.data.jcr.nodeByPath, 'itemNumber');
                expect(itemNumber, `itemNumber of ${name}`).to.match(/^\d+$/);
                itemNumbers.push(itemNumber);
            }));
            cy.wrap(itemNumbers).should(numbers => expect(new Set(numbers).size).to.eq(2));
        });
    });

    describe('SamplePublicationListener', () => {
        const node = `${contents}/tracked`;
        let firstPublicationDate: string;

        before(() => {
            addChildOrFail(contents, 'tracked', 'foont:text', [{name: 'text', language: 'en', value: '<p>tracked</p>'}], ['foomix:trainingNodetypes']);
            publishAndWaitJobEnding(node, ['en']);
        });

        it('sets the first publication date in both workspaces', () => {
            ['EDIT', 'LIVE'].forEach((workspace: 'EDIT' | 'LIVE') => nodeProperties(node, ['firstPublicationDate'], 'en', workspace).then(result => {
                const tracked = result.data.jcr.nodeByPath;
                expect(tracked.mixinTypes.map(m => m.name), workspace).to.include('foomix:publicationMetaData');
                expect(propertyValue(tracked, 'firstPublicationDate'), workspace).to.be.a('string');
                if (workspace === 'EDIT') {
                    firstPublicationDate = propertyValue(tracked, 'firstPublicationDate');
                }
            }));
        });

        it('keeps the first publication date on the next publication', () => {
            gqlMutation(`mutation($path: String!) {
                jcr { mutateNode(pathOrId: $path) { mutateProperty(name: "text") { setValue(language: "en", value: "<p>updated</p>") } } }
            }`, {path: node});
            publishAndWaitJobEnding(node, ['en']);
            nodeProperties(node, ['firstPublicationDate'], 'en', 'LIVE').should(result =>
                expect(propertyValue(result.data.jcr.nodeByPath, 'firstPublicationDate')).to.eq(firstPublicationDate));
        });

        it('ignores nodes without foomix:trainingNodetypes', () => {
            addChildOrFail(contents, 'untracked', 'foont:text', [{name: 'text', language: 'en', value: '<p>untracked</p>'}]);
            publishAndWaitJobEnding(`${contents}/untracked`, ['en']);
            nodeProperties(`${contents}/untracked`, ['firstPublicationDate'], 'en', 'LIVE').should(result => {
                expect(result.data.jcr.nodeByPath.mixinTypes.map(m => m.name)).not.to.include('foomix:publicationMetaData');
            });
        });
    });
});
