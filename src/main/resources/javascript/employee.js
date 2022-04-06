const sayHi  = (event, url, successMessage, errorMessage) => {
    event.preventDefault();

    fetch(url, {headers: {'Accept': 'application/json'}}).then(response => response.json()).then(data => {
        alert(successMessage.replace('$firstname', data.firstname).replace('$lastname', data.lastname));
    }).catch(() => {
        alert(errorMessage);
    });

    return false;
};
