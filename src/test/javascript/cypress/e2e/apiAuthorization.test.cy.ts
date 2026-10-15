describe('API authorization of the training scope', () => {
    const graphql = (query: string, headers: Record<string, string> = {}) => cy.request({
        method: 'POST',
        url: '/modules/graphql',
        headers: {Origin: Cypress.config().baseUrl, ...headers},
        body: {query},
        failOnStatusCode: false
    });
    const rootAuth = () => ({Authorization: `Basic ${btoa(`root:${Cypress.env('SUPER_USER_PASSWORD')}`)}`});
    const jcrQuery = '{ jcr { nodeByPath(path: "/sites/systemsite") { name } } }';

    it('allows root to read the JCR through GraphQL', () => {
        graphql(jcrQuery, rootAuth()).then(response => {
            expect(response.body.errors).to.be.undefined;
            expect(response.body.data.jcr.nodeByPath.name).to.eq('systemsite');
        });
    });

    it('refuses the jcr root field to guest, even from a hosted origin', () => {
        graphql(jcrQuery).then(response => {
            expect(response.body.data?.jcr ?? null).to.be.null;
            expect(response.body.errors.map(e => e.message)).to.include('Permission denied');
        });
    });

    it('refuses the employee field to guest outside /sites/digitall', () => {
        graphql('{ employee(path: "/sites/systemsite/anything") { firstname } }').then(response => {
            expect(response.body.data?.employee ?? null).to.be.null;
            expect(response.body.errors.map(e => e.message)).to.include('Permission denied');
        });
    });
});
