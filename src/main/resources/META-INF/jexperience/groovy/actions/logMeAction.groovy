import org.apache.unomi.api.services.EventService
import org.apache.unomi.groovy.actions.annotations.Action
import org.apache.unomi.persistence.spi.CustomObjectMapper

import java.util.logging.Logger

@Action(id = "logMeAction", actionExecutor = "groovy:logMeAction")
def execute() {
    Logger logger = Logger.getLogger("")
    CustomObjectMapper objectMapper = CustomObjectMapper.getObjectMapper() as CustomObjectMapper
    logger.info "Event: ${objectMapper.writeValueAsString(event)}"
    EventService.NO_CHANGE
}
