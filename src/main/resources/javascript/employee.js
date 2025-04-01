const sayHi = (event, url, successMessage, errorMessage) => {
    event.preventDefault();

    fetch(url, {headers: {'Accept': 'application/json'}}).then(response => response.json()).then(data => {
        alert(successMessage.replace('$firstname', data.firstname).replace('$lastname', data.lastname));
    }).catch(() => {
        alert(errorMessage);
    });

    return false;
};

const clickByInterest = (event, currentNodeIdentifier, mainResourceType, interests, errorMessage) => {
    event.preventDefault();

    if (!wem) {
        console.error(errorMessage);
        return false;
    }

    const sourcePage = wem.buildSourcePage();
    const target = wem.buildTarget(`click-on-${currentNodeIdentifier}`, mainResourceType);
    const customEvent = wem.buildEvent('clickByInterest', target, sourcePage);
    try {
        if (interests) customEvent.flattenedProperties = {interests};
        console.log(customEvent);
        wem.collectEvent(customEvent, () => {
            console.log('successfulEventSubmission');
        }, () => {
            console.error(`failedEventSubmission: ${errorMessage}`);
        });
    } catch (e) {
        console.error(e);
    }

    return false;
};
