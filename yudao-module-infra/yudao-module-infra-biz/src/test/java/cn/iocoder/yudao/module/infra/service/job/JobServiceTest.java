package cn.iocoder.yudao.module.infra.service.job;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.framework.quartz.core.scheduler.SchedulerManager;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.infra.controller.admin.job.vo.job.JobCreateReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.job.vo.job.JobExportReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.job.vo.job.JobPageReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.job.vo.job.JobUpdateReqVO;
import cn.iocoder.yudao.module.infra.convert.job.JobConvert;
import cn.iocoder.yudao.module.infra.dal.dataobject.job.JobDO;
import cn.iocoder.yudao.module.infra.dal.mysql.job.JobMapper;
import cn.iocoder.yudao.module.infra.enums.job.JobStatusEnum;
import org.junit.jupiter.api.Test;
import org.quartz.SchedulerException;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomString;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Import(JobServiceImpl.class)
public class JobServiceTest extends BaseDbUnitTest {

    @Resource
    private JobServiceImpl jobService;
    @Resource
    private JobMapper jobMapper;
    @MockBean
    private SchedulerManager schedulerManager;

    @Test
    public void testCreateJob_cronExpressionValid() {
        // ???????????????Cron ???????????? String ?????????????????????????????????
        JobCreateReqVO reqVO = randomPojo(JobCreateReqVO.class);
        // ????????????????????????
        assertServiceException(() -> jobService.createJob(reqVO), JOB_CRON_EXPRESSION_VALID);
    }

    @Test
    public void testCreateJob_jobHandlerExists() throws SchedulerException {
        // ???????????? ?????? Cron ?????????
        JobCreateReqVO reqVO = randomPojo(JobCreateReqVO.class, o -> o.setCronExpression("0 0/1 * * * ? *"));
        // ??????
        jobService.createJob(reqVO);
        // ????????????????????????
        assertServiceException(() -> jobService.createJob(reqVO), JOB_HANDLER_EXISTS);
    }

    @Test
    public void testCreateJob_success() throws SchedulerException {
        // ???????????? ?????? Cron ?????????
        JobCreateReqVO reqVO = randomPojo(JobCreateReqVO.class, o -> o.setCronExpression("0 0/1 * * * ? *"));
        // ??????
        String jobId = jobService.createJob(reqVO);
        // ??????
        assertNotNull(jobId);
        // ?????????????????????????????????
        JobDO job = jobMapper.selectById(jobId);
        assertPojoEquals(reqVO, job);
        assertEquals(JobStatusEnum.NORMAL.getStatus(), job.getStatus());
        // ????????????
        verify(schedulerManager, times(1)).addJob(eq(job.getId()), eq(job.getHandlerName()), eq(job.getHandlerParam()), eq(job.getCronExpression()),
                eq(reqVO.getRetryCount()), eq(reqVO.getRetryInterval()));
    }

    @Test
    public void testUpdateJob_jobNotExists(){
        // ????????????
        JobUpdateReqVO reqVO = randomPojo(JobUpdateReqVO.class, o -> o.setCronExpression("0 0/1 * * * ? *"));
        // ????????????????????????
        assertServiceException(() -> jobService.updateJob(reqVO), JOB_NOT_EXISTS);
    }

    @Test
    public void testUpdateJob_onlyNormalStatus(){
        // mock ??????
        JobCreateReqVO createReqVO = randomPojo(JobCreateReqVO.class, o -> o.setCronExpression("0 0/1 * * * ? *"));
        JobDO job = JobConvert.INSTANCE.convert(createReqVO);
        job.setStatus(JobStatusEnum.INIT.getStatus());
        fillJobMonitorTimeoutEmpty(job);
        jobMapper.insert(job);
        // ????????????
        JobUpdateReqVO updateReqVO = randomPojo(JobUpdateReqVO.class, o -> {
            o.setId(job.getId());
            o.setName(createReqVO.getName());
            o.setCronExpression(createReqVO.getCronExpression());
        });
        // ????????????????????????
        assertServiceException(() -> jobService.updateJob(updateReqVO), JOB_UPDATE_ONLY_NORMAL_STATUS);
    }

