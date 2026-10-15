import {gqlQuery, setupTrainingSite, teardownTrainingSite} from "../support/training";

type Field = { name: string, selectorType: string, visible: boolean, readOnly: boolean };
type FieldSet = { name: string, fields: Field[] };
type Section = { name: string, visible: boolean, fieldSets: FieldSet[] };

describe('Content editor form overrides', () => {
    const siteKey = 'trainingForms';
    const contents = `/sites/${siteKey}/contents`;

    const createForm = (primaryNodeType: string) => gqlQuery(`query($type: String!, $parent: String!) {
        forms { createForm(primaryNodeType: $type, uiLocale: "en", locale: "en", uuidOrPath: $parent) {
            sections { name visible fieldSets { name fields { name selectorType visible readOnly } } }
        } }
    }`, {type: primaryNodeType, parent: contents}).then(result => {
        expect(result.errors, JSON.stringify(result.errors)).to.be.undefined;
        return result.data.forms.createForm.sections as Section[];
    });

    const findField = (sections: Section[], name: string): Field =>
        sections.flatMap(s => s.fieldSets).flatMap(fs => fs.fields).find(f => f.name === name);

    before(() => setupTrainingSite(siteKey));
    after(() => teardownTrainingSite(siteKey));

    it('uses the ColorPalette selector for foomix:colorizable', () => {
        createForm('foont:text').then(sections => expect(findField(sections, 'color').selectorType).to.eq('ColorPalette'));
    });

    it('hides the system name of foont:text', () => {
        createForm('foont:text').then(sections => {
            const systemName = findField(sections, 'ce:systemName');
            expect(systemName === undefined || systemName.visible === false, 'ce:systemName hidden').to.be.true;
        });
    });

    it('uses the ToggleBox selector for foont:choiceListOption', () => {
        createForm('foont:choiceListOption').then(sections => expect(findField(sections, 'isDefaultValue').selectorType).to.eq('ToggleBox'));
    });

    it('moves the description of foont:employee into its own field set', () => {
        createForm('foont:employee').then(sections => {
            const content = sections.find(s => s.name === 'content');
            const employeeFields = content.fieldSets.find(fs => fs.name === 'foont:employee').fields.map(f => f.name);
            expect(employeeFields).to.include('jcr:description');
            expect(employeeFields.indexOf('jcr:description')).to.be.lessThan(employeeFields.indexOf('firstname'));
        });
    });
});
