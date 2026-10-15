import {addChildOrFail, gqlQuery, setupTrainingSite, teardownTrainingSite} from "../support/training";

type Constraint = { value: { string: string }, displayValue: string, properties: { name: string, value: string }[] };

describe('Choicelist initializers', () => {
    const siteKey = 'trainingChoicelists';
    const contents = `/sites/${siteKey}/contents`;

    const fieldConstraints = (args: {
        primaryNodeType: string,
        fieldName: string,
        fieldNodeType?: string,
        parentPath?: string,
        nodePath?: string,
        context?: { key: string, value: string }[]
    }) => gqlQuery(`query($parent: String!, $node: String, $type: String!, $fieldType: String!, $field: String!, $context: [InputContextEntryInput]) {
        forms {
            fieldConstraints(nodeUuidOrPath: $node, parentNodeUuidOrPath: $parent, primaryNodeType: $type, fieldNodeType: $fieldType,
                             fieldName: $field, context: $context, uiLocale: "en", locale: "en") {
                value { string }
                displayValue
                properties { name value }
            }
        }
    }`, {
        parent: args.parentPath || contents,
        node: args.nodePath,
        type: args.primaryNodeType,
        fieldType: args.fieldNodeType || args.primaryNodeType,
        field: args.fieldName,
        context: args.context || []
    }).then(result => {
        expect(result.errors, JSON.stringify(result.errors)).to.be.undefined;
        return result.data.forms.fieldConstraints as Constraint[];
    });

    const values = (constraints: Constraint[]) => constraints.map(c => c.value.string);
    const property = (constraint: Constraint, name: string) => constraint.properties.find(p => p.name === name)?.value;

    before(() => setupTrainingSite(siteKey));
    after(() => teardownTrainingSite(siteKey));

    describe('Dependent region and city on foont:myComponentLocalizable', () => {
        const region = (country: string) => fieldConstraints({
            primaryNodeType: 'foont:myComponentLocalizable', fieldName: 'region',
            context: [{key: 'country', value: country}]
        });
        const city = (country: string, region: string) => fieldConstraints({
            primaryNodeType: 'foont:myComponentLocalizable', fieldName: 'city',
            context: [{key: 'country', value: country}, {key: 'region', value: region}]
        });

        [
            {country: 'france', regions: ['ile-de-france', 'bretagne', 'alsace']},
            {country: 'england', regions: ['dorset', 'gloucestershire', 'hampshire']},
            {country: 'deutschland', regions: ['hessen', 'bavaria', 'saxony']}
        ].forEach(({country, regions}) => it(`lists the regions of ${country}`, () => {
            region(country).then(constraints => expect(values(constraints)).to.deep.eq(regions));
        }));

        it('lists the cities of a region', () => {
            city('france', 'bretagne').then(constraints => expect(values(constraints)).to.deep.eq(['renne', 'lancieux', 'dinard']));
            city('deutschland', 'bavaria').then(constraints => expect(values(constraints)).to.deep.eq(['nuremberg', 'munich', 'augsbourg']));
        });

        it('lists nothing for an unknown country', () => {
            region('spain').then(constraints => expect(constraints).to.be.empty);
        });
    });

    describe('addMixin initializers', () => {
        it('maps each card type to its template mixin', () => {
            fieldConstraints({primaryNodeType: 'foont:card', fieldName: 'type'}).then(constraints => {
                expect(constraints.map(c => [c.value.string, property(c, 'addMixin')])).to.deep.eq([
                    ['default', 'foomix:cardDefault'],
                    ['type1', 'foomix:cardType1'],
                    ['type2', 'foomix:cardType2']
                ]);
            });
        });

        it('maps each simple list item type to its mixin and translated label', () => {
            fieldConstraints({primaryNodeType: 'foont:simpleList', fieldName: 'itemType'}).then(constraints => {
                expect(constraints.map(c => [c.value.string, property(c, 'addMixin'), c.displayValue])).to.deep.eq([
                    ['foomix:textType', 'foomix:textType', 'Text'],
                    ['foomix:longType', 'foomix:longType', 'Number'],
                    ['foomix:dateType', 'foomix:dateType', 'Date'],
                    ['foomix:datetimeType', 'foomix:datetimeType', 'Datetime']
                ]);
            });
        });
    });

    describe('cellTypeInitializer on foont:tcell', () => {
        const table = `${contents}/table`;

        before(() => {
            addChildOrFail(contents, 'table', 'foont:table');
            addChildOrFail(`${table}/header`, 'head-1', 'foont:tcell', [{name: 'type', value: 'foomix:tCellLongType'}], ['foomix:tCellLongType']);
            addChildOrFail(`${table}/header`, 'head-2', 'foont:tcell', [{name: 'type', value: 'foomix:tCellDateType'}], ['foomix:tCellDateType']);
            addChildOrFail(table, 'row', 'foont:trow');
        });

        const defaultType = (constraints: Constraint[]) => constraints.filter(c => property(c, 'defaultProperty') === 'true').map(c => c.value.string);

        it('defaults an existing cell to the type of its header cell', () => {
            fieldConstraints({primaryNodeType: 'foont:tcell', fieldName: 'type', parentPath: `${table}/row`, nodePath: `${table}/row/cell-2`})
                .then(constraints => {
                    expect(values(constraints)).to.have.length(5);
                    expect(defaultType(constraints)).to.deep.eq(['foomix:tCellDateType']);
                });
        });

        it('offers every cell type with its mixin', () => {
            fieldConstraints({primaryNodeType: 'foont:tcell', fieldName: 'type', parentPath: `${table}/row`, nodePath: `${table}/row/cell-1`})
                .then(constraints => {
                    expect(constraints.every(c => property(c, 'addMixin') === c.value.string)).to.be.true;
                    expect(defaultType(constraints)).to.deep.eq(['foomix:tCellLongType']);
                });
        });
    });

    describe('foont:employee fields', () => {
        it('translates the job titles', () => {
            fieldConstraints({primaryNodeType: 'foont:employee', fieldName: 'jobTitle'}).then(constraints => {
                expect(constraints.map(c => [c.value.string, c.displayValue])).to.deep.eq([
                    ['developer', 'Developer'],
                    ['it_administrator', 'IT Administrator'],
                    ['it_consultant', 'IT Consultant'],
                    ['manager', 'Manager']
                ]);
            });
        });

        it('excludes the edited employee from the managers', () => {
            const employee = (name: string) => addChildOrFail(contents, name, 'foont:employee', [
                {name: 'jcr:title', value: name}, {name: 'firstname', value: name}, {name: 'lastname', value: 'Doe'}
            ]);
            employee('alice').then(alice => employee('bob').then(bob => {
                fieldConstraints({primaryNodeType: 'foont:employee', fieldName: 'manager', nodePath: `${contents}/alice`})
                    .then(constraints => {
                        expect(values(constraints)).to.include(bob.uuid);
                        expect(values(constraints)).not.to.include(alice.uuid);
                    });
            }));
        });

        it('lists the sub categories of the selected category', () => {
            gqlQuery(`{ jcr { nodeByPath(path: "/sites/systemsite/categories/training") { uuid children { nodes { uuid name } } } } }`)
                .then(result => {
                    const category = result.data.jcr.nodeByPath;
                    fieldConstraints({
                        primaryNodeType: 'foont:employee', fieldName: 'subcategory',
                        context: [{key: 'category', value: category.uuid}]
                    }).then(constraints => {
                        expect(values(constraints)).to.deep.eq(category.children.nodes.map(c => c.uuid));
                    });
                });
        });
    });

    describe('FirstDayOfWeekInitializer', () => {
        it('defaults foont:date to the monday of the current week', () => {
            gqlQuery(`query($parent: String!) {
                forms { createForm(primaryNodeType: "foont:date", uiLocale: "en", locale: "en", uuidOrPath: $parent) {
                    sections { fieldSets { fields { name defaultValues { string } } } }
                } }
            }`, {parent: contents}).then(result => {
                const fields = result.data.forms.createForm.sections.flatMap(s => s.fieldSets).flatMap(fs => fs.fields);
                const date = new Date(fields.find(f => f.name === 'date').defaultValues[0].string);
                const today = new Date();
                const monday = Date.UTC(today.getUTCFullYear(), today.getUTCMonth(), today.getUTCDate() - ((today.getUTCDay() + 6) % 7));
                // The initializer computes monday 00:00 UTC, the form returns it shifted by the server time zone.
                const maxTimeZoneOffset = 14 * 3600 * 1000;
                expect(Math.abs(date.getTime() - monday), `${date.toISOString()} is monday of the current week`).to.be.at.most(maxTimeZoneOffset);
            });
        });
    });
});
