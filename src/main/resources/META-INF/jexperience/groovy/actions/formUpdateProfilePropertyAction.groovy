import org.apache.unomi.api.services.EventService

import java.util.logging.Logger

@Action(id = "formUpdateProfilePropertyAction", actionExecutor = "groovy:formUpdateProfilePropertyAction")
def execute() {
    Logger logger = Logger.getLogger("formUpdateProfilePropertyAction")
    logger.info "${event.properties['email']}"
    event.profile.setProperty("email", event.properties['email'])

    return EventService.PROFILE_UPDATED
}
