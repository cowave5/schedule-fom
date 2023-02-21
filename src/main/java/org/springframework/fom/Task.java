package org.springframework.fom;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.fom.ScheduleContext.CompleteLatch;
import org.springframework.util.StringUtils;

/**
 * 
 * @param <E> 任务执行结果类型
 * 
 * @author shanhm1991@163.com
 *
 */
public abstract class Task<E> implements Callable<Result<E>> {
	
	protected volatile Logger logger = LoggerFactory.getLogger(Task.class);
	
	private static ThreadLocal<ScheduleContext<?>> localSchedule = new ThreadLocal<>();

	protected final String id;

	// 定时线程提交时设置
	private long submitTime;

	// 任务线程启动时自己设置
	private volatile long startTime;

	// 定时线程设置，任务线程读取
	private volatile ScheduleContext<E> scheduleContext;

	// 定时线程设置，任务线程读取
	private volatile CompleteLatch<E> completeLatch;
	
	public Task(){
		String name = this.getClass().getSimpleName();
		if(!StringUtils.hasText(name)){
			name = "Task";
		}
		this.id = name;
	}

	public Task(String id) { 
		this.id = id;
	}
	
	public static ScheduleContext<?> getCurrentSchedule(){
		return localSchedule.get();
	}

	@Override
	public final Result<E> call() throws InterruptedException {   
		Thread.currentThread().setName("task[" + id + "]");
		localSchedule.set(scheduleContext); 
		logger.debug("task started."); 

		this.startTime = System.currentTimeMillis();
		final Result<E> result = new Result<>(id, submitTime, startTime); 
		doCall(result);
		result.setCostTime(System.currentTimeMillis() - startTime); 

		if(scheduleContext != null){
			if(completeLatch != null){
				completeLatch.addResult(result); 
				scheduleContext.checkComplete(completeLatch);
			}
			scheduleContext.record(result); 
		}

		if(result.isSuccess()){
			logger.debug("task success, cost={}ms, restult={}", result.getCostTime(), result.getContent());
		}else{
			Throwable e = null;
			if(result.getThrowable() != null){
				Throwable throwable = result.getThrowable();
				Throwable cause;
				while((cause = throwable.getCause()) != null){
					throwable = cause;
				}
				e = throwable;
			}
			logger.error("task failed, cost={}ms, restult={}", result.getCostTime(), result.getContent(), e);
		}
		return result;
	}

	private void doCall(Result<E> result){
		try {
			if(!beforeExec()){
				result.setSuccess(false); 
				return;
			}
			result.setContent(exec());
		} catch(Throwable e) {
			result.setSuccess(false);
			result.setThrowable(e);
		} finally{
			try {
				afterExec(result.isSuccess(), result.getContent(), result.getThrowable());
			}catch(Throwable e) {
				logger.error("", e); 
			}
		}
	}

	public boolean beforeExec() throws Exception {
		return true;
	}

	public abstract E exec() throws Exception;

	public void afterExec(boolean isExecSuccess,  E content, Throwable e) throws Exception {

	}
	
	public final String getTaskId() {
		return id;
	}
	
	void setSubmitTime(long submitTime) {
		this.submitTime = submitTime;
	}

	public long getSubmitTime() {
		return submitTime;
	}

	public final long getStartTime() {
		return startTime;
	}

	CompleteLatch<E> getCompleteLatch() {
		return completeLatch;
	}

	void setCompleteLatch(CompleteLatch<E> completeLatch) {
		this.completeLatch = completeLatch;
	}

	ScheduleContext<E> getScheduleContext() {
		return scheduleContext;
	}

	void setScheduleContext(ScheduleContext<E> scheduleContext) {
		this.scheduleContext = scheduleContext;
		this.logger = scheduleContext.getLogger();
	}

	public String getScheduleName(){
		if(scheduleContext != null){
			return scheduleContext.getScheduleName();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public <V> V getConfig(String key){
		if(scheduleContext != null){
			return (V)scheduleContext.getScheduleConfig().get(key);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final boolean equals(Object obj) {
		if(!(obj instanceof Task)){
			return false;
		}
		Task<E> task = (Task<E>)obj;
		return this.id.equals(task.id);
	}

	@Override
	public final int hashCode() {
		return this.id.hashCode();
	}
	
	@Override
	public String toString() {
		return id;
	}
}