    @Test
    public void testUpdateJob_success() throws SchedulerException {
        // mock ??????
        JobCreateReqVO createReqVO = randomPojo(JobCreateReqVO.class, o -> o.setCronExpression("0 0/1 * * * ? *"));
        JobDO job = JobConvert.INSTANCE.convert(createReqVO);
        job.setStatus(JobStatusEnum.NORMAL.getStatus());
        fillJobMonitorTimeoutEmpty(job);
        jobMapper.insert(job);
        // ????????????
        JobUpdateReqVO updateReqVO = randomPojo(JobUpdateReqVO.class, o -> {
            o.setId(job.getId());
            o.setName(createReqVO.getName());
            o.setCronExpression(createReqVO.getCronExpression());
        });
        // ??????
        jobService.updateJob(updateReqVO);
        // ?????????????????????????????????
        JobDO updateJob = jobMapper.selectById(updateReqVO.getId());
        assertPojoEquals(updateReqVO, updateJob);
        // ????????????
        verify(schedulerManager, times(1)).updateJob(eq(job.getHandlerName()), eq(updateReqVO.getHandlerParam()), eq(updateReqVO.getCronExpression()),
                eq(updateReqVO.getRetryCount()), eq(updateReqVO.getRetryInterval()));
    }

    @Test
    public void testUpdateJobStatus_changeStatusInvalid() {
        // ????????????????????????
        assertServiceException(() -> jobService.updateJobStatus("1", JobStatusEnum.INIT.getStatus()), JOB_CHANGE_STATUS_INVALID);
    }

    @Test
    public void testUpdateJobStatus_changeStatusEquals() {
        // mock ??????
        JobCreateReqVO createReqVO = randomPojo(JobCreateReqVO.class, o -> o.setCronExpression("0 0/1 * * * ? *"));
        JobDO job = JobConvert.INSTANCE.convert(createReqVO);
        job.setStatus(JobStatusEnum.NORMAL.getStatus());
        fillJobMonitorTimeoutEmpty(job);
        jobMapper.insert(job);
        // ????????????????????????
        assertServiceException(() -> jobService.updateJobStatus(job.getId(), job.getStatus()), JOB_CHANGE_STATUS_EQUALS);
    }

    @Test
    public void testUpdateJobStatus_NormalToStop_success() throws SchedulerException {
        // mock ??????
        JobCreateReqVO createReqVO = randomPojo(JobCreateReqVO.class, o -> o.setCronExpression("0 0/1 * * * ? *"));
        JobDO job = JobConvert.INSTANCE.convert(createReqVO);
        job.setStatus(JobStatusEnum.NORMAL.getStatus());
        fillJobMonitorTimeoutEmpty(job);
        jobMapper.insert(job);
        // ??????
        jobService.updateJobStatus(job.getId(), JobStatusEnum.STOP.getStatus());
        // ?????????????????????????????????
        JobDO updateJob = jobMapper.selectById(job.getId());
        assertEquals(JobStatusEnum.STOP.getStatus(), updateJob.getStatus());
        // ????????????
        verify(schedulerManager, times(1)).pauseJob(eq(job.getHandlerName()));
    }

    @Test
    public void testUpdateJobStatus_StopToNormal_success() throws SchedulerException {
        // mock ??????
        JobCreateReqVO createReqVO = randomPojo(JobCreateReqVO.class, o -> o.setCronExpression("0 0/1 * * * ? *"));
        JobDO job = JobConvert.INSTANCE.convert(createReqVO);
        job.setStatus(JobStatusEnum.STOP.getStatus());
        fillJobMonitorTimeoutEmpty(job);
        jobMapper.insert(job);
        // ??????
        jobService.updateJobStatus(job.getId(), JobStatusEnum.NORMAL.getStatus());
        // ?????????????????????????????????
        JobDO updateJob = jobMapper.selectById(job.getId());
        assertEquals(JobStatusEnum.NORMAL.getStatus(), updateJob.getStatus());
        // ????????????
        verify(schedulerManager, times(1)).resumeJob(eq(job.getHandlerName()));
    }

    @Test
    public void testTriggerJob_success() throws SchedulerException {
        // mock ??????
        JobCreateReqVO createReqVO = randomPojo(JobCreateReqVO.class, o -> o.setCronExpression("0 0/1 * * * ? *"));
        JobDO job = JobConvert.INSTANCE.convert(createReqVO);
        job.setStatus(JobStatusEnum.NORMAL.getStatus());
        fillJobMonitorTimeoutEmpty(job);
        jobMapper.insert(job);
        // ??????
        jobService.triggerJob(job.getId());
        // ????????????
        verify(schedulerManager, times(1)).triggerJob(eq(job.getId()), eq(job.getHandlerName()), eq(job.getHandlerParam()));
    }

