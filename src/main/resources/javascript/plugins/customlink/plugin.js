CKEDITOR.plugins.add('customlink', {
    requires: 'dialog',
    onLoad: function() {
        // Add the CSS styles for anchor placeholders.
        var iconPath = CKEDITOR.getUrl( this.path + 'images' + ( CKEDITOR.env.hidpi ? '/hidpi' : '' ) + '/anchor.png' ),
            baseStyle = 'background:url(' + iconPath + ') no-repeat %1 center;border:1px dotted #00f;background-size:16px;';

        var template = '.%2 a.cke_anchor,' +
            '.%2 a.cke_anchor_empty' +
            ',.cke_editable.%2 a[name]' +
            ',.cke_editable.%2 a[data-cke-saved-name]' +
            '{' +
            baseStyle +
            'padding-%1:18px;' +
            // Show the arrow cursor for the anchor image (FF at least).
            'cursor:auto;' +
            '}' +
            '.%2 img.cke_anchor' +
            '{' +
            baseStyle +
            'width:16px;' +
            'min-height:15px;' +
            // The default line-height on IE.
            'height:1.15em;' +
            // Opera works better with "middle" (even if not perfect)
            'vertical-align:text-bottom;' +
            '}';

        // Styles with contents direction awareness.
        function cssWithDir( dir ) {
            return template.replace( /%1/g, dir == 'rtl' ? 'right' : 'left' ).replace( /%2/g, 'cke_contents_' + dir );
        }

        CKEDITOR.addCss( cssWithDir( 'ltr' ) + cssWithDir( 'rtl' ) );
    },
    init: function (editor) {
        var allowed = 'a[!href]', required = 'a[href]';
        // Add the link and unlink buttons.
        editor.addCommand('lier', new CKEDITOR.dialogCommand('lier', {
            allowedContent: allowed,
            requiredContent: required
        }));
        CKEDITOR.dialog.add('lier', function (editor) {
            var plugin = CKEDITOR.plugins.customlink,
                initialLinkText;

            function createRangeForLink(editor, link) {
                var range = editor.createRange();
                range.setStartBefore(link);
                range.setEndAfter(link);
                return range;
            }

            function insertLinksIntoSelection(editor, data) {
                var attributes = plugin.getLinkAttributes(editor, data),
                    ranges = editor.getSelection().getRanges(),
                    style = new CKEDITOR.style({
                        element: 'a',
                        attributes: attributes.set
                    }),
                    rangesToSelect = [],
                    range,
                    text,
                    nestedLinks,
                    i,
                    j;

                style.type = CKEDITOR.STYLE_INLINE; // need to override... dunno why.

                for (i = 0; i < ranges.length; i++) {
                    range = ranges[i];

                    // Use link URL as text with a collapsed cursor.
                    if (range.collapsed) {
                        // Short mailto link text view (https://dev.ckeditor.com/ticket/5736).
                        text = new CKEDITOR.dom.text(data.linkText || (data.type == 'email' ?
                            data.email.address : attributes.set['data-cke-saved-href']), editor.document);
                        range.insertNode(text);
                        range.selectNodeContents(text);
                    } else if (initialLinkText !== data.linkText) {
                        text = new CKEDITOR.dom.text(data.linkText, editor.document);

                        // Shrink range to preserve block element.
                        range.shrink(CKEDITOR.SHRINK_TEXT);

                        // Use extractHtmlFromRange to remove markup within the selection. Also this method is a little
                        // smarter than range#deleteContents as it plays better e.g. with table cells.
                        editor.editable().extractHtmlFromRange(range);

                        range.insertNode(text);
                    }

                    // Editable links nested within current range should be removed, so that the link is applied to whole selection.
                    nestedLinks = range._find('a');

                    for (j = 0; j < nestedLinks.length; j++) {
                        nestedLinks[j].remove(true);
                    }


                    // Apply style.
                    style.applyToRange(range, editor);

                    rangesToSelect.push(range);
                }

                editor.getSelection().selectRanges(rangesToSelect);
            }

            function editLinksInSelection(editor, selectedElements, data) {
                var attributes = plugin.getLinkAttributes(editor, data),
                    ranges = [],
                    element,
                    href,
                    textView,
                    newText,
                    i;

                for (i = 0; i < selectedElements.length; i++) {
                    // We're only editing an existing link, so just overwrite the attributes.
                    element = selectedElements[i];
                    href = element.data('cke-saved-href');
                    textView = element.getHtml();

                    element.setAttributes(attributes.set);
                    element.removeAttributes(attributes.removed);


                    if (data.linkText && initialLinkText != data.linkText) {
                        // Display text has been changed.
                        newText = data.linkText;
                    } else if (href == textView || data.type == 'email' && textView.indexOf('@') != -1) {
                        // Update text view when user changes protocol (https://dev.ckeditor.com/ticket/4612).
                        // Short mailto link text view (https://dev.ckeditor.com/ticket/5736).
                        newText = data.type == 'email' ? data.email.address : attributes.set['data-cke-saved-href'];
                    }

                    if (newText) {
                        element.setText(newText);
                    }

                    ranges.push(createRangeForLink(editor, element));
                }

                // We changed the content, so need to select it again.
                editor.getSelection().selectRanges(ranges);
            }

            var commonLang = editor.lang.common, linkLang = editor.lang.link;

            return {
                title: linkLang.title,
                minWidth: (CKEDITOR.skinName || editor.config.skin) == 'moono-lisa' ? 450 : 350,
                minHeight: 240,
                contents: [{
                    id: 'info',
                    label: linkLang.info,
                    title: linkLang.info,
                    elements: [{
                        type: 'text',
                        id: 'linkDisplayText',
                        label: linkLang.displayText,
                        setup: function () {
                            this.enable();

                            this.setValue(editor.getSelection().getSelectedText());

                            // Keep inner text so that it can be compared in commit function. By obtaining value from getData()
                            // we get value stripped from new line chars which is important when comparing the value later on.
                            initialLinkText = this.getValue();
                        },
                        commit: function (data) {
                            data.linkText = this.isEnabled() ? this.getValue() : '';
                        }
                    },
                        {
                            id: 'linkType',
                            type: 'select',
                            label: linkLang.type,
                            items: [
                                ['Lien interne', 'internalLink'],
                                ['Lien externe', 'externalLink'],
                                ['Lien de service', 'serviceLink'],
                                ['Ancre', 'anchorLink']
                            ],
                            onChange: function () {
                                var dialog = this.getDialog(),
                                    partIds = ['internalLinkOptions', 'externalLinkOptions', 'serviceLinkOptions','anchorLinkOptions'];

                                for (var i = 0; i < partIds.length; i++) {
                                    var element = dialog.getContentElement('info', partIds[i]);
                                    if (!element) {
                                        continue;
                                    }

                                    element = element.getElement().getParent().getParent();
                                    if (partIds[i] == this.getValue() + 'Options') {
                                        element.show();
                                    } else {
                                        element.hide();
                                    }
                                }

                                if (this.getValue() !== 'externalLink') {
                                    dialog.getContentElement('info', 'url').getInputElement().setAttribute('readOnly', true);
                                }
                                else if (this.getValue() === 'anchorLink') {
                                    dialog.getContentElement('info', 'url').remove();
                                }
                                else {
                                    dialog.getContentElement('info', 'url').getInputElement().removeAttribute('readOnly');
                                }
                                dialog.layout();
                            },
                            setup: function (data) {
                                this.setValue(data.type || 'internalLink');
                            },
                            commit: function (data) {
                                data.type = this.getValue();
                            }
                        },
                        {
                            id: 'url',
                            type: 'text',
                            label: commonLang.url,
                            required: true,
                            setup: function (data) {
                                if (data.url) {
                                    this.setValue(data.url);
                                }
                            },
                            commit: function (data) {
                                data.url = this.getValue();
                                if (data.type == 'externalLink' && (/^mailto:/.test(data.url)) ) {
                                    data.url = data.url;
                                }
                                else if (data.type == 'externalLink' && !/^(?:f|ht)tps?\:\/\//.test(data.url)) {
                                    data.url = "http://" + data.url;
                                }

                            }
                        },
                        {
                            type: 'vbox',
                            id: 'internalLinkOptions',
                            children: [{
                                type: 'vbox',
                                children: [{
                                    type: 'hbox',
                                    align: 'center',
                                    children: [{
                                        type: 'button',
                                        id: 'browse',
                                        style: 'float:right',
                                        hidden: 'true',
                                        filebrowser: {
                                            action: 'Browse',
                                            url: editor.config.filebrowserLinkBrowseUrl,
                                            target: 'info:url'
                                        },
                                        label: commonLang.browseServer +
                                            ' (' + (commonLang.browseServerPages || 'Content') + ')'
                                    }, {
                                        type: 'button',
                                        id: 'browseFiles',
                                        style: 'float:left',
                                        hidden: 'true',
                                        filebrowser: {
                                            action: 'Browse',
                                            url: editor.config.filebrowserBrowseUrl,
                                            target: 'info:url'
                                        },
                                        label: commonLang.browseServer + ' (' + (commonLang.browseServerFiles || 'Files') + ')'
                                    }]
                                }]
                            }]
                        },
                        {
                            type: 'vbox',
                            id: 'externalLinkOptions',
                            children: []
                        },
                        {
                            type: 'vbox',
                            id: 'serviceLinkOptions',
                            children: [{
                                type: 'hbox',
                                align: 'center',
                                children: [{
                                    type: 'button',
                                    id: 'browse',
                                    style: 'float:right',
                                    hidden: 'true',
                                    filebrowser: {
                                        action: 'Browse',
                                        url: editor.config.lienserviceBrowseUrl,
                                        target: 'info:url'
                                    },
                                    label: commonLang.browseServer
                                }]
                            }, {
                                id: 'endUrl',
                                type: 'text',
                                label: 'Fin d\'URL de service',
                                required: true,
                                setup: function (data) {
                                    if (data.endUrl) {
                                        this.setValue(data.endUrl);
                                    }
                                },
                                commit: function (data) {
                                    data.endUrl = this.getValue();
                                }
                            }]
                        },
                        {
                            type: 'vbox',
                            id: 'anchorLinkOptions',
                            width: 260,
                            align: 'center',
                            padding: 0,
                            children: [ {
                                type: 'fieldset',
                                id: 'selectAnchorText',
                                label: linkLang.selectAnchor,
                                setup: function() {
                                    anchors = plugin.getEditorAnchors( editor );

                                    this.getElement()[ anchors && anchors.length ? 'show' : 'hide' ]();
                                },
                                children: [ {
                                    type: 'hbox',
                                    id: 'selectAnchor',
                                    children: [ {
                                        type: 'select',
                                        id: 'anchorName',
                                        'default': '',
                                        label: linkLang.anchorName,
                                        style: 'width: 100%;',
                                        items: [
                                            [ '' ]
                                        ],
                                        setup: function( data ) {
                                            this.clear();
                                            this.add( '' );

                                            if ( anchors ) {
                                                for ( var i = 0; i < anchors.length; i++ ) {
                                                    if ( anchors[ i ].name ) {
                                                        this.add( anchors[ i ].name );
                                                    }
                                                }
                                            }

                                            if ( data.anchor ) {
                                                this.setValue( data.anchor.name );
                                            }

                                            var linkType = this.getDialog().getContentElement( 'info', 'linkType' );
                                            if ( linkType && linkType.getValue() == 'email' ) {
                                                this.focus();
                                            }
                                        },
                                        commit: function( data ) {
                                            if ( !data.anchor ) {
                                                data.anchor = {};
                                            }

                                            data.anchor.name = this.getValue();
                                        }
                                    },
                                        {
                                            type: 'select',
                                            id: 'anchorId',
                                            'default': '',
                                            label: linkLang.anchorId,
                                            style: 'width: 100%;',
                                            items: [
                                                [ '' ]
                                            ],
                                            setup: function( data ) {
                                                this.clear();
                                                this.add( '' );

                                                if ( anchors ) {
                                                    for ( var i = 0; i < anchors.length; i++ ) {
                                                        if ( anchors[ i ].id ) {
                                                            this.add( anchors[ i ].id );
                                                        }
                                                    }
                                                }

                                                if ( data.anchor ) {
                                                    this.setValue( data.anchor.id );
                                                }
                                            },
                                            commit: function( data ) {
                                                if ( !data.anchor ) {
                                                    data.anchor = {};
                                                }

                                                data.anchor.id = this.getValue();
                                            }
                                        } ],
                                    setup: function() {
                                        this.getElement()[ anchors && anchors.length ? 'show' : 'hide' ]();
                                    }
                                } ]
                            },
                                {
                                    type: 'html',
                                    id: 'noAnchors',
                                    style: 'text-align: center;',
                                    html: '<div role="note" tabIndex="-1">' + CKEDITOR.tools.htmlEncode( linkLang.noAnchors ) + '</div>',
                                    // Focus the first element defined in above html.
                                    focus: true,
                                    setup: function() {
                                        this.getElement()[ anchors && anchors.length ? 'hide' : 'show' ]();
                                    }
                                } ],
                            setup: function() {
                                if ( !this.getDialog().getContentElement( 'info', 'linkType' ) ) {
                                    this.getElement().hide();
                                }
                            }
                        },
                        {
                            id: 'target',
                            requiredContent: 'a[target]',
                            type: 'checkbox',
                            label: commonLang.targetNew,
                            setup: function (data) {
                                if (data.target) {
                                    this.setValue(data.target == '_blank');
                                }
                            },
                            commit: function (data) {
                                data.target = this.getValue() ? '_blank' : '';
                            }
                        }]
                }],
                onShow: function () {
                    var editor = this.getParentEditor(),
                        selection = editor.getSelection(),
                        displayTextField = this.getContentElement('info', 'linkDisplayText').getElement().getParent().getParent(),
                        elements = plugin.getSelectedLink(editor, true),
                        firstLink = elements[0] || null;

                    // Fill in all the relevant fields if there's already one link selected.
                    if (firstLink && firstLink.hasAttribute('href')) {
                        // Don't change selection if some element is already selected.
                        // For example - don't destroy fake selection.
                        if (!selection.getSelectedElement() && !selection.isInTable()) {
                            selection.selectElement(firstLink);
                        }
                    }

                    var data = plugin.parseLinkAttributes(editor, firstLink);

                    // Here we'll decide whether or not we want to show Display Text field.
                    if (elements.length <= 1 && plugin.showDisplayTextForElement(firstLink, editor)) {
                        displayTextField.show();
                    } else {
                        displayTextField.hide();
                    }

                    // Record down the selected element in the dialog.
                    this._.selectedElements = elements;

                    this.setupContent(data);
                },
                onOk: function () {
                    var data = {};

                    // Collect data from fields.
                    this.commitContent(data);

                    if (!this._.selectedElements.length) {
                        insertLinksIntoSelection(editor, data);
                    } else {
                        editLinksInSelection(editor, this._.selectedElements, data);

                        delete this._.selectedElements;
                    }
                },
                // Inital focus on 'url' field if link is of type URL.
                onFocus: function () {
                    this.getContentElement('info', 'url').select();
                }
            };
        });
        editor.addCommand('delier', {
            exec: function (editor) {
                // IE/Edge removes link from selection while executing "unlink" command when cursor
                // is right before/after link's text. Therefore whole link must be selected and the
                // position of cursor must be restored to its initial state after unlinking. (https://dev.ckeditor.com/ticket/13062)
                if (CKEDITOR.env.ie) {
                    var range = editor.getSelection().getRanges()[0],
                        link = (range.getPreviousEditableNode() && range.getPreviousEditableNode().getAscendant('a', true)) ||
                            (range.getNextEditableNode() && range.getNextEditableNode().getAscendant('a', true)),
                        bookmark;

                    if (range.collapsed && link) {
                        bookmark = range.createBookmark();
                        range.selectNodeContents(link);
                        range.select();
                    }
                }

                var style = new CKEDITOR.style({element: 'a', type: CKEDITOR.STYLE_INLINE, alwaysRemoveElement: 1});
                editor.removeStyle(style);

                if (bookmark) {
                    range.moveToBookmark(bookmark);
                    range.select();
                }
            },

            refresh: function (editor, path) {
                // Despite our initial hope, document.queryCommandEnabled() does not work
                // for this in Firefox. So we must detect the state by element paths.

                var element = path.lastElement && path.lastElement.getAscendant('a', true);

                if (element && element.getName() == 'a' && element.getAttribute('href') && element.getChildCount()) {
                    this.setState(CKEDITOR.TRISTATE_OFF);
                } else {
                    this.setState(CKEDITOR.TRISTATE_DISABLED);
                }
            },

            contextSensitive: 1,
            startDisabled: 1,
            requiredContent: 'a[href]',
            editorFocus: 1
        });
        editor.addCommand( 'anchor', new CKEDITOR.dialogCommand( 'anchor', {
            allowedContent: 'a[!name,id]',
            requiredContent: 'a[name]'
        } ) );
        editor.addCommand( 'removeAnchor', new CKEDITOR.removeAnchorCommand() );
        if (editor.ui.addButton) {
            editor.ui.addButton('Lier', {
                label: editor.lang.link.toolbar,
                command: 'lier',
                toolbar: 'liens,10',
                icon: contextJsParameters.contextPath + '/modules/jahia-training-developer/icons/lier.png'
            });
            editor.ui.addButton('Delier', {
                label: editor.lang.link.unlink,
                command: 'delier',
                toolbar: 'liens,20',
                icon: contextJsParameters.contextPath + '/modules/jahia-training-developer/icons/delier.png'
            });
            editor.ui.addButton( 'Ancre', {
                label: editor.lang.link.anchor.toolbar,
                command: 'anchor',
                toolbar: 'liens,30',
                icon: contextJsParameters.contextPath + '/modules/jahia-training-developer/icons/anchor.png'
            } );
        }
        CKEDITOR.dialog.add( 'anchor', this.path + 'dialogs/anchor.js' );
        if ( editor.contextMenu ) {
            editor.contextMenu.addListener( function( element ) {
                if ( !element || element.isReadOnly() ) {
                    return null;
                }

                var anchor = CKEDITOR.plugins.customlink.tryRestoreFakeAnchor( editor, element );

                if ( !anchor && !( anchor = CKEDITOR.plugins.customlink.getSelectedLink( editor ) ) ) {
                    return null;
                }

                var menu = {};

                if ( anchor.getAttribute( 'href' ) && anchor.getChildCount() ) {
                    menu = { link: CKEDITOR.TRISTATE_OFF, unlink: CKEDITOR.TRISTATE_OFF };
                }

                if ( anchor && anchor.hasAttribute( 'name' ) ) {
                    menu.anchor = menu.removeAnchor = CKEDITOR.TRISTATE_OFF;
                }

                return menu;
            } );
        }
        editor.dataProcessor.htmlFilter.addRules({
            elements: {
                a: function (element) {
                    element.addClass('text-link');
                }
            }
        });
    },
    afterInit: function( editor ) {
        // Empty anchors upcasting to fake objects.
        editor.dataProcessor.dataFilter.addRules( {
            elements: {
                a: function( element ) {
                    if ( !element.attributes.name ) {
                        return null;
                    }

                    if ( !element.children.length ) {
                        return editor.createFakeParserElement( element, 'cke_anchor', 'anchor' );
                    }

                    return null;
                }
            }
        } );

        var pathFilters = editor._.elementsPath && editor._.elementsPath.filters;
        if ( pathFilters ) {
            pathFilters.push( function( element, name ) {
                if ( name == 'a' ) {
                    if ( CKEDITOR.plugins.link.tryRestoreFakeAnchor( editor, element ) || ( element.getAttribute( 'name' ) && ( !element.getAttribute( 'href' ) || !element.getChildCount() ) ) ) {
                        return 'anchor';
                    }
                }
            } );
        }
    }
});

