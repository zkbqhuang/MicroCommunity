package com.java110.job.task;

import com.java110.common.util.SpringBeanInvoker;
import com.java110.job.dao.IPrvncFtpFileDAO;
import com.java110.job.smo.PrvncFtpToFileSystemQuartz;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;

import java.util.HashMap;
import java.util.Map;

/**
 * 将集团文件同步至本地支持的文件系统
 * 
 * @author wuxw7
 * 
 */

public class PrvncFtpToFileSystemJob extends MethodInvokingJobDetailFactoryBean.StatefulMethodInvokingJob {
	
	private static final Logger logger = LoggerFactory.getLogger(PrvncFtpToFileSystemJob.class);

	public static String JOB_DATA_CONFIG_NAME = "ftpToFileSystemTaskName";
	public static String JOB_DATA_TASK_ID = "ftpToFileSystemTaskID";
	public static String JOB_GROUP_NAME = "ftpToFileSystemJobGroup"; // 任务的 分组名称
	@Autowired
	private IPrvncFtpFileDAO prvncFtpFileDAO;
	@Autowired
	private PrvncFtpToFileSystemQuartz prvncDumpQuartz;
	
	protected void executeInternal(JobExecutionContext context) {
		try {
			
			if(logger.isDebugEnabled()){
				logger.debug("FTP通用数据文件传接任务：" +
						 context.getJobDetail().getFullName() + " taskID：" +
						 context.getJobDetail().getJobDataMap().get(JOB_DATA_TASK_ID) +
						 " ftpfileTaskName：" +
						 context.getJobDetail().getJobDataMap().get(JOB_DATA_CONFIG_NAME), context);
			}

			long taskId = Long.parseLong(context.getJobDetail().getJobDataMap()
					.getString(JOB_DATA_TASK_ID));
			// 根据taskId 查询配置信息
			Map ftpItemConfigInfo = this.getFtpConfigInfo(taskId);
			
			//如果查询不到数据，或者是没有处理class，不在运行
			if(ftpItemConfigInfo == null || !ftpItemConfigInfo.containsKey("DEAL_CLASS") || ftpItemConfigInfo.get("DEAL_CLASS") == null){
				logger.error("---【PrvncFtpToFileSystemQuartz.executeInternal】查询到的ftp配置数据为空，或没有处理类", ftpItemConfigInfo);
				return;
			}
			
			String dealClass = ftpItemConfigInfo.get("DEAL_CLASS").toString();
			prvncDumpQuartz = (PrvncFtpToFileSystemQuartz) SpringBeanInvoker.getBean(dealClass);
			prvncDumpQuartz.startFtpTask(ftpItemConfigInfo);
		} catch (Throwable ex) {
			logger.error("执行任务失败：", ex);
		}
	}

	/**
	 * 查询配置相关信息
	 * 
	 * @param taskId
	 * @return
	 */
	private Map getFtpConfigInfo(long taskId) {
		Map info = new HashMap();
		info.put("taskId", taskId);
		Map ftpItem = getPrvncFtpFileDAO().queryFtpItemByTaskId(info);
		if (logger.isDebugEnabled()) {
			logger.debug(
					"---【PrvncFtpToFileSystemQuartz.getFtpConfigInfo】查询到的配置数据为："
							+ ftpItem, ftpItem);
		}
		return ftpItem;
	}

	public IPrvncFtpFileDAO getPrvncFtpFileDAO() {
		if (this.prvncFtpFileDAO == null) {
			this.prvncFtpFileDAO = ((IPrvncFtpFileDAO) SpringBeanInvoker
					.getBean("provInner.PrvncFtpFileDAO"));
		}
		return prvncFtpFileDAO;
	}

}