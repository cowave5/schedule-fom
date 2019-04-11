package com.fom.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fom.context.ContextUtil;

/**
 * 
 * @author shanhm
 *
 */
@RestController
public class FomController {

	@RequestMapping("/list")
	@ResponseBody
	public Map<String, Object> list() throws Exception{
		List<Map<String, String>> list = ContextUtil.list();
		Map<String, Object> map = new HashMap<>();
		map.put("data", list);
		map.put("length", list.size());
		map.put("recordsTotal", list.size());
		map.put("recordsFiltered", list.size());
		return map;
	}

	@RequestMapping("/save")
	@ResponseBody
	public Map<String,Object> save(String name, String data) throws Exception{ 
		return ContextUtil.save(name, data);
	}

	@RequestMapping("/operation")
	@ResponseBody
	public Map<String,Object> operation(String name, int opid) throws Exception{ 
		switch(opid){
		case 1: return ContextUtil.startup(name);
		case 2: return ContextUtil.shutDown(name);
		case 3: return ContextUtil.execNow(name);
		default : 
			Map<String,Object> map = new HashMap<>();
			map.put("result", false);
			map.put("msg", "unsupported operation.");
			return map;
		}
	}

	@RequestMapping("/state")
	@ResponseBody
	public Map<String,Object> state(String name) throws Exception{ 
		return ContextUtil.state(name);
	}

	@RequestMapping("/log")
	public String log(String name, String level) throws Exception{ 
		ContextUtil.changeLogLevel(name, level); 
		return "success";
	}

	@RequestMapping("/create")
	@ResponseBody
	public Map<String,Object> create(String json) throws Exception{ 
		return ContextUtil.create(json);
	}

	@RequestMapping("/listOtherLogs")
	@ResponseBody
	public Map<String, String> listOtherLogs() throws Exception {
		return ContextUtil.listOtherLogs();
	}

	@RequestMapping("/queryLevel")
	@ResponseBody
	public Map<String, String> queryLevel(String logger) {
		Map<String, String> map = new HashMap<>();
		map.put("level", ContextUtil.queryLogLevel(logger));
		return map;
	}

	@RequestMapping("/saveLevel")
	public String saveLevel(String logger, String level) {
		ContextUtil.saveLogLevel(logger, level);
		return "success";
	}

	@RequestMapping("/successdetail")
	@ResponseBody
	public Map<String,Object> successDetail(String name) throws Exception { 
		return ContextUtil.successDetail(name);
	}

	@RequestMapping("/faileddetail")
	@ResponseBody
	public Map<String,Object> failedDetail(String name) throws Exception { 
		return ContextUtil.failedDetail(name);
	}
	
	@RequestMapping("/executedetail")
	@ResponseBody
	public Map<String,String> executedetail(String name) throws Exception { 
		return  ContextUtil.getLastExceptions(name);
	}

	@RequestMapping("/activedetail")
	@ResponseBody
	public Map<String,Object> activeDetail(String name) throws Exception{ 
		return ContextUtil.activeDetail(name);
	}

	@RequestMapping("/waitingdetail")
	@ResponseBody
	public Map<String,Object> waitingdetail(String name) throws Exception { 
		return ContextUtil.waitingdetail(name);
	}

	@RequestMapping("/saveCostLevel")
	@ResponseBody
	public Map<String,Object> saveCostLevel(String name, String levelStr, String saveDay, String date) throws Exception { 
		return ContextUtil.saveCostLevel(name, levelStr, saveDay, date);
	}

	@RequestMapping("/changeDate")
	@ResponseBody
	public Map<String,Object> changeDate(String name, String date) throws Exception { 
		return ContextUtil.changeDate(name, date);
	}

	@RequestMapping("/dataDownload")
	@ResponseBody
	public Map<String,Object> dataDownload(String name, HttpServletResponse resp) throws Exception{ 
		ContextUtil.dataDownload(name, resp); 
		return null;
	}

}