/**
 * Set of Link plugin helpers.
 *
 * @class
 * @singleton
 */
CKEDITOR.plugins.customlink = {
    /**
     * Get the surrounding link element of the current selection.
     *
     *        CKEDITOR.plugins.link.getSelectedLink( editor );
     *
     *        // The following selections will all return the link element.
     *
     *        <a href="#">li^nk</a>
     *        <a href="#">[link]</a>
     *        text[<a href="#">link]</a>
     *        <a href="#">li[nk</a>]
     *        [<b><a href="#">li]nk</a></b>]
     *        [<a href="#"><b>li]nk</b></a>
     *
     * @since 3.2.1
     * @param {CKEDITOR.editor} editor
     * @param {Boolean} [returnMultiple=false] Indicates whether the function should return only the first selected link or all of them.
     * @returns {CKEDITOR.dom.element/CKEDITOR.dom.element[]/null} A single link element or an array of link
     * elements relevant to the current selection.
     */
    getSelectedLink: function (editor, returnMultiple) {
        var selection = editor.getSelection(),
            selectedElement = selection.getSelectedElement(),
            ranges = selection.getRanges(),
            links = [],
            link,
            range,
            i;

        if (!returnMultiple && selectedElement && selectedElement.is('a')) {
            return selectedElement;
        }

        for (i = 0; i < ranges.length; i++) {
            range = selection.getRanges()[i];

            // Skip bogus to cover cases of multiple selection inside tables (#tp2245).
            // Shrink to element to prevent losing anchor (#859).
            range.shrink(CKEDITOR.SHRINK_ELEMENT, true, {skipBogus: true});
            link = editor.elementPath(range.getCommonAncestor()).contains('a', 1);

            if (link && returnMultiple) {
                links.push(link);
            } else if (link) {
                return link;
            }
        }

        return returnMultiple ? links : null;
    },

    /**
     * For browsers that do not support CSS3 `a[name]:empty()`. Note that IE9 is included because of https://dev.ckeditor.com/ticket/7783.
     *
     * @readonly
     * @deprecated 4.3.3 It is set to `false` in every browser.
     * @property {Boolean} synAnchorSelector
     */

    /**
     * For browsers that have editing issues with an empty anchor.
     *
     * @readonly
     * @deprecated 4.3.3 It is set to `false` in every browser.
     * @property {Boolean} emptyAnchorFix
     */

    /**
     * Parses attributes of the link element and returns an object representing
     * the current state (data) of the link. This data format is a plain object accepted
     * e.g. by the Link dialog window and {@link #getLinkAttributes}.
     *
     * **Note:** Data model format produced by the parser must be compatible with the Link
     * plugin dialog because it is passed directly to {@link CKEDITOR.dialog#setupContent}.
     *
     * @since 4.4
     * @param {CKEDITOR.editor} editor
     * @param {CKEDITOR.dom.element} element
     * @returns {Object} An object of link data.
     */
    parseLinkAttributes: function (editor, element) {
        var retval = {
            url: (element && (element.data('cke-saved-href') || element.getAttribute('href'))) || ''
        };

        // Load target and popup settings.
        if (element) {
            retval.type = element.data('type');
            retval.endUrl = element.data('endUrl');
            var target = element.getAttribute('target');
            if (target) {
                retval.target = target;
            }
        }

        return retval;
    },

    /**
     * Converts link data produced by {@link #parseLinkAttributes} into an object which consists
     * of attributes to be set (with their values) and an array of attributes to be removed.
     * This method can be used to compose or to update any link element with the given data.
     *
     * @since 4.4
     * @param {CKEDITOR.editor} editor
     * @param {Object} data Data in {@link #parseLinkAttributes} format.
     * @returns {Object} An object consisting of two keys, i.e.:
     *
     *        {
     *			// Attributes to be set.
     *			set: {
     *				href: 'http://foo.bar',
     *				target: 'bang'
     *			},
     *			// Attributes to be removed.
     *			removed: [
     *				'id', 'style'
     *			]
     *		}
     *
     */
    getLinkAttributes: function (editor, data) {
        var set = {};
        set['data-cke-saved-href'] = (data.url && CKEDITOR.tools.trim(data.url)) || '';

        // Popups and target.
        if (data.target) {
            set.target = data.target;
        }

        // Browser need the "href" fro copy/paste link to work. (https://dev.ckeditor.com/ticket/6641)
        if (set['data-cke-saved-href']) {
            set.href = set['data-cke-saved-href'];
        }

        set['data-type'] = data.type;
        if (data.type == 'serviceLink') {
            set['data-endUrl'] = data.endUrl;
        }

        if (data.type == 'anchorLink') {
            var name = ( data.anchor && data.anchor.name ),
                id = ( data.anchor && data.anchor.id );

            set[ 'data-cke-saved-href' ] = '#' + ( name || id || '' );
        }

        var removed = {
            target: 1,
            onclick: 1,
            'data-cke-pa-onclick': 1,
            'data-cke-saved-name': 1,
            'download': 1
        };

        // Remove all attributes which are not currently set.
        for (var s in set) {
            delete removed[s];
        }

        return {
            set: set,
            removed: CKEDITOR.tools.objectKeys(removed)
        };
    },


    /**
     * Determines whether an element should have a "Display Text" field in the Link dialog.
     *
     * @since 4.5.11
     * @param {CKEDITOR.dom.element/null} element Selected element, `null` if none selected or if a ranged selection
     * is made.
     * @param {CKEDITOR.editor} editor The editor instance for which the check is performed.
     * @returns {Boolean}
     */
    showDisplayTextForElement: function (element, editor) {
        var undesiredElements = {
                img: 1,
                table: 1,
                tbody: 1,
                thead: 1,
                tfoot: 1,
                input: 1,
                select: 1,
                textarea: 1
            },
            selection = editor.getSelection();

        // Widget duck typing, we don't want to show display text for widgets.
        if (editor.widgets && editor.widgets.focused) {
            return false;
        }

        if (selection && selection.getRanges().length > 1) {
            return false;
        }

        return !element || !element.getName || !element.is(undesiredElements);
    },
    /**
     * Collects anchors available in the editor (i.e. used by the Link plugin).
     * Note that the scope of search is different for inline (the "global" document) and
     * classic (`iframe`-based) editors (the "inner" document).
     *
     * @since 4.3.3
     * @param {CKEDITOR.editor} editor
     * @returns {CKEDITOR.dom.element[]} An array of anchor elements.
     */
    getEditorAnchors: function( editor ) {
        var editable = editor.editable(),

            // The scope of search for anchors is the entire document for inline editors
            // and editor's editable for classic editor/divarea (https://dev.ckeditor.com/ticket/11359).
            scope = ( editable.isInline() && !editor.plugins.divarea ) ? editor.document : editable,

            links = scope.getElementsByTag( 'a' ),
            imgs = scope.getElementsByTag( 'img' ),
            anchors = [],
            iterator = 0,
            item;

        // Retrieve all anchors within the scope.
        while ( ( item = links.getItem( iterator++ ) ) ) {
            if ( (item.data( 'cke-saved-name' ) || item.hasAttribute( 'name' )) ) {
                anchors.push( {
                    name: item.data( 'cke-saved-name' ) || item.getAttribute( 'name' ),
                    id: item.getAttribute( 'id' )
                } );
            }
        }
        // Retrieve all "fake anchors" within the scope.
        iterator = 0;

        while ( ( item = imgs.getItem( iterator++ ) ) ) {
            if ( ( item = this.tryRestoreFakeAnchor( editor, item ) ) ) {
                anchors.push( {
                    name: item.getAttribute( 'name' ),
                    id: item.getAttribute( 'id' )
                } );
            }
        }

        return anchors;
    },

    /**
     * Opera and WebKit do not make it possible to select empty anchors. Fake
     * elements must be used for them.
     *
     * @readonly
     * @deprecated 4.3.3 It is set to `true` in every browser.
     * @property {Boolean}
     */
    fakeAnchor: true,

    /**
     * For browsers that do not support CSS3 `a[name]:empty()`. Note that IE9 is included because of https://dev.ckeditor.com/ticket/7783.
     *
     * @readonly
     * @deprecated 4.3.3 It is set to `false` in every browser.
     * @property {Boolean} synAnchorSelector
     */

    /**
     * For browsers that have editing issues with an empty anchor.
     *
     * @readonly
     * @deprecated 4.3.3 It is set to `false` in every browser.
     * @property {Boolean} emptyAnchorFix
     */

    /**
     * Returns an element representing a real anchor restored from a fake anchor.
     *
     * @param {CKEDITOR.editor} editor
     * @param {CKEDITOR.dom.element} element
     * @returns {CKEDITOR.dom.element} Restored anchor element or nothing if the
     * passed element was not a fake anchor.
     */
    tryRestoreFakeAnchor: function( editor, element ) {
        if ( element && element.data( 'cke-real-element-type' ) && element.data( 'cke-real-element-type' ) == 'anchor' ) {
            var link = editor.restoreRealElement( element );
            if ( link.data( 'cke-saved-name' ) ) {
                return link;
            }
        }
    }

};

CKEDITOR.removeAnchorCommand = function() {};
CKEDITOR.removeAnchorCommand.prototype = {
    exec: function( editor ) {
        var sel = editor.getSelection(),
            bms = sel.createBookmarks(),
            anchor;

        if ( sel && ( anchor = sel.getSelectedElement() ) && ( !anchor.getChildCount() ? CKEDITOR.plugins.link.tryRestoreFakeAnchor( editor, anchor ) : anchor.is( 'a' ) ) ) {
            anchor.remove( 1 );
        } else {
            if ( ( anchor = CKEDITOR.plugins.link.getSelectedLink( editor ) ) ) {
                if ( anchor.hasAttribute( 'href' ) ) {
                    anchor.removeAttributes( { name: 1, 'data-cke-saved-name': 1 } );
                    anchor.removeClass( 'cke_anchor' );
                } else {
                    anchor.remove( 1 );
                }
            }
        }
        sel.selectBookmarks( bms );
    },
    requiredContent: 'a[name]'
};
