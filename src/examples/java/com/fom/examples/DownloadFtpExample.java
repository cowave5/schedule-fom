package com.fom.examples;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import com.fom.context.Context;
import com.fom.context.FomContext;
import com.fom.context.Task;
import com.fom.task.DownloadTask;
import com.fom.task.helper.DownloadHelper;
import com.fom.task.helper.impl.FtpHelper;

/**
 * 
 * @author shanhm
 *
 */
@FomContext(remark="下载Ftp服务指定文件")
public class DownloadFtpExample extends Context {

	private static final long serialVersionUID = 9006928975258471271L;

	public DownloadFtpExample(){
		String dest = new File("").getAbsolutePath() + "/download/" + name;
		setValue("hostname", "");
		setValue("port", "");
		setValue("user", "");
		setValue("passwd", "");
		setValue("destpath", dest);
	}

	@Override
	protected Set<String> getTaskIdSet() throws Exception {
		Thread.sleep(5000); 
		
		Set<String> set = new HashSet<String>();
		set.add("/ftp/test.txt");
		return set;
	}

	@Override
	protected Task cronBatchSubmitTask(String taskId) throws Exception { 
		String hostname = getValue("hostname");
		int port = getInt("port", 0);
		String user = getValue("user");
		String passwd = getValue("passwd");
		String dest = getValue("dest");
		
		DownloadHelper helper = new FtpHelper(hostname, port, user, passwd);
		String sourceName = new File(taskId).getName();
		return new DownloadTask(taskId, sourceName, dest, false, true, helper);
	}

}