    @Test
    public void testDeleteJob_success() throws SchedulerException {
        // mock ??????
        JobCreateReqVO createReqVO = randomPojo(JobCreateReqVO.class, o -> o.setCronExpression("0 0/1 * * * ? *"));
        JobDO job = JobConvert.INSTANCE.convert(createReqVO);
        job.setStatus(JobStatusEnum.NORMAL.getStatus());
        fillJobMonitorTimeoutEmpty(job);
        jobMapper.insert(job);
        // ?????? UPDATE inf_job SET deleted=1 WHERE id=? AND deleted=0
        jobService.deleteJob(job.getId());
        // ????????????????????????  WHERE id=? AND deleted=0 ??????????????????
        assertNull(jobMapper.selectById(job.getId()));
        // ????????????
        verify(schedulerManager, times(1)).deleteJob(eq(job.getHandlerName()));
    }

    @Test
    public void testGetJobListByIds_success() {
        // mock ??????
        JobDO dbJob = randomPojo(JobDO.class, o -> {
            o.setStatus(randomEle(JobStatusEnum.values()).getStatus()); // ?????? status ?????????
        });
        JobDO cloneJob = ObjectUtils.cloneIgnoreId(dbJob, o -> o.setHandlerName(randomString()));
        jobMapper.insert(dbJob);
        // ?????? handlerName ?????????
        jobMapper.insert(cloneJob);
        // ????????????
        ArrayList<String> ids = new ArrayList<>();
        ids.add(dbJob.getId());
        ids.add(cloneJob.getId());
        // ??????
        List<JobDO> list = jobService.getJobList(ids);
        // ??????
        assertEquals(2, list.size());
        assertPojoEquals(dbJob, list.get(0));
    }

    @Test
    public void testGetJobPage_success() {
        // mock ??????
        JobDO dbJob = randomPojo(JobDO.class, o -> {
            o.setName("??????????????????");
            o.setHandlerName("handlerName ????????????");
            o.setStatus(JobStatusEnum.INIT.getStatus());
        });
        jobMapper.insert(dbJob);
        // ?????? name ?????????
        jobMapper.insert(ObjectUtils.cloneIgnoreId(dbJob, o -> o.setName("??????")));
        // ?????? status ?????????
        jobMapper.insert(ObjectUtils.cloneIgnoreId(dbJob, o -> o.setStatus(JobStatusEnum.NORMAL.getStatus())));
        // ?????? handlerName ?????????
        jobMapper.insert(ObjectUtils.cloneIgnoreId(dbJob, o -> o.setHandlerName(randomString())));
        // ????????????
        JobPageReqVO reqVo = new JobPageReqVO();
        reqVo.setName("??????");
        reqVo.setStatus(JobStatusEnum.INIT.getStatus());
        reqVo.setHandlerName("??????");
        // ??????
        PageResult<JobDO> pageResult = jobService.getJobPage(reqVo);
        // ??????
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbJob, pageResult.getList().get(0));
    }

    @Test
    public void testGetJobListForExport_success() {
        // mock ??????
        JobDO dbJob = randomPojo(JobDO.class, o -> {
            o.setName("??????????????????");
            o.setHandlerName("handlerName ????????????");
            o.setStatus(JobStatusEnum.INIT.getStatus());
        });
        jobMapper.insert(dbJob);
        // ?????? name ?????????
        jobMapper.insert(ObjectUtils.cloneIgnoreId(dbJob, o -> o.setName("??????")));
        // ?????? status ?????????
        jobMapper.insert(ObjectUtils.cloneIgnoreId(dbJob, o -> o.setStatus(JobStatusEnum.NORMAL.getStatus())));
        // ?????? handlerName ?????????
        jobMapper.insert(ObjectUtils.cloneIgnoreId(dbJob, o -> o.setHandlerName(randomString())));
        // ????????????
        JobExportReqVO reqVo = new JobExportReqVO();
        reqVo.setName("??????");
        reqVo.setStatus(JobStatusEnum.INIT.getStatus());
        reqVo.setHandlerName("??????");
        // ??????
        List<JobDO> list = jobService.getJobList(reqVo);
        // ??????
        assertEquals(1, list.size());
        assertPojoEquals(dbJob, list.get(0));
    }

    private static void fillJobMonitorTimeoutEmpty(JobDO job) {
        if (job.getMonitorTimeout() == null) {
            job.setMonitorTimeout(0);
        }
    }

}
