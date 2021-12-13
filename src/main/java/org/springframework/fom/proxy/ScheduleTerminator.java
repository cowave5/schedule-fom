package org.springframework.fom.proxy;

/**
 * 
 * @author shanhm1991@163.com
 *
 */
public interface ScheduleTerminator {

	/**
	 * 定时器关闭处理
	 * @param execTimes 定时任务已经执行次数
	 * @param lastExecTime 定时任务最后一次执行时间
	 * @throws Exception
	 */
	public void onScheduleTerminate(long execTimes, long lastExecTime) throws Exception;
}
