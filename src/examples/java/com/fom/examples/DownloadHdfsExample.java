package com.fom.examples;

import java.io.File;
import java.util.List;

import org.apache.hadoop.fs.FileSystem;

import com.fom.context.Context;
import com.fom.context.Executor;
import com.fom.context.FomContext;
import com.fom.context.executor.Downloader;
import com.fom.context.helper.impl.HdfsHelper;

/**
 * 
 * @author shanhm
 *
 */
@FomContext(remark="扫描下载Hdfs指定目录下的文件")
public class DownloadHdfsExample extends Context {

	private static final long serialVersionUID = -8950649337670940490L;

	private FileSystem fs;

	@Override
	protected List<String> getUriList() throws Exception { 
		log.info("没有初始化hdfs环境,不创建下载任务");
		return null;

		//		return HdfsUtil.listPath(fs, "/test", new PathFilter(){
		//			@Override
		//			public boolean accept(Path path) {
		//				return PatternUtil.match("regex", path.getName());
		//			}
		//		});
	}

	@Override
	protected Executor createExecutor(String sourceUri) {
		HdfsHelper helper = new HdfsHelper(fs);
		String sourceName = new File(sourceUri).getName();
		Downloader downloader = new Downloader(sourceName, sourceUri, "${webapp.root}/download", 
				false, true, helper);
		return downloader;
	}
}
