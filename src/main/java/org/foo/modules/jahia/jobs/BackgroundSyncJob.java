package org.foo.modules.jahia.jobs;

import org.jahia.api.settings.SettingsBean;
import org.jahia.osgi.BundleUtils;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.scheduler.SchedulerService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;

@Component(service = {BackgroundSyncJob.class, BackgroundJob.class})
public class BackgroundSyncJob extends BackgroundJob {
    private static final Logger logger = LoggerFactory.getLogger(BackgroundSyncJob.class);

    private static final int REPEAT_INTERVAL = 5 * 60 * 1000;
    private static final String TRIGGER_NAME = BackgroundSyncJob.class.getName() + "_trigger";
    private static final String JOB_GROUP = BackgroundSyncJob.class.getName();

    @Reference
    private SchedulerService schedulerService;
    @Reference
    private SettingsBean settingsBean;
    private JobDetail jobDetail;

    @Activate
    private void start() throws Exception {
        // TODO: Move to @Deactivate method
        deleteJob();

        jobDetail = BackgroundJob.createJahiaJob("Simple background job made declared with OSGi", BackgroundSyncJob.class);
        if (schedulerService.getAllJobs(JOB_GROUP).isEmpty() && settingsBean.isProcessingServer()) {
            schedulerService.getScheduler().scheduleJob(jobDetail, new SimpleTrigger(TRIGGER_NAME, JOB_GROUP, SimpleTrigger.REPEAT_INDEFINITELY, REPEAT_INTERVAL));
        }
    }

    private void deleteJob() throws Exception {
        if (!schedulerService.getAllJobs(JOB_GROUP).isEmpty() && settingsBean.isProcessingServer()) {
            schedulerService.getAllJobs(JOB_GROUP).forEach(job -> {
                try {
                    schedulerService.getScheduler().deleteJob(job.getName(), JOB_GROUP);
                } catch (SchedulerException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    @Override
    public void executeJahiaJob(JobExecutionContext jobExecutionContext) throws RepositoryException {
        logger.info("Trigger job!");
        BundleUtils.getOsgiService(BackgroundSyncService.class, null).executeJahiaJob();
    }

    public void reschedule() {
        Trigger newTrigger = new SimpleTrigger(TRIGGER_NAME, jobDetail.getGroup(), SimpleTrigger.REPEAT_INDEFINITELY, REPEAT_INTERVAL);
        newTrigger.setJobGroup(jobDetail.getGroup());
        newTrigger.setJobName(jobDetail.getName());
        try {
            schedulerService.getScheduler().rescheduleJob(TRIGGER_NAME, jobDetail.getGroup(), newTrigger);
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }
}
