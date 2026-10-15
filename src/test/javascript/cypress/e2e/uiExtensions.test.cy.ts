describe('UI extensions registered by the training module', () => {
    // Cypress resets the page and the cookies between tests, so every test opens the app again.
    beforeEach(() => {
        cy.login();
        cy.visit('/jahia/dashboard');
        cy.window().its('jahia.uiExtender.registry').should('exist');
        // The module callback runs on jahiaApp-init, wait for its last registration.
        cy.window().should(win => expect(win['jahia'].uiExtender.registry.get('action', '3dotsAutoSplitDialog')).to.exist);
    });

    // A then() callback returning undefined yields the previous subject, hence the explicit null.
    const registryEntry = (type: string, key: string) => cy.window().then(win => win['jahia'].uiExtender.registry.get(type, key) ?? null);

    it('registers the ColorPalette and ToggleBox selector types', () => {
        registryEntry('selectorType', 'ColorPalette').should(entry => {
            expect(entry.dataType).to.deep.eq(['String']);
            expect(entry.supportMultiple).to.be.true;
        });
        registryEntry('selectorType', 'ToggleBox').should(entry => {
            expect(entry.dataType).to.deep.eq(['Boolean']);
            expect(entry.adaptValue({multiple: false}, {value: 'true'})).to.be.true;
            expect(entry.adaptValue({multiple: true}, {values: ['true', 'false']})).to.deep.eq([true, false]);
            expect(entry.initValue({mandatory: true, multiple: false})).to.be.false;
            expect(entry.initValue({mandatory: false, multiple: false})).to.be.undefined;
        });
    });

    it('registers the content actions', () => {
        registryEntry('action', '3dotsAutoSplitDialog').should(entry => {
            expect(entry.targets.map(t => t.id)).to.include('contentActions');
            expect(entry.showOnNodeTypes).to.deep.eq(['jnt:contentFolder']);
        });
        registryEntry('action', '3dotsNodeInfoDialog').should(entry =>
            expect(entry.targets.map(t => t.id)).to.include('content-editor/header/3dots'));
    });

    it('registers the tools entries and the custom jContent route', () => {
        registryEntry('primary-nav-item', 'toolsEntry').should(entry => expect(entry.path).to.eq('/tools'));
        registryEntry('route', 'toolsRoute').should(entry => expect(entry.requiredPermission).to.eq('systemToolsAccess'));
        registryEntry('adminRoute', 'myComponent').should(entry => expect(entry.targets.map(t => t.id)).to.include('jcontent'));
    });

    it('removes the files admin route', () => {
        registryEntry('adminRoute', 'files').should('be.null');
    });

    it('shows the tools entry in the primary navigation', () => {
        // The labels of a collapsed primary navigation are not visible, so only their presence is checked.
        cy.get('.moonstone-primaryNav').contains('.moonstone-primaryNavItem_label', 'Tools').should('exist');
    });
});
