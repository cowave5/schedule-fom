package com.examples;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;

import com.fom.context.Context;
import com.fom.context.FomContext;
import com.fom.task.DownloadTask;
import com.fom.task.helper.DownloadHelper;
import com.fom.task.helper.impl.HdfsHelper;
import com.fom.util.HdfsUtil;
import com.fom.util.PatternUtil;

/**
 * 
 * @author shanhm
 *
 */
@FomContext(remark="扫描下载Hdfs指定目录下的文件")
public class DownloadHdfsExample extends Context {

	private static final long serialVersionUID = -8950649337670940490L;

	private String masterUrl;

	private String slaveUrl;
	
	private String dest;
	
	public DownloadHdfsExample(){
		dest = new File("").getAbsolutePath() + "/download/" + name;
	}

	@Override
	protected Set<DownloadTask> scheduleBatchTasks() throws Exception {  
		List<String> list = HdfsUtil.list(masterUrl, slaveUrl, new Path("/test"), new PathFilter(){
			@Override
			public boolean accept(Path path) {
				return PatternUtil.match("regex", path.getName());
			}
		});
		
		Set<DownloadTask> set = new HashSet<>();
		for(String uri : list){
			DownloadHelper helper = new HdfsHelper(masterUrl, slaveUrl);
			String sourceName = new File(uri).getName();
			set.add(new DownloadTask(uri, sourceName, dest, false, true, helper));
		}
		return set;
	}
}
