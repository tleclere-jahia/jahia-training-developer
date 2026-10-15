import {addChild, expectServerRejection, setupTrainingSite, teardownTrainingSite} from "../support/training";

describe('Html JCR Validator Test', () => {
    const siteKey = 'trainingHtmlValidator';
    const contents = `/sites/${siteKey}/contents`;

    before(() => setupTrainingSite(siteKey));
    after(() => teardownTrainingSite(siteKey));

    it('accepts a well-formed rich text', () => {
        addChild(contents, 'valid-html', 'foont:text', [{name: 'text', value: '<span>blabla</span>'}]).should(result => {
            expect(result.errors).to.be.undefined;
            expect(result.data.jcr.addNode.uuid).to.be.a('string');
        });
    });

    it('rejects a rich text with an unclosed tag', () => {
        addChild(contents, 'unclosed-html', 'foont:text', [{name: 'text', value: '<span>blabla'}])
            .should(result => expectServerRejection(result, 'text'));
    });

    it('validates the rich text of a table cell too', () => {
        addChild(contents, 'table', 'foont:table').should(result => expect(result.errors).to.be.undefined);
        addChild(`${contents}/table/header`, 'bad-cell', 'foont:tcell', [
            {name: 'type', value: 'foomix:tCellTextType'},
            {name: 'value', value: '<p>unclosed'}
        ], ['foomix:tCellTextType']).should(result => expectServerRejection(result, 'value'));
    });
});
