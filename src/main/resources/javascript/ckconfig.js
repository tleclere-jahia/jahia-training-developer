CKEDITOR.editorConfig = config => {
    CKEDITOR.plugins.addExternal('customlink', '/modules/jahia-training-developer/javascript/plugins/customlink/plugin.js');
    config.removePlugins = 'link,pastefromword';
    config.extraPlugins = 'copyformatting,customlink';
    config.toolbar_Full[8] = ['Image', 'Flash', 'Table', 'HorizontalRule', 'Smiley', 'SpecialChar', 'PageBreak', 'CopyFormatting']
    config.toolbar_Full[9] = ['Lier', 'Delier', 'Ancre'];

    config.language = (typeof contextJsParameters != 'undefined') ? contextJsParameters.uilang : 'en';
    config.siteKey = (typeof contextJsParameters != 'undefined') ? contextJsParameters.siteKey : '';
    config.contextPath = (window.contextJsParameters && window.contextJsParameters.contextPath) || '';

    const getCKEditorUrlInputId = (dialog) => {
        if (!dialog) {
            return;
        }
        return dialog.getContentElement('info', 'url') ? 'url' : 'txtUrl';
    };

    const fillCKEditorPicker = (setUrl, dialog, pickerResult) => {
        const eltId = getCKEditorUrlInputId(dialog);
        const altElementId = dialog.getName() === 'image2' ? 'alt' : 'txtAlt';
        const contentElement = dialog.getContentElement('info', eltId === 'url' ? 'advTitle' : altElementId);
        if (contentElement !== undefined) {
            if (eltId === 'url' && pickerResult.displayName) {
                contentElement.setValue(pickerResult.displayName);
            } else {
                contentElement.setValue(pickerResult.name);
            }
        }

        if (pickerResult.url) {
            setUrl(pickerResult.url);
        } else {
            // Wrap path to build Jahia url.
            const pathWithEncodedFileName = pickerResult.path.replace(/\/([^/]+\.[^/?#]+)(\?|#|$)/, (_, fileName, suffix) => `/${encodeURIComponent(fileName)}${suffix}`);
            setUrl(`${config.contextPath}/cms/{mode}/{lang}${pathWithEncodedFileName}.html`, {});
        }
    };

    config.lienserviceBrowseUrl = (dialog, params, setUrl) => {
        window.CE_API.openPicker({
            type: 'lienservicepicker',
            value: '',
            setValue: pickerResult => {
                fillCKEditorPicker(setUrl, dialog, pickerResult.length > 0 && pickerResult[0]);
            },
            site: config.siteKey,
            lang: config.language,
            uilang: config.language
        });
    };
};
