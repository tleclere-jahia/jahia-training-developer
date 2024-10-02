package org.foo.modules.jahia.jobs;

import org.jahia.api.settings.SettingsBean;
import org.jahia.osgi.BundleUtils;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.scheduler.SchedulerService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.SimpleTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;

@Component
public class BackgroundSyncJob extends BackgroundJob {
    private static final Logger logger = LoggerFactory.getLogger(BackgroundSyncJob.class);

    private static final int REPEAT_INTERVAL = 5 * 60 * 1000;

    @Reference
    private SchedulerService schedulerService;
    @Reference
    private SettingsBean settingsBean;
    private JobDetail jobDetail;

    @Activate
    public void start() throws Exception {
        jobDetail = BackgroundJob.createJahiaJob("Simple background job made declared with OSGi", BackgroundSyncJob.class);
        if (schedulerService.getAllJobs(jobDetail.getGroup()).isEmpty() && settingsBean.isProcessingServer()) {
            schedulerService.getScheduler().scheduleJob(jobDetail, new SimpleTrigger(BackgroundSyncJob.class.getName() + "_trigger", jobDetail.getGroup(), SimpleTrigger.REPEAT_INDEFINITELY, REPEAT_INTERVAL));
        }
    }

    @Deactivate
    public void stop() throws Exception {
        if (!schedulerService.getAllJobs(jobDetail.getGroup()).isEmpty() && settingsBean.isProcessingServer()) {
            schedulerService.getScheduler().deleteJob(jobDetail.getName(), jobDetail.getGroup());
        }
    }

    @Override
    public void executeJahiaJob(JobExecutionContext jobExecutionContext) throws RepositoryException {
        logger.info("Trigger job!");
        BundleUtils.getOsgiService(BackgroundSyncService.class, null).executeJahiaJob();
    }
}
